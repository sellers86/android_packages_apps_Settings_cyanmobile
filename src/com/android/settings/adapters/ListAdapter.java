package com.android.settings.adapters;

import java.util.List;

import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.pm.PackageManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.graphics.drawable.Drawable;
import android.widget.TextView;
import android.widget.ImageView;

import com.android.settings.R;

public class ListAdapter extends ArrayAdapter<RunningAppProcessInfo> {
	// List context
	private final Context context;
	// List values
	private final List<RunningAppProcessInfo> values;

	public ListAdapter(Context context, List<RunningAppProcessInfo> values) {
		super(context, R.layout.datatrafficmain, values);
		this.context = context;
		this.values = values;
	}

	
	/**
	 * Constructing list element view
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                PackageManager pm = context.getPackageManager();
                View rowView = inflater.inflate(R.layout.datatrafficmain, parent, false);
		ImageView appIcon = (ImageView) rowView.findViewById(R.id.detailsIco);
		TextView appName = (TextView) rowView.findViewById(R.id.appNameText);
                if (!values.get(position).processName.equals("system") && 
                    !values.get(position).processName.equals("com.android.phone") &&
                    !values.get(position).processName.equals("com.android.inputmethod.latin") &&
                    !values.get(position).processName.equals("com.android.settings") &&
                    !values.get(position).processName.equals("com.android.tmanager") &&
                    !values.get(position).processName.equals("com.android.systemui") &&
                    !values.get(position).processName.equals("android.process.contacts") &&
                    !values.get(position).processName.equals("com.cyanogenmod.cmparts") &&
                    !values.get(position).processName.equals("com.bel.android.dspmanager") &&
                    !values.get(position).processName.equals("com.cyanmobile.finder") &&
                    !values.get(position).processName.equals("com.google.process.gapps")) {
                    Drawable icon = null;
                    CharSequence title = null;
                    try {
  		        icon = pm.getApplicationIcon(pm.getApplicationInfo(values.get(position).processName, PackageManager.GET_META_DATA));
                        title = pm.getApplicationLabel(pm.getApplicationInfo(values.get(position).processName, PackageManager.GET_META_DATA));
  		    } catch(Exception e) {
                       title = null;
                    }
                    if (icon != null && title != null) {
                        rowView.setVisibility(View.VISIBLE);
                        appIcon.setVisibility(View.VISIBLE);
		        appIcon.setImageDrawable(icon);
                        appName.setVisibility(View.VISIBLE);
		        appName.setText(title.toString());
                    } else {
                        rowView.setVisibility(View.GONE);
                        appIcon.setVisibility(View.GONE);
                        appName.setVisibility(View.GONE);
                    }
                } else {
                    rowView.setVisibility(View.GONE);
                    appIcon.setVisibility(View.GONE);
                    appName.setVisibility(View.GONE);
                }
		return rowView;
	}
	
	
}
