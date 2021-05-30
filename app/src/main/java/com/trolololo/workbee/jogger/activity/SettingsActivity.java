package com.trolololo.workbee.jogger.activity;

import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.trolololo.workbee.jogger.R;

public class SettingsActivity extends AppCompatActivity {

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = (preference, value) -> {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference.setSummary(
                    index >= 0
                            ? listPreference.getEntries()[index]
                            : null);

        } else {
            preference.setSummary(stringValue);
        }
        return true;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        getSupportFragmentManager().beginTransaction().replace(R.id.settings, new PreferencesRoot()).commit();
    }

    public static class PreferencesRoot extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.settings, rootKey);

            Preference connectTimeoutPref = findPreference("preference_connect_timeout");
            if (connectTimeoutPref != null) {
                connectTimeoutPref.setOnPreferenceChangeListener(new NumberValidator());
            }
            updatePreferenceValue(connectTimeoutPref, null);
            Preference readTimeoutPref = findPreference("preference_read_timeout");
            if (readTimeoutPref != null) {
                readTimeoutPref.setOnPreferenceChangeListener(new NumberValidator());
            }
            updatePreferenceValue(readTimeoutPref, null);
        }


        private class NumberValidator implements Preference.OnPreferenceChangeListener {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue == null) {
                    return false;
                }
                try {
                    int val = Integer.parseInt(newValue.toString());
                    boolean ok = val > 0;
                    if (ok) {
                        updatePreferenceValue(preference, val);
                    }
                    return ok;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }

        private void updatePreferenceValue(Preference preference, Object value) {
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    value == null
                            ? PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), "")
                            : value
            );
        }

    }
}