/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gui.swing.contexttree.popup;

import java.awt.Component;

import javax.swing.SpinnerNumberModel;
import javax.swing.tree.TreePath;

import org.geotools.gui.swing.contexttree.ContextTreeNode;
import org.geotools.styling.Rule;

/**
 * Min scale rule item
 *
 * @author  Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/contexttree/popup/RuleMinScaleItem.java $
 */
public class RuleMinScaleItem extends javax.swing.JPanel implements TreePopupItem{
    
    private Rule rule = null;
    final SpinnerNumberModel model = new SpinnerNumberModel(1000, 0, Double.MAX_VALUE, 1000);
    
    
    /** Creates new form RuleMaScaleItem */
    public RuleMinScaleItem() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        gui_scale = new javax.swing.JSpinner();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2));
        setPreferredSize(new java.awt.Dimension(280, 24));

        jLabel1.setText("Min Scale :");

        gui_scale.setModel(model);
        gui_scale.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                gui_scaleStateChanged(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 31, Short.MAX_VALUE)
                .add(gui_scale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                .add(jLabel1)
                .add(gui_scale, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void gui_scaleStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_gui_scaleStateChanged
        if(rule != null){
                    rule.setMinScaleDenominator(model.getNumber().doubleValue());
                }
    }//GEN-LAST:event_gui_scaleStateChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner gui_scale;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables

    public boolean isValid(TreePath[] selection) {
        rule = null;
        if (selection.length == 1) {
            ContextTreeNode node = (ContextTreeNode) selection[0].getLastPathComponent();  
            return (node.getUserObject() instanceof Rule) ;
        }
        return false;
    }

    public Component getComponent(TreePath[] selection) {
        ContextTreeNode node = (ContextTreeNode) selection[0].getLastPathComponent();  
        rule = (Rule) node.getUserObject() ;
        Double d = new Double(rule.getMinScaleDenominator());
        gui_scale.setValue( d );
        return this;
    }
    
}
