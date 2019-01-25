import tensorflow as tf
import sys

sess = tf.Session()
sess.run(tf.global_variables_initializer())

def parser(record):
    keys_to_features = {
        "features":  tf.FixedLenSequenceFeature([], dtype=tf.float32,allow_missing=True),
        "label":     tf.FixedLenFeature([], tf.int64)
    }
    parsed = tf.parse_single_example(record, keys_to_features)
    features = tf.cast(parsed["features"], tf.float32)
    label = tf.cast(parsed["label"], tf.int32)

    return {'features': features}, label

def input_fn(filenames):
  dataset = tf.data.TFRecordDataset(filenames=filenames, num_parallel_reads=40)
  dataset = dataset.apply(
      tf.contrib.data.shuffle_and_repeat(1024, 1)
  )
  dataset = dataset.apply(
      tf.contrib.data.map_and_batch(parser, 32)
  )
  dataset = dataset.prefetch(buffer_size=2)
  return dataset


def train_input_fn():
    return input_fn(filenames=["tfrecords/train.tfrecords", "tfrecords/test.tfrecords"])

def val_input_fn():
    return input_fn(filenames=["tfrecords/val.tfrecords"])


def model_fn(features, labels, mode, params):
    num_classes = 5
    net = features["features"]

    net = tf.identity(net, name="input_tensor")
    
    net = tf.reshape(net, [160,5])    

    net = tf.layers.dense(inputs=net, name='layer_fc1',
                        units=10, activation=tf.nn.relu)  

    net = tf.layers.dense(inputs=net, name='layer_fc2',
                    units=10, activation=tf.nn.relu)  
    
    net = tf.layers.dropout(net, rate=0.5, noise_shape=None, 
                        seed=None, training=(mode == tf.estimator.ModeKeys.TRAIN))
    
    net = tf.layers.dense(inputs=net, name='layer_fc3',
                        units=num_classes)

    logits = net
    y_pred = tf.nn.softmax(logits=logits)

    y_pred = tf.identity(y_pred, name="output_pred")

    y_pred_cls = tf.argmax(y_pred, axis=1)

    y_pred_cls = tf.identity(y_pred_cls, name="output_cls")


    if mode == tf.estimator.ModeKeys.PREDICT:
        spec = tf.estimator.EstimatorSpec(mode=mode,
                                          predictions=y_pred_cls)
    else:
        cross_entropy = tf.nn.sparse_softmax_cross_entropy_with_logits(labels=labels,
                                                                       logits=logits)
        loss = tf.reduce_mean(cross_entropy)

        optimizer = tf.train.AdamOptimizer(learning_rate=params["learning_rate"])
        train_op = optimizer.minimize(
            loss=loss, global_step=tf.train.get_global_step())
        metrics = {
            "accuracy": tf.metrics.accuracy(labels, y_pred_cls)
        }

        spec = tf.estimator.EstimatorSpec(
            mode=mode,
            loss=loss,
            train_op=train_op,
            eval_metric_ops=metrics)
        
    return spec

model = tf.estimator.Estimator(model_fn=model_fn,
                               params={"learning_rate": 1e-4},
                               model_dir="./model/")

count = 0
while (count < 1000):
    model.train(input_fn=train_input_fn, steps=201)
    result = model.evaluate(input_fn=val_input_fn)
    print(result)
    print("Classification accuracy: {0:.2%} ".format(result["accuracy"]))
    print("count: " + str(count))
    sys.stdout.flush()
    count = count + 1