from SBP_LED import SBP_LED
import time

class SBP_LED_Controller:
    led_port_dist = None
    led_switch_interval = 1

    def __init__(self):
        self.led_port_dist = {}

    def __init__(self,led_port_dist):
        self.led_port_dist = led_port_dist
        self.init()
    
    def init(self):
        for key,val in self.led_port_dist.items():
            self.addLED(key,val)

    def addLED(name,port):
        self.led_port_dist[name] = SBP_LED(port)

    def litSingleLED(name):
        for key,val in self.led_port_dist.items():
            if key is name:
                val.on()
            else:
                val.off
            time.sleep(self.led_switch_interval)
    
    def litMultipleLED(namelist)
        for key,val in self.led_port_dist.items():
            if key in namelist:
                val.on()
            else:
                val.off
            time.sleep(self.led_switch_interval)
            

