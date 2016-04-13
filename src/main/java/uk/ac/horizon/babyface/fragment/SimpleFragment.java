package uk.ac.horizon.babyface.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import uk.ac.horizon.babyface.R;

public class SimpleFragment extends PageFragment
{
	public SimpleFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		int layout = R.layout.welcome;
		if(getArguments() != null)
		{
			layout = getArguments().getInt("layout", layout);
		}
		return inflater.inflate(layout, container, false);
	}

	public static SimpleFragment create(int layout)
	{
		Bundle bundle = new Bundle();
		bundle.putInt("layout", layout);
		SimpleFragment fragment = new SimpleFragment();
		fragment.setArguments(bundle);
		return fragment;
	}
}