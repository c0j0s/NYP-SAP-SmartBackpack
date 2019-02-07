import time
import grovepi

class SBP_LED:
    def __init__(self, LED, enable, debug=False):
        self.LED = LED
        self.debug = debug
        self.enable = enable
        self.isLit = 0
        grovepi.pinMode(LED,"OUTPUT")
    
    def toggleEnable(self,enable):
        self.enable = int(enable)

    def on(self):
        if self.debug:
            print("[LED] on: " + str(self.LED) + " mode:" + str(self.enable))

        print("[LED] on: " + str(self.LED) + " mode:" + str(self.enable))
        self.isLit = self.enable
        grovepi.digitalWrite(self.LED,int(self.enable))

    def off(self):
        if self.debug:
            print("[LED] off: " + str(self.LED) + " mode:" + str(self.enable))
        self.isLit = 0
        grovepi.digitalWrite(self.LED,0)

    def litForSeconds(self,seconds):
        try:
            self.on()
            time.sleep(seconds)
            self.off()

        except KeyboardInterrupt:
            self.off()
        except IOError:
            if self.debug:
                print("[LED] litForSeconds(IOError): " + str(self.LED))
    
