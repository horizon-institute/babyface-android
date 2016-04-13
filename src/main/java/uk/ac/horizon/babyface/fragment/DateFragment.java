package uk.ac.horizon.babyface.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import uk.ac.horizon.babyface.R;

public class DateFragment extends PageFragment
{
	public static DateFragment create(String param, long start, long end)
	{
		final Bundle bundle = new Bundle();
		bundle.putString("param", param);
		bundle.putLong("start", start);
		bundle.putLong("end", end);
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

		final CalendarView calendarView = (CalendarView) rootView.findViewById(R.id.calendarView);
		final String param = getArguments().getString("param");
		final long start = getArguments().getLong("start");
		final long end = getArguments().getLong("end");
		calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener()
		{
			@Override
			public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth)
			{
				Calendar calendar = Calendar.getInstance();
				calendar.set(year, month, dayOfMonth);
				setDate(calendar);
			}
		});
		calendarView.setMinDate(start);
		calendarView.setMaxDate(end);

		setDate(Calendar.getInstance());

		int label = getResources().getIdentifier(param + "_question", "string", getActivity().getPackageName());

		if(label != 0)
		{
			TextView textView = (TextView) rootView.findViewById(R.id.label);
			textView.setText(label);
		}

		return rootView;
	}

	private void setDate(Calendar calendar)
	{
		final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy", getResources().getConfiguration().locale);

		setValue(dateFormat.format(calendar.getTime()));

		if(getParam().equals("birthDate"))
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
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{

	}
}