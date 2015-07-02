package uk.ac.horizon.babyface.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import uk.ac.horizon.babyface.R;
import uk.ac.horizon.babyface.model.BabyData;

import java.io.File;
import java.util.Locale;

public class UploadFragment extends PageFragment
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		final View root = inflater.inflate(R.layout.fragment_upload, container, false);

		root.findViewById(R.id.uploadButton).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				upload();
			}
		});

		root.findViewById(R.id.retryButton).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				upload();
			}
		});

		return root;
	}

	private void upload()
	{
		final BabyData babyData = getController().getModel();
		final View root = getView();
		final ProgressBar progressBar = (ProgressBar) root.findViewById(R.id.progress);
		final View progressButton = root.findViewById(R.id.progressButton);
		final View uploadButton = root.findViewById(R.id.uploadButton);
		final View retryView = root.findViewById(R.id.retryView);

		progressBar.setVisibility(View.VISIBLE);
		progressButton.setVisibility(View.VISIBLE);
		uploadButton.setVisibility(View.GONE);
		retryView.setVisibility(View.GONE);

		Ion.with(getActivity())
				.load("http://www.cs.nott.ac.uk/babyface/upload.php")
				.uploadProgressBar(progressBar)
				.setMultipartParameter("weight", Float.toString(babyData.getWeight()))
				.setMultipartParameter("due", Integer.toString(babyData.getDue()))
				.setMultipartParameter("age", Integer.toString(babyData.getAge()))
				.setMultipartParameter("gender", babyData.getGender().name())
				.setMultipartParameter("ethnicity", babyData.getEthnicity().name())
				.setMultipartParameter("country", getUserCountry(getActivity()))
				.setMultipartFile("face", new File(babyData.getImages().get("face")))
				.setMultipartFile("ear", new File(babyData.getImages().get("ear")))
				.setMultipartFile("foot", new File(babyData.getImages().get("foot")))
				.asString()
				.withResponse()
				.setCallback(new FutureCallback<Response<String>>()
				{
					@Override
					public void onCompleted(Exception e, Response<String> result)
					{
						progressBar.setVisibility(View.INVISIBLE);
						progressButton.setVisibility(View.GONE);
						if (e != null || (result.getHeaders() != null && result.getHeaders().code() != 200))
						{
							retryView.setVisibility(View.VISIBLE);
						}
						else
						{
							root.findViewById(R.id.shareButton).setVisibility(View.VISIBLE);
						}
					}
				});
	}

	public static String getUserCountry(Context context)
	{
		try
		{
			final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			final String simCountry = tm.getSimCountryIso();
			if (simCountry != null && simCountry.length() == 2)
			{
				// SIM country code is available
				return simCountry.toLowerCase(Locale.US);
			}
			else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA)
			{
				// device is not 3G (would be unreliable)
				String networkCountry = tm.getNetworkCountryIso();
				if (networkCountry != null && networkCountry.length() == 2)
				{
					// network country code is available
					return networkCountry.toLowerCase(Locale.US);
				}
			}
			else
			{
				return context.getResources().getConfiguration().locale.getCountry().toLowerCase(Locale.US);
			}
		}
		catch (Exception e)
		{
			Log.i("", e.getMessage(), e);
		}
		return null;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser)
		{
			ImageView photoView = (ImageView) getView().findViewById(R.id.photoView);
			String image = "foot";
			Bitmap bit = BitmapFactory.decodeFile(getController().getModel().getImages().get(image));
			photoView.setImageBitmap(bit);
		}
	}
}
