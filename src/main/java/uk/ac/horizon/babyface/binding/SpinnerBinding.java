package uk.ac.horizon.babyface.binding;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import uk.ac.horizon.babyface.R;
import uk.ac.horizon.babyface.adapter.PropertyAdapter;
import uk.ac.horizon.babyface.adapter.ValueAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpinnerBinding<U> extends EditorBinding<Spinner, U>
{
	private List<Object> items = new ArrayList<>();
	private final LayoutInflater layoutInflater;
	private final BaseAdapter spinnerAdapter = new BaseAdapter()
	{
		private String capitalize(Object object)
		{
			if(object == null)
			{
				return null;
			}
			String result = object.toString();
			if (result != null && result.length() > 0 && Character.isLowerCase(result.charAt(0)))
			{
				return Character.toUpperCase(result.charAt(0)) + result.substring(1);
			}
			else
			{
				return result;
			}
		}

		@Override
		public int getCount()
		{
			return items.size() + 1;
		}

		@Override
		public Object getItem(int position)
		{
			if(position == 0)
			{
				return null;
			}
			else
			{
				return items.get(position - 1);
			}
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent)
		{
			parent.setVerticalScrollBarEnabled(false);
			if (position == 0)
			{
				TextView textView = new TextView(view.getContext());
				textView.setHeight(0);
				textView.setVisibility(View.GONE);
				return textView;
			}
			else
			{
				// Pass convertView as null to prevent reuse of special case views
				return super.getDropDownView(position, null, parent);
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			TextView view = (TextView)layoutInflater.inflate(R.layout.select_item, parent, false);
			if(position == 0)
			{
				if(adapter instanceof PropertyAdapter)
				{
					view.setText(capitalize(((PropertyAdapter) adapter).getName()));
				}
				view.setTextColor(0xFF999999);
			}
			else
			{
				view.setText(capitalize(getItem(position)));
				view.setTextColor(0xFF111111);
			}
			return view;
		}
	};

	public SpinnerBinding(Spinner view, final ValueAdapter<U> adapter)
	{
		super(view, adapter);
		layoutInflater = (LayoutInflater) view.getContext().getSystemService
			(Context.LAYOUT_INFLATER_SERVICE);
		view.setAdapter(spinnerAdapter);
		view.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				Log.i("", "Select " + position);
				if(position == 0)
				{
					adapter.setValue(controller, null);
				}
				else
				{
					adapter.setValue(controller, parent.getItemAtPosition(position));
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent)
			{

			}
		});
	}

	@Override
	protected void updateView(Object object)
	{
		if (adapter.getType() != null && adapter.getType().isEnum())
		{
			final Object[] objects = adapter.getType().getEnumConstants();
			items = Arrays.asList(objects);
			Log.i("", "Items " + TextUtils.join(", ", items));
		}

		spinnerAdapter.notifyDataSetChanged();
		final int selected = items.indexOf(object) + 1;
		Log.i("", "Selected " + selected);
		view.setSelection(selected);
	}
}
