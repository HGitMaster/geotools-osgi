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

import javax.xml.namespace.QName;

import org.geotools.filter.v1_1.OGC;
import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.BinaryLogicOperator;
import org.opengis.filter.Filter;
import org.opengis.filter.Id;
import org.opengis.filter.spatial.BinarySpatialOperator;

/**
 * Convenience class for filter parsing. 
 * <p>
 * The primary function of this class is to share code among the different versions 
 * of filter parsing.
 * </p>
 * @author Justin Deoliveira, OpenGEO
 *
 */
public class FilterParsingUtils {

    public static Object Filter_getProperty(Object object, QName name ) {
        Filter filter = (Filter) object;

        //&lt;xsd:element ref="ogc:spatialOps"/&gt;
        if (OGC.spatialOps.equals(name) && filter instanceof BinarySpatialOperator) {
            return filter;
        }

        //&lt;xsd:element ref="ogc:comparisonOps"/&gt;
        if (OGC.comparisonOps.equals(name) && filter instanceof BinaryComparisonOperator
                //JD: extra check here because many of our spatial implementations
            // extend both      
                && !(filter instanceof BinarySpatialOperator)) {
            return filter;
        }

        //&lt;xsd:element ref="ogc:logicOps"/&gt;
        if (OGC.logicOps.equals(name) && filter instanceof BinaryLogicOperator) {
            return filter;
        }

        //&lt;xsd:element maxOccurs="unbounded" ref="ogc:_Id"/&gt;
        if ( filter instanceof Id && 
            ( OGC._Id.equals(name) /*1.1*/ || OGC.FeatureId.equals(name) /*1.0*/ ) ) {
            //unwrap
            Id id = (Id) filter;

            return id.getIdentifiers();
        }

        return null;
    }
}
