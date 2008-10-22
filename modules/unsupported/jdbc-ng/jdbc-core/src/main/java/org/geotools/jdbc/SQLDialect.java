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
package org.geotools.jdbc;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.jdbc.FilterToSQL;
import org.geotools.filter.FilterCapabilities;
import org.geotools.referencing.CRS;
import org.geotools.util.Converters;
import org.geotools.util.logging.Logging;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.Id;
import org.opengis.filter.IncludeFilter;
import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.PropertyIsLike;
import org.opengis.filter.PropertyIsNull;
import org.opengis.filter.expression.Add;
import org.opengis.filter.expression.Divide;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.Multiply;
import org.opengis.filter.expression.Subtract;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;


/**
 * The driver used by JDBCDataStore to directly communicate with the database.
 * <p>
 * This class encapsulates all the database specific operations that JDBCDataStore
 * needs to function. It is implemented on a per-database basis.
 * </p>
 * <p>
 *  <h3>Type Mapping</h3>
 * One of the jobs of a dialect is to map sql types to java types and vice
 * versa. This abstract implementation provides default mappings for "primitive"
 * java types. The following mappings are provided. A '*' denotes that the
 * mapping is the default java to sql mapping as well.
 * <ul>
 *   <li>VARCHAR -> String *
 *   <li>CHAR -> String
 *   <li>LONGVARCHAR -> String
 *   <li>BIT -> Boolean
 *   <li>BOOLEAN -> Boolean *
 *   <li>SMALLINT -> Short *
 *   <li>TINYINT -> Short
 *   <li>INTEGER -> Integer *
 *   <li>BIGINT -> Long *
 *   <li>REAL -> Float *
 *   <li>DOUBLE -> Double *
 *   <li>FLOAT -> Double
 *   <li>NUMERIC -> BigDecimal *
 *   <li>DECIMAL -> BigDecimal
 *   <li>DATE -> java.sql.Date *
 *   <li>TIME -> java.sql.Time *
 *   <li>TIMESTAMP -> java.sql.Timestmap *
 * </ul>
 * Subclasses should <b>extend</b> (not override) the following methods to
 * configure the mappings:
 * <ul>
 *   <li>{@link #registerSqlTypeToClassMappings(Map)}
 *   <li>{@link #registerSqlTypeNameToClassMappings(Map)}
 *   <li>{@link #registerClassToSqlMappings(Map)}
 * </ul>
 * </p>
 * <p>
 *
 * </p>
 * <p>
 * This class is intended to be stateless, therefore subclasses should not
 * maintain any internal state. If for some reason a subclass must keep some
 * state around (not recommended), it must ensure that the state is accessed in
 * a thread safe manner.
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public abstract class SQLDialect {
    protected static final Logger LOGGER = Logging.getLogger(SQLDialect.class);
    
    /**
     * The basic filter capabilities all databases should have
     */
    public static FilterCapabilities BASE_DBMS_CAPABILITIES = new FilterCapabilities() {
        {
            addAll(FilterCapabilities.LOGICAL_OPENGIS);
            addAll(FilterCapabilities.SIMPLE_COMPARISONS_OPENGIS);
            
            //simple arithmetic
            addType(Add.class);
            addType(Subtract.class);
            addType(Multiply.class);
            addType(Divide.class);
            
            //simple comparisons
            addType(PropertyIsNull.class);
            addType(PropertyIsBetween.class);
            addType(Id.class);
            addType(IncludeFilter.class);
            addType(ExcludeFilter.class);
            addType(PropertyIsLike.class);
        }
    };
    
    /**
     * The datastore using the dialect
     */
    protected JDBCDataStore dataStore;
    
    /**
     * Creates the dialect.
     * @param dataStore The dataStore using the dialect.
     */
    protected SQLDialect( JDBCDataStore dataStore ) {
        this.dataStore = dataStore;
    }
    
    /**
     * Determines if the specified table should be included in those published
     * by the datastore.
     * <p>
     * This method returns <code>true</code> if the table should be published as
     * a feature type, otherwise it returns <code>false</code>. Subclasses should
     * override this method, this default implementation returns <code>true<code>.
     * </p>
     * <p>
     * A database connection is provided to the dialect but it should not be closed.
     * However any statements objects or result sets that are instantiated from it
     * must be closed.
     * </p>
     * @param tableName The name of the table.
     * @param cx Database connection.
     *
     */
    public boolean includeTable(String tableName, Connection cx)
        throws SQLException {
        return true;
    }

    /**
     * Registers the sql type name to java type mappings that the dialect uses when
     * reading and writing objects to and from the database.
     * <p>
     * Subclasses should extend (not override) this method to provide additional
     * mappings, or to override mappings provided by this implementation. This
     * implementation provides the following mappings:
     * </p>
     */
    public void registerSqlTypeNameToClassMappings(Map<String, Class<?>> mappings) {
        //TODO: do the normal types
    }

    /**
     * Determines the class mapping for a particular column of a table.
     * <p>
     * Implementing this method is optional. It is used to allow database to
     * perform custom type mappings based on various column metadata. It is called
     * before the mappings registered in {@link #registerSqlTypeToClassMappings(Map)}
     * and {@link #registerSqlTypeNameToClassMappings(Map) are used to determine
     * the mapping. Subclasses should implement as needed, this default implementation
     * returns <code>null</code>.
     * </p>
     * <p>
     * The <tt>columnMetaData</tt> argument is provided from
     * {@link DatabaseMetaData#getColumns(String, String, String, String)}.
     * </p>
     * @param columnMetaData The column metadata
     * @param The connection used to retrieve the metadata
     * @return The class mapped to the to column, or <code>null</code>.
     */
    public Class<?> getMapping(ResultSet columnMetaData, Connection cx)
        throws SQLException {
        return null;
    }

    /**
     * Registers the sql type to java type mappings that the dialect uses when
     * reading and writing objects to and from the database.
     * <p>
     * Subclasses should extend (not override) this method to provide additional
     * mappings, or to override mappings provided by this implementation. This
     * implementation provides the following mappings:
     * </p>
     *
     */
    public void registerSqlTypeToClassMappings(Map<Integer, Class<?>> mappings) {
        mappings.put(new Integer(Types.VARCHAR), String.class);
        mappings.put(new Integer(Types.CHAR), String.class);
        mappings.put(new Integer(Types.LONGVARCHAR), String.class);

        mappings.put(new Integer(Types.BIT), Boolean.class);
        mappings.put(new Integer(Types.BOOLEAN), Boolean.class);

        mappings.put(new Integer(Types.TINYINT), Short.class);
        mappings.put(new Integer(Types.SMALLINT), Short.class);

        mappings.put(new Integer(Types.INTEGER), Integer.class);
        mappings.put(new Integer(Types.BIGINT), Long.class);

        mappings.put(new Integer(Types.REAL), Float.class);
        mappings.put(new Integer(Types.FLOAT), Double.class);
        mappings.put(new Integer(Types.DOUBLE), Double.class);

        mappings.put(new Integer(Types.DECIMAL), BigDecimal.class);
        mappings.put(new Integer(Types.NUMERIC), BigDecimal.class);

        mappings.put(new Integer(Types.DATE), Date.class);
        mappings.put(new Integer(Types.TIME), Time.class);
        mappings.put(new Integer(Types.TIMESTAMP), Timestamp.class);

        //subclasses should extend to provide additional
    }

    /**
     * Registers the java type to sql type mappings that the datastore uses when
     * reading and writing objects to and from the database.
     * * <p>
     * Subclasses should extend (not override) this method to provide additional
     * mappings, or to override mappings provided by this implementation. This
     * implementation provides the following mappings:
     * </p>
     */
    public void registerClassToSqlMappings(Map<Class<?>, Integer> mappings) {
        mappings.put(String.class, new Integer(Types.VARCHAR));

        mappings.put(Boolean.class, new Integer(Types.BOOLEAN));

        mappings.put(Short.class, new Integer(Types.SMALLINT));

        mappings.put(Integer.class, new Integer(Types.INTEGER));
        mappings.put(Long.class, new Integer(Types.BIGINT));

        mappings.put(Float.class, new Integer(Types.REAL));
        mappings.put(Double.class, new Integer(Types.DOUBLE));

        mappings.put(BigDecimal.class, new Integer(Types.NUMERIC));

        mappings.put(Date.class, new Integer(Types.DATE));
        mappings.put(Time.class, new Integer(Types.TIME));
        mappings.put(java.util.Date.class, new Integer(Types.TIMESTAMP));
        mappings.put(Timestamp.class, new Integer(Types.TIMESTAMP));

        //subclasses should extend and provide additional
    }

    /**
     * Returns the java class mapping for a particular column.
     * <p>
     * This method is used as a "last resort" when the mappings specified by the
     * dialect in the {@link #registerSqlTypeToClassMappings(Map)}" method fail
     * to yield a java type.
     * </p>
     * <p>
     * The most common case is for databases which store all geometric values under
     * a single type, and use some secondary means to store the specific type
     * (like a metadata table).
     * </p>
     *  * <p>
     * This method is given a direct connection to the database. The connection
     * must not be closed. However any statements or result sets instantiated
     * from the connection must be closed.
     * </p>
     * <p>
     * In the event that the mapping cannot be determined, this method should return
     * <code>null</code>.
     * </p>
     * @param schemaName The schema name, may be <code>null</code>.
     * @param tableName The table name.
     * @param columnName The column name.
     * @param type The data type from {@link Types}, reported by database metadata.
     * @param cx The database connection.
     *
     * @return The mapped type of the column, or <code>null</code> if it can not
     * be inferred.
     */

    //    public final Class getMapping( String schemaName, String tableName, String columnName, Integer type, Connection cx )
    //        throws SQLException {
    //        return null;
    //    }

    /**
     * Returns the string used to escape names.
     * <p>
     * This value is used to escape any name in a query. This includes columns,
     * tables, schemas, indexes, etc... If no escape is necessary this method
     * should return the empty string, and never return <code>null</code>.
     * </p>
     * <p>
     * This default implementation returns a single double quote ("), subclasses
     * must override to provide a different espcape.
     * </p>
     */
    public String getNameEscape() {
        return "\"";
    }

    /**
     * Quick accessor for {@link #getNameEscape()}.
     */
    protected final String ne() {
        return getNameEscape();
    }

    /**
     * Encodes the name of a column in an SQL statement.
     * <p>
     * This method wraps <tt>raw</tt> in the character provided by
     * {@link #getNameEscape()}. Subclasses usually dont override this method
     * and instead override {@link #getNameEscape()}.
     * </p>
     */
    public void encodeColumnName(String raw, StringBuffer sql) {
        sql.append(ne()).append(raw).append(ne());
    }

    /**
     * Encodes the type of a column in an SQL CREATE TABLE statement.
     * <p>
     * The default implementation simply outputs the <tt>sqlTypeName</tt> argument
     * as is. Subclasses may override this method. Such cases might include:
     * <ul>
     *   <li>A type definition requires some parameter, ex: size of a varchar
     *   <li>The provided attribute (<tt>att</tt>) contains some additional
     *   restrictions that can be encoded in the type, ex: field length
     * </ul>
     * </p>
     * @param sqlTypeName
     * @param sql
     */
    public void encodeColumnType(String sqlTypeName, StringBuffer sql) {
        sql.append(sqlTypeName);
    }

    /**
     * Encodes the alias of a column in an sql query.
     * <p>
     * This default implementation uses the syntax: <pre>as "alias"</pre>.
     * Subclasses should override to provide a different syntax.
     * </p>
     */
    public void encodeColumnAlias(String raw, StringBuffer sql) {
        sql.append(" as ");
        encodeColumnName(raw, sql);
    }

    /**
     * Encodes the name of a table in an SQL statement.
     * <p>
     * This method wraps <tt>raw</tt> in the character provided by
     * {@link #getNameEscape()}. Subclasses usually dont override this method
     * and instead override {@link #getNameEscape()}.
     * </p>
     */
    public void encodeTableName(String raw, StringBuffer sql) {
        sql.append(ne()).append(raw).append(ne());
    }

    /**
     * Encodes the name of a schema in an SQL statement.
     * <p>
     * This method wraps <tt>raw</tt> in the character provided by
     * {@link #getNameEscape()}. Subclasses usually dont override this method
     * and instead override {@link #getNameEscape()}.
     * </p>
     */
    public void encodeSchemaName(String raw, StringBuffer sql) {
        sql.append(ne()).append(raw).append(ne());
    }

    /**
     * Returns the name of a geometric type based on its integer constant.
     * <p>
     * The constant, <tt>type</tt>, is registered in {@link #registerSqlTypeNameToClassMappings(Map)}.
     * </p>
     * <p>
     * This default implementation returns <code>null</code>, subclasses should
     * override.
     * </p>
     */
    public String getGeometryTypeName(Integer type) {
        return null;
    }

    /**
     * Returns the spatial reference system identifier (srid) for a particular
     * geometry column.
     * <p>
     * This method is given a direct connection to the database. The connection
     * must not be closed. However any statements or result sets instantiated
     * from the connection must be closed.
     * </p>
     * <p>
     * In the event that the srid cannot be determined, this method should return
     * <code>null</code>.
     * </p>
     * @param schemaName The database schema, could be <code>null</code>.
     * @param tableName The table, never <code>null</code>.
     * @param columnName The column name, never <code>null</code>
     * @param cx The database connection.
     */
    public Integer getGeometrySRID(String schemaName, String tableName, String columnName,
        Connection cx) throws SQLException {
        return null;
    }
    
    /**
     * Turns the specified srid into a {@link CoordinateReferenceSystem}, or returns <code>null</code> if not possible.
     * The implementation might just use <code>CRS.decode("EPSG:" + srid)</code>, but most spatial databases will have 
     * their own SRS database that can be queried as well.
     * @param srid
     * @return
     */
    public CoordinateReferenceSystem createCRS(int srid, Connection cx) throws SQLException {
        try {
            return CRS.decode("EPSG:" + srid);
        } catch(Exception e) {
            if(LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Could not decode " + srid + " using the built-in EPSG database", e);
            }
            return null;
        }
    }

    /**
     * Encodes the spatial extent function of a geometry column in a SELECT statement.
     * <p>
     * This method must also be sure to properly encode the name of the column
     * with the {@link #encodeColumnName(String, StringBuffer)} function.
     * </p>
     */
    public abstract void encodeGeometryEnvelope(String geometryColumn, StringBuffer sql);

    /**
     * Decodes the result of a spatial extent function in a SELECT statement.
     * <p>
     * This method is given direct access to a result set. The <tt>column</tt>
     * parameter is the index into the result set which contains the spatial
     * extent value. The query for this value is build with the {@link #encodeGeometryEnvelope(String, StringBuffer)}
     * method.
     * </p>
     * <p>
     * This method must not read any other objects from the result set other then
     * the one referenced by <tt>column</tt>.
     * </p>
     * @param rs A result set
     * @param column Index into the result set which points at the spatial extent
     * value.
     * @param The database connection.
     */
    public abstract Envelope decodeGeometryEnvelope(ResultSet rs, int column, Connection cx )
        throws SQLException, IOException;

    /**
     * Encodes the name of a geometry column in a SELECT statement.
     * <p>
     * This method should wrap the column name in any functions that are used to
     * retrieve its value. For instance, often it is necessary to use the function
     * <code>asText</code>, or <code>asWKB</code> when fetching a geometry.
     * </p>
     * <p>
     * This method must also be sure to properly encode the name of the column
     * with the {@link #encodeColumnName(String, StringBuffer)} function.
     * </p>
     * <p>
     * Example:
     * </p>
     * <pre>
     *   <code>
     *   sql.append( "asText(" );
     *   column( gatt.getLocalName(), sql );
     *   sql.append( ")" );
     *   </code>
     * </pre>
     * </p>
     * <p>
     * This default implementation simply uses the column name without any
     * wrapping function, subclasses must override.
     * </p>
     */
    public void encodeGeometryColumn(GeometryDescriptor gatt, int srid, StringBuffer sql) {
        encodeColumnName(gatt.getLocalName(), sql);
    }

    /**
     * Encodes a geometry value in an sql statement.
     * <p>
     * An implementations should serialize <tt>value</tt> into some exchange
     * format which will then be transported to the underlying database. For
     * example, consider an implementation which converts a geometry into its
     * well known text representation:
     * <pre>
     *   <code>
     *   sql.append( "GeomFromText('" );
     *   sql.append( new WKTWriter().write( value ) );
     *   sql.append( ")" );
     *   </code>
     *  </pre>
     * </p>
     * <p>
     *  The <tt>srid</tt> parameter is the spatial reference system identifier
     *  of the geometry, or 0 if not known.
     * </p>
     */
    public abstract void encodeGeometryValue(Geometry value, int srid, StringBuffer sql)
        throws IOException;
    
    /**
     * Allows the dialect to specify the placeholder for a geometry value in a INSERT or UPDATE
     * statement. The default implementation simply appends <code>?</code> to the sql buffer,
     * but specialized dialects might override to call decoding functions, perform casts,
     * force the srid and so on.
     * @param descriptor
     * @param srid
     * @param sql
     */
    public void prepareGeometryValue(Geometry geom, int srid, Class binding, StringBuffer sql) {
        sql.append("?");
    }

    /**
     * Sets the geometry value into the prepared statement. 
     * @param g The geometry
     * @param srid the geometry native srid (should be forced into the encoded geometry)
     * @param binding the geometry type
     * @param ps the prepared statement
     * @param column the column index where the geometry is to be set
     * @throws SQLException
     */
    public abstract void setGeometryValue(Geometry g, int srid,
            Class binding, PreparedStatement ps, int column) throws SQLException;
    
    /**
     * Decodes a geometry value from the result of a query.
     * <p>
     * This method is given direct access to a result set. The <tt>column</tt>
     * parameter is the index into the result set which contains the geometric
     * value.
     * </p>
     * <p>
     * An implementation should deserialize the value provided by the result
     * set into {@link Geometry} object. For example, consider an implementation
     * which deserializes from well known text:
     * <code>
     *   <pre>
     *   String wkt = rs.getString( column );
     *   if ( wkt == null ) {
     *     return null;
     *   }
     *   return new WKTReader(factory).read( wkt );
     *   </pre>
     * </code>
     * Note that implementations must handle <code>null</code> values.
     * </p>
     * <p>
     * The <tt>factory</tt> parameter should be used to instantiate any geometry
     * objects.
     * </p>
     */
    public abstract Geometry decodeGeometryValue(GeometryDescriptor descriptor, ResultSet rs,
        String column, GeometryFactory factory, Connection cx ) throws IOException, SQLException;
    /**
     * Decodes a geometry value from the result of a query specifying the column 
     * as an index.
     * <p>
     * See {@link #decodeGeometryValue(GeometryDescriptor, ResultSet, String, GeometryFactory)} 
     * for a more in depth description.
     * </p>
     * @see {@link #decodeGeometryValue(GeometryDescriptor, ResultSet, String, GeometryFactory)}.
     */
    public final Geometry decodeGeometryValue(GeometryDescriptor descriptor, ResultSet rs,
        int column, GeometryFactory factory, Connection cx ) throws IOException, SQLException {
        
        String columnName = rs.getMetaData().getColumnName( column );
        return decodeGeometryValue(descriptor, rs, columnName, factory, cx);
    }
    
    /**
     * Encodes the primary key definition in a CREATE TABLE statement.
     * <p>
     * Subclasses should override this method if need be, the default implementation does the 
     * following:
     * <pre>
     *   <code>
     *   encodeColumnName( column, sql );
     *   sql.append( " int PRIMARY KEY" );
     *   </code>
     * </pre>
     * </p>
     *
     */
    public void encodePrimaryKey(String column, StringBuffer sql) {
        encodeColumnName( column, sql );
        sql.append( " INTEGER PRIMARY KEY" );
    }

    /**
     * Encodes anything post a CREATE TABLE statement.
     * <p>
     * This is appended to a CREATE TABLE statement after the column definitions.
     * This default implementation does nothing, subclasses should override as
     * need be.
     * </p>
     */
    public void encodePostCreateTable(String tableName, StringBuffer sql) {
    }

    /**
     * Callback to execute any additional sql statements post a create table
     * statement.
     * <p>
     * This method should be implemented by subclasses that need to do some post
     * processing on the database after a table has been created. Examples might
     * include:
     * <ul>
     *   <li>Creating a sequence for a primary key
     *   <li>Registering geometry column metadata
     *   <li>Creating a spatial index
     * </ul>
     * </p>
     * <p>
     * A common case is creating an auto incrementing sequence for the primary
     * key of a table. It should be noted that all tables created through the
     * datastore use the column "fid" as the primary key.
     * </p>
     * <p>
     * A direct connection to the database is provided (<tt>cx</tt>). This
     * connection must not be closed, however any statements or result sets
     * instantiated from the connection must be closed.
     * </p>
     * @param schemaName The name of the schema, may be <code>null</code>.
     * @param featureType The feature type that has just been created on the database. 
     * @param cx Database connection.
     *
     */
    public void postCreateTable(String schemaName, SimpleFeatureType featureType, Connection cx)
        throws SQLException {
    }

    /**
     * Encodes a value in an sql statement.
     * <p>
     * Subclasses may wish to override or extend this method to handle specific
     * types. This default implementation does the following:
     * <ol>
     *   <li>The <tt>value</tt> is encoded via its {@link #toString()} representation.
     *   <li>If <tt>type</tt> is a character type (extends {@link CharSequence}),
     *   it is wrapped in single quotes (').
     * </ol>
     * </p>
     *
     */
    public void encodeValue(Object value, Class type, StringBuffer sql) {
        
        //turn the value into a literal and use FilterToSQL to encode it
        Literal literal = dataStore.getFilterFactory().literal( value );
        FilterToSQL filterToSQL = dataStore.createFilterToSQL(null);
        
        StringWriter w = new StringWriter();
        filterToSQL.setWriter(w);
        
        filterToSQL.visit(literal,type);
        
        sql.append( w.getBuffer().toString() );
//        if (CharSequence.class.isAssignableFrom(type)) {
//            sql.append("'").append(value).append("'");
//        } else {
//            sql.append(value);
//        }
    }
    
    public void setValue(Object value, Class binding, PreparedStatement ps,
            int column, Connection cx) throws SQLException {
        
        //get the sql type
        Integer sqlType = dataStore.getMapping( binding );
        
        //handl null case
        if ( value == null ) {
            ps.setNull( column, sqlType );
            return;
        }
        
        //convert the value if necessary
        if ( ! binding.isInstance( value ) ) {
            Object converted = Converters.convert(value, binding);
            if ( converted != null ) {
                value = converted;
            }
            else {
                dataStore.getLogger().warning( "Unable to convert " + value + " to " + binding.getName() );
            }
        }
        
        switch( sqlType ) {
            case Types.VARCHAR:
                ps.setString( column, (String) value );
                break;
            case Types.BOOLEAN:
                ps.setBoolean( column, (Boolean) value );
                break;
            case Types.SMALLINT:
                ps.setShort( column, (Short) value );
                break;
            case Types.INTEGER:
                ps.setInt( column, (Integer) value );
                break;
            case Types.BIGINT:
                ps.setLong( column, (Long) value );
                break;
            case Types.REAL:
                ps.setFloat( column, (Float) value );
                break;
            case Types.DOUBLE:
                ps.setDouble( column, (Double) value );
                break;
            case Types.NUMERIC:
                ps.setBigDecimal( column, (BigDecimal) value );
                break;
            case Types.DATE:
                ps.setDate( column, (Date) value );
                break;
            case Types.TIME:
                ps.setTime( column, (Time) value );
                break;
            case Types.TIMESTAMP:
                ps.setTimestamp( column, (Timestamp) value );
                break;
            default:
                ps.setObject( column, value );
        }
        
    }

    /**
     * Obtains the next value of the primary key of a column.
     * <p>
     * Implementations should determine the next value of a column for which 
     * values are automatically generated by the database.
     * </p>
     * <p>
     * This method is given a direct connection to the database, but this connection
     * should never be closed. However any statements or result sets instantiated
     * from the connection must be closed.
     * </p>
     * <p>
     * Implementations should handle the case where <tt>schemaName</tt> is <code>null</code>.
     * </p>
     * @param schemaName The schema name, this might be <code>null</code>.
     * @param tableName The name of the table.
     * @param columnName The column.
     * @param cx The database connection.
     *
     * @return The next value of the column, or <code>null</code>.
     */
    public Object getNextAutoGeneratedValue(String schemaName, String tableName,
        String columnName, Connection cx) throws SQLException {
        return null;
    }

    /**
     * Determines the name of the sequence (if any) which is used to increment
     * generate values for a table column.
     * <p>
     * This method should return null if no such sequence exists.
     * </p>
     * <p>
     * This method is given a direct connection to the database, but this connection
     * should never be closed. However any statements or result sets instantiated
     * from the connection must be closed.
     * </p>
     * @param schemaName The schema name, this might be <code>null</code>.
     * @param tableName The table name.
     * @param columnName The column name.
     * @param cx The database connection.
     * 
     */
    public String getSequenceForColumn(String schemaName, String tableName, String columnName,
            Connection cx) throws SQLException {
        return null;
    }
    
    /**
     * Obtains the next value of a sequence, incrementing the sequence to the next state in the 
     * process.
     * <p>
     * Implementations should determine the next value of a column for which 
     * values are automatically generated by the database.
     * </p>
     * <p>
     * This method is given a direct connection to the database, but this connection
     * should never be closed. However any statements or result sets instantiated
     * from the connection must be closed.
     * </p>
     * <p>
     * Implementations should handle the case where <tt>schemaName</tt> is <code>null</code>.
     * </p>
     * @param schemaName The schema name, this might be <code>null</code>.
     * @param sequenceName The name of the sequence.
     * @param cx The database connection.
     *
     * @return The next value of the sequence, or <code>null</code>.
     */
    public Object getNextSequenceValue(String schemaName, String sequenceName, Connection cx ) 
        throws SQLException {
        return null;
    }
    
    /**
     * Flag controlling wether prepared statements should be used.
     */
    public boolean isUsingPreparedStatements() {
        return true;
    }
    
    /**
     * Creates the filter encoder to be used by the datastore when encoding 
     * query predicates.
     * <p>
     * Sublcasses can override this method to return a subclass of {@link FilterToSQL}
     * if need be.
     * </p>
     */
    public FilterToSQL createFilterToSQL() {
        FilterToSQL f2s = new FilterToSQL();
        f2s.setCapabilities(BASE_DBMS_CAPABILITIES);
        return f2s;
    }
    public PreparedFilterToSQL createPreparedFilterToSQL() {
        PreparedFilterToSQL f2s = new PreparedFilterToSQL();
        f2s.setCapabilities(BASE_DBMS_CAPABILITIES);
        return f2s;
    }
}
