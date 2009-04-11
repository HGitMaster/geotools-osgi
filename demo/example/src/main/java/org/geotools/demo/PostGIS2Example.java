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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureSource;
import org.geotools.demo.postgis.PostGISDialog;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.collection.AbstractFeatureVisitor;
import org.geotools.filter.FilterTransformer;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.gui.swing.ProgressWindow;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;

public class PostGIS2Example {
	
	public static void main(String[] args) throws Exception {
		DataStore dataStore = getDatabase(args);
		if( dataStore == null ){
		    System.out.println( "Could not connect");
		    System.exit(0);
		}
		String[] typeNames = dataStore.getTypeNames();
		String typeName = typeNames[0];

		System.out.println("Reading content " + typeName);
		FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource(typeName);

		SimpleFeatureType simpleFeatureType = featureSource.getSchema();
		System.out.println("Header: "+DataUtilities.spec( simpleFeatureType ));
		
		JQuery dialog = new JQuery( dataStore );
		dialog.show();
		dialog.dispose();
		System.exit(0);
	}

	private static DataStore getDatabase(String[] args) throws IOException {
		PostGISDialog dialog;
		
		if (args.length == 0){
			dialog = new PostGISDialog();
		}
		else {
			File file = new File( args[0] );
			if (!file.exists()){
				throw new FileNotFoundException( file.getAbsolutePath() );
			}
			InputStream reader = new FileInputStream( file );
			Properties config = new Properties();			
			config.load(reader);
			
			dialog = new PostGISDialog( config );
		}		
		dialog.show();
		Map properties = dialog.getProperties();
		dialog.dispose();

		if( properties == null){
			System.exit(0);
		}
		DataStore dataStore = DataStoreFinder.getDataStore( properties );
		return dataStore;
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
		
		JQuery( DataStore database ) throws IOException {
			this.dataStore = database;
			setTitle("Query");
			setModal( true );
			setDefaultCloseOperation( JDialog.HIDE_ON_CLOSE );
			
			setLayout( new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
						
			Vector<String> options = new Vector<String>();
			for( String typeName : dataStore.getTypeNames() ){
				options.add( typeName );
			}
			typeNameSelect = new JComboBox( options );
			c.gridx=0;
			c.gridy=0;
			c.gridwidth = 1;
			c.gridheight = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			add( typeNameSelect, c );
						
			schemaButton = new JButton("Describe Schema");
			schemaButton.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					try {
						String typeName = (String) typeNameSelect.getSelectedItem();
						SimpleFeatureType simpleFeatureType = dataStore.getSchema( typeName );
						display( simpleFeatureType );
					} catch (Throwable t ){
						display( t );
					}
				}
			});
			c.gridx=GridBagConstraints.RELATIVE;
			add( schemaButton, c );
			
			query = new JTextArea(5,80);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = 1;
			c.gridwidth = 5;
			c.fill = GridBagConstraints.BOTH;
			JScrollPane scrollPane1 = new JScrollPane(query);
			scrollPane1.setPreferredSize( new Dimension(600,100));
			add( scrollPane1, c);
			
			selectButton = new JButton("Select Features");
			selectButton.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					try {
						String text = query.getText();
						FeatureCollection<SimpleFeatureType, SimpleFeature> features = filter( text );	
						display( features );
					} catch (Throwable t ){
						display( t );
					}
				}
			});
			c.gridx=0;
			c.gridy=2;
			c.gridheight=1;
			c.gridwidth=1;			
			add( selectButton, c );
		
			filterButton = new JButton("CQL to Filter 1.0");
			filterButton.addActionListener( new ActionListener(){
				public void actionPerformed(ActionEvent e){
					try {
						String text = query.getText();
						Filter filter = CQL.toFilter(text);
							
						display( filter );
					} catch (Throwable t ){
						display( t );
					}
				}
			});
			c.gridx=1;
			add( filterButton, c );
		
			closeButton = new JButton("Close");
			closeButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}				
			});
			c.gridx=3;
			c.gridy=4;
			add( closeButton, c );

			show = new JTextArea(40,80);
			show.setTabSize(2);
			c.fill = GridBagConstraints.BOTH;
			c.gridx = 0;
			c.gridy = 3;			
			c.gridheight = 1;
			c.gridwidth = 4;
			c.fill = GridBagConstraints.BOTH;
			JScrollPane scrollPane2 = new JScrollPane(show);
			scrollPane2.setPreferredSize( new Dimension(600,400));
			add( scrollPane2, c);
			
			this.pack();
		}		
		
		protected void display(Filter filter) throws Exception {
			StringBuffer buf = new StringBuffer();
			FilterTransformer transform = new FilterTransformer();
			transform.setIndentation(2);
			String xml = transform.transform( filter );
			
			show.setText( xml );
		}

		public void display(SimpleFeatureType schema) {
			if( schema == null ){
				show.setText("");
				return;
			}
			StringBuffer buf = new StringBuffer();
			buf.append("typeName=");
			buf.append( schema.getTypeName() );
			
			buf.append(" namespace=");
			buf.append( schema.getName().getNamespaceURI() );
			buf.append("attributes = ([\n");
			
			for( PropertyDescriptor type : schema.getDescriptors() ){
				buf.append( type.getName().getLocalPart() );
				buf.append(" [\n");					
				
				buf.append("\t binding=");
				buf.append( type.getType().getBinding() );
				buf.append("\n");
				
				buf.append("\t minOccurs=");
				buf.append( type.getMinOccurs() );
				buf.append(" maxOccurs=");
				buf.append( type.getMaxOccurs());
				buf.append(" nillable=");
				buf.append( type.isNillable() );				
				buf.append("\n");
				
				buf.append("\t restrictions=");
				buf.append( type.getType().getRestrictions() );
				buf.append("\n");
				
				if( type instanceof GeometryDescriptor ){
				    GeometryDescriptor geomType = (GeometryDescriptor) type;
					buf.append("\t crs=");
					if( geomType.getCoordinateReferenceSystem() == null ){
						buf.append("null");						
					}
					else {
						buf.append( geomType.getCoordinateReferenceSystem().getName() );	
					}					
					buf.append("\n");						
				}
				buf.append("]\n");
			}	
			buf.append(")");
			show.setText( buf.toString() );
		}

		public FeatureCollection<SimpleFeatureType, SimpleFeature> filter(String text ) throws Exception {
			Filter filter; 
			filter = CQL.toFilter( text );
			
			String typeName = (String) typeNameSelect.getSelectedItem();
			FeatureSource<SimpleFeatureType, SimpleFeature> table = dataStore.getFeatureSource( typeName );
			return table.getFeatures(filter);
		}
		
		protected void display(FeatureCollection<SimpleFeatureType, SimpleFeature> features) throws Exception {
			if( features == null ){
				show.setText("");
				return;
			}
			final StringBuffer buf = new StringBuffer();
			final SimpleFeatureType schema = features.getSchema();
			buf.append( DataUtilities.spec( schema ));
			buf.append("\n");
			features.accepts( new AbstractFeatureVisitor(){
				public void visit(Feature feature) {
					buf.append( feature.getIdentifier() );
					buf.append(" [\n");
					for( AttributeDescriptor type : schema.getAttributeDescriptors() ){
						String name = type.getLocalName();
						buf.append("\t");
						buf.append( name );
						buf.append( "=" );
						buf.append( feature.getProperty(name ).getValue() );
					}
					buf.append("]");					
				}
			}, new ProgressWindow( this ));
			
			show.setText( buf.toString() );
		}
		public void display(Throwable t ){
			show.setText( t.getLocalizedMessage() );
			show.setForeground( Color.RED );
		}
	}
}
