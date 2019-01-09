package com.nyp.fypj.smartbackpackapp.test.pages;


import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import com.nyp.fypj.smartbackpackapp.R;
import com.nyp.fypj.smartbackpackapp.test.core.AbstractMasterDetailPage;
import com.nyp.fypj.smartbackpackapp.test.core.UIElements;
import com.nyp.fypj.smartbackpackapp.test.core.WizardDevice;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;

public class EntityListPage extends AbstractMasterDetailPage {

    public EntityListPage(int resourceID) {
        super(resourceID);
    }

    public EntityListPage() {
        super(UIElements.EntityListScreen.entityList);
    }

    @Override
    public void clickFirstElement() {
        onData(anything()).inAdapterView(withId(R.id.entity_list)).atPosition(0).perform(click());
    }

    @Override
    public void clickBack() {
        // There is no back ui element on this screen

    }

    public void clickSettings() {
        if (WizardDevice.fromBackground) {
            try {
                new UiObject(new UiSelector().descriptionContains(UIElements.EntityListScreen.settingsToolBar)).click();
                new UiObject(new UiSelector().textContains("Settings")).click();
            } catch (UiObjectNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            onView(withContentDescription(UIElements.EntityListScreen.settingsToolBar)).perform(click());
            onView(withText("Settings")).perform(click());
        }
    }


    public void clickEntity(int i) {
        onData(anything())
                .inAdapterView(withId(R.id.entity_list))
                .atPosition(i)
                .perform(click());

    }


}
