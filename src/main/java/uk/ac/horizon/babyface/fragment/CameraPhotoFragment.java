package uk.ac.horizon.babyface.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import uk.ac.horizon.babyface.R;

public class CameraPhotoFragment extends PageFragment
{
	public static CameraPhotoFragment create(String param)
	{
		final Bundle bundle = new Bundle();
		bundle.putString("param", param);
		CameraPhotoFragment fragment = new CameraPhotoFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public boolean showBanner()
	{
		return false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.camera_photo, container, false);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser)
		{
			loadBitmap();
		}
	}

	private void loadBitmap(View view)
	{
		loadBitmap();
	}
	private void loadBitmap()
	{
		String param = getParam();
		int stringID = getResources().getIdentifier(param + "_reward", "string", getActivity().getPackageName());
		if (stringID != 0)
		{
			TextView rewardText = (TextView) getView().findViewById(R.id.rewardText);
			rewardText.setText(stringID);
		}

		new Thread()
		{
			@Override
			public void run()
			{
				Object value = getValue();
				if(value instanceof File)
				{
					final Bitmap bit = BitmapFactory.decodeFile(((File)value).getAbsolutePath());
					getActivity().runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							if (bit != null)
							{
								final View photoErrorView = getView().findViewById(R.id.imageLoadErrorMessage);
								photoErrorView.setVisibility(View.GONE);
								final ImageView photoView = (ImageView) getView().findViewById(R.id.photoView);
								photoView.setVisibility(View.VISIBLE);
								photoView.setImageBitmap(bit);
							}
							else
							{
								final View photoErrorView = getView().findViewById(R.id.imageLoadErrorMessage);
								photoErrorView.setVisibility(View.VISIBLE);
								final ImageView photoView = (ImageView) getView().findViewById(R.id.photoView);
								photoView.setVisibility(View.GONE);
							}
						}
					});
				}
			}
		}.start();
	}
}
