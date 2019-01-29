/*global QUnit*/

sap.ui.define([
	"sbpml/SmartBackpackML/controller/Master.controller"
], function (oController) {
	"use strict";

	QUnit.module("Master Controller");

	QUnit.test("I should test the Master controller", function (assert) {
		var oAppController = new oController();
		oAppController.onInit();
		assert.ok(oAppController);
	});

});