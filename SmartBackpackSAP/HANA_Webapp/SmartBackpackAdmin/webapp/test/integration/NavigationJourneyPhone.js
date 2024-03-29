/*global QUnit*/

sap.ui.define([
	"sap/ui/test/opaQunit",
	"./pages/Master",
	"./pages/Browser",
	"./pages/Detail"
], function (opaTest) {
	"use strict";

	QUnit.module("Phone navigation");

	opaTest("Should see the objects list", function (Given, When, Then) {
		// Arrangements
		Given.iStartTheApp();

		// Assertions
		Then.onTheMasterPage.iShouldSeeTheList();
		Then.onTheBrowserPage.iShouldSeeAnEmptyHash();
	});

	opaTest("Should react on hash change", function (Given, When, Then) {
		// Actions
		When.onTheMasterPage.iRememberTheIdOfListItemAtPosition(1);
		When.onTheBrowserPage.iChangeTheHashToTheRememberedItem();

		// Assertions
		Then.onTheDetailPage.iShouldSeeTheRememberedObject();
	});

	opaTest("Should navigate on press", function (Given, When, Then) {
		// Actions
		When.onTheDetailPage.iPressTheHeaderActionButton("closeColumn");
		When.onTheMasterPage.iRememberTheIdOfListItemAtPosition(2).
			and.iPressOnTheObjectAtPosition(2);

		// Assertions
		Then.onTheDetailPage.iShouldSeeTheRememberedObject().
			and.iTeardownMyAppFrame();
	});

});
