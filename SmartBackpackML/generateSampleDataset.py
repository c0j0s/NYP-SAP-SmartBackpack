import json
from lib.ManifestHandler import ManifestHandler

mode  = input("mode train/test: ")

manifestPath = "manifest.json"

if mode == 'train':
    outputFile = "src/train_dataset.json"
else:
    outputFile = "src/test_dataset.json"


def main():
    datasetSize = input("Dataset size (100): ")

    if datasetSize == "":
        datasetSize = 100

    manifestHandler = ManifestHandler(manifestPath)
    festures = manifestHandler.getFeatures()

    datalist = []

    with open(outputFile,'w') as f:
        for i in range(int(datasetSize)):
            dataset = {}

            for feature in festures:
                if feature == "USER_FEEDBACK_COMFORT_LEVEL":
                    value = ""
                else:
                    value = str(manifestHandler.generateRandomValue(feature))
                dataset[feature] = value

            datalist.append(dataset)

        json.dump(datalist,f,indent=4,ensure_ascii=False)
        print("completed")
                
if __name__ == "__main__":
    main()