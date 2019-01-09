package com.nyp.fypj.smartbackpackapp.test.core;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.widget.TextView;

import com.nyp.fypj.smartbackpackapp.test.core.factory.LoginPageFactory;
import com.nyp.fypj.smartbackpackapp.test.core.factory.PasscodePageFactory;
import com.nyp.fypj.smartbackpackapp.test.pages.ActivationPage;
import com.nyp.fypj.smartbackpackapp.test.pages.WelcomePage;

import org.hamcrest.Matcher;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.runner.lifecycle.Stage.RESUMED;

public class Utils {

    public static Activity getCurrentActivity() throws IllegalStateException {
        // The array is just to wrap the Activity and be able to access it from the Runnable.
        final Activity[] resumedActivity = new Activity[1];
        // Not Run on the main thread
        getInstrumentation().runOnMainSync(() -> {
            Collection resumedActivities = ActivityLifecycleMonitorRegistry.getInstance()
                    .getActivitiesInStage(RESUMED);
            if (resumedActivities.iterator().hasNext()) {
                resumedActivity[0] = (Activity) resumedActivities.iterator().next();
            } else {
                throw new IllegalStateException("No Activity in stage RESUMED");
            }
        });
        return resumedActivity[0];
    }

    public static void doOnboarding() {
        WelcomePage welcomePage = new WelcomePage();
        welcomePage.clickGetStarted();

        if (Constants.ONBOARDING_TYPE == Constants.OnboardingType.DISCOVERY_SERVICE) {
            ActivationPage activationPage = new ActivationPage();
            activationPage.clickStart();
        }

        AbstractLoginPage loginPage = LoginPageFactory.getLoginPage();
        loginPage.authenticate();

        PasscodePageFactory.PasscodeFlow();
    }

    public static void clearSessionCookies() throws InterruptedException {
        CountDownLatch clearSessionCookiesLatch = new CountDownLatch(1);
        Looper.prepare();
        CookieManager.getInstance().removeSessionCookies(value -> clearSessionCookiesLatch.countDown());
        clearSessionCookiesLatch.await(1000, TimeUnit.MILLISECONDS);
    }

    public static String getStringFromUiWithId(int resourceId) throws InterruptedException {
        final String[] uiString = new String[1];
        CountDownLatch countDownLatch = new CountDownLatch(1);
        onView(withId(resourceId)).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Getting error title.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                TextView textView = (TextView) view;
                synchronized (uiString) {
                    uiString[0] = textView.getText().toString();
                }
                countDownLatch.countDown();
            }
        });
        countDownLatch.await(1000, TimeUnit.MILLISECONDS);
        synchronized (uiString) {
            return uiString[0];
        }
    }

    public static String getResourceString(int id) {
        Context targetContext = InstrumentationRegistry.getTargetContext();
        return targetContext.getResources().getString(id);
    }
}
