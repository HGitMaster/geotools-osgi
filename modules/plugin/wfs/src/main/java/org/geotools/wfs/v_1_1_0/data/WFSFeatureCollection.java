/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.wfs.v_1_1_0.data;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.store.DataFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureReaderIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.logging.Logging;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.geometry.BoundingBox;

/**
 * A {@link FeatureCollection} whose iterators are based on the FeatureReaders
 * returned by a {@link WFS110ProtocolHandler}.
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id: WFSFeatureCollection.java 30986 2008-07-09 22:08:46Z groldan $
 * @since 2.5.x
 * @source $URL:
 *         http://svn.geotools.org/trunk/modules/plugin/wfs/src/main/java/org/geotools/wfs/v_1_1_0/data/XmlSimpleFeatureParser.java $
 */

class WFSFeatureCollection extends DataFeatureCollection {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.data.wfs");

    private Query query;

    private WFS110ProtocolHandler protocolHandler;

    private SimpleFeatureType contentType;

    /**
     * Cached size so multiple calls to {@link #getCount()} does not require
     * multiple server calls
     */
    private int cachedSize = -1;

    /**
     * Cached collection bounds
     */
    private ReferencedEnvelope cachedBounds = null;

    /**
     * 
     * @param protocolHandler
     * @param query
     *            properly named query
     * @throws IOException
     */
    public WFSFeatureCollection(WFS110ProtocolHandler protocolHandler, Query query)
            throws IOException {
        this.contentType = protocolHandler.getQueryType(query);
        this.protocolHandler = protocolHandler;
        this.query = query;
    }

    @Override
    public SimpleFeatureType getSchema() {
        return contentType;
    }

    /**
     * Calculates and returns the aggregated bounds of the collection contents,
     * potentially doing a full scan.
     * <p>
     * As a bonuns, if a full scan needs to be done updates the cached
     * collection size so a future call to {@link #getCount()} does not require
     * an extra server call.
     * </p>
     */
    @Override
    public ReferencedEnvelope getBounds() {
        if (cachedBounds != null) {
            return cachedBounds;
        }

        ReferencedEnvelope bounds = null;
        try {
            bounds = protocolHandler.getBounds(query);
            if (bounds == null) {
                // bad luck, do a full scan
                final Name defaultgeom = contentType.getGeometryDescriptor().getName();
                final DefaultQuery geomQuery = new DefaultQuery(this.query);
                geomQuery.setPropertyNames(new String[] { defaultgeom.getLocalPart() });

                FeatureReader<SimpleFeatureType, SimpleFeature> reader;
                reader = protocolHandler.getFeatureReader(geomQuery, Transaction.AUTO_COMMIT);
                bounds = new ReferencedEnvelope(contentType.getCoordinateReferenceSystem());
                try {
                    BoundingBox featureBounds;
                    // collect size to alleviate #getCount if needed
                    int collectionSize = 0;
                    while (reader.hasNext()) {
                        featureBounds = reader.next().getBounds();
                        bounds.expandToInclude(featureBounds.getMinX(), featureBounds.getMinY());
                        bounds.expandToInclude(featureBounds.getMaxX(), featureBounds.getMaxY());
                        collectionSize++;
                    }
                    if (this.cachedSize == -1) {
                        this.cachedSize = collectionSize;
                    }
                } finally {
                    reader.close();
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.FINE, "Error getting bounds for " + query);
            bounds = new ReferencedEnvelope(getSchema().getCoordinateReferenceSystem());
        }
        return bounds;
    }

    /**
     * Calculates the feature collection size, doing a full scan if needed.
     * <p>
     * <b>WARN</b>: this method could be very inefficient if the size cannot be
     * efficiently calculated. That is, it is not cached and
     * {@link WFS110ProtocolHandler#getCount(Query)} returns {@code -1}.
     * </p>
     * 
     * @return the FeatureCollection<SimpleFeatureType, SimpleFeature> size.
     * @see DataFeatureCollection#getCount()
     * @see WFS110ProtocolHandler#getCount(Query)
     */
    @Override
    public int getCount() throws IOException {
        if (cachedSize != -1) {
            return cachedSize;
        }
        cachedSize = protocolHandler.getCount(query);
        if (cachedSize == -1) {
            // no luck, cache both bounds and count with a full scan
            getBounds();
        }
        return cachedSize;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Iterator<SimpleFeature> openIterator() throws IOException {
        FeatureReader<SimpleFeatureType, SimpleFeature> reader;
        reader = protocolHandler.getFeatureReader(query, Transaction.AUTO_COMMIT);
        return new FeatureReaderIterator<SimpleFeature>(reader);
    }
}
