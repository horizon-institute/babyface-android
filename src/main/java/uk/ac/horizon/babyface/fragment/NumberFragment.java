package uk.ac.horizon.babyface.fragment;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.ac.horizon.babyface.R;

public class NumberFragment extends PageFragment
{
	public static class Value
	{
		private final int range;
		private final String[] items;

		public Value(int min, int max, String postfix)
		{
			this(min, max, "", postfix);
		}

		public Value(int min, int max, String prefix, String postfix)
		{
			this.range = max - min;

			List<String> items = new ArrayList<>();
			for (int index = min; index <= max; index++)
			{
				items.add(prefix + index + postfix);
			}
			this.items = items.toArray(new String[items.size()]);
		}
	}

	public static class Option
	{
		private final String name;
		private final List<Value> values = new ArrayList<>();

		public Option(String name, Value... values)
		{
			this.name = name;
			Collections.addAll(this.values, values);
		}
	}

	private static final Gson gson = new Gson();
	private LinearLayout pickers;
	private Option option;

	public NumberFragment()
	{
	}

	public static NumberFragment create(String param, Option... options)
	{
		final Bundle bundle = new Bundle();
		bundle.putString("param", param);
		bundle.putString("options", gson.toJson(options));
		final NumberFragment fragment = new NumberFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.number, container, false);

		String param = getArguments().getString("param");
		pickers = (LinearLayout) rootView.findViewById(R.id.pickers);

		final String rangeString = getArguments().getString("options");
		Log.i("range", rangeString);
		if (rangeString != null)
		{
			final Option[] options = gson.fromJson(rangeString, Option[].class);

			if (options.length > 0)
			{
				final RadioGroup unitGroup = (RadioGroup) rootView.findViewById(R.id.unitLayout);
				setOption(options[0]);
				@IdRes
				int index = 1;
				boolean selected = false;

				for (final Option option : options)
				{
					final RadioButton rangeButton = new RadioButton(getContext());
					rangeButton.setText(option.name);
					rangeButton.setId(index);
					index++;
					if (!selected)
					{
						rangeButton.setChecked(true);
						selected = true;
					}
					rangeButton.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							setOption(option);
						}
					});
					Log.i("range", "Add button " + rangeButton);
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

		rootView.forceLayout();

		return rootView;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{

	}

	private String createValue()
	{
		if (option == null)
		{
			return null;
		}

		if (option.values.isEmpty())
		{
			return option.name;
		}

		StringBuilder builder = new StringBuilder();
		for (int index = 0; index < pickers.getChildCount(); index++)
		{
			View view = pickers.getChildAt(index);
			if (view instanceof NumberPicker)
			{
				NumberPicker picker = (NumberPicker) view;
				builder.append(picker.getDisplayedValues()[picker.getValue()]);
			}
		}
		return builder.toString();
	}

	private void setOption(Option option)
	{
		if (this.option != option)
		{
			this.option = option;
			pickers.removeAllViews();
			for (Value value : option.values)
			{
				NumberPicker picker = new NumberPicker(getContext());
				picker.setMaxValue(value.range);
				picker.setDisplayedValues(value.items);
				picker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
				picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener()
				{
					@Override
					public void onValueChange(NumberPicker picker, int oldVal, int newVal)
					{
						setValue(createValue());
					}
				});

				Log.i("range", "Add view " + picker);
				pickers.addView(picker);
			}

			pickers.forceLayout();
			setValue(createValue());
		}
	}
}