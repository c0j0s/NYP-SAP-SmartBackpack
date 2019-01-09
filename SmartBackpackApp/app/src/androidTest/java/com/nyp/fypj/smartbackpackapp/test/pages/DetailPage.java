package com.nyp.fypj.smartbackpackapp.test.pages;

import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import com.nyp.fypj.smartbackpackapp.test.core.AbstractMasterDetailPage;
import com.nyp.fypj.smartbackpackapp.test.core.UIElements;
import com.nyp.fypj.smartbackpackapp.test.core.WizardDevice;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.not;

public class DetailPage extends AbstractMasterDetailPage {

    public DetailPage() {
        super(UIElements.DetailScreen.updateButton);
    }

    public DetailPage(int resourceID) {
        super(resourceID);
    }

    @Override
    public void clickFirstElement() {

    }

    @Override
    public void clickBack() {
        if (WizardDevice.fromBackground) {
            try {
                new UiObject(new UiSelector().descriptionContains(UIElements.DetailScreen.toolBarBackButton)).click();
            } catch (UiObjectNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            onView(withContentDescription(UIElements.DetailScreen.toolBarBackButton)).perform(click());
        }
        // call leave page
        this.leavePage();
    }

}
