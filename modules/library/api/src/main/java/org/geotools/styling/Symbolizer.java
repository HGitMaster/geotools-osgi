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
 *
 */
package org.geotools.styling;



/**
 * This is the parent interface of all Symbolizers.
 *
 * <p>
 * A symbolizer describes how a feature should appear on a map. The symbolizer
 * describes not just the shape that should appear but also  such graphical
 * properties as color and opacity.
 * </p>
 *
 * <p>
 * A symbolizer is obtained by specifying one of a small number of  different
 * types of symbolizer and then supplying parameters to overide its default
 * behaviour.
 * </p>
 *
 * <p>
 * The details of this object are taken from the <a
 * href="https://portal.opengeospatial.org/files/?artifact_id=1188"> OGC
 * Styled-Layer Descriptor Report (OGC 02-070) version 1.0.0.</a>
 * </p>
 *
 * <p>
 * Renderers can use this information when displaying styled features,  though
 * it must be remembered that not all renderers will be able to fully
 * represent strokes as set out by this interface.  For example, opacity may
 * not be supported.
 * </p>
 *
 * <p>
 * The graphical parameters and their values are derived from SVG/CSS2
 * standards with names and semantics which are as close as possible.
 * </p>
 *
 * <p></p>
 *
 * @author James Macgill, CCG
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/api/src/main/java/org/geotools/styling/Symbolizer.java $
 * @version $Id: Symbolizer.java 31133 2008-08-05 15:20:33Z johann.sorel $
 */
public interface Symbolizer extends org.opengis.style.Symbolizer{
    void accept(org.geotools.styling.StyleVisitor visitor);
}
