/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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

/**
 * Extention to the FilterFactoryImpl, supplies the HSQL specific filters 
 * needed for query generation.
 *
 * @author Amr Alam, Refractions Research
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/hsql/src/main/java/org/geotools/filter/HsqlFilterFactoryImpl.java $
 */
public class HsqlFilterFactoryImpl extends FilterFactoryImpl {
    /**
     * Creates a new instance of HsqlFilterFactoryImpl
     */
    public HsqlFilterFactoryImpl() {
    }

    /**
     * Creates a Geometry Expression with an initial schema.
     *
     * @param colName a Strint with the schema to create with
     *
     * @return the new Attribute Expression
     */
    public GeometryExpressionImpl createGeometryExpression(String colName) {
        return new GeometryExpressionImpl(colName);
    }
}
