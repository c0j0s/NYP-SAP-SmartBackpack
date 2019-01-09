package com.nyp.fypj.smartbackpackapp.test.testcases.ui;

import android.support.test.rule.ActivityTestRule;

import com.nyp.fypj.smartbackpackapp.logon.LogonActivity;
import com.nyp.fypj.smartbackpackapp.test.core.BaseTest;
import com.nyp.fypj.smartbackpackapp.test.core.UIElements;
import com.nyp.fypj.smartbackpackapp.test.core.Utils;
import com.nyp.fypj.smartbackpackapp.test.pages.DetailPage;
import com.nyp.fypj.smartbackpackapp.test.pages.EntityListPage;
import com.nyp.fypj.smartbackpackapp.test.pages.MasterPage;

import org.junit.Rule;
import org.junit.Test;

public class NavigationTests extends BaseTest {

    @Rule
    public ActivityTestRule<LogonActivity> activityTestRule = new ActivityTestRule<>(LogonActivity.class);

    @Test
    public void testBackButtons() {

        // First do the onboarding flow
        Utils.doOnboarding();

        // Click on the first element
        EntityListPage entityListPage = new EntityListPage();
        // Check the page directly
        entityListPage.checkPageVisible(UIElements.EntityListScreen.entityList);
        entityListPage.clickFirstElement();

        // We should arrive on the master page
        MasterPage masterPage = new MasterPage();
        // Check the page directly
        masterPage.checkPageVisible(UIElements.MasterScreen.listView);
        masterPage.checkFloatingButton(true);
        masterPage.clickFirstElement();

        // We should arrive on the detail page
        DetailPage detailPage = new DetailPage();
        // Unregister idling resource
        detailPage.leavePage();
        detailPage.checkPageVisible(UIElements.DetailScreen.updateButton);
        masterPage.checkFloatingButton(false);
        detailPage.clickBack();

        // After clicking the back button we should arrive on the MasterPage
        masterPage = new MasterPage();
        // Check the page directly
        masterPage.checkPageVisible(UIElements.MasterScreen.listView);
        masterPage.checkFloatingButton(true);
        masterPage.clickBack();

        // After clicking the back button we should arrive on the EntityListPage
        entityListPage = new EntityListPage();
        // Check the page directly
        entityListPage.checkPageVisible(UIElements.EntityListScreen.entityList);

    }

}
