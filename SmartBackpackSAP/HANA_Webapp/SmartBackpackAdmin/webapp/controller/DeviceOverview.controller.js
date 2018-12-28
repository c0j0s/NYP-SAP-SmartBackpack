sap.ui.define([
	"sbp/SmartBackpackAdmin/controller/BaseController",
	"sap/ui/model/json/JSONModel",
	"sap/ui/Device",
	"sap/m/Dialog",
	"sap/m/Button",
	"sap/m/MessageBox",
	"sap/m/MessageToast",
	"sbp/SmartBackpackAdmin/model/formatter",
], function (BaseController, JSONModel, formatter, Dialog, Button, MessageBox, MessageToast) {
	"use strict";
	return BaseController.extend("sbp.SmartBackpackAdmin.controller.DeviceOverview", {
		formatter: formatter,
		/**
		 * Called when a controller is instantiated and its View controls (if available) are already created.
		 * Can be used to modify the View before it is displayed, to bind event handlers and do other one-time initialization.
		 * @memberOf sbp.SmartBackpackAdmin.view.DeviceOverview
		 */
		onInit: function () {
			var oViewModel = new JSONModel({
				busy: false,
				delay: 0
			});
			this.getRouter().getRoute("userDeviceOvp").attachPatternMatched(this._onObjectMatched, this);
			this.setModel(oViewModel, "deviceOvp");
			this.getOwnerComponent().getModel().metadataLoaded().then(this._onMetadataLoaded.bind(this));
		},
		/**
		 * Similar to onAfterRendering, but this hook is invoked before the controller's View is re-rendered
		 * (NOT before the first rendering! onInit() is used for that one!).
		 * @memberOf sbp.SmartBackpackAdmin.view.DeviceOverview
		 */
		//	onBeforeRendering: function() {
		//
		//	},
		/**
		 * Called when the View has been rendered (so its HTML is part of the document). Post-rendering manipulations of the HTML could be done here.
		 * This hook is the same one that SAPUI5 controls get after being rendered.
		 * @memberOf sbp.SmartBackpackAdmin.view.DeviceOverview
		 */
		//	onAfterRendering: function() {
		//
		//	},
		/**
		 * Called when the Controller is destroyed. Use this one to free resources and finalize activities.
		 * @memberOf sbp.SmartBackpackAdmin.view.DeviceOverview
		 */
		//	onExit: function() {
		//
		//	}
		_onObjectMatched: function (oEvent) {
			var objectId = oEvent.getParameter("arguments").userid;
			var devicesn = oEvent.getParameter("arguments").devicesn;
			//UPDATE to use combinded view
			this.getModel("appView").setProperty("/layout", "TwoColumnsMidExpanded");
			this.getModel().metadataLoaded().then(function () {
				var sObjectPath = this.getModel().createKey("userDevices", {
					USER_ID: objectId,
					DEVICE_SN: devicesn
				});
				this._bindView("/" + sObjectPath);
			}.bind(this));
			var aboutDevicePanel = this.getView().byId("aboutDevicePanelContent");
			var devicePath = this.getModel().createKey("/iotDevice", {
				DEVICE_SN: devicesn
			});
			aboutDevicePanel.bindElement(devicePath);
			var iotDataTable = this.getView().byId("iotDataTable");
			iotDataTable.bindItems({
				path: "/iotData",
				template: iotDataTable.getBindingInfo("items").template,
				filters: [
					new sap.ui.model.Filter("DEVICE_SN", sap.ui.model.FilterOperator.EQ, devicesn),
					new sap.ui.model.Filter("USER_ID", sap.ui.model.FilterOperator.EQ, objectId)
				]
			});
		},
		_bindView: function (sObjectPath) {
			// Set busy indicator during view binding
			var oViewModel = this.getModel("deviceOvp");
			// If the view was not bound yet its not busy, only if the binding requests data it is set to busy again
			oViewModel.setProperty("/busy", false);
			this.getView().bindElement({
				path: sObjectPath,
				events: {
					change: this._onBindingChange.bind(this),
					dataRequested: function () {
						oViewModel.setProperty("/busy", true);
					},
					dataReceived: function () {
						oViewModel.setProperty("/busy", false);
					}
				}
			});
		},
		_onBindingChange: function () {
			var oView = this.getView(),
				oElementBinding = oView.getElementBinding();
			// No data for the binding
			if (!oElementBinding.getBoundContext()) {
				this.getRouter().getTargets().display("detailObjectNotFound");
				// if object could not be found, the selection in the master list
				// does not make sense anymore.
				this.getOwnerComponent().oListSelector.clearMasterListSelection();
				return;
			}
			var sPath = oElementBinding.getPath(),
				oResourceBundle = this.getResourceBundle(),
				oObject = oView.getModel().getObject(sPath),
				sObjectId = oObject.DEVICE_SN,
				sObjectName = oObject.DEVICE_NAME,
				oViewModel = this.getModel("deviceOvp");
			this.getOwnerComponent().oListSelector.selectAListItem(sPath);
			oViewModel.setProperty("/saveAsTileTitle", oResourceBundle.getText("shareSaveTileAppTitle", [sObjectName]));
			oViewModel.setProperty("/shareOnJamTitle", sObjectName);
			oViewModel.setProperty("/shareSendEmailSubject", oResourceBundle.getText("shareSendEmailObjectSubject", [sObjectId]));
			oViewModel.setProperty("/shareSendEmailMessage", oResourceBundle.getText("shareSendEmailObjectMessage", [
				sObjectName,
				sObjectId,
				location.href
			]));
		},
		_onMetadataLoaded: function () {
			// Store original busy indicator delay for the detail view
			var iOriginalViewBusyDelay = this.getView().getBusyIndicatorDelay(),
				oViewModel = this.getModel("deviceOvp");
			// Make sure busy indicator is displayed immediately when
			// detail view is displayed for the first time
			oViewModel.setProperty("/delay", 0);
			// Binding the view will set it to not busy - so the view is always busy if it is not bound
			oViewModel.setProperty("/busy", true);
			// Restore original busy indicator delay for the detail view
			oViewModel.setProperty("/delay", iOriginalViewBusyDelay);
		},

		/**
		 * Toggle between full and non full screen mode.
		 */
		toggleFullScreen: function () {
			var bFullScreen = this.getModel("appView").getProperty("/actionButtonsInfo/midColumn/fullScreen");
			this.getModel("appView").setProperty("/actionButtonsInfo/midColumn/fullScreen", !bFullScreen);
			if (!bFullScreen) {
				// store current layout and go full screen
				this.getModel("appView").setProperty("/previousLayout", this.getModel("appView").getProperty("/layout"));
				this.getModel("appView").setProperty("/layout", "MidColumnFullScreen");
			} else {
				// reset to previous layout
				this.getModel("appView").setProperty("/layout", this.getModel("appView").getProperty("/previousLayout"));
			}
		},
		/**
		 *@memberOf sbp.SmartBackpackAdmin.controller.DeviceOverview
		 */
		openCustomiseDeviceDialog: function (oEvent) {
			var sPath = this.getView().getElementBinding().getPath();
			var oObject = this.getView().getModel().getObject(sPath);
			var oModel = this.getView().getModel();

			var pressDialog = new Dialog({
					title: "Update Device Settigs",
					type: "Message",
					contentWidth:"400px",
					content: [
						new sap.m.Label({
							text:"Device Name",
							labelFor: "input-deviceName"
						}),
						new sap.m.Input("input-deviceName",{
							type:"Text",
							value: oObject.DEVICE_NAME
						}),
						new sap.m.Label({
							text:"Device Configurations",
							design:"Bold",
							textAlign:"Center"
						}),
						new sap.m.VBox({
						}),
						new sap.m.Label({
							text:"Minutes to record sensor readings",
							labelFor: "input-minutestorecord"
						}),
						new sap.m.Input("input-minutestorecord",{
							type:"Number",
							value:oObject.MINUTES_TO_RECORD_DATA
						}),
						new sap.m.CheckBox("input-enablebuzzer",{
							text:"Enable Buzzer ",
							selected:((oObject.CONFIG_ENABLE_BUZZER === "Y") ? true : false)
						}),
						new sap.m.CheckBox("input-enableled",{
							text:"Enable LED [NOT IMPLEMENTED]",
							selected:((oObject.CONFIG_ENABLE_LED === "Y") ? true : false)
						}),
						],
					beginButton: new Button({
						text: "Update Configurations",
						press: function () {
							var devicename = sap.ui.getCore().byId("input-deviceName").getValue();
							var minutestorecord = sap.ui.getCore().byId("input-minutestorecord").getValue();
							var enablebuzzer = sap.ui.getCore().byId("input-enablebuzzer").getSelected();
							var enableled = sap.ui.getCore().byId("input-enableled").getSelected();
							var oEntry = {
								DEVICE_NAME:devicename,
								MINUTES_TO_RECORD_DATA:Number(minutestorecord),
								CONFIG_ENABLE_BUZZER:(enablebuzzer) ? "Y" : "N",
								CONFIG_ENABLE_LED:(enableled) ? "Y" : "N",
								CONFIG_UPDATED_ON:"/Date(" + new Date().getTime() + ")/"
							};
							console.log(oEntry);
							
							oModel.update("/userDevices(USER_ID='" + oObject.USER_ID + "',DEVICE_SN='" + oObject.DEVICE_SN + "')", oEntry, { 
						    success: function() { 
						        MessageToast.show("Device Settings Updated");
						    },
						    error: function() { 
						        MessageToast.show("Fail to Update Device Settings");
						    }
							});
							pressDialog.destroy();
						}.bind(this)
					}),
					endButton: new Button({
						text: "Close",
						press: function () {
							pressDialog.destroy();
						}
					})
			});
			pressDialog.open();
		}
	});
});