package com.trolololo.workbee.jogger.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.trolololo.workbee.jogger.R;
import com.trolololo.workbee.jogger.domain.Profile;

import java.util.List;

public class ProfileAdapter extends BaseAdapter {
    private Context context;
    private List<Profile> profiles;
    private LayoutInflater inflater = null;

    public ProfileAdapter(Context context, List<Profile> profiles) {
        this.context = context;
        this.profiles = profiles;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return profiles.size();
    }

    @Override
    public Object getItem(int position) {
        return profiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
    }

    private static class ViewHolder {
        TextView urlView;
        TextView fieldView;
        ImageView image;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Profile p = (Profile) getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.profile_list_entry, parent, false);
            holder = new ViewHolder();

            holder.urlView = convertView.findViewById(R.id.profile_list_entry_url);
            holder.fieldView = convertView.findViewById(R.id.profile_list_entry_field_or_storedop);
            holder.image = convertView.findViewById(R.id.profile_type_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.urlView.setText(p.getUrl());
        holder.fieldView.setText(p.isLegacyDateFieldProfile() ? p.getField() : p.getOperationName());
//        holder.image.setImageResource(p.isLegacyDateFieldProfile()
//            ? R.drawable.ic_date_range_black_24dp
//            : R.drawable.ic_play_circle_outline_black_24dp
//        );
        convertView.setBackgroundResource(p.isSelected() ? R.color.lightGrey : R.color.white);
        return convertView;
    }
}
