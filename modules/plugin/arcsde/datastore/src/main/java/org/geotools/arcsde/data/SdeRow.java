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
 *
 */
package org.geotools.arcsde.data;

import java.io.IOException;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.data.DataSourceException;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeShape;

/**
 * Wrapper for an SeRow so it allows asking multiple times for the same property.
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: SdeRow.java 30722 2008-06-13 18:15:42Z acuster $
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java/org/geotools/arcsde/data/SdeRow.java $
 * @since 2.4.0
 */
public class SdeRow {

    /** cached SeRow values */
    private Object[] values;

    private int[] colStatusIndicator;

    private final int nCols;

    public SdeRow(SeRow row) throws IOException {
        this.nCols = row.getNumColumns();

        values = new Object[nCols];
        colStatusIndicator = new int[nCols];

        int i = 0;
        int statusIndicator = 0;

        try {
            for (i = 0; i < nCols; i++) {
                statusIndicator = row.getIndicator(i);
                colStatusIndicator[i] = statusIndicator;

                if (statusIndicator == SeRow.SE_IS_ALREADY_FETCHED
                        || statusIndicator == SeRow.SE_IS_REPEATED_FEATURE
                        || statusIndicator == SeRow.SE_IS_NULL_VALUE) {
                } else {
                    values[i] = row.getObject(i);
                }
            }
        } catch (SeException e) {
            throw new ArcSdeException("getting property #" + i, e);
        }
    }

    /**
     * Creates a new SdeRow object.
     * 
     * @param row DOCUMENT ME!
     * @param previousValues needed in case of its a joined result, thus arcsde does not returns
     *            geometry attributes duplicated, just on their first occurrence (sigh)
     * @throws IOException DOCUMENT ME!
     * @throws DataSourceException DOCUMENT ME!
     * @deprecated
     */
    public SdeRow(SeRow row, Object[] previousValues) throws IOException {
        this(row);
        setPreviousValues(previousValues);
    }

    public void setPreviousValues(Object[] previousValues) {
        int statusIndicator;
        for (int i = 0; i < nCols; i++) {
            statusIndicator = colStatusIndicator[i];

            if (statusIndicator == SeRow.SE_IS_ALREADY_FETCHED
                    || statusIndicator == SeRow.SE_IS_REPEATED_FEATURE) {
                values[i] = previousValues[i];
            }
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param index DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public Object getObject(int index) throws IOException {
        return values[index];
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     */
    public Object[] getAll() {
        return values;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param index DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public Long getLong(int index) throws IOException {
        return (Long) getObject(index);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param index DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public SeShape getShape(int index) throws IOException {
        return (SeShape) getObject(index);
    }

    /**
     * @param columnIndex
     * @return one of {@link SeRow#SE_IS_ALREADY_FETCHED}, {@link SeRow#SE_IS_NOT_NULL_VALUE},
     *         {@link SeRow#SE_IS_NULL_VALUE}, {@link SeRow#SE_IS_REPEATED_FEATURE}
     */
    public int getIndicator(int columnIndex) {
        return colStatusIndicator[columnIndex];
    }

    public Integer getInteger(int index) throws IOException {
        return (Integer) getObject(index);
    }

}
