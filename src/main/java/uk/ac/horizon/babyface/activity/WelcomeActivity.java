package uk.ac.horizon.babyface.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import uk.ac.horizon.babyface.R;

public class WelcomeActivity extends AppCompatActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
	}

	public void start(View view)
	{
		startActivity(new Intent(this, PagerActivity.class));
	}
}
