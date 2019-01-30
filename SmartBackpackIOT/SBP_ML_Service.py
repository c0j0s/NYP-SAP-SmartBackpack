# TensorFlow and tf.keras
import tensorflow as tf
from tensorflow import keras
import numpy as np

def main():
    data = [[0.48,0.67,2.52,1.84,0.2]]
    shapedata = np.array(data)
    model = keras.models.load_model('./model/sbp_model.h5')
    predictions = model.predict(shapedata)
    print(np.argmax(predictions[0]))

if __name__ == "__main__":
    main()