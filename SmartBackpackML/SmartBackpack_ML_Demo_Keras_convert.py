import tensorflow as tf

shape={"input_tensor":[5,1]}
converter = tf.lite.TFLiteConverter.from_frozen_graph("/tmp/sbp_frozen.pb",['input_tensor'], ['output_pred'],shape)
tflite_model = converter.convert()
open("./model/sbp_model.tflite", "wb").write(tflite_model)
