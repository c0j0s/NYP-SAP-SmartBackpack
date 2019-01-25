import tensorflow as tf

converter = tf.contrib.lite.TFLiteConverter.from_keras_model_file("./model/sbp_model.h5")
tflite_model = converter.convert()
open("./model/sbp_model.tflite", "wb").write(tflite_model)