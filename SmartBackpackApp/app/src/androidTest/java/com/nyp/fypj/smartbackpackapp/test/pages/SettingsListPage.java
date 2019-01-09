package com.nyp.fypj.smartbackpackapp.test.pages;

import android.support.test.espresso.matcher.PreferenceMatchers;

import com.nyp.fypj.smartbackpackapp.R;
import com.nyp.fypj.smartbackpackapp.test.core.AbstractMasterDetailPage;
import com.nyp.fypj.smartbackpackapp.test.core.UIElements;
import com.nyp.fypj.smartbackpackapp.test.core.Utils;
import com.nyp.fypj.smartbackpackapp.test.core.matcher.ToastMatcher;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.nyp.fypj.smartbackpackapp.test.core.matcher.WizardViewMatcher.withIndex;
import static org.hamcrest.Matchers.anything;

public class SettingsListPage extends AbstractMasterDetailPage {

    public SettingsListPage(int resourceID) {
        super(resourceID);
    }

    public SettingsListPage() {
        super((UIElements.SettingsScreen.settingsList));
    }

    @Override
    public void clickFirstElement() {
        onData(anything()).inAdapterView(withId(android.R.id.list)).atPosition(0).perform(click());
    }

    @Override
    public void clickBack() {
        // There is no back ui element on this screen
        onView(withContentDescription(UIElements.MasterScreen.toolBarBackButton)).perform(click());
    }

    public void clickLogLevel() {
        onData(PreferenceMatchers.withKey(Utils.getResourceString(R.string.log_level)))
                .perform(click());
    }

    public void clickUploadLog() {
        onData(PreferenceMatchers.withKey(Utils.getResourceString(R.string.upload_log)))
                .perform(click());

    }

    public void clickManagePasscode() {
        onData(PreferenceMatchers.withKey(Utils.getResourceString(R.string.manage_passcode)))
                .perform(click());
    }

    public void clickResetApp() {
        onData(PreferenceMatchers.withKey(Utils.getResourceString(R.string.reset_app)))
                .perform(click());
    }

    public void clickYes() {
        onView(withId(UIElements.SettingsScreen.yesButtonResetApp))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    public void clickCancelOnDialog() {
        onView(withId(UIElements.SettingsScreen.noButtonResetApp))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /* Checkers */

    public void checkLoglevel(String expectedLoglevel) {
        onView(withIndex(withId(android.R.id.summary), 0)).check(matches(withText(expectedLoglevel)));
    }

    public void checkLogUploadToast() {
        onView(withText(R.string.log_upload_ok)).inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));

        onView(withText(R.string.log_upload_ok)).inRoot(new ToastMatcher())
                .check(matches(withText(R.string.log_upload_ok)));
    }

     public void checkConfirmationDialog() {
         onView(withText(R.string.reset_app_confirmation))
                .check(matches(isDisplayed()));
     }

}
