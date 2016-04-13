package uk.ac.horizon.babyface.fragment;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.Map;

import uk.ac.horizon.babyface.activity.PagerActivity;

public abstract class PageFragment extends Fragment
{
	void nextPage()
	{
		if(getActivity() instanceof PagerActivity)
		{
			((PagerActivity) getActivity()).nextPage(null);
		}
	}

	void update()
	{
		if(getActivity() instanceof PagerActivity)
		{
			((PagerActivity) getActivity()).update();
		}
	}

	String getParam()
	{
		return getArguments().getString("param");
	}

	void setValue(Object value)
	{
		String param = getParam();
		if(param != null)
		{
			Log.i("data", "Set " + param + " to " + value);
			getData().put(param, value);
		}
	}

	Map<String, Object> getData()
	{
		PagerActivity activity = (PagerActivity)getActivity();
		return activity.getData();
	}

	String getString(String named, String defaultValue)
	{
		int stringID = getResources().getIdentifier(named, "string", getActivity().getPackageName());
		if (stringID != 0)
		{
			return getString(stringID);
		}
		else
		{
			return defaultValue;
		}
	}

	Object getValue()
	{
		String param = getParam();
		if(param != null)
		{
			return getData().get(param);
		}
		return null;
	}

	public int getTint()
	{
		return Color.BLACK;
	}

	public boolean allowPrev()
	{
		return true;
	}
	public boolean allowNext()
	{
		return true;
	}
}
