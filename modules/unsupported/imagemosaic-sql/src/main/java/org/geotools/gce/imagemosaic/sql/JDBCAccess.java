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
package org.geotools.gce.imagemosaic.sql;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.imageio.stream.ImageInputStream;

import org.opengis.geometry.Envelope;

/**
 * This interface lists the methods wich are used by 
 * the ImageMosaicJDBCReader class to interact with the 
 * database system.
 * 
 * @author mcr
 *
 */
interface JDBCAccess 
{
    /**
     * @param level		the level (0 is original, 1 is first pyramid,...)
     * @return			the corresponding ImageLevelInfo object
     */
    public ImageLevelInfo getLevelInfo(int level);

    /**
     * @return the number of existing pyramids
     */
    public int getNumOverviews();

    /**
     * initialze the the JDBCAccess object, has to be called exactly once
     * 
     * @throws SQLException
     * @throws IOException
     */
    public void initialize() throws SQLException, IOException;
    
    List<?> getMatchingTileIds(ImageLevelInfo levelInfo, Envelope requestEnvelope);
    
    Envelope getTileEnvelope(ImageLevelInfo levelInfo, Object tileId);
    
    ImageInputStream getImageInputStream(ImageLevelInfo levelInfo, Object tileId);    
}
