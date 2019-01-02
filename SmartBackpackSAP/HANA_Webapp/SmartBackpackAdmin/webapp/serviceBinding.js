function initModel() {
	var sUrl = "/NYPFYPJ_SMARTBACKPACK/sbpview.xsodata/";
	var oModel = new sap.ui.model.odata.ODataModel(sUrl, true);
	sap.ui.getCore().setModel(oModel);
}