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

		public Range(int min, int max, String units)
		{
			this.min = min;
			this.max = max;
			this.units = units;
		}
	}

	private NumberPicker numberPicker;
	private NumberPicker numberPicker2;
	private Range range;

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
		this.range = range;
		List<String> items = new ArrayList<>();
		int rangeMin = range.min / 10;
		int rangeMax = range.max / 10;
		for (int index = rangeMin; index <= rangeMax; index++)
		{
			items.add("" + index);
		}
		String[] itemArray = items.toArray(new String[items.size()]);
		numberPicker.setDisplayedValues(null);
		numberPicker.setMaxValue(rangeMax - rangeMin);
		numberPicker.setDisplayedValues(itemArray);

		if(rangeMin == rangeMax)
		{
			numberPicker.setAlpha(0.5f);
			numberPicker2.setAlpha(0.5f);
			numberPicker2.setDisplayedValues(null);
			numberPicker2.setMaxValue(0);

			items = new ArrayList<>();
			items.add(".0");
		}
		else
		{
			numberPicker.setAlpha(1);
			numberPicker2.setAlpha(1);
			numberPicker2.setDisplayedValues(null);
			numberPicker2.setMaxValue(9);

			items = new ArrayList<>();
			for (int index = 0; index <= 9; index++)
			{
				items.add("." + index);
			}
		}

		itemArray = items.toArray(new String[items.size()]);
		numberPicker2.setDisplayedValues(itemArray);

		Log.i("number", "Value = " + numberPicker.getValue() + " of " + range.min + "-" + range.max);

		if(rangeMin == rangeMax)
		{
			setValue("Unknown");
		}
		else
		{
			setValue(numberPicker.getDisplayedValues()[numberPicker.getValue()] + numberPicker2.getDisplayedValues()[numberPicker2.getValue()] + range.units);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.number, container, false);


		String param = getArguments().getString("param");
		numberPicker = (NumberPicker) rootView.findViewById(R.id.numberPicker);
		numberPicker2 = (NumberPicker) rootView.findViewById(R.id.numberPicker2);
		numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
		{
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal)
			{
				setValue(numberPicker.getDisplayedValues()[numberPicker.getValue()] + numberPicker2.getDisplayedValues()[numberPicker2.getValue()] + range.units);
			}
		});

		final String rangeString = getArguments().getString("ranges");
		if (rangeString != null)
		{
			final Range[] ranges = gson.fromJson(rangeString, Range[].class);

			if (ranges.length > 0)
			{
				final RadioGroup unitGroup = (RadioGroup) rootView.findViewById(R.id.unitLayout);
				setRange(ranges[0]);
				@IdRes
				int index = 1;
				boolean selected = false;

				for (final Range range : ranges)
				{
					final RadioButton rangeButton = new RadioButton(getContext());
					rangeButton.setText(range.units);
					rangeButton.setId(index);
					index++;
					if(!selected)
					{
						rangeButton.setChecked(true);
						selected = true;
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

		int label = getResources().getIdentifier(param + "_question", "string", getActivity().getPackageName());

		if (label != 0)
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