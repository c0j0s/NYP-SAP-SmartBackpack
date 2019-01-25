import tensorflow as tf
import sys

def _int64_feature(value):
    return tf.train.Feature(int64_list=tf.train.Int64List(value=[value]))
def _float_feature(value):
    return tf.train.Feature(float_list=tf.train.FloatList(value=value))

def load_data(dataset_path):
    labels = []
    features = []

    with open(dataset_path) as f:
        for line in f:
            columns = line.split(',')
            int_features = []
            for value in columns[:-1]:
                int_features.append(float(value))

            features.append(int_features)
            labels.append(int(columns[-1]))
    
    return features,labels

def createDataRecord(out_filename, addrs, labels):
    # open the TFRecords file
    writer = tf.python_io.TFRecordWriter(out_filename)
    for i in range(len(addrs)):
        if not i % 50:
            print('Train data: {}/{}'.format(i, len(addrs)))
            sys.stdout.flush()
        # Load the image
        features = addrs[i]

        label = labels[i]

        if features is None:
            continue

        # Create a feature
        feature = {
            'features': _float_feature(features),
            'label': _int64_feature(label)
        }
        # Create an example protocol buffer
        example = tf.train.Example(features=tf.train.Features(feature=feature))
        
        # Serialize to string and write on the file
        writer.write(example.SerializeToString())
        
    writer.close()
    sys.stdout.flush()

features,labels = load_data("training/dataset.csv")

# Divide the data into 60% train, 20% validation, and 20% test
train_features = features[0:int(0.6*len(features))]
train_labels = labels[0:int(0.6*len(labels))]
val_features = features[int(0.6*len(features)):int(0.8*len(features))]
val_labels = labels[int(0.6*len(features)):int(0.8*len(features))]
test_features = features[int(0.8*len(features)):]
test_labels = labels[int(0.8*len(labels)):]

createDataRecord('./tfrecords/train.tfrecords', train_features, train_labels)
createDataRecord('./tfrecords/val.tfrecords', val_features, val_labels)
createDataRecord('./tfrecords/test.tfrecords', test_features, test_labels)