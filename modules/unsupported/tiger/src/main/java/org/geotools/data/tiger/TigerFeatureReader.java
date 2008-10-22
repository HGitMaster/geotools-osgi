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

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureBuilder;


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
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/tiger/src/main/java/org/geotools/data/tiger/TigerFeatureReader.java $
 * @version 1.0
 */
public class TigerFeatureReader implements  FeatureReader<SimpleFeatureType, SimpleFeature> {
    /** DOCUMENT ME! */
    private TigerAttributeReader reader;

    /** DOCUMENT ME! */
    private String namespace;

    /** DOCUMENT ME! */
    private String typeName;

    /**
     * Creates a new TigerFeatureReader object.
     *
     * @param dir DOCUMENT ME!
     * @param typeName DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public TigerFeatureReader(File dir, String typeName)
        throws IOException {
        // Typename contains both the file prefix and the subtype to be read
        this.namespace = typeName.substring(0, typeName.indexOf("_"));
        this.typeName = typeName.substring(typeName.indexOf("_") + 1);

        // here we deal with case sensetivity. A little clumsy but what can you do... 
        // Try lower case extension first
        File file = new File(dir, this.namespace + ".rt1");
        if (! file.exists()) {
            file = new File(dir, this.namespace + ".RT1");
        }
        if (! file.exists()) {
        	throw new IOException("Can't read RT1 file.");
        }
            
        reader = new TigerAttributeReader(file, this.namespace, this.typeName);
    }

    /**
     * getFeatureType
     *
     * @return FeatureType
     */
    public SimpleFeatureType getFeatureType() {
        return reader.getFeatureType();
    }

    /**
     * next
     *
     * @return Feature
     *
     * @throws IOException
     * @throws IllegalAttributeException
     * @throws NoSuchElementException
     */
    public SimpleFeature next() throws IOException, IllegalAttributeException, NoSuchElementException {
        reader.next();

        SimpleFeatureType type = reader.getFeatureType();
        String fid = reader.getFeatureID();

        int numAtts = reader.getAttributeCount();
        Object[] values = new Object[numAtts];

        for (int i = 0; i < numAtts; i++) {
            values[i] = reader.read(i);
        }
        
        return SimpleFeatureBuilder.build(type, values, fid);
    }

    /**
     * hasNext
     *
     * @return boolean
     *
     * @throws IOException
     */
    public boolean hasNext() throws IOException {
        return reader.hasNext();
    }

    /**
     * close
     *
     * @throws IOException
     */
    public void close() throws IOException {
        reader.close();
        reader = null;
    }
}
