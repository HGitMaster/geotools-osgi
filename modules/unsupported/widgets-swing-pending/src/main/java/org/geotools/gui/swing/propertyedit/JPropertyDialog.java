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
package org.geotools.gui.swing.propertyedit;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.geotools.gui.swing.icon.IconBundle;


/**
 * Property panel
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/propertyedit/JPropertyDialog.java $
 */
public class JPropertyDialog extends JDialog{
        
    private JButton apply = new JButton(BUNDLE.getString("apply"));
    private JButton revert = new JButton(BUNDLE.getString("revert"));
    private JButton close = new JButton(BUNDLE.getString("close"));
    
    private JTabbedPane tabs = new JTabbedPane();    
    private PropertyPane activePanel = null;    
    private ArrayList<PropertyPane> panels = new ArrayList<PropertyPane>();
    
    /** Creates a new instance of ASDialog */
    private JPropertyDialog() {
        super();
        setModal(true);
        setTitle(BUNDLE.getString("properties"));
        //setIconImage(IconBundle.getResource().getIcon("16_jpropertydialog").getImage());
        
        JToolBar bas = new JToolBar();
        bas.setFloatable(false);
        bas.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        bas.add(apply);
        bas.add(revert);
        bas.add(close);
        
        apply.setIcon(IconBundle.getResource().getIcon("16_apply"));
        revert.setIcon(IconBundle.getResource().getIcon("16_reload"));
        close.setIcon(IconBundle.getResource().getIcon("16_close"));
        
        tabs.setTabPlacement(JTabbedPane.LEFT);
        
        tabs.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                activePanel = (PropertyPane)tabs.getSelectedComponent();
            }
        });
        
        apply.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for(PropertyPane edit : panels){
                    edit.apply();
                }
            }
        });
        
        revert.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(activePanel != null)
                    activePanel.reset();
            }
        });
        
        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for(PropertyPane edit : panels){
                    edit.apply();
                }
                dispose();
            }
        });
        
        setLayout( new BorderLayout());
        add(BorderLayout.SOUTH,bas);
        
    }
    
    public void addEditPanel(PropertyPane pan){
        panels.add(pan);        
        tabs.addTab(pan.getTitle(),pan.getIcon(),pan.getComponent(),pan.getToolTip());        
    }
    

    @Override
    public void setVisible(boolean b) {
        if(b){
            if(panels.size()>1){
                add(BorderLayout.CENTER,tabs);
            }else if(panels.size() == 1){
                add(BorderLayout.CENTER,(JComponent)panels.get(0));
            }
        }      
        super.setVisible(b);
    }
    
    public static void showDialog(List<PropertyPane> lst, Object target){
        JPropertyDialog dia = new JPropertyDialog();
        
        for(PropertyPane pro : lst){
            pro.setTarget(target);
            dia.addEditPanel(pro);
        }
        
        dia.setSize(700,500);
        dia.setLocationRelativeTo(null);
        dia.setVisible(true);
    }
   
    
}
