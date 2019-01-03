import time,sys
import smbus
import RPi.GPIO as GPIO

class SBP_Display:
    # this device has two I2C addresses
    DISPLAY_RGB_ADDR = 0x62
    DISPLAY_TEXT_ADDR = 0x3e

    def __init__(self, debug=False):
        self.bus = smbus.SMBus(1)

    # set backlight to (R,G,B) (values from 0..255 for each)
    def setDisplayRGB(self,r,g,b):
        try:
            self.bus.write_byte_data(self.DISPLAY_RGB_ADDR,0,0)
            self.bus.write_byte_data(self.DISPLAY_RGB_ADDR,1,0)
            self.bus.write_byte_data(self.DISPLAY_RGB_ADDR,0x08,0xaa)
            self.bus.write_byte_data(self.DISPLAY_RGB_ADDR,4,r)
            self.bus.write_byte_data(self.DISPLAY_RGB_ADDR,3,g)
            self.bus.write_byte_data(self.DISPLAY_RGB_ADDR,2,b)
        except:
            print("[DISPLAY] falal: Display bus write_byte_data() error in setDisplayRGB")
            pass

    # send command to display (no need for external use)    
    def textCommand(self,cmd):
        try:
            self.bus.write_byte_data(self.DISPLAY_TEXT_ADDR,0x80,cmd)
        except:
            print("[DISPLAY] falal: Display bus write_byte_data() error in textCommand")
            pass

    # set display text \n for second line(or auto wrap)     
    def setDisplayText(self,text):
        self.textCommand(0x01) # clear display
        time.sleep(.05)
        self.textCommand(0x08 | 0x04) # display on, no cursor
        self.textCommand(0x28) # 2 lines
        time.sleep(.05)
        count = 0
        row = 0
        for c in text:
            if c == '\n' or count == 16:
                count = 0
                row += 1
                if row == 2:
                    break
                self.textCommand(0xc0)
                if c == '\n':
                    continue
            count += 1
            self.bus.write_byte_data(self.DISPLAY_TEXT_ADDR,0x40,ord(c))

    #Update the display without erasing the display
    def setDisplayText_noRefresh(self,text):
        self.textCommand(0x02) # return home
        time.sleep(.05)
        self.textCommand(0x08 | 0x04) # display on, no cursor
        self.textCommand(0x28) # 2 lines
        time.sleep(.05)
        count = 0
        row = 0
        while len(text) < 32: #clears the rest of the screen
            text += ' '
        for c in text:
            if c == '\n' or count == 16:
                count = 0
                row += 1
                if row == 2:
                    break
                self.textCommand(0xc0)
                if c == '\n':
                    continue
            count += 1
            self.bus.write_byte_data(self.DISPLAY_TEXT_ADDR,0x40,ord(c))

    def setDisplayOn(self):
        self.setDisplayRGB(0,255,0)

    def setDisplayOff(self):
        self.setDisplayRGB(0,0,0)
        self.setDisplayText("")