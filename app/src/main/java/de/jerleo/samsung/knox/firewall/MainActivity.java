package de.jerleo.samsung.knox.firewall;

import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.app.enterprise.license.EnterpriseLicenseManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final static int DEVICE_ADMIN_ADD_RESULT_ENABLE = 1;
    private final static int NUMBER_OF_TABS = 2;

    private static ComponentName admin;
    private static DevicePolicyManager dpm;
    private static EnterpriseLicenseManager elm;

    private static AppAdapter[] adapters;
    private static List<AppModel> apps;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Rules only available with administrator and license
        if (hasActiveAdministrator() && hasActiveLicense())
            Firewall.initialize(this);

        // Initialize static member
        if (adapters == null) {
            List<AppModel> apps = Firewall.getApps(this);
            String[] titles = getResources().getStringArray(R.array.tab_titles);

            // Create adapters for allowed and denied apps
            adapters = new AppAdapter[NUMBER_OF_TABS];
            adapters[0] = new AppAdapter(this, R.id.app_row, apps, titles[0], false);
            adapters[1] = new AppAdapter(this, R.id.app_row, apps, titles[1], true);

            // Link the adpaters
            adapters[0].setOther(adapters[1]);
            adapters[1].setOther(adapters[0]);
        }

        setContentView(R.layout.main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    private void activateAdministrator() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin);
        startActivityForResult(intent, DEVICE_ADMIN_ADD_RESULT_ENABLE);
    }

    private void activateLicense() {
        final EditText edit = new EditText(MainActivity.this);
        final Toast invalid = Toast.makeText(this, getString(R.string.license_key_invalid),
                Toast.LENGTH_LONG);

        AlertDialog.Builder alert = new AlertDialog.Builder(this)
                .setTitle(R.string.license_activation)
                .setMessage(R.string.license_key_paste)
                .setView(edit)
                .setPositiveButton(R.string.license_activate, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String key = edit.getText().toString();

                        // Check for valid key length
                        if (key.length() == 128)

                            // Activate license and restart app
                            elm.activateLicense(key);

                        else
                            invalid.show();
                    }
                });
        alert.show();
    }

    private boolean hasActiveAdministrator() {
        admin = new ComponentName(MainActivity.this, AdminReceiver.class);
        dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        return dpm.isAdminActive(admin);
    }

    private boolean hasActiveLicense() {
        try {
            Class.forName("android.app.enterprise.license.EnterpriseLicenseManager");
            elm = EnterpriseLicenseManager.getInstance(this);
        } catch (Exception e) {
            return false;
        }
        return (checkCallingOrSelfPermission("android.permission.sec.MDM_FIREWALL") ==
                PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        boolean allowed = (mViewPager.getCurrentItem() == 0);
        boolean hasAdmin = hasActiveAdministrator();
        boolean hasLicense = hasActiveLicense();

        MenuItem itemAdmin = menu.findItem(R.id.action_admin);
        MenuItem itemLicense = menu.findItem(R.id.action_license);
        MenuItem itemAllow = menu.findItem(R.id.action_allow_selected);
        MenuItem itemDeny = menu.findItem(R.id.action_deny_selected);
        MenuItem itemApply = menu.findItem(R.id.action_apply_rules);

        // Set visibility of options menu items
        itemAdmin.setEnabled(!hasAdmin);
        itemLicense.setEnabled(hasAdmin && !hasLicense);

        itemAllow.setEnabled(hasAdmin && hasLicense);
        itemAllow.setVisible(!allowed);

        itemDeny.setEnabled(hasAdmin && hasLicense);
        itemDeny.setVisible(allowed);

        itemApply.setEnabled(hasAdmin && hasLicense);

        invalidateOptionsMenu();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Get adapter of current tab
        AppList appList = (AppList) mSectionsPagerAdapter.getItem(mViewPager.getCurrentItem());
        AppAdapter adapter = (AppAdapter) appList.getListAdapter();

        // Get selected menu item
        int id = item.getItemId();
        switch (id) {

            case R.id.action_admin:
                activateAdministrator();
                break;

            case R.id.action_license:
                activateLicense();
                break;

            case R.id.action_toggle_select:
                adapter.toggleSelection();
                break;

            case R.id.action_allow_selected:
                adapter.denySelection(false);
                break;

            case R.id.action_deny_selected:
                adapter.denySelection(true);
                break;

            case R.id.action_apply_rules:
                applyRules();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void applyRules() {

        // Get context
        final Context context = this;

        // Show progress
        final ProgressDialog progress = new ProgressDialog(this);

        // Finish message
        final Toast completed = Toast.makeText(this, getString(R.string.firewall_rules_complete), Toast.LENGTH_SHORT);
        progress.setIndeterminate(true);
        progress.setProgress(0);
        progress.show();

        // Handler to dismiss progress
        final Handler handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                progress.dismiss();
                completed.show();
            }
        };

        // Apply firewall rules
        new Thread(new Runnable() {

            @Override
            public void run() {
                progress.setMessage(getString(R.string.firewall_rules_create));
                Firewall.createRules(context);
                handler.sendMessage(handler.obtainMessage());
            }
        }).start();
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUMBER_OF_TABS;
        }

        @Override
        public Fragment getItem(int position) {
            AppList list = new AppList();
            list.setListAdapter(adapters[position]);

            // Make screen rotation work
            list.setRetainInstance(true);
            return list;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return adapters[position].getTitle();
        }
    }
}