package uk.ac.horizon.babyface.activity;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import uk.ac.horizon.babyface.R;
import uk.ac.horizon.babyface.SavedFiles;
import uk.ac.horizon.babyface.Uploader;

public class UploadSavedActivity extends AppCompatActivity implements Uploader.UploadListener
{
	private static final String LOG_KEY = "UploadSaved";
	private boolean uploading = false;

	private File[] filesToUpload;
	private int index = 0;
	private ArrayList<File> uploaded = new ArrayList<>();
	private ArrayList<File> failed = new ArrayList<>();
	private ArrayList<String> messages = new ArrayList<>();

	private LooperThread looperThread;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload_saved);

		File[] savedFiles = SavedFiles.getSavedFiles(this);

		TextView tv = (TextView) findViewById(R.id.number_of_saved_files);
		tv.setText(getResources().getString(R.string.there_are_x_saved_files, savedFiles==null? 0 : savedFiles.length));
	}



	public void upload(View view)
	{
		uploading = true;
		View uploadSavedFilesDetailsView = findViewById(R.id.uploadSavedFilesDetailsView);
		View uploadSavedFilesProgressView = findViewById(R.id.uploadSavedFilesProgressView);
		uploadSavedFilesDetailsView.setVisibility(View.GONE);
		uploadSavedFilesProgressView.setVisibility(View.VISIBLE);


		filesToUpload = SavedFiles.getSavedFiles(this);
		Log.i(LOG_KEY, "Starting to upload "+filesToUpload.length+" files");

		index = -1;

		looperThread = new LooperThread();
		looperThread.start();
		while (looperThread.mHandler == null) {}
		uploadNextRecord();
	}

	private void uploadNextRecord()
	{
		++index;
		if (index < filesToUpload.length)
		{
			looperThread.mHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					uploadRecord();
				}
			});
		}
		else
		{
			looperThread.mHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					cleanUp();
				}
			});
		}
	}

	private void uploadRecord() {
		final String uiMessage = "Reading "+(index+1)+"/"+filesToUpload.length;
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				((TextView)findViewById(R.id.progressText)).setText(uiMessage);
			}
		});

		Map<String, Object> data = null;
		try
		{
			Log.i(LOG_KEY, "Reading file "+(index+1)+"/"+filesToUpload.length);
			 data = SavedFiles.loadFile(filesToUpload[index]);
		}
		catch (IOException e)
		{
			Log.e(LOG_KEY, "Error reading file.", e);
			messages.add("Error reading file "+(index+1)+"/"+filesToUpload.length+": "+e.getMessage());
		}
		catch (JSONException e)
		{
			Log.e(LOG_KEY, "Error reading file.", e);
			messages.add("Error reading file "+(index+1)+"/"+filesToUpload.length+": "+e.getMessage());
		}

		if (data != null)
		{
			final String uiMessage2 = "Reading "+(index+1)+"/"+filesToUpload.length;
			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					((TextView)findViewById(R.id.progressText)).setText(uiMessage2);
				}
			});
			Log.i(LOG_KEY, "Uploading record " + (index+1) + "/" + filesToUpload.length);
			Uploader.uploadData(this, data, this);
		}
		else
		{
			failed.add(filesToUpload[index]);
			uploadNextRecord();
		}
	}

	@Override
	public void onBackPressed()
	{
		if (uploading)
		{

		}
		else
		{
			super.onBackPressed();
		}
	}

	@Override
	public void onUploadSuccess()
	{
		Log.i(LOG_KEY, "Uploaded record "+(index+1)+"/"+filesToUpload.length+".");
		uploaded.add(filesToUpload[index]);
		uploadNextRecord();
	}

	@Override
	public void onUploadFailure()
	{
		Log.e(LOG_KEY, "Error uploading record "+(index+1)+"/"+filesToUpload.length+".");
		messages.add("Error uploading record "+(index+1)+"/"+filesToUpload.length+".");
		failed.add(filesToUpload[index]);
		uploadNextRecord();
	}

	private void cleanUp()
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				((TextView)findViewById(R.id.progressText)).setText("Cleaning up...");
			}
		});

		for (File file : uploaded)
		{
			file.delete();
		}

		done();
	}

	private void done()
	{
		uploading = false;
		final View uploadSavedFilesDetailsView = findViewById(R.id.uploadSavedFilesDetailsView);
		final View uploadSavedFilesProgressView = findViewById(R.id.uploadSavedFilesProgressView);
		final View uploadSavedFilesResultView = findViewById(R.id.uploadSavedFilesResultView);
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				uploadSavedFilesDetailsView.setVisibility(View.GONE);
				uploadSavedFilesProgressView.setVisibility(View.GONE);
				uploadSavedFilesResultView.setVisibility(View.VISIBLE);

				TextView tv = (TextView) findViewById(R.id.resultText);

				StringBuilder sb = new StringBuilder();
				sb.append("Process finished.\n\n");
				sb.append("Records successfully uploaded: ");
				sb.append(uploaded.size());
				sb.append('\n');
				sb.append("Records failed to upload: ");
				sb.append(failed.size());
				sb.append('\n');
				if (failed.size() > 0)
				{
					sb.append("Records that failed to upload for been retained, you can retry uploading they by returning to the first screen and selecting 'Upload saved' again.\n ");
				}
				View failedRecordsOptions = findViewById(R.id.failedRecordsOptions);
				if (failedRecordsOptions!=null)
				{
					failedRecordsOptions.setVisibility(failed.size() > 0 ? View.VISIBLE : View.GONE);
				}
				if (messages.size() > 0)
				{
					sb.append("Messages:\n ");
					for (String message : messages)
					{
						sb.append(message);
						sb.append('\n');
					}
				}

				tv.setText(sb.toString());
			}
		});
		looperThread.quitLooper();
		looperThread = null;
	}

	public void retryFailed(View view)
	{
		upload(view);
	}

	public void deleteFailed(View view)
	{
		for (File file : failed)
		{
			file.delete();
		}

		finish();
	}

	class LooperThread extends Thread {
		public Handler mHandler;

		public void run() {
			Looper.prepare();

			mHandler = new Handler();

			Looper.loop();
		}

		public void quitLooper()
		{
			mHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					Looper.myLooper().quit();
				}
			});
		}
	}
}
