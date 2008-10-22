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

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import org.geotools.gui.swing.misc.Render.RandomStyleFactory;
import org.geotools.gui.swing.propertyedit.JPropertyDialog;
import org.geotools.gui.swing.propertyedit.LayerStylePropertyPanel;
import org.geotools.gui.swing.propertyedit.PropertyPane;
import org.geotools.map.MapLayer;
import org.geotools.styling.Symbolizer;

/**
 * Component used to present style glyph
 * 
 * @author Johann Sorel
 */
public final class StyleComponent extends org.geotools.gui.swing.contexttree.renderer.RenderAndEditComponent {
    
    private RandomStyleFactory RANDOM_STYLE_FACTORY = new RandomStyleFactory();
    private MapLayer layer = null;
    private Symbolizer symbol = null;
    
    
    /**
     * create new instance
     */
    public StyleComponent(){
        super();   
        init();
    }
    
    private void init(){
        
        addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                if( layer != null ){
                    ArrayList<PropertyPane> lst = new ArrayList<PropertyPane>();
                    lst.add(new LayerStylePropertyPanel());
                    JPropertyDialog.showDialog(lst, layer);
                }
            }

            public void mousePressed(MouseEvent e) {
            }

            public void mouseReleased(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseExited(MouseEvent e) {
            }
        });     
        
    }       
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);      
        
        if(RANDOM_STYLE_FACTORY != null){
        if( layer != null ){
            g.drawImage( RANDOM_STYLE_FACTORY.createGlyph(layer), (getWidth()-16)/2,(getHeight()-16)/2,this);
        }
        else if( symbol != null ){
            g.drawImage( RANDOM_STYLE_FACTORY.createGlyph(symbol), (getWidth()-16)/2,(getHeight()-16)/2,this);
        }
        }
    }

    @Override
    public void parse(Object obj) {
        layer = null;
        symbol = null;
        
        if(obj instanceof MapLayer)
            layer = (MapLayer) obj ;
        else if(obj instanceof Symbolizer){
            symbol = (Symbolizer) obj;
        }
    }

    @Override
    public Object getValue() {
        return null;
    }
    
}
