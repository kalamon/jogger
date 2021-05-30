package com.trolololo.workbee.jogger.domain;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class Machine implements Serializable {
    private static final String TAG = Machine.class.getName();
    private static final String P = ".machine.";

    private String guid;
    private String url;
    private boolean selected;

    public Machine() {
    }

    public Machine(String guid, String url) {
        this.guid = guid;
        this.url = url;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getUrl() {
        return url != null ? url : "";
    }

    public void setUrl(String url) {
        this.url = url;
    }
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }


    public static List<Machine> loadAll(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int count = preferences.getInt(Machine.class.getCanonicalName() + ".machinesCount", 0);
        List<Machine> result = Lists.newArrayList();
        for (int i = 0; i < count; ++i) {
            String guid = preferences.getString(Machine.class.getCanonicalName() + P + i + ".guid", null);
            if (guid == null) {
                continue;
            }
            String url = preferences.getString(Machine.class.getCanonicalName() + P + i + ".url", "");

            result.add(new Machine(guid, url));
        }
        result.sort((o1, o2) -> o1.getUrl().compareTo(o2.getUrl()));
        return result;
    }

    public static Machine getLastOpenProfile(Context context, List<Machine> ofThese) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String guid = preferences.getString(Machine.class.getCanonicalName() + P + ".lastopen", null);
        if (guid == null) {
            return null;
        }
        Stream<Machine> stream = ofThese.stream().filter(machine -> machine.guid.equals(guid));
        return stream.findFirst().orElse(null);
    }

    public static void setLastOpenProfile(Context context, Machine machine) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Machine.class.getCanonicalName() + P + ".lastopen", machine != null ? machine.getGuid() : null);
        editor.commit();
    }

    public static List<Machine> saveAll(Context context, List<Machine> machines) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Machine.class.getCanonicalName() + ".machinesCount", machines.size());
        int i = 0;
        for (Machine machine : machines) {
            editor.putString(Machine.class.getCanonicalName() + P + i + ".guid", machine.getGuid());
            editor.putString(Machine.class.getCanonicalName() + P + i + ".url", machine.getUrl());
            ++i;
        }
        editor.commit();
        return machines;
    }

    public static Machine save(Context context, Machine machine) {
        if (machine.getGuid() == null) {
            return add(context, machine);
        }
        return update(context, machine);
    }

    private static Machine add(Context context, Machine machine) {
        List<Machine> machines = loadAll(context);
        UUID uuid = UUID.randomUUID();
        machine.setGuid(uuid.toString());
        machines.add(machine);
        saveAll(context, machines);
        return machine;
    }

    private static Machine update(Context context, Machine machine) {
        List<Machine> machines = loadAll(context);
        List<Machine> toSave = Lists.newArrayList();
        for (Machine p : machines) {
            if (p.getGuid().equals(machine.getGuid())) {
                toSave.add(machine);
            } else {
                toSave.add(p);
            }
        }
        saveAll(context, toSave);
        return machine;
    }

    public static List<Machine> remove(Context context, Machine machine) {
        List<Machine> machines = loadAll(context);
        List<Machine> toSave = Lists.newArrayList();
        for (Machine p : machines) {
            if (p.getGuid().equals(machine.getGuid())) {
                continue;
            }
            toSave.add(p);
        }
        saveAll(context, toSave);
        return toSave;
    }
}
