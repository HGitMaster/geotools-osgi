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
package org.geotools.gui.swing.misc.filter;

import java.io.File;
import java.util.ResourceBundle;
import javax.swing.filechooser.FileFilter;
import org.geotools.util.ResourceInternationalString;
import org.opengis.util.InternationalString;

/**
 * file filter factory
 *  
 * @author Johann Sorel
 */
public class FileFilterFactory {

    public static FileFilter FolderFilter = new FileFilter(){
    
    public boolean accept(File fichier) {        
        return fichier.isDirectory();
    }
    
    public String getDescription() {
        return ResourceBundle.getBundle("org/geotools/gui/swing/misv/filter/Bundle").getString("folder");
    }
    
};
    
    public static enum FORMAT {

        ACCESS_DATABASE("access", "mdb"),
        AUTOCAD_DWG("dwg", "dwg"),
        COMMA_SEPARATED_VALUES("csv", "csv"),
        ERDAS_IMAGE("erdas_img", "img"),
        ERMAPPER_COMPRESSED_WAVELETS("ecw", "ecw"),
        ESRI_SHAPEFILE("shapefile", "shp"),
        ESRI_ASCII_GRID("asc", "asc"),
        GEOTIFF("geotiff", "tif", "tiff"),
        GEOGRAPHY_MARKUP_LANGUAGE("gml", "gml"),
        KEYHOLE_MARKUP_LANGUAGE("kml", "kml"),
        KEYHOLE_MARKUP_LANGUAGE_ZIPPED("kmz", "kmz"),
        JOINT_PHOTOGRAPHIC_EXPERTS_GROUP("jpg", "jpg", "jpeg","gif"),
        JPEG_2000("jpg2", "jp2", "j2k"),
        MAPINFO_EXCHANGE("mif", "mif"),
        MAPINFO_TAB("tab", "tab"),
        MICROSTATION_DGN("microstation_dgn", "dgn"),
        PORTABLE_NETWORK_GRAPHICS("png", "png"),
        SCALABLE_VECTOR_GRAPHICS("svg", "svg"),
        STYLE_LAYER_DESCRIPTOR("sld", "sld"),
        TIFF("tiff", "tif", "tiff"),
        VISUALDEM("vdem", "dem"),
        WEBMAPCONTEXT("wmc", "wmc"),
        WORLD_IMAGE("world_image", "jpg", "jpeg", "bmp", "png");
        final InternationalString desc;
        final String[] ends;

        FORMAT(String i18n, String... ends) {
            this.desc = new ResourceInternationalString("org.geotools.gui.swing.misc.filter.Bundle", i18n);
            this.ends = new String[ends.length];
            String dot = ".";
            for (int i = 0; i < ends.length; i++) {
                this.ends[i] = dot + ends[i];
            }
        }

        public InternationalString getDescription() {
            return desc;
        }

        public String[] getFileEnds() {
            return ends.clone();
        }
    }

    
    public static FileFilter createFileFilter(final FORMAT format) {

        if (format == null) {
            throw new NullPointerException();
        }

        FileFilter ff = new FileFilter() {

            private String desc;

            {
                StringBuffer buff = new StringBuffer();
                
                buff.append(format.getDescription().toString());
                buff.append(" (");
                
                String[] ends = format.getFileEnds();
                
                buff.append('*');
                buff.append(ends[0]);
                
                for(int i=1;i<ends.length;i++){
                    String end = ends[i];
                    buff.append(',');
                    buff.append('*');
                    buff.append(end);
                }
                buff.append(')');
                
                desc = buff.toString();
            }

            public boolean accept(File pathname) {
                String[] ends = format.getFileEnds();

                String nom = pathname.getName();

                if (pathname.isDirectory()) {
                    return true;
                }

                for (int i = 0,  n = ends.length; i < n; i++) {
                    if (nom.toLowerCase().endsWith(ends[i])) {
                        return true;
                    }
                }

                return false;
            }

            public String getDescription() {
                return desc;
            }
        };


        return ff;
    }
}
