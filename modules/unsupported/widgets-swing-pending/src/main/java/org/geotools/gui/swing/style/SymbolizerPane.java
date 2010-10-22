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


import java.util.Map;
import org.geotools.styling.Style;
import org.geotools.styling.Symbolizer;

/**
 * Symbolizer panel interface
 * 
 * @param T 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/style/SymbolizerPane.java $
 */
public interface SymbolizerPane<T extends Symbolizer> extends StyleElementEditor<T>{
          
        
    /**
     * @return return a Style with only one rule and the symbolizer
     */
    public Style getStyle();
    
    /**
     * parse the target style, this will use the first correct symbolizer found in the style.
     * @param style
     */
    public void setStyle(Style style);
            
    /**
     * a exemple list that will be used 
     * @param symbols
     */
    public void setDemoSymbolizers(Map<T,String> symbols);
    
    /**
     * 
     * @return the exemple list used
     */
    public Map<T,String> getDemoSymbolizers();
    
}
