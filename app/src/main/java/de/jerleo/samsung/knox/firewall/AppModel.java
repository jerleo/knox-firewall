package de.jerleo.samsung.knox.firewall;

import android.graphics.drawable.Drawable;

public class AppModel implements Comparable<AppModel> {

    private Drawable icon;
    private String label;
    private String packageName;
    private boolean denied;
    private boolean selected;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isDenied() {
        return denied;
    }

    public void setDenied(boolean denied) {
        this.denied = denied;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    @Override
    public int compareTo(AppModel appModel) {
        return this.label.compareTo(appModel.label);
    }

}