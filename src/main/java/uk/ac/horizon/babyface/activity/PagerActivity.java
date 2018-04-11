package uk.ac.horizon.babyface.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import uk.ac.horizon.babyface.R;
import uk.ac.horizon.babyface.fragment.CameraFragment;
import uk.ac.horizon.babyface.fragment.CameraPhotoFragment;
import uk.ac.horizon.babyface.fragment.DateFragment;
import uk.ac.horizon.babyface.fragment.ListFragment;
import uk.ac.horizon.babyface.fragment.NumberFragment;
import uk.ac.horizon.babyface.fragment.PageFragment;
import uk.ac.horizon.babyface.fragment.SimpleFragment;
import uk.ac.horizon.babyface.fragment.SpinnerFragment;
import uk.ac.horizon.babyface.fragment.TextFragment;
import uk.ac.horizon.babyface.fragment.UploadFragment;

public class PagerActivity extends AppCompatActivity
{
	private static long MILLISECONDS_IN_A_SECOND = 1000L;
	private static long MILLISECONDS_IN_A_MINUTE = MILLISECONDS_IN_A_SECOND * 60L;
	private static long MILLISECONDS_IN_AN_HOUR = MILLISECONDS_IN_A_MINUTE * 60L;
	private static long MILLISECONDS_IN_A_DAY = MILLISECONDS_IN_AN_HOUR * 24L;
	private static long MILLISECONDS_IN_A_WEEK = MILLISECONDS_IN_A_DAY * 7L;
	private static long MILLISECONDS_IN_A_MONTH = MILLISECONDS_IN_A_WEEK * 4L;

	public boolean dataIsUnsaved = false;
	private SectionsPagerAdapter pageAdapter;
	private ViewPager pager;
	private final Map<String, Object> data = new HashMap<>();
	private CustomTabsClient customTabsClient;
	private CustomTabsSession customTabsSession;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pager);

		pager = (ViewPager) findViewById(R.id.pager);
		pageAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		pager.setAdapter(pageAdapter);
		pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
		{
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
			{
				update();
			}

			@Override
			public void onPageSelected(int position)
			{

			}

			@Override
			public void onPageScrollStateChanged(int state)
			{

			}
		});
		pager.setOnTouchListener(new View.OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				return true;
			}
		});

		// Binds to the service.
		CustomTabsClient.bindCustomTabsService(this, getPackageName(), new CustomTabsServiceConnection()
		{
			@Override
			public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client)
			{
				// mClient is now valid.
				customTabsClient = client;
				client.warmup(0);
				customTabsSession = client.newSession(new CustomTabsCallback());
				customTabsSession.mayLaunchUrl(Uri.parse("http://cvl.cs.nott.ac.uk/resources/babyface/pages/app.html"), null, null);
			}

			@Override
			public void onServiceDisconnected(ComponentName name)
			{
				customTabsClient = null;
			}
		});

		// add to data

		// time
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		String date = df.format(Calendar.getInstance().getTime());
		getData().put("client_start_time",date);
		Log.i(this.getClass().getSimpleName(), "client_start_time="+date);

		try
		{
			// version number
			try
			{
				PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
				getData().put("client_version", pInfo.versionName);
				Log.i(this.getClass().getSimpleName(), "client_version=" + pInfo.versionName);
			}
			catch (PackageManager.NameNotFoundException e)
			{
				Log.e("PagerActivity", "Could not get app version to add to data.", e);
			}

			// device id
			SharedPreferences sharedPref = this.getSharedPreferences(
					getString(R.string.preference_file_key), Context.MODE_PRIVATE);
			String deviceId = sharedPref.getString("deviceId", "");
			if ("".equals(deviceId))
			{
				//create deviceId
				SharedPreferences.Editor editor = sharedPref.edit();
				deviceId = UUID.randomUUID().toString();
				editor.putString("deviceId", deviceId);
				editor.commit();
			}
			getData().put("client_device_id", deviceId);
			Log.i(this.getClass().getSimpleName(), "client_device_id=" + deviceId);
		}
		catch (Exception e)
		{
			Log.e("PagerActivity", "Error adding app version/device id to data.", e);
		}

	}

	public void nextPage(View view)
	{
		PageFragment fragment = (PageFragment) pageAdapter.getItem(pager.getCurrentItem());
		if (fragment.allowNext())
		{
			dataIsUnsaved = true;
			pager.setCurrentItem(pager.getCurrentItem() + 1);
			PageFragment fragment2 = (PageFragment) pageAdapter.getItem(pager.getCurrentItem());
			showHideBanner(fragment2.showBanner());
		}
	}

	public void prevPage(View view)
	{
		pager.setCurrentItem(pager.getCurrentItem() - 1);
		PageFragment fragment2 = (PageFragment) pageAdapter.getItem(pager.getCurrentItem());
		showHideBanner(fragment2.showBanner());
	}

	private void showHideBanner(boolean b)
	{
		View banner = findViewById(R.id.header);
		if (banner != null)
		{
			banner.setVisibility(b ? View.VISIBLE : View.INVISIBLE);
		}
		View bannerShadow = findViewById(R.id.headerShadow);
		if (bannerShadow != null)
		{
			bannerShadow.setVisibility(b ? View.VISIBLE : View.INVISIBLE);
		}
	}

	public Map<String, Object> getData()
	{
		return data;
	}

	public void openURL(View view)
	{
		CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(customTabsSession);
		builder.setToolbarColor(ContextCompat.getColor(this, R.color.primary));

		CustomTabsIntent customTabsIntent = builder.build();
		customTabsIntent.launchUrl(this, Uri.parse("http://cvl.cs.nott.ac.uk/resources/babyface/pages/app.html"));
	}

	public void share(View view)
	{
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		intent.putExtra(Intent.EXTRA_TEXT, "We’ve just helped @GestStudy research to save premature babies in developing countries. You can help, too! http://cvl.cs.nott.ac.uk/resources/babyface/index.html");
		startActivity(Intent.createChooser(intent, "Share"));
	}

	public void done(View view)
	{
		finish();
	}

	public void update()
	{
		final Button nextButton = (Button) findViewById(R.id.nextButton);
		final ImageButton prevButton = (ImageButton) findViewById(R.id.prevButton);
		PageFragment fragment = (PageFragment) pageAdapter.getItem(pager.getCurrentItem());
		if (fragment.allowNext() && pager.getCurrentItem() < (pageAdapter.getCount() - 1))
		{
			nextButton.setVisibility(View.VISIBLE);
		}
		else
		{
			nextButton.setVisibility(View.INVISIBLE);
		}
		if (fragment.allowPrev() && pager.getCurrentItem() > 0)
		{
			prevButton.setVisibility(View.VISIBLE);
		}
		else
		{
			prevButton.setVisibility(View.INVISIBLE);
		}
		nextButton.setTextColor(fragment.getTint());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			nextButton.setCompoundDrawableTintList(ColorStateList.valueOf(fragment.getTint()));
		}
		prevButton.setColorFilter(fragment.getTint());
	}


	public class SectionsPagerAdapter extends FragmentPagerAdapter
	{
		private final List<PageFragment> fragments = new ArrayList<>();

		public SectionsPagerAdapter(FragmentManager fm)
		{
			super(fm);
			// new fragments
			// 0. Data collection page:
			// 1.	informed consent

			fragments.add(ListFragment.create("consent"));
			// 	a.	Place of birth: a pull down menu with numbers 1 to 10 right now.
			fragments.add(SpinnerFragment.create("placeOfBirth"));
			//fragments.add(NumberFragment.create("placeOfBirth",
			//		new NumberFragment.Option("placeOfBirth",
			//				new NumberFragment.Value(1, 10, ""))));
			//	b.	User ID: free text box with 4 caracters.
			fragments.add(TextFragment.create("userId", 1, 4, true, false, false));

			// 2.	Unique Study ID: Free text of 15 characters
			fragments.add(TextFragment.create("studyId", 1, 15, true, false, false));

			// 3.	Baby’s gender:  Male/Female
			fragments.add(ListFragment.create("gender"));
			// 4.	Ethnicity: (in this order) Indian/White/Black/Other Asian/Other
			fragments.add(ListFragment.create("ethnicity"));
			// 5.	Birth weight: number keypad to enter number in grams. Value invalid if <400g or
			// 		>6000g (Cannot be empty)

			fragments.add(TextFragment.create("weight", 400, 6000, true, true, true));


			// 6.	Head Circumference (OFC): number keypad to enter number in centimetres between
			// 		20 and 50 - Can be empty. If empty: store as “unknown”

			fragments.add(TextFragment.create("headCircumference", 20, 50, true, true, true));

			// 7.	Number of babies at birth: singleton/twin/triplet/quadruplet/other
			fragments.add(ListFragment.create("babiesAtBirth"));
			// 8.	Mode of delivery: Vaginal/Instrumental/Caesarean Section
			fragments.add(ListFragment.create("modeOfDelivery"));
			// 9.	Date of birth: calendar format (dd-mm-yyyy) Cannot be empty.
			fragments.add(DateFragment.create("birthDate", new Date().getTime() - 2L*MILLISECONDS_IN_A_MONTH, new Date().getTime()));
			// 10.	Expected date of delivery (EDD): calendar format (dd-mm-yyyy)
			// 		Can be empty. Store as “unknown”
			fragments.add(DateFragment.create("expectedDate", new Date().getTime() - 5L*MILLISECONDS_IN_A_MONTH, new Date().getTime() + 5L*MILLISECONDS_IN_A_MONTH, true));
			// 11.	EDD based on: first trimester ultrasound/Last menstrual period/estimate/other
			fragments.add(ListFragment.create("expectedBasis"));
			// 12.	Date of images: calendar format (dd-mm-yyyy)
			// fragments.add(DateFragment.create("imageDate", new Date().getTime() - 5184000000L, new Date().getTime()));

			// 13.	Foot: left/right
			// 14.	Ear: left/right
			// 15.	Image section (3 each): face/foot/ear (this could be three buttons,
			// 		when you press one, the camera opens for that option.

			fragments.add(SimpleFragment.create(R.layout.camera_confirm));

			fragments.add(CameraFragment.create("face"));
			fragments.add(CameraPhotoFragment.create("face"));
			fragments.add(CameraFragment.create("face2"));
			fragments.add(CameraPhotoFragment.create("face2"));
			fragments.add(CameraFragment.create("face3"));
			fragments.add(CameraPhotoFragment.create("face3"));

			fragments.add(ListFragment.create("earLeftRight"));
			fragments.add(CameraFragment.create("ear"));
			fragments.add(CameraPhotoFragment.create("ear"));
			fragments.add(CameraFragment.create("ear2"));
			fragments.add(CameraPhotoFragment.create("ear2"));
			fragments.add(CameraFragment.create("ear3"));
			fragments.add(CameraPhotoFragment.create("ear3"));

			fragments.add(ListFragment.create("footLeftRight"));
			fragments.add(CameraFragment.create("foot"));
			fragments.add(CameraPhotoFragment.create("foot"));
			fragments.add(CameraFragment.create("foot2"));
			fragments.add(CameraPhotoFragment.create("foot2"));
			fragments.add(CameraFragment.create("foot3"));
			fragments.add(CameraPhotoFragment.create("foot3"));

			// 16.	Additional info (yes/no – if so give details)
			fragments.add(TextFragment.create("additionalInfo", 0, 1500, false, false, false));
			fragments.add(new UploadFragment());







			// old fragments
			/*
			fragments.add(SimpleFragment.create(R.layout.about));
			fragments.add(SimpleFragment.create(R.layout.policy));
			fragments.add(ListFragment.create("gender"));
			fragments.add(ListFragment.create("ethnicity"));
			fragments.add(NumberFragment.create("weight",
					new NumberFragment.Option("Unknown"),
					new NumberFragment.Option("kg",
							new NumberFragment.Value(1, 5, "."),
							new NumberFragment.Value(0, 9, "kg")),
					new NumberFragment.Option("lb",
							new NumberFragment.Value(1, 12, "lb "),
							new NumberFragment.Value(0, 15, "oz"))));
			fragments.add(NumberFragment.create("headCircumference",
					new NumberFragment.Option("Unknown"),
					new NumberFragment.Option("cm",
							new NumberFragment.Value(22, 42, "."),
							new NumberFragment.Value(0, 9, "cm"))));
			fragments.add(DateFragment.create("birthDate", new Date().getTime() - 5184000000L, new Date().getTime()));
			fragments.add(DateFragment.create("expectedDate", new Date().getTime() - 5184000000L - 1814400000L, new Date().getTime() + 1814400000L));
			fragments.add(ListFragment.create("expectedBasis"));
			fragments.add(SimpleFragment.create(R.layout.camera_confirm));
			fragments.add(CameraFragment.create("face"));
			fragments.add(CameraPhotoFragment.create("face"));
			fragments.add(CameraFragment.create("ear"));
			fragments.add(CameraPhotoFragment.create("ear"));
			fragments.add(CameraFragment.create("foot"));
			fragments.add(CameraPhotoFragment.create("foot"));
			fragments.add(new UploadFragment());
			*/
		}

		@Override
		public Fragment getItem(int position)
		{
			return fragments.get(position);
		}

		@Override
		public int getCount()
		{
			return fragments.size();
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			return null;// fragments.get(position).getTitle();
		}
	}

	@Override
	public void onBackPressed()
	{
		if (dataIsUnsaved)
		{
			DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					switch (which)
					{
						case DialogInterface.BUTTON_POSITIVE:
							finish();
							break;

						case DialogInterface.BUTTON_NEGATIVE:
							break;
					}
				}
			};

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Are you sure you want to quit this entry? Data entered will be lost.").setPositiveButton("Quit", dialogClickListener)
					.setNegativeButton("No", dialogClickListener).show();
		}
		else
		{
			super.onBackPressed();
		}
	}
}
