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

package uk.ac.horizon.babyface.adapter;

import uk.ac.horizon.babyface.controller.Controller;

public class URLAdapter<T> extends TextAdapter<T>
{
	private static final String HTTP = "http://";

	public URLAdapter(String name)
	{
		super(name);
	}

	@Override
	public String getText(Object value)
	{
		if(value == null)
		{
			return null;
		}
		String text = value.toString();
		if(text.startsWith(HTTP))
		{
			return text.substring(HTTP.length());
		}
		return text;
	}


	@Override
	public void setValue(Controller<T> controller, Object value)
	{
		if(value instanceof String && !((String)value).contains("://"))
		{
			super.setValue(controller, HTTP + value);
		}
		else
		{
			super.setValue(controller, value);
		}
	}
}
