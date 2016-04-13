package uk.ac.horizon.babyface.fragment;

import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uk.ac.horizon.babyface.R;
import uk.ac.horizon.babyface.activity.PagerActivity;

public class UploadFragment extends PageFragment
{
	private static final OkHttpClient client = new OkHttpClient();
	private boolean uploading = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		final View root = inflater.inflate(R.layout.upload, container, false);

		Gson gson = new Gson();
		final PagerActivity activity = (PagerActivity)getActivity();
		final Map<String, Object> data = activity.getData();
		Log.i("data", gson.toJson(data));

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
		uploading = true;
		update();
		final View root = getView();
		final ProgressBar progressBar = (ProgressBar) root.findViewById(R.id.progress);
		final View progressButton = root.findViewById(R.id.progressButton);
		final View uploadButton = root.findViewById(R.id.uploadButton);
		final View retryView = root.findViewById(R.id.retryView);

		progressBar.setVisibility(View.VISIBLE);
		progressButton.setVisibility(View.VISIBLE);
		uploadButton.setVisibility(View.GONE);
		retryView.setVisibility(View.GONE);

		final MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
		bodyBuilder.setType(MultipartBody.FORM);

		final Map<String, Object> data = getData();
		final MediaType imageType = MediaType.parse("image/jpeg");
		for(String param: data.keySet())
		{
			Object value = data.get(param);
			if(value instanceof String)
			{
				bodyBuilder.addFormDataPart(param, (String)value);
			}
			else if(value instanceof File)
			{
				bodyBuilder.addFormDataPart(param, param, RequestBody.create(imageType, (File) value));
			}
		}
		bodyBuilder.addFormDataPart("country", getUserCountry(getContext()));

		Request request = new Request.Builder()
				.url("http://www.cs.nott.ac.uk/babyface/upload.php")
				.post(bodyBuilder.build())
				.build();

		Log.i("request", ""+request);

		client.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(final Call call, final IOException e)
			{
				Log.w("response", e.getMessage(), e);
				getActivity().runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						progressBar.setVisibility(View.INVISIBLE);
						progressButton.setVisibility(View.GONE);
						retryView.setVisibility(View.VISIBLE);
					}
				});
			}

			@Override
			public void onResponse(final Call call, final Response response) throws IOException
			{
				getActivity().runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						progressBar.setVisibility(View.INVISIBLE);
						progressButton.setVisibility(View.GONE);
						if(response.code() != 200)
						{
							Log.w("response", response.message());
							Log.w("response", response.body().toString());
							onFailure(call, null);
						}
						else
						{
							root.findViewById(R.id.shareButton).setVisibility(View.VISIBLE);
						}
					}
				});
			}
		});
	}

	@Override
	public boolean allowPrev()
	{
		return !uploading;
	}

	private static String getUserCountry(Context context)
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
}
