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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.VerifierDeviceIdentity;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Build;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.CheckBoxPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.text.TextUtils;

/*
 * Displays preferences for application developers.
 */
public class DevelopmentSettings extends PreferenceActivity
        implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener, OnPreferenceChangeListener {

    private static final String ENABLE_ADB = "enable_adb";
    private static final String ADB_TCPIP  = "adb_over_network";
    private static final String ADB_NOTIFY = "adb_notify";
    private static final String KEEP_SCREEN_ON = "keep_screen_on";
    private static final String ALLOW_MOCK_LOCATION = "allow_mock_location";
    private static final String KILL_APP_LONGPRESS_BACK = "kill_app_longpress_back";
    private static final String VERIFIER_DEVICE_IDENTIFIER = "verifier_device_identifier";
    private static final String ROOT_ACCESS_KEY = "root_access";
    private static final String ROOT_ACCESS_PROPERTY = "persist.sys.root_access";
    private static final String MOUNT_SD_EXT="mount_sd_ext";
    private static final String DATA_BIND_MOUNT="data_bind_mount";
    private static final String DOWN_CACHE_MOUNT="down_cache_mount";

    private static final String KILLTIMEOUT_PREF = "pref_killtimeout";
    private static final String KILLTIMEOUT_PERSIST_PROP = "persist.sys.back_kill_timeout";
    private static final String KILLTIMEOUT_DEFAULT = "1500";

    private CheckBoxPreference mEnableAdb;
    private CheckBoxPreference mAdbOverNetwork;
    private CheckBoxPreference mAdbNotify;
    private CheckBoxPreference mKeepScreenOn;
    private CheckBoxPreference mAllowMockLocation;
    private CheckBoxPreference mKillAppLongpressBack;
    private ListPreference mRootAccess;
    private ListPreference mKillTimeOutPref;
    private CheckBoxPreference mMountSDExt;
    private CheckBoxPreference mDataBindMount;
    private CheckBoxPreference mDownCacheMount;

    private String mCurrentDialog;
    private Object mSelectedRootValue;

    // To track whether Yes was clicked in the adb warning dialog
    private boolean mOkClicked;

    private Dialog mOkDialog;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.development_prefs);

        mEnableAdb = (CheckBoxPreference) findPreference(ENABLE_ADB);
	mAdbOverNetwork = (CheckBoxPreference) findPreference(ADB_TCPIP);
        mAdbNotify = (CheckBoxPreference) findPreference(ADB_NOTIFY);
        mKeepScreenOn = (CheckBoxPreference) findPreference(KEEP_SCREEN_ON);
        mAllowMockLocation = (CheckBoxPreference) findPreference(ALLOW_MOCK_LOCATION);
        mKillAppLongpressBack = (CheckBoxPreference) findPreference(KILL_APP_LONGPRESS_BACK);
        mRootAccess = (ListPreference) findPreference(ROOT_ACCESS_KEY);

        // user builds don't get root, and eng always gets root
        if (SystemProperties.getInt("ro.debuggable", 0) == 0 || "eng".equals(Build.TYPE)) {
            getPreferenceScreen().removePreference(mRootAccess);
        } else {
            mRootAccess.setOnPreferenceChangeListener(this);
        }

        mMountSDExt = (CheckBoxPreference) findPreference(MOUNT_SD_EXT);
        mDataBindMount = (CheckBoxPreference) findPreference(DATA_BIND_MOUNT);
        mDownCacheMount = (CheckBoxPreference) findPreference(DOWN_CACHE_MOUNT);

        mKillTimeOutPref = (ListPreference) findPreference(KILLTIMEOUT_PREF);
        mKillTimeOutPref.setOnPreferenceChangeListener(this);

        final Preference verifierDeviceIdentifier = findPreference(VERIFIER_DEVICE_IDENTIFIER);
        final PackageManager pm = this.getPackageManager();
        final VerifierDeviceIdentity verifierIndentity = pm.getVerifierDeviceIdentity();
        if (verifierIndentity != null) {
            verifierDeviceIdentifier.setSummary(verifierIndentity.toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mEnableAdb.setChecked(Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ADB_ENABLED, 0) != 0);
        
	mAdbOverNetwork.setChecked(Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ADB_PORT, 0) > 0);

        mAdbNotify.setChecked(Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ADB_NOTIFY, 1) != 0);
                
        mKeepScreenOn.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.STAY_ON_WHILE_PLUGGED_IN, 0) != 0);
        mAllowMockLocation.setChecked(Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.ALLOW_MOCK_LOCATION, 0) != 0);
        mKillAppLongpressBack.setChecked((Settings.Secure.getInt(getContentResolver(),
                Settings.Secure.KILL_APP_LONGPRESS_BACK, 0) != 0));
        mKillTimeOutPref.setValue(SystemProperties.get(KILLTIMEOUT_PERSIST_PROP, KILLTIMEOUT_DEFAULT));
        mMountSDExt.setChecked(SystemProperties.get("persist.sys.magpie.allow","0").equals("1"));
        mDataBindMount.setEnabled(SystemProperties.get("persist.sys.magpie.allow","0").equals("1"));
        mDataBindMount.setChecked(SystemProperties.get("persist.sys.magpie.data2sd","0").equals("1"));
        mDownCacheMount.setEnabled(SystemProperties.get("persist.sys.magpie.allow","0").equals("1"));
        mDownCacheMount.setChecked(SystemProperties.get("persist.sys.dcache.allow","0").equals("1"));

        updateRootAccessOptions();
    }

    private void updateRootAccessOptions() {
        String value = SystemProperties.get(ROOT_ACCESS_PROPERTY, "1");
        mRootAccess.setValue(value);
        mRootAccess.setSummary(getResources().getStringArray(R.array.root_access_entries)[Integer.valueOf(value)]);
    }
	
    private void writeRootAccessOptions(Object newValue) {
        String oldValue = SystemProperties.get(ROOT_ACCESS_PROPERTY, "1");
        SystemProperties.set(ROOT_ACCESS_PROPERTY, newValue.toString());
        if (Integer.valueOf(newValue.toString()) < 2 && !oldValue.equals(newValue)
                && "1".equals(SystemProperties.get("service.adb.root", "0"))) {
            SystemProperties.set("service.adb.root", "0");
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.ADB_ENABLED, 0);
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.ADB_ENABLED, 1);
        }
        updateRootAccessOptions();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (Utils.isMonkeyRunning()) {
            return false;
        }

        if (preference == mEnableAdb) {
            if (mEnableAdb.isChecked()) {
                mOkClicked = false;
                if (mOkDialog != null) dismissDialog();
                mOkDialog = new AlertDialog.Builder(this).setMessage(
                        getResources().getString(R.string.adb_warning_message))
                        .setTitle(R.string.adb_warning_title)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, this)
                        .setNegativeButton(android.R.string.no, this)
                        .show();
		mCurrentDialog = ENABLE_ADB;
                mOkDialog.setOnDismissListener(this);
            } else {
                Settings.Secure.putInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 0);
	    }
        } else if (preference == mAdbOverNetwork) {
            if (mAdbOverNetwork.isChecked()) {
                mOkClicked = false;
                if (mOkDialog != null) dismissDialog();
                mOkDialog = new AlertDialog.Builder(this).setMessage(
                        getResources().getString(R.string.adb_over_network_warning))
                        .setTitle(R.string.adb_over_network)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, this)
                        .setNegativeButton(android.R.string.no, this)
                        .show();
                mCurrentDialog = ADB_TCPIP;
                mOkDialog.setOnDismissListener(this);
            } else {
                Settings.Secure.putInt(getContentResolver(), Settings.Secure.ADB_PORT, -1);
            }
        } else if (preference == mAdbNotify) {
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.ADB_NOTIFY,
                    mAdbNotify.isChecked() ? 1 : 0);
        } else if (preference == mKeepScreenOn) {
            Settings.System.putInt(getContentResolver(), Settings.System.STAY_ON_WHILE_PLUGGED_IN,
                    mKeepScreenOn.isChecked() ? 
                    (BatteryManager.BATTERY_PLUGGED_AC | BatteryManager.BATTERY_PLUGGED_USB) : 0);
        } else if (preference == mAllowMockLocation) {
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION,
                    mAllowMockLocation.isChecked() ? 1 : 0);
        } else if (preference == mMountSDExt) {
            SystemProperties.set("persist.sys.magpie.allow",mMountSDExt.isChecked() ? "1" : "0");
            mDataBindMount.setEnabled(SystemProperties.get("persist.sys.magpie.allow","0").equals("1"));
            mDownCacheMount.setEnabled(SystemProperties.get("persist.sys.magpie.allow","0").equals("1"));
        } else if (preference == mDataBindMount) {
            SystemProperties.set("persist.sys.magpie.data2sd",mDataBindMount.isChecked() ? "1" : "0");
        } else if (preference == mDownCacheMount) {
            SystemProperties.set("persist.sys.dcache.allow",mDownCacheMount.isChecked() ? "1" : "0");
        } else if (preference == mKillAppLongpressBack) {
            Settings.Secure.putInt(getContentResolver(), Settings.Secure.KILL_APP_LONGPRESS_BACK,
                    mKillAppLongpressBack.isChecked() ? 1 : 0);
        }

        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mRootAccess) {
            if ("0".equals(SystemProperties.get(ROOT_ACCESS_PROPERTY, "1"))
                    && !"0".equals(newValue)) {
                mSelectedRootValue = newValue;
                mOkClicked = false;
                if (mOkDialog != null) dismissDialog();
                mOkDialog = new AlertDialog.Builder(this).setMessage(
                    getResources().getString(R.string.root_access_warning_message))
                    .setTitle(R.string.root_access_warning_title)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, this)
                    .setNegativeButton(android.R.string.no, this)
                    .show();
                mCurrentDialog = ROOT_ACCESS_KEY;
                mOkDialog.setOnDismissListener(this);
            } else {
                writeRootAccessOptions(newValue);
            }
            return true;
        } else if (preference == mKillTimeOutPref) {
            if (newValue != null) {
                SystemProperties.set(KILLTIMEOUT_PERSIST_PROP, (String)newValue);
                return true;
            }
        }
        return false;
    }

    private void dismissDialog() {
        if (mOkDialog == null) return;
        mOkDialog.dismiss();
        mOkDialog = null;
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            mOkClicked = true;
	    if (mCurrentDialog.equals(ENABLE_ADB)) {
            	Settings.Secure.putInt(getContentResolver(), Settings.Secure.ADB_ENABLED, 1);
            } else if (mCurrentDialog.equals(ROOT_ACCESS_KEY)) {
                writeRootAccessOptions(mSelectedRootValue);
	    } else {
                Settings.Secure.putInt(getContentResolver(), Settings.Secure.ADB_PORT, 5555);
            }
        } else {
            // Reset the toggle
            if (mCurrentDialog.equals(ENABLE_ADB)) {
		mEnableAdb.setChecked(false);
            } else if (mCurrentDialog.equals(ROOT_ACCESS_KEY)) {
                writeRootAccessOptions("0");
            } else {
                mAdbOverNetwork.setChecked(false);
            }
        }
    }

    public void onDismiss(DialogInterface dialog) {
        // Assuming that onClick gets called first
        if (!mOkClicked) {
            if (mCurrentDialog.equals(ENABLE_ADB)) {
		mEnableAdb.setChecked(false);
            } else if (mCurrentDialog.equals(ROOT_ACCESS_KEY)) {
                mRootAccess.setValue("0");
            } else {
                mAdbOverNetwork.setChecked(false);
            }
        }
    }

    @Override
    public void onDestroy() {
        dismissDialog();
        super.onDestroy();
    }
}
