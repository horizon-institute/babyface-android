package uk.ac.horizon.babyface.fragment;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.List;

import uk.ac.horizon.babyface.R;

public class ListFragment extends PageFragment
{
	public static ListFragment create(String param)
	{
		final Bundle bundle = new Bundle();
		bundle.putString("param", param);
		final ListFragment fragment = new ListFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	public ListFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.list, container, false);

		String param = getArguments().getString("param");
		int label = getResources().getIdentifier(param + "_question", "string", getActivity().getPackageName());

		TextView textView = (TextView) rootView.findViewById(R.id.label);
		textView.setText(label);

		Log.i("", param);

		int listID = getResources().getIdentifier(param + "_list", "array", getActivity().getPackageName());
		if(listID != 0)
		{
			String[] items = getResources().getStringArray(listID);
			RadioGroup listView = (RadioGroup) rootView.findViewById(R.id.list);
			@IdRes
			int index = 1;
			for (final String item : items)
			{
				View root = getLayoutInflater(savedInstanceState).inflate(R.layout.list_item, listView, false);
				final RadioButton itemView = (RadioButton)root.findViewById(R.id.radioButton);
				itemView.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						setValue(item);
						update();
					}
				});
				itemView.setId(index);
				index++;
				itemView.setText(item);


				listView.addView(root);
			}
		}


		rootView.forceLayout();

		return rootView;
	}


	@Override
	public boolean allowNext()
	{
		String param = getArguments().getString("param");
		int validOptionsListID = getResources().getIdentifier(param + "_list_valid_options", "array", getActivity().getPackageName());
		Object value = getValue();
		if (getValue() == null)
		{
			return false;
		}
		else
		{
			if (validOptionsListID == 0)
			{
				return true;
			}
			else
			{
				String[] items = getResources().getStringArray(validOptionsListID);
				for (String s : items)
				{
					if (s.equals((String) value)) return true;
				}
				return false;
			}
		}
	}


}