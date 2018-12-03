import time
import grovepi

class SBP_LED:
    def __init__(self, LED, debug=False):
        self.LED = LED
        self.debug = debug
        grovepi.pinMode(LED,"OUTPUT")
    
    def on(self):
        if self.debug:
            print("[LED] on: " + str(self.LED))
        grovepi.digitalWrite(self.LED,1)

    def off(self):
        if self.debug:
            print("[LED] off: " + str(self.LED))
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
    
