package com.nyp.fypj.smartbackpackapp.test.core.factory;

import com.nyp.fypj.smartbackpackapp.app.SAPWizardApplication;
import com.nyp.fypj.smartbackpackapp.logon.ClientPolicy;
import com.nyp.fypj.smartbackpackapp.logon.ClientPolicyManager;
import com.nyp.fypj.smartbackpackapp.test.core.Credentials;
import com.nyp.fypj.smartbackpackapp.test.pages.PasscodePage;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

public class PasscodePageFactory {

    private static final int WAIT_TIMEOUT = 5000;

    public static void PasscodeFlow() {

        // Get the current clientpolicy
        ClientPolicy clientPolicy = getClientPolicyManager().getClientPolicy(true);
        // If there is a passcode policy
        if (clientPolicy.isPasscodePolicyEnabled()) {
            // Actions on the passcode Page
            PasscodePage.CreatePasscodePage createPasscodePage = new PasscodePage().new CreatePasscodePage();
            createPasscodePage.createPasscode(Credentials.PASSCODE);
            createPasscodePage.clickNext();
            createPasscodePage.leavePage();

            // Actions on the verifypasscode Page
            PasscodePage.VerifyPasscodePage verifyPasscodePage = new PasscodePage().new VerifyPasscodePage();
            verifyPasscodePage.verifyPasscode(Credentials.PASSCODE);
            verifyPasscodePage.clickNext();
            verifyPasscodePage.leavePage();
        } else {
            // we skip the passcode flow
        }

    }

    public static void NewPasscodeFlow() {

        // Get the current clientpolicy
        getClientPolicyManager().getClientPolicy(true);

        PasscodePage.CreatePasscodePage createPasscodePage = new PasscodePage().new CreatePasscodePage();
        createPasscodePage.createPasscode(Credentials.NEWPASSCODE);
        createPasscodePage.clickSignIn();
        createPasscodePage.leavePage();

        PasscodePage.VerifyPasscodePage verifyPasscodePage = new PasscodePage().new VerifyPasscodePage();
        verifyPasscodePage.verifyPasscode(Credentials.NEWPASSCODE);
        verifyPasscodePage.clickSignIn();
        verifyPasscodePage.leavePage();
    }

    private static ClientPolicyManager getClientPolicyManager() {
        return ((SAPWizardApplication)getInstrumentation().getTargetContext().getApplicationContext())
                .getClientPolicyManager();
    }

}
