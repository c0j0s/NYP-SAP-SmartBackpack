import json
import time
import os
import subprocess
import redis

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
        try:
            obj = json.loads(raw_data)
            self.function_code = obj["function_code"]
            self.data = obj["data"]
            self.end_code = obj["end_code"]
            self.debug = obj["debug"]
        except:
            self.debug = ""
            pass

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
        self.client.send(self.toBTObject(self.command.function_code,output,"EOT"))
        print("Sent back : " + str(BtCommandObject(self.command.function_code,output,"EOT").toJson()))

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
        holding_zone_dir = "holding_zone/"
        holding_zone_files = os.listdir(holding_zone_dir)
        files_renamed = []
        files_transmitted = []

        if len(holding_zone_files) == 0:
            end_index = len(holding_zone_files) - 1
            end_code = "MSE"
            last_line = ""

            for fidx, holding_zone_file in enumerate(holding_zone_files):
                #rename files
                if holding_zone_file.startswith("holding_zone_"):
                    oldfile = holding_zone_dir + holding_zone_file
                    newfile = oldfile
                    if not holding_zone_file.endswith(".temp"):
                        newfile = oldfile + ".temp"
                        os.rename(oldfile, newfile)
                    files_renamed.append(newfile)
                    
            print("[sync_holding_zone] File renamed: " + str(files_renamed))

            for fidx, holding_zone_file in enumerate(files_renamed):
                print(str(holding_zone_file))
                if holding_zone_file.startswith(holding_zone_dir + "holding_zone_"):
                    if end_index is fidx:
                        with open(holding_zone_file,"r+") as lc:
                            last_line = self.toCompactString(list(lc)[-1])
                            lc.close()
                    
                    with open(holding_zone_file,"r+") as f:
                        
                        for line in f:
                            output = self.toCompactString(line)

                            if output == last_line:
                                end_code = "EOT"

                            print(str(output) + " l:" + str(last_line))
                            self.client.send(self.toBTObject(self.command.function_code,output,end_code))
                            time.sleep(0.05)

                    files_transmitted.append(holding_zone_file)

            print("[sync_holding_zone] File transmitted: " + str(files_renamed))
        else:
            self.client.send(self.toBTObject(self.command.function_code,{},"EOT"))


    def flush_holding_zone_temp(self,clear_holding_zone_after_sync):
        holding_zone_dir = "holding_zone/"
        holding_zone_files = os.listdir(holding_zone_dir)
        files_cleared = []

        if clear_holding_zone_after_sync is 1:
            for fidx, holding_zone_file in enumerate(holding_zone_files):
                if holding_zone_file.endswith(".temp"):
                    os.remove(holding_zone_dir + holding_zone_file)
                    files_cleared.append(holding_zone_file)

        output = {
            "message":"Holding zone temp files cleared"
        }
        
        debug_content = ""

        if self.debug:
            debug_content = "Files cleared: " + str(files_cleared)

        self.client.send(self.toBTObject(self.command.function_code,output,"EOT",debug_content))

    def restart_device(self):
        output = {
            'message':'System rebooting now, reconnect after reboot'
        }
        self.client.send(self.toBTObject(self.command.function_code,output,"EOT"))
        time.sleep(1)
        os.system("sudo reboot")

    def changeDeviceConfigs(self,config_file,redis_cursor):

        with open(config_file) as f:
            config = json.load(f)
            sensor_config_path = config['settings']['Sensor_Server_Settings']
            
            for key, val in self.command.data.items():
                changeVal = 0
                if val == "true":
                    changeVal = 1
                elif val == "false":
                    changeVal = 0
                else:
                    changeVal = int(val)

                redis_cursor.set(key,changeVal)
                sensor_config_path[key] = changeVal
                print("[changeDeviceConfigs] Config change for: " + key + " to: " + str(changeVal))

            f.close()

        with open(config_file,'w',encoding='utf-8') as outfile:
            json.dump(config, outfile, ensure_ascii=False,indent = 4)
            print("[changeDeviceConfigs] Config saved")

        output = {
            "message":"Config Changed"
        }
        self.client.send(self.toBTObject(self.command.function_code,output,"EOT"))

    def toggle_debug(self,config_file):
        with open(config_file) as f:
            config = json.load(f)
            config = config['settings']['debug']
            if self.debug:
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

    def toCompactString(self,raw):
        jsObj = json.loads(raw)
        mergeString = str(jsObj["HUMIDITY"]) + ";" + str(jsObj["TEMPERATURE"]) + ";" + str(jsObj["PM2_5"]) + ";" + str(jsObj["PM10"]) + ";" + str(jsObj["PREDICTED_COMFORT_LEVEL"]) + ";" + str(jsObj["ALERT_TRIGGERED"]) 
        output = {}
        output[str(jsObj["RECOREDED_ON"])] = mergeString
        return output
    