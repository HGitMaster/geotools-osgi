/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.caching.util;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.opengis.feature.Attribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.geotools.feature.DefaultFeatureBuilder;
import org.geotools.feature.IllegalAttributeException;


/** Simple marshaller that can write features to an ObjectOutputStream.
 * Feature is not Serializable, but this is based on the idea that most attributes object are Serializable
 * (JTS geometries are Serializable), and that attributes which are not simple, are either a collection we can iterate through, or another Feature.
 * Serialization is then achieved recursively.
 * Unmarshalling implies to know the FeatureType of the marshalled feature.
 *
 * Storage format : Header,
 *                  Attributes
 *
 * Header := int     : FeatureType hashCode,
 *           String  : FeatureType name,
 *           String  : Feature ID,
 *           int     : number of attributes
 * Attributes := [Attribute]
 * Attribute  := int : multiplicity, or O if simple, or -1 if FeatureAttribute,
 *               Object|Feature|[Attribute] : attribute value
 *
 * This implementation does not have the ambition of being robust.
 *
 * @task test with other FeatureType than DefaultFeatureType
 * @task add method marshall(Feature, ByteArrayOutputStream) and unmarshall(ByteArrayOutputStream), or create sub class.
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 */
public class SimpleFeatureMarshaller {
    /**
     * marker to indicate an attribute is a feature in the serialized form
     */
    public static final int FEATURE = -1;
    public static final int SIMPLEATTRIBUTE = 0;
    static DefaultFeatureBuilder builder = new DefaultFeatureBuilder();
    HashMap<TypeKey, SimpleFeatureType> types;

    /** Default constructor.
     */
    public SimpleFeatureMarshaller() {
        types = new HashMap<TypeKey, SimpleFeatureType>();
    }

    public void registerType(SimpleFeatureType type) {
        TypeKey key = new TypeKey(type);

        if (!types.containsKey(key)) {
            types.put(key, type);
        }
    }

    public SimpleFeatureType typeLookUp(int typeHash, String typeName) {
        TypeKey key = new TypeKey(typeHash, typeName);

        return types.get(key);
    }

    public Map getRegisteredTypes() {
        return types;
    }

    /** Marshall a feature into a stream.
     * The type of that feature is not marshalled,
     * only hashCode of FeatureType and type name is marshalled.
     *
     * @param f the Feature to marshall
     * @param s the stream to write to
     * @throws IOException
     */
    public void marshall(SimpleFeature f, ObjectOutput s)
        throws IOException {
        SimpleFeatureType type = (SimpleFeatureType) f.getType();
        registerType(type);
        s.writeInt(type.hashCode());
        s.writeObject(type.getName().getURI());
        s.writeObject(f.getID());

        int natt = f.attributes().size();
        s.writeInt(natt);

        for (Iterator it = f.attributes().iterator(); it.hasNext();) {
            Attribute att = (Attribute) it.next();
            marshallSimpleAttribute(att.getValue(), s);
        }
    }

    /** Marshall an attribute into a stream.
     *
     * @task test object is instance of Serializable
     *
     * @param o an attribute value which is Serializable, or a feature, or a collection
     * @param s the stream to write to
     * @throws IOException
     */
    protected void marshallSimpleAttribute(Object o, ObjectOutput s)
        throws IOException {
        if (o instanceof Collection) { // this should never happen ...
                                       //            Collection c = (Collection) o;
                                       //            s.writeInt(c.size());
                                       //
                                       //            for (Iterator it = c.iterator(); it.hasNext();) {
                                       //                Object nxt = it.next();
                                       //                marshallComplexAttribute(nxt, s);
                                       //            }
            throw new IllegalArgumentException(
                "Got instance of SimpleFeature with complex attributes.");
        } else if (o instanceof SimpleFeature) {
            s.writeInt(FEATURE);
            marshall((SimpleFeature) o, s);
        } else {
            s.writeInt(SIMPLEATTRIBUTE);
            s.writeObject(o);
        }
    }

    /** Inverse operation of marshall : read a feature from a stream.
     *
     * @param s the stream to read from
     * @return the unmarshalled feature
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws IllegalAttributeException
     */
    public SimpleFeature unmarshall(ObjectInput s)
        throws IOException, ClassNotFoundException, IllegalAttributeException {
        int typeHash = s.readInt();
        String typeName = (String) s.readObject();
        SimpleFeatureType type = typeLookUp(typeHash, typeName);

        if (type != null) {
            SimpleFeature f = unmarshall(s, type);

            if (f == null) {
                System.err.println("Returning null feature");
            }

            return f;
        } else {
            throw new IllegalStateException(typeName + " is not a registered type.");
        }
    }

    /** Inverse operation of marshall : read a feature from a stream.
     *
     * @param s the stream to read from
     * @param the type of the feature to unmarshall
     * @return the unmarshalled feature
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws IllegalAttributeException
     */
    protected SimpleFeature unmarshall(ObjectInput s, SimpleFeatureType type)
        throws IOException, ClassNotFoundException, IllegalAttributeException {
        String fid = (String) s.readObject();
        int natt = s.readInt();

        builder.setType(type);

        if (!(natt == type.getAttributeCount())) {
            throw new IOException("Schema error");
        }

        for (int i = 0; i < natt; i++) {
            builder.add(unmarshallSimpleAttribute(s));
        }

        return builder.feature(fid);
    }

    /** Read attribute values from a stream.
     *
     * @param s the stream to read from
     * @return a list of attribute values, possibly a singleton, if attribute's multiplicity is 1
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws IllegalAttributeException
     */
    protected Object unmarshallSimpleAttribute(ObjectInput s)
        throws IOException, ClassNotFoundException, IllegalAttributeException {
        int m = s.readInt();
        Object att;

        if (m == SIMPLEATTRIBUTE) {
            att = s.readObject();
        } else if (m == FEATURE) {
            SimpleFeature f = unmarshall(s);
            att = f;
        } else { // this should never happen
            throw new IllegalAttributeException(
                "Found complex attribute which is not legal for SimpleFeature.");

            //            for (int i = 0; i < m; i++) {
            //                atts.addAll(unmarshallComplexAttribute(s));
            //            }
        }

        return att;
    }
}


class TypeKey implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 3939840339831295334L;
    int typeHash;
    String typeName;

    public TypeKey(SimpleFeatureType type) {
        this.typeHash = type.hashCode();
        this.typeName = type.getName().getURI();
    }

    public TypeKey(int hash, String name) {
        this.typeHash = hash;
        this.typeName = name;
    }

    public int hashCode() {
        return typeHash;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (o instanceof TypeKey) {
            TypeKey key = (TypeKey) o;

            return (typeHash == key.typeHash) && (typeName.equals(key.typeName));
        } else {
            return false;
        }
    }
}
