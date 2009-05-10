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
import com.esri.sde.sdk.geom.GeometryFactory;

/**
 * Wrapper for an SeRow so it allows asking multiple times for the same property.
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: SdeRow.java 32322 2009-01-24 20:11:52Z groldan $
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java
 *         /org/geotools/arcsde/data/SdeRow.java $
 * @since 2.4.0
 */
public class SdeRow {

    /** cached SeRow values */
    private Object[] values;

    private int[] colStatusIndicator;

    private int nCols;

    /**
     * A possible non set SDE geometry factory that provides the means for geometry fetching other
     * than SeRow.getShape(). That is, if this geometryFactory is non null, it will be used to fetch
     * the geometric attributes. Otherwise SeRow.getShape():SeShape will be used
     */
    private GeometryFactory geometryFactory;

    private int geometryIndex = -1;

    public SdeRow(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    public SdeRow(SeRow row) throws IOException {
        this(row, null);
    }

    public SdeRow(SeRow row, GeometryFactory geometryFactory) throws IOException {
        this.geometryFactory = geometryFactory;
        setRow(row);
    }

    public void setRow(SeRow row) throws ArcSdeException {
        final int ncols = row.getNumColumns();
        if (this.nCols != ncols) {
            this.nCols = ncols;
            values = new Object[nCols];
            colStatusIndicator = new int[nCols];
        }

        int i = 0;
        int statusIndicator = 0;

        try {
            for (i = 0; i < nCols; i++) {
                statusIndicator = row.getIndicator(i);
                colStatusIndicator[i] = statusIndicator;

                if (statusIndicator == SeRow.SE_IS_ALREADY_FETCHED
                        || statusIndicator == SeRow.SE_IS_REPEATED_FEATURE
                        || statusIndicator == SeRow.SE_IS_NULL_VALUE) {
                    // ignore, will use previous values
                } else {
                    if (this.geometryFactory != null && this.geometryIndex == i) {
                        values[i] = row.getGeometry(geometryFactory, i);
                    } else {
                        values[i] = row.getObject(i);
                    }
                }
            }
        } catch (SeException e) {
            throw new ArcSdeException("getting property #" + i, e);
        }
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
     * @param index
     *            DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws IOException
     *             DOCUMENT ME!
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
     * @param index
     *            DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws IOException
     *             DOCUMENT ME!
     */
    public Long getLong(int index) throws IOException {
        return (Long) getObject(index);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param index
     *            DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws IOException
     *             DOCUMENT ME!
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

    /**
     * @param geometryIndex
     *            a value >= 0 indicates which index in the row contains the geometry attribute. If
     *            not set, geometryFactory will be ignored
     */
    public void setGeometryIndex(int geometryIndex) {
        this.geometryIndex = geometryIndex;
    }

}
