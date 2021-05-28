package com.trolololo.workbee.jogger.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.trolololo.workbee.jogger.R;
import com.trolololo.workbee.jogger.domain.Profile;

import java.util.List;

public abstract class AbstractNameAndIdViewAdapter extends ArrayAdapter<NameAndIdWrapper> {
    private final LayoutInflater inflater;
    protected final Profile profile;

    public AbstractNameAndIdViewAdapter(Context context, Profile profile, int resource, List<NameAndIdWrapper> elements) {
        super(context, resource, elements);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.profile = profile;
    }

    private static class ViewHolder {
        TextView fieldView;
    }

    protected abstract int getSpinnerId();
    protected abstract int getTextId();
    protected abstract boolean isSelected(NameAndIdWrapper item);

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        NameAndIdWrapper item = getItem(position);
        AbstractNameAndIdViewAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(getSpinnerId(), parent, false);
            holder = new AbstractNameAndIdViewAdapter.ViewHolder();

            holder.fieldView = convertView.findViewById(getTextId());
            convertView.setTag(holder);
        } else {
            holder = (AbstractNameAndIdViewAdapter.ViewHolder) convertView.getTag();
        }

        holder.fieldView.setText(item != null ? item.getName() : "");
        convertView.setBackgroundResource(
            isSelected(item)
                ? R.color.lightGrey
                : android.R.drawable.list_selector_background
        );
        return convertView;
    }

}
