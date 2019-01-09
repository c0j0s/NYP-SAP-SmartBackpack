package com.nyp.fypj.smartbackpackapp.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.nyp.fypj.smartbackpackapp.R;

import java.util.List;

/** This is an activity which is presented as a dialog for presenting
 * error notifications to the user. The notifications can have a short title, a detailed
 * message describing the error and its consequences. Finally, notifications have a so-called fatal
 * flag. It it were true, then the application is killed, after the user pressed the OK button.
 */
public class ErrorNotificationDialog extends Activity {

    public static final String TITLE = "error_title";
    public static final String MSG = "error_msg";
    public static final String FATAL = "isFatal";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_notification);

        Intent startIntent = getIntent();
        String title = startIntent.getStringExtra(TITLE);
        String msg = startIntent.getStringExtra(MSG);
        boolean isFatal = startIntent.getBooleanExtra(FATAL, false);

        TextView msgView = findViewById(R.id.error_notification_msg);
        Button okButton = findViewById(R.id.error_notification_button);

        if (title !=null && !title.isEmpty()) {
            setTitle(title);
        } else {
            setTitle(getApplication().getResources().getString(R.string.error));
        }

        okButton.setOnClickListener(v -> {
            if (isFatal) {
                ActivityManager activityManager = (ActivityManager) ErrorNotificationDialog.this.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();
                for (ActivityManager.AppTask task : tasks) {
                    task.finishAndRemoveTask();
                }
            } else {
                ErrorNotificationDialog.this.finish();
            }
        });
        msgView.setText(msg);
    }

    @Override
    public void onBackPressed() {
        // hardware back button is disabled here
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ErrorPresenterByNotification.errorDialogDismissed();
    }
}
