package edu.ucdenver.androidsecurity;




import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

public class DevicePolicyAdmin extends Activity {
    private final static String LOG_TAG = "DevicePolicyAdmin";
    DevicePolicyManager sindhuDevicePolicyManager;
    ComponentName sindhuDevicePolicyAdmin;
    private CheckBox sindhuAdminEnabledCheckbox;
    protected static final int REQUEST_ENABLE = 1;
    protected static final int SET_PASSWORD = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_policy_admin);
        sindhuDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        sindhuDevicePolicyAdmin = new ComponentName(this,
                MyDevicePolicyReceiver.class);

        sindhuAdminEnabledCheckbox = (CheckBox) findViewById(R.id.checkBox1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isMyDevicePolicyReceiverActive()) {
            sindhuAdminEnabledCheckbox.setChecked(true);
        } else {
            sindhuAdminEnabledCheckbox.setChecked(false);
        }
        sindhuAdminEnabledCheckbox
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        if (isChecked) {
                            Intent intent = new Intent(
                                    DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                            intent.putExtra(
                                    DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                                    sindhuDevicePolicyAdmin);
                            intent.putExtra(
                                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                    getString(R.string.admin_explanation));
                            startActivityForResult(intent, REQUEST_ENABLE);
                        } else {
                            sindhuDevicePolicyManager
                                    .removeActiveAdmin(sindhuDevicePolicyAdmin);
                        }
                    }
                });
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_ENABLE:
                    Log.v(LOG_TAG, "Enabling Policies Now");
                    sindhuDevicePolicyManager.setMaximumTimeToLock(
                            sindhuDevicePolicyAdmin, 30000L);
                    sindhuDevicePolicyManager.setMaximumFailedPasswordsForWipe(
                           sindhuDevicePolicyAdmin, 50);
                    sindhuDevicePolicyManager.setPasswordQuality(
                            sindhuDevicePolicyAdmin,
                            DevicePolicyManager.PASSWORD_QUALITY_COMPLEX);
                    sindhuDevicePolicyManager.setCameraDisabled(
                            sindhuDevicePolicyAdmin, true);
                    boolean isSufficient = sindhuDevicePolicyManager
                            .isActivePasswordSufficient();
                    if (isSufficient) {
                        sindhuDevicePolicyManager.lockNow();
                    } else {
                        Intent setPasswordIntent = new Intent(
                                DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
                        startActivityForResult(setPasswordIntent, SET_PASSWORD);
                        sindhuDevicePolicyManager.setPasswordExpirationTimeout(
                                sindhuDevicePolicyAdmin, 1000000L);
                    }
                    break;
            }
        }
    }

    private boolean isMyDevicePolicyReceiverActive() {
        return sindhuDevicePolicyManager
                .isAdminActive(sindhuDevicePolicyAdmin);
    }

    public static class MyDevicePolicyReceiver extends DeviceAdminReceiver {

        @Override
        public void onDisabled(Context context, Intent intent) {
            Toast.makeText(context, " Device Admin Disabled",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onEnabled(Context context, Intent intent) {
            Toast.makeText(context, "Device Admin is now enabled",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public CharSequence onDisableRequested(Context context, Intent intent) {
            CharSequence disableRequestedSeq = "Requesting to disable Device Admin";
            return disableRequestedSeq;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void onPasswordChanged(Context context, Intent intent) {
            Toast.makeText(context, "Device password is now changed",
                    Toast.LENGTH_SHORT).show();
            DevicePolicyManager localDPM = (DevicePolicyManager) context
                    .getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName localComponent = new ComponentName(context,
                    MyDevicePolicyReceiver.class);
            localDPM.setPasswordExpirationTimeout(localComponent, 0L);
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void onPasswordExpiring(Context context, Intent intent) {
            // This would require API 11 an above
            Toast.makeText(
                    context,
                    " Device password is going to expire, please change to a new password",
                    Toast.LENGTH_LONG).show();

            DevicePolicyManager localDPM = (DevicePolicyManager) context
                    .getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName localComponent = new ComponentName(context,
                    MyDevicePolicyReceiver.class);
            long expr = localDPM.getPasswordExpiration(localComponent);
            long delta = expr - System.currentTimeMillis();
            boolean expired = delta < 0L;
            if (expired) {
                localDPM.setPasswordExpirationTimeout(localComponent, 1000000L);
                Intent passwordChangeIntent = new Intent(
                        DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
                passwordChangeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(passwordChangeIntent);
            }
        }

        @Override
        public void onPasswordFailed(Context context, Intent intent) {
            Toast.makeText(context, "Password failed", Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onPasswordSucceeded(Context context, Intent intent) {
            Toast.makeText(context, "Access Granted", Toast.LENGTH_SHORT)
                    .show();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG,
                    "MyDevicePolicyReciever Received: " + intent.getAction());
            super.onReceive(context, intent);
        }
    }
}
