package com.nyp.fypj.smartbackpackapp.test.pages;

import com.pgssoft.espressodoppio.idlingresources.ViewIdlingResource;
import com.nyp.fypj.smartbackpackapp.test.core.UIElements;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class WelcomePage {

    // Default constructor
    public WelcomePage() {
        ViewIdlingResource viewIdlingResource = (ViewIdlingResource) new ViewIdlingResource(
                withId(UIElements.WelcomePage.getStartedButton)).register();

    }

    public void clickGetStarted() {
        // Close the soft keyboard first, since it might be covering the get started button.
        onView(withId(UIElements.WelcomePage.getStartedButton)).perform(closeSoftKeyboard(), click());
    }
}
