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

package uk.ac.horizon.babyface.controller;

import uk.ac.horizon.babyface.binding.Binding;
import uk.ac.horizon.babyface.binding.BindingFactory;

public interface Controller<T>
{
	void bindView(int viewID, String propertyName);
	void bindView(int viewID, BindingFactory<T> factory);
	void bind(Binding<T> binding);
	void unbind(Binding<T> binding);
	void unbind();
	T getModel();
	void setModel(T model);
	void notifyChanges(String... properties);
}
