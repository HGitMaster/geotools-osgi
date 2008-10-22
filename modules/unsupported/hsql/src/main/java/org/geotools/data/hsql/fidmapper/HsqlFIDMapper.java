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
package org.geotools.data.hsql.fidmapper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;

import org.geotools.data.jdbc.fidmapper.AbstractFIDMapper;
import org.geotools.data.jdbc.fidmapper.FIDMapper;
import org.geotools.data.jdbc.fidmapper.TypedFIDMapper;
import org.opengis.feature.simple.SimpleFeature;

/**
 * This fidmapper just takes another fid mapper aand wraps it! Due
 * to volatility issues, it seems that without a FIDMapper that isn't
 * declared to be volatile, some things don't work...the TypedFIDMapper
 * would've been a good choice except that it makes changes to the FID
 * in the getID method.
 *
 * @author aalam
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/hsql/src/main/java/org/geotools/data/hsql/fidmapper/HsqlFIDMapper.java $
 */
public class HsqlFIDMapper extends AbstractFIDMapper {
	private static final long serialVersionUID = 1L;
    private String featureTypeName;
    private FIDMapper wrappedMapper;

    /**
     * Creates a new HsqlFIDMapper object.
     *
     * @param wrapped
     * @param featureTypeName
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public HsqlFIDMapper(FIDMapper wrapped, String featureTypeName) {
        if (wrapped == null) {
            throw new IllegalArgumentException(
                "The wrapped feature mapper cannot be null");
        }

        if (featureTypeName == null) {
            throw new IllegalArgumentException(
                "The featureTypeName cannot be null");
        }

        this.wrappedMapper = wrapped;
        this.featureTypeName = featureTypeName;
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getID(java.lang.Object[])
     */
    public String getID(Object[] attributes) {
//        return featureTypeName + "." + wrappedMapper.getID(attributes);
    	return wrappedMapper.getID(attributes);
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getPKAttributes(java.lang.String)
     */
    public Object[] getPKAttributes(String FID) throws IOException {
        //int pos = FID.indexOf(".");

        return wrappedMapper.getPKAttributes(FID);
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#returnFIDColumnsAsAttributes()
     */
    public boolean returnFIDColumnsAsAttributes() {
        return wrappedMapper.returnFIDColumnsAsAttributes();
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnCount()
     */
    public int getColumnCount() {
        return wrappedMapper.getColumnCount();
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnName(int)
     */
    public String getColumnName(int colIndex) {
        return wrappedMapper.getColumnName(colIndex);
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnType(int)
     */
    public int getColumnType(int colIndex) {
        return wrappedMapper.getColumnType(colIndex);
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnSize(int)
     */
    public int getColumnSize(int colIndex) {
        return wrappedMapper.getColumnSize(colIndex);
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#getColumnDecimalDigits(int)
     */
    public int getColumnDecimalDigits(int colIndex) {
        return wrappedMapper.getColumnSize(colIndex);
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#isAutoIncrement(int)
     */
    public boolean isAutoIncrement(int colIndex) {
        return wrappedMapper.isAutoIncrement(colIndex);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object object) {
        if (!(object instanceof TypedFIDMapper)) {
            return false;
        }

        HsqlFIDMapper other = (HsqlFIDMapper) object;

        return other.wrappedMapper.equals(wrappedMapper)
        	&& (other.featureTypeName == featureTypeName);
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#createID(java.sql.Connection,
     *      org.geotools.feature.Feature, Statement)
     */
    public String createID(Connection conn, SimpleFeature feature, Statement statement)
        throws IOException {
        return featureTypeName + "."
        + wrappedMapper.createID(conn, feature, statement);
    }

    /**
     * @see org.geotools.data.jdbc.fidmapper.FIDMapper#initSupportStructures()
     */
    public void initSupportStructures() {
        wrappedMapper.initSupportStructures();
    }

    /**
     * Returns the base mapper wrapped by this TypedFIDMapper
     *
     */
    public FIDMapper getWrappedMapper() {
        return wrappedMapper;
    }
}
