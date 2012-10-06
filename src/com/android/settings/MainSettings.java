/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import com.android.settings.R;

import android.os.Bundle;
import android.os.SystemProperties;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

public class MainSettings extends TabActivity {

	private static TabHost mTabHost;
	private Intent intent;
    private ActionBar mActionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        if ("1".equals(SystemProperties.get("ro.squadzone.build", "0"))) {
           setContentView(R.xml.mainsettings);
        } else {
           return;
        }
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        mActionBar = (ActionBar) findViewById(R.id.actionBar);
        mActionBar.setTitle(R.string.settings_label);
        mActionBar.setHomeLogo(R.drawable.icon, new OnClickListener() {
                  @Override
                  public void onClick(View v) {
                        MainSettings.this.finish();
                  }
        });

		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.getTabWidget().setDividerDrawable(null);
		Intent intent; // Reusable Intent for each tab

		intent = new Intent().setClass(MainSettings.this, Settings.class);
		setupTab(new TextView(this), getString(R.string.variouse_settings_title), intent);

		intent = new Intent().setClass(MainSettings.this, WirelessSettings.class);
		setupTab(new TextView(this), getString(R.string.settings_radio_more), intent);

		intent = new Intent().setClass(MainSettings.this, com.android.settings.wifi.WifiSettings.class);
		setupTab(new TextView(this), getString(R.string.wifi_settings_category), intent);

		intent = new Intent().setClass(MainSettings.this, TetherSettings.class);
		setupTab(new TextView(this), getString(R.string.tether_settings_title_all), intent);

		intent = new Intent().setClass(MainSettings.this, AccessibilitySettings.class);
		setupTab(new TextView(this), getString(R.string.accessibility_settings), intent);

		intent = new Intent().setClass(MainSettings.this, ApplicationSettings.class);
		setupTab(new TextView(this), getString(R.string.applications_settings), intent);

		intent = new Intent().setClass(MainSettings.this, com.android.settings.fuelgauge.PowerUsageSummary.class);
		setupTab(new TextView(this), getString(R.string.power_usage_summary_title), intent);

		intent = new Intent().setClass(MainSettings.this, DateTimeSettings.class);
		setupTab(new TextView(this), getString(R.string.date_and_time_settings_title), intent);

		intent = new Intent().setClass(MainSettings.this, DevelopmentSettings.class);
		setupTab(new TextView(this), getString(R.string.development_settings_title), intent);

		intent = new Intent().setClass(MainSettings.this, DevelopmentSettings2.class);
		setupTab(new TextView(this), getString(R.string.devtools_settings_title), intent);

		intent = new Intent().setClass(MainSettings.this, DisplaySettings.class);
		setupTab(new TextView(this), getString(R.string.display_settings_title), intent);

		intent = new Intent().setClass(MainSettings.this, LanguageSettings.class);
		setupTab(new TextView(this), getString(R.string.language_settings), intent);

		intent = new Intent().setClass(MainSettings.this, ProfileList.class);
		setupTab(new TextView(this), getString(R.string.profile_settings_title), intent);

		intent = new Intent().setClass(MainSettings.this, ProfileGroupConfig.class);
		setupTab(new TextView(this), getString(R.string.profilegroup_settings_title), intent);

		intent = new Intent().setClass(MainSettings.this, PrivacySettings.class);
		setupTab(new TextView(this), getString(R.string.privacy_settings_title), intent);

		intent = new Intent().setClass(MainSettings.this, SoundSettings.class);
		setupTab(new TextView(this), getString(R.string.sound_settings_title), intent);

		intent = new Intent().setClass(MainSettings.this, com.android.settings.deviceinfo.Memory.class);
		setupTab(new TextView(this), getString(R.string.storage_settings_title), intent);

		intent = new Intent().setClass(MainSettings.this, ExtendedDeviceInfo.class);
		setupTab(new TextView(this), getString(R.string.extended_info_title), intent);

		intent = new Intent().setClass(MainSettings.this, VoiceInputOutputSettings.class);
		setupTab(new TextView(this), getString(R.string.voice_input_output_settings), intent);

		intent = new Intent().setClass(MainSettings.this, DeviceInfoSettings.class);
		setupTab(new TextView(this), getString(R.string.about_settings), intent);

		intent = new Intent().setClass(MainSettings.this, DeviceInfoMisc.class);
		setupTab(new TextView(this), getString(R.string.about_misc_settings), intent);
    }

	private void setupTab(final View view, final String tag, final Intent myIntent) {

		View tabview = createTabView(mTabHost.getContext(), tag);
		TabSpec setContent =  mTabHost.newTabSpec(tag).setIndicator(tabview).setContent(myIntent);
		mTabHost.addTab(setContent);
	}

	private static View createTabView(final Context context, final String text) {

		View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		return view;
	}
}
