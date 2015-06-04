package uk.ac.horizon.babyface.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import uk.ac.horizon.babyface.R;
import uk.ac.horizon.babyface.binding.Binding;
import uk.ac.horizon.babyface.controller.CameraController;
import uk.ac.horizon.babyface.controller.Controller;
import uk.ac.horizon.babyface.controller.ControllerActivity;
import uk.ac.horizon.babyface.fragment.CameraFragment;
import uk.ac.horizon.babyface.fragment.DataFragment;
import uk.ac.horizon.babyface.fragment.PageFragment;
import uk.ac.horizon.babyface.fragment.PhotoFragment;
import uk.ac.horizon.babyface.fragment.SimpleFragment;
import uk.ac.horizon.babyface.fragment.UploadFragment;
import uk.ac.horizon.babyface.model.BabyData;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ControllerActivity<BabyData>
{
	private CameraController cameraController;
	private SectionsPagerAdapter pageAdapter;
	private ViewPager pager;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		pageAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(pageAdapter);

		bind(new Binding<BabyData>()
		{
			private int getPageCount()
			{
				BabyData babyData = getModel();
				if (babyData == null || babyData.getAge() == null || babyData.getDue() == null || babyData.getWeight() == null || babyData.getEthnicity() == null || babyData.getGender() == null)
				{
					return 4;
				}
				if (!babyData.getImages().containsKey("face"))
				{
					return 5;
				}
				if (!babyData.getImages().containsKey("ear"))
				{
					return 7;
				}
				if (!babyData.getImages().containsKey("foot"))
				{
					return 9;
				}
				return pageAdapter.fragments.size();
			}


			@Override
			public void updateView(Controller<BabyData> controller, String... properties)
			{
				int pageCount = getPageCount();
				if (pageAdapter.pages != pageCount)
				{
					pageAdapter.pages = pageCount;
					pageAdapter.notifyDataSetChanged();
				}
			}
		});

		setModel(new BabyData());
		cameraController = new CameraController(this);
	}

	public CameraController getCamera()
	{
		return cameraController;
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		if (cameraController != null)
		{
			cameraController.release();
		}
	}

	public void nextPage(View view)
	{
		pager.setCurrentItem(pager.getCurrentItem() + 1);
	}

	public void openURL(View view)
	{
		//final String EXTRA_HOSTED_MODE = "com.android.chrome.append_task";
		String url = "http://cvl.cs.nott.ac.uk/resources/babyface/pages/app.html";
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		//intent.putExtra(EXTRA_HOSTED_MODE, true);
		//intent.addCategory("android.intent.category.CUSTOM_TABS");

		startActivity(intent);
	}

	public void share(View view)
	{
		Intent intent=new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		intent.putExtra(Intent.EXTRA_TEXT, "We’ve just helped the University of Nottingham in their research to automatically calculate the gestational age of premature babies. You can help, too! http://cvl.cs.nott.ac.uk/resources/babyface/pages/app.html");
		startActivity(Intent.createChooser(intent, "Share"));
	}

	public void prevPage(View view)
	{
		pager.setCurrentItem(pager.getCurrentItem() - 1);
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter
	{
		private final List<PageFragment> fragments = new ArrayList<>();
		private int pages = 4;

		public SectionsPagerAdapter(FragmentManager fm)
		{
			super(fm);
			fragments.add(SimpleFragment.create(R.layout.fragment_welcome));
			fragments.add(SimpleFragment.create(R.layout.fragment_about));
			fragments.add(SimpleFragment.create(R.layout.fragment_disclaimer));
			fragments.add(new DataFragment());
			fragments.add(CameraFragment.create("face"));
			fragments.add(PhotoFragment.create("face"));
			fragments.add(CameraFragment.create("ear"));
			fragments.add(PhotoFragment.create("ear"));
			fragments.add(CameraFragment.create("foot"));
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
			return pages;
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			return null;// fragments.get(position).getTitle();
		}
	}
}
