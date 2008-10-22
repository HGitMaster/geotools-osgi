/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.feature.iso.simple;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import org.geotools.feature.iso.type.AttributeTypeImpl;
import org.geotools.feature.iso.type.GeometryTypeImpl;
import org.geotools.feature.iso.type.SchemaImpl;
import org.geotools.feature.type.TypeName;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryType;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Schema containing a simple set of types for import into
 * the SimpleFeatureBuilder.
 * <p>
 * These types represent a good choice for default java bindings, for data
 * sources that do not have specific or complicated needs. As such these
 * types are made available as static final constants to be inlined in code
 * where needed.
 * </p>
 * When would you not use this class?
 * <ul>
 * <li><b>For specific mappings:</b> Create a custom Schema when working with GML or where specific XML Schema
 *    mappings are useful to track.
 * <li><b>For restricted basic types:</b> Create a custom Schema when working with a Data Source that has different
 *    needs for "basic" types. Shapefile for example needs a length restriction
 *    on its Text type and cannot make use of STRING as provided here.
 * </ul>
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 */
public class SimpleSchema extends SchemaImpl {
    
    public static final String NAMESPACE = "http://www.geotools.org/simple";
    //
    // Builtin Java Types
    //
    /** BOOLEAN to Boolean.class */        
    public static final AttributeType BOOLEAN = new AttributeTypeImpl(
        new TypeName(NAMESPACE,"boolean"), Boolean.class, false,
        false,Collections.EMPTY_SET,null, null
    );
    /** String to String.class */ 
    public static final AttributeType STRING = new AttributeTypeImpl(
        new TypeName(NAMESPACE,"string"), String.class, false,
        false,Collections.EMPTY_SET,null, null
    );
    //
    // Numerics
    //
    /** NUMBER to Number.class */    
    public static final AttributeType NUMBER = new AttributeTypeImpl(
        new TypeName(NAMESPACE,"number"), Number.class, false,
        false,Collections.EMPTY_SET,null, null
    );
    /**
     * INTEGER to java Integer.class
     */    
    public static final AttributeType INTEGER = new AttributeTypeImpl(
        new TypeName(NAMESPACE,"integer"), Integer.class, false,
        false,Collections.EMPTY_SET,NUMBER, null
    );
    /**
     * FLOAT to java Float.class
     */      
    public static final AttributeType FLOAT = new AttributeTypeImpl(
        new TypeName(NAMESPACE,"float"), Float.class, false,
        false,Collections.EMPTY_SET,NUMBER, null
    );
    /** DOUBLE to Double.class */
    public static final AttributeType DOUBLE = new AttributeTypeImpl(
        new TypeName(NAMESPACE,"double"), Double.class, false,
        false,Collections.EMPTY_SET,NUMBER, null
    );
    /** LONG to Long.class */
    public static final AttributeType LONG = new AttributeTypeImpl(
        new TypeName(NAMESPACE,"long"), Long.class, false,
        false,Collections.EMPTY_SET,NUMBER, null
    );
    /** SHORT to Short.class */
    public static final AttributeType SHORT = new AttributeTypeImpl(
        new TypeName(NAMESPACE,"short"), Short.class, false,
        false,Collections.EMPTY_SET,NUMBER, null
    );
    /** BYTE to Byte.class */
    public static final AttributeType BYTE = new AttributeTypeImpl(
        new TypeName(NAMESPACE,"byte"), Byte.class, false,
        false,Collections.EMPTY_SET,NUMBER, null
    );

    //
    // TEMPORAL
    //
    /** DATE to Data.class */
    public static final AttributeType DATE = new AttributeTypeImpl(
        new TypeName(NAMESPACE,"date"), Date.class, false,
        false,Collections.EMPTY_SET,null, null
    );
    /**
     * DATETIME to Calendar.class.
     * <p>
     * Data and a Time like a timestamp.
     */    
    public static final AttributeType DATETIME = new AttributeTypeImpl(
        new TypeName(NAMESPACE,"datetime"), Calendar.class, false,
        false,Collections.EMPTY_SET,null, null
    );
    
    //
    // Geomtries
    //
    /** Geometry to Geometry.class */
    public static final GeometryType GEOMETRY = new GeometryTypeImpl(
        new TypeName(NAMESPACE,"geometry"), Geometry.class, null, false, false, 
        Collections.EMPTY_SET, null, null
    );
    /** POINT (extends GEOMETRY) binds to Point.class */    
    public static final GeometryType POINT = new GeometryTypeImpl(
        new TypeName(NAMESPACE,"point"), Point.class, null, false, false, 
        Collections.EMPTY_SET, GEOMETRY, null
    );
    /** LINESTRING (extends GEOMETRY) binds to LineString.class */        
    public static final GeometryType LINESTRING = new GeometryTypeImpl(
        new TypeName(NAMESPACE,"linestring"), LineString.class, null, false, 
        false, Collections.EMPTY_SET, GEOMETRY, null
    );
    /** LINEARRING (extends GEOMETRY) binds to LinearRing.class */            
    public static final GeometryType LINEARRING = new GeometryTypeImpl(
        new TypeName(NAMESPACE,"linearring"), LinearRing.class, null, false, 
        false, Collections.EMPTY_SET, LINESTRING, null
    );
    /**  POLYGON (extends GEOMETRY) binds to Polygon.class */            
    public static final GeometryType POLYGON = new GeometryTypeImpl(
        new TypeName(NAMESPACE,"polygon"), Polygon.class, null, false, 
        false, Collections.EMPTY_SET, GEOMETRY, null
    );
    /**  MULTIGEOMETRY (extends GEOMETRY) binds to GeometryCollection.class */                
    public static final GeometryType MULTIGEOMETRY = new GeometryTypeImpl(
        new TypeName(NAMESPACE,"multigeometry"), GeometryCollection.class, null,
        false, false, Collections.EMPTY_SET, GEOMETRY, null
    );
    
    /**  MULTIPOINT (extends MULTIGEOMETRY) binds to MultiPoint.class */            
    public static final GeometryType MULTIPOINT = new GeometryTypeImpl(
        new TypeName(NAMESPACE,"multipoint"), MultiPoint.class, null, false, false, 
        Collections.EMPTY_SET, MULTIGEOMETRY, null
    );
    
    /**  MULTILINESTRING (extends MULTIGEOMETRY) binds to MultiLineString.class */            
    public static final GeometryType MULTILINESTRING = new GeometryTypeImpl(
        new TypeName(NAMESPACE,"multilinestring"), MultiLineString.class, null, 
        false, false, Collections.EMPTY_SET, MULTIGEOMETRY, null
    );
    
    /** MULTIPOLYGON (extends MULTIGEOMETRY) binds to MultiPolygon.class */            
    public static final GeometryType MULTIPOLYGON = new GeometryTypeImpl(
        new TypeName(NAMESPACE,"multipolygon"), MultiPolygon.class, null, false, 
        false, Collections.EMPTY_SET, MULTIGEOMETRY, null
    );
    
    public SimpleSchema() {
        super(NAMESPACE);
        
        put(INTEGER.getName(),INTEGER);
        put(DOUBLE.getName(),DOUBLE);
        put(LONG.getName(),LONG);
        put(FLOAT.getName(),FLOAT);
        put(SHORT.getName(),SHORT);
        put(BYTE.getName(),BYTE);
        put(NUMBER.getName(),NUMBER);
        put(STRING.getName(),STRING);
        put(BOOLEAN.getName(),BOOLEAN);
        put(DATE.getName(),DATE);
        put(DATETIME.getName(),DATETIME);
        
        put(GEOMETRY.getName(),GEOMETRY);
        put(POINT.getName(),POINT);
        put(LINESTRING.getName(),LINESTRING);
        put(LINEARRING.getName(),LINEARRING);
        put(POLYGON.getName(),POLYGON);
        put(MULTIGEOMETRY.getName(),MULTIGEOMETRY);
        put(MULTIGEOMETRY.getName(),MULTIGEOMETRY);
        put(MULTIPOINT.getName(),MULTIPOINT);
        put(MULTILINESTRING.getName(),MULTILINESTRING);
        put(MULTIPOLYGON.getName(),MULTIPOLYGON);
        
    }

}
