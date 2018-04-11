package uk.ac.horizon.babyface.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

import uk.ac.horizon.babyface.R;
import uk.ac.horizon.babyface.SavedFiles;

public class WelcomeActivity extends AppCompatActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);


		// version number
		try
		{
			PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
			TextView appTitle = (TextView) findViewById(R.id.appTitle);
			if (appTitle != null)
			{
				appTitle.setText(appTitle.getText() + " " + pInfo.versionName);
			}
			Log.i(this.getClass().getSimpleName(), "client_version=" + pInfo.versionName);
		}
		catch (PackageManager.NameNotFoundException e)
		{
			Log.e(this.getClass().getSimpleName(), "Could not get app version to display.", e);
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();

		File[] savedFiles = SavedFiles.getSavedFiles(this);
		boolean hasSavedEntries = savedFiles != null && savedFiles.length > 0;

		Button v = (Button) findViewById(R.id.uploadSavedEntriesButton);
		if (v != null)
		{
			v.setText(getResources().getString(R.string.uploadSaved, hasSavedEntries ? savedFiles.length : 0));
			v.setVisibility(hasSavedEntries ? View.VISIBLE : View.GONE);
		}

	}

	public void start(View view)
	{
		startActivity(new Intent(this, PagerActivity.class));
	}

	public void uploadSaved(View view)
	{
		startActivity(new Intent(this, UploadSavedActivity.class));
	}
}
