package uk.ac.horizon.babyface;


import android.content.ContentValues;
import android.content.Context;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;

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

public class Uploader
{

	private static OkHttpClient client = null;

	public interface UploadListener {
		void onUploadSuccess();
		void onUploadFailure();
	}

	public static void uploadData(final Context context, final Map<String, Object> data, final UploadListener listener) {


		if (client == null)
		{
			try
			{
				ProviderInstaller.installIfNeeded(context);
			}
			catch (GooglePlayServicesRepairableException e)
			{
				GooglePlayServicesUtil.showErrorNotification(
						e.getConnectionStatusCode(), context);
			}
			catch (GooglePlayServicesNotAvailableException e)
			{
				Log.e("UPLOAD", "", e);
			}

			client = new OkHttpClient();

			try
			{
				Log.i("UPLOAD", "getDefaultCipherSuites = " + TextUtils.join(",", client.sslSocketFactory().getDefaultCipherSuites()));
				Log.i("UPLOAD", "getSupportedCipherSuites = " + TextUtils.join(",", client.sslSocketFactory().getSupportedCipherSuites()));
			} catch (Exception e)
			{
				Log.e("UPLOAD", "", e);
			}
		}

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

		String country = getUserCountry(context);
		if (country != null)
		{
			bodyBuilder.addFormDataPart("country", country);
		}

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
					// move images to uploaded folder
					for(String param: data.keySet())
					{
						try
						{
							Object value = data.get(param);
							File file = null;
							if (value instanceof String && ((String) value).startsWith("file://"))
							{
								file = new File(((String) value).replace("file://", ""));
								if (file.exists())
								{
									bodyBuilder.addFormDataPart(param, param, RequestBody.create(imageType, file));
								}
							}
							else if (value instanceof File)
							{
								file = (File) value;
							}

							if (file != null)
							{
								File uploadedFileDir = new File(file.getParentFile(), "neogest_uploaded");
								if (!uploadedFileDir.exists()) uploadedFileDir.mkdir();
								File newFileLocation = new File(uploadedFileDir, file.getName());
								file.renameTo(newFileLocation);
								data.put(param, newFileLocation);

								// update mediastore
								ContentValues values = new ContentValues();
								values.put(MediaStore.MediaColumns.DATA, newFileLocation.getAbsolutePath());
								boolean successMediaStore = context.getContentResolver().update(
										MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
										values,
										MediaStore.MediaColumns.DATA + "='" + file.getAbsolutePath() + "'", null
								) == 1;
							}
						}
						catch (Exception e)
						{
							Log.w("UPLOAD", "Exception moving uploaded image after upload.", e);
						}
					}

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
