import time
import grovepi

class SBP_LED:
    def __init__(self,LED):
        self.LED = LED
        grovepi.pinMode(LED,"OUTPUT")
    
    def on(self):
        #print("LIT " + str(self.LED))
        grovepi.digitalWrite(self.LED,1)

    def off(self):
        #print("OFF " + str(self.LED))
        grovepi.digitalWrite(self.LED,0)

    def litForSeconds(self,seconds):
        try:
            self.on()
            time.sleep(seconds)
            self.off()

        except KeyboardInterrupt:
            self.off()
        except IOError:
            print ("Error")
    
