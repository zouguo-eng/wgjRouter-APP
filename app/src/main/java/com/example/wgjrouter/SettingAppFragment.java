package com.example.wgjrouter;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

public class SettingAppFragment extends PreferenceFragment {
    private CheckBoxPreference protect_ear_volume;
    private EditTextPreference ideal_ear_volume;
    private EditTextPreference max_concurrent_task;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.selfstyle_setting_preference);

        protect_ear_volume = (CheckBoxPreference) findPreference("protect_ear_volume");
        ideal_ear_volume = (EditTextPreference) findPreference("ideal_ear_volume");
        max_concurrent_task = (EditTextPreference) findPreference("max_concurrent_task");

        if(protect_ear_volume.isChecked()){
            ideal_ear_volume.setEnabled(true);
        }else{
            ideal_ear_volume.setEnabled(false);
        }

        protect_ear_volume.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(newValue.toString().equals("true")){
                    ideal_ear_volume.setEnabled(true);
                }else{
                    ideal_ear_volume.setEnabled(false);
                }
                return true;
            }
        });

        ideal_ear_volume.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(newValue.toString().equals("")){
                    return false;
                }

                if(Integer.parseInt(newValue.toString()) > 100){
                    return false;
                }
                return true;
            }
        });

        max_concurrent_task.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(newValue.toString().equals("")){
                    return false;
                }

                if(Integer.parseInt(newValue.toString()) <= 0){
                    preference.setDefaultValue(1);
                }else if(Integer.parseInt(newValue.toString()) > 10){
                    preference.setDefaultValue(10);
                }
                return true;
            }
        });
    }
}
