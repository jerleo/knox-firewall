package de.jerleo.samsung.knox.firewall;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AppAdapter extends ArrayAdapter<AppModel> {

    private final Context context;
    private final List<AppModel> apps;
    private final String title;
    private final boolean denied;

    private AppAdapter other;
    private List<Integer> index;

    public AppAdapter(Context context, int resource,
                      List<AppModel> apps, String title, boolean denied) {
        super(context, resource, apps);
        this.context = context;
        this.denied = denied;
        this.title = title;
        this.apps = apps;
        updateIndex();
    }

    public String getTitle() {
        return title;
    }

    private void updateIndex() {
        // Update indexes for apps on current tab
        index = new ArrayList<Integer>();
        for (AppModel app : apps)
            if (app.isDenied() == denied)
                index.add(apps.indexOf(app));
    }

    public void setOther(AppAdapter other) {
        this.other = other;
    }

    public void denySelection(boolean deny) {
        boolean changed = false;
        for (Integer ix : index) {
            AppModel app = apps.get(ix);
            if (app.isSelected()) {
                app.setDenied(deny);
                app.setSelected(false);
                changed = true;
            }
        }
        if (changed) {
            notifyDataSetChanged();
            if (other != null)
                other.notifyDataSetChanged();
        }
    }

    public void toggleSelection() {
        for (Integer ix : index) {
            AppModel app = apps.get(ix);
            app.setSelected(!app.isSelected());
        }
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        this.updateIndex();
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return index.size();
    }

    @Override
    public AppModel getItem(int position) {
        return apps.get(index.get(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View row = convertView;
        if (row == null) {

            final LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.app, parent, false);

            final ViewHolder holder = new ViewHolder();
            holder.selected = (CheckBox) row.findViewById(R.id.app_selected);
            holder.icon = (ImageView) row.findViewById(R.id.app_icon);
            holder.label = (TextView) row.findViewById(R.id.app_label);
            holder.packageName = (TextView) row.findViewById(R.id.app_package_name);

            row.setTag(holder);
        }

        AppModel app = getItem(position);
        if (app.isSelected())
            row.setAlpha(1.0f);
        else
            row.setAlpha(0.5f);

        final ViewHolder holder = (ViewHolder) row.getTag();
        holder.selected.setChecked(app.isSelected());
        holder.icon.setImageDrawable(app.getIcon());
        holder.label.setText(app.getLabel());
        holder.packageName.setText(app.getPackageName());

        return row;
    }

    private static class ViewHolder {
        public CheckBox selected;
        public ImageView icon;
        public TextView label;
        public TextView packageName;
    }
}