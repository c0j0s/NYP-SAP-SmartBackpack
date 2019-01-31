import tensorflow as tf
from tensorflow import keras
import numpy as np
from lib.ManifestHandler import *
from keras import backend as K

manifest_file = "SmartBackpackML\\manifest.json"

def init():
    global model
    global mlconfig

    #load config
    manifestHandler = ManifestHandler(manifest_file)
    mlconfig = manifestHandler.getMLConfigs()

    #load ml model
    model = keras.models.load_model(mlconfig['model_h5_path'])

def main():
    x = np.array([[
        float(48)/100,
        float(67)/100,
        float(252)/100,
        float(184)/100,
        float(2)/10
    ]])

    print(x)

    y_prob = model.predict(x) 
    print(y_prob)

    K.clear_session()

    y_classes = y_prob.argmax(axis=-1)
    print(y_classes)

if __name__ == "__main__":
    init()
    main()