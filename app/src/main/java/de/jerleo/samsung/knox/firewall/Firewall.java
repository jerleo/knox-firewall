package de.jerleo.samsung.knox.firewall;

import android.Manifest;
import android.app.enterprise.EnterpriseDeviceManager;
import android.app.enterprise.FirewallPolicy;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

class Firewall {

    private static final HashSet<String> denyRules = new HashSet<>();
    private static List<AppModel> installedApps;

    public static void createRules(final Context context) {

        final List<String> denyRules = new ArrayList<>();

        // Block all traffic on the data interface
        final String hostName = "*";
        final String portNumber = "*";
        final String portLocation = "*";
        final String netInterface = "data";

        // Add rules for all denied apps
        for (AppModel app : installedApps)
            if (app.isDenied())
                denyRules.add(hostName + ":" + portNumber + ";"
                        + portLocation + ";"
                        + app.getPackageName() + ";"
                        + netInterface);

        // Re-create deny rules
        FirewallPolicy firewall = getFirewall(context);
        assert firewall != null;
        firewall.cleanIptablesDenyRules();
        firewall.addIptablesDenyRules(denyRules);
        firewall.setIptablesOption(true);
    }

    public static List<AppModel> getApps(final Context context) {

        return (installedApps == null ? getInstalledApps(context) : installedApps);
    }

    private static FirewallPolicy getFirewall(final Context context) {

        // Get enterprise device manager instance
        final EnterpriseDeviceManager edm = (EnterpriseDeviceManager) context.getSystemService(
                EnterpriseDeviceManager.ENTERPRISE_POLICY_SERVICE);

        // Leave when unable to retrieve instance
        if (edm == null)
            return null;

        return edm.getFirewallPolicy();
    }

    private static List<AppModel> getInstalledApps(final Context context) {

        installedApps = new ArrayList<>();

        // Get app info from package manager
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> appInfos = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo appInfo : appInfos) {

            String packageName = appInfo.packageName;

            // Only add apps with internet access
            if (pm.checkPermission(Manifest.permission.INTERNET,
                    packageName) == PackageManager.PERMISSION_GRANTED) {

                AppModel app = new AppModel();
                app.setIcon(appInfo.loadIcon(pm));
                app.setPackageName(packageName);
                app.setLabel(pm.getApplicationLabel(appInfo).toString());
                app.setSelected(false);

                // Check if app has deny rule
                app.setDenied(denyRules.contains(packageName));

                installedApps.add(app);
            }
        }

        Collections.sort(installedApps);
        return installedApps;
    }

    public static void initialize(final Context context) {

        FirewallPolicy firewall = getFirewall(context);

        if (firewall == null)
            return;

        // Get firewall deny rules
        List<String> denied = firewall.getIptablesDenyRules();

        // Add deny rules to hash set
        for (String deny : denied)
            denyRules.add(deny.split(";")[2]);
    }
}