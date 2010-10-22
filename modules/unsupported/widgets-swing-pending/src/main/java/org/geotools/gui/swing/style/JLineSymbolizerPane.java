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

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.map.MapLayer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;


/**
 * Line symbolizer edition panel
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/style/JLineSymbolizerPane.java $
 */
public class JLineSymbolizerPane extends javax.swing.JPanel implements org.geotools.gui.swing.style.SymbolizerPane<LineSymbolizer> {
  
    private static final ImageIcon ICO_STROKE = IconBundle.getResource().getIcon("16_paint_stroke");
    
    private LineSymbolizer symbol = null;
    private MapLayer layer = null;
    
    /** 
     * Creates new form JLineSymbolizerPanel
     */
    public JLineSymbolizerPane() {
        initComponents();
        init();
    }
    
    
    
    private void init(){
        tab_demo.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                int ligne;
                Point p = e.getPoint();
                ligne = tab_demo.rowAtPoint(p);
                if(ligne<tab_demo.getModel().getRowCount() && ligne>=0)
                setEdited((LineSymbolizer) tab_demo.getModel().getValueAt(ligne, 0));
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
    
    public void setDemoSymbolizers(Map<LineSymbolizer,String> symbols){
        tab_demo.setMap(symbols);        
    }
    
    public Map<LineSymbolizer,String> getDemoSymbolizers(){
        return tab_demo.getMap();
    }
    
    public void setLayer(MapLayer layer){
        this.layer = layer;
        guiStroke.setLayer(layer);
        guiGeom.setLayer(layer);
    }
    
    public MapLayer getLayer(){
        return layer;
    }
 
    public void setEdited(LineSymbolizer sym) {
        symbol = (LineSymbolizer) sym;
        
        if (sym != null) {            
            guiGeom.setGeom(symbol.getGeometryPropertyName());
            guiStroke.setEdited(symbol.getStroke());            
            guiStroke.setLayer(layer);
        }
    }

    public LineSymbolizer getEdited(){ 
        
        if(symbol == null){
            StyleBuilder sb = new StyleBuilder();
            symbol = sb.createLineSymbolizer();
        }        
        apply();        
        return symbol;
    }
    
    public void apply(){
        if(symbol!= null){
            symbol.setGeometryPropertyName(guiGeom.getGeom());
            symbol.setStroke(guiStroke.getEdited());
        }
    }
    
    public void setStyle(Style style){
        FeatureTypeStyle[] sty = style.getFeatureTypeStyles();

        Rule[] rules = sty[0].getRules();
        for (int i = 0; i < rules.length; i++) {
            Rule r = rules[i];

            //on regarde si la regle s'applique au maplayer (s'il n'y a aucun filtre)
            if (r.getFilter() == null) {
                Symbolizer[] symbolizers = r.getSymbolizers();
                for (int j = 0; j < symbolizers.length; j++) {
                    
                if (symbolizers[j] instanceof LineSymbolizer) {
                    setEdited((LineSymbolizer)symbolizers[j]);
                }
                }
            }
        }
    }
    
    public Style getStyle() {
        StyleBuilder sb = new StyleBuilder();               
        Style style = sb.createStyle();
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle( getEdited()));
        return style;
    }

    public JComponent getComponent() {
        return this;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tab_demo = new org.geotools.gui.swing.style.sld.JDemoTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jXTaskPaneContainer1 = new org.jdesktop.swingx.JXTaskPaneContainer();
        guiGeom = new org.geotools.gui.swing.style.sld.JGeomPane();
        jXTaskPane1 = new org.jdesktop.swingx.JXTaskPane();
        guiStroke = new org.geotools.gui.swing.style.sld.JStrokePane();

        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.LOWERED));
        jScrollPane1.setViewportView(tab_demo);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jScrollPane2.setBorder(null);
        jScrollPane2.setViewportBorder(null);

        jXTaskPaneContainer1.add(guiGeom);

        jXTaskPane1.setIcon(ICO_STROKE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/geotools/gui/swing/style/sld/Bundle"); // NOI18N
        jXTaskPane1.setTitle(bundle.getString("stroke")); // NOI18N
        jXTaskPane1.getContentPane().add(guiStroke);

        jXTaskPaneContainer1.add(jXTaskPane1);

        jScrollPane2.setViewportView(jXTaskPaneContainer1);

        add(jScrollPane2, java.awt.BorderLayout.WEST);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.geotools.gui.swing.style.sld.JGeomPane guiGeom;
    private org.geotools.gui.swing.style.sld.JStrokePane guiStroke;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private org.jdesktop.swingx.JXTaskPane jXTaskPane1;
    private org.jdesktop.swingx.JXTaskPaneContainer jXTaskPaneContainer1;
    private org.geotools.gui.swing.style.sld.JDemoTable tab_demo;
    // End of variables declaration//GEN-END:variables


}
