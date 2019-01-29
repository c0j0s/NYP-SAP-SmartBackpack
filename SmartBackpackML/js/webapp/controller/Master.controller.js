sap.ui.define([
	"sap/ui/core/mvc/Controller"
], function (Controller) {
	"use strict";

	return Controller.extend("sbpml.SmartBackpackML.controller.Master", {
		onInit: function () {
			// Define a model for linear regression.
			var tf = require('@tensorflow/tfjs-node');
			
			var model = tf.sequential();
			model.add(tf.layers.dense({units: 1, inputShape: [1]}));
			
			model.compile({loss: 'meanSquaredError', optimizer: 'sgd'});
			
			// Generate some synthetic data for training.
			var xs = tf.tensor2d([1, 2, 3, 4], [4, 1]);
			var ys = tf.tensor2d([1, 3, 5, 7], [4, 1]);
			
			// Train the model using the data.
			model.fit(xs, ys, {epochs: 10}).then(function(){
				// Use the model to do inference on a data point the model hasn't seen before:
			  model.predict(tf.tensor2d([5], [1, 1])).print();
			  // Open the browser devtools to see the output
			});
		}
	});
});