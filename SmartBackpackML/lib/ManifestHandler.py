import json
import random

class ManifestHandler:

    def __init__(self,manifestPath):
        self.manifestPath = manifestPath
        with open(manifestPath) as f:
            jsObj = json.load(f)
            self.manifest = jsObj
    
    def getFeatures(self):
        return self.manifest['TRAINING_INPUT_FEATURES']

    def getClassLabels(self):
        return self.manifest['PREDICTED_COMFORT_LEVEL']

    def generateRandomValue(self,feature):
        featurePath = self.manifest['DATASET_VALID_RANGE'][feature]
        
        if featurePath['type'] == 'option':
            return random.sample(featurePath['options'],  1)[0]
        else:
            return random.randrange(featurePath['min'], featurePath['max'])