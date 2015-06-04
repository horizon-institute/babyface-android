package uk.ac.horizon.babyface.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import uk.ac.horizon.babyface.R;
import uk.ac.horizon.babyface.model.BabyData;

import java.io.File;

public class UploadFragment extends PageFragment
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		final View root = inflater.inflate(R.layout.fragment_upload, container, false);

		final View uploadButton = root.findViewById(R.id.uploadButton);
		uploadButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				final BabyData babyData = getController().getModel();
				final ProgressBar progressBar = (ProgressBar) root.findViewById(R.id.progress);
				final View progressButton = root.findViewById(R.id.progressButton);

				progressBar.setVisibility(View.VISIBLE);
				progressButton.setVisibility(View.VISIBLE);
				uploadButton.setVisibility(View.GONE);

				Ion.with(getActivity())
						.load("http://www.cs.nott.ac.uk/babyface/upload.php")
						.uploadProgressBar(progressBar)
						.setMultipartParameter("weight", Float.toString(babyData.getWeight()))
						.setMultipartParameter("due", Integer.toString(babyData.getDue()))
						.setMultipartParameter("age", Integer.toString(babyData.getAge()))
						.setMultipartParameter("gender", babyData.getGender().name())
						.setMultipartParameter("ethnicity", babyData.getEthnicity().name())
						.setMultipartFile("face", new File(babyData.getImages().get("face")))
						.setMultipartFile("ear", new File(babyData.getImages().get("ear")))
						.setMultipartFile("foot", new File(babyData.getImages().get("foot")))
						.asString()
						.setCallback(new FutureCallback<String>()
						{
							@Override
							public void onCompleted(Exception e, String result)
							{
								progressBar.setVisibility(View.INVISIBLE);
								progressButton.setVisibility(View.GONE);
								root.findViewById(R.id.shareButton).setVisibility(View.VISIBLE);
							}
						});
			}
		});

		return root;
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
