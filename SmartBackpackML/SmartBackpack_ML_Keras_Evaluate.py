import tensorflow as tf
from tensorflow import keras
import numpy as np
from lib.ManifestHandler import *
from lib.MlHelper import *
from keras import backend as K

manifest_file = "manifest.json"

def init():
    global model
    global mlconfig

    #load config
    manifestHandler = ManifestHandler(manifest_file)
    mlconfig = manifestHandler.getMLConfigs()

    #load ml model
    model = keras.models.load_model(mlconfig['model_h5_path_2'])

def main():
    test_features, test_labels = load_dataset(mlconfig['test_data_path'])

    test_loss, test_acc = model.evaluate(test_features, test_labels, steps=1)
    print('Test accuracy:', test_acc)

if __name__ == "__main__":
    init()
    main()