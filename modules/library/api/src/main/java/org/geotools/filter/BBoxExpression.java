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
package org.geotools.filter;

import com.vividsolutions.jts.geom.Envelope;


/**
 * A convenience expression to form a geometry literal from an  envelope.
 *
 * @author Ian Turton, CCG
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/library/api/src/main/java/org/geotools/filter/BBoxExpression.java $
 * @version $Id: BBoxExpression.java 37277 2011-05-24 06:48:54Z mbedward $
 * @depreaced Please just use a simple literal
 */
@SuppressWarnings("deprecation")
public interface BBoxExpression extends LiteralExpression {
    /**
     * Set the bbox for this expression
     *
     * @param env The envelope to set as the bounds.
     *
     * @throws IllegalFilterException If the box can not be created.
     */
    void setBounds(Envelope env) throws IllegalFilterException;
}
