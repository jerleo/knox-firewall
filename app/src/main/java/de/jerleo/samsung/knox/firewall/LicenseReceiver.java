package de.jerleo.samsung.knox.firewall;

import android.app.enterprise.license.EnterpriseLicenseManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class LicenseReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (action.equals(EnterpriseLicenseManager.ACTION_LICENSE_STATUS)) {
            String result = intent.getStringExtra(EnterpriseLicenseManager.EXTRA_LICENSE_STATUS);
            int message = (result.equals("fail") ?
                    R.string.license_activation_fail :
                    R.string.license_activation_success);
            showToast(context, context.getString(message));
        }
    }

    private void showToast(Context context, CharSequence msg) {

        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}