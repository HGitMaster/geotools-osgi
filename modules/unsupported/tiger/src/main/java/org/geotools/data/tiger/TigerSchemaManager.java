/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2003-2004, Julian J. Ray, All Rights Reserved
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
package org.geotools.data.tiger;

import java.util.Hashtable;


/**
 * <p>
 * Title: GeoTools2 Development
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 *
 * @author Julian J. Ray
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/tiger/src/main/java/org/geotools/data/tiger/TigerSchemaManager.java $
 * @version 1.0
 */
public class TigerSchemaManager {
    // These are the typeNames supported by the DataStore

    /** DOCUMENT ME! */
    private static final String[] typeNames = {
        "HIGHWAYS", "RAILROADS", "OTHER_TRANSPORTATION", "HYDROGRAPHY", "LANDMARKS", "PHYSICAL_FEATURES"
    };

    // CFCC Codes used to map Type1 features to typeNames

    /** DOCUMENT ME! */
    private static final String[] typeNameKeys = { "A", "B", "C", "H", "D", "E" };

    /** DOCUMENT ME! */
    private TigerSchemaElement[] highwayAttributes;

    /** DOCUMENT ME! */
    private TigerSchemaElement[] railroadAttributes;

    /** DOCUMENT ME! */
    private TigerSchemaElement[] otherTransAttributes;

    /** DOCUMENT ME! */
    private TigerSchemaElement[] hydroAttributes;

    /** DOCUMENT ME! */
    private TigerSchemaElement[] landmarkAttributes;

    /** DOCUMENT ME! */
    private TigerSchemaElement[] physicalAttributes;

    /** DOCUMENT ME! */
    private Hashtable schemaMapping;

    /** DOCUMENT ME! */
    private Hashtable typeNameKey;

    /**
     * TigerSchemaManager
     */
    public TigerSchemaManager() {
        highwayAttributes = new TigerSchemaElement[14];
        highwayAttributes[0] = new TigerSchemaElement("TLID", "Integer", 5, 15);
        highwayAttributes[1] = new TigerSchemaElement("Fedirp", "String", 17, 19);
        highwayAttributes[2] = new TigerSchemaElement("Fename", "String", 19, 49);
        highwayAttributes[3] = new TigerSchemaElement("Fetype", "String", 49, 53);
        highwayAttributes[4] = new TigerSchemaElement("Fedirs", "String", 53, 55);
        highwayAttributes[5] = new TigerSchemaElement("Ctype", "String", 55, 56); // Classification type always 'A'
        highwayAttributes[6] = new TigerSchemaElement("Ccode", "String", 56, 58); // Classification sub-code
        highwayAttributes[7] = new TigerSchemaElement("Fraddl", "String", 58, 69);
        highwayAttributes[8] = new TigerSchemaElement("Toaddl", "String", 69, 80);
        highwayAttributes[9] = new TigerSchemaElement("Fraddr", "String", 80, 91);
        highwayAttributes[10] = new TigerSchemaElement("Toaddr", "String", 91, 102);
        highwayAttributes[11] = new TigerSchemaElement("Zipl", "String", 106, 111);
        highwayAttributes[12] = new TigerSchemaElement("Zipr", "String", 111, 116);
        highwayAttributes[13] = new TigerSchemaElement("Geometry", "Geometry", -1, -1);

        railroadAttributes = new TigerSchemaElement[5];
        railroadAttributes[0] = new TigerSchemaElement("TLID", "Integer", 5, 15);
        railroadAttributes[1] = new TigerSchemaElement("Name", "String", 19, 49);
        railroadAttributes[2] = new TigerSchemaElement("Ctype", "String", 55, 56); // Classification type always 'B'
        railroadAttributes[3] = new TigerSchemaElement("Ccode", "String", 56, 58); // Classification sub-code
        railroadAttributes[4] = new TigerSchemaElement("Geometry", "Geometry", -1, -1);

        hydroAttributes = new TigerSchemaElement[5];
        hydroAttributes[0] = new TigerSchemaElement("TLID", "Integer", 5, 15);
        hydroAttributes[1] = new TigerSchemaElement("Name", "String", 19, 49);
        hydroAttributes[2] = new TigerSchemaElement("Ctype", "String", 55, 56); // Classification type
        hydroAttributes[3] = new TigerSchemaElement("Ccode", "String", 56, 58); // Classification sub-code
        hydroAttributes[4] = new TigerSchemaElement("Geometry", "Geometry", -1, -1);

        otherTransAttributes = new TigerSchemaElement[5];
        otherTransAttributes[0] = new TigerSchemaElement("TLID", "Integer", 5, 15);
        otherTransAttributes[1] = new TigerSchemaElement("Name", "String", 19, 49);
        otherTransAttributes[2] = new TigerSchemaElement("Ctype", "String", 55, 56); // Classification type
        otherTransAttributes[3] = new TigerSchemaElement("Ccode", "String", 56, 58); // Classification sub-code
        otherTransAttributes[4] = new TigerSchemaElement("Geometry", "Geometry", -1, -1);

        landmarkAttributes = new TigerSchemaElement[5];
        landmarkAttributes[0] = new TigerSchemaElement("TLID", "Integer", 5, 15);
        landmarkAttributes[1] = new TigerSchemaElement("Name", "String", 19, 49);
        landmarkAttributes[2] = new TigerSchemaElement("Ctype", "String", 55, 56); // Classification type
        landmarkAttributes[3] = new TigerSchemaElement("Ccode", "String", 56, 58); // Classification sub-code
        landmarkAttributes[4] = new TigerSchemaElement("Geometry", "Geometry", -1, -1);

        physicalAttributes = new TigerSchemaElement[5];
        physicalAttributes[0] = new TigerSchemaElement("TLID", "Integer", 5, 15);
        physicalAttributes[1] = new TigerSchemaElement("Name", "String", 19, 49);
        physicalAttributes[2] = new TigerSchemaElement("Ctype", "String", 55, 56); // Classification type
        physicalAttributes[3] = new TigerSchemaElement("Ccode", "String", 56, 58); // Classification sub-code
        physicalAttributes[4] = new TigerSchemaElement("Geometry", "Geometry", -1, -1);

        // Store the mapping between type names and schema structure for fast access
        schemaMapping = new Hashtable();
        schemaMapping.put(typeNames[0], highwayAttributes);
        schemaMapping.put(typeNames[1], railroadAttributes);
        schemaMapping.put(typeNames[2], otherTransAttributes);
        schemaMapping.put(typeNames[3], hydroAttributes);
        schemaMapping.put(typeNames[4], landmarkAttributes);
        schemaMapping.put(typeNames[5], physicalAttributes);

        // Set up the typeName mapping
        typeNameKey = new Hashtable();

        for (int i = 0; i < typeNames.length; i++) {
            typeNameKey.put(typeNames[i], typeNameKeys[i]);
        }
    }

    /**
     * Returns a list of typeNames supported by this version of the DataStore
     *
     * @return String[]
     */
    public static String[] getTypeNames() {
        return typeNames;
    }

    /**
     * Returns the schema mapping for a given typeName. The schema mapping is used to extract attributes from a
     * TIGER/Line
     *
     * @param typeName String
     *
     * @return TigerSchemaElement[]
     */
    public TigerSchemaElement[] getSchema(String typeName) {
        TigerSchemaElement[] schema = (TigerSchemaElement[])schemaMapping.get(typeName);
        if(null == schema){
            String typeNameString = getTypeSubNameString(typeName);
            schema  =  (TigerSchemaElement[])schemaMapping.get(typeNameString);
        }
        return schema;
    }

    /**
     * Returns a string containing the attribute properties for the given typeName. This is used to construct the
     * AttributeType array for a Feature.
     *
     * @param typeName String
     *
     * @return String
     */
    public String getTypeSpec(String typeName) {
        TigerSchemaElement[] atts = getSchema(typeName);
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < atts.length; i++) {
            buffer.append(atts[i].getAttributeName());
            buffer.append(":");
            buffer.append(atts[i].getClassType());

            if (i != (atts.length - 1)) {
                buffer.append(",");
            }
        }

        return buffer.toString();
    }

    /**
     * getTypeKey
     *
     * @param typeName String
     *
     * @return String
     */
    protected String getTypeKey(String typeName) {
        return (String) typeNameKey.get(typeName);
    }

    /**
     * getTypeSubNameString
     *
     * @param inputName String
     *
     * @return String
     */
    public static String getTypeSubNameString(String inputName) {
        int index = inputName.indexOf("_");

        if (index > 0) {
            return inputName.substring(index + 1);
        } else {
            return inputName;
        }
    }
}
