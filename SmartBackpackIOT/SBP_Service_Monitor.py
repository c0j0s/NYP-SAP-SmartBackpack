import subprocess
import time
import re
import os

def main():
    while True:
        try:
            sensor_service = 'SBP_Sensor_Server.service'
            bt_service = 'SBP_BT_Server.service'

            sensor_service_status = filterOutput(subprocess.getoutput(['sudo systemctl status ' + str(sensor_service)]).split("\n")[2])
            bt_service_status = filterOutput(subprocess.getoutput(['sudo systemctl status ' + str(bt_service)]).split("\n")[2])
            
            print("sensor_service_status: " + str(sensor_service_status))
            if not sensor_service_status:
                restartService('sensor')

            print("bt_service_status: " + str(bt_service_status))
            if not bt_service_status:
                restartService('bt')

            time.sleep(60)
        except KeyboardInterrupt:
            exit(0)
    
def filterOutput(status):
    #SAMPLE: Active: active (running) since Wed 2018-11-14 19:44:10 +08; 56min ago
    matchObj = re.search( r'\(.*\)', status, re.M|re.I).group()
    if "(running)" in matchObj:
        return True
    else:
        return False

def restartService(servicename):
    if servicename is 'sensor':
        service = 'SBP_Sensor_Server.service'
    elif servicename is 'bt':
        service = 'SBP_BT_Server.service'
    else:
        service = servicename

    print("\n   " + service + " is not running: trying to reboot")

    return_code = os.system("sudo systemctl restart " + str(service))
    if(return_code is 0):
        time.sleep(5)
        status = subprocess.getoutput(['sudo systemctl status SBP_Sensor_Server.service']).split("\n")[2]
    else:
        status = "Fail to reboot sensor service, will retry again later"

    print(status + "\n")

if __name__ == '__main__':
    main()