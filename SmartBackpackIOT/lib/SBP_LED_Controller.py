from lib.SBP_LED import SBP_LED
import time

class SBP_LED_Controller:
    led_port_dist = None
    led_switch_interval = 1

    def __init__(self, enable, debug=False):
        self.led_port_dist = {}
        self.debug = debug
        self.enable = enable

    def toggleEnable(self,enable):
        self.enable = enable
        for key, led in self.led_port_dist.items():
            led.toggleEnable(enable)

    def set_LED_switch_interval(self,seconds = 1):
        self.led_switch_interval = seconds

    def addLED(self,name,port):
        self.led_port_dist[name] = SBP_LED(port,int(self.enable),self.debug)

    def getLED(self,name):
        return self.led_port_dist[name]

    def litSingleLED(self,name):
        print("[SBP_LED_Controller] litSingleLED enable state " + str(self.enable))
        if self.enable != 0:
            for key, led in self.led_port_dist.items():
                if key == name:
                    led.on()
                else:
                    led.off()
                time.sleep(self.led_switch_interval)
    
    def litMultipleLED(self,namelist):
        if self.enable != 0:
            for key, led in self.led_port_dist.items():
                if key in namelist:
                    led.on()
                else:
                    led.off()
                time.sleep(self.led_switch_interval)

    def offAllLED(self):
        if self.enable != 0:
            for key, led in self.led_port_dist.items():
                led.off()
                time.sleep(self.led_switch_interval)
            
    def updateAllLEDState(self):
        for key, led in self.led_port_dist.items():
            if led.isLit == 1:
                if self.enable == 1:
                    led.on()
                else:
                    led.off()
            time.sleep(self.led_switch_interval/2)

