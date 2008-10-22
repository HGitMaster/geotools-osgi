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
package org.geotools.gui.swing.style;

import java.awt.Component;
import org.geotools.map.MapLayer;

/**
 * Style element editor
 * 
 * @param T : style element class edited
 * @author Johann Sorel
 */
public interface StyleElementEditor<T> {

    
    /**
     * Style element nearly always have an Expression field
     * the layer is used to fill the possible attribut in the expression editor
     * @param layer
     */
    public void setLayer(MapLayer layer);
    
    /**
     * Layer used for expression edition in the style element
     * @return MapLayer
     */
    public MapLayer getLayer();
    
    /**
     * the the edited object
     * @param target : object to edit
     */
    public void setEdited(T target);
    
    /**
     * return the edited object if there is one.
     * Id no edited object has been set this will create a new one.
     * @return T object
     */
    public T getEdited();
    
    /**
     * apply the modification on the edited object if there is one.
     * If there is no edited object this method has no effect.
     */
    public void apply();
    
    /**
     * 
     * @return return the Component for style element edition
     */
    public Component getComponent();
}
