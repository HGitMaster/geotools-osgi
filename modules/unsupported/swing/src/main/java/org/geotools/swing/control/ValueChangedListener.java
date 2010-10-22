/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.swing.control;

/**
 * A listener to work with controls derived from {@code JValueTextField}.
 *
 * @see JValueField
 * @see ValueChangedEvent
 * 
 * @author Michael Bedward
 * @since 2.6.1
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/swing/src/main/java/org/geotools/swing/control/ValueChangedListener.java $
 * @version $Id: ValueChangedListener.java 34290 2009-10-30 12:36:40Z mbedward $
 */
public interface ValueChangedListener {

    /**
     * Called by the control whose value has just changed
     *
     * @param ev the event
     */
    public void onValueChanged( ValueChangedEvent ev );

}
