package uk.ac.horizon.babyface.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import uk.ac.horizon.babyface.R;

public class PhotoFragment extends PageFragment
{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.fragment_photo, container, false);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
		if(isVisibleToUser)
		{
			ImageView photoView = (ImageView) getView().findViewById(R.id.photoView);

			String image = "face";
			if (getArguments() != null && getArguments().containsKey("image"))
			{
				image = getArguments().getString("image");
			}

			int stringID = getResources().getIdentifier("reward_" + image, "string", getActivity().getPackageName());
			if(stringID != 0)
			{
				TextView rewardText = (TextView) getView().findViewById(R.id.rewardText);
				rewardText.setText(stringID);
			}

			Bitmap bit = BitmapFactory.decodeFile(getController().getModel().getImages().get(image));
			photoView.setImageBitmap(bit);
		}
	}

	public static PhotoFragment create(String image)
	{
		Bundle bundle = new Bundle();
		bundle.putString("image", image);
		PhotoFragment fragment = new PhotoFragment();
		fragment.setArguments(bundle);
		return fragment;
	}
}
