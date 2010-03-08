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
package org.geotools.gui.swing.datachooser;

import java.awt.Component;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import org.geotools.data.FeatureSource;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.geotools.gui.swing.datachooser.model.DBModel;
import org.geotools.gui.swing.icon.IconBundle;
import org.geotools.gui.swing.misc.Render.RandomStyleFactory;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapLayer;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * WFS DataChoosert panel
 *
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/datachooser/JWFSDataPanel.java $
 */
public class JWFSDataPanel extends javax.swing.JPanel implements DataPanel {

    private static ResourceBundle BUNDLE = ResourceBundle.getBundle("org/geotools/gui/swing/datachooser/Bundle");
    private WFSDataStore store;
    private final Map params = new HashMap<String, Object>();

    /** Creates new form DefaultShapeTypeChooser */
    public JWFSDataPanel() {
        initComponents();

        params.put("dbtype", "postgis");

        params.put(WFSDataStoreFactory.URL.key, WFSDataStoreFactory.URL.sample);
        params.put(WFSDataStoreFactory.USERNAME.key, WFSDataStoreFactory.USERNAME.sample);
        params.put(WFSDataStoreFactory.PASSWORD.key, WFSDataStoreFactory.PASSWORD.sample);
        params.put(WFSDataStoreFactory.BUFFER_SIZE.key, WFSDataStoreFactory.BUFFER_SIZE.sample);
        params.put(WFSDataStoreFactory.MAXFEATURES.key, WFSDataStoreFactory.MAXFEATURES.sample);
        params.put(WFSDataStoreFactory.TIMEOUT.key, WFSDataStoreFactory.TIMEOUT.sample);
        params.put(WFSDataStoreFactory.ENCODING.key, WFSDataStoreFactory.ENCODING.sample);
        params.put(WFSDataStoreFactory.LENIENT.key, WFSDataStoreFactory.LENIENT.sample);
        params.put(WFSDataStoreFactory.PROTOCOL.key, WFSDataStoreFactory.PROTOCOL.sample);
        params.put(WFSDataStoreFactory.TRY_GZIP.key, WFSDataStoreFactory.TRY_GZIP.sample);

        setProperties(params);

        jtf_url.setToolTipText(WFSDataStoreFactory.URL.description.toString());
        jtf_user.setToolTipText(WFSDataStoreFactory.USERNAME.description.toString());
        jtf_password.setToolTipText(WFSDataStoreFactory.PASSWORD.description.toString());
        jsp_buff_size.setToolTipText(WFSDataStoreFactory.BUFFER_SIZE.description.toString());
        jsp_max_features.setToolTipText(WFSDataStoreFactory.MAXFEATURES.description.toString());
        jsp_timeout.setToolTipText(WFSDataStoreFactory.TIMEOUT.description.toString());
        jtf_encoding.setToolTipText(WFSDataStoreFactory.ENCODING.description.toString());
        chk_lenient.setToolTipText(WFSDataStoreFactory.LENIENT.description.toString());
        chk_gzip.setToolTipText(WFSDataStoreFactory.TRY_GZIP.description.toString());
        chk_protocol.setToolTipText(WFSDataStoreFactory.PROTOCOL.description.toString());

        tab_table.setTableHeader(null);
        tab_table.setModel(new DBModel(tab_table));

    }

    public Map getProperties() {
        return params;
    }

    public void setProperties(Map map) {

        if (map == null) {
            throw new NullPointerException();
        }

        Object val = null;

        val = map.get(WFSDataStoreFactory.URL.key);
        jtf_url.setText((val == null) ? "" : val.toString());
        val = map.get(WFSDataStoreFactory.USERNAME.key);
        jtf_user.setText((val == null) ? "" : val.toString());
        val = map.get(WFSDataStoreFactory.PASSWORD.key);
        jtf_password.setText((val == null) ? "" : val.toString());
        val = map.get(WFSDataStoreFactory.BUFFER_SIZE.key);
        jsp_buff_size.setValue((val == null) ? 100 : val);
        val = map.get(WFSDataStoreFactory.MAXFEATURES.key);
        jsp_max_features.setValue((val == null) ? 0 : val);
        val = map.get(WFSDataStoreFactory.TIMEOUT.key);
        jsp_timeout.setValue((val == null) ? 3 : val);
        val = map.get(WFSDataStoreFactory.ENCODING.key);
        jtf_encoding.setText((val == null) ? "" : val.toString());
        val = map.get(WFSDataStoreFactory.LENIENT.key);
        chk_lenient.setSelected((val == null) ? (Boolean) true : (Boolean) val);
        val = map.get(WFSDataStoreFactory.TRY_GZIP.key);
        chk_gzip.setSelected((val == null) ? (Boolean) true : (Boolean) val);
        val = map.get(WFSDataStoreFactory.PROTOCOL.key);
        chk_protocol.setSelected((val == null) ? (Boolean) true : (Boolean) val);

    }

    private void refreshTable() {

        if (store != null) {
            ((DBModel) tab_table.getModel()).clean();
            try {
                ((DBModel) tab_table.getModel()).add(store.getTypeNames());
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jtf_url = new javax.swing.JTextField();
        jtf_user = new javax.swing.JTextField();
        jtf_password = new javax.swing.JPasswordField();
        jPanel2 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jtf_encoding = new javax.swing.JTextField();
        chk_lenient = new javax.swing.JCheckBox();
        chk_protocol = new javax.swing.JCheckBox();
        chk_gzip = new javax.swing.JCheckBox();
        jsp_buff_size = new javax.swing.JSpinner();
        jsp_max_features = new javax.swing.JSpinner();
        jLabel9 = new javax.swing.JLabel();
        jsp_timeout = new javax.swing.JSpinner();
        jScrollPane1 = new javax.swing.JScrollPane();
        tab_table = new org.jdesktop.swingx.JXTable();
        but_refresh = new javax.swing.JButton();
        gui_progress = new javax.swing.JProgressBar();

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/geotools/gui/swing/datachooser/Bundle"); // NOI18N
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("connection"))); // NOI18N

        jLabel1.setText(bundle.getString("url")); // NOI18N

        jLabel5.setText(bundle.getString("user")); // NOI18N

        jLabel6.setText(bundle.getString("password")); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel1)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jtf_url, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel5)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jtf_user, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jLabel6)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jtf_password, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jtf_url, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel5)
                    .add(jtf_user, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel6)
                    .add(jtf_password, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("advanced"))); // NOI18N

        jLabel7.setText(bundle.getString("buffer_size")); // NOI18N

        jLabel8.setText(bundle.getString("max_features")); // NOI18N

        jLabel13.setText(bundle.getString("encoding")); // NOI18N

        chk_lenient.setText(bundle.getString("lenient")); // NOI18N

        chk_protocol.setText(bundle.getString("protocol")); // NOI18N

        chk_gzip.setText(bundle.getString("try_gzip")); // NOI18N

        jsp_buff_size.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));

        jsp_max_features.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));

        jLabel9.setText(bundle.getString("timeout")); // NOI18N

        jsp_timeout.setModel(new javax.swing.SpinnerNumberModel(0, 0, 255, 1));

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2Layout.createSequentialGroup()
                                .add(jLabel9)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jsp_timeout))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2Layout.createSequentialGroup()
                                .add(jLabel7)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jsp_buff_size, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 92, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2Layout.createSequentialGroup()
                                .add(jLabel8)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(jsp_max_features)))
                        .addContainerGap(90, Short.MAX_VALUE))
                    .add(jPanel2Layout.createSequentialGroup()
                        .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(chk_gzip)
                            .add(jPanel2Layout.createSequentialGroup()
                                .add(chk_protocol)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(chk_lenient))
                        .addContainerGap(185, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                        .add(jLabel13)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jtf_encoding, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel7)
                    .add(jsp_buff_size, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel8)
                    .add(jsp_max_features, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel9)
                    .add(jsp_timeout, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chk_lenient)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chk_protocol)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(chk_gzip)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(jLabel13)
                    .add(jtf_encoding, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tab_table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tab_table);

        but_refresh.setText(bundle.getString("connect")); // NOI18N
        but_refresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                but_refreshactionRefresh(evt);
            }
        });

        gui_progress.setString(bundle.getString("waiting")); // NOI18N
        gui_progress.setStringPainted(true);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 199, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(gui_progress, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(but_refresh)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(but_refresh)
                    .add(gui_progress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    private void but_refreshactionRefresh(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_but_refreshactionRefresh

        but_refresh.setEnabled(false);
        gui_progress.setString(BUNDLE.getString("connecting"));
        gui_progress.setIndeterminate(true);
        Thread t = new Thread() {

            @Override
            public void run() {
                try {

                    params.put(WFSDataStoreFactory.URL.key, new URL(jtf_url.getText()));
                    params.put(WFSDataStoreFactory.USERNAME.key, jtf_user.getText());
                    params.put(WFSDataStoreFactory.PASSWORD.key, new String(jtf_password.getPassword()));
                    params.put(WFSDataStoreFactory.BUFFER_SIZE.key, jsp_buff_size.getValue());
                    params.put(WFSDataStoreFactory.ENCODING.key, jtf_encoding.getText());
                    params.put(WFSDataStoreFactory.LENIENT.key, chk_lenient.isSelected());
                    params.put(WFSDataStoreFactory.MAXFEATURES.key, jsp_max_features.getValue());
                    params.put(WFSDataStoreFactory.PROTOCOL.key, chk_protocol.isSelected());
                    params.put(WFSDataStoreFactory.TIMEOUT.key, jsp_timeout.getValue());
                    params.put(WFSDataStoreFactory.TRY_GZIP.key, chk_gzip.isSelected());

//                    store = DataStoreFinder.getDataStore(params);
                    store = new WFSDataStoreFactory().createDataStore(params);
                    refreshTable();
                    gui_progress.setString(BUNDLE.getString("waiting"));
                } catch (Exception ex) {
                    store = null;
                    gui_progress.setString(BUNDLE.getString("error"));
                }
            }
        };
        t.start();

        gui_progress.setIndeterminate(false);
        but_refresh.setEnabled(true);
    }//GEN-LAST:event_but_refreshactionRefresh

    public ImageIcon getIcon() {
        return IconBundle.getResource().getIcon("16_web");
    }

    public String getTitle() {
        return BUNDLE.getString("wfs");
    }

    public Component getChooserComponent() {
        return this;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton but_refresh;
    private javax.swing.JCheckBox chk_gzip;
    private javax.swing.JCheckBox chk_lenient;
    private javax.swing.JCheckBox chk_protocol;
    private javax.swing.JProgressBar gui_progress;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner jsp_buff_size;
    private javax.swing.JSpinner jsp_max_features;
    private javax.swing.JSpinner jsp_timeout;
    private javax.swing.JTextField jtf_encoding;
    private javax.swing.JPasswordField jtf_password;
    private javax.swing.JTextField jtf_url;
    private javax.swing.JTextField jtf_user;
    private org.jdesktop.swingx.JXTable tab_table;
    // End of variables declaration//GEN-END:variables
    public MapLayer[] getLayers() {
        ArrayList<MapLayer> layers = new ArrayList<MapLayer>();
        RandomStyleFactory rsf = new RandomStyleFactory();

        WFSDataStoreFactory factory = new WFSDataStoreFactory();
        try {
            store = factory.createDataStore(params);
            FeatureSource<SimpleFeatureType, SimpleFeature> fs = store.getFeatureSource(store.getTypeNames()[0]);
            Style style = rsf.createRandomVectorStyle(fs);
            MapLayer layer = new DefaultMapLayer(fs, style);
            layer.setTitle("layer");
            layers.add(layer);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (store != null) {


//            for (int i = 0; i < tab_table.getSelectedRows().length; i++) {
//                try {
//                    DBModel model = (DBModel) tab_table.getModel();
//                    String name = (String) model.getValueAt(tab_table.getSelectedRows()[i], 0);
//                    FeatureSource<SimpleFeatureType, SimpleFeature> fs = store.getFeatureSource(name);
//                    Style style = rsf.createRandomVectorStyle(fs);
//
//                    MapLayer layer = new DefaultMapLayer(fs, style);
//                    layer.setTitle("oracle - " + name);
//                    layers.add(layer);
//                } catch (IOException ex) {
//                    System.out.println(ex);
//                }
//            }

        }

        return layers.toArray(new MapLayer[layers.size()]);
    }
}
