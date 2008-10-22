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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.Diff;
import org.geotools.data.DiffFeatureReader;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FilteringFeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.hsql.fidmapper.HsqlFIDMapperFactory;
import org.geotools.data.jdbc.JDBC1DataStore;
import org.geotools.data.jdbc.JDBCDataStoreConfig;
import org.geotools.data.jdbc.JDBCFeatureWriter;
import org.geotools.data.jdbc.QueryData;
import org.geotools.data.jdbc.SQLBuilder;
import org.geotools.data.jdbc.attributeio.AttributeIO;
import org.geotools.data.jdbc.attributeio.WKTAttributeIO;
import org.geotools.data.jdbc.fidmapper.FIDMapperFactory;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;
import org.geotools.filter.SQLEncoderHsql;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

import com.vividsolutions.jts.geom.Geometry;

/**
 * An implementation of the GeoTools Data Store API for the HSQL database platform.
 * <br>
 * Please see {@link org.geotools.data.jdbc.JDBC1DataStore class JDBC1DataStore} and
 * {@link org.geotools.data.DataStore interface DataStore} for DataStore usage details.
 * 
 * @author Amr Alam, Refractions Research, aalam@refractions.net
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/hsql/src/main/java/org/geotools/data/hsql/HsqlDataStore.java $
 */
public class HsqlDataStore extends JDBC1DataStore implements DataStore {
	/** The logger for the hsql module. */
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.data.hsql");
	private Connection connection;
	private HsqlConnectionFactory hsqlConnFactory;
	private boolean typeTableExists;
    
	/**
     * Basic constructor for HsqlDataStore.  Requires creation of a
     * {@link org.geotools.data.hsql.HsqlConnectionFactory HsqlConnectionFactory}, which could
     * be done similar to the following:<br>
     * <br>
     * <code>HsqlConnectionFactory connectionFactory = new HsqlConnectionFactory("dbFileName", "username", "password");</code><br>
     * <code>DataStore dataStore = new HsqlDataStore(connectionFactory);</code><br>
     * 
     * @param connectionFactory an HSQL {@link org.geotools.data.hsql.HsqlConnectionFactory HsqlConnectionFactory}
     * @throws IOException if the database cannot be properly accessed
     * @see org.geotools.data.hsql.HsqlConnectionFactory
     */
    public HsqlDataStore(HsqlConnectionFactory connectionFactory) throws IOException {
        this(connectionFactory, null);
    }

    protected boolean requireAutoCommit() {
        //hsql is wacky in that it wants to autocommit, but also have a state object?
        return true;
    }

    /**
     * Constructor for HSQLDataStore where the database schema name is provided.
     * @param connectionFactory an HSQL {@link org.geotools.data.hsql.HsqlConnectionFactory HsqlConnectionFactory}
     * @param databaseSchemaName the database schema.  Can be null.  See the comments for the parameter schemaPattern in {@link java.sql.DatabaseMetaData#getTables(String, String, String, String[]) DatabaseMetaData.getTables}, because databaseSchemaName behaves in the same way.
     * @throws IOException if the database cannot be properly accessed
     */
    public HsqlDataStore(HsqlConnectionFactory connectionFactory, String databaseSchemaName)
        throws IOException {
        this(connectionFactory, databaseSchemaName, null);
    }

    /**
     * Constructor for HSQLDataStore where the database schema name is provided.
     * @param connectionFactory an HSQL {@link org.geotools.data.hsql.HsqlConnectionFactory HsqlConnectionFactory}
     * @param databaseSchemaName the database schema.  Can be null.  See the comments for the parameter schemaPattern in {@link java.sql.DatabaseMetaData#getTables(String, String, String, String[]) DatabaseMetaData.getTables}, because databaseSchemaName behaves in the same way.
     * @param namespace the namespace for this data store.  Can be null, in which case the namespace will simply be the schema name.
     * @throws IOException if the database cannot be properly accessed
     */
    public HsqlDataStore(
    	HsqlConnectionFactory connectionFactory,
        String databaseSchemaName,
        String namespace)
        throws IOException {
    	super(JDBCDataStoreConfig.createWithNameSpaceAndSchemaName(namespace, databaseSchemaName));
    	this.hsqlConnFactory = connectionFactory;
    }
    
    /**
     * Provides FeatureReader over the entire contents of <code>typeName</code>.
     * 
     * <p>
     * Implements getFeatureReader contract for AbstractDataStore.
     * </p>
     *
     * @param typeName
     *
     * @return a featureReader
     *
     * @throws IOException If typeName could not be found
     */
    public FeatureReader getFeatureReader(final String typeName)
        throws IOException {
    	SimpleFeatureType featureType = getSchema(typeName);
    	return getFeatureReader(featureType, Filter.INCLUDE, Transaction.AUTO_COMMIT);
    }
    
    
    /**
     * Provides a featureReader over the query results using the given transaction
     * 
     * @param query the Query object we want to narrow the results down by
     * @param transaction the transaction object to be operated on
     * 
     * @return a featureReader based on the given query and transaction
	 * @see org.geotools.data.jdbc.JDBC1DataStore#getFeatureReader(org.geotools.data.Query, org.geotools.data.Transaction)
	 */
	public FeatureReader getFeatureReader(Query query, Transaction transaction) throws IOException {
		FeatureReader reader =  super.getFeatureReader(query, transaction);
		
		if (transaction != Transaction.AUTO_COMMIT) {
			String typeName = query.getTypeName();
			Diff diff = state(transaction).diff(typeName);
            reader = new DiffFeatureReader(reader, diff);
        }
		
		if ((query.getFilter() != null) && (query.getFilter() != Filter.INCLUDE)) {
            reader = new FilteringFeatureReader(reader, query.getFilter());
        }
    	
    	return reader;
	}
    
    HsqlTransactionStateDiff state(Transaction transaction) {
        synchronized (transaction) {
        	HsqlTransactionStateDiff state = (HsqlTransactionStateDiff) transaction
                .getState(this);

            if (state == null) {
                try {
					state = new HsqlTransactionStateDiff(this, createConnection());
	                transaction.putState(this, state);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }

            return state;
        }
    }
    
    /**
     * Override the default FIDMapperFactory since it doesn't work well with HSQL
     * 
     * @see org.geotools.data.jdbc.JDBCDataStore#buildFIDMapperFactory(org.geotools.data.jdbc.JDBCDataStoreConfig)
     */
    protected FIDMapperFactory buildFIDMapperFactory(JDBCDataStoreConfig config) {
        return new HsqlFIDMapperFactory();
    }
    
    
    /**
     * Convenience method to add feature to the datastore
     * 
     * @param features an array of features that should be added to the datastore
     * @throws IOException
     *
    public void addFeatures(final Feature[] features) throws IOException {
    	if( features.length == 0 ) return;
    	
		FeatureStore fs = (FeatureStore)getFeatureSource(features[0].getFeatureType().getTypeName());
	    fs.addFeatures( DataUtilities.collection( features ));
    }*/

    /**
     * Utility method for getting a FeatureWriter for modifying existing features,
     * using no feature filtering and auto-committing.  Not used for adding new
     * features.
     * @param typeName the feature type name (the table name)
     * @return a FeatureWriter for modifying existing features
     * @throws IOException if the database cannot be properly accessed
     */
    public FeatureWriter getFeatureWriter(String typeName) throws IOException {
        return getFeatureWriter(typeName, Filter.INCLUDE, Transaction.AUTO_COMMIT);
    }
    
    /**
     * Acquire FeatureWriter for modification of contents specifed by filter.
     * 
     * @param typeName
     * @param filter
     * @param transaction
     *
     *
     * @throws IOException If typeName could not be located
     * @throws NullPointerException If the provided filter is null
     *
     * @see org.geotools.data.DataStore#getFeatureWriter(java.lang.String,
     *      org.geotools.filter.Filter, org.geotools.data.Transaction,
     *      org.geotools.data.jdbc.JDBC1DataStore#getFeatureWriter)
     */
    public FeatureWriter getFeatureWriter(String typeName, Filter filter,
        Transaction transaction) throws IOException {
        FeatureWriter writer;

        if (transaction == null) {
            throw new NullPointerException(
                "getFeatureWriter requires Transaction: "
                + "did you mean to use Transaction.AUTO_COMMIT?");
        }

        if (transaction == Transaction.AUTO_COMMIT) {
            writer = super.getFeatureWriter(typeName, filter, transaction);
        } else {
            writer = state(transaction).writer(typeName, filter);
        }
        
        return writer;
    }

    /**
     * Utility method for getting a FeatureWriter for adding new features, using
     * auto-committing.  Not used for modifying existing features.
     * @param typeName the feature type name (the table name)
     * @return a FeatureWriter for adding new features
     * @throws IOException if the database cannot be properly accessed
     */
    public FeatureWriter getFeatureWriterAppend(String typeName) throws IOException {
        return getFeatureWriterAppend(typeName, Transaction.AUTO_COMMIT);
    }

    /**
     * Constructs an AttributeType from a row in a ResultSet. The ResultSet
     * contains the information retrieved by a call to  getColumns() on the
     * DatabaseMetaData object.  This information  can be used to construct an
     * Attribute Type.
     * 
     * <p>
     * In addition to standard SQL types, this method identifies MySQL 4.1's geometric
     * datatypes and creates attribute types accordingly.  This happens when the
     * datatype, identified by column 5 of the ResultSet parameter, is equal to
     * java.sql.Types.OTHER.  If a Types.OTHER ends up not being geometric, this
     * method simply calls the parent class's buildAttributeType method to do something
     * with it.
     * </p>
     * 
     * <p>
     * Note: Overriding methods must never move the current row pointer in the
     * result set.
     * </p>
     *
     * @param rs The ResultSet containing the result of a
     *        DatabaseMetaData.getColumns call.
     *
     * @return The AttributeType built from the ResultSet.
     *
     * @throws SQLException If an error occurs processing the ResultSet.
     * @throws DataSourceException Provided for overriding classes to wrap
     *         exceptions caused by other operations they may perform to
     *         determine additional types.  This will only be thrown by the
     *         default implementation if a type is present that is not present
     *         in the TYPE_MAPPINGS.
     */
    protected AttributeDescriptor buildAttributeType(ResultSet rs) throws IOException {
        final int COLUMN_NAME = 4;
        final int TABLE_NAME = 3; //Position of table name in the ResultSet

        try {
            String tableName = rs.getString(TABLE_NAME);
            String type = findType(tableName, rs.getString(COLUMN_NAME));
            
            return new AttributeTypeBuilder().binding( Class.forName(type)).buildDescriptor( rs.getString("COLUMN_NAME"));
        } catch (SQLException e) {
            throw new IOException("SQL exception occurred: " + e.getMessage());
        } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return super.buildAttributeType(rs);
    }

	 /**
     * @see org.geotools.data.jdbc.JDBC1DataStore#getSqlBuilder(java.lang.String)
     */
    public SQLBuilder getSqlBuilder(String typeName) throws IOException {
		SQLEncoderHsql encoder = new SQLEncoderHsql(); 
        encoder.setFIDMapper(getFIDMapper(typeName));
        return new HsqlSQLBuilder(encoder, getSchema(typeName));
    }

    /**
     * @see org.geotools.data.jdbc.JDBC1DataStore#getGeometryAttributeIO(org.geotools.feature.AttributeType)
     */
    protected AttributeIO getGeometryAttributeIO(AttributeDescriptor type, QueryData queryData) {
        return new WKTAttributeIO();
    }

    /**
     * @see org.geotools.data.jdbc.JDBC1DataStore#createFeatureWriter(org.geotools.data.FeatureReader, org.geotools.data.jdbc.QueryData)
     */
    protected JDBCFeatureWriter createFeatureWriter(FeatureReader reader, QueryData queryData)
        throws IOException {
        LOGGER.fine("returning jdbc feature writer");

        return new HsqlFeatureWriter(reader, queryData);
    }

	/**
	 * @see org.geotools.data.jdbc.JDBC1DataStore#createConnection()
	 */
	protected Connection createConnection() throws SQLException {
		//HsqlConnectionFactory hcf = new HsqlConnectionFactory();
		if( connection == null || ( connection.isClosed() && hsqlConnFactory != null )) {
			connection = hsqlConnFactory.getConnection();
			return connection;
		}
		return connection;
	}
	    
	/**
     * Gets a connection for the provided transaction.
     *
     * @param transaction
     * @return A single use connection.
     *
     * @throws IOException
     * @throws DataSourceException If the connection can not be obtained.
     */
    public Connection getConnection(Transaction transaction)
        throws IOException {
        if (transaction != Transaction.AUTO_COMMIT) {
            // we will need to save a JDBC connection is
            // transaction.putState( connectionPool, JDBCState )
            //throw new UnsupportedOperationException("Transactions not supported yet");
        	HsqlTransactionStateDiff state = (HsqlTransactionStateDiff) transaction
                .getState( this );

            if (state == null) {
            	try {
            		state = new HsqlTransactionStateDiff( this, createConnection() );
            		transaction.putState( this, state);
            	}
            	catch( SQLException eep ){
            		throw new DataSourceException("Connection failed:"+eep, eep );
            	}
            }
            return state.getConnection();
        }

        try {
            if( connection==null || connection.isClosed() )
                return createConnection();
            else
                return connection;
        } catch (SQLException sqle) {
            throw new DataSourceException("Connection failed:" + sqle, sqle);
        }
    }
	
	/**
     * Adds support for a new featureType to HsqlDataStore.
     * 
     * <p>
     * FeatureTypes are stored by typeName (in this case, table name = typeName), 
     * an IOException will be thrown if the requested typeName
     * is already in use.
     * </p>
     *
     * @param featureType FeatureType to be added
     *
     * @throws IOException If featureType already exists
     *
     * @see org.geotools.data.DataStore#createSchema(org.geotools.feature.FeatureType)
     */
	public void createSchema(SimpleFeatureType featureType) throws IOException {
		String typeName = featureType.getTypeName();
		String namespace = featureType.getName().getNamespaceURI();
		String colName = null;
		Class colClass = null;
		String colType = null;
		
		List<AttributeDescriptor> atts = featureType.getAttributes();
		try {
			createConnection();
			Statement st = connection.createStatement();
		    String sql = "CREATE CACHED TABLE " + typeName + "( ";
			
		    //Add fid column right at the start...auto-increment PK
		    sql += "_FID INTEGER IDENTITY";
		    addTypeTable(typeName, namespace, "_FID", "java.lang.Integer");
		    
			for( int i = 0; i < atts.size(); i++ ) {
				//if( i != 0 ) 
				sql += ",";
				colName = atts.get(i).getName().getLocalPart();
				colClass = atts.get(i).getType().getBinding();
				if (colClass.isAssignableFrom(int.class) 
						|| colClass.isAssignableFrom(Integer.class)) {
					colType = "integer";
				} else if (colClass.isAssignableFrom(String.class)) {
					colType = "varchar";
				} else if (colClass.isAssignableFrom(double.class) 
						|| colClass.isAssignableFrom(Double.class)) {
					colType = "double";
				} else if (colClass.isAssignableFrom(Geometry.class)) {
					colType = "varchar";
				} else if (Geometry.class.isAssignableFrom(colClass)) {
					colType = "varchar";
				}
				sql += " " + colName + " " + colType;
				addTypeTable(typeName, namespace, atts.get(i));
			}
		    sql += " )";
		    st.execute(sql);
            typeHandler.forceRefresh();
		} catch (SQLException e) {
			// Attempted to re-create typeTable table...OK
		}
	}
    
    protected boolean allowTable( String tablename ) {
        return !tablename.equalsIgnoreCase("TYPETABLE");
    }
	
	/**
     * Removes support for the featureType schema to HsqlDataStore. (Drops the table)
     * 
     * <p>
     * FeatureTypes are stored by typeName (in this case, table name = typeName).
     * </p>
     *
     * @param featureType FeatureType to be removed
     */
	public void removeSchema(SimpleFeatureType featureType) {
		String typeName = featureType.getTypeName();
		
		try {
			createConnection();
			Statement st = connection.createStatement();
		    String sql = "DROP TABLE " + typeName;
		    st.execute(sql);
		    
		} catch (SQLException e) {
			//e.printStackTrace();
			LOGGER.fine("Table does not exist.");
		}
	}
	
	private String findType(String typeName, String columnName) {
		try {
			if( connection == null )
				createConnection();

			Statement st = connection.createStatement();
		    String sql = "SELECT typeName, columnName, class FROM typeTable "
		    						+ "WHERE TYPENAME = '" + typeName
		    						+ "' AND COLUMNNAME = '" + columnName 
		    						+ "'";
			
		    ResultSet rs = st.executeQuery(sql);
		    rs.next();
		    String type = rs.getString(3);
		    return type;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * Adds the given attribute with it's type into the typeTable
	 * 
	 * @param typeName the featureType name
	 * @param namespace the featureType namespace
	 * @param attribute the attribute we want to store info about
	 */
	private void addTypeTable(String typeName, String namespace, AttributeDescriptor attribute) {
		addTypeTable(typeName, namespace, 
				attribute.getName().getLocalPart().toUpperCase(), attribute.getType().getName().getLocalPart());
	}
	
	/**
	 * Adds the given attribute with it's type into the typeTable
	 * 
	 * @param typeName the featureType name
	 * @param namespace the featureType namespace
	 * @param name the entry's name
	 * @param type the entry's class
	 */
	private void addTypeTable(String typeName, String namespace, String name, String type) {
		try {
			//Might want to add the CRS into the type table...
//			FeatureType featureType;
//			int SRID = -1;
//			try {
//				featureType = getSchema(typeName);
//				CoordinateReferenceSystem refSys = featureType.getDefaultGeometry().getCoordinateSystem();
//
//				// so for now we just use -1
//				if (refSys != null) {
//					SRID = -1;
//				} else {
//					SRID = -1;
//				}
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			
			if( connection == null )
				createConnection();
			createTypeTable();
			Statement st = connection.createStatement();
		    String sql = "INSERT INTO typeTable (typeName, namespace, columnName, class) "
		    						+ "VALUES( "
		    						+ "'" + typeName.toUpperCase() + "', "
		    						+ "'" + namespace + "', "
		    						+ "'" + name + "', "
		    						+ "'" + type + "'"
		    						+ ")";
			
		    st.execute(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
//			System.out.println("INSERTING into typtTable failed due to duplicates...OK");
		}
	}
	
	//Will this be needed at some point?
//	private void removeTypeTable() {
//		if(!typeTableExists) return;
//		try {
//			if( connection == null )
//				createConnection();
//			Statement st = connection.createStatement();
//		    String sql = "DROP TABLE typeTable";
//			
//		    st.execute(sql);
//		    typeTableExists = false;
//		    
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	private void createTypeTable() {
		if(typeTableExists) return;
		try {
			if( connection == null )
				createConnection();
			typeTableExists = true;
			Statement st = connection.createStatement();
			String sql = "CREATE CACHED TABLE typeTable( "
		    		+ "typeName varchar, namespace varchar, columnName varchar, "
		    		+ "class varchar, encoding varchar, srid integer, "
		    		+ "PRIMARY KEY(typeName, namespace, columnName))";
			
		    st.execute(sql);
		    
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
//			System.out.println("Attempted to re-create typeTable table...OK");
		}
	}
    
	protected void setAutoCommit(boolean arg0, Connection arg1) throws SQLException {
		// do nothing
	}
	
}
