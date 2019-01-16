import json
import time
import os
import subprocess
class BtCommandObject:
    def newCommandObject(self, function_code, data, end_code, debug = "",debug_mode=False):
        self.function_code = function_code
        self.data = data
        self.end_code = end_code
        if debug_mode:
            self.debug = debug
        else:
            self.debug = ""
    
    def convertToCommandObject(self,raw_data):
        obj = json.loads(raw_data)
        self.function_code = obj["function_code"]
        self.data = obj["data"]
        self.end_code = obj["end_code"]
        self.debug = obj["debug"]

    def toJson(self):
        json = {
            'function_code': self.function_code,
            'data' : self.data,
            'end_code' : self.end_code,
            'debug' : self.debug
        }
        return json

class SBP_BT_Command_Manager:

    def __init__(self,client_sock,original_received,debug=False):
        self.client = client_sock
        self.debug = debug
        self.command = original_received

    def get_service_status(self,servicename):
        if servicename is 'sensor':
            service = 'SBP_Sensor_Server.service'
        elif servicename is 'bt':
            service = 'SBP_BT_Server.service'
        else:
            service = 'SBP_Service_Monitor.service'

        output = {
            'status': subprocess.getoutput(['sudo systemctl status ' + str(service)]).split("\n")[2]
        }
        self.client.send(self.toBTObject(self.command.function_code,output,"EOT"))

    def get_sensor_data(self,redis_cursor):
        output = {
            'HUMIDITY': redis_cursor.get('hum'),
            'TEMPERATURE': redis_cursor.get('temp'),
            'PM2_5': redis_cursor.get('pm2_5'),
            'PM10': redis_cursor.get('pm10')
        }
        self.client.send(self.toBTObject(self.command.function_code,output,"EOT"))

    def restart_sensor_service(self):
        """
        admin function
        """
        output = {
            'message':'Restarting Sensor Service'
        }
        self.client.send(self.toBTObject(self.command.function_code,output,"MSE"))
        print("Sent back : " + str(BtCommandObject(self.command.function_code,output,"MSE").toJson()))

        try:
            return_code = os.system("sudo systemctl restart SBP_Sensor_Server.service")
            if(return_code is 0):
                time.sleep(5)
                status = subprocess.getoutput(['sudo systemctl status SBP_Sensor_Server.service']).split("\n")[2]
            else:
                status = "Fail to reboot sensor service"

            print("Sent back : %s" % status)

            output = {
                'status': status
            }
            self.client.send(self.toBTObject(self.command.function_code,output,"EOT"))
        except subprocess.CalledProcessError as err:
            print(err)
            pass

    def restart_bt_service(self):
        exit(0)

    def sync_holding_zone(self):
        pass

    def restart_device(self):
        output = {
            'message':'System rebooting now, reconnect after reboot'
        }
        self.client.send(self.toBTObject(self.command.function_code,output,"EOT"))
        time.sleep(1)
        os.system("sudo reboot")

    def toggle_debug(self,config_file):

        with open(config_file) as f:
            config = json.load(f)
            localdebug = config['settings']['debug']
            if localdebug:
                config["settings"]["debug"] = False
            else:
                config["settings"]["debug"] = True
            f.close()
        with open(config_file,'w',encoding='utf-8') as outfile:
            json.dump(config, outfile, ensure_ascii=False,indent = 4)

        output = {
            'message':'Debug mode for sensor server changed to: ' + str(config["settings"]["debug"])
        }
        self.client.send(self.toBTObject(self.command.function_code,output,"EOT"))

    def sh_execute_command(self,command):
        pass

    def message(self,message):
        output = {
            'message':message
        }
        self.client.send(self.toBTObject(self.command.function_code,output,"EOT"))

    def toBTObject(self,function_code,data,end_code,debug=""):
        result = {}
        result["function_code"] = function_code
        result["data"] = data
        result["end_code"] = end_code

        if debug and debug is not "":
            result["debug"] = debug
        
        return json.dumps(result)
    