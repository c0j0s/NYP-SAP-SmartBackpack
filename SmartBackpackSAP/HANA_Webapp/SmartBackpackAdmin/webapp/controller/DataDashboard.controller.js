sap.ui.define([
	"sbp/SmartBackpackAdmin/controller/BaseController",
	"sap/ui/model/json/JSONModel",
	"sap/ui/Device",
	"sbp/SmartBackpackAdmin/model/formatter",
	"sap/ui/vbm/AnalyticMap",
	"sap/viz/ui5/format/ChartFormatter",
	"sap/viz/ui5/api/env/Format"
], function (BaseController, JSONModel, formatter, AnalyticMap, Device, ChartFormatter, Format) {
	"use strict";
	return BaseController.extend("sbp.SmartBackpackAdmin.controller.DataDashboard", {
		/**
		 * Called when a controller is instantiated and its View controls (if available) are already created.
		 * Can be used to modify the View before it is displayed, to bind event handlers and do other one-time initialization.
		 * @memberOf sbp.SmartBackpackAdmin.view.DataDashboard
		 */
		onInit: function () {
			var oViewModel = new JSONModel({
				busy: false,
				delay: 0
			});
			this.getRouter().getRoute("DataDashboard").attachPatternMatched(function () {
				this.getModel("appView").setProperty("/layout", "TwoColumnsMidExpanded");
			}, this);
			this.setModel(oViewModel, "dataovpview");
			var incidentMap = this.getView().byId("incident_map");
			var ioModel = this.getOwnerComponent().getModel();
			ioModel.read("/incidentmap", {
				method: "GET",
				success: function (data) {
					var iModel = new sap.ui.model.json.JSONModel();
					var idata = {
						"Regions": [],
						"Legend": [{
							"text": "0 - 10",
							"color": "rgb(171,219,242)"
						}, {
							"text": "10 - 20",
							"color": "rgb(132,202,236)"
						}, {
							"text": "20 - 30",
							"color": "rgb(92,186,229)"
						}, {
							"text": "30 - 40",
							"color": "rgb(39,163,221)"
						}, {
							"text": "> 50",
							"color": "rgb(27,126,172)"
						}]
					};
					var list = data.results;
					for (var i = 0; i < list.length; i++) {
						if (list[i].ALERT_TRIGGERED === "Y") {
							idata.Regions.push({
								"COUNTRY_CODE": list[i].COUNTRY_CODE,
								"COUNTRY": list[i].COUNTRY + " " + list[i].COUNT,
								"COLOR": list[i].COLOR
							});
						}
					}
					
					iModel.setData(idata);
					incidentMap.setModel(iModel);
				},
				error: function () {}
			});
			var mModel = new sap.ui.model.json.JSONModel();
			var mdata = {
				"Regions": [{
					"country": "China",
					"code": "CN",
					"Value": "158626687",
					"color": "rgb(27,126,172)"
				}, {
					"country": "Singapore",
					"code": "SG",
					"Value": "531160986",
					"color": "rgb(27,126,172)"
				}, {
					"country": "United States",
					"code": "US",
					"Value": "915105168",
					"color": "rgb(27,126,172)"
				}, {
					"country": "Japan",
					"code": "JP",
					"Value": "1093786762",
					"color": "rgb(27,126,172)"
				}, {
					"country": "Russia",
					"code": "RU",
					"Value": "1274018495",
					"color": "rgb(27,126,172)"
				}]
			};
			mModel.setData(mdata);
			var aMap = this.getView().byId("analyticalmap");
			aMap.setModel(mModel);
			// set the device model
			var oDeviceModel = new JSONModel(Device);
			oDeviceModel.setDefaultBindingMode("OneWay");
			this.getView().setModel(oDeviceModel, "device"); 
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
		// onAfterRendering: function() {
		// 	var incidentMap = this.getView().byId("incident_map");
		//       console.log(this.max)
		// 	incidentMap.zoomToRegions( [this.max] );
		// },
		/**
		 * Called when the Controller is destroyed. Use this one to free resources and finalize activities.
		 * @memberOf sbp.SmartBackpackAdmin.view.DataDashboard
		 */
		//	onExit: function() {
		//
		//	}
		onRegionClick: function (e) {
			sap.m.MessageToast.show("onRegionClick " + e.getParameter("code"));
			var allUserTables = this.getView().byId("allUserTables");
			var filters = [];
			if (e.getParameter("code").trim() != "") {
				var oFilter1 = new sap.ui.model.Filter("COUNTRY_CODE", sap.ui.model.FilterOperator.EQ, e.getParameter("code"));
				filters = [oFilter1];
			}
			allUserTables.getBinding("items").filter(filters, sap.ui.model.FilterType.Application);
		},
		/**
		 *@memberOf sbp.SmartBackpackAdmin.controller.DataDashboard
		 */
		toggleFullScreen: function (oEvent) {
			var bFullScreen = this.getModel("appView").getProperty("/actionButtonsInfo/midColumn/fullScreen");
			this.getModel("appView").setProperty("/actionButtonsInfo/midColumn/fullScreen", !bFullScreen);
			var button = oEvent.getSource();
			if (!bFullScreen) {
				// store current layout and go full screen
				this.getModel("appView").setProperty("/previousLayout", this.getModel("appView").getProperty("/layout"));
				this.getModel("appView").setProperty("/layout", "MidColumnFullScreen");
				button.setIcon("sap-icon://exit-full-screen");
			} else {
				// reset to previous layout
				this.getModel("appView").setProperty("/layout", this.getModel("appView").getProperty("/previousLayout"));
				button.setIcon("sap-icon://full-screen");
			}
		},
		/**
		 *@memberOf sbp.SmartBackpackAdmin.controller.DataDashboard
		 */
		onRegionDeselect: function (oEvent) {
			sap.m.MessageToast.show("onRegionDeselect ");
			var allUserTables = this.getView().byId("allUserTables");
			var filters = [];
			allUserTables.getBinding("items").filter(filters, sap.ui.model.FilterType.Application);
		},
		/**
		 *@memberOf sbp.SmartBackpackAdmin.controller.DataDashboard
		 */
		onIncidentItemClick: function (oEvent) {
			//This code was generated by the layout editor.
			var incidentMap = this.getView().byId("incident_map");
			var sPath = oEvent.getSource().getBindingContext().sPath;
			var obj = this.getView().getModel().getObject(sPath);
			sap.m.MessageToast.show("Zoom to " + obj.COUNTRY);
			incidentMap.zoomToRegions([obj.COUNTRY_CODE]);
		}
	});
});