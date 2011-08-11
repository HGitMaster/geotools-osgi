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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import javax.swing.JComponent;

import javax.swing.JDialog;
import org.geotools.gui.swing.style.sld.JChannelSelectionPane;
import org.geotools.gui.swing.style.sld.JExpressionPane;
import org.geotools.map.MapLayer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;

/**
 * Raster Sybolizer edition panel
 *
 * @author  Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/style/JRasterSymbolizerPane.java $
 */
public class JRasterSymbolizerPane extends javax.swing.JPanel implements SymbolizerPane<RasterSymbolizer> {

    private RasterSymbolizer symbol = null;
    private MapLayer layer = null;
    private Symbolizer outLine = null;

    /** Creates new form RasterStylePanel
     * @param layer the layer style to edit
     */
    public JRasterSymbolizerPane() {
        initComponents();
        init();
    }

    private void init() {

        guiOpacity.setType(JExpressionPane.EXP_TYPE.NUMBER);

        tabDemo.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                int ligne;
                Point p = e.getPoint();
                ligne = tabDemo.rowAtPoint(p);
                if (ligne < tabDemo.getModel().getRowCount() && ligne >= 0) {
                    setEdited((RasterSymbolizer) tabDemo.getModel().getValueAt(ligne, 0));
                }
            }
        });
    }

    public void setDemoSymbolizers(Map<RasterSymbolizer, String> symbols) {
        tabDemo.setMap(symbols);
    }

    public Map<RasterSymbolizer, String> getDemoSymbolizers() {
        return tabDemo.getMap();
    }

    public void setStyle(Style style) {

        FeatureTypeStyle[] sty = style.getFeatureTypeStyles();

        Rule[] rules = sty[0].getRules();
        for (int i = 0; i < rules.length; i++) {
            Rule r = rules[i];

            //on regarde si la regle s'applique au maplayer (s'il n'y a aucun filtre)
            if (r.getFilter() == null) {
                Symbolizer[] symbolizers = r.getSymbolizers();
                for (int j = 0; j < symbolizers.length; j++) {

                    if (symbolizers[j] instanceof RasterSymbolizer) {
                        setEdited((RasterSymbolizer) symbolizers[j]);
                    }
                }
            }
        }
    }

    public Style getStyle() {
        StyleBuilder sb = new StyleBuilder();

        Style style = sb.createStyle();
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle("GridCoverage",getEdited()));

        return style;
    }

    
    public void setLayer(MapLayer layer) {
        this.layer = layer;
        guiOpacity.setLayer(layer);
        guiGeom.setLayer(layer);
        guiOverLap.setLayer(layer);
        guiContrast.setLayer(layer);
        guiRelief.setLayer(layer);
    }

    public MapLayer getLayer() {
        return layer;
    }
    
    
    public void setEdited(RasterSymbolizer sym) {
        symbol = sym;

        if (sym != null) {
            guiGeom.setGeom(symbol.getGeometryPropertyName());
            guiOpacity.setExpression(symbol.getOpacity());
            guiOverLap.setExpression(symbol.getOverlap());
            guiContrast.setEdited(symbol.getContrastEnhancement());
            guiRelief.setEdited(symbol.getShadedRelief());
                                    
            outLine = symbol.getImageOutline();
            if(outLine == null){
                guinone.setSelected(true);
            }else if(outLine instanceof LineSymbolizer){
                guiLine.setSelected(true);
            }else if(outLine instanceof PolygonSymbolizer){
                guiPolygon.setSelected(true);
            }
            testOutLine();
            
            //handle by a button
            //symbol.getChannelSelection();
            symbol.getColorMap();
            
        }
    }

    public RasterSymbolizer getEdited() {

        if (symbol == null) {
            StyleBuilder sb = new StyleBuilder();
            symbol = sb.createRasterSymbolizer();
        }
        apply();
        return symbol;
    }

    public void apply() {
        if (symbol != null) {
            symbol.setGeometryPropertyName(guiGeom.getGeom());
            symbol.setOpacity(guiOpacity.getExpression());
            symbol.setOverlap(guiOverLap.getExpression());
            symbol.setImageOutline(outLine);
            symbol.setContrastEnhancement(guiContrast.getEdited());
            symbol.setShadedRelief(guiRelief.getEdited());
        }
    }
    
    public JComponent getComponent() {
        return this;
    }

    private void testOutLine(){
        if(guinone.isSelected()){
            butLineSymbolizer.setEnabled(false);
            butPolygonSymbolizer.setEnabled(false);
            outLine = null;
        }else if(guiLine.isSelected()){
            butLineSymbolizer.setEnabled(true);
            butPolygonSymbolizer.setEnabled(false);     
            outLine = new StyleBuilder().createLineSymbolizer();
        }else if(guiPolygon.isSelected()){
            butLineSymbolizer.setEnabled(false);
            butPolygonSymbolizer.setEnabled(true);      
            outLine = new StyleBuilder().createPolygonSymbolizer();
        }
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grpOutline = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabDemo = new org.geotools.gui.swing.style.sld.JDemoTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jXTaskPaneContainer1 = new org.jdesktop.swingx.JXTaskPaneContainer();
        guiGeom = new org.geotools.gui.swing.style.sld.JGeomPane();
        jXTaskPane1 = new org.jdesktop.swingx.JXTaskPane();
        jPanel2 = new javax.swing.JPanel();
        butChannels = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        guiOverLap = new org.geotools.gui.swing.style.sld.JExpressionPane();
        guiOpacity = new org.geotools.gui.swing.style.sld.JExpressionPane();
        jLabel1 = new javax.swing.JLabel();
        jXTaskPane2 = new org.jdesktop.swingx.JXTaskPane();
        guiContrast = new org.geotools.gui.swing.style.sld.JContrastEnhancement();
        jXTaskPane3 = new org.jdesktop.swingx.JXTaskPane();
        guiRelief = new org.geotools.gui.swing.style.sld.JShadedReliefPane();
        jXTaskPane4 = new org.jdesktop.swingx.JXTaskPane();
        jPanel1 = new javax.swing.JPanel();
        guinone = new javax.swing.JRadioButton();
        guiLine = new javax.swing.JRadioButton();
        butLineSymbolizer = new javax.swing.JButton();
        guiPolygon = new javax.swing.JRadioButton();
        butPolygonSymbolizer = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setViewportView(tabDemo);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jScrollPane2.setBorder(null);
        jScrollPane2.setViewportBorder(null);

        jXTaskPaneContainer1.add(guiGeom);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/geotools/gui/swing/style/sld/Bundle"); // NOI18N
        jXTaskPane1.setTitle(bundle.getString("general")); // NOI18N

        jPanel2.setOpaque(false);

        butChannels.setText(bundle.getString("edit")); // NOI18N
        butChannels.setBorderPainted(false);
        butChannels.setPreferredSize(new java.awt.Dimension(79, 22));
        butChannels.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butChannelsActionPerformed(evt);
            }
        });

        jLabel3.setText(bundle.getString("channels")); // NOI18N

        jLabel2.setText(bundle.getString("overlap")); // NOI18N

        java.util.ResourceBundle bundle1 = java.util.ResourceBundle.getBundle("org/geotools/gui/swing/style/Bundle"); // NOI18N
        jLabel1.setText(bundle1.getString("opacity")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(guiOpacity, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(jPanel2Layout.createSequentialGroup()
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(guiOverLap, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(jPanel2Layout.createSequentialGroup()
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(butChannels, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        jPanel2Layout.linkSize(new java.awt.Component[] {jLabel1, jLabel2, jLabel3}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel2Layout.linkSize(new java.awt.Component[] {butChannels, guiOpacity, guiOverLap}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, guiOpacity, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(guiOverLap, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(butChannels, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, Short.MAX_VALUE)
                    .add(jLabel3)))
        );

        jXTaskPane1.getContentPane().add(jPanel2);

        jXTaskPaneContainer1.add(jXTaskPane1);

        jXTaskPane2.setExpanded(false);
        jXTaskPane2.setTitle(bundle.getString("contrast")); // NOI18N

        guiContrast.setOpaque(false);
        jXTaskPane2.getContentPane().add(guiContrast);

        jXTaskPaneContainer1.add(jXTaskPane2);

        jXTaskPane3.setExpanded(false);
        jXTaskPane3.setTitle(bundle.getString("relief")); // NOI18N
        jXTaskPane3.getContentPane().add(guiRelief);

        jXTaskPaneContainer1.add(jXTaskPane3);

        jXTaskPane4.setExpanded(false);
        jXTaskPane4.setTitle(bundle.getString("outline")); // NOI18N

        jPanel1.setOpaque(false);

        grpOutline.add(guinone);
        guinone.setSelected(true);
        guinone.setText(bundle.getString("none")); // NOI18N
        guinone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guinoneActionPerformed(evt);
            }
        });

        grpOutline.add(guiLine);
        guiLine.setText(bundle.getString("line")); // NOI18N
        guiLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiLineActionPerformed(evt);
            }
        });

        butLineSymbolizer.setText(bundle.getString("edit")); // NOI18N
        butLineSymbolizer.setBorderPainted(false);
        butLineSymbolizer.setEnabled(false);
        butLineSymbolizer.setPreferredSize(new java.awt.Dimension(79, 20));
        butLineSymbolizer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butLineSymbolizerActionPerformed(evt);
            }
        });

        grpOutline.add(guiPolygon);
        guiPolygon.setText(bundle.getString("polygon")); // NOI18N
        guiPolygon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                guiPolygonActionPerformed(evt);
            }
        });

        butPolygonSymbolizer.setText(bundle.getString("edit")); // NOI18N
        butPolygonSymbolizer.setBorderPainted(false);
        butPolygonSymbolizer.setEnabled(false);
        butPolygonSymbolizer.setPreferredSize(new java.awt.Dimension(79, 20));
        butPolygonSymbolizer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                butPolygonSymbolizerActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(guinone)
                .add(85, 85, 85))
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(guiLine)
                    .add(guiPolygon))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(butLineSymbolizer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(butPolygonSymbolizer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {guiLine, guiPolygon, guinone}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(guinone)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(butLineSymbolizer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(guiLine))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(butPolygonSymbolizer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(guiPolygon)))
        );

        jXTaskPane4.getContentPane().add(jPanel1);

        jXTaskPaneContainer1.add(jXTaskPane4);

        jScrollPane2.setViewportView(jXTaskPaneContainer1);

        add(jScrollPane2, java.awt.BorderLayout.WEST);
    }// </editor-fold>//GEN-END:initComponents

    private void butPolygonSymbolizerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butPolygonSymbolizerActionPerformed
        JDialog dia = new JDialog();
        dia.setModal(true);
        
        JPolygonSymbolizerPane pane = new JPolygonSymbolizerPane();
        pane.setEdited((PolygonSymbolizer)outLine);
        pane.setLayer(layer);
        
        dia.getContentPane().add(pane);
        
        dia.pack();
        dia.setLocationRelativeTo(butLineSymbolizer);
        dia.setVisible(true);
        
        outLine = pane.getEdited();
    }//GEN-LAST:event_butPolygonSymbolizerActionPerformed

    private void butLineSymbolizerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butLineSymbolizerActionPerformed
        JDialog dia = new JDialog();
        dia.setModal(true);
        
        JLineSymbolizerPane pane = new JLineSymbolizerPane();
        pane.setEdited((LineSymbolizer)outLine);
        pane.setLayer(layer);
        
        dia.getContentPane().add(pane);
        
        dia.pack();
        dia.setLocationRelativeTo(butLineSymbolizer);
        dia.setVisible(true);
        
        outLine = pane.getEdited();
    }//GEN-LAST:event_butLineSymbolizerActionPerformed

    private void guiLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiLineActionPerformed
        testOutLine();
}//GEN-LAST:event_guiLineActionPerformed

    private void guinoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guinoneActionPerformed
       testOutLine();
    }//GEN-LAST:event_guinoneActionPerformed

    private void guiPolygonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_guiPolygonActionPerformed
        testOutLine();
    }//GEN-LAST:event_guiPolygonActionPerformed

    private void butChannelsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_butChannelsActionPerformed
        
        JDialog dia = new JDialog();
        
        JChannelSelectionPane pane = new JChannelSelectionPane();
        pane.setLayer(layer);
        
        if(symbol != null){
            pane.setEdited(symbol.getChannelSelection());
        }
        
        dia.setContentPane(pane);
        dia.pack();
        dia.setLocationRelativeTo(butChannels);
        dia.setModal(true);
        dia.setVisible(true);
        
        if(symbol == null){
            symbol =  new StyleBuilder().createRasterSymbolizer();
        }
        symbol.setChannelSelection(pane.getEdited());        
        
    }//GEN-LAST:event_butChannelsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton butChannels;
    private javax.swing.JButton butLineSymbolizer;
    private javax.swing.JButton butPolygonSymbolizer;
    private javax.swing.ButtonGroup grpOutline;
    private org.geotools.gui.swing.style.sld.JContrastEnhancement guiContrast;
    private org.geotools.gui.swing.style.sld.JGeomPane guiGeom;
    private javax.swing.JRadioButton guiLine;
    private org.geotools.gui.swing.style.sld.JExpressionPane guiOpacity;
    private org.geotools.gui.swing.style.sld.JExpressionPane guiOverLap;
    private javax.swing.JRadioButton guiPolygon;
    private org.geotools.gui.swing.style.sld.JShadedReliefPane guiRelief;
    private javax.swing.JRadioButton guinone;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private org.jdesktop.swingx.JXTaskPane jXTaskPane1;
    private org.jdesktop.swingx.JXTaskPane jXTaskPane2;
    private org.jdesktop.swingx.JXTaskPane jXTaskPane3;
    private org.jdesktop.swingx.JXTaskPane jXTaskPane4;
    private org.jdesktop.swingx.JXTaskPaneContainer jXTaskPaneContainer1;
    private org.geotools.gui.swing.style.sld.JDemoTable tabDemo;
    // End of variables declaration//GEN-END:variables
    
}
