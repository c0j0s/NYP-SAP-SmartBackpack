# TensorFlow and tf.keras
import tensorflow as tf
from tensorflow import keras

# Helper libraries
# import numpy as np
# import matplotlib.pyplot as plt

from lib.MlHelper import *

print(tf.__version__)

train_dataset_fp = "./training/train_dataset.csv"
test_dataset_fp = "./training/test_dataset.csv"
manifest_file = "./manifest.json"
saved_model_path = "./model/"

column_names = ['humidity', 'temperature', 'pm2_5', 'pm10', 'asthmatic_level',
                'predicted_comfort_level']

feature_names = column_names[:-1]
label_name = column_names[-1]

comfort_level_desc = ['Very Good', 'Ok', 'Uncomfortable', 'Very Uncomfortable', 'Hazardous']

def main():
    #Load training data
    train_features, train_labels = load_dataset(train_dataset_fp)

    #Build model 
    model = keras.Sequential([
        keras.layers.Dense(10, activation=tf.nn.relu, input_shape=(5,)),
        keras.layers.Dense(10, activation=tf.nn.relu),
        keras.layers.Dense(5, activation=tf.nn.softmax)
    ])

    #Compile model 
    model.compile(optimizer=tf.train.AdamOptimizer(), 
        loss='sparse_categorical_crossentropy',
        metrics=['accuracy'])

    # model.compile(optimizer='adam', 
    #             loss=tf.keras.losses.sparse_categorical_crossentropy,
    #             metrics=['accuracy'])

    tbCallBack=keras.callbacks.TensorBoard(log_dir='./log', histogram_freq=0, write_graph=True)

    print(train_features)

    #Train model 
    model.fit(train_features, train_labels, epochs=201, steps_per_epoch=32, callbacks=[tbCallBack])

    #save the model
    tf.contrib.saved_model.save_keras_model(
        model,
        saved_model_path,
        custom_objects=None,
        as_text=None
    )
    
    # model.save('./model/sbp_model.h5')
    # converter = tf.contrib.lite.TFLiteConverter.from_keras_model_file('./model/sbp_model.h5')
    # tflite_model = converter.convert()
    # open('./model/sbp_model.tflite', "wb").write(tflite_model)

if __name__ == "__main__":
    main()
    