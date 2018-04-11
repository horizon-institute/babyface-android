package uk.ac.horizon.babyface.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import uk.ac.horizon.babyface.R;

public class DateFragment extends PageFragment
{
	private static final String LOG_KEY = "DateFragment";
	public static final String DATE_PICKER_FORMAT = "dd-MM-yyyy";

	public static DateFragment create(String param, long start, long end)
	{
		return create(param, start, end, false);
	}
	public static DateFragment create(String param, long start, long end, boolean canBeUnknown)
	{
		final Bundle bundle = new Bundle();
		bundle.putString("param", param);
		bundle.putLong("start", start);
		bundle.putLong("end", end);
		bundle.putBoolean("canBeUnknown", canBeUnknown);
		final DateFragment fragment = new DateFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	public DateFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.date, container, false);

		final boolean canBeUnknown = getArguments().getBoolean("canBeUnknown");
		final CheckBox unknownCheckBox = (CheckBox)rootView.findViewById(R.id.unknownCheckBox);

		final CalendarView calendarView = (CalendarView) rootView.findViewById(R.id.calendarView);
		final String param = getArguments().getString("param");
		final long start = getArguments().getLong("start");
		final long end = getArguments().getLong("end");

		// Load previously entered value as calendar view will revert to current date if fragment
		// recreates the view! (which happens if you move forward 2 pages then back)
		if (getValue() != null)
		{
			String prevDate = (String) getValue();
			Log.i(LOG_KEY, "Attempting to load previously selected date '"+param+"'='"+prevDate+"'.");
			if (prevDate.equals("Unknown"))
			{
				unknownCheckBox.setChecked(true);
				calendarView.setAlpha(0.5f);
			}
			else
			{
				final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PICKER_FORMAT, getResources().getConfiguration().locale);
				try
				{
					Date d = dateFormat.parse(prevDate);
					calendarView.setDate(d.getTime());
				}
				catch (ParseException e)
				{
					e.printStackTrace();
				}
			}
		} else
		{
			Log.i(LOG_KEY, "No previously selected date for '"+param+"'.");
			setDate(Calendar.getInstance());
		}

		calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener()
		{
			@Override
			public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth)
			{
				Calendar calendar = Calendar.getInstance();
				calendar.set(year, month, dayOfMonth);
				setDate(calendar);
				if (canBeUnknown)
				{
					unknownCheckBox.setChecked(false);
				}
			}
		});
		//calendarView.setMinDate(start);
		//calendarView.setMaxDate(end);

		int label = getResources().getIdentifier(param + "_question", "string", getActivity().getPackageName());

		if(label != 0)
		{
			TextView textView = (TextView) rootView.findViewById(R.id.label);
			textView.setText(label);
		}

		if (canBeUnknown)
		{
			unknownCheckBox.setVisibility(View.VISIBLE);
		}
		else
		{
			unknownCheckBox.setVisibility(View.GONE);
		}
		unknownCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b)
			{
				if (b)
				{
					calendarView.setAlpha(0.5f);
					setValue("Unknown");
					update();
				}
				else
				{
					calendarView.setAlpha(1f);
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(calendarView.getDate());
					setDate(calendar);
				}
			}
		});

		return rootView;
	}

	private void setDate(Calendar calendar)
	{
		final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PICKER_FORMAT, getResources().getConfiguration().locale);

		try
		{
			final long start = dateFormat.parse(dateFormat.format(getArguments().getLong("start"))).getTime();
			final long end = dateFormat.parse(dateFormat.format(getArguments().getLong("end"))).getTime();
			final long selectedTime = dateFormat.parse(dateFormat.format(calendar.getTime())).getTime();

			Log.i(this.getClass().getSimpleName(), start + "<=" + selectedTime + "<=" + end);
			if (start <= selectedTime && selectedTime <= end)
			{
				Log.i(this.getClass().getSimpleName(), "true");
				setValue(dateFormat.format(selectedTime));
			}
			else
			{
				Log.i(this.getClass().getSimpleName(), "false");
				setValue(null);
			}

			if (getParam().equals("birthDate"))
			{
				try
				{
					final Date date = dateFormat.parse(dateFormat.format(new Date()));
					final long diff = date.getTime() - calendar.getTimeInMillis();
					final long age = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
					getData().put("age", age);
				}
				catch (ParseException e)
				{
					e.printStackTrace();
				}
			}
		} catch (Exception e)
		{
			Log.e(this.getClass().getSimpleName(), "Exception setting date.", e);
			setValue(null);
		}

		update();
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{

	}
	public boolean allowNext()
	{
		return getValue() != null;
	}
}