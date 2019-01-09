package com.nyp.fypj.smartbackpackapp.test.core.factory;

import android.support.annotation.NonNull;

import com.nyp.fypj.smartbackpackapp.test.core.AbstractLoginPage;
import com.nyp.fypj.smartbackpackapp.test.pages.LoginPage;

import static com.nyp.fypj.smartbackpackapp.test.core.Constants.APPLICATION_AUTH_TYPE;

public class LoginPageFactory {

    @NonNull
    public static AbstractLoginPage getLoginPage() {

        switch (APPLICATION_AUTH_TYPE) {
            case BASIC:
                return new LoginPage.BasicAuthPage();
            case OAUTH:
                return new LoginPage.WebviewPage();
            case SAML:
                return new LoginPage.WebviewPage();
            case NOAUTH:
                return new LoginPage.NoAuthPage();
            default:
                return new LoginPage.NoAuthPage();
        }
    }
}
