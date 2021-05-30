package com.trolololo.workbee.jogger.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.trolololo.workbee.jogger.R;
import com.trolololo.workbee.jogger.domain.Machine;

import java.util.List;

public class MachineAdapter extends BaseAdapter {
    private List<Machine> machines;
    private LayoutInflater inflater = null;

    public MachineAdapter(Context context, List<Machine> machines) {
        this.machines = machines;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return machines.size();
    }

    @Override
    public Object getItem(int position) {
        return machines.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setMachines(List<Machine> machines) {
        this.machines = machines;
    }

    private static class ViewHolder {
        TextView urlView;
        ImageView image;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Machine p = (Machine) getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.machine_list_entry, parent, false);
            holder = new ViewHolder();

            holder.urlView = convertView.findViewById(R.id.machine_list_entry_url);
            holder.image = convertView.findViewById(R.id.machine_type_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.urlView.setText(p.getUrl());
        convertView.setBackgroundResource(p.isSelected() ? R.color.lightGrey : R.color.white);
        return convertView;
    }
}
