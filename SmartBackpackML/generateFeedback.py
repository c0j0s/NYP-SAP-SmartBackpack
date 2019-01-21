import csv
import json
from lib.ManifestHandler import ManifestHandler



manifestPath = "manifest.json"


demo = True
exclude_list = ["AGE","RACE","GENDER"]

def main():
    mode  = input("mode train/test: ")
    if mode == 'train':
        inputFile = "generated/train_dataset.json"
        outputFile = "training/train_dataset.csv"
    else:
        inputFile = "generated/test_dataset.json"
        outputFile = "training/test_dataset.csv"

    manifestHandler = ManifestHandler(manifestPath)
    festures = manifestHandler.getFeatures()

    with open(inputFile) as f:
        jsonList = json.load(f)

        with open(outputFile,"w") as o:
            for dataset in jsonList:
                dataset["USER_FEEDBACK_COMFORT_LEVEL"] = generatePredictedComfortLevel(dataset)
                o.write(toCSV(festures,dataset))
                o.write("\n")
    
    print("Completed")

def toCSV(festures,dataset):
    result = ""
    for idx, feature in enumerate(festures):
        if demo:
            if feature not in exclude_list:
                if idx == 0:
                    result = compressValue(feature,dataset[feature])
                else:
                    result += "," + compressValue(feature,dataset[feature])
        else:
            if idx == 0:
                result = compressValue(feature,dataset[feature])
            else:
                result += "," + compressValue(feature,dataset[feature])

    if result.startswith(","):
        result = result[1:]

    return result

def compressValue(feature,value):
    if feature == "USER_FEEDBACK_COMFORT_LEVEL":
        return str(int(value))
    elif feature == "ASTHMATIC_LEVEL":
        return str(float(value)/10)
    else:
        return str(float(value)/100)

def generatePredictedComfortLevel(dataset):
    #filter extreme conditions
    if int(dataset["TEMPERATURE"]) >= 61 or int(dataset["TEMPERATURE"]) <= -31:
        return 4

    if int(dataset["PM2_5"]) >= 251:
        return 4
    
    if int(dataset["PM10"]) >= 431:
        return 4

    weight = dataset["ASTHMATIC_LEVEL"]

    new_temperature_level_base_on_user = getFeatureLevel(-30,60,20,weight)
    new_humidity_level_base_on_user = getFeatureLevel(0,100,50,weight)
    new_pm2_5_level_base_on_user = getFeatureLevel(0,250,0,weight)
    new_PM10_level_base_on_user = getFeatureLevel(0,430,0,weight)

    jobs = [
        new_temperature_level_base_on_user,
        new_humidity_level_base_on_user,
        new_pm2_5_level_base_on_user,
        new_PM10_level_base_on_user
    ]

    dataset_values = [
        abs(float(dataset["TEMPERATURE"]) - 20) + 20,
        abs(float(dataset["HUMIDITY"]) - 50) + 50,
        dataset["PM2_5"],
        dataset["PM10"]
    ]

    dataset_feature_levels = getAllFeatureLevels(jobs,dataset_values)

    return getFinalComfortLevel(dataset_feature_levels)

def getFeatureLevel(min,max,safe_mid,weight):

    min_to_mid = abs(safe_mid - min)
    mid_to_max = abs(max - safe_mid)

    min_to_mid_interval = min_to_mid / 4
    mid_to_max_interval = mid_to_max / 4

    return getLevelMinMax(safe_mid, min_to_mid_interval,mid_to_max_interval,weight)

def getLevelMinMax(start, min_to_mid_interval,mid_to_max_interval,weight):
    results = []

    for i in range(4):
        level = i

        if i == 0:
            min = start + level * mid_to_max_interval
        else:
            min = max

        if i == 3:
            max = 9999
        else:
            max = start + (level + 1) * mid_to_max_interval - 1 - float(weight) * mid_to_max_interval/4  
        results.append({
            "level":level,
            "max":max,
            "min":min
        })

    return results

def getAllFeatureLevels(jobs,dataset_value):
    print(str(dataset_value))
    accessment = []
    for idx,item in enumerate(jobs):
        print(item)
        for interval in item:
            if float(dataset_value[idx]) < float(interval['max']) and float(dataset_value[idx]) >= float(interval['min']) :
                accessment.append(interval['level'])

    print(accessment)
    print("=========")
    return accessment

def getFinalComfortLevel(accessments):
    sum = 0

    grade_weight =[
        1,
        0.5,
        1.5,
        1.5
    ]

    for idx,value in enumerate(accessments):
        sum += int(value) * grade_weight[idx]

    if sum <= 3:
        return 0
    elif sum <= 6:
        return 1
    elif sum <= 9:
        return 2
    elif sum <= 12:
        return 3
    else:
        return 4

if __name__ == "__main__":
    main()