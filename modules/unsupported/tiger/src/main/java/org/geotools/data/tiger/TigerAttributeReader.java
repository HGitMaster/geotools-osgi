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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.data.AttributeReader;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.feature.SchemaException;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.simple.SimpleFeatureType;


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
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/tiger/src/main/java/org/geotools/data/tiger/TigerAttributeReader.java $
 * @version 1.0
 */
public class TigerAttributeReader implements AttributeReader {
    /** DOCUMENT ME! */
    private BufferedReader rt1Reader;

    /** DOCUMENT ME! */
    private BufferedReader rt2Reader;

    /** DOCUMENT ME! */
    private SimpleFeatureType featureType;

    /** DOCUMENT ME! */
    private String currentLine;

    /** DOCUMENT ME! */
    private String nextLine;

    /** DOCUMENT ME! */
    private TigerSchemaManager schemaManager;

    /** DOCUMENT ME! */
    private TigerGeometryAdapter geometryAdapter;

    /** DOCUMENT ME! */
    private String typeName;

    /** DOCUMENT ME! */
    private String namespace;

    /** DOCUMENT ME! */
    private String typeKey;

    /**
     * TigerAttributeReader
     *
     * @param file File
     * @param namespace String
     * @param typeName String
     *
     * @throws IOException
     * @throws DataSourceException DOCUMENT ME!
     */
    public TigerAttributeReader(File file, String namespace, String typeName)
        throws IOException {
        this.schemaManager = new TigerSchemaManager();
        this.typeName = typeName;
        this.namespace = namespace;
        this.typeKey = schemaManager.getTypeKey(typeName);

        String fileName1 = file.getPath();
        String fileName = fileName1.substring(0, fileName1.lastIndexOf("1")) + "2";

        rt1Reader = new BufferedReader(new FileReader(file)); // RT1 records
        rt2Reader = new BufferedReader(new FileReader(new File(fileName))); // RT2 records

        geometryAdapter = new TigerGeometryAdapter(rt2Reader);

        String typeSpec = schemaManager.getTypeSpec(typeName);

        try {
            featureType = DataUtilities.createType(this.typeName, typeSpec);
        } catch (SchemaException e) {
            throw new DataSourceException(this.typeName + " schema not available", e);
        }
    }

    /**
     * getFeatureType
     *
     * @return FeatureType
     */
    public SimpleFeatureType getFeatureType() {
        return featureType;
    }

    /**
     * getTypeName
     *
     * @param file File
     *
     * @return String
     */
    private static String getTypeName(File file) {
        String name = file.getName();
        int split = name.lastIndexOf('.');

        return (split == -1) ? name : name.substring(split);
    }

    /**
     * getNamespace
     *
     * @param file File
     *
     * @return String
     */
    private static String getNamespace(File file) {
        String name = file.getName();
        int split = name.lastIndexOf('.');

        return (split == -1) ? name : name.substring(split);
    }

    /**
     * getAttributeCount
     *
     * @return int
     */
    public int getAttributeCount() {
        return featureType.getAttributeCount();
    }

    /**
     * getAttributeType
     *
     * @param index int
     *
     * @return AttributeType
     *
     * @throws ArrayIndexOutOfBoundsException
     */
    public AttributeDescriptor getAttributeType(int index) throws ArrayIndexOutOfBoundsException {
        return featureType.getDescriptor(index);
    }

    /**
     * close
     *
     * @throws IOException
     */
    public void close() throws IOException {
        rt1Reader.close();
        rt1Reader = null;

        // Close the shape points reader as well
        rt2Reader.close();
        rt2Reader = null;
    }

    /**
     * hasNext
     *
     * @return boolean
     *
     * @throws IOException
     */
    public boolean hasNext() throws IOException {
        if (nextLine != null) {
            return true;
        }

        while (true) {
            // We keep reading from the file until we either hit the end (null is returned) or we find a record with
            // the correct key type
            nextLine = rt1Reader.readLine();

            if (nextLine == null) {
                break;
            }

            if (isKeyTypeRecord()) {
                break;
            }
        }

        return nextLine != null;
    }

    /**
     * next
     *
     * @throws IOException
     * @throws NoSuchElementException DOCUMENT ME!
     */
    public void next() throws IOException {
        if (hasNext()) {
            currentLine = nextLine;
            nextLine = null;
        } else {
            throw new NoSuchElementException();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private boolean isKeyTypeRecord() {
        if (nextLine == null) {
            return false;
        } else {
            return nextLine.substring(55, 56).equals(typeKey);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param index DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     * @throws ArrayIndexOutOfBoundsException DOCUMENT ME!
     */
    public Object read(int index) throws IOException, ArrayIndexOutOfBoundsException {
        if (currentLine == null) {
            throw new IOException("No data available.");
        }

        TigerSchemaElement[] elements = schemaManager.getSchema(typeName);

        if ((index < 0) || (index >= elements.length)) {
            throw new ArrayIndexOutOfBoundsException();
        }

        // Process geometry
        if (elements[index].getClassType() == "Geometry") {
            Object geom = geometryAdapter.deSerialize(getFeatureID(), currentLine);

            return geom;//featureType.getAttribute(index).getType().parse(geom);
        } else {
            String elemData = currentLine.substring(elements[index].getStartPos(), elements[index].getEndPos()).trim();

            // Always return a null if the string is 0 length
            if (elemData.length() == 0) {
                return null;
            }

            if (elements[index].getClassType() == "String") {
                return elemData.toString();//featureType.getAttributeType(index).parse(elemData);
            } else if (elements[index].getClassType() == "Integer") {
                Integer ival = new Integer(elemData.trim());

                return ival;//featureType.getAttributeType(index).parse(ival);
            } else {
                return elemData;//featureType.getAttributeType(index).parse(elemData);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getFeatureID() {
        if (currentLine == null) {
            return null;
        }

        return currentLine.substring(5, 15).trim();
    }
}
