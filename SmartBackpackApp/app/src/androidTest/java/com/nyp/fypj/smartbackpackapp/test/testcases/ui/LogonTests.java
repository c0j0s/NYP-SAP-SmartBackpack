package com.nyp.fypj.smartbackpackapp.test.testcases.ui;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.nyp.fypj.smartbackpackapp.app.SAPWizardApplication;
import com.nyp.fypj.smartbackpackapp.logon.LogonActivity;
import com.nyp.fypj.smartbackpackapp.test.core.BaseTest;
import com.nyp.fypj.smartbackpackapp.test.core.UIElements;
import com.nyp.fypj.smartbackpackapp.test.core.Utils;
import com.nyp.fypj.smartbackpackapp.test.core.WizardDevice;
import com.nyp.fypj.smartbackpackapp.test.pages.DetailPage;
import com.nyp.fypj.smartbackpackapp.test.pages.EntityListPage;
import com.nyp.fypj.smartbackpackapp.test.pages.MasterPage;
import com.nyp.fypj.smartbackpackapp.test.pages.SettingsListPage;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.nyp.fypj.smartbackpackapp.test.core.UIElements.EntityListScreen.entityList;

@RunWith(AndroidJUnit4.class)
public class LogonTests extends BaseTest {

    @Rule
    public ActivityTestRule<LogonActivity> activityTestRule = new ActivityTestRule<>(LogonActivity.class);


    @Test
    public void testLogonFlow() {

        // Take care of welcome screen, authentication, and passcode flow.
        Utils.doOnboarding();

        // Actions on the entitylist Page
        EntityListPage entityListPage = new EntityListPage(entityList);
        entityListPage.clickFirstElement();
        entityListPage.leavePage();

        // Actions on the master Page
        MasterPage masterPage = new MasterPage(UIElements.MasterScreen.refreshButton);
        masterPage.clickFirstElement();
        masterPage.leavePage();

        DetailPage detailPage = new DetailPage();
        detailPage.clickBack();

        masterPage = new MasterPage(UIElements.MasterScreen.refreshButton);
        masterPage.clickBack();

        entityListPage = new EntityListPage(entityList);
        entityListPage.clickSettings();

        SettingsListPage settingsListPage = new SettingsListPage(UIElements.SettingsScreen.settingsList);
        settingsListPage.clickResetApp();

        settingsListPage.checkConfirmationDialog();

        settingsListPage.clickYes();
    }


    @Test
    public void logonFlowPutAppIntoBackground() {
        // Take care of welcome screen, authentication, and passcode flow.
        Utils.doOnboarding();

        EntityListPage entityListPage = new EntityListPage(entityList);
        entityListPage.clickFirstElement();

        MasterPage masterPage = new MasterPage(UIElements.MasterScreen.refreshButton);
        masterPage.clickFirstElement();

        // Put the app into background and immediately start again
        WizardDevice.putApplicationBackground(0, activityTestRule);
        WizardDevice.reopenApplication();

        DetailPage mDetailPage = new DetailPage(UIElements.DetailScreen.deleteButton);
        mDetailPage.clickBack();

        masterPage = new MasterPage(UIElements.MasterScreen.refreshButton);
        masterPage.clickBack();

        entityListPage = new EntityListPage(entityList);
        entityListPage.clickSettings();

        SettingsListPage settingsListPage = new SettingsListPage(UIElements.SettingsScreen.settingsList);
        settingsListPage.clickResetApp();

        settingsListPage.checkConfirmationDialog();

        settingsListPage.clickYes();
    }
}
