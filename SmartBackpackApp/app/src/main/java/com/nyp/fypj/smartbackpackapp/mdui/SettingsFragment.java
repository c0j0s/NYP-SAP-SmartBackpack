package com.nyp.fypj.smartbackpackapp.mdui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;


import com.nyp.fypj.smartbackpackapp.R;
import com.nyp.fypj.smartbackpackapp.app.ErrorHandler;
import com.nyp.fypj.smartbackpackapp.app.SAPWizardApplication;
import com.nyp.fypj.smartbackpackapp.logon.SecureStoreManager;

import com.sap.cloud.mobile.onboarding.passcode.ChangePasscodeActivity;
import com.sap.cloud.mobile.onboarding.passcode.SetPasscodeActivity;
import com.sap.cloud.mobile.onboarding.passcode.SetPasscodeSettings;
import com.sap.cloud.mobile.onboarding.passcode.EnterPasscodeSettings;

import com.sap.cloud.mobile.foundation.common.ClientProvider;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import android.content.res.Resources;
import com.nyp.fypj.smartbackpackapp.app.ErrorMessage;
import android.preference.ListPreference;
import android.support.annotation.NonNull;
import android.widget.Toast;
import com.sap.cloud.mobile.foundation.logging.Logging;
import com.nyp.fypj.smartbackpackapp.logon.ClientPolicyManager;


/**
* This fragment represents the settings screen.
*/

public class SettingsFragment extends PreferenceFragment
implements Logging.UploadListener
{

    private SAPWizardApplication sapWizardApplication;
    private SecureStoreManager secureStoreManager;
    private ErrorHandler errorHandler;
    private ClientPolicyManager clientPolicyManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sapWizardApplication = ((SAPWizardApplication)getActivity().getApplication());
        secureStoreManager = ((SAPWizardApplication)getActivity().getApplication()).getSecureStoreManager();
        errorHandler = ((SAPWizardApplication)getActivity().getApplication()).getErrorHandler();
        clientPolicyManager = ((SAPWizardApplication)getActivity().getApplication()).getClientPolicyManager();

        addPreferencesFromResource(R.xml.preferences);
        final ListPreference logLevelPreference = (ListPreference) findPreference(getActivity().getApplicationContext().getString(R.string.log_level));

        Resources res = getActivity().getResources();

        CharSequence[] entries = new CharSequence[]{
                res.getString(R.string.log_level_path),
                res.getString(R.string.log_level_debug),
                res.getString(R.string.log_level_info),
                res.getString(R.string.log_level_warning),
                res.getString(R.string.log_level_error),
                res.getString(R.string.log_level_none)};

        CharSequence[] entryValues = new CharSequence[]{
                String.valueOf(Level.ALL.levelInt),
                String.valueOf(Level.DEBUG.levelInt),
                String.valueOf(Level.INFO.levelInt),
                String.valueOf(Level.WARN.levelInt),
                String.valueOf(Level.ERROR.levelInt),
                String.valueOf(Level.OFF.levelInt)};

        // IMPORTANT - This is where set entries...
        logLevelPreference.setEntries(entries);
        logLevelPreference.setEntryValues(entryValues);
        logLevelPreference.setPersistent(true);

        Level logLevelStored = secureStoreManager.getWithPasscodePolicyStore(
                passcodePolicyStore -> passcodePolicyStore.getSerializable(ClientPolicyManager.KEY_CLIENT_LOG_LEVEL)
        );
        int i = Arrays.asList(entryValues).indexOf(String.valueOf(logLevelStored.levelInt));
        logLevelPreference.setSummary(entries[i]);
        logLevelPreference.setValue(String.valueOf(logLevelStored.levelInt));
        logLevelPreference.setOnPreferenceChangeListener((preference, newValue) -> {

            // Get the new value
            Level logLevel = Level.toLevel(Integer.valueOf((String) newValue));
            // Write the new value to the SecureStore
            secureStoreManager.doWithPasscodePolicyStore(passcodePolicyStore -> {
                passcodePolicyStore.put(ClientPolicyManager.KEY_CLIENT_LOG_LEVEL, logLevel);
            });

            int j = Arrays.asList(entryValues).indexOf(newValue);
            // Initialize logging
            Logging.getRootLogger().setLevel(logLevel);
            preference.setSummary(entries[j]);
            return true;
        });
        Preference changePasscodePreference = findPreference(getActivity().getApplicationContext().getString(R.string.manage_passcode));
        changePasscodePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (secureStoreManager.isUserPasscodeSet()) {
                    Intent intent = new Intent(SettingsFragment.this.getActivity(), ChangePasscodeActivity.class);
                    SetPasscodeSettings setPasscodeSettings = new SetPasscodeSettings();
                    setPasscodeSettings.setSkipButtonText(getString(R.string.skip_passcode));
                    setPasscodeSettings.saveToIntent(intent);
                    int currentRetryCount = secureStoreManager.getWithPasscodePolicyStore(
                            passcodePolicyStore -> passcodePolicyStore.getInt(ClientPolicyManager.KEY_RETRY_COUNT)
                    );
                    int retryLimit = clientPolicyManager.getClientPolicy(false).getPasscodePolicy().getRetryLimit();
                    if (retryLimit <= currentRetryCount) {
                        EnterPasscodeSettings enterPasscodeSettings = new EnterPasscodeSettings();
                        enterPasscodeSettings.setFinalDisabled(true);
                        enterPasscodeSettings.saveToIntent(intent);
                    }
                    SettingsFragment.this.getActivity().startActivity(intent);
                } else {
                    Intent intent = new Intent(SettingsFragment.this.getActivity(), SetPasscodeActivity.class);
                    SetPasscodeSettings setPasscodeSettings = new SetPasscodeSettings();
                    setPasscodeSettings.setSkipButtonText(getString(R.string.skip_passcode));
                    setPasscodeSettings.saveToIntent(intent);
                    SettingsFragment.this.getActivity().startActivity(intent);
                }
                return false;
            }
        });

        //Reset App
        Preference resetAppPreference = findPreference(getActivity().getApplicationContext().getString(R.string.reset_app));
        resetAppPreference.setOnPreferenceClickListener((preference) -> {
            sapWizardApplication.resetAppWithUserConfirmation();
            return false;
        });
        // Uploading the logs
        final Preference logUploadPreference = findPreference(getActivity().getApplicationContext().getString(R.string.upload_log));
        logUploadPreference.setOnPreferenceClickListener((preference) -> {
            Logging.uploadLog(ClientProvider.get(), sapWizardApplication.getSettingsParameters());
            logUploadPreference.setEnabled(false);
            return false;
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        Logging.addLogUploadListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Logging.removeLogUploadListener(this);
    }
    @Override
    public void onSuccess() {
        enableLogUploadButton();
        Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.log_upload_ok), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(@NonNull Throwable throwable) {
        enableLogUploadButton();
        String errorCause = throwable.getLocalizedMessage();
        ErrorMessage errorMessage = new ErrorMessage(getActivity().getResources().getString(R.string.log_upload_failed), errorCause, new Exception(throwable), false);
        errorHandler.sendErrorMessage(errorMessage);
    }

    @Override
    public void onProgress(int i) {
        // You could add a progress indicator and update it from here
    }

    private void enableLogUploadButton() {
        final Preference logUploadPreference = findPreference(getActivity().getApplicationContext().getString(R.string.upload_log));
        logUploadPreference.setEnabled(true);
    }
}
