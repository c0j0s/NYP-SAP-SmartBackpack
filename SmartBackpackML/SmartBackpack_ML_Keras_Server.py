import tensorflow as tf
from tensorflow import keras
import numpy as np
from flask import Flask, jsonify, request, make_response, abort
from flask_httpauth import HTTPBasicAuth
from lib.ManifestHandler import *
from keras import backend as K

app = Flask(__name__)
auth = HTTPBasicAuth()
manifest_file = "manifest.json"
# manifest_file = "SmartBackpackML\\manifest.json"

@auth.get_password
def get_password(username):
    if username == 'mlservice':
        return 'passwd'
    return None

@auth.error_handler
def unauthorized():
    return make_response(jsonify({'error': 'Unauthorized access'}), 401)

@app.route('/')
def index():
    return "Hello, World!"

@app.route('/predict', methods=['POST'])
@auth.login_required
def predict():
    if not request.json:
        abort(400)

    try:
        data = request.json
        print(data)
        x = np.array([[
                float(data["HUMIDITY"])/100,
                float(data['TEMPERATURE'])/100,
                float(data['PM2_5'])/100,
                float(data['PM10'])/100,
                float(data['ASTHMATIC_LEVEL'])/10
            ]])

        print(x)

        y_prob = model.predict(x)
        print(y_prob)

        K.clear_session()

        y_classes = y_prob.argmax(axis=-1)
        print(y_classes)
        return jsonify({'PREDICTED_COMFORT_LEVEL': int(y_classes[0])}), 200

    except Exception as identifier:
        return jsonify({'ERROR': str(identifier)}), 200
    
    

def init():
    global model
    global mlconfig

    #load config
    manifestHandler = ManifestHandler(manifest_file)
    mlconfig = manifestHandler.getMLConfigs()

    #load ml model
    model = keras.models.load_model(mlconfig['model_h5_path'])
    model._make_predict_function()

if __name__ == '__main__':
    init()
    app.run(debug=False, port=80, host='0.0.0.0') 
    # app.run(debug=True)
