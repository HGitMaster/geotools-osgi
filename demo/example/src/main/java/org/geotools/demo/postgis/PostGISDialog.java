/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This file is hereby placed into the Public Domain. This means anyone is
 *    free to do whatever they wish with this file. Use it well and enjoy!
 */
package org.geotools.demo.postgis;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.geotools.data.DataAccessFactory.Param;
import org.geotools.data.postgis.PostgisDataStoreFactory;

/**
 * A dialog that displays the PostGIS connection parameters.
 * 
 * This class is not important to our main story of connecting to PostGIS
 * it does serve as a nice example of how to use the Param information
 * published by the PostgidDataStoreFactory class.
 * <p>
 * If you want to write a generic DatastoreDialog you can make use of
 * the getParametersInfo() method at runtime.
 * </p>
 * @author Jody Garnett
 */
public class PostGISDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = -4773502007868922959L;
	private PostgisDataStoreFactory factory;

	private JParamField database;
	private JParamField dbtype;
	private JParamField host;
	private JParamField port;
	private JParamField schema;
	private JParamField user;
	private JPasswordField password;
	private JButton okay;
	private JButton cancel;
	
	boolean connect = false;
	public PostGISDialog(){
		this( Collections.EMPTY_MAP);
	}

	public PostGISDialog(Map config) {
		setTitle("Connection Parameters");
		setModal( true );
		dbtype = new JParamField( PostgisDataStoreFactory.DBTYPE, config );
		host = new JParamField( PostgisDataStoreFactory.HOST, config );
		port = new JParamField( PostgisDataStoreFactory.PORT, config );
		
		schema = new JParamField( PostgisDataStoreFactory.SCHEMA, config );
		database = new JParamField( PostgisDataStoreFactory.DATABASE, config );
		user = new JParamField( PostgisDataStoreFactory.USER, config );
		password = new JPasswordField( (String) config.get( PostgisDataStoreFactory.USER.key ));			
		password.setToolTipText( PostgisDataStoreFactory.PASSWD.description.toString() );

		okay = new JButton("OK");
		cancel = new JButton("Cancel");
		
		okay.addActionListener( this );
		cancel.addActionListener( this );
		
		// layout dialog
		setLayout( new GridLayout(0,2));
		
		add( new JLabel("DBType") );
		add( dbtype );
		add( new JLabel("Host"));
		add( host );
		add( new JLabel("Port"));
		add( port );
		add( new JLabel("Schema"));
		add( schema );
		add( new JLabel("Database"));
		add( database );
		add( new JLabel("user"));
		add( user );
		add( new JLabel("password"));
		add( password );
		
		add( new JLabel(""));
		JPanel buttons = new JPanel();
		add( buttons );			
		buttons.add( okay );
		buttons.add( cancel );
		
		setDefaultCloseOperation( JDialog.HIDE_ON_CLOSE );
		Dimension preferredSize = getPreferredSize();
		preferredSize.height += 30;
		setSize( preferredSize );
	}

	public Map getProperties() {
		if( !connect ){
			return null;
		}
		Map config = new HashMap();
		config.put( PostgisDataStoreFactory.DBTYPE.key, dbtype.getValue() );
		
		config.put( PostgisDataStoreFactory.HOST.key, host.getValue() );
		config.put( PostgisDataStoreFactory.PORT.key, port.getValue() );
		
		config.put( PostgisDataStoreFactory.SCHEMA.key, schema.getValue() );
		config.put( PostgisDataStoreFactory.DATABASE.key, database.getValue() );
		config.put( PostgisDataStoreFactory.USER.key, user.getValue() );
		config.put( PostgisDataStoreFactory.PASSWD.key, password.getText() );			
		
		
		return config;
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if( "OK".equals( action )){
			connect = true;
		}
		setVisible( false );			
	}
	class JParamField extends JTextField {
		Param param;
		Object value;
		
		JParamField( Param param ){
			this( param, Collections.EMPTY_MAP );
		}
		JParamField( Param param, Map map ){
			super( 14 );
			this.param = param;
			setValue( map.get( param.key ));
			addKeyListener( new KeyAdapter(){
				public void keyReleased(KeyEvent e) {
					refresh();
				}
			});
			setToolTipText( param.description.toString() );
		}
		public void refresh(){
			try {
				JParamField.this.value = param.parse( getText() );
				setToolTipText( param.description.toString() );
				setForeground( Color.BLACK );
			} catch (Throwable e) {
				setToolTipText( e.getLocalizedMessage() );
				setForeground( Color.RED );
				JParamField.this.value = null;
			}
		}
		public void setValue( Object value ){
			if( value == null ){
				value = param.sample;
			}
			this.value = value;
			if( value == null ){
				setText("");
			}
			else {
				setText( param.text( value ) );			
			}
		}
		public Object getValue() {
			return value;
		}
	}
}
