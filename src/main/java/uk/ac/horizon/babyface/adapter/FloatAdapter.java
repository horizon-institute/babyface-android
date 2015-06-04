package uk.ac.horizon.babyface.adapter;

import android.util.Log;
import uk.ac.horizon.babyface.controller.Controller;

public class FloatAdapter<T> extends PropertyAdapter<T>
{
	public FloatAdapter(String name)
	{
		super(name);
	}

	@Override
	public void setValue(Controller<T> controller, Object value)
	{
		Log.i("", "Save float value " + value);
		if(value instanceof String)
		{
			if(value.equals(""))
			{
				super.setValue(controller, null);
			}
			else
			{
				super.setValue(controller, Float.parseFloat((String) value));
			}
		}
		else
		{
			super.setValue(controller, value);
		}
	}
}
