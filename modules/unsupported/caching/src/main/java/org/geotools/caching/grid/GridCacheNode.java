/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.caching.grid;

import org.geotools.caching.spatialindex.Region;
import org.geotools.caching.spatialindex.grid.Grid;
import org.geotools.caching.spatialindex.grid.GridData;
import org.geotools.caching.spatialindex.grid.GridNode;


public class GridCacheNode extends GridNode {
    /**
     *
     */
    private static final long serialVersionUID = -760685958776143228L;

    protected GridCacheNode(Grid grid, Region mbr) {
        super(grid, mbr);
    }

    @Override
    protected boolean insertData(GridData data) {
        if (getIdentifier().isValid()) {
            return super.insertData(data);
        }

        return false;
    }

    //    protected GridData getData(int i) {
    //        return this.data[i];
    //    }
}
