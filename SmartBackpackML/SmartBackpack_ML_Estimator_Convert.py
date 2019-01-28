import tensorflow as tf

converter = tf.contrib.lite.TFLiteConverter.from_saved_model("./model/1548641835")
tflite_model = converter.convert()
open("./model/sbp_model.tflite", "wb").write(tflite_model)
