import json

def toCompactString(raw):
    jsObj = json.loads(raw)
    mergeString = str(jsObj["HUMIDITY"]) + ";" + str(jsObj["TEMPERATURE"]) + ";" + str(jsObj["PM2_5"]) + ";" + str(jsObj["PM10"]) + ";" + str(jsObj["PREDICTED_COMFORT_LEVEL"]) + ";" + str(jsObj["ALERT_TRIGGERED"]) 
    output = {}
    output[str(jsObj["RECOREDED_ON"])] = mergeString
    return output

output = toCompactString('{"RECOREDED_ON": "2019-01-16 15:06:45", "PM10": 3, "PM2_5": 2, "ALERT_TRIGGERED": false, "HUMIDITY": 50.0, "TEMPERATURE": 19.0, "PREDICTED_COMFORT_LEVEL": "dry"}')
last_line = toCompactString('{"RECOREDED_ON": "2019-01-16 15:06:45", "PM10": 3, "PM2_5": 2, "ALERT_TRIGGERED": false, "HUMIDITY": 50.0, "TEMPERATURE": 19.0, "PREDICTED_COMFORT_LEVEL": "dry"}')

print(str(output))
print(str(last_line))

if output == last_line:
    print("true")

