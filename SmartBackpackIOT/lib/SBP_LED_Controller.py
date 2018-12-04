from lib.SBP_LED import SBP_LED
import time

class SBP_LED_Controller:
    led_port_dist = None
    led_switch_interval = 1

    def __init__(self, debug=False):
        self.led_port_dist = {}
        self.debug = debug

    def set_LED_switch_interval(self,seconds = 1):
        self.led_switch_interval = seconds

    def addLED(self,name,port):
        self.led_port_dist[name] = SBP_LED(port,self.debug)

    def getLED(self,name):
        return self.led_port_dist[name]

    def litSingleLED(self,name):
        for key, led in self.led_port_dist.items():
            if key is name:
                led.on()
            else:
                led.off()
            time.sleep(self.led_switch_interval)
    
    def litMultipleLED(self,namelist):
        for key, led in self.led_port_dist.items():
            if key in namelist:
                led.on()
            else:
                led.off()
            time.sleep(self.led_switch_interval)

    def offAllLED(self):
        for key, led in self.led_port_dist.items():
            led.off()
            time.sleep(self.led_switch_interval)
            

