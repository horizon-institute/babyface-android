package uk.ac.horizon.babyface;


import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import uk.ac.horizon.babyface.activity.PagerActivity;

public class Uploader
{

	private static final OkHttpClient client = new OkHttpClient();

	public interface UploadListener {
		void onUploadSuccess();
		void onUploadFailure();
	}

	public static void uploadData(final Context context, Map<String, Object> data, final UploadListener listener) {

		final MultipartBody.Builder bodyBuilder = new MultipartBody.Builder();
		bodyBuilder.setType(MultipartBody.FORM);

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
		String date = df.format(Calendar.getInstance().getTime());
		data.put("client_upload_time",date);
		Log.i(Uploader.class.getSimpleName(), "client_upload_time="+date);

		//final Map<String, Object> data = getData();
		final MediaType imageType = MediaType.parse("image/jpeg");
		for(String param: data.keySet())
		{
			Object value = data.get(param);
			if(value instanceof String)
			{
				if (((String)value).startsWith("file://"))
				{
					File file = new File(((String)value).replace("file://",""));
					if (file.exists())
					{
						bodyBuilder.addFormDataPart(param, param, RequestBody.create(imageType, file));
					}
				}
				else
				{
					bodyBuilder.addFormDataPart(param, (String) value);
				}
			}
			else if(value instanceof File)
			{
				bodyBuilder.addFormDataPart(param, param, RequestBody.create(imageType, (File) value));
			}
		}
		bodyBuilder.addFormDataPart("country", getUserCountry(context));

		Request request = new Request.Builder()
				.url("https://babyface.cs.nott.ac.uk/upload.php")
				.post(bodyBuilder.build())
				.build();

		Log.i("request", ""+request);

		client.newCall(request).enqueue(new Callback()
		{
			@Override
			public void onFailure(final Call call, final IOException e)
			{
				Log.w("response", e.getMessage(), e);
				listener.onUploadFailure();
			}

			@Override
			public void onResponse(final Call call, final Response response) throws IOException
			{
				JSONObject json_ = null;
				try
				{
					json_ = new JSONObject(response.body().string());
				}
				catch (JSONException e)
				{
					Log.e("","",e);
				}
				final JSONObject json = json_;

				if(response.code() != 200 || json==null || !json.optBoolean("success", false))
				{
					Log.w("response", response.message());
					Log.w("response", response.body().toString());
					onFailure(call, null);
				}
				else
				{
					listener.onUploadSuccess();
				}
			}
		});
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
