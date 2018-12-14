sap.ui.define(["sap/ui/core/mvc/Controller"], function (Controller) {
	"use strict";
	return Controller.extend("HANA_Webapp.controller.Master", {
		onInit: function () {
			var userid = 1;
			//load user profile
			var userProfileTile = this.getView().byId("userProfileTile");
			userProfileTile.bindElement("/USER_PROFILE(" + userid + ")");
			//load user devices
			var userDevices = this.getView().byId("userDevices");
			var oFilter = new sap.ui.model.Filter("ACCOUNT_ID", sap.ui.model.FilterOperator.EQ, userid);
			userDevices.bindItems({
				path: "/ACCOUNT_DEVICE",
				template: userDevices.getBindingInfo("items").template,
				//filters: [oFilter]
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
			var userid = 1;
			
		    var oItem = oEvent.getSource();
		    var oContext = oItem.getBindingContext();
		    var deviceid = oContext.getObject().DEVICE_ID;
		    console.log(deviceid)
		    var oPath = oContext.getPath();
		    
		    this.getView().byId("deviceDetailsPanelBlank").setVisible(false);
		    
		    var deviceDetailsPanel = this.getView().byId("deviceDetailsPanel");
		    deviceDetailsPanel.setVisible(true);
		    deviceDetailsPanel.bindElement(oPath);
		    
		    var deviceDataTable = this.getView().byId("deviceDataTable");
		    var accountFilter = new sap.ui.model.Filter("ACCOUNT_ID", sap.ui.model.FilterOperator.EQ, userid);
		    var deviceFilter = new sap.ui.model.Filter("DEVICE_ID", sap.ui.model.FilterOperator.EQ, deviceid);

			deviceDataTable.bindItems({
				path: "/IOT_DATA",
				template: deviceDataTable.getBindingInfo("items").template,
				filters: [accountFilter,deviceFilter]
			});
		}
	});
});