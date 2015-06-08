package uk.ac.horizon.babyface.fragment;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import uk.ac.horizon.babyface.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraFragment extends PageFragment implements SurfaceHolder.Callback
{
	private SurfaceHolder surfaceHolder;

	public CameraFragment()
	{
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser)
		{
			Log.i("", "Visible");
			try
			{
				if (getCamera() != null)
				{
					getCamera().release();
					getCamera().start(surfaceHolder);
				}
			}
			catch (Exception e)
			{
				Log.w("", e.getMessage(), e);
			}
		}
	}

	public static CameraFragment create(String image)
	{
		Bundle bundle = new Bundle();
		bundle.putString("image", image);
		CameraFragment fragment = new CameraFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_camera, container, false);

		SurfaceView surfaceView = (SurfaceView) rootView.findViewById(R.id.cameraView);
		surfaceHolder = surfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		String image = "face";
		if (getArguments() != null && getArguments().containsKey("image"))
		{
			image = getArguments().getString("image");
		}

		int viewID = getResources().getIdentifier(image, "drawable", getActivity().getPackageName());
		if (viewID != 0)
		{
			ImageView viewfinder = (ImageView) rootView.findViewById(R.id.viewfinder);
			viewfinder.setImageResource(viewID);
		}

		View button = rootView.findViewById(R.id.photoButton);
		button.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				getCamera().takePicture(new Camera.PictureCallback()
				{
					@Override
					public void onPictureTaken(byte[] data, Camera camera)
					{
						try
						{
							// Create a media file name
							final String title = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
							final String DCIM = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
							final String DIRECTORY = DCIM + "/Camera";
							final File directory = new File(DIRECTORY);
							if (!directory.exists())
							{
								directory.mkdir();
							}
							final String path = DIRECTORY + '/' + title + ".jpg";

							int angleToRotate = getCamera().getRotation();
							// Solve image inverting problem
							//angleToRotate = angleToRotate + 180;
							Bitmap orignalImage = BitmapFactory.decodeByteArray(data, 0, data.length);
							Bitmap bitmapImage = rotate(orignalImage, angleToRotate);

							OutputStream out = new FileOutputStream(path);
							bitmapImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
							out.close();

							String image = "face";
							if (getArguments() != null && getArguments().containsKey("image"))
							{
								image = getArguments().getString("image");
							}

							// Insert into MediaStore.
							ContentValues values = new ContentValues(5);
							values.put(MediaStore.Images.Media.TITLE, "BabyFace " + image);
							values.put(MediaStore.Images.Media.DISPLAY_NAME, "BabyFace " + image);
							values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
							// Add the date meta data to ensure the image is added at the front of the gallery
							values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
							values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
							values.put(MediaStore.Images.Media.DATA, path);
							// Clockwise rotation in degrees. 0, 90, 180, or 270.
							//values.put(MediaStore.Images.Media.ORIENTATION, getActivity().getWindowManager().getDefaultDisplay().getRotation() + 90);

							Uri uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//							String image = "face";
//							if (getArguments() != null && getArguments().containsKey("image"))
//							{
//								image = getArguments().getString("image");
//							}

							getController().getModel().getImages().put(image, path);
							getController().notifyChanges("images");

							nextPage(null);
						}
						catch (Throwable e)
						{
							// This can happen when the external volume is already mounted, but
							// MediaScanner has not notify MediaProvider to add that volume.
							// The picture is still safe and MediaScanner will find it and
							// insert it into MediaProvider. The only problem is that the user
							// cannot click the thumbnail to review the picture.
							Log.e("", e.getMessage(), e);
						}
					}

				});

			}
		});

		return rootView;
	}

	public static Bitmap rotate(Bitmap bitmap, int degree)
	{
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		Matrix mtx = new Matrix();
		mtx.postRotate(degree);

		return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
	}

	public void surfaceCreated(SurfaceHolder holder)
	{
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
//		Log.i("", "Surface Changed");
//		try
//		{
//			getCamera().start(holder);
//		}
//		catch (Exception e)
//		{
//			Log.d("", "Error setting camera preview: " + e.getMessage());
//		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{

	}
}
