package uk.ac.horizon.babyface.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import uk.ac.horizon.babyface.R;
import uk.ac.horizon.babyface.adapter.FloatAdapter;
import uk.ac.horizon.babyface.adapter.IntAdapter;
import uk.ac.horizon.babyface.adapter.TextAdapter;
import uk.ac.horizon.babyface.binding.Binding;
import uk.ac.horizon.babyface.controller.Controller;
import uk.ac.horizon.babyface.controller.ViewController;
import uk.ac.horizon.babyface.model.BabyData;

public class DataFragment extends PageFragment
{
	private Controller<BabyData> controller;

	public DataFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_data, container, false);

		controller = new ViewController<>(rootView);
		controller.bindView(R.id.ageText, new TextAdapter<BabyData>("age") {
			@Override
			public String getText(Object value)
			{
				if(value instanceof Integer)
				{
					int intValue = (Integer)value;
					if(intValue == 0)
					{
						return "Born today";
					}
					else if(intValue == 1)
					{
						return "Born yesterday";
					}
					else
					{
						return "Born " + value + " days ago";
					}
				}
				else
				{
					return "Born today";
				}
			}
		});
		controller.bindView(R.id.dueText, new TextAdapter<BabyData>("due") {
			@Override
			public String getText(Object value)
			{
				if(value instanceof Integer)
				{
					int intValue = (Integer)value;
					if(intValue == -1)
					{
						return "Born 1 day early";
					}
					else if(intValue == 0)
					{
						return "Born on due date";
					}
					else if(intValue == 1)
					{
						return "Born 1 day late";
					}
					else if(intValue < -1)
					{
						return "Born " + -intValue + " days early";
					}
					else if(intValue > 1)
					{
						return "Born " + intValue + " days late";
					}
				}
				else
				{
					return "Born on due date";
				}

				return null;
			}
		});
		controller.bindView(R.id.ageSlider, new IntAdapter<BabyData>("age", -28, 0)
		{
			@Override
			protected Object adapt(Object value)
			{
				if(value instanceof Integer)
				{
					int intValue = (Integer)value;
					if(intValue > 0)
					{
						return 0 - intValue;
					}
				}
				return super.adapt(value);
			}

			@Override
			public void setValue(Controller<BabyData> controller, Object value)
			{
				if(value instanceof Integer)
				{
					int intValue = (Integer)value;
					if(intValue < 0)
					{
						super.setValue(controller, 0 - intValue);
						return;
					}
				}
				super.setValue(controller, value);
			}
		});
		controller.bindView(R.id.dueSlider, new IntAdapter<BabyData>("due", -28, 14));
		controller.bindView(R.id.weightEdit, new FloatAdapter<BabyData>("weight"));
		controller.bindView(R.id.genderSpinner, "gender");
		controller.bindView(R.id.ethnicitySpinner, "ethnicity");

		controller.bind(new Binding<BabyData>() {
			@Override
			public void updateView(Controller<BabyData> controller, String... properties)
			{
				getController().notifyChanges(properties);
			}
		});

		return rootView;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser)
	{
		super.setUserVisibleHint(isVisibleToUser);
		if(isVisibleToUser)
		{
			controller.setModel(getController().getModel());
		}
	}
}