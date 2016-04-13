package uk.ac.horizon.babyface.fragment;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import uk.ac.horizon.babyface.R;

public class NumberFragment extends PageFragment
{
	private static final Gson gson = new Gson();

	public static class Range
	{
		private final int min;
		private final int max;
		private final String units;
		private final String name;
		private final boolean unknown;

		public Range(int min, int max, String units)
		{
			this.min = min;
			this.max = max;
			this.units = units;
			this.name = units;
			this.unknown = true;
		}

		public Range(int min, int max, String units, String name)
		{
			this.min = min;
			this.max = max;
			this.units = units;
			this.name = name;
			this.unknown = true;
		}
	}

	private NumberPicker numberPicker;

	public static NumberFragment create(String param, Range... ranges)
	{
		final Bundle bundle = new Bundle();
		bundle.putString("param", param);
		bundle.putString("ranges", gson.toJson(ranges));
		final NumberFragment fragment = new NumberFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	public NumberFragment()
	{
	}

	private void setRange(Range range)
	{
		List<String> items = new ArrayList<>();
		if(range.unknown)
		{
			items.add("Unknown");
		}
		for(int index = range.min; index <= range.max; index++)
		{
			items.add(""+ (index / 10.0) + range.units);
		}
		String[] itemArray = items.toArray(new String[items.size()]);
		numberPicker.setDisplayedValues(null);
		if(range.unknown)
		{
			numberPicker.setMaxValue(range.max + 1 - range.min);
		}
		else
		{
			numberPicker.setMaxValue(range.max - range.min);
		}
		numberPicker.setDisplayedValues(itemArray);
		Log.i("number", "Value = " + numberPicker.getValue() + " of " + range.min + "-" + range.max);
		setValue(numberPicker.getDisplayedValues()[numberPicker.getValue()]);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.number, container, false);


		String param = getArguments().getString("param");
		numberPicker = (NumberPicker) rootView.findViewById(R.id.numberPicker);
		numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
		{
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal)
			{
				setValue(numberPicker.getDisplayedValues()[newVal]);
			}
		});

		final String rangeString = getArguments().getString("ranges");
		if(rangeString != null)
		{
			final Range[] ranges = gson.fromJson(rangeString, Range[].class);

			if(ranges.length > 0)
			{
				setRange(ranges[0]);
				if(ranges.length > 1)
				{
					final RadioGroup unitGroup = (RadioGroup) rootView.findViewById(R.id.unitLayout);
					boolean selected = false;
					@IdRes
					int index = 1;
					for(final Range range: ranges)
					{
						final RadioButton rangeButton = new RadioButton(getContext());
						rangeButton.setText(range.name);
						rangeButton.setId(index);
						index++;
						if(!selected)
						{
							selected = true;
							rangeButton.setChecked(true);
						}
						rangeButton.setOnClickListener(new View.OnClickListener()
						{
							@Override
							public void onClick(View v)
							{
								setRange(range);
							}
						});
						unitGroup.addView(rangeButton);
					}
				}
			}
		}

		int label = getResources().getIdentifier(param + "_question", "string", getActivity().getPackageName());

		if(label != 0)
		{
			TextView textView = (TextView) rootView.findViewById(R.id.label);
			textView.setText(label);
		}

		return rootView;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{

	}
}