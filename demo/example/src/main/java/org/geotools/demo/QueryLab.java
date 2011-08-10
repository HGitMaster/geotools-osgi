// docs start source
/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This file is hereby placed into the Public Domain. This means anyone is
 *    free to do whatever they wish with this file. Use it well and enjoy!
 */

package org.geotools.demo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.postgis.PostgisDataStoreFactory;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.swing.action.SafeAction;
import org.geotools.swing.data.JDataStoreWizard;
import org.geotools.swing.table.FeatureCollectionTableModel;
import org.geotools.swing.wizard.JWizard;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * The Query Lab is an excuse to try out Filters and Expressions on your own data with a table to
 * show the results.
 * <p>
 * Remember when programming that you have other options then the CQL parser, you can directly make
 * a Filter using CommonFactoryFinder.getFilterFactory2(null).
 */
@SuppressWarnings("serial")
public class QueryLab extends JFrame {
    DataStore datastore;
    JComboBox types;
    JTable table;
    JTextField text;

    public QueryLab(DataStore data) {
        this.datastore = data;
        // USER INTERFACE
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new BorderLayout());

        try {
            types = new JComboBox(datastore.getTypeNames());
        } catch (IOException e1) {
            JOptionPane.showMessageDialog(null, "Unable to find any published content");
            System.exit(0);
        }

        text = new JTextField(80);
        text.setText("include"); // include selects everything!
        getContentPane().add(text, BorderLayout.NORTH);

        table = new JTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setModel(new DefaultTableModel(5, 5));
        table.setPreferredScrollableViewportSize(new Dimension(500, 200));

        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        JMenuBar menubar = new JMenuBar();
        setJMenuBar(menubar);

        menubar.add(types);
        JMenu menu = new JMenu("Data");
        menubar.add(menu);
        pack();
        // ACTIONS
        // begin filter
        // ACTIONS
        menu.add(new SafeAction("Get features") {
            public void action(ActionEvent e) throws Throwable {
                filterFeatures();
            }
        });
        // end filter
        // begin count
        menu.add(new SafeAction("Count") {
            public void action(ActionEvent e) throws Throwable {
                countFeatures();
            }
        });
        // end count
        // begin center
        menu.add(new SafeAction("Center") {
            public void action(ActionEvent e) throws Throwable {
                centerFeatures();
            }

        });
        // end center
        // begin query
        menu.add(new SafeAction("Geometry") {
            public void action(ActionEvent e) throws Throwable {
                queryFeatures();
            }
        });
        // end query

    }

    // docs start main
    public static void main(String[] args) throws Exception {
        /*
         * We use a GeoTools wizard to prompt the user for an input shapefile.
         * 
         * To modify this example to work with a PostGIS database instead just replace 'new
         * ShapefileDataStoreFactory()' in the line below with 'new PostgisDataStoreFactory()'
         */
        JDataStoreWizard wizard = new JDataStoreWizard(new ShapefileDataStoreFactory());
        //JDataStoreWizard wizard = new JDataStoreWizard(new PostgisDataStoreFactory());
        int result = wizard.showModalDialog();
        if (result != JWizard.FINISH) {
            System.exit(0);
        }

        Map<String, Object> connectionParameters = wizard.getConnectionParameters();
        DataStore dataStore = DataStoreFinder.getDataStore(connectionParameters);
        if (dataStore == null) {
            JOptionPane.showMessageDialog(null, "Could not connect - check parameters");
            System.exit(0);
        }

        JFrame frame = new QueryLab(dataStore);
        frame.setVisible(true);
    }
    // docs end main
    
    @SuppressWarnings("unchecked")
    // begin filterFeatures
    public void filterFeatures() throws Exception {
        String typeName = (String) types.getSelectedItem();
        FeatureSource source = datastore.getFeatureSource(typeName);

        Filter filter = CQL.toFilter(text.getText());
        FeatureCollection features = source.getFeatures(filter);
        FeatureCollectionTableModel model = new FeatureCollectionTableModel(features);
        table.setModel(model);
    }
    // end filterFeatures

    @SuppressWarnings("unchecked")
    // begin countFeatures
    public void countFeatures() throws Exception {
        String typeName = (String) types.getSelectedItem();
        FeatureSource source = datastore.getFeatureSource(typeName);

        Filter filter = CQL.toFilter(text.getText());
        FeatureCollection features = source.getFeatures(filter);

        int count = features.size();
        JOptionPane.showMessageDialog(text, "Number of selected features:" + count);
    }

    // end countFeatures

    @SuppressWarnings("unchecked")
    // begin centerFeatures
    private void centerFeatures() throws Exception {
        String typeName = (String) types.getSelectedItem();
        FeatureSource source = datastore.getFeatureSource(typeName);

        Filter filter = CQL.toFilter(text.getText());
        FeatureCollection<SimpleFeatureType, SimpleFeature> features = source.getFeatures(filter);

        double totalX = 0.0;
        double totalY = 0.0;
        long count = 0;
        FeatureIterator<SimpleFeature> iterator = features.features();
        try {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                Geometry geom = (Geometry) feature.getDefaultGeometry();
                Point centroid = geom.getCentroid();
                totalX += centroid.getX();
                totalY += centroid.getY();
                count++;
            }
        } finally {
            iterator.close(); // IMPORTANT
        }
        double averageX = totalX / (double) count;
        double averageY = totalY / (double) count;
        Coordinate center = new Coordinate(averageX, averageY);

        JOptionPane.showMessageDialog(text, "Center of selected features:" + center);
    }

    // end centerFeatures

    @SuppressWarnings("unchecked")
    // begin queryFeatures
    public void queryFeatures() throws Exception {
        String typeName = (String) types.getSelectedItem();
        FeatureSource source = datastore.getFeatureSource(typeName);

        FeatureType schema = source.getSchema();
        String name = schema.getGeometryDescriptor().getLocalName();

        Filter filter = CQL.toFilter(text.getText());

        DefaultQuery query = new DefaultQuery(schema.getName().getLocalPart(), filter,
                new String[] { name });

        FeatureCollection<SimpleFeatureType, SimpleFeature> features = source.getFeatures(query);

        FeatureCollectionTableModel model = new FeatureCollectionTableModel(features);
        table.setModel(model);
    }
    // end queryFeatures
}
// docs end source
