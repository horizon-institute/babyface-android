package uk.ac.horizon.babyface.activity;

import android.content.ComponentName;
import android.content.Intent;
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
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.horizon.babyface.R;
import uk.ac.horizon.babyface.fragment.CameraFragment;
import uk.ac.horizon.babyface.fragment.CameraPhotoFragment;
import uk.ac.horizon.babyface.fragment.DateFragment;
import uk.ac.horizon.babyface.fragment.ListFragment;
import uk.ac.horizon.babyface.fragment.NumberFragment;
import uk.ac.horizon.babyface.fragment.PageFragment;
import uk.ac.horizon.babyface.fragment.SimpleFragment;
import uk.ac.horizon.babyface.fragment.UploadFragment;

public class PagerActivity extends AppCompatActivity
{
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
	}

	public void nextPage(View view)
	{
		PageFragment fragment = (PageFragment) pageAdapter.getItem(pager.getCurrentItem());
		if (fragment.allowNext())
		{
			pager.setCurrentItem(pager.getCurrentItem() + 1);
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

	public void prevPage(View view)
	{
		pager.setCurrentItem(pager.getCurrentItem() - 1);
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter
	{
		private final List<PageFragment> fragments = new ArrayList<>();

		public SectionsPagerAdapter(FragmentManager fm)
		{
			super(fm);
			fragments.add(SimpleFragment.create(R.layout.about));
			fragments.add(SimpleFragment.create(R.layout.policy));
			fragments.add(ListFragment.create("gender"));
			fragments.add(ListFragment.create("ethnicity"));
			fragments.add(NumberFragment.create("weight", new NumberFragment.Range(0, 0, "Unknown"), new NumberFragment.Range(4, 55, "kg"), new NumberFragment.Range(10, 120, "lb")));
			fragments.add(NumberFragment.create("headCircumference", new NumberFragment.Range(0, 0, "Unknown"), new NumberFragment.Range(220, 420, "cm")));
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
}
