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
package org.geotools.util;

import java.awt.Color;

import org.geotools.factory.Hints;

/**
 * ConverterFactory for handling color conversions.
 * <p>
 * Supported conversions:
 * <ul>
 * 	<li>"#FF0000" (String) -> Color.RED
 * 	<li>"false" -> Boolean.FALSE
 * 	<li>0xFF0000FF (Integer) -> RED with Alpha
 * </ul>
 * </p>
 * <p>
 * This code was previously part of the SLD utility class, it is being made
 * available as part of the Converters framework to allow for broader use.
 * </p>
 * @author Jody Garnett (Refractions Research)
 * @since 2.5
 */
public class ColorConverterFactory implements ConverterFactory {

	public Converter createConverter(Class source, Class target, Hints hints) {
	    if ( target.equals( Color.class ) ) {
			// string to color
			if ( source.equals( String.class ) ) {
				return new Converter() {
					public Object convert(Object source, Class target) throws Exception {
					    String rgba = (String) source;
					    try {
				            return Color.decode(rgba);
				        } catch (NumberFormatException badRGB) {
				            // unavailable
				            return null;
				        }					    
					}					
				};
			}
			
			// integer to color
			if ( source.equals( Integer.class ) ) {
				return new Converter() {
					public Object convert(Object source, Class target) throws Exception {
					    Integer rgba = (Integer) source;
					    int alpha = 0xff000000 & rgba;
					    return new Color(rgba, alpha != 0 );					    
					}					
				};
			}			
		}		
		return null;
	}

}
