import csv
import random
from generate_data import *

#gender	age	humidity	temperature	pm2_5	pm10	is_asthmatic	comfort_level

offset = {}
offset[0] = [0,0,0,0,0,0,0,0]
offset[1] = [0,0,0,0,1,2,3,4]
offset[2] = [0,0,0,1,2,3,4,5]
offset[3] = [0,0,1,2,3,4,5,6]

def test():
    temp_hum_data_file = "temp_hum_data.csv"

    with open(temp_hum_data_file) as csvfile:
        tempHumData = list(csv.reader(csvfile, delimiter=","))
        print(temp_hum_data_file + " loaded")


    gender = int(input("gender 1/0: "))
    age = int(input("age 1-70: "))

    humidity = int(input("humidity 0-100: "))
    temperature = int(input("temperature 0-50: "))
    pm2_5 = int(input("pm2_5 0-300: "))
    pm10 = int(input("pm10 0-300: "))

    is_asthmatic = int(input("is_asthmatic 1/0: "))

    comfort_level = -1

    

    offset_weight = getWeight(age,is_asthmatic)
    level = 0
    for index, row in enumerate(tempHumData):
        y = getTempByRange(temperature)
        if index is y:
            x = getHumByRange(humidity)
            level = row[x]

    comfort_level = getNewLevelAfterOffset(offset_weight,level)

    age /= 10
    humidity /= 100
    temperature /= 100
    pm2_5 /= 100
    pm10 /= 100

    final_level = -1
    if comfort_level is 0:
        final_level = 1
    elif comfort_level is 1 and comfort_level is 2:
        final_level = 0
    elif comfort_level is 3 and comfort_level is 4:
        final_level = 1
    elif comfort_level >= 5:
        final_level = 2
    else:
        final_level = 2

    print("{},{},{},{},{},{},{},{}".format(gender,age,humidity,temperature,pm2_5,pm10,is_asthmatic,final_level))

test()
