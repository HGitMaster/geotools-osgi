/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This file is hereby placed into the Public Domain. This means anyone is
 *    free to do whatever they wish with this file. Use it well and enjoy!
 */
package org.geotools.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class Shp2Shp {
	public static void main(String[] args) throws Exception {
        System.out.println("Welcome to GeoTools:" + GeoTools.getVersion());

        File file = promptShapeFile(args);

        Map<String, Object> connect = new HashMap<String, Object>();
        connect.put("url", file.toURI().toURL());

        DataStore dataStore = DataStoreFinder.getDataStore(connect);
        String[] typeNames = dataStore.getTypeNames();
        String typeName = typeNames[0];

        System.out.println("Reading content " + typeName);
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource(typeName);

        SimpleFeatureType simpleFeatureType = featureSource.getSchema();
        System.out.println("Header: " + DataUtilities.spec( simpleFeatureType ));

        DefaultQuery query = new DefaultQuery();
        query.setTypeName(typeName);

        CoordinateReferenceSystem prj = simpleFeatureType.getCoordinateReferenceSystem();
        if (prj == null) {
            prj = getCoordinateReferenceSystem("No projection fround for "
                    + file + " please choose one:");
            query.setCoordinateSystem(prj);
        }

        CoordinateReferenceSystem crs = getCoordinateReferenceSystem("Project "
                + file + " to:");
        query.setCoordinateSystemReproject(crs);

        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = featureSource.getFeatures(query);
        File newFile = getNewShapeFile(file);

        DataStoreFactorySpi factory = new ShapefileDataStoreFactory();

        Map<String, Serializable> create = new HashMap<String,Serializable>();
        create.put("url", newFile.toURI().toURL());
        create.put("create spatial index", Boolean.TRUE);
        DataStore newDataStore = factory.createNewDataStore(create);

        newDataStore.createSchema(collection.getSchema());
        Transaction transaction = new DefaultTransaction("Reproject");
        FeatureStore<SimpleFeatureType, SimpleFeature> featureStore;
        featureStore = (FeatureStore<SimpleFeatureType, SimpleFeature>) newDataStore
                .getFeatureSource(typeName);
        featureStore.setTransaction(transaction);
        try {
            featureStore.addFeatures(collection);
            transaction.commit();
        } catch (Exception problem) {
            problem.printStackTrace();
            transaction.rollback();
        }
        finally {
            transaction.close();
        }
        System.exit(0);
    }

    private static File getNewShapeFile(File file) {
        String path = file.getAbsolutePath();
        String newPath = path.substring(0, path.length() - 4) + "2.shp";

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save reprojected shapefile");
        chooser.setSelectedFile(new File(newPath));
        chooser.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getPath().endsWith("shp")
                        || f.getPath().endsWith("SHP");
            }

            public String getDescription() {
                return "Shapefiles";
            }
        });
        int returnVal = chooser.showSaveDialog(null);

        if (returnVal != JFileChooser.APPROVE_OPTION) {
            System.exit(0);
        }
        File newFile = chooser.getSelectedFile();
        if (newFile.equals(file)) {
            System.out.println("Cannot replace " + file);
            System.exit(0);
        }
        return newFile;
    }

    private static CoordinateReferenceSystem getCoordinateReferenceSystem(
            String message) throws Exception {
        Set<String> codes = CRS.getSupportedCodes("EPSG");
        String selected = (String) JOptionPane.showInputDialog(null, message,
                "Choose a Projection", JOptionPane.QUESTION_MESSAGE, null,
                codes.toArray(), "4326");
        if (selected == null) {
            System.exit(0);
        }
        return CRS.decode("EPSG:" + selected);
    }

    private static File promptShapeFile(String[] args)
            throws FileNotFoundException {
        File file;
        if (args.length == 0) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Open Shapefile for Reprojection");
            chooser.setFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    return f.isDirectory() || f.getPath().endsWith("shp")
                            || f.getPath().endsWith("SHP");
                }

                public String getDescription() {
                    return "Shapefiles";
                }
            });
            int returnVal = chooser.showOpenDialog(null);

            if (returnVal != JFileChooser.APPROVE_OPTION) {
                System.exit(0);
            }
            file = chooser.getSelectedFile();

            System.out
                    .println("You chose to open this file: " + file.getName());
        } else {
            file = new File(args[0]);
        }
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        return file;
    }
}
