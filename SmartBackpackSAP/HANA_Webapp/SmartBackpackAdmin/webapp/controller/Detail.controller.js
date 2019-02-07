/*global location */
sap.ui.define([
	"sbp/SmartBackpackAdmin/controller/BaseController",
	"sap/ui/model/json/JSONModel",
	"sap/ui/Device",
	"sap/m/Dialog",
	"sap/m/Button",
	"sap/m/MessageBox",
	"sap/m/MessageToast",
	"sbp/SmartBackpackAdmin/model/formatter"
], function (BaseController, JSONModel, formatter, Dialog, Button, MessageBox, MessageToast) {
	"use strict";
	return BaseController.extend("sbp.SmartBackpackAdmin.controller.Detail", {
		formatter: formatter,
		/* =========================================================== */
		/* lifecycle methods                                           */
		/* =========================================================== */
		onInit: function () {
			// Model used to manipulate control states. The chosen values make sure,
			// detail page is busy indication immediately so there is no break in
			// between the busy indication for loading the view's meta data
			var oViewModel = new JSONModel({
				busy: false,
				delay: 0
			});
			this.getRouter().getRoute("object").attachPatternMatched(this._onObjectMatched, this);
			this.setModel(oViewModel, "detailView");
			this.getOwnerComponent().getModel().metadataLoaded().then(this._onMetadataLoaded.bind(this));
		},
		/* =========================================================== */
		/* event handlers                                              */
		/* =========================================================== */
		/**
		 * Event handler when the share by E-Mail button has been clicked
		 * @public
		 */
		onSendEmailPress: function () {
			var oViewModel = this.getModel("detailView");
			sap.m.URLHelper.triggerEmail(null, oViewModel.getProperty("/shareSendEmailSubject"), oViewModel.getProperty(
				"/shareSendEmailMessage"));
		},
		/**
		 * Event handler when the share in JAM button has been clicked
		 * @public
		 */
		onShareInJamPress: function () {
			var oViewModel = this.getModel("detailView"),
				oShareDialog = sap.ui.getCore().createComponent({
					name: "sap.collaboration.components.fiori.sharing.dialog",
					settings: {
						object: {
							id: location.href,
							share: oViewModel.getProperty("/shareOnJamTitle")
						}
					}
				});
			oShareDialog.open();
		},
		/* =========================================================== */
		/* begin: internal methods                                     */
		/* =========================================================== */
		/**
		 * Binds the view to the object path and expands the aggregated line items.
		 * @function
		 * @param {sap.ui.base.Event} oEvent pattern match event in route 'object'
		 * @private
		 */
		_onObjectMatched: function (oEvent) {
			var sObjectId = oEvent.getParameter("arguments").objectId;
			this.getModel("appView").setProperty("/layout", "TwoColumnsMidExpanded");
			this.getModel("appView").setProperty("/detailsexpended", true);
			this.getModel().metadataLoaded().then(function () {
				var sObjectPath = this.getModel().createKey("userinfos", {
					USER_ID: sObjectId
				});
				this._bindView("/" + sObjectPath);
			}.bind(this));
			
			var userDeviceTable = this.getView().byId("userDeviceTable");
			userDeviceTable.bindItems({
				path: "/userDevices",
				template: userDeviceTable.getBindingInfo("items").template,
				filters: [new sap.ui.model.Filter("USER_ID", sap.ui.model.FilterOperator.EQ, sObjectId)]
			});
		},
		/**
		 * Binds the view to the object path. Makes sure that detail view displays
		 * a busy indicator while data for the corresponding element binding is loaded.
		 * @function
		 * @param {string} sObjectPath path to the object to be bound to the view.
		 * @private
		 */
		_bindView: function (sObjectPath) {
			// Set busy indicator during view binding
			var oViewModel = this.getModel("detailView");
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
				sObjectId = oObject.USER_ID,
				sObjectName = oObject.EMAIL,
				oViewModel = this.getModel("detailView");
			this.getOwnerComponent().oListSelector.selectAListItem(sPath);
			oViewModel.setProperty("/userid", sObjectId);
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
				oViewModel = this.getModel("detailView");
			// Make sure busy indicator is displayed immediately when
			// detail view is displayed for the first time
			oViewModel.setProperty("/delay", 0);
			// Binding the view will set it to not busy - so the view is always busy if it is not bound
			oViewModel.setProperty("/busy", true);
			// Restore original busy indicator delay for the detail view
			oViewModel.setProperty("/delay", iOriginalViewBusyDelay);
		},
		/**
		 * Set the full screen mode to false and navigate to master page
		 */
		onCloseDetailPress: function () {
			this.getModel("appView").setProperty("/actionButtonsInfo/midColumn/fullScreen", false);
			this.getModel("appView").setProperty("/detailsexpended", false);
			// No item should be selected on master after detail page is closed
			this.getOwnerComponent().oListSelector.clearMasterListSelection();
			this.getRouter().navTo("master");
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
		 *@memberOf sbp.SmartBackpackAdmin.controller.Detail
		 */
		expandIotDataOverview: function (oEvent) {
			var sObjectId = this.getModel("detailView").getProperty("/userid");
			//console.log(sObjectId);
			var DEVICE_SN = oEvent.getSource().data("device-sn");
			var oRouter = sap.ui.core.UIComponent.getRouterFor(this);
			oRouter.navTo("userDeviceOvp", {
				userid: sObjectId,
				devicesn: DEVICE_SN
			});
		},
		pressDialog: null,
		/**
		 *@memberOf sbp.SmartBackpackAdmin.controller.Detail
		 */
		openEditProfileDialog: function (oEvent) {
			var sPath = this.getView().getElementBinding().getPath();
			var oObject = this.getView().getModel().getObject(sPath);
			var oModel = this.getView().getModel();

			this.pressDialog = new Dialog({
				title: "Update user profile",
				type: "Message",
				contentWidth: "415px",
				content: [
					new sap.m.Label({
						text: "Personal Details",
						design: "Bold",
						textAlign: "Center"
					}),
					new sap.m.VBox({}),
					new sap.m.Label({
						required : true,
						text: "Name",
						labelFor: "input-username"
					}),
					new sap.m.Input("input-username", {
						type: "Text",
						value: oObject.NAME
					}),
					new sap.m.Label({
						required : true,
						text: "Gender",
						labelFor: "input-gender"
					}),
					new sap.m.RadioButtonGroup("input-gender", {
						required : true,
						selectedIndex: ((oObject.GENDER === "M") ? 0 : 1),
						columns: 1,
						buttons: [
							new sap.m.RadioButton({
								text: "Male"
							}),
							new sap.m.RadioButton({
								text: "Female"
							})
						]
					}),
					new sap.m.VBox({}),
					new sap.m.Label({
						text: "Race",
						labelFor: "input-race"
					}),
					new sap.m.Input("input-race", {
						type: "Text",
						value: oObject.RACE
					}),
					new sap.m.Label({
						required : true,
						text: "Date of Birth",
						labelFor: "input-dob"
					}),
					new sap.m.DatePicker("input-dob", {
						displayFormat: "long",
						value: new Date(oObject.DOB).toDateString()
					}),
					new sap.m.Label({
						required : true,
						text: "Asthmatic level",
						labelFor: "input-asthlevel"
					}),
					new sap.m.Slider("input-asthlevel", {
						enableTickmarks: true,
						min: 0,
						max: 4,
						value: oObject.ASTHMATIC_LEVEL,
						liveChange: this.handleAsthmaticSliderChanges,
						width: "95%"
					}),
					new sap.m.VBox({}),
					new sap.m.Text("input-asthlevel-info", {
						text: oObject.ASTHMATIC_LEVEL
					}),
					new sap.m.VBox({}),
					new sap.m.Label({
						text: "Contact information",
						design: "Bold",
						textAlign: "Center"
					}),
					new sap.m.VBox({}),
					new sap.m.Label({
						text: "Email",
						labelFor: "input-email"
					}),
					new sap.m.Input("input-email", {
						type: "Email",
						value: oObject.EMAIL
					}),
					new sap.m.Label({
						text: "Contact number",
						labelFor: "input-contactno"
					}),
					new sap.m.Input("input-contactno", {
						type: "Number",
						value: oObject.CONTACT_NO
					})
				],
				beginButton: new Button({
					text: "Update Profile",
					press: function () {
						var asthlevel = sap.ui.getCore().byId("input-asthlevel").getValue();
						var contactnum = sap.ui.getCore().byId("input-contactno").getValue();
						var email = sap.ui.getCore().byId("input-email").getValue();
						var username = sap.ui.getCore().byId("input-username").getValue();
						var race = sap.ui.getCore().byId("input-race").getValue();
						var gender = sap.ui.getCore().byId("input-gender").getSelectedIndex();
						var dob = new Date(sap.ui.getCore().byId("input-dob").getValue()).getTime();
						dob = (dob/1) + 86400000;
						var oEntry = {
							ASTHMATIC_LEVEL: asthlevel,
							CONTACT_NO: contactnum.toString(),
							EMAIL: email,
							NAME: username,
							RACE: race,
							GENDER: (gender === 0) ? "M" : "F",
							DOB: "/Date(" + dob + ")/"
						};
						
						var that = this;
						oModel.update("/user('" + oObject.USER_ID + "')", oEntry, {
							success: function () {
								that.getView().getElementBinding().refresh(true);
								MessageToast.show("Profile Updated");
							},
							error: function () {
								MessageToast.show("Fail to Add New Device");
							}
						});
						this.pressDialog.destroy();
					}.bind(this)
				}),
				endButton: new Button({
					text: "Close",
					press: function () {
						this.pressDialog.destroy();
					}.bind(this)
				})
			});
			//to get access to the global model
			this.getView().addDependent(this.pressDialog);

			this.pressDialog.open();
		},
		handleAsthmaticSliderChanges: function (oEvent) {
			var leveldesc = [
				"level 0 - Nil",
				"level 1 - Intermittent Asthma",
				"level 2 - Mild Persistent Asthma",
				"level 3 - Moderate Persistent Asthma",
				"level 4 - Severe Persistent Asthma"
			];
			var level = oEvent.getParameters().value;
			sap.ui.getCore().byId("input-asthlevel-info").setText(leveldesc[level]);
		},
		/**
		 *@memberOf sbp.SmartBackpackAdmin.controller.Detail
		 */
		openAddDeviceDialog: function (oEvent) {
			var userid = this.getModel("detailView").getProperty("/userid");
			var userDeviceTable = this.getView().byId("userDeviceTable");
			var oModel = userDeviceTable.getModel();
			var timeint = new Date().getTime();

			this.pressDialog = new Dialog({
				title: "Add device to user",
				type: "Message",
				contentWidth: "400px",
				content: [
					new sap.m.Label({
						text: "Device Serial Number",
						labelFor: "input-deviceSn"
					}),
					new sap.m.Input("input-deviceSn", {
						required : true,
						type: "Text",
						placeholder: "Enter Device Serial Number",
						showSuggestion: true,
						showValueHelp: true,
						valueHelpRequest: [this.handleValueHelp, this],
						valueHelpOnly: true,
					}),
					new sap.m.Label({
						text: "Device Name",
						labelFor: "input-deviceName"
					}),
					new sap.m.Input("input-deviceName", {
						type: "Text"
					}),
					new sap.m.Label({
						text: "Device Configurations",
						design: "Bold",
						textAlign: "Center"
					}),
					new sap.m.VBox({}),
					new sap.m.Label({
						text: "Minutes to record sensor readings",
						labelFor: "input-minutestorecord"
					}),
					new sap.m.Input("input-minutestorecord", {
						type: "Number",
						value: 10
					}),
					new sap.m.CheckBox("input-enablebuzzer", {
						text: "Enable Buzzer ",
						selected: true
					}),
					new sap.m.CheckBox("input-enableled", {
						text: "Enable LED",
						selected: true
					}),
				],
				beginButton: new Button({
					text: "Add Device",
					press: function () {
						var devicesn = sap.ui.getCore().byId("input-deviceSn").getValue();
						var devicename = sap.ui.getCore().byId("input-deviceName").getValue();
						var minutestorecord = sap.ui.getCore().byId("input-minutestorecord").getValue();
						var enablebuzzer = sap.ui.getCore().byId("input-enablebuzzer").getSelected();
						var enableled = sap.ui.getCore().byId("input-enableled").getSelected();
						if(devicename === ""){
							devicename = "new bag";
						}
						if(minutestorecord === ""){
							minutestorecord = 10;
						}
						var oEntry = {
							USER_ID: userid,
							DEVICE_SN: devicesn,
							DEVICE_NAME: devicename,
							REGISTERED_ON: "/Date(" + timeint + ")/",
							MINUTES_TO_RECORD_DATA: Number(minutestorecord),
							CONFIG_ENABLE_BUZZER: (enablebuzzer) ? "Y" : "N",
							CONFIG_ENABLE_LED: (enableled) ? "Y" : "N",
							CONFIG_UPDATED_ON: "/Date(" + timeint + ")/",
						};

						oModel.create("/userDevices", oEntry, {
							success: function () {
								MessageToast.show("New Device Added");
							},
							error: function () {
								MessageToast.show("Fail to Add New Device");
							}
						});
						this.pressDialog.destroy();
					}.bind(this)
				}),
				endButton: new Button({
					text: "Close",
					press: function () {
						this.pressDialog.destroy();
					}.bind(this)
				})
			});
			//to get access to the global model
			this.getView().addDependent(this.pressDialog);

			this.pressDialog.open();
		},
		handleValueHelp: function (oController) {

			this.inputId = oController.getSource().getId();
			// create value help dialog
			if (!this._valueHelpDialog) {
				this._valueHelpDialog = sap.ui.xmlfragment(
					"sbp.SmartBackpackAdmin.fragment.inputDeviceSNValueHelp",
					this
				);
				this.getView().addDependent(this._valueHelpDialog);
			}

			this._valueHelpDialog.open();
		},
		_handleValueHelpSearch: function (oEvent) {
			var sTerm = oEvent.getParameter("value");
			var aFilters = [];
			if (sTerm) {
				aFilters.push(new sap.ui.model.Filter("DEVICE_SN", sap.ui.model.FilterOperator.Contains, sTerm));
			}
			oEvent.getSource().getBinding("items").filter(aFilters);
		},
		_handleValueHelpClose: function (evt) {
			var oSelectedItem = evt.getParameter("selectedItem");

			if (oSelectedItem) {
				var productInput = sap.ui.getCore().byId("input-deviceSn");

				productInput.setValue(oSelectedItem.getTitle());
			}
			evt.getSource().getBinding("items").filter([]);
		},
		/**
		 *@memberOf sbp.SmartBackpackAdmin.controller.Detail
		 */
		openDeleteConfirmDialog: function (oEvent) {
			var devicesn = oEvent.getSource().data("device-sn");
			var userid = this.getModel("detailView").getProperty("/userid");
			var userDeviceTable = this.getView().byId("userDeviceTable");
			var oModel = userDeviceTable.getModel();

			var bCompact = !!this.getView().$().closest(".sapUiSizeCompact").length;
			MessageBox.confirm(
				"Remove Device?", {
					styleClass: bCompact ? "sapUiSizeCompact" : "",
					onClose: function (sAction) {
						if (sAction === "OK") {
							oModel.remove("/userDevices(USER_ID='" + userid + "',DEVICE_SN='" + devicesn + "')", {
								success: function () {
									MessageToast.show("Delete successful");
								},
								error: function () {
									MessageToast.show("Delete failed");
								}

							});

						}
					}
				}

			);
		}
	});
});