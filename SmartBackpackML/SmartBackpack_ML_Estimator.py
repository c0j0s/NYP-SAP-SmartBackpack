import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import tensorflow as tf
from sklearn.model_selection import train_test_split

plt.style.use("seaborn-colorblind")

# only displays the most important warnings
# tf.logging.set_verbosity(tf.logging.FATAL)

used_features = ['humidity','temperature','pm2_5','pm10','asthmatic_level','user_feedback_comfort_level']
dataset = pd.read_csv('./training/dataset.csv', usecols = used_features)

features = dataset.drop('user_feedback_comfort_level',axis=1)
labels = dataset['user_feedback_comfort_level']

X_train, X_test, y_train, y_test = train_test_split(
     features, labels, test_size=0.33, random_state=42)

numeric_columns = ['humidity','temperature','pm2_5','pm10','asthmatic_level']

numeric_features = [tf.feature_column.numeric_column(key = column) for column in numeric_columns]
print(numeric_features[0])

# Create training input function
training_input_fn = tf.estimator.inputs.pandas_input_fn(x = X_train,
                                                        y=y_train,
                                                        batch_size=32,
                                                        shuffle= True,
                                                        num_epochs = None)
# create testing input function
eval_input_fn = tf.estimator.inputs.pandas_input_fn(x=X_test,
                                                    y=y_test,
                                                    batch_size=32,
                                                    shuffle=False,
                                                    num_epochs = 1)

classifier = tf.estimator.DNNClassifier(
 feature_columns=numeric_features,
 hidden_units=[256, 32],
 optimizer=tf.train.AdamOptimizer(1e-4),
 n_classes=5,
 dropout=0.1,
 model_dir="./log/classifier"
)

# linear_regressor = tf.estimator.LinearRegressor(feature_columns=numeric_features,
#                                                 model_dir = "./log/linear_regressor")

classifier.train(input_fn = training_input_fn,steps=2000)

feature_spec = {
    'humidity': tf.FixedLenSequenceFeature([], dtype=tf.float32,allow_missing=True),
    'temperature': tf.FixedLenSequenceFeature([], dtype=tf.float32,allow_missing=True),
    'pm2_5': tf.FixedLenSequenceFeature([], dtype=tf.float32,allow_missing=True),
    'pm10': tf.FixedLenSequenceFeature([], dtype=tf.float32,allow_missing=True),
    'asthmatic_level': tf.FixedLenSequenceFeature([], dtype=tf.float32,allow_missing=True),
                }

# def serving_input_receiver_fn():
#   """An input receiver that expects a serialized tf.Example."""
#   serialized_tf_example = tf.placeholder(dtype=tf.string,
#                                          shape=[5,],
#                                          name='input_features')
#   receiver_tensors = {'examples': serialized_tf_example}
#   features = tf.parse_example(serialized_tf_example, feature_spec)
#   return tf.estimator.export.ServingInputReceiver(features, receiver_tensors)

# classifier.export_savedmodel("./model", serving_input_receiver_fn,
#                             strip_default_attrs=True)

loss = classifier.evaluate(input_fn = eval_input_fn)

prediction_set = pd.DataFrame({'humidity':[0.5],'temperature':[0.3],'pm2_5':[1.5],'pm10':[1.5],'asthmatic_level':[3]})

predict_input_fn = tf.estimator.inputs.pandas_input_fn(
                      x=prediction_set, 
                      num_epochs=1, 
                      shuffle=False)


print(str(loss))