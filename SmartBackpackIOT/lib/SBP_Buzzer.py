import time
import grovepi

class SBP_Buzzer:
    def __init__(self,buzzer,enable):
        self.buzzer = buzzer
        self.enable = enable
        grovepi.pinMode(buzzer,"OUTPUT")

    def on(self):
        print("BUZZED")
        grovepi.digitalWrite(self.buzzer,self.enable)

    def off(self):
        grovepi.digitalWrite(self.buzzer,0)

    def buzzForSeconds(self,seconds):
        try:
            self.on()
            time.sleep(seconds)
            self.off()

        except KeyboardInterrupt:
            self.off()
        except IOError:
            print ("Error")