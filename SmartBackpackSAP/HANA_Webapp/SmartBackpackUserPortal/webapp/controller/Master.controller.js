sap.ui.define(["sap/ui/core/mvc/Controller"], function (Controller) {
	"use strict";
	return Controller.extend("HANA_Webapp.controller.Master", {
		onInit: function () {
			var userid = 'P2000000001';
			//load user profile
			var userProfileTile = this.getView().byId("userProfileTile");
			userProfileTile.bindElement("/user('" + userid + "')");
			//load user devices
			var userDevices = this.getView().byId("userDevices");
			var oFilter = new sap.ui.model.Filter("USER_ID", sap.ui.model.FilterOperator.EQ, userid);
			userDevices.bindItems({
				path: "/userDevices",
				template: userDevices.getBindingInfo("items").template,
				filters: [oFilter]
			});
		},
		/**
		 *@memberOf HANA_Webapp.controller.Master
		 */
		rowSelectionChanged: function (event) {
			var aItems = this.getView().byId("userDevices").getItems();
			var aSelectedItems = [];
			for (var i = 0; i < aItems.length; i++) {
				if (aItems[i].getSelected()) {
					aSelectedItems.push(aItems[i]);
				}
			}
			console.log(aSelectedItems);
		},
		/**
		 *@memberOf HANA_Webapp.controller.Master
		 */
		tableItemPress: function (oEvent) {
			var userid = 'P2000000001';
			
		    var oItem = oEvent.getSource();
		    var oContext = oItem.getBindingContext();
		    var deviceid = oContext.getObject().DEVICE_SN;
		    console.log(deviceid)
		    var oPath = oContext.getPath();
		    
		    this.getView().byId("deviceDetailsPanelBlank").setVisible(false);
		    
		    var deviceDetailsPanel = this.getView().byId("deviceDetailsPanel");
		    deviceDetailsPanel.setVisible(true);
		    deviceDetailsPanel.bindElement(oPath);
		    
		    var deviceDataTable = this.getView().byId("deviceDataTable");
		    var accountFilter = new sap.ui.model.Filter("USER_ID", sap.ui.model.FilterOperator.EQ, userid);
		    var deviceFilter = new sap.ui.model.Filter("DEVICE_SN", sap.ui.model.FilterOperator.EQ, deviceid);

			deviceDataTable.bindItems({
				path: "/iotData",
				template: deviceDataTable.getBindingInfo("items").template,
				filters: [accountFilter,deviceFilter]
			});
		}
	});
});