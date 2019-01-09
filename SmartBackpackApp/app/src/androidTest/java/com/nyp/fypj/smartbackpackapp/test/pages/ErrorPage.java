package com.nyp.fypj.smartbackpackapp.test.pages;

import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiSelector;

import com.nyp.fypj.smartbackpackapp.test.core.UIElements;
import com.nyp.fypj.smartbackpackapp.test.core.Utils;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class ErrorPage {
    private static final int WAIT_TIMEOUT = 2000;
    UiDevice device;

    public ErrorPage() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    public String getErrorTitle() throws InterruptedException {
        UiObject usernameField = device.findObject(new UiSelector()
                .resourceId(UIElements.ErrorScreen.errorTitleId));
        usernameField.waitForExists(WAIT_TIMEOUT);
        return Utils.getStringFromUiWithId(UIElements.ErrorScreen.errorTitle);
    }

    public String getErrorMessage() throws InterruptedException {
        UiObject usernameField = device.findObject(new UiSelector()
                .resourceId(UIElements.ErrorScreen.errorTitleId));
        usernameField.waitForExists(WAIT_TIMEOUT);
        return Utils.getStringFromUiWithId(UIElements.ErrorScreen.errorMessage);
    }

    public void dismiss() {
        onView(withId(UIElements.ErrorScreen.errorOkButton)).check(matches(isDisplayed())).perform(click());
    }
}
