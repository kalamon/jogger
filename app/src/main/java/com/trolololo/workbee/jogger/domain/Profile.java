package com.trolololo.workbee.jogger.domain;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Profile implements Serializable {
    private static final String TAG = Profile.class.getName();
    private static final String P = ".profile.";

    private String guid;
    private String url;
    private boolean selected;

    public Profile() {
    }

    public Profile(String guid, String url) {
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


    public static List<Profile> loadAll(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int count = preferences.getInt(Profile.class.getCanonicalName() + ".profilesCount", 0);
        List<Profile> result = Lists.newArrayList();
        for (int i = 0; i < count; ++i) {
            String guid = preferences.getString(Profile.class.getCanonicalName() + P + i + ".guid", null);
            if (guid == null) {
                continue;
            }
            String url = preferences.getString(Profile.class.getCanonicalName() + P + i + ".url", "");

            result.add(new Profile(guid, url));
        }
        result.sort((o1, o2) -> o1.getUrl().compareTo(o2.getUrl()));
        return result;
    }

    public static Profile getLastOpenProfile(Context context, List<Profile> ofThese) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String guid = preferences.getString(Profile.class.getCanonicalName() + P + ".lastopen", null);
        if (guid == null) {
            return null;
        }
        Stream<Profile> stream = ofThese.stream().filter(profile -> profile.guid.equals(guid));
        return stream.findFirst().orElse(null);
    }

    public static void setLastOpenProfile(Context context, Profile profile) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Profile.class.getCanonicalName() + P + ".lastopen", profile != null ? profile.getGuid() : null);
        editor.commit();
    }

    public static List<Profile> saveAll(Context context, List<Profile> profiles) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Profile.class.getCanonicalName() + ".profilesCount", profiles.size());
        int i = 0;
        for (Profile profile : profiles) {
            editor.putString(Profile.class.getCanonicalName() + P + i + ".guid", profile.getGuid());
            editor.putString(Profile.class.getCanonicalName() + P + i + ".url", profile.getUrl());
            ++i;
        }
        editor.commit();
        return profiles;
    }

    public static Profile save(Context context, Profile profile) {
        if (profile.getGuid() == null) {
            return add(context, profile);
        }
        return update(context, profile);
    }

    private static Profile add(Context context, Profile profile) {
        List<Profile> profiles = loadAll(context);
        UUID uuid = UUID.randomUUID();
        profile.setGuid(uuid.toString());
        profiles.add(profile);
        saveAll(context, profiles);
        return profile;
    }

    private static Profile update(Context context, Profile profile) {
        List<Profile> profiles = loadAll(context);
        List<Profile> toSave = Lists.newArrayList();
        for (Profile p : profiles) {
            if (p.getGuid().equals(profile.getGuid())) {
                toSave.add(profile);
            } else {
                toSave.add(p);
            }
        }
        saveAll(context, toSave);
        return profile;
    }

    public static List<Profile> remove(Context context, Profile profile) {
        List<Profile> profiles = loadAll(context);
        List<Profile> toSave = Lists.newArrayList();
        for (Profile p : profiles) {
            if (p.getGuid().equals(profile.getGuid())) {
                continue;
            }
            toSave.add(p);
        }
        saveAll(context, toSave);
        return toSave;
    }
}
