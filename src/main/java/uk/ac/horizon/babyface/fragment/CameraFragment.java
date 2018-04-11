package uk.ac.horizon.babyface.fragment;

import android.Manifest;
import android.content.ContentValues;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

import uk.ac.horizon.babyface.R;
import uk.ac.horizon.babyface.camera.CameraView;

@SuppressWarnings("deprecation")
public class CameraFragment extends PageFragment
{
	private static final int CAMERA_PERMISSION_REQUEST = 47;
	private static final int STORAGE_PERMISSION_REQUEST = 57;
	private View photoButton;
	private CameraView cameraView;
	private String title;
	private Bitmap image;
	private View progress;

	@Override
	public boolean showBanner()
	{
		return false;
	}

	public CameraFragment()
	{
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
		if (cameraView != null)
		{
			if (isVisibleToUser)
			{
				if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
				{
					requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
				}
				else
				{
					cameraView.startCamera();
				}
			}
			else
			{
				new Thread()
				{
					@Override
					public void run()
					{
						cameraView.stopCamera();
					}
				}.start();
			}
		}
	}

	public static CameraFragment create(String param)
	{
		final Bundle bundle = new Bundle();
		bundle.putString("param", param);
		final CameraFragment fragment = new CameraFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	private String capitalize(final String line)
	{
		return Character.toUpperCase(line.charAt(0)) + line.substring(1);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.camera, container, false);

		cameraView = (CameraView) rootView.findViewById(R.id.cameraView);
		cameraView.setCallback(new CameraView.Callback()
		{
			@Override
			public void started()
			{
				getActivity().runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						progress.setVisibility(View.GONE);
						photoButton.setVisibility(View.VISIBLE);
					}
				});
			}
		});

		final String param = getParam();
		int imageID = getResources().getIdentifier(param, "drawable", getActivity().getPackageName());
		if (imageID != 0)
		{
			ImageView viewfinder = (ImageView) rootView.findViewById(R.id.viewfinder);
			viewfinder.setImageResource(imageID);
		}

		title = getString(param + "_title", param);
		TextView hintText = (TextView) rootView.findViewById(R.id.photoText);
		hintText.setText(title);

		progress = rootView.findViewById(R.id.progress);
		progress.setVisibility(View.VISIBLE);
		photoButton = rootView.findViewById(R.id.photoButton);
		photoButton.setVisibility(View.GONE);
		photoButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				photoButton.setVisibility(View.GONE);
				progress.setVisibility(View.VISIBLE);
				cameraView.takePicture(new Camera.PictureCallback()
				{
					@Override
					public void onPictureTaken(final byte[] data, Camera camera)
					{
						new Thread()
						{
							@Override
							public void run()
							{
								try
								{
									int angleToRotate = cameraView.getInfo().getRotation();
									// Solve image inverting problem
									//angleToRotate = angleToRotate + 180;
									Bitmap orignalImage = BitmapFactory.decodeByteArray(data, 0, data.length);
									image = rotate(orignalImage, angleToRotate);


									DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
									String date = df.format(Calendar.getInstance().getTime());
									getData().put("photo_"+param+"_time",date);
									Log.i(this.getClass().getSimpleName(), "photo_"+param+"_time="+date);

									if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
									{
										if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE))
										{
											requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST);
										}
										else
										{
											writeToLocal();
											getActivity().runOnUiThread(new Runnable()
											{
												@Override
												public void run()
												{
													nextPage();
												}
											});
										}
									}
									else
									{
										writeToMedia();
										getActivity().runOnUiThread(new Runnable()
										{
											@Override
											public void run()
											{
												nextPage();
											}
										});
									}
								}
								catch (Exception e)
								{
									Log.e("PHOTO", e.getMessage(), e);
									progress.setVisibility(View.GONE);
									photoButton.setVisibility(View.VISIBLE);
								}
							}
						}.start();
					}

				});

			}
		});

		return rootView;
	}

	private void writeToLocal()
	{
		try
		{
			final String filename = "IMG_" + UUID.randomUUID().toString() + ".jpg";
			ContextWrapper cw = new ContextWrapper(getContext());
			final File file = new File(cw.getFilesDir(), filename);

			final FileOutputStream fos = new FileOutputStream(file);
			image.compress(Bitmap.CompressFormat.JPEG, 90, fos);
			image = null;
			fos.close();
			setValue(file);
		}
		catch (Exception e)
		{
			Log.e("PHOTO", e.getMessage(), e);
		}

	}

	private void writeToMedia()
	{
		try
		{
			final String filename = "IMG_" + UUID.randomUUID().toString();
			final String DCIM = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
			final String DIRECTORY = DCIM + "/Camera";
			final File directory = new File(DIRECTORY);
			if (!directory.exists())
			{
				directory.mkdir();
			}
			final String path = DIRECTORY + '/' + filename + ".jpg";

			Log.i("imagePath", path);
			OutputStream out = new FileOutputStream(path);
			image.compress(Bitmap.CompressFormat.JPEG, 90, out);
			image = null;
			out.close();

			setValue(new File(path));

			File file = (File) getValue();
			if (file != null)
			{
				// Insert into MediaStore.
				ContentValues values = new ContentValues(5);

				values.put(MediaStore.Images.Media.TITLE, "BabyFace " + title);
				values.put(MediaStore.Images.Media.DISPLAY_NAME, "BabyFace " + title);
				values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
				values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
				values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
				values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());

				getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
			}
		}
		catch (Exception e)
		{
			Log.e("PHOTO", e.getMessage(), e);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		switch (requestCode)
		{
			case CAMERA_PERMISSION_REQUEST:
			{
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					cameraView.startCamera();
				}
				else
				{
					Log.i("a", "Permission not granted");
					// TODO
				}
			}

			case STORAGE_PERMISSION_REQUEST:
			{
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					writeToMedia();
					getActivity().runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							nextPage();
						}
					});
				}
				else
				{
					writeToLocal();
					getActivity().runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							nextPage();
						}
					});
				}
			}
		}
	}

	@Override
	public int getTint()
	{
		return Color.WHITE;
	}

	@Override
	public boolean allowNext()
	{
		return getValue() != null;
	}

	private static Bitmap rotate(Bitmap bitmap, int degree)
	{
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		Matrix mtx = new Matrix();
		mtx.postRotate(degree);

		return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
	}
}
