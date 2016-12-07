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

public class Firewall {

    private static List<AppModel> installedApps;
    private static HashSet<String> denyRules = new HashSet<String>();

    public static void initialize(final Context context) {

        // Get firewall deny rules
        List<String> denied = getFirewall(context).getIptablesDenyRules();

        // Add deny rules to hash set
        for (String deny : denied)
            denyRules.add(deny.split(";")[2]);
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

    public static List<AppModel> getApps(final Context context) {
        return (installedApps == null ? getInstalledApps(context) : installedApps);
    }

    private static List<AppModel> getInstalledApps(final Context context) {

        installedApps = new ArrayList<AppModel>();

        // Get app infos from package manager
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

    public static void createRules(final Context context) {

        final List<String> denyRules = new ArrayList<String>();

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
        firewall.cleanIptablesDenyRules();
        firewall.addIptablesDenyRules(denyRules);
        firewall.setIptablesOption(true);
    }

    public static boolean disableRules(final Context context) {
        FirewallPolicy firewall = getFirewall(context);
        firewall.cleanIptablesDenyRules();
        return firewall.setIptablesOption(false);
    }
}