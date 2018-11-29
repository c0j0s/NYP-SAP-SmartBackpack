#device
import lib.HPMA115S0 
from lib.Display import *
from lib.SBP_Buzzer import SBP_Buzzer
from lib.SBP_LED import SBP_LED
from lib.DataUtils import *
import serial
import grovepi
#utils
import subprocess
import math
import json,csv
import MySQLdb as mariadb
import sys, traceback
#datetime
import time
import datetime
from datetime import date
from time import sleep

debug = True
config_file = "./config.json"
temp_hum_data_file = "./src/temp_hum_data.csv"
testing_button_triggered = 0

def init():
    try:
        #init global variables
        global pm_sensor
        global temp_hum
        global button

        global led_green
        global led_blue
        global led_red
        global buzzer

        global cursor

        #load config files
        with open(config_file) as f:
            config = json.load(f)
            
        sensor_path = config['device']['sensors']
        actuator_path = config['device']['actuators']
        mysql_path = config['env']['mysql']
        hana_path = config['env']['HANA']
        custom_settings = config['settings']

        #init sensors
        pm_sensor = int(sensor_path['particle'])
        temp_hum = int(sensor_path['temp_hum'])
        button = int(sensor_path['button'])

        #init actuators
        led_green_port = int(actuator_path['led_green'])
        led_blue_port = int(actuator_path['led_blue'])
        led_red_port = int(actuator_path['led_red'])
        buzzer_port = int(actuator_path['buzzer'])

        led_green = SBP_LED(led_green_port)
        led_blue = SBP_LED(led_blue_port)
        led_red = SBP_LED(led_red_port)
        buzzer = SBP_Buzzer(buzzer_port,custom_settings['buzzer'])

        #init grovepi
        grovepi.pinMode(button,"INPUT")

        #init mysql
        user = mysql_path['user']
        passwd = mysql_path['password']
        host = mysql_path['host']
        database = mysql_path['database']

        mysql = mariadb.connect(user=user, password=passwd, host=host, database=database)
        cursor = mysql.cursor()
        
        #turn on screen
        setDisplayOn()

        #debug log
        if debug:
            print("DEVICE INITIALISATION")
            print("pm_sensor: " + str(pm_sensor))
            print("temp_hum: " + str(temp_hum))
            print("led_green: " + str(led_green_port))
            print("led_blue: " + str(led_blue_port))
            print("led_red: " + str(led_red_port))
            print("buzzer: " + str(buzzer_port))

            print("ENVIRONMENT INITIALISATION")
            print("mysql connection: {} {} {}".format(host,user,passwd))

    except Exception as ex:
        print("Initialisation error: " + str(ex))
        print("Exception in user code:")
        print("-"*60)
        traceback.print_exc(file=sys.stdout)
        print("-"*60)

def initTesting():
    global testing_button_triggered
    if testing_button_triggered is 0:
        print("Start Testing Routine")
        setDisplayText("Start Testing \nRoutine")

        buzzer.buzzForSeconds(1)
        time.sleep(1)
        led_green.litForSeconds(1)
        time.sleep(1)
        led_blue.litForSeconds(1)
        time.sleep(1)
        led_red.litForSeconds(1)
        time.sleep(1)

        setDisplayText("End")
        testing_button_triggered = 1
        print("End Testing Routine")
        time.sleep(2)


def main():
    print("""
                    =============
                    SMARTBACKPACK
                    version:0.0.1
                    =============
    """)

    #load temp hum data
    with open(temp_hum_data_file) as csvfile:
        tempHumData = list(csv.reader(csvfile, delimiter=","))
        print(temp_hum_data_file + " loaded")

    #start loop
    while True:
        try:
            #listen for testing mode
            if grovepi.digitalRead(button) is 1:
                initTesting()

            #if not in testing mode
            if testing_button_triggered is not 0:
                [temp,hum] = grovepi.dht(temp_hum,0)  
                if math.isnan(temp) == False and math.isnan(hum) == False:
                    setDisplayText_noRefresh("temp: %.02f C \nhumidity: %.02f%%"%(temp, hum))

                    for index, row in enumerate(tempHumData):
                        y = getTempByRange(temp)
                        if index is y:
                            x = getHumByRange(hum)
                            level = row[x]
                            desc = getComfortLevel(level) 
                            print("desc:{} x:{} y:{} h:{} t:{}".format(desc,x,y,hum,temp))

                light_level = getLeveByHum(hum)
                if light_level is 2:
                    print("red light shall lit")
                    led_blue.off()
                    led_green.off()
                    time.sleep(1)
                    led_red.on()
                elif light_level is 1:
                    print("blue light shall lit")
                    led_red.off()
                    led_green.off()
                    time.sleep(1)
                    led_blue.on()
                elif light_level is 0:
                    print("green light shall lit")
                    led_red.off()
                    led_blue.off()
                    time.sleep(1)
                    led_green.on()

            time.sleep(5)
        except KeyboardInterrupt:
            #clean up devices
            setDisplayOff()
            led_red.off()
            led_blue.off()
            led_green.off()
            print ("Keyboard interrupted")
            break
        except IOError:
            print ("IO Error")
            print("Exception in user code:")
            print("-"*60)
            traceback.print_exc(file=sys.stdout)
            print("-"*60)
            break
        except Exception as ex:
            print("Exception in user code:")
            print("-"*60)
            traceback.print_exc(file=sys.stdout)
            print("-"*60)
            break
        

if __name__ == '__main__':
    if debug:
        init()
    main()