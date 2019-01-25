import tensorflow as tf

def load_dataset(dataset_path):
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
    
    return tf.convert_to_tensor(features, dtype=tf.float32, name="input"), tf.convert_to_tensor(labels, dtype=tf.int32, name="output")