/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.gui.swing.process;

import javax.swing.JComponent;

/**
 *  Interface for creating parameter widgets.  A ParamWidget handles
 *  creating, validating and maintaining a widget for one process parameter.
 * 
 * @author Graham Davis
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/process/ParamWidget.java $
 */
public interface ParamWidget {
	
	/**
	 * Called to build the widget, initialize it (setting defaults or
	 * whatever) and setup any listeners needed for validation of the widget value.
	 * The returned JComponent will contain the widget for editing.
	 * 
	 * @return JComponent or null if error
	 */	
	public JComponent doLayout();

	/**
	 * Validates the current value of the widget, returns false if not valid,
	 * true otherwise
	 * 
	 * @return boolean if validated
	 */	
	public boolean validate();
	
	/**
	 * Sets the value of the widget.  
	 * 
	 * @param Object an object containing the value to set for the widget
	 */	
	public void setValue(Object value);
	
	/**
	 * Returns the current value of the widget.  
	 * 
	 * @return Object representing the current value of the widget
	 */	
	public Object getValue();	
}
