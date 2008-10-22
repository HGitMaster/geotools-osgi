/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.jdbc.fidmapper;

import org.opengis.feature.simple.SimpleFeature;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.Statement;


/**
 * <p>
 * The FIDMapper interface manages the mapping of feature id to the identifiers
 * provided in a database.
 * </p>
 * 
 * <p>
 * Basically a FIDMapper must:
 * 
 * <ul>
 * <li>
 * generate the FID (a String) given the set of values that compose the primary
 * key in the database
 * </li>
 * <li>
 * turn the FID into the primary key values, or generate them should the FID be
 * null
 * </li>
 * <li>
 * provide notice wheter the identifier values should be included as attributes
 * in the feature or not (this is necessary when reverse engineering the
 * feature type from the database metadata)
 * </li>
 * <li>
 * describe the primary key columns, if any (this is necessary when creating
 * the table that describes the feature type in a table)
 * </li>
 * </ul>
 * </p>
 * 
 * <p>
 * Concrete instances of this class should provide support for the most common
 * primary key mapping and generation strategis, such as pk with business
 * meaning, serials, sequences, and so on
 * </p>
 * 
 * <p>
 * Classes that implement this interface should ovveride equals to provide a
 * state based comparison.
 * </p>
 *
 * @author Dani Daniele Franzoni
 * @author aaime Andrea Aime
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/jdbc/src/main/java/org/geotools/data/jdbc/fidmapper/FIDMapper.java $
 */
public interface FIDMapper extends Serializable {
    /**
     * This method will be called by JDBCDataStore when creating new tables to
     * give the FID mapper an opportunity to initialize needed data
     * structures, such as support tables, sequences, and so on.
     */
    public void initSupportStructures();

    /**
     * Returns the FID given the values of the prymary key attributes
     *
     * @param attributes DOCUMENT ME!
     *
     */
    public String getID(Object[] attributes);

    /**
     * Creates the value for the PK attributes given the feature. If the FID is
     * null, will throw an IOException if not possible. If null is returned,
     * no primary key value needs to be specified, which is what we want for
     * auto-increment fields.
     *
     * @param FID The feature ID is going to be parsed
     *
     *
     * @throws IOException
     */
    public Object[] getPKAttributes(String FID) throws IOException;

    /**
     * Creates a new ID for a feature. <br>
     * This is done either by querying the database (for auto-increment like
     * types, for example sequences) or by inspecting the Feature (for
     * example, for primary keys with business meaning that whose attributes
     * are included in the Feature ones).
     *
     * @param conn - the database connection
     * @param feature - the feature that needs the new FID
     * @param statement - the statement used to insert the feature into the
     *        database
     *
     *
     * @throws IOException
     */
    public String createID(Connection conn, SimpleFeature feature, Statement statement)
        throws IOException;

    /**
     * If true the primary key columns will be returned as attributes. This is
     * fundamental for primary key with businnes meaning.
     *
     */
    public boolean returnFIDColumnsAsAttributes();

    /**
     * Returns the number of columns in the primary keys handled by this mapper
     *
     */
    public int getColumnCount();

    /**
     * Returns the name of the specified column in the primary key
     *
     * @param colIndex
     *
     */
    public String getColumnName(int colIndex);

    /**
     * Returns the column type by using a constant available in the
     * java.sql.Types interface
     *
     * @param colIndex
     *
     */
    public int getColumnType(int colIndex);

    /**
     * Returns the size of a primary key column as it would be provided by the
     * database metadata. Some fields requires a size specification, such as
     * VARCHAR or NUMBER, whilst other don't have or don't need it (for
     * example, an INTEGER or a TEXT field).
     *
     * @param colIndex
     *
     */
    public int getColumnSize(int colIndex);

    /**
     * Provides the number of decimal digits for this column. This is relevant
     * in particular when the column is a scaled integer such as a NUMBER
     * column
     *
     * @param colIndex
     *
     */
    public int getColumnDecimalDigits(int colIndex);

    /**
     * Returns true if the column is of serial type, that is, its value is
     * automatically generated by the database if the user does not provide
     * one
     *
     * @param colIndex
     *
     */
    public boolean isAutoIncrement(int colIndex);

    /**
     * Returns true if at least one column is of auto-increment type
     *
     */
    public boolean hasAutoIncrementColumns();

    /**
     * Returns true it the FID generated by this mapper are volatile, that is,
     * if asking twice for the same Feature will not provide the same FID.
     * 
     * <p>
     * This is usually true for mappers that try to generate a FID for tables
     * without primary keys.
     * </p>
     * 
     * <p>
     * When this method returns true, it's up to the datastore to decide what
     * to do, but a sane policy may be to prevent Feature writing
     * </p>
     *
     */
    public boolean isVolatile();
}
