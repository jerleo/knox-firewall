package de.jerleo.samsung.knox.firewall;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AdminReceiver extends DeviceAdminReceiver {

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {

        return context.getString(R.string.administrator_disable_requested);
    }

    @Override
    public void onDisabled(final Context context, Intent intent) {

        showToast(context, context.getString(R.string.administrator_disable_requested));

    }

    @Override
    public void onEnabled(Context context, Intent intent) {

        showToast(context, context.getString(R.string.administrator_device) + ": " +
                context.getString(R.string.administrator_enabled));
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {

        showToast(context, context.getString(R.string.administrator_device) + ": " +
                context.getString(R.string.password_changed));
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {

        showToast(context, context.getString(R.string.administrator_device) + ": " +
                context.getString(R.string.password_failed));
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {

        showToast(context, context.getString(R.string.administrator_device) + ": " +
                context.getString(R.string.password_succeeded));
    }

    private void showToast(Context context, CharSequence msg) {

        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}