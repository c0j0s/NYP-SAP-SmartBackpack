sap.ui.define([
	"sbp/SmartBackpackAdmin/controller/BaseController",
	"sap/ui/model/json/JSONModel",
	"sap/ui/Device",
	"sbp/SmartBackpackAdmin/model/formatter",
	"sap/ui/vbm/AnalyticMap"
], function (BaseController, JSONModel, formatter,AnalyticMap,Device) {
	"use strict";

	return BaseController.extend("sbp.SmartBackpackAdmin.controller.DataDashboard", {

		/**
		 * Called when a controller is instantiated and its View controls (if available) are already created.
		 * Can be used to modify the View before it is displayed, to bind event handlers and do other one-time initialization.
		 * @memberOf sbp.SmartBackpackAdmin.view.DataDashboard
		 */
		onInit: function() {
			var oViewModel = new JSONModel({
				busy: false,
				delay: 0
			});
			this.getRouter().getRoute("DataDashboard").attachPatternMatched(function(){
				this.getModel("appView").setProperty("/layout", "TwoColumnsMidExpanded");
			}, this);
			this.setModel(oViewModel, "dataovpview"); 
			
			var mModel = new sap.ui.model.json.JSONModel();
			var mdata = {
					'Regions' : [
			            {"country": "China","code": "CN","Value": "158626687","color":"rgb(27,126,172)"},
			            {"country": "Singapore","code": "SG","Value": "531160986","color":"rgb(27,126,172)"},
			            {"country": "United States","code": "US","Value": "915105168","color":"rgb(27,126,172)"},
			            {"country": "Japan","code": "JP","Value": "1093786762","color":"rgb(27,126,172)"},
			            {"country": "Russia","code": "RU","Value": "1274018495","color":"rgb(27,126,172)"},
			           ]};
			mModel.setData(mdata);
			var aMap = this.getView().byId("analyticalmap");
			aMap.setModel(mModel);
			
			
			// set the device model
			var oDeviceModel = new JSONModel(Device);
			oDeviceModel.setDefaultBindingMode("OneWay");
			this.getView().setModel(oDeviceModel, "device");
			
			
			//      1.Get the id of the VizFrame		
			var oVizFrame = this.getView().byId("idcolumn");
			
	//      2.Create a JSON Model and set the data
			var oModel = new sap.ui.model.json.JSONModel();
			var data = {
					'Population' : [
			            {"Year": "2010","Value": "158626687"},
			            {"Year": "2011","Value": "531160986"},
			            {"Year": "2012","Value": "915105168"},
			            {"Year": "2013","Value": "1093786762"},
			            {"Year": "2014","Value": "1274018495"},
			           ]};
			oModel.setData(data);
			
	//      3. Create Viz dataset to feed to the data to the graph
			var oDataset = new sap.viz.ui5.data.FlattenedDataset({
				dimensions : [{
					name : 'Year',
					value : "{Year}"}],
				               
				measures : [{
					name : 'Population',
					value : '{Value}'} ],
				             
				data : {
					path : "/Population"
				}
			});		
			oVizFrame.setDataset(oDataset);
			oVizFrame.setModel(oModel);	
			oVizFrame.setVizType('column');
			
	//      4.Set Viz properties
			oVizFrame.setVizProperties({
	            plotArea: {
	            	colorPalette : d3.scale.category20().range()
	                }});
			
			var feedValueAxis = new sap.viz.ui5.controls.common.feeds.FeedItem({
			      'uid': "valueAxis",
			      'type': "Measure",
			      'values': ["Population"]
			    }), 
			    feedCategoryAxis = new sap.viz.ui5.controls.common.feeds.FeedItem({
			      'uid': "categoryAxis",
			      'type': "Dimension",
			      'values': ["Year"]
			    });
			oVizFrame.addFeed(feedValueAxis);
			oVizFrame.addFeed(feedCategoryAxis);
		},

		/**
		 * Similar to onAfterRendering, but this hook is invoked before the controller's View is re-rendered
		 * (NOT before the first rendering! onInit() is used for that one!).
		 * @memberOf sbp.SmartBackpackAdmin.view.DataDashboard
		 */
		//	onBeforeRendering: function() {
		//
		//	},

		/**
		 * Called when the View has been rendered (so its HTML is part of the document). Post-rendering manipulations of the HTML could be done here.
		 * This hook is the same one that SAPUI5 controls get after being rendered.
		 * @memberOf sbp.SmartBackpackAdmin.view.DataDashboard
		 */
		//	onAfterRendering: function() {
		//
		//	},

		/**
		 * Called when the Controller is destroyed. Use this one to free resources and finalize activities.
		 * @memberOf sbp.SmartBackpackAdmin.view.DataDashboard
		 */
		//	onExit: function() {
		//
		//	}
		
		onRegionClick: function(e) {
			sap.m.MessageToast.show("onRegionClick " + e.getParameter("code") );
		},


	});

});