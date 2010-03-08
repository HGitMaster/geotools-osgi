/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gce.imagemosaic.shp;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * This class simply builds an SRTREE spatial index in memory for fast indexed
 * geometric queries.
 * 
 * <p>
 * Since the {@link ImageMosaicReader} heavily uses spatial queries to find out
 * which are the involved tiles during mosaic creation, it is better to do some
 * caching and keep the index in memory as much as possible, hence we came up
 * with this index.
 * 
 * @author Simone Giannecchini
 * @since 2.3
 */
public final class MemorySpatialIndex
{

    /** Logger. */
    private final static Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.gce.imagemosaic.shp");

    /** The {@link STRtree} index. */
    private final STRtree index;

    /** The time the index was created */
    private final Date dateCreated;

    /**
     * Constructs a {@link MemorySpatialIndex} out of a
     * {@link FeatureCollection}.
     * 
     * @param features
     */
    public MemorySpatialIndex(
            FeatureCollection<SimpleFeatureType, SimpleFeature> features)
    {
        dateCreated = new Date();
        if (features == null)
        {
            String msg = "The provided FeatureCollection is null, " +
                "it's impossible to create an index!";
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.warning(msg);
            throw new IllegalArgumentException(msg);
        }
        final FeatureIterator<SimpleFeature> it = features.features();
        try
        {

            if (!it.hasNext())
            {
                String msg = "The provided FeatureCollection is empty, " +
                		"it's impossible to create an index!";
                if (LOGGER.isLoggable(Level.WARNING))
                    LOGGER.warning(msg);
                throw new IllegalArgumentException(msg);
            }
            index = new com.vividsolutions.jts.index.strtree.STRtree();
            while (it.hasNext())
            {
                final SimpleFeature f = it.next();
                final Geometry g = (Geometry) f.getDefaultGeometry();
                index.insert(g.getEnvelopeInternal(), f);
            }
        } 
        finally
        {
            // closing the iterator to free some resources.
            features.close(it);
        }
        // force index construction --> STRTrees are built on first call to
        // query
        index.build();
    }

    /**
     * Finds the features that intersects the provided {@link Envelope}:
     * 
     * @param envelope
     *            The {@link Envelope} to test for intersection.
     * @return List of {@link Feature} that intersect the providede
     *         {@link Envelope}.
     */
    @SuppressWarnings("unchecked")
    public List<SimpleFeature> findFeatures(Envelope envelope)
    {
        return index.query(envelope);
    }

    /**
     * 
     * @return the date the index was created
     */
    public Date getCreatedDate()
    {
        return this.dateCreated;
    }

}
