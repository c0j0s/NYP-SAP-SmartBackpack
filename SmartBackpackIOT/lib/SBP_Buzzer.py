import time
import grovepi

class SBP_Buzzer:
    def __init__(self,buzzer,enable,debug=False):
        self.buzzer = buzzer
        self.enable = enable
        self.debug = debug
        grovepi.pinMode(buzzer,"OUTPUT")

    def toggleEnable(self,enable):
        self.enable = enable

    def on(self):
        if self.debug:
            print("[BUZZER] on: mode=" + str(self.enable))
        grovepi.digitalWrite(self.buzzer,int(self.enable))

    def off(self):
        if self.debug:
            print("[BUZZER] off: mode=" + int(self.enable))
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