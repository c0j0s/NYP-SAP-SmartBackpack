#!/usr/bin/python3
#SBP libraries
from lib.HPMA115S0 import HPMA115S0
from lib.SBP_Display import SBP_Display
from lib.SBP_Buzzer import SBP_Buzzer
from lib.SBP_LED_Controller import SBP_LED_Controller
from lib.SBP_Redis_Wrapper import SBP_Redis_Wrapper
from lib.Common import *
#device
import serial
import grovepi
import redis
#utils
import subprocess
import math
import json,csv
import sys, traceback, os
import signal
import socket
#datetime
import time
import datetime

debug = False
config_file = "./config.json"
temp_hum_data_file = "./src/temp_hum_data.csv"
testing_button_triggered = 0

def init():
    try:
        #init global variables
        global pm_sensor
        global temp_hum
        global button
        global pm_sensor

        global led_controller
        global buzzer
        global display

        global redis_cursor

        global killer
        global debug

        global minute_to_record_data
        global seconds_to_update_data
        global countdown_to_record_data

        #load config files
        try:
            with open(config_file) as f:
                config = json.load(f)
        except:
            print("[INIT] Fail to load configs")
            exit(0)
            
        sensor_path = config['device']['sensors']
        actuator_path = config['device']['actuators']
        redis_path = config['env']['redis']
        debug = config['settings']['debug']
        sensor_server_settings = config['settings']['Sensor_Server_Settings']

        #init sensors
        pm_sensor_port = sensor_path['particle']
        pm_sensor = HPMA115S0(pm_sensor_port,debug=debug)
        pm_sensor.init()
        pm_sensor.startParticleMeasurement()
        
        #start taking the readings
        pm_sensor.readParticleMeasurement()

        temp_hum = int(sensor_path['temp_hum'])

        button = int(sensor_path['button'])
        grovepi.pinMode(button,"INPUT")

        #init actuators
        led_green_port = int(actuator_path['led_green'])
        led_blue_port = int(actuator_path['led_blue'])
        led_red_port = int(actuator_path['led_red'])
        buzzer_port = int(actuator_path['buzzer'])

        led_controller = SBP_LED_Controller(debug=debug)
        led_controller.addLED('green',led_green_port)
        led_controller.addLED('blue',led_blue_port)
        led_controller.addLED('red',led_red_port)

        #load custom settings
        buzzer = SBP_Buzzer(buzzer_port,sensor_server_settings['enable_buzzer'],debug=debug)

        minute_to_record_data = sensor_server_settings['minute_to_record_data']
        seconds_to_update_data = sensor_server_settings['seconds_to_update_data']
        countdown_to_record_data = calIntervalNeeded(minute_to_record_data,seconds_to_update_data)

        #init redis
        host = redis_path['host']
        redis_cursor = SBP_Redis_Wrapper(host,debug=debug)

        #init killer
        killer = GracefulKiller()

        #start display
        display = SBP_Display()
        display.setDisplayOn()

        if debug:
            display.setDisplayText("[Debug mode]\nWait for network")
            #wait for network to connect
            time.sleep(3)
            #print ip on boot
            try:
                ip = (([ip for ip in socket.gethostbyname_ex(socket.gethostname())[2] if not ip.startswith("127.")] or [[(s.connect(("8.8.8.8", 53)), s.getsockname()[0], s.close()) for s in [socket.socket(socket.AF_INET, socket.SOCK_DGRAM)]][0][1]]) + ["no IP found"])[0]
            except:
                ip = "Cant get IP"

            display.setDisplayText("[Debug mode]\n" + str(ip))
            time.sleep(5)
            display.setDisplayText_noRefresh("[Debug mode]\nPress the btn >")

            #debug log
            print("\n[SENSOR] INIT: DEVICE INITIALISATION")
            print("\tpm_sensor: " + pm_sensor_port)
            print("\ttemp_hum: " + str(temp_hum))
            print("\tled_green: " + str(led_green_port))
            print("\tled_blue: " + str(led_blue_port))
            print("\tled_red: " + str(led_red_port))
            print("\tbuzzer: " + str(buzzer_port))

            print("\n[SENSOR] INIT: ENVIRONMENT INITIALISATION")
            print("\tredis connection: {}".format(host))

    except Exception as ex:
        print("Initialisation error: " + str(ex))
        print("Exception in user code:")
        print("-"*60)
        traceback.print_exc(file=sys.stdout)
        print("-"*60)
        #terminate itself when error and wait for monitoring service to restart
        exit(0)

def initTesting():
    global testing_button_triggered
    global button

    if testing_button_triggered is 0:
        print("[SENSOR] initTesting: Start Testing Routine")
        display.setDisplayText("Start Testing \nRoutine")

        buzzer.buzzForSeconds(1)
        time.sleep(1)
        led_controller.getLED('green').litForSeconds(1)
        time.sleep(1)
        led_controller.getLED('blue').litForSeconds(1)
        time.sleep(1)
        led_controller.getLED('red').litForSeconds(1)
        time.sleep(1)

        display.setDisplayText("End")
        testing_button_triggered = 1
        button = None
        print("[SENSOR] initTesting: End Testing Routine")
        time.sleep(2)


def main():
    global testing_button_triggered
    global countdown_to_record_data
    global button
    countdown_to_update = countdown_to_record_data
    displayAlternate = 1

    if debug:
        print("[SENSOR] main: count to write " + str(countdown_to_record_data))

    #load temp hum data
    with open(temp_hum_data_file) as csvfile:
        tempHumData = list(csv.reader(csvfile, delimiter=","))
        print(temp_hum_data_file + " loaded")

    try:
        """
        =================================================================================================================
        MAIN SERVER LOOP
        =================================================================================================================
        """
        while True:
            

            #killer to handle service stopping
            if killer.kill_now:
                closing()
                break
            
            try:
                #listen for testing mode
                if debug and button is not None:
                    if grovepi.digitalRead(button) is 1:
                        initTesting()
                else:
                    testing_button_triggered = 1
                #if not in testing mode
                if testing_button_triggered is not 0 and button is None:

                    pm2_5 = 0
                    pm10 = 0
                    temp = 0
                    hum = 0
                    desc = "ok" 
                    alert_triggered = False

                    #particle sensor
                    if (pm_sensor.readParticleMeasurement()):
                        pm2_5 = pm_sensor._pm2_5
                        pm10 = pm_sensor._pm10

                        if debug:
                            print("[HPMA115S0] PM2.5: %d ug/m3" % (pm2_5))
                            print("[HPMA115S0] PM 10: %d ug/m3" % (pm10))

                    #temp and hum sensor
                    [temp,hum] = grovepi.dht(temp_hum,0)  
                    if math.isnan(temp) == False and math.isnan(hum) == False:
                        desc = "ok"
                        for index, row in enumerate(tempHumData):
                            y = getTempByRange(temp)
                            if index is y:
                                x = getHumByRange(hum)
                                level = row[x]
                                desc = getComfortLevel(level) 
                                #print("desc:{} x:{} y:{} h:{} t:{}".format(desc,x,y,hum,temp))
                    
                    #check sensor data
                    if temp is -1 and hum is -1:
                        #sensor error disconnect power and reboot device
                        print("[Fatal] SENSOR ERROR, sensor service suiciding and wait for reboot")
                        print("[Fatal] SENSOR ERROR, disconnect power and reboot the device")
                        print("[Fatal] SENSOR ERROR, hum:{} temp:{}".format(temp,hum))
                        print("[Fatal] Stopping monitoring service to prevent service boot loop")
                        os.system("sudo systemctl stop SBP_Service_Monitor.service")
                        print("[Fatal] Exiting sensor server")
                        exit(0)

                    #store in redis
                    redis_cursor.set('hum',hum)
                    redis_cursor.set('temp',temp)
                    redis_cursor.set('pm2_5',pm2_5)
                    redis_cursor.set('pm10',pm10)

                    #handel display output
                    if pm2_5 is 0 and pm10 is 0:
                        displayAlternate = 0

                    if displayAlternate is 1:
                        displayAlternate = 0
                        display.setDisplayText("PM 2.5: %d ug/m3\nPM 10.: %d ug/m3"%(pm2_5,pm10))
                    else:
                        displayAlternate = 1
                        display.setDisplayText("T: %.02f'C \nH: %.02f%% %s"%(temp, hum, desc))

                    #prevent zero values triggering false alarm during initialisation
                    if pm2_5 is not 0 and pm10 is not 0 and temp is not 0 and hum is not 0:
                        #handles led light
                        light_level = getLeveByHum(hum)
                        if light_level is 2:
                            led_controller.litSingleLED('red')
                            buzzer.buzzForSeconds(1)
                            alert_triggered = True
                        elif light_level is 1:
                            led_controller.litSingleLED('blue')
                        elif light_level is 0:
                            led_controller.litSingleLED('green')

                        #handles holding zone
                        if countdown_to_update is 0:
                            countdown_to_update = countdown_to_record_data

                            output_data_to_hoding_file(hum,temp,pm2_5,pm10,desc,alert_triggered)
                        else:
                            countdown_to_update = countdown_to_update - 1

            except KeyboardInterrupt:
                #clean up devices
                closing()
                print ("Keyboard interrupted")
                break
            except IOError:
                print ("IO Error")
                print("Exception in user code:")
                print("-"*60)
                traceback.print_exc(file=sys.stdout)
                print("-"*60)
                break
            except:
                print("Exception in user code:")
                print("-"*60)
                traceback.print_exc(file=sys.stdout)
                print("-"*60)
                break

            time.sleep(seconds_to_update_data)

            """
            =================================================================================================================
            END
            =================================================================================================================
            """
 
    except KeyboardInterrupt:
            #clean up devices
            closing()
            print ("Keyboard interrupted")
            exit(0)
        
def output_data_to_hoding_file(hum,temp,pm2_5,pm10,predict_comfort,alert_triggered):
    try:
        today = datetime.datetime.now().strftime("%Y-%m-%d")
        filepath = "holding_zone/holding_zone_" + today
        filemode = "a"

        if not os.path.isfile(filepath):
            print("[HOLDING_ZONE] Creating File in " + filepath)
            filemode = "w"

        current = datetime.datetime.fromtimestamp(time.time()).strftime('%Y-%m-%d %H:%M:%S')

        jsonObj = {
                'RECOREDED_ON':current,
                'HUMIDITY':hum,
                'TEMPERATURE':temp,
                'PM2_5':pm2_5,
                'PM10':pm10,
                'PREDICTED_COMFORT_LEVEL':predict_comfort,
                'ALERT_TRIGGERED':alert_triggered,
            }
        if debug:
            print("[HOLDING_ZONE] Writing: "+ str(jsonObj))
            
        with open(filepath,filemode) as f:
            json.dump(jsonObj, f)
            f.write("\n")
            f.close()
    except:
        print("[HOLDING_ZONE] Exception occurred")
        print("-"*60)
        traceback.print_exc(file=sys.stdout)
        print("-"*60)
        pass

def closing():
    display.setDisplayOff()
    time.sleep(1)
    led_controller.offAllLED()
    time.sleep(1) 
    buzzer.off()

if __name__ == '__main__':
    init()
    main()