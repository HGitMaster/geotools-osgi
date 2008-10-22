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
package org.geotools.gui.swing.contexttree.column;

import java.util.ResourceBundle;

/**
 * Bundle class
 *
 * @author Johann Sorel
 */
class BUNDLE {

    private static ResourceBundle bundle = ResourceBundle.getBundle("org/geotools/gui/swing/contexttree/column/Bundle");

    static String getString(String key){
        return bundle.getString(key);
    }

}
