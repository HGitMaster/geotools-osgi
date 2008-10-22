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

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

/**
 * The example code for the "FirstProject" in the GeoTools wiki.
 * <p>
 * This code matches these examples:
 * <ul>
 * <li><a href="http://docs.codehaus.org/display/GEOTDOC/03+First+Project">First Project</a>
 * <li><a href="http://docs.codehaus.org/display/GEOTDOC/04+How+to+Read+a+Shapefile">How to Read a Shapefile</a>
 * </ul>
 * 
 * @author Jody Garnett
 */
public class FirstProject {
	
public static void main(String[] args) throws Exception {
    System.out.println("Welcome to GeoTools:" + GeoTools.getVersion());
    
    File file = promptShapeFile(args);
    try {
    	// Connection parameters
        Map<String,Serializable>
        	connectParameters = new HashMap<String,Serializable>();
        
        connectParameters.put("url", file.toURI().toURL());
        connectParameters.put("create spatial index", true );
        DataStore dataStore = DataStoreFinder.getDataStore(connectParameters);
        
        // we are now connected
        String[] typeNames = dataStore.getTypeNames();
        String typeName = typeNames[0];

        System.out.println("Reading content " + typeName);

        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource;
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection;
        FeatureIterator<SimpleFeature> iterator;
        
        featureSource = dataStore.getFeatureSource(typeName);
        collection = featureSource.getFeatures();
        iterator = collection.features();
        
        double totalLength=0.0;
        try {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                
                Geometry geometry = (Geometry) feature.getDefaultGeometry();
                totalLength += geometry.getLength();
            }
        }
        finally {
        	if( iterator != null ){
        		// YOU MUST CLOSE THE ITERATOR!
                iterator.close();	
        	}                
        }
        System.out.println("Total Length " + totalLength);
    } catch (Exception ex) {
        ex.printStackTrace();
        System.exit(1);
    }
    System.exit(0);
}

/** 
 * Prompt for File if not provided on the command line.
 * Don't forget the quotes around your path if there are spaces!
 * 
 * @throws FileNotFoundException 
 */
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

		System.out.println("You chose to open this file: " + file.getName());
	} else {
		file = new File(args[0]);
	}
	if (!file.exists()) {
		throw new FileNotFoundException(file.getAbsolutePath());
	}
	return file;
}
}