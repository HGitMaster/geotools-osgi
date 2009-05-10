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
package org.geotools.arcsde.pool;

import java.io.IOException;

import org.apache.commons.pool.ObjectPool;

/**
 * There can be only one! It ends up being modal based on a transaction being around. If supplied
 * with Transaction.AUTO_COMMIT the connection is viewed as being read only.
 * 
 * @author Jody Garnett
 */
public class ArcSDEReferencedConnection extends Session {

    public ArcSDEReferencedConnection(ObjectPool pool, ArcSDEConnectionConfig config)
            throws IOException {
        super(pool, config);
    }

}
