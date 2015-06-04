/*
 * Aestheticodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2015  Aestheticodes
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.horizon.babyface.binding;

import android.content.Context;
import android.view.View;
import uk.ac.horizon.babyface.controller.Controller;

public abstract class Action<T> implements BindingFactory<T>
{
	public Action()
	{
	}

	@Override
	public Binding<T> createBinding(final View view)
	{
		return new Binding<T>() {
			@Override
			public void updateView(final Controller<T> controller, String... properties)
			{
				view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v)
					{
						execute(view.getContext(), controller);
					}
				});
			}
		};
	}

	public abstract void execute(Context context, Controller<T> controller);
}
