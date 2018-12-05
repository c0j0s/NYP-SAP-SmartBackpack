#!/usr/bin/python3
#SBP libraries
from lib.SBP_Redis_Wrapper import SBP_Redis_Wrapper
from lib.Common import *
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

def init():
    print("Initialising BT Server")

    global redis_cursor
    global debug
    global clear_holding_zone_after_sync

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

# Main loop
def main():

    killer = GracefulKiller()

    # wait until Bluetooth init is done
    time.sleep(10)

    # Make device visible
    os.system("sudo hciconfig hci0 piscan")

    # Create a new server socket using RFCOMM protocol
    server_sock = BluetoothSocket(RFCOMM)
    # Bind to any port
    server_sock.bind(("", PORT_ANY))
    # Start listening
    server_sock.listen(1)

    # Get the port the server socket is listening
    port = server_sock.getsockname()[1]

    # The service UUID to advertise
    uuid = "7be1fcb3-5776-42fb-91fd-2ee7b5bbb86d"

    # Start advertising the service
    advertise_service(server_sock, "SmartBackpackIOT",
                       service_id=uuid,
                       service_classes=[uuid, SERIAL_PORT_CLASS],
                       profiles=[SERIAL_PORT_PROFILE])

    operations = ["ping", "example"]

    print("Startup complete waiting for connection")

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

            print("Received [%s]" % data)

            result = {}

            # Handle the request
            if data == "getop":
                result['msg'] = "op:%s" % ",".join(operations)
            elif data == "ping":
                result['msg'] = "Pong"
            elif data == "get_sensor_status":
                result = get_service_status('sensor')
            elif data == "get_bt_status":
                result = get_service_status('bt')
            elif data == "get_sensor_data":
                result = get_sensor_data()
            elif data == "sync_holding_zone":
                result = sync_holding_zone()
            elif data == "cmd_reboot_now":
                cmd_reboot_now(client_sock)
            elif data == "cmd_reboot_sensor_server":
                cmd_reboot_sensor_server(client_sock)
            elif data == "cmd_reboot_bt_server":
                cmd_reboot_bt_server(client_sock)
            elif data == "cmd_disconnect":
                if client_sock is not None:
                    client_sock.close()
            elif "sh_" in data :
                sh_execute_command(client_sock,data)
            else:
                result['msg'] = "Not supported"

            if result is not {}:
                response = json.dumps(result)
                client_sock.send(response)
                print("Sent back : %s" % response)
            
        except IOError: 
            closing(client_sock,msg="IOError ")
            break
        except KeyboardInterrupt:
            closing(client_sock,msg="Server going down")
            break
        except:
            closing(client_sock,msg="Error in main loop")
            break

def get_service_status(servicename):
    result = {}
    if servicename is 'sensor':
        service = 'SBP_Sensor_Server.service'
    elif servicename is 'bt':
        service = 'SBP_BT_Server.service'
    else:
        service = servicename

    result['msg'] = subprocess.getoutput(['sudo systemctl status ' + str(service)]).split("\n")[2]
    return result

def get_sensor_data():
    result = {}
    result['hum'] = redis_cursor.get('hum')
    result['temp'] = redis_cursor.get('temp')
    result['pm2_5'] = redis_cursor.get('pm2_5')
    result['pm10'] = redis_cursor.get('pm10')
    return result

def sync_holding_zone():
    result = {}
    with open("holding_zone","r+") as f:
        lines = [line.rstrip('\n') for line in f]
        result['data'] = lines

        if clear_holding_zone_after_sync is 1:
            f.truncate(0)
        f.close()

    return result

def cmd_reboot_now(client_sock):
    client_sock.send("{'msg':'System rebooting now, reconnect after reboot'}")
    os.system("sudo reboot")

def cmd_reboot_sensor_server(client_sock):
    """
    admin function
    """
    client_sock.send("{'msg':'Restarting Sensor Service'}")
    print("Sent back : {'msg':'Restarting Sensor Service'}")

    try:
        return_code = os.system("sudo systemctl restart SBP_Sensor_Server.service")
        if(return_code is 0):
            time.sleep(5)
            status = subprocess.getoutput(['sudo systemctl status SBP_Sensor_Server.service']).split("\n")[2]
        else:
            status = "Fail to reboot sensor service"

        print("Sent back : %s" % status)
        client_sock.send("{'msg':'"+str(status)+"'}")
    except subprocess.CalledProcessError as err:
        print(err)


def cmd_reboot_bt_server(client_sock):
    """
    NOT WORKING, PENDING FOR REVIEW
    admin function
    """
    client_sock.send("{'msg':'Restarting BT Service'}")
    print("Sent back : {'msg':'Restarting BT Service'}")
    try:
        os.execv(os.path.dirname(__file__) + "/SBP_BT_Server.py","")
    except Exception as err:
        print(err)

def sh_execute_command(client_sock,data):
    """
    admin function
    """
    command = data.replace("sh_","")
    client_sock.send("\r\nExecuting: " + command)
    print("Sent back : %s" % command)
    output = subprocess.getoutput([str(command)])

    if output is not None:
        status = output
    else:
        status = "Fail to execute command"
    
    client_sock.send("\r\n" + status)
    print("Sent back : %s" % status)

def closing(client_sock,msg="Closing",exception=""):
    print(msg + ": " + str(exception))
    if client_sock is not None:
        client_sock.close()

    if debug:
        print("-"*60)
        traceback.print_exc(file=sys.stdout)
        print("-"*60)

if __name__ == '__main__':
    init()
    main()