package com.nyp.fypj.smartbackpackapp.test.testcases.ui;

import android.os.SystemClock;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.nyp.fypj.smartbackpackapp.app.SAPWizardApplication;
import com.nyp.fypj.smartbackpackapp.logon.ClientPolicy;
import com.nyp.fypj.smartbackpackapp.logon.ClientPolicyManager;
import com.nyp.fypj.smartbackpackapp.logon.LogonActivity;
import com.nyp.fypj.smartbackpackapp.test.core.BaseTest;
import com.nyp.fypj.smartbackpackapp.test.core.Credentials;
import com.nyp.fypj.smartbackpackapp.test.core.UIElements;
import com.nyp.fypj.smartbackpackapp.test.core.Utils;
import com.nyp.fypj.smartbackpackapp.test.core.WizardDevice;
import com.nyp.fypj.smartbackpackapp.test.core.factory.PasscodePageFactory;
import com.nyp.fypj.smartbackpackapp.test.pages.EntityListPage;
import com.nyp.fypj.smartbackpackapp.test.pages.MasterPage;
import com.nyp.fypj.smartbackpackapp.test.pages.PasscodePage;
import com.nyp.fypj.smartbackpackapp.test.pages.SettingsListPage;
import com.nyp.fypj.smartbackpackapp.test.pages.WelcomePage;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static com.nyp.fypj.smartbackpackapp.test.core.UIElements.EntityListScreen.entityList;

@RunWith(AndroidJUnit4.class)
public class PasscodeTests extends BaseTest {

    @Rule
    public ActivityTestRule<LogonActivity> activityTestRule = new ActivityTestRule<>(LogonActivity.class);


    @Test
    public void testPasscodeLockTimeOut() {
        // Take care of welcome screen, authentication, and passcode flow.
        Utils.doOnboarding();

        EntityListPage entityListPage = new EntityListPage(UIElements.EntityListScreen.entityList);
        entityListPage.clickFirstElement();

        MasterPage masterPage = new MasterPage();

        // Get the lockTimeOut (in seconds) from the SecureStoreManager
        int lockTimeOut = ((SAPWizardApplication)activityTestRule.getActivity().getApplication())
                .getSecureStoreManager().getPasscodeLockTimeout();
        // We put the app into background
        WizardDevice.putApplicationBackground(3000, activityTestRule);
        // We reopen the app
        WizardDevice.reopenApplication();

        SystemClock.sleep(1000);
        // We should arrive in the Item list Page
        entityListPage = new EntityListPage(UIElements.EntityListScreen.entityList);

        // Put and reopen the app
        WizardDevice.putApplicationBackground((lockTimeOut + 1) * 1000, activityTestRule);
        WizardDevice.reopenApplication();
        // We should arrive in the EnterPasscodeScreen

        PasscodePage.EnterPasscodePage enterPasscodePage = new PasscodePage().new EnterPasscodePage();
        enterPasscodePage.enterPasscode(Credentials.PASSCODE);
        enterPasscodePage.clickSignIn();

        // Go Back from the Master page
        masterPage.clickBack();

        // We should arrive in the EntityListPage
        entityListPage = new EntityListPage(entityList);
        entityListPage.clickSettings();

        SettingsListPage settingsListPage = new SettingsListPage(UIElements.SettingsScreen.settingsList);
        settingsListPage.clickResetApp();

    }


    @Test
    public void testManagePasscodeBackground() {
        // Take care of welcome screen, authentication, and passcode flow.
        Utils.doOnboarding();

        EntityListPage entityListPage = new EntityListPage(UIElements.EntityListScreen.entityList);
        entityListPage.clickFirstElement();

        int lockTimeOut = ((SAPWizardApplication)activityTestRule.getActivity().getApplication())
                .getSecureStoreManager().getPasscodeLockTimeout();

        WizardDevice.putApplicationBackground((lockTimeOut + 1) * 1000, activityTestRule);

        WizardDevice.reopenApplication();
        PasscodePage.EnterPasscodePage enterPasscodePage = new PasscodePage().new EnterPasscodePage();
        enterPasscodePage.enterPasscode(Credentials.PASSCODE);
        enterPasscodePage.clickSignIn();

        MasterPage masterPage = new MasterPage();
        masterPage.clickBack();

        entityListPage = new EntityListPage(entityList);
        entityListPage.clickSettings();

        SettingsListPage settingsListPage = new SettingsListPage(UIElements.SettingsScreen.settingsList);
        settingsListPage.clickManagePasscode();

        enterPasscodePage = new PasscodePage().new EnterPasscodePage();
        enterPasscodePage.enterPasscode(Credentials.PASSCODE);
        enterPasscodePage.clickSecondNextButton();
        enterPasscodePage.leavePage();

        PasscodePageFactory.NewPasscodeFlow();

        WizardDevice.putApplicationBackground((lockTimeOut + 1) * 1000, activityTestRule);
        WizardDevice.reopenApplication();

        enterPasscodePage.enterPasscode(Credentials.NEWPASSCODE);
        enterPasscodePage.clickSignIn();

        settingsListPage = new SettingsListPage(UIElements.SettingsScreen.settingsList);
        settingsListPage.clickResetApp();

        settingsListPage.checkConfirmationDialog();

        settingsListPage.clickYes();
    }


    @Test
    public void testManagePasscodeCancelBackground() {
        // Take care of welcome screen, authentication, and passcode flow.
        Utils.doOnboarding();

        EntityListPage entityListPage = new EntityListPage(entityList);
        entityListPage.clickSettings();

        SettingsListPage settingsListPage = new SettingsListPage(UIElements.SettingsScreen.settingsList);
        settingsListPage.clickManagePasscode();

        PasscodePage.EnterPasscodePage enterPasscodePage = new PasscodePage().new EnterPasscodePage();
        enterPasscodePage.enterPasscode(Credentials.PASSCODE);

        enterPasscodePage.clickCancel();

        int lockTimeOut = ((SAPWizardApplication)activityTestRule.getActivity().getApplication())
                .getSecureStoreManager().getPasscodeLockTimeout();

        WizardDevice.putApplicationBackground((lockTimeOut + 1) * 1000, activityTestRule);

        WizardDevice.reopenApplication();
        enterPasscodePage.enterPasscode(Credentials.PASSCODE);
        enterPasscodePage.clickSignIn();

        settingsListPage = new SettingsListPage(UIElements.SettingsScreen.settingsList);
        settingsListPage.clickResetApp();

        settingsListPage.checkConfirmationDialog();

        settingsListPage.clickYes();
    }


    @Test
    public void testManagePasscodeDefaultBackground() {
        SAPWizardApplication sapWizardApplication = ((SAPWizardApplication)activityTestRule.getActivity().getApplication());

        // Take care of welcome screen, authentication, and passcode flow.
        Utils.doOnboarding();

        EntityListPage entityListPage = new EntityListPage(UIElements.EntityListScreen.entityList);
        entityListPage.clickFirstElement();

        int lockTimeOut = sapWizardApplication.getSecureStoreManager().getPasscodeLockTimeout();

        WizardDevice.putApplicationBackground((lockTimeOut + 1) * 1000, activityTestRule);
        WizardDevice.reopenApplication();

        PasscodePage.EnterPasscodePage enterPasscodePage = new PasscodePage().new EnterPasscodePage();
        enterPasscodePage.enterPasscode(Credentials.PASSCODE);
        enterPasscodePage.clickSignIn();

        MasterPage masterPage = new MasterPage();
        masterPage.clickBack();

        entityListPage = new EntityListPage(entityList);
        entityListPage.clickSettings();

        SettingsListPage settingsListPage = new SettingsListPage(UIElements.SettingsScreen.settingsList);
        settingsListPage.clickManagePasscode();

        enterPasscodePage.enterPasscode(Credentials.PASSCODE);
        enterPasscodePage.clickSecondNextButton();
        enterPasscodePage.leavePage();

        // Get the current clientpolicy
        ClientPolicy clientPolicy = sapWizardApplication.getClientPolicyManager().getClientPolicy(true);
        PasscodePage.CreatePasscodePage createPasscodePage = new PasscodePage().new CreatePasscodePage();

        PasscodePageFactory.NewPasscodeFlow();

        WizardDevice.putApplicationBackground((lockTimeOut + 1) * 1000, activityTestRule);
        WizardDevice.reopenApplication();

        enterPasscodePage = new PasscodePage().new EnterPasscodePage();
        enterPasscodePage.enterPasscode(Credentials.NEWPASSCODE);
        enterPasscodePage.clickSignIn();

        settingsListPage.clickResetApp();

        settingsListPage.clickYes();
        WelcomePage welcomePage = new WelcomePage();
    }


    @Test
    public void testPasscodeRetryLimitBackground() {
        // Take care of welcome screen, authentication, and passcode flow.
        Utils.doOnboarding();
        EntityListPage entityListPage = new EntityListPage(UIElements.EntityListScreen.entityList);
        entityListPage.clickFirstElement();
        // Get the lockTimeOut (in seconds) from the SecureStoreManager
        int lockTimeOut = ((SAPWizardApplication)activityTestRule.getActivity().getApplication())
                .getSecureStoreManager().getPasscodeLockTimeout();
        // We put the app into background
        WizardDevice.putApplicationBackground((lockTimeOut + 1) * 1000, activityTestRule);
        // We reopen the app
        WizardDevice.reopenApplication();
        // Try the retry limit flow
        PasscodePage.EnterPasscodePage enterPasscodePage = new PasscodePage().new EnterPasscodePage();
        ClientPolicyManager clientPolicyManager = ((SAPWizardApplication) activityTestRule.getActivity().getApplication()).getClientPolicyManager();
        for (int i = 0; i < clientPolicyManager.getClientPolicy(false).getPasscodePolicy().getRetryLimit(); i++) {
            enterPasscodePage.enterPasscode(Credentials.WRONGPASSCODE);
            enterPasscodePage.clickSignIn();
        }
        enterPasscodePage.clickResetAppButton();

    }
}
