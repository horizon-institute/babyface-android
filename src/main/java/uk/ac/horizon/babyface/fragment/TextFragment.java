package uk.ac.horizon.babyface.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import uk.ac.horizon.babyface.R;

public class TextFragment extends PageFragment
{


	/**
	 * minLength <= ResultString.length() <= maxLength
	 */
	public static TextFragment create(String param, int minLength, int maxLength, boolean singleLine, boolean numberEntry, boolean allowUnknown)
	{
		final Bundle bundle = new Bundle();
		bundle.putString("param", param);
		bundle.putInt("minLength", minLength);
		bundle.putInt("maxLength", maxLength);
		bundle.putBoolean("singleLine", singleLine);
		bundle.putBoolean("numberEntry", numberEntry);
		bundle.putBoolean("allowUnknown", allowUnknown);
		final TextFragment fragment = new TextFragment();
		fragment.setArguments(bundle);
		return fragment;
	}

	public TextFragment()
	{
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.text, container, false);

		String param = getArguments().getString("param");
		final int minLength = getArguments().getInt("minLength");
		final int maxLength = getArguments().getInt("maxLength");
		int label = getResources().getIdentifier(param + "_question", "string", getActivity().getPackageName());


		final boolean numberEntry = getArguments().getBoolean("numberEntry");
		final boolean allowUnknown = getArguments().getBoolean("allowUnknown");


		TextView textView = (TextView) rootView.findViewById(R.id.label);
		textView.setText(label);
		final EditText editText = (EditText) rootView.findViewById(R.id.editText);
		editText.setSingleLine(getArguments().getBoolean("singleLine"));

		final CheckBox unknownCheckbox = (CheckBox) rootView.findViewById(R.id.unknownCheckBox);

		if (getValue() != null)
		{
			String value = (String) getValue();
			if ("Unknown".equals(value))
			{
				unknownCheckbox.setChecked(true);
				editText.setAlpha(0.5f);
			}
			else
			{
				unknownCheckbox.setChecked(false);
				editText.setText(value);
			}
		}
		else if (minLength == 0)
		{
			setValue("");
		}

		if (numberEntry)
		{
			editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		}

		editText.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
			{

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
			{

			}

			@Override
			public void afterTextChanged(Editable editable)
			{
				// Check if the fragment is visible here as the EditText will retain focus when
				// the page is changed!
				if (getUserVisibleHint())
				{
					String currentString = editText.getText().toString();
					validateTextAndSave(currentString);
					update();
					if (unknownCheckbox!=null) unknownCheckbox.setChecked(false);
				}
				else
				{
				}
			}
		});

		editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				boolean handled = false;
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					hideKeyboard(getActivity());
					nextPage();
					handled = true;
				}
				return handled;
			}
		});

		if (allowUnknown && unknownCheckbox != null)
		{
			unknownCheckbox.setVisibility(View.VISIBLE);
			unknownCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
			{
				@Override
				public void onCheckedChanged(CompoundButton compoundButton, boolean b)
				{
					if (b)
					{
						setValue("Unknown");
						update();
						editText.setAlpha(0.5f);
					}
					else
					{
						String currentString = editText.getText().toString();
						validateTextAndSave(currentString);
						update();
						editText.setAlpha(1f);
					}
				}
			});
		}
		else
		{
			unknownCheckbox.setVisibility(View.GONE);
		}

		rootView.forceLayout();

		return rootView;
	}

	@Override
	public boolean allowNext()
	{
		return getValue() != null;
	}

	void validateTextAndSave(String currentString)
	{

		final int minLength = getArguments().getInt("minLength");
		final int maxLength = getArguments().getInt("maxLength");
		final boolean numberEntry = getArguments().getBoolean("numberEntry");

		if (!numberEntry)
		{
			if (minLength <= currentString.length() && currentString.length() <= maxLength)
			{
				setValue(currentString);
			}
			else
			{
				setValue(null);
			}
		}
		else
		{
			if (currentString != null && !currentString.trim().equals(""))
			{
				try
				{
					float currentFloat = Float.parseFloat(currentString);
					if (minLength <= currentFloat && currentFloat <= maxLength)
					{
						setValue(String.valueOf(currentFloat));
					}
					else
					{
						setValue(null);
					}
				}
				catch (Exception e)
				{
					Log.e(this.getClass().getSimpleName(), "Exception saving number.", e);
					setValue(null);
				}
			}
			else
			{
				setValue(null);
			}
		}
	}

	public static void hideKeyboard(Activity activity) {
		// from: https://gist.github.com/lopspower/6e20680305ddfcb11e1e
		View view = activity.findViewById(android.R.id.content);
		if (view != null) {
			InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}
	}
}