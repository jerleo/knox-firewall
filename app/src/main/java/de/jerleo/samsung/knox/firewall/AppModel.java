package de.jerleo.samsung.knox.firewall;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

public class AppModel implements Comparable<AppModel> {

    private Drawable icon;
    private String label;
    private String packageName;
    private boolean denied;
    private boolean selected;

    @Override
    public int compareTo(@NonNull AppModel appModel) {

        return this.label.compareTo(appModel.label);
    }

    public Drawable getIcon() {

        return icon;
    }

    public String getLabel() {

        return label;
    }

    public String getPackageName() {

        return packageName;
    }

    public boolean isDenied() {

        return denied;
    }

    public boolean isSelected() {

        return selected;
    }

    public void setDenied(boolean denied) {

        this.denied = denied;
    }

    public void setIcon(Drawable icon) {

        this.icon = icon;
    }

    public void setLabel(String label) {

        this.label = label;
    }

    public void setPackageName(String packageName) {

        this.packageName = packageName;
    }

    public void setSelected(boolean selected) {

        this.selected = selected;
    }

}