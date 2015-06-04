package uk.ac.horizon.babyface.controller;

import android.view.View;
import uk.ac.horizon.babyface.adapter.PropertyAdapter;
import uk.ac.horizon.babyface.binding.Binding;
import uk.ac.horizon.babyface.binding.BindingFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class ControllerBase<T> implements Controller<T>
{
	protected final List<Binding<T>> bindings = new ArrayList<>();

	protected abstract View findView(int viewID);

	public void bindView(int viewID, String propertyName)
	{
		final View view = findView(viewID);
		if (view != null)
		{
			final PropertyAdapter<T> adapter = new PropertyAdapter<>(propertyName);
			bind(adapter.createBinding(view));
		}
	}

	@Override
	public void notifyChanges(String... properties)
	{
		for(Binding<T> binding: bindings)
		{
			binding.updateView(this, properties);
		}
	}

	public void bindView(int viewID, BindingFactory<T> factory)
	{
		final View view = findView(viewID);
		if (view != null)
		{
			bind(factory.createBinding(view));
		}
	}

	@Override
	public void bind(Binding<T> binding)
	{
		if(binding != null)
		{
			bindings.add(binding);
			binding.updateView(this);
		}
	}

	@Override
	public void unbind(Binding<T> binding)
	{
		bindings.remove(binding);
	}

	@Override
	public void unbind()
	{
		bindings.clear();
	}
}
