function initModel() {
	var sUrl = "/NYPFYPJ_SMARTBACKPACK/sbp.xsodata/";
	var oModel = new sap.ui.model.odata.ODataModel(sUrl, true);
	sap.ui.getCore().setModel(oModel);
}