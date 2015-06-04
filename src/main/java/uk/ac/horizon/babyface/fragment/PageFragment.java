package uk.ac.horizon.babyface.fragment;

import android.support.v4.app.Fragment;
import android.view.View;
import uk.ac.horizon.babyface.activity.MainActivity;
import uk.ac.horizon.babyface.controller.CameraController;
import uk.ac.horizon.babyface.controller.Controller;
import uk.ac.horizon.babyface.model.BabyData;

public abstract class PageFragment extends Fragment
{
	public Controller<BabyData> getController()
	{
		if(getActivity() instanceof Controller)
		{
			return ((MainActivity)getActivity());
		}
		return null;
	}

	public void prevPage(View view)
	{
		if(getActivity() instanceof MainActivity)
		{
			((MainActivity) getActivity()).nextPage(view);
		}
	}

	public CameraController getCamera()
	{
		if (getActivity() instanceof  MainActivity)
		{
			return ((MainActivity)getActivity()).getCamera();
		}
		return null;
	}

	public void nextPage(View view)
	{
		if(getActivity() instanceof MainActivity)
		{
			((MainActivity) getActivity()).nextPage(view);
		}
	}
}
