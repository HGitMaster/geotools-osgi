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
package org.geotools.gui.swing.misc;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.coverage.grid.io.UnknownFormat;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.factory.Hints;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.gce.image.WorldImageReader;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;

/**
 * Static class to build GridCoverage
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/misc/GridCoverageFinder.java $
 */
public class GridCoverageFinder {

    
    private static final String GEOTIFF = ".tif";
    private static final String BMP = ".bmp";
    private static final String JPG = ".jpg";
    private static final String JPEG = ".jpeg";
    private static final String PNG = ".png";

    /**
     * return a gridcoverage for Raster file. Use a Map containing key "url"
     * @param params 
     * @return GridCoverage
     */
    public static GridCoverage getGridCoverage(Map params){

        GridCoverage cover = null;

        URL url = (URL) params.get("url");

        if (url != null) {
            String name = url.getFile().toLowerCase();
            File file = DataUtilities.urlToFile(url);

            if (file != null) {
                // try a geotiff gridcoverage
                if (name.endsWith(GEOTIFF)) {

                    try {
                        GeoTiffReader reader = new GeoTiffReader(file, new Hints(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE));
                        cover = (GridCoverage2D) reader.read(null);
                    } catch (DataSourceException ex) {
                        cover = null;
                        ex.printStackTrace();
                    }catch (IOException ex){
                        cover = null;
                        ex.printStackTrace();
                    }
                } 
                // try a world image file
                else if (name.endsWith(BMP) || name.endsWith(JPG) || name.endsWith(JPEG) || name.endsWith(PNG)) {

                    try {
                        WorldImageReader reader = new WorldImageReader(file);
                        cover = (GridCoverage2D) reader.read(null);
                    } catch (DataSourceException ex) {
                        cover = null;
                        ex.printStackTrace();
                    }catch (IOException ex){
                        cover = null;
                        ex.printStackTrace();
                    }
                }
            }

        }

        return cover;
    }
    
    
    
    /**
     * return a gridcoverage for Raster file. Use a Map containing key "url"
     * @param params 
     * @return GridCoverage
     */
    public static GridCoverageReader getGridCoverageReader(Map params){

        URL url = (URL) params.get("url");
        if (url != null) {
            File file = DataUtilities.urlToFile(url);
            if (file != null&&file.exists()&&file.canRead()) {
                // try a geotiff gridcoverage
                GridFormatFinder.scanForPlugins();
                final Format format=GridFormatFinder.findFormat(file);
                if(format!=null&&!(format instanceof UnknownFormat))
                    return ((AbstractGridFormat)format).getReader(file);
            }

        }
        return null;

    }
}
