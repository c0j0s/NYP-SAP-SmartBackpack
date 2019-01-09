package com.nyp.fypj.smartbackpackapp.test.core;

import com.nyp.fypj.smartbackpackapp.R;

public class UIElements {

    public static class WelcomePage {
        public static int getStartedButton = R.id.launchscreen_button_primary;
        public static int getStartedText = R.id.launchscreen_title;
    }

    public static class ActivationPage {
        public static int startButton = R.id.activationscreen_button_discovery;
        public static int emailText = R.id.activation_email_address;
    }

    public static class LoginScreen {
        // Login screen elements including Basic / Oauth / SAML / Noauth elements

        public static class BasicAuthScreen {
            public static String usernameID = "com.nyp.fypj.smartbackpackapp:id/username";
            public static int usernameText = R.id.username;
            public static int passwordText = R.id.password;
            public static int okButton = android.R.id.button1;

        }

        public static class OauthScreen {
            public static String oauthUsernameText = "j_username";
            public static String oauthPasswordText = "j_password";
            public static String oauthLogonButton = "logOnFormSubmit";
            public static String oauthAuthorizeButton = "buttonAuthorize";
        }

    }

    public static class PasscodeScreen {
        // Passcode screen elements including the all pages
        public static int createPasscodeText = R.id.passcode_field;
        public static int verifyPasscodeText = createPasscodeText;
        public static int enterPasscodeText = createPasscodeText;
        public static int nextButton = R.id.done_button;
        public static int secondNextButton = R.id.second_done_button;
        public static int cancelButton = R.id.skip_button;
        public static int useDefaultButton = R.id.skip_button;
        public static int reachedRetryLimitTitle = R.string.max_retries_title;
        public static int reachedRetryLimitMessage = R.string.max_attempts_reached_message;
        public static int backButton = R.id.cancel_button;
        public static int retryLimitDialog = R.id.action_bar_root;
        public static int resetAppButton = android.R.id.button2;

    }

    public static class EntityListScreen {
        // EntityListScreen elements
        public static int entityList = R.id.entity_list;
        public static String settingsToolBar = "More options"; //settingsToolCar invokes SettingsScreen

        public static int settingsText = R.string.settings_activity_name;
    }


    public static class MasterScreen {
        // MasterScreen ui elements
        public static int listView = R.id.item_list;
        public static int refreshButton = R.id.menu_refresh;
        public static int addButton = R.id.fab; //addButton invokes AddOrUpdateItemScreen
        public static String toolBarBackButton = "Navigate up";
        public static int floatingActionButton = R.id.fab;

    }

    public static class DetailScreen {
        // DetailScreen ui elements
        public static int detailToolbar = R.id.detail_toolbar;
        public static int updateButton = R.id.update_item; //updateButton invokes AddOrUpdateItemScreen
        public static int deleteButton = R.id.delete_item;
        public static int propertyText = R.id.property_value;
        public static String toolBarBackButton = "Navigate up";


    }

    public static class SettingsScreen {

        //settings tab
        public static int settingsButton = R.id.settings_container;
        // Settings screen ui elements
        public static int settingsList = android.R.id.list;
        public static int resetApp = R.string.reset_app;
        public static int logLevel = R.string.log_level;
        public static int logUpload = R.string.upload_log;
        public static int managePasscode = R.string.manage_passcode;
        public static int logLevelValue = android.R.id.summary;
        public static int yesButtonResetApp = android.R.id.button1;
        public static int noButtonResetApp = android.R.id.button2;

    }

    public static class AddOrUpdateItemScreen {
        // Add New Item UI elements. (Input parameters of item varies by the type of entity chosen on entity list)
        public static int propertyText = R.id.property_value;
        public static int addButton = R.id.fab;

    }

    public static class ErrorScreen {
        public static String errorTitleId = "android.R.id.title";
        public static int errorTitle = android.R.id.title;
        public static int errorMessage = R.id.error_notification_msg;
        public static int errorOkButton = R.id.error_notification_button;
    }

    public static class NoUIScreen{
        public static String helloWorldTextID = "Hello World!";
    }

}
