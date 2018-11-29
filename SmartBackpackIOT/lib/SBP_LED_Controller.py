from lib.SBP_LED import SBP_LED
import time

class SBP_LED_Controller:
    led_port_dist = None
    led_switch_interval = 1

    def __init__(self):
        self.led_port_dist = {}

    def set_LED_switch_interval(self,seconds = 1):
        self.led_switch_interval = seconds

    def addLED(self,name,port):
        self.led_port_dist[name] = SBP_LED(port)

    def getLED(self,name):
        return self.led_port_dist[name]

    def litSingleLED(self,name):
        for key,val in self.led_port_dist.items():
            if key is name:
                val.on()
            else:
                val.off()
            time.sleep(self.led_switch_interval)
    
    def litMultipleLED(self,namelist):
        for key,val in self.led_port_dist.items():
            if key in namelist:
                val.on()
            else:
                val.off()
            time.sleep(self.led_switch_interval)

    def offAllLED(self):
        for key,val in self.led_port_dist.items():
                val.off()
            

