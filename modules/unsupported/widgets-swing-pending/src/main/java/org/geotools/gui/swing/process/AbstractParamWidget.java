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

import org.geotools.data.Parameter;
import org.geotools.text.Text;

/**
 * Super class that provides additional helper methods
 * useful when implementing your own ParamWidget.

 * @author gdavis
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/process/AbstractParamWidget.java $
 */
public abstract class AbstractParamWidget implements ParamWidget {
	protected final Parameter< ? > parameter;
	
	/**
	 * Holds on to the parameter so implementations
	 * can consult the type and metadata information.
	 * 
	 * @param parameter
	 */
	AbstractParamWidget( Parameter<?> parameter ){
	    this.parameter = parameter; 
	}
}
