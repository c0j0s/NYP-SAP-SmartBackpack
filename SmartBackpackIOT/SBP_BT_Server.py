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
                command.changeDeviceConfigs(config_file)
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
        if killer.kill_now:
            break

        main()