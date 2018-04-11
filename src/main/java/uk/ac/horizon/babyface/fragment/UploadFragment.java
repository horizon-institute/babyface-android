package uk.ac.horizon.babyface.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
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
import uk.ac.horizon.babyface.SavedFiles;
import uk.ac.horizon.babyface.Uploader;
import uk.ac.horizon.babyface.activity.PagerActivity;

public class UploadFragment extends PageFragment
{
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

		root.findViewById(R.id.saveButton).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				saveEntry();
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
		final View saveOfflineView = root.findViewById(R.id.saveOfflineArea);

		progressBar.setVisibility(View.VISIBLE);
		progressButton.setVisibility(View.VISIBLE);
		uploadButton.setVisibility(View.GONE);
		retryView.setVisibility(View.GONE);
		saveOfflineView.setVisibility(View.GONE);

		Uploader.uploadData(getActivity(), getData(), new Uploader.UploadListener()
		{
			@Override
			public void onUploadSuccess()
			{

				getActivity().runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						progressBar.setVisibility(View.INVISIBLE);
						progressButton.setVisibility(View.GONE);
						root.findViewById(R.id.doneUploadedButton).setVisibility(View.VISIBLE);
						((PagerActivity) getActivity()).dataIsUnsaved = false;
					}
				});
			}

			@Override
			public void onUploadFailure()
			{
				getActivity().runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						progressBar.setVisibility(View.INVISIBLE);
						progressButton.setVisibility(View.GONE);
						retryView.setVisibility(View.VISIBLE);
						saveOfflineView.setVisibility(View.VISIBLE);
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

	protected void saveEntry()
	{

		uploading = true;
		update();
		final View root = getView();
		final ProgressBar progressBar = (ProgressBar) root.findViewById(R.id.progress);
		final View progressButton = root.findViewById(R.id.progressButton);
		final View uploadButton = root.findViewById(R.id.uploadButton);
		final View retryView = root.findViewById(R.id.retryView);
		final View saveOfflineView = root.findViewById(R.id.saveOfflineArea);

		progressBar.setVisibility(View.GONE);
		progressButton.setVisibility(View.GONE);
		uploadButton.setVisibility(View.GONE);
		retryView.setVisibility(View.GONE);
		saveOfflineView.setVisibility(View.GONE);

		SavedFiles.saveData(getActivity(), getData(), new SavedFiles.FileSaveListener()
		{
			@Override
			public void onSaveSuccess()
			{
				((PagerActivity)getActivity()).dataIsUnsaved = false;
				update();
				final View root = getView();
				final ProgressBar progressBar = (ProgressBar) root.findViewById(R.id.progress);
				final View progressButton = root.findViewById(R.id.progressButton);
				final View uploadButton = root.findViewById(R.id.uploadButton);
				final View retryView = root.findViewById(R.id.retryView);
				final View saveOfflineView = root.findViewById(R.id.saveOfflineArea);
				final View doneSavedButtonView = root.findViewById(R.id.doneSavedButton);

				getActivity().runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						progressBar.setVisibility(View.GONE);
						progressButton.setVisibility(View.GONE);
						uploadButton.setVisibility(View.GONE);
						retryView.setVisibility(View.GONE);
						saveOfflineView.setVisibility(View.GONE);
						doneSavedButtonView.setVisibility(View.VISIBLE);
					}
				});
			}

			@Override
			public void onSaveFailure(final String why)
			{
				uploading = false;
				update();
				final View root = getView();
				final ProgressBar progressBar = (ProgressBar) root.findViewById(R.id.progress);
				final View progressButton = root.findViewById(R.id.progressButton);
				final View uploadButton = root.findViewById(R.id.uploadButton);
				final View retryView = root.findViewById(R.id.retryView);
				final View saveOfflineView = root.findViewById(R.id.saveOfflineArea);

				getActivity().runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						progressBar.setVisibility(View.VISIBLE);
						progressButton.setVisibility(View.VISIBLE);
						uploadButton.setVisibility(View.VISIBLE);
						retryView.setVisibility(View.GONE);
						saveOfflineView.setVisibility(View.VISIBLE);


						DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
							}
						};

						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						builder.setTitle("ERROR: DATA NOT SAVED")
								.setMessage("There was a problem saving this record: " + why)
								.setPositiveButton("OK", dialogClickListener)
								.show();
					}
				});
			}
		});
	}


}
