package com.trolololo.workbee.jogger.domain;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class Profile implements Serializable {
    private static final String TAG = Profile.class.getName();
    private static final String P = ".profile.";

    private String guid;
    private String url;
    private String login;
    private String password;
    private String field;
    private int operationId;
    private String operationName;
    private boolean selected;
    private boolean legacyDateFieldProfile;

    public Profile() {
    }

    public Profile(String guid, String url, String login, String password, String field, boolean legacyDateFieldProfile, int operationId, String operationName) {
        this.guid = guid;
        this.url = url;
        this.login = login;
        this.password = password;
        this.field = field;
        this.legacyDateFieldProfile = legacyDateFieldProfile;
        this.operationId = operationId;
        this.operationName = operationName;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getUrl() {
        return url != null ? url : "";
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setLegacyDateFieldProfile(boolean legacyDateFieldProfile) {
        this.legacyDateFieldProfile = legacyDateFieldProfile;
    }

    public boolean isLegacyDateFieldProfile() {
        return legacyDateFieldProfile;
    }

    public int getOperationId() {
        return operationId;
    }

    public void setOperationId(int operationId) {
        this.operationId = operationId;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
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
            String login = preferences.getString(Profile.class.getCanonicalName() + P + i + ".login", "");
            String password = preferences.getString(Profile.class.getCanonicalName() + P + i + ".password", "");
            String field = preferences.getString(Profile.class.getCanonicalName() + P + i + ".field", "");
            boolean legacy = preferences.getBoolean(Profile.class.getCanonicalName() + P + i + ".datefieldprofile", true);
            int operationId = preferences.getInt(Profile.class.getCanonicalName() + P + i + ".operationid", 0);
            String operationName = preferences.getString(Profile.class.getCanonicalName() + P + i + ".operationname", "");

            result.add(new Profile(guid, url, login, password, field, legacy, operationId, operationName));
        }
        result.sort((o1, o2) -> o1.getUrl().compareTo(o2.getUrl()));
        return result;
    }

    public static List<Profile> saveAll(Context context, List<Profile> profiles) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Profile.class.getCanonicalName() + ".profilesCount", profiles.size());
        int i = 0;
        for (Profile profile : profiles) {
            editor.putString(Profile.class.getCanonicalName() + P + i + ".guid", profile.getGuid());
            editor.putString(Profile.class.getCanonicalName() + P + i + ".url", profile.getUrl());
            editor.putString(Profile.class.getCanonicalName() + P + i + ".login", profile.getLogin());
            editor.putString(Profile.class.getCanonicalName() + P + i + ".password", profile.getPassword());
            editor.putString(Profile.class.getCanonicalName() + P + i + ".field", profile.getField());
            editor.putBoolean(Profile.class.getCanonicalName() + P + i + ".datefieldprofile", profile.isLegacyDateFieldProfile());
            editor.putInt(Profile.class.getCanonicalName() + P + i + ".operationid", profile.getOperationId());
            editor.putString(Profile.class.getCanonicalName() + P + i + ".operationname", profile.getOperationName());
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

    public static Profile clone(Profile profile) {
        return new Profile(
            null,
            profile.getUrl(), profile.getLogin(), profile.getPassword(),
            profile.getField(), profile.isLegacyDateFieldProfile(),
            profile.getOperationId(), profile.getOperationName()
        );
    }
}
