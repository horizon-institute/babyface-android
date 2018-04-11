package uk.ac.horizon.babyface.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import uk.ac.horizon.babyface.R;

public class SpinnerFragment extends PageFragment
{


	public static SpinnerFragment create(String param)
	{
		final Bundle bundle = new Bundle();
		bundle.putString("param", param);
		final SpinnerFragment fragment = new SpinnerFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	public SpinnerFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.spinner, container, false);

		final String param = getArguments().getString("param");
		int label = getResources().getIdentifier(param + "_question", "string", getActivity().getPackageName());



		TextView textView = (TextView) rootView.findViewById(R.id.label);
		textView.setText(label);
		final Spinner spinner = (Spinner) rootView.findViewById(R.id.spinner);


		int listID = getResources().getIdentifier(param + "_list", "array", getActivity().getPackageName());
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), listID, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		if (getValue() == null)
		{
			setValue(adapter.getItem(0));
		}

		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
			{
				setValue(parent.getItemAtPosition(position));
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView)
			{

			}
		});


		rootView.forceLayout();

		return rootView;
	}

	@Override
	public boolean allowNext()
	{
		return getValue() != null;
	}

}