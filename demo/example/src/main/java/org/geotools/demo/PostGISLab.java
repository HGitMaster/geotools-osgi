package org.geotools.demo;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.postgis.PostgisDataStoreFactory;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.FilterTransformer;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.swing.data.JDataStoreWizard;
import org.geotools.swing.data.TypeNameChooser;
import org.geotools.swing.wizard.JWizard;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;

/**
 * A nice swing app to connect to PostGIS an try out different queries.
 * 
 * @author Jody Garnett
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/demo/example/src/main/java/org/geotools/demo/PostGISLab.java $
 */
public class PostGISLab {
    /**
     * Tip: When running from eclipse include the ${file_prompt} as an argument!
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // new ShapefileDataStoreFactory()
        
        JDataStoreWizard wizard = new JDataStoreWizard( new PostgisDataStoreFactory() );
        int result = wizard.showModalDialog();
        if (result != JWizard.FINISH) System.exit(0);
        Map<String, Object> connectionParameters = wizard.getConnectionParameters();
        DataStore dataStore = DataStoreFinder.getDataStore(connectionParameters);
        if (dataStore == null) {
            JOptionPane.showMessageDialog(null, "Could not conntect");
            System.exit(0);
        }
        String typeName = TypeNameChooser.showTypeNameChooser(dataStore);
        
        JQuery dialog = new JQuery(dataStore);
        dialog.setVisible(true);
        dialog.dispose();
        System.exit(0);
    }

    static class JQuery extends JDialog {
        final DataStore dataStore;

        JTextArea query;

        JTextArea show;

        JButton selectButton;

        JButton closeButton;

        JComboBox typeNameSelect;

        JButton schemaButton;

        private JButton filterButton;

        JQuery(DataStore database) throws IOException {
            this.dataStore = database;
            setTitle("Query");
            setModal(true);
            setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

            JPanel panel = new JPanel();
            panel.setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(2, 2, 2, 2);
            c.weightx = 1.0;

            Vector options = new Vector();
            String typeNames[] = dataStore.getTypeNames();
            for (int i = 0; i < typeNames.length; i++) {
                String typeName = typeNames[i];
                options.add(typeName);
            }
            typeNameSelect = new JComboBox(options);
            panel.add(typeNameSelect, c);

            schemaButton = new JButton("Describe Schema");
            schemaButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    displaySchema();
                }
            });
            c.gridwidth = GridBagConstraints.REMAINDER;
            panel.add(schemaButton, c);

            c.weightx = 0.0;
            c.weighty = 0.0;
            query = new JTextArea(4, 80);
            c.fill = GridBagConstraints.BOTH;
            JScrollPane scrollPane1 = new JScrollPane(query);
            scrollPane1.setPreferredSize(query.getPreferredScrollableViewportSize());
            scrollPane1.setMinimumSize(query.getPreferredScrollableViewportSize());
            panel.add(scrollPane1, c);

            c.fill = GridBagConstraints.NONE;
            c.weightx = 0.0;
            c.weighty = 0.0;
            c.gridwidth = GridBagConstraints.RELATIVE;
            selectButton = new JButton("Select Features");
            selectButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selectFeatures();
                }
            });
            panel.add(selectButton, c);

            c.gridwidth = GridBagConstraints.REMAINDER;
            filterButton = new JButton("CQL to Filter 1.0");
            filterButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cqlToFilter();
                }
            });
            panel.add(filterButton, c);

            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 1.0;
            show = new JTextArea(24, 80);
            show.setTabSize(2);
            JScrollPane scrollPane2 = new JScrollPane(show);
            scrollPane2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            scrollPane2.setPreferredSize(show.getPreferredScrollableViewportSize());
            // scrollPane2.setMinimumSize(show.getMinimumSize() );
            panel.add(scrollPane2, c);
            add(panel);

            c.weighty = 0.0;
            c.weightx = 0.0;
            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.CENTER;
            c.gridheight = GridBagConstraints.REMAINDER;
            c.gridwidth = GridBagConstraints.REMAINDER;
            closeButton = new JButton("Close");
            closeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                }
            });
            panel.add(closeButton, c);

            this.setSize(panel.getSize());
            this.pack();
        }

        private void displaySchema() {
            try {
                String typeName = (String) typeNameSelect.getSelectedItem();
                SimpleFeatureType schema = dataStore.getSchema(typeName);
                display(schema);
            } catch (Throwable t) {
                display(t);
            }
        }

        protected void display(Filter filter) throws Exception {
            FilterTransformer transform = new FilterTransformer();
            transform.setIndentation(2);
            String xml = transform.transform(filter);

            show.setText(xml);
        }

        public void display(SimpleFeatureType schema) {
            if (schema == null) {
                show.setText("null");
                return;
            } else {
                show.setText(schema.toString());
            }
        }

        public FeatureCollection filter(String text) throws Exception {
            Filter filter;
            filter = CQL.toFilter(text);

            String typeName = (String) typeNameSelect.getSelectedItem();
            DefaultQuery query = new DefaultQuery();
            query.setTypeName(typeName);
            query.setFilter(filter);
            query.setMaxFeatures(1000);

            FeatureSource table = dataStore.getFeatureSource(typeName);
            return table.getFeatures(query);
        }

        protected void display(FeatureCollection features) throws Exception {
            if (features == null) {
                show.setText("empty");
                return;
            }
            final FeatureType schema = features.getSchema();
            final StringBuffer buf = new StringBuffer();

            buf.append(DataUtilities.spec(schema));
            buf.append("\n");

            features.accepts(new FeatureVisitor() {
                public void visit(Feature feature) {
                    buf.append(feature.getIdentifier());
                    buf.append("=");
                    for (Property property : feature.getProperties()) {
                        buf.append("\t");
                        buf.append(property.getName());
                        buf.append("=");
                        buf.append(property.getValue());
                    }
                    buf.append("]");
                }
            }, null);
            show.setText(buf.toString());
        }

        public void display(Throwable t) {
            show.setText(t.getLocalizedMessage());
            show.setForeground(Color.RED);
        }

        private void selectFeatures() {
            try {
                String text = query.getText();
                FeatureCollection features = filter(text);
                display(features);
            } catch (Throwable t) {
                display(t);
            }
        }

        private void cqlToFilter() {
            try {
                String text = query.getText();
                Filter filter = CQL.toFilter(text);

                display(filter);
            } catch (Throwable t) {
                display(t);
            }
        }
    }
}
