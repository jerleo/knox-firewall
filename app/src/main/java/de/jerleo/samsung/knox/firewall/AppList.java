package de.jerleo.samsung.knox.firewall;

import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

public class AppList extends ListFragment {

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        AppAdapter adapter = (AppAdapter) getListAdapter();
        AppModel app = adapter.getItem(position);
        app.setSelected(!app.isSelected());
        adapter.notifyDataSetChanged();
    }
}