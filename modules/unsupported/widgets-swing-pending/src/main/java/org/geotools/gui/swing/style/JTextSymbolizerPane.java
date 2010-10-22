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
import org.geotools.gui.swing.style.sld.JExpressionPane;
import org.geotools.map.MapLayer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;

/**
 * Text Symbolizer edition panel
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/style/JTextSymbolizerPane.java $
 */
public class JTextSymbolizerPane extends javax.swing.JPanel implements SymbolizerPane<TextSymbolizer> {

    private static final ImageIcon ICO_FILL = IconBundle.getResource().getIcon("16_paint_fill");
    private static final ImageIcon ICO_HALO = IconBundle.getResource().getIcon("16_paint_stroke");
    private static final ImageIcon ICO_FONT = IconBundle.getResource().getIcon("16_paint_font");
    private static final ImageIcon ICO_PLACEMENT = IconBundle.getResource().getIcon("16_paint_placement");
    
    
    private TextSymbolizer symbol = null;
    private MapLayer layer = null;

    /** Creates new form JTextSymbolizer */
    public JTextSymbolizerPane() {
        initComponents();
        init();
    }

    private void init() {
        guiLabel.setType(JExpressionPane.EXP_TYPE.OTHER);
        guiPriority.setType(JExpressionPane.EXP_TYPE.NUMBER);
        jsp.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                int ligne;
                Point p = e.getPoint();
                ligne = tab_demo.rowAtPoint(p);
                if (ligne < tab_demo.getModel().getRowCount() && ligne >= 0) {
                    setEdited((TextSymbolizer) tab_demo.getModel().getValueAt(ligne, 0));
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

    public void setDemoSymbolizers(Map<TextSymbolizer, String> symbols) {
        tab_demo.setMap(symbols);
    }

    public Map<TextSymbolizer, String> getDemoSymbolizers() {
        return tab_demo.getMap();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jsp = new javax.swing.JScrollPane();
        tab_demo = new org.geotools.gui.swing.style.sld.JDemoTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        jXTaskPaneContainer1 = new org.jdesktop.swingx.JXTaskPaneContainer();
        guiGeom = new org.geotools.gui.swing.style.sld.JGeomPane();
        jXTaskPane1 = new org.jdesktop.swingx.JXTaskPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        guiLabel = new org.geotools.gui.swing.style.sld.JExpressionPane();
        guiPriority = new org.geotools.gui.swing.style.sld.JExpressionPane();
        jXTaskPane2 = new org.jdesktop.swingx.JXTaskPane();
        guiFill = new org.geotools.gui.swing.style.sld.JFillPane();
        jXTaskPane3 = new org.jdesktop.swingx.JXTaskPane();
        guiHalo = new org.geotools.gui.swing.style.sld.JHaloPane();
        jXTaskPane4 = new org.jdesktop.swingx.JXTaskPane();
        guiFonts = new org.geotools.gui.swing.style.sld.JFontTable();
        jXTaskPane5 = new org.jdesktop.swingx.JXTaskPane();
        guiPlacement = new org.geotools.gui.swing.style.sld.JLabelPlacementPane();

        setLayout(new java.awt.BorderLayout());

        jsp.setViewportView(tab_demo);

        add(jsp, java.awt.BorderLayout.CENTER);

        jScrollPane1.setBorder(null);
        jScrollPane1.setViewportBorder(null);

        jXTaskPaneContainer1.add(guiGeom);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/geotools/gui/swing/style/sld/Bundle"); // NOI18N
        jXTaskPane1.setTitle(bundle.getString("general")); // NOI18N

        jPanel1.setOpaque(false);

        java.util.ResourceBundle bundle1 = java.util.ResourceBundle.getBundle("org/geotools/gui/swing/style/Bundle"); // NOI18N
        jLabel1.setText(bundle1.getString("label")); // NOI18N

        jLabel2.setText(bundle.getString("priority")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(guiLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
            .add(jPanel1Layout.createSequentialGroup()
                .add(jLabel2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(guiPriority, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {jLabel1, jLabel2}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(guiLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(5, 5, 5)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(jLabel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(guiPriority, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        jXTaskPane1.getContentPane().add(jPanel1);

        jXTaskPaneContainer1.add(jXTaskPane1);

        jXTaskPane2.setExpanded(false);
        jXTaskPane2.setIcon(ICO_FILL);
        jXTaskPane2.setTitle(bundle.getString("fill")); // NOI18N
        jXTaskPane2.getContentPane().add(guiFill);

        jXTaskPaneContainer1.add(jXTaskPane2);

        jXTaskPane3.setExpanded(false);
        jXTaskPane3.setIcon(ICO_HALO);
        jXTaskPane3.setTitle(bundle.getString("halo")); // NOI18N
        jXTaskPane3.getContentPane().add(guiHalo);

        jXTaskPaneContainer1.add(jXTaskPane3);

        jXTaskPane4.setExpanded(false);
        jXTaskPane4.setIcon(ICO_FONT);
        jXTaskPane4.setTitle(bundle.getString("fonts")); // NOI18N
        jXTaskPane4.getContentPane().add(guiFonts);

        jXTaskPaneContainer1.add(jXTaskPane4);

        jXTaskPane5.setExpanded(false);
        jXTaskPane5.setIcon(ICO_PLACEMENT);
        jXTaskPane5.setTitle(bundle.getString("placement")); // NOI18N
        jXTaskPane5.getContentPane().add(guiPlacement);

        jXTaskPaneContainer1.add(jXTaskPane5);

        jScrollPane1.setViewportView(jXTaskPaneContainer1);

        add(jScrollPane1, java.awt.BorderLayout.WEST);
    }// </editor-fold>//GEN-END:initComponents
    public void setLayer(MapLayer layer) {
        this.layer = layer;
        guiFill.setLayer(layer);
        guiHalo.setLayer(layer);
        guiFonts.setLayer(layer);
        guiGeom.setLayer(layer);
        guiLabel.setLayer(layer);
        guiPlacement.setLayer(layer);
    }

    public MapLayer getLayer() {
        return layer;
    }

    public TextSymbolizer getEdited() {

        if (symbol == null) {
            symbol = new StyleBuilder().createTextSymbolizer();
        }

        apply();
        return symbol;
    }

    public void setEdited(TextSymbolizer sym) {
        symbol = (TextSymbolizer) sym;

        if (sym != null) {

            guiFill.setEdited(symbol.getFill());
            guiLabel.setExpression(symbol.getLabel());
            guiGeom.setGeom(symbol.getGeometryPropertyName());
            guiFonts.setEdited(symbol.getFonts());
            guiHalo.setEdited(symbol.getHalo());
            guiPriority.setExpression(symbol.getPriority());
            guiPlacement.setEdited(symbol.getPlacement());

            //dont know how to handle that option map
            // lack informations
            //symbol.getOptions();               
        }
    }

    public Style getStyle() {
        StyleBuilder sb = new StyleBuilder();
        Style style = sb.createStyle();
        style.addFeatureTypeStyle(sb.createFeatureTypeStyle(getEdited()));
        return style;
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

                    if (symbolizers[j] instanceof TextSymbolizer) {
                        setEdited((TextSymbolizer) symbolizers[j]);
                    }
                }
            }
        }
    }

    public void apply() {
        if (symbol != null) {
            symbol.setFill(guiFill.getEdited());
            symbol.setLabel(guiLabel.getExpression());
            symbol.setGeometryPropertyName(guiGeom.getGeom());
            symbol.setFonts(guiFonts.getEdited());
            symbol.setHalo(guiHalo.getEdited());
            symbol.setPlacement(guiPlacement.getEdited());
        }
    }

    public JComponent getComponent() {
        return this;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.geotools.gui.swing.style.sld.JFillPane guiFill;
    private org.geotools.gui.swing.style.sld.JFontTable guiFonts;
    private org.geotools.gui.swing.style.sld.JGeomPane guiGeom;
    private org.geotools.gui.swing.style.sld.JHaloPane guiHalo;
    private org.geotools.gui.swing.style.sld.JExpressionPane guiLabel;
    private org.geotools.gui.swing.style.sld.JLabelPlacementPane guiPlacement;
    private org.geotools.gui.swing.style.sld.JExpressionPane guiPriority;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private org.jdesktop.swingx.JXTaskPane jXTaskPane1;
    private org.jdesktop.swingx.JXTaskPane jXTaskPane2;
    private org.jdesktop.swingx.JXTaskPane jXTaskPane3;
    private org.jdesktop.swingx.JXTaskPane jXTaskPane4;
    private org.jdesktop.swingx.JXTaskPane jXTaskPane5;
    private org.jdesktop.swingx.JXTaskPaneContainer jXTaskPaneContainer1;
    private javax.swing.JScrollPane jsp;
    private org.geotools.gui.swing.style.sld.JDemoTable tab_demo;
    // End of variables declaration//GEN-END:variables
}
