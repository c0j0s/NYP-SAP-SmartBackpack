import tensorflow as tf
from tensorflow import keras
import numpy as np
from lib.ManifestHandler import *
from lib.MlHelper import *
import os
import importlib
from keras import backend as K

manifest_file = "manifest.json"

def set_keras_backend(backend):
    if K.backend() != backend:
        os.environ['KERAS_BACKEND'] = backend
        importlib.reload(K)
        assert K.backend() == backend
    if backend == "tensorflow":
        K.get_session().close()
        cfg = K.tf.ConfigProto()
        cfg.gpu_options.allow_growth = True
        K.set_session(K.tf.Session(config=cfg))
        K.clear_session()

def init():
    global mlconfig

    manifestHandler = ManifestHandler(manifest_file)
    mlconfig = manifestHandler.getMLConfigs()

    set_keras_backend("tensorflow")  

def main():

    #Load training data
    train_features, train_labels = load_dataset(mlconfig['training_data_path'])

    #Build model 
    model = keras.Sequential([
        keras.layers.Dense(10, activation=tf.nn.relu, input_shape=(5,)),
        keras.layers.Dense(10, activation=tf.nn.relu),
        keras.layers.Dense(5, activation=tf.nn.softmax)
    ])

    #Compile model 
    model.compile(optimizer='adam', 
                loss=tf.keras.losses.sparse_categorical_crossentropy,
                metrics=['accuracy'])

    #Train model 
    model.fit(train_features, train_labels, epochs=mlconfig['training_epochs'], steps_per_epoch=32)

    #Save model
    model.save(mlconfig['model_h5_path_2'])

if __name__ == "__main__":
    init()
    main()