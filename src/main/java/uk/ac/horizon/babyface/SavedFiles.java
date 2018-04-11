package uk.ac.horizon.babyface;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SavedFiles
{
	public interface FileSaveListener {
		void onSaveSuccess();
		void onSaveFailure(final String why);
	}
	public static void saveData(Context context, Map<String,Object> dataToSave, FileSaveListener listener)
	{
		final String SAVE_FILE_LOG_KEY = "SAVE_ENTRY";
		Log.i(SAVE_FILE_LOG_KEY, "Attempting to save data to file.");

		try
		{

			// swap file objects for uri
			final Map<String, Object> data = new HashMap<>();
			data.putAll(dataToSave);
			for (String param : data.keySet())
			{
				Object value = data.get(param);
				if (value instanceof File)
				{
					data.put(param, "file://" + ((File) value).getAbsoluteFile());
				}
			}


			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
			String date = df.format(Calendar.getInstance().getTime());
			data.put("client_save_time",date);
			Log.i(SAVE_FILE_LOG_KEY, "client_save_time="+date);

			JSONObject jsonObject = new JSONObject(data);

			// Get or make directory:
			File externalFilesDir = context.getExternalFilesDir(null);
			File savedDataDir = new File(externalFilesDir, "savedData");
			if (!savedDataDir.exists())
			{
				savedDataDir.mkdir();
			}
			if (!savedDataDir.exists())
			{
				Log.w(SAVE_FILE_LOG_KEY, "Save data directory could not be created.");
				listener.onSaveFailure("Save data directory could not be created.");
				return;
			}
			Log.i(SAVE_FILE_LOG_KEY, "Save dir: "+savedDataDir.getAbsolutePath());

			// Write to file:
			String filename = "ngsavedata_" + System.currentTimeMillis() + ".json";
			Log.i(SAVE_FILE_LOG_KEY, "Filename: "+filename);
			Writer output;
			File file = new File(savedDataDir, filename);
			try
			{
				output = new BufferedWriter(new FileWriter(file));
				output.write(jsonObject.toString());
				output.close();
			}
			catch (IOException e)
			{
				Log.e(SAVE_FILE_LOG_KEY, "There was an error saving data file.", e);
				listener.onSaveFailure("There was an error saving data file (" + e.getMessage() + ").");
				return;
			}

			// Check data was written:
			try
			{
				if (file.exists())
				{
					JSONObject jsonObjectRead = new JSONObject(readFile(file));
					if (jsonObject.equals(jsonObjectRead))
					{
						Log.i(SAVE_FILE_LOG_KEY, "Data read back confirm.");
					}
					else
					{
						Log.w(SAVE_FILE_LOG_KEY, "Data read back does not equal data written.");
					}
				}
				else
				{
					Log.w(SAVE_FILE_LOG_KEY, "File written does not exist.");
				}
			}
			catch (JSONException e)
			{
				Log.e(SAVE_FILE_LOG_KEY, "Error parsing data read back from file.", e);
			}
			catch (IOException e)
			{
				Log.e(SAVE_FILE_LOG_KEY, "Error reading file back after saving.", e);
			}
		}
		catch (Exception e)
		{
			Log.e(SAVE_FILE_LOG_KEY, "There was an unexpected exception.", e);
			listener.onSaveFailure("There was an unexpected exception ("+e.getMessage()+")");
			return;
		}

		listener.onSaveSuccess();
	}

	public static File[] getSavedFiles(Context context)
	{
		final String SAVE_FILE_LOG_KEY = "SavedFiles.getSavedFile";

		// Get or make directory:
		File externalFilesDir = context.getExternalFilesDir(null);
		File savedDataDir = new File(externalFilesDir, "savedData");
		if (!savedDataDir.exists())
		{
			savedDataDir.mkdir();
		}
		if (!savedDataDir.exists())
		{
			Log.w(SAVE_FILE_LOG_KEY, "Save data directory could not be created.");
			return null;
		}
		Log.i(SAVE_FILE_LOG_KEY, "Save dir: "+savedDataDir.getAbsolutePath());

		return savedDataDir.listFiles();
	}

	public static Map<String, Object> loadFile(File file) throws IOException, JSONException
	{
		String dataString = readFile(file);
		JSONObject jsonObject = new JSONObject(dataString);
		Map<String, Object> dataMap = new HashMap<>();
		Iterator<String> keys = jsonObject.keys();
		while (keys.hasNext())
		{
			String key = keys.next();
			Object value = jsonObject.get(key);
			dataMap.put(key, value);
		}

		// swap uri objects for files
		for (String param : dataMap.keySet())
		{
			Object value = dataMap.get(param);
			if (value instanceof String && ((String)value).startsWith("file://"))
			{
				File valueFile = new File(((String)value).replace("file://",""));
				dataMap.put(param, valueFile);
				if (!valueFile.exists())
				{
					Log.w("SaveFiles.loadFile", "File referenced in saved data does not exist.");
					throw new FileNotFoundException("One or more of the image files could not be found.");
				}
			}
		}

		return dataMap;
	}


	private static String readFile(File file) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		StringBuilder stringBuilder = new StringBuilder();
		while ((line = reader.readLine()) != null)
		{
			stringBuilder.append(line);
		}
		reader.close();
		return stringBuilder.toString();
	}
}
