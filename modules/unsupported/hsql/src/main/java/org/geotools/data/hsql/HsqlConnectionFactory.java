/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.hsql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * This creates connections for the HSQL datasource to make 
 * its  transactions.  To create a HsqlDataStore, create a 
 * HsqlConnectionFactory, and pass that connection factory 
 * to the HsqlDataStore constructor.
 *
 * @author Amr Alam, Refractions Research
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/hsql/src/main/java/org/geotools/data/hsql/HsqlConnectionFactory.java $
 */
public class HsqlConnectionFactory {

	/** Standard logging instance */
//    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.data.hsql");

    /** Creates Hsql-specific JDBC driver class. */
    private static final String DRIVER_CLASS = "org.hsqldb.jdbcDriver";
    private String _username = "";
    private String _password = "";
    /** An alternate character set to use. */
    private String charSet;

	private String _dbFileName = null;
    
    /**
     * Creates a new HsqlConnectionFactory object from a DB filename, a username
     * and a password.
     * @param dbFileName the HSQL database filename
     * @param user the HSQL database username
     * @param password the HSQL database password for user
     */
    public HsqlConnectionFactory(String dbFileName, String user, String password) {
    	_dbFileName = dbFileName;
    	_username = user;
    	_password = password;
    }
    
    /**
     * 
     * @return an HSQL Connection object
     * @throws SQLException if an error occurs connecting to the HSQL database
     */
    public Connection getConnection() throws SQLException {
    	return getConnection(_dbFileName, _username, _password);
    }
    
    public Connection getConnection(String dbFileName) throws SQLException {
    	_dbFileName = dbFileName;
    	return getConnection(_dbFileName, _username, _password);
    }
    
    /**
     * Creates and returns a HSQL Connection based upon the username 
     * and password parameters passed to this
     * method.  This is shorthand for the following two calls:<br>
     * <br>
     * connPool.setLogin(username, password);<br>
     * connPool.setDBFileName(filename);<br>
     * connPool.getConnection();<br>
     * @param username the HSQL username
     * @param password the password corresponding to <code>username</code>
     * @return an HSQL Connection object
     * @throws SQLException if an error occurs connecting to the HSQL database
     */
    public Connection getConnection(String username, String password) throws SQLException {
        setLogin(username, password);
        return getConnection(_dbFileName, username, password);
    }

    /**
	 * Creates and returns a HSQL Connection based upon the username 
     * and password parameters passed to this
     * method.  This is shorthand for the following two calls:<br>
     * <br>
     * connPool.setLogin(username, password);<br>
     * connPool.setDBFileName(filename);<br>
     * connPool.getConnection();<br>
     * 
     * @param dbFileName the filename to use for the new database connection.
	 * @param user the name of the user connect to connect to the pgsql db.
	 * @param password the password for the user.
	 * 
	 * @return the sql Connection object to the database.
	 * 
	 * @throws SQLException if the postgis sql driver could not be found
	 */
    public Connection getConnection(String dbFileName, String user, String password) throws SQLException {
    	_dbFileName = dbFileName;
    	_username = user;
    	_password = password;
    	
        Properties props = new Properties();
        props.put("user", user);
        props.put("password", password);

        if (charSet != null) {
            props.put("charSet", charSet);
        }

        return getConnection(dbFileName, props);
    }

    /**
	 * Creates a database connection method to initialize a given database for
	 * feature extraction with the given Properties.
	 * 
	 * @param dbFileName the filename to use for the new database connection.
	 * @param props Should contain at a minimum the user and password. Additional
	 *            properties, such as charSet, can also be added.
	 * 
	 * @return the sql Connection object to the database.
	 * 
	 * @throws SQLException if the postgis sql driver could not be found
	 */
    public Connection getConnection(String dbFileName, Properties props) throws SQLException {
    	_dbFileName = dbFileName;
    	Connection dbConnection = null;
        try {
        	//Load the HSQL Database Engine JDBC driver
            // hsqldb.jar should be in the class path or made part of the current jar
			Class.forName(DRIVER_CLASS);
			
			//connect to the database.   This will load the db files and start the
	        // database if it is not alread running.
	        // dbFileName is used to open or create files that hold the state
	        // of the db.
	        // It can contain directory names relative to the
	        // current working directory
	        dbConnection = DriverManager.getConnection("jdbc:hsqldb:"//"jdbc:hsqldb:"
	                                           + dbFileName,   	   // filenames
	                                           props); // properties (include user/password)
	        setSpatialAliases(dbConnection);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return dbConnection;
    }

    /**
     * Sets the HSQL database login credentials.
     * @param username the username
     * @param password the password
     */
    public void setLogin(String username, String password) {
        _username = username;
        _password = password;
    }
    
    /**
     * Sets the HSQL database filename.
     * @param dbFileName the filename to use for this database
     */
    public void setDBFileName(String dbFileName){
    	_dbFileName = dbFileName;
    }

    /**
	 * Sets a different character set for the hsql driver to use.
	 *
	 * @param charSet the string of a valid charset name.
	 */
	public void setCharSet(String charSet) {
		this.charSet = charSet;
	}
	
	/**
	 * Sets up all the spatial DB in a box aliases
	 * @param conn The connection we've just created
	 * @throws IOException
	 * @throws SQLException
	 */
	private void setSpatialAliases(Connection conn) throws IOException, SQLException{
    	Statement st = conn.createStatement();
    	st.execute("CREATE ALIAS equals FOR \"org.openplans.spatialDBbox.StaticGeometry.equals\";" +
    			"CREATE ALIAS toString FOR \"org.openplans.spatialDBbox.StaticGeometry.toString\";" +
    			"CREATE ALIAS contains FOR \"org.openplans.spatialDBbox.StaticGeometry.contains\";" +
    			"CREATE ALIAS isEmpty FOR \"org.openplans.spatialDBbox.StaticGeometry.isEmpty\";" +
    			"CREATE ALIAS length FOR \"org.openplans.spatialDBbox.StaticGeometry.getLength\";" +
    			"CREATE ALIAS intersects FOR \"org.openplans.spatialDBbox.StaticGeometry.intersects\";" +
    			"CREATE ALIAS geomFromWKT FOR \"org.openplans.spatialDBbox.StaticGeometry.geomFromWKT\";" +
    			"CREATE ALIAS isValid FOR \"org.openplans.spatialDBbox.StaticGeometry.isValid\";" +
    			"CREATE ALIAS geometryType FOR \"org.openplans.spatialDBbox.StaticGeometry.getGeometryType\";" +
    			"CREATE ALIAS sRID FOR \"org.openplans.spatialDBbox.StaticGeometry.getSRID\";" +
    			"CREATE ALIAS numPoints FOR \"org.openplans.spatialDBbox.StaticGeometry.getNumPoints\";" +
    			"CREATE ALIAS isSimple FOR \"org.openplans.spatialDBbox.StaticGeometry.isSimple\";" +
    			"CREATE ALIAS distance FOR \"org.openplans.spatialDBbox.StaticGeometry.distance\";" +
    			"CREATE ALIAS isWithinDistance FOR \"org.openplans.spatialDBbox.StaticGeometry.isWithinDistance\";" +
    			"CREATE ALIAS area FOR \"org.openplans.spatialDBbox.StaticGeometry.getArea\";" +
    			"CREATE ALIAS centroid FOR \"org.openplans.spatialDBbox.StaticGeometry.getCentroid\";" +
    			"CREATE ALIAS interiorPoint FOR \"org.openplans.spatialDBbox.StaticGeometry.getInteriorPoint\";" +
    			"CREATE ALIAS dimension FOR \"org.openplans.spatialDBbox.StaticGeometry.getDimension\";" +
    			"CREATE ALIAS boundary FOR \"org.openplans.spatialDBbox.StaticGeometry.getBoundary\";" +
    			"CREATE ALIAS boundaryDimension FOR \"org.openplans.spatialDBbox.StaticGeometry.getBoundaryDimension\";" +
    			"CREATE ALIAS envelope FOR \"org.openplans.spatialDBbox.StaticGeometry.getEnvelope\";" +
    			"CREATE ALIAS disjoint FOR \"org.openplans.spatialDBbox.StaticGeometry.disjoint\";" +
    			"CREATE ALIAS touches FOR \"org.openplans.spatialDBbox.StaticGeometry.touches\";" +
    			"CREATE ALIAS crosses FOR \"org.openplans.spatialDBbox.StaticGeometry.crosses\";" +
    			"CREATE ALIAS within FOR \"org.openplans.spatialDBbox.StaticGeometry.within\";" +
    			"CREATE ALIAS overlaps FOR \"org.openplans.spatialDBbox.StaticGeometry.overlaps\";" +
    			"CREATE ALIAS relatePattern FOR \"org.openplans.spatialDBbox.StaticGeometry.relatePattern\";" +
    			"CREATE ALIAS relate FOR \"org.openplans.spatialDBbox.StaticGeometry.relate\";" +
    			"CREATE ALIAS toText FOR \"org.openplans.spatialDBbox.StaticGeometry.toText\";" +
    			"CREATE ALIAS buffer_with_segments FOR \"org.openplans.spatialDBbox.StaticGeometry.buffer_with_segments\";" +
    			"CREATE ALIAS buffer FOR \"org.openplans.spatialDBbox.StaticGeometry.buffer\";" +
    			"CREATE ALIAS convexHull FOR \"org.openplans.spatialDBbox.StaticGeometry.convexHull\";" +
    			"CREATE ALIAS intersection FOR \"org.openplans.spatialDBbox.StaticGeometry.intersection\";" +
    			"CREATE ALIAS unionGeom FOR \"org.openplans.spatialDBbox.StaticGeometry.unionGeom\";" +
    			"CREATE ALIAS difference FOR \"org.openplans.spatialDBbox.StaticGeometry.difference\";" +
    			"CREATE ALIAS symDifference FOR \"org.openplans.spatialDBbox.StaticGeometry.symDifference\";" +
    			"CREATE ALIAS equalsExactTolerance FOR \"org.openplans.spatialDBbox.StaticGeometry.equalsExactTolerance\";" +
    			"CREATE ALIAS equalsExact FOR \"org.openplans.spatialDBbox.StaticGeometry.equalsExact\";" +
    			"CREATE ALIAS numGeometries FOR \"org.openplans.spatialDBbox.StaticGeometry.getNumGeometries\";" +
    			"CREATE ALIAS geometryN FOR \"org.openplans.spatialDBbox.StaticGeometry.getGeometryN\";" +
    			"CREATE ALIAS x FOR \"org.openplans.spatialDBbox.StaticGeometry.getX\";" +
    			"CREATE ALIAS y FOR \"org.openplans.spatialDBbox.StaticGeometry.getY\";" +
    			"CREATE ALIAS isClosed FOR \"org.openplans.spatialDBbox.StaticGeometry.isClosed\";" +
    			"CREATE ALIAS pointN FOR \"org.openplans.spatialDBbox.StaticGeometry.getPointN\";" +
    			"CREATE ALIAS startPoint FOR \"org.openplans.spatialDBbox.StaticGeometry.getStartPoint\";" +
    			"CREATE ALIAS endPoint FOR \"org.openplans.spatialDBbox.StaticGeometry.getEndPoint\";" +
    			"CREATE ALIAS isRing FOR \"org.openplans.spatialDBbox.StaticGeometry.isRing\";" +
    			"CREATE ALIAS exteriorRing FOR \"org.openplans.spatialDBbox.StaticGeometry.getExteriorRing\";" +
    			"CREATE ALIAS numInteriorRing FOR \"org.openplans.spatialDBbox.StaticGeometry.getNumInteriorRing\";" +
    			"CREATE ALIAS interiorRingN FOR \"org.openplans.spatialDBbox.StaticGeometry.getInteriorRingN\";");
    }
}
