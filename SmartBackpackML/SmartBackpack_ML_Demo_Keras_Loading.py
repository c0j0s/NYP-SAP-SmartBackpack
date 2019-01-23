# TensorFlow and tf.keras
import tensorflow as tf
from tensorflow import keras

# Helper libraries
import numpy as np
import matplotlib.pyplot as plt

from lib.MlHelper import *

print(tf.__version__)

test_dataset_fp = "./training/test_dataset.csv"
saved_model_path = "./model/1548213148"

def main():
    #Load test data 
    test_features,test_labels = load_dataset(test_dataset_fp)

    #load model
    model = tf.contrib.saved_model.load_keras_model(saved_model_path)
    
    # #Build model 
    # model = keras.Sequential([
    #     keras.layers.Dense(10, activation=tf.nn.relu, input_shape=(5,)),
    #     keras.layers.Dense(10, activation=tf.nn.relu),
    #     keras.layers.Dense(5, activation=tf.nn.softmax)
    # ])

    #Compile model 
    model.compile(optimizer=tf.train.AdamOptimizer(), 
        loss='sparse_categorical_crossentropy',
        metrics=['accuracy'])

    #Evaluate accuracy
    test_loss, test_acc = model.evaluate(test_features, test_labels, steps=32)
    print('Test accuracy:', test_acc)

if __name__ == "__main__":
    main()