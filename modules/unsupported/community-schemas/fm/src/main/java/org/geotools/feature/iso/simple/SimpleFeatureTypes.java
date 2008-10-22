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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.geotools.feature.SchemaException;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.simple.SimpleTypeFactory;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Utility class for working with simple feature types.
 * 
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class SimpleFeatureTypes {

    /**
     * Injected factory
     */
    SimpleTypeFactory factory;
    
    /**
     * Builder used to create new types.
     */
    SimpleTypeBuilder builder;
    
    /**
     * Creates a new instance of the class.
     * 
     * @param builder The factory dependency.
     */
    public SimpleFeatureTypes(SimpleTypeFactory factory) {
        this.factory = factory;
        builder = new SimpleTypeBuilder(factory);
        builder.load( new SimpleSchema() );
    }
    
    /**
     * Creates a new simple feature type from an old one restricting the set
     * of attributes to maintain in the new type.
     * <p>
     *  The order of hte attributes in the new type is specified by the order
     *  of the names in the <code>properties</code> paramter, not by the 
     *  order of the properties in the original type.
     * </p>
     * 
     * @param featureType The original type.
     * @param properties The set of names to include from the original type.
     * 
     * @return The newly created type.
     */
    public SimpleFeatureType sub(SimpleFeatureType featureType, Name[] properties) {
        
        if ((properties == null)) {
            return featureType;
        }
        
        boolean same = featureType.getAttributeCount() == properties.length;
        
        for (int i = 0; (i < featureType.getAttributeCount()) && same; i++) {
            AttributeType type = featureType.getType(i);
            same = type.getName().equals(properties[i]);
        }

        if (same) {
            return featureType;
        }

        builder.init(featureType);
        
        //remove any attributes not specified and ensure order
        List attributes = new ArrayList();
        for (int i = 0; i < properties.length; i++) {
            for (Iterator itr = builder.getAttributes().iterator(); itr.hasNext();) {
                AttributeDescriptor ad = (AttributeDescriptor) itr.next();
                if (ad.getName().equals(properties[i])) {
                    attributes.add(itr.next());
                }
            }
        }
        builder.getAttributes().clear();
        builder.getAttributes().addAll(attributes);
        
        return (SimpleFeatureType) builder.feature();
       
    }
    
    /**
     * Utility method for SimpleFeatureType construction.
     * 
     * <p>
     * Will parse a String of the form: <i>"name:Type,name2:Type2,..."</i>
     * </p>
     * 
     * <p>
     * Where <i>Type</i> is defined by createAttribute.
     * </p>
     * 
     * <p>
     * You may indicate the default Geometry with an astrix: "*geom:Geometry".
     * </p>
     * 
     * <p>
     * Example:<code>name:"",age:0,geom:Geometry,centroid:Point,url:java.io.URL"</code>
     * </p>
     *
     * @param identification identification of FeatureType:
     *        (<i>namesapce</i>).<i>typeName</i>
     * @param typeSpec Specification for FeatureType
     *
     * @throws SchemaException In the event of an invalid type spec.
     */
    public SimpleFeatureType create(String identification, String typeSpec) 
        throws SchemaException {
        
        int split = identification.lastIndexOf('.');
        String namespace = (split == -1) 
            ? null : identification.substring(0, split);
        String typeName = (split == -1) ? 
                 identification : identification.substring(split + 1);

        if (namespace != null) {
           try {
                new URI(namespace);
           } 
           catch (URISyntaxException badNamespace ) {
               throw (SchemaException) new SchemaException( badNamespace )
                .initCause(badNamespace);       
           }    
        }
    
        builder.init();
        builder.setNamespaceURI(namespace);
        builder.setName(typeName);
    
        String[] types = typeSpec.split(",");    
        for (int i = 0; i < types.length; i++) {
            if (types[i].startsWith("*")) {
                types[i] = types[i].substring(1);
                builder.setGeometryName( types[i] );
            }            
            String name = name(types[i]);
            Class clazz = clazz(types[i]);
            
            builder.attribute(name, clazz);
        }
        return (SimpleFeatureType) builder.feature();
    }
    
    /**
     * Helper method used by {@link #create(String, String)} to pull a name
     * out of a type spec.
     * 
     */ 
    protected String name(String typeSpec) {
        int split = typeSpec.indexOf(":");

        String name;
        
        if (split == -1) {
            name = typeSpec;
        
        } 
        else {
            name = typeSpec.substring(0, split);
        }

        return name;
    }

    /**
     * Helper method used by {@link #create(String, String)} to determine if 
     * a type is nillable based on a type spec.
     * 
     */ 
    protected boolean isNillable(String typeSpec) {
        int split = typeSpec.indexOf(":");

        String hint = null;

        if (split != -1) {
            int split2 = typeSpec.indexOf(":", split + 1);

            if (split2 != -1) {
                hint = typeSpec.substring(split2 + 1);
            }
        }
        
        return hint != null && hint.indexOf("nillable") != -1;
    }
    
    /**
     * Helper method used by {@link #create(String, String)} to determine the 
     * class of a type from the typeSpec.
     * 
     */ 
    protected Class clazz(String typeSpec) {
        int split = typeSpec.indexOf(":");

         String type;
         
         if (split == -1) {
             type = "String";
         } 
         else {
         
             int split2 = typeSpec.indexOf(":", split + 1);
    
             if (split2 == -1) {
                 type = typeSpec.substring(split + 1);
             } else {
                 type = typeSpec.substring(split + 1, split2);
             }
         }
    
         
        try {
            return type(type);
        } 
         catch (ClassNotFoundException e) {
            return null;
        }
    }
    
    static Map typeMap = new HashMap();

    static {
        typeMap.put("String", String.class);
        typeMap.put("string", String.class);
        typeMap.put("\"\"", String.class);
        typeMap.put("Integer", Integer.class);
        typeMap.put("int", Integer.class);
        typeMap.put("0", Integer.class);
        typeMap.put("Double", Double.class);
        typeMap.put("double", Double.class);
        typeMap.put("0.0", Double.class);
        typeMap.put("Float", Float.class);
        typeMap.put("float", Float.class);
        typeMap.put("0.0f", Float.class);
        typeMap.put("Geometry", Geometry.class);
        typeMap.put("Point", Point.class);
        typeMap.put("LineString", LineString.class);
        typeMap.put("Polygon", Polygon.class);
        typeMap.put("MultiPoint", MultiPoint.class);
        typeMap.put("MultiLineString", MultiLineString.class);
        typeMap.put("MultiPolygon", MultiPolygon.class);
        typeMap.put("GeometryCollection", GeometryCollection.class);
    }
    
    /**
     * Helper method used by {@link #create(String, String)} to map strings
     * to java classes.
     * 
     * @throws ClassNotFoundException
     */
    protected Class type(String typeName) throws ClassNotFoundException {
        if (typeMap.containsKey(typeName)) {
            return (Class) typeMap.get(typeName);
        }

        return Class.forName(typeName);
    }
}
