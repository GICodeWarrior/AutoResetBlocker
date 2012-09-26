package net.gicode.android.autoresetblocker;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

public class AutoResetBlockerActivity extends Activity {

	private SharedPreferences SP;
	private Intent dialIntent;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.blocker_activity);

		Button dialButton = (Button) findViewById(R.id.dial);
		dialButton.setOnClickListener(new DialListener());

		CheckBox autoDialOption = (CheckBox) findViewById(R.id.auto_dial);
		autoDialOption.setOnCheckedChangeListener(new AutoDialListener());

		SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		autoDialOption.setChecked(SP.getBoolean("auto_dial", false));

		Intent intent = getIntent();
		String number = intent.getData().getSchemeSpecificPart();
		number = number.replaceFirst("^//", "");

		boolean safe = number.matches("[0-9.\\-() +]+");
		String type = safe ? "safe" : "malicious";

		TextView labelView = (TextView) findViewById(R.id.number_label);
		labelView.setText("This phone number appears " + type + ".");
		EditText numberView = (EditText) findViewById(R.id.number);
		numberView.setText(number);

		Intent cleanIntent = new Intent(intent.getAction(), intent.getData());
		List<ResolveInfo> activities = getPackageManager()
				.queryIntentActivities(cleanIntent, 0);

		if (!safe || (activities.size() != 2)) {
			dialIntent = Intent.createChooser(intent, "Dial " + type
					+ " number");
		} else {
			ActivityInfo info = activities.get(0).activityInfo;
			if (getClass().getCanonicalName().equals(info.name)) {
				info = activities.get(1).activityInfo;
			}
			dialIntent = intent.setClassName(info.packageName, info.name);
		}

		if (safe && autoDialOption.isChecked()) {
			startActivity(dialIntent);
		}
	}

	private class DialListener implements View.OnClickListener {
		public void onClick(View v) {
			startActivity(dialIntent);
		}
	}

	private class AutoDialListener implements
			CompoundButton.OnCheckedChangeListener {
		public void onCheckedChanged(CompoundButton c, boolean isChecked) {
			Editor editor = SP.edit();
			editor.putBoolean("auto_dial", isChecked);
			editor.commit();
		}
	}
}
