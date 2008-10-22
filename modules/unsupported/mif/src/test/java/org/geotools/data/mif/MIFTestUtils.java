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
package org.geotools.data.mif;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.geotools.feature.AttributeTypes;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.ExpressionBuilder;
import org.geotools.filter.parser.ParseException;
import org.geotools.test.TestData;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;


/**
 * DOCUMENT ME!
 *
 * @author Luca S. Percich, AMA-MI
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/mif/src/test/java/org/geotools/data/mif/MIFTestUtils.java $
 */
public class MIFTestUtils {
    public static final int SRID = 26591;
    public static final GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(
                PrecisionModel.FLOATING_SINGLE), SRID);
    public static final String coordsysClause = "Earth Projection 8, 87, \"m\", 9, 0, 0.9996, 1500000, 0 Bounds (-6746230.6469, -9998287.38389) (9746230.6469, 9998287.38389)";

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private static File getDataPath() {
        try {
            return TestData.file(MIFTestUtils.class, null);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param inMif DOCUMENT ME!
     * @param outMif DOCUMENT ME!
     *
     * @throws IOException
     */
    public static void copyMif(String inMif, String outMif)
        throws IOException {
        File path = getDataPath();

        copyFileUsingChannels(MIFFile.getFileHandler(path, inMif, ".mif", true),
            new File(path, outMif + ".mif"));
        copyFileUsingChannels(MIFFile.getFileHandler(path, inMif, ".mid", true),
            new File(path, outMif + ".mid"));
    }

    /**
     * DOCUMENT ME!
     *
     * @param in DOCUMENT ME!
     * @param out DOCUMENT ME!
     *
     * @throws IOException
     */
    public static void copyFileUsingChannels(File in, File out)
        throws IOException {
        FileChannel sourceChannel = new FileInputStream(in).getChannel();
        FileChannel destinationChannel = new FileOutputStream(out).getChannel();
        destinationChannel.transferFrom( sourceChannel, 0, sourceChannel.size() );
        sourceChannel.close();
        destinationChannel.close();
    }

    /**
     * DOCUMENT ME!
     *
     * @param mifName MIF file to be deleted (no extension)
     */
    public static void safeDeleteMif(String mifName) {
        File f;

        try {
            f = MIFFile.getFileHandler(getDataPath(), mifName, ".mif", false);

            if (f.exists()) {
                f.delete();
            }

            f = MIFFile.getFileHandler(getDataPath(), mifName, ".mid", false);

            if (f.exists()) {
                f.delete();
            }
        } catch (FileNotFoundException e) {
        }
    }

    /**
     * Deletes temporary files in test-data
     */
    public static void cleanFiles() {
        safeDeleteMif("grafo_new");
        safeDeleteMif("grafo_out");
        safeDeleteMif("mixed_wri");
        safeDeleteMif("grafo_append");
        safeDeleteMif("newschema");
        safeDeleteMif("mixed_fs");
    }

    /**
     * DOCUMENT ME!
     *
     * @param f DOCUMENT ME!
     * @param logger DOCUMENT ME!
     */
    public static void printFeature(SimpleFeature f, Logger logger) {
        print(f.toString(), logger);
    }

    /**
     * Utility print method
     *
     * @param msg DOCUMENT ME!
     * @param logger DOCUMENT ME!
     */
    public static void print(String msg, Logger logger) {
        logger.fine(msg);
    }

    /**
     * DOCUMENT ME!
     *
     * @param ft DOCUMENT ME!
     * @param logger DOCUMENT ME!
     */
    public static void printSchema(SimpleFeatureType ft, Logger logger) {
        print(ft.getTypeName(), logger);

        List<AttributeDescriptor> attrs = ft.getAttributeDescriptors();

        for (int i = 0; i < attrs.size(); i++) {
            print("   " + attrs.get(i).getLocalName() + " - "
                + attrs.get(i).getType().getBinding().toString() + "("
                + AttributeTypes.getFieldLength(attrs.get(i), 0) + ")", logger);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param dbtype DOCUMENT ME!
     * @param path DOCUMENT ME!
     * @param uri DOCUMENT ME!
     * @param geomType DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected static HashMap getParams(String dbtype, String path, URI uri,
        String geomType) {
        HashMap params = new HashMap();

        params.put("dbtype", dbtype);
        params.put("path", path);

        if (uri != null) {
            params.put("namespace", uri);
        }

        params.put(MIFDataStore.PARAM_FIELDCASE, "upper");
        params.put(MIFDataStore.PARAM_GEOMNAME, "the_geom");
        params.put(MIFDataStore.PARAM_GEOMTYPE, geomType);

        // params.put(MIFDataStore.PARAM_GEOMFACTORY, MIFTestUtils.geomFactory);
        params.put(MIFDataStore.PARAM_SRID, new Integer(SRID));

        params.put(MIFDataStore.HCLAUSE_COORDSYS, MIFTestUtils.coordsysClause);

        return params;
    }

    /**
     * DOCUMENT ME!
     *
     * @param dbtype DOCUMENT ME!
     * @param path DOCUMENT ME!
     * @param uri DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected static HashMap getParams(String dbtype, String path, URI uri) {
        return getParams(dbtype, path, uri, "untyped");
    }

    /**
     * Duplicates a given feature type
     *
     * @param inFeatureType
     * @param typeName
     *
     *
     * @throws SchemaException
     */
    protected static SimpleFeatureType duplicateSchema(SimpleFeatureType inFeatureType,
        String typeName) throws SchemaException {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName(typeName);

        for (int i = 0; i < inFeatureType.getAttributeCount(); i++) {
            builder.add(inFeatureType.getDescriptor(i));
        }

        return builder.buildFeatureType();
    }

    /**
     * DOCUMENT ME!
     *
     * @param expression DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected static Filter parseFilter(String expression) {
        try {
            return (Filter) ExpressionBuilder.parse(expression);
        } catch (ParseException e) {
            return Filter.EXCLUDE;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param fileName DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected static String fileName(String fileName) {
        if (fileName.equals("")) {
            return getDataPath().getAbsolutePath();
        }

        File file = new File(getDataPath(), fileName);

        return file.getAbsolutePath();
    }
}
