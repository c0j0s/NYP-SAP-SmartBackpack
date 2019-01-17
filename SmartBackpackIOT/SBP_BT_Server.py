#!/usr/bin/python3
#SBP libraries
from lib.SBP_Redis_Wrapper import SBP_Redis_Wrapper
from lib.Common import *
from lib.SBP_BT_Command_Manager import *
#utils
import sys, traceback
import os
import signal
import redis
import json
from bluetooth import *
import subprocess
#datetime
import time
import datetime

debug=False
config_file = "config.json"
holding_zone_file = "holding_zone"

def init():
    print("Initialising BT Server")

    global redis_cursor
    global debug
    global clear_holding_zone_after_sync
    global killer

    #load config files
    try:
        with open(config_file) as f:
            config = json.load(f)
    except:
        closing(None,msg="[INIT] Fail to load configs")
        exit(0)
        
    debug = config['settings']['debug']

    redis_path = config['env']['redis']
    host = redis_path['host']
    redis_cursor = SBP_Redis_Wrapper(host,debug=debug)
    BT_server_settings = config['settings']['BT_Server_Settings'] 
    clear_holding_zone_after_sync = BT_server_settings['clear_holding_zone_after_sync']

    
    killer = GracefulKiller()

    # wait until Bluetooth init is done
    time.sleep(5)

    # Make device visible
    os.system("sudo hciconfig hci0 piscan")

# Main loop
def main():

    print("Startup complete waiting for connection")

    # Create a new server socket using RFCOMM protocol
    server_sock = BluetoothSocket(RFCOMM)
    # Bind to any port
    server_sock.bind(("", PORT_ANY))
    # Start listening
    server_sock.listen(1)

    # Get the port the server socket is listening
    port = server_sock.getsockname()[1]

    # The service UUID to advertise
    uuid = "00001101-0000-1000-8000-00805f9b34fb"

    # Start advertising the service
    advertise_service(server_sock, "SmartBackpackIOT",
                       service_id=uuid,
                       service_classes=[uuid, SERIAL_PORT_CLASS],
                       profiles=[SERIAL_PORT_PROFILE])

    client_sock = None        
    client_sock, client_info = server_sock.accept()

    while True:

        #killer to handle service stopping
        if killer.kill_now:
            if client_sock is not None:
                client_sock.close()
            server_sock.close()
            break

        try:

            print("Accepted connection from ", client_info)

            data = client_sock.recv(1024)
            data = data.decode("utf-8").replace("\r\n","")

            if len(data) == 0:
                break

            print("Received: %s" % data)

            received = BtCommandObject() 
            received.convertToCommandObject(data)

            if debug and received is not None:
                print("===============")
                print(str(received))
                print("===============")

            # Handle the request
            command = SBP_BT_Command_Manager(client_sock,received)

            if received.function_code == "00000":
                closing(server_sock,client_sock)
            elif received.function_code == "10000":
                command.restart_device()
            elif received.function_code == "11000":
                command.restart_sensor_service()
            elif received.function_code == "11500":
                command.get_service_status('sensor')
            elif received.function_code == "12000":
                command.restart_bt_service()
            elif received.function_code == "12500":
                command.get_service_status('bt')
            elif received.function_code == "30000":
                command.get_sensor_data(redis_cursor)
            elif received.function_code == "31000":
                command.message("Function Not Implemented")
            elif received.function_code == "32000":
                command.sync_holding_zone()
            elif received.function_code == "32500":
                command.flush_holding_zone_temp(clear_holding_zone_after_sync)
            elif received.function_code == "41000":
                command.toggle_debug(config_file)
            elif received.function_code == "42000":
                command.sh_execute_command(command)
            else:
                command.message("Function Not Supported")
            
        except IOError: 
            closing(server_sock,client_sock,msg="IOError ")
            break
        except KeyboardInterrupt:
            closing(server_sock,client_sock,msg="Server going down")
            break
        except:
            closing(server_sock,client_sock,msg="Error in main loop")
            break

# def toBTObject(function_code,data,end_code,debug=""):
#     result = {}
#     result["function_code"] = function_code
#     result["data"] = data
#     result["end_code"] = end_code

#     if debug and debug is not "":
#         result["debug"] = debug
    
#     return result

# def handleBtCommand(function_code,data,end_code,debug=""):
#     switcher={
#                 "00000": closing,
#                 "10000": cmd_reboot_now,
#                 "11000": cmd_reboot_sensor_server,
#                 "11500": get_service_status("sensor"),
#                 "12000": cmd_reboot_bt_server,
#                 "12500": get_service_status("bt"),
#                 "30000": get_sensor_data,
#                 "31000": "Not Implemented",
#                 "32000": sync_holding_zone,
#                 "41000": cmd_toggle_debug,
#                 "42000": sh_execute_command,
#              }
#     func = switcher.get(function_code,lambda :'Invalid')
#     return func()

# def get_service_status(servicename):
#     result = {}
#     if servicename is 'sensor':
#         service = 'SBP_Sensor_Server.service'
#     elif servicename is 'bt':
#         service = 'SBP_BT_Server.service'
#     else:
#         service = servicename

#     result['msg'] = subprocess.getoutput(['sudo systemctl status ' + str(service)]).split("\n")[2]
#     return result

# def get_sensor_data():
#     result = {}
#     result['hum'] = redis_cursor.get('hum')
#     result['temp'] = redis_cursor.get('temp')
#     result['pm2_5'] = redis_cursor.get('pm2_5')
#     result['pm10'] = redis_cursor.get('pm10')
#     return result

# def sync_holding_zone():
#     result = {}
#     with open("holding_zone","r+") as f:
#         lines = [line.rstrip('\n') for line in f]
#         result['data'] = lines

#         if clear_holding_zone_after_sync is 1:
#             f.truncate(0)
#         f.close()

#     return result

# def cmd_reboot_now(client_sock):
#     client_sock.send("{'msg':'System rebooting now, reconnect after reboot'}")
#     os.system("sudo reboot")

# def cmd_reboot_sensor_server(client_sock):
#     """
#     admin function
#     """
#     client_sock.send("{'msg':'Restarting Sensor Service'}")
#     print("Sent back : {'msg':'Restarting Sensor Service'}")

#     try:
#         return_code = os.system("sudo systemctl restart SBP_Sensor_Server.service")
#         if(return_code is 0):
#             time.sleep(5)
#             status = subprocess.getoutput(['sudo systemctl status SBP_Sensor_Server.service']).split("\n")[2]
#         else:
#             status = "Fail to reboot sensor service"

#         print("Sent back : %s" % status)
#         client_sock.send("{'msg':'"+str(status)+"'}")
#     except subprocess.CalledProcessError as err:
#         print(err)


# def cmd_reboot_bt_server(client_sock):
#     """
#     NOT WORKING, PENDING FOR REVIEW
#     admin function
#     """
#     client_sock.send("{'msg':'Restarting BT Service'}")
#     print("Sent back : {'msg':'Restarting BT Service'}")
#     try:
#         os.execv(os.path.dirname(__file__) + "/SBP_BT_Server.py","")
#     except Exception as err:
#         print(err)

# def cmd_toggle_debug():
#     with open(config_file) as f:
#         config = json.load(f)
#         localdebug = config['settings']['debug']
#         if localdebug:
#             config["settings"]["debug"] = False
#         else:
#             config["settings"]["debug"] = True
#         f.close()
#     with open(config_file,'w',encoding='utf-8') as outfile:
#         json.dump(config, outfile, ensure_ascii=False,indent = 4)
#     return "{'msg':'Debug mode for sensor server changed to: " + str(config["settings"]["debug"]) +"'}"

# def sh_execute_command(client_sock,data):
#     """
#     admin function
#     """
#     command = data.replace("sh_","")
#     client_sock.send("\r\nExecuting: " + command)
#     print("Sent back : %s" % command)
#     output = subprocess.getoutput([str(command)])

#     if output is not None:
#         status = output
#     else:
#         status = "Fail to execute command"
    
#     client_sock.send("\r\n" + status)
#     print("Sent back : %s" % status)

def closing(server_sock,client_sock,msg="Closing",exception=""):
    client_sock.close()
    server_sock.close()

    if debug:
        print("-"*60)
        traceback.print_exc(file=sys.stdout)
        print("-"*60)

if __name__ == '__main__':
    init()
    while True:
        main()