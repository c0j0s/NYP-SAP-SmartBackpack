import csv
import random

#gender	age	humidity	temperature	pm2_5	pm10	is_asthmatic	comfort_level

offset = {}
offset[0] = [0,0,0,0,0,0,0,0]
offset[1] = [0,0,0,0,1,2,3,4]
offset[2] = [0,0,0,1,2,3,4,5]
offset[3] = [0,0,1,2,3,4,5,6]

def main():
    temp_hum_data_file = "temp_hum_data.csv"

    with open(temp_hum_data_file) as csvfile:
        tempHumData = list(csv.reader(csvfile, delimiter=","))
        print(temp_hum_data_file + " loaded")

    amt = input("amt: ")

    for x in range(0,int(amt)):
        gender = float(random.randint(0, 1))
        age = random.randint(1, 70)

        humidity = random.randint(0, 100) 
        temperature = random.randint(0, 50) 
        pm2_5 = random.randint(0, 300) 
        pm10 = random.randint(0, 300) 

        is_asthmatic = float(random.randint(0, 1))

        comfort_level = random.randint(0, 2)

        

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


def getWeight(age,is_asthmatic):
    weight = 0
    if is_asthmatic is 1:
        if age < 10:
            weight = 3
        elif age >= 10 and age < 20:
            weight = 2
        elif age >= 20 and age < 30:
            weight = 1
        elif age >= 30 and age < 40:
            weight = 1
        elif age >= 40 and age < 50:
            weight = 2
        elif age >= 50 and age < 60:
            weight = 2
        elif age >= 60:
            weight = 3
    else:
        if age < 10:
            weight = 2
        elif age >= 10 and age < 20:
            weight = 1
        elif age >= 20 and age < 30:
            weight = 0
        elif age >= 30 and age < 40:
            weight = 0
        elif age >= 40 and age < 50:
            weight = 1
        elif age >= 50 and age < 60:
            weight = 1
        elif age >= 60:
            weight = 2
    return weight

def getComfortLevel(level):
    levels = {
    '0' : 'very bad',
    '1' : 'dry',
    '2' : 'ok',
    '3' : 'ok ~',
    '4' : 'can',
    '5' : 'uncomfortable',
    '6' : 'very uncomfortable',
    '7' : 'very uncomfortable ~',
    }
    return levels[level]

def getTempByRange(temp):
    y = 46 - int(temp)
    return y

def getHumByRange(hum):
    hum = roundHum(int(hum))
    x = round(hum/5) - 1
    return x

def getNewLevelAfterOffset(offset_weight,normal_level):
    new_level = 0
    new_level_offset = offset[int(offset_weight)][int(normal_level)]
    if int(normal_level) > 0:
        new_level = int(normal_level) + new_level_offset
    else:
        new_level = int(normal_level)
    return new_level

def roundHum(x, base=5):
    return int(base * round(float(x)/base))

if __name__ == "__main__":
    main()

