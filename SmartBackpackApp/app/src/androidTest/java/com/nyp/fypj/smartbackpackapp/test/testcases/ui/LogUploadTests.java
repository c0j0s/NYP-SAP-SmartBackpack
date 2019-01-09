package com.nyp.fypj.smartbackpackapp.test.testcases.ui;

import android.support.test.rule.ActivityTestRule;

import com.nyp.fypj.smartbackpackapp.app.SAPWizardApplication;
import com.nyp.fypj.smartbackpackapp.logon.LogonActivity;
import com.nyp.fypj.smartbackpackapp.test.core.BaseTest;
import com.nyp.fypj.smartbackpackapp.test.core.Credentials;
import com.nyp.fypj.smartbackpackapp.test.core.Utils;
import com.nyp.fypj.smartbackpackapp.test.core.WizardDevice;
import com.nyp.fypj.smartbackpackapp.test.pages.DetailPage;
import com.nyp.fypj.smartbackpackapp.test.pages.EntityListPage;
import com.nyp.fypj.smartbackpackapp.test.pages.MasterPage;
import com.nyp.fypj.smartbackpackapp.test.pages.PasscodePage;
import com.nyp.fypj.smartbackpackapp.test.pages.SettingsListPage;

import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

public class LogUploadTests extends BaseTest {

    @Rule
    public ActivityTestRule<LogonActivity> activityTestRule = new ActivityTestRule<>(LogonActivity.class);


    @Test
    public void testLogUpload() {
        // This test just tests whether the buttons works as expected
        // no crash and the toast appears or not
        Utils.doOnboarding();

        EntityListPage entityListPage = new EntityListPage();
        entityListPage.clickFirstElement();

        MasterPage masterPage = new MasterPage();
        masterPage.clickFirstElement();

        DetailPage detailPage = new DetailPage();
        detailPage.clickBack();

        masterPage = new MasterPage();
        masterPage.clickBack();

        entityListPage = new EntityListPage();
        entityListPage.clickSettings();
        SettingsListPage settingsListPage = new SettingsListPage();
        setUpLogs();
        settingsListPage.clickUploadLog();
        settingsListPage.checkLogUploadToast();
    }


    @Test
    public void testLogUploadBackgroundLocked() {

        Utils.doOnboarding();

        EntityListPage entityListPage = new EntityListPage();
        entityListPage.clickFirstElement();

        MasterPage masterPage = new MasterPage();
        masterPage.clickFirstElement();

        DetailPage detailPage = new DetailPage();
        detailPage.clickBack();

        masterPage = new MasterPage();
        masterPage.clickBack();

        // Put the application into background and wait until the app is locked
        int lockTimeOut = ((SAPWizardApplication)activityTestRule.getActivity().getApplication())
                .getSecureStoreManager().getPasscodeLockTimeout();
        WizardDevice.putApplicationBackground((lockTimeOut + 1) * 1000, activityTestRule);
        // Reopen app
        WizardDevice.reopenApplication();

        PasscodePage.EnterPasscodePage enterPasscodePage = new PasscodePage().new EnterPasscodePage();
        enterPasscodePage.enterPasscode(Credentials.PASSCODE);
        enterPasscodePage.clickSignIn();

        // Logupload flow
        entityListPage = new EntityListPage();
        entityListPage.clickFirstElement();

        masterPage = new MasterPage();
        masterPage.clickBack();

        entityListPage = new EntityListPage();
        entityListPage.clickSettings();
        SettingsListPage settingsListPage = new SettingsListPage();
        setUpLogs();
        settingsListPage.clickUploadLog();
        settingsListPage.checkLogUploadToast();
    }

    private void setUpLogs() {
        Logger LOGGER = LoggerFactory.getLogger(LogonActivity.class);
        LOGGER.error("first error message");
        LOGGER.error("second error message");
        LOGGER.error("third error message");
    }

}
