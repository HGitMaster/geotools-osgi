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
package org.geotools.caching.spatialindex.grid;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import org.geotools.caching.spatialindex.Node;
import org.geotools.caching.spatialindex.NodeIdentifier;
import org.geotools.caching.spatialindex.Region;
import org.geotools.caching.spatialindex.RegionNodeIdentifier;
import org.geotools.caching.spatialindex.Shape;
import org.geotools.caching.spatialindex.SpatialIndex;
import org.geotools.caching.spatialindex.grid.GridData;


/** A node in the grid.
 * Data objects are stored in an array.
 * Extra data about the node may be stored in node_data, which is a HashMap.
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 */
public class GridNode implements Node, Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 7786313461725794946L;
    Region mbr;

    //HashMap node_data;
    int num_data;

    //protected int[] data_ids;
    protected LinkedHashSet<GridData> data;
    transient protected RegionNodeIdentifier id = null;

    //transient boolean visited = false;
    transient protected Grid grid;

    /**No-arg constructor for serialization purpose.
     * Deserialized nodes must call init(Grid grid) before any other operation.
     *
     */
    protected GridNode() {
    }

    protected GridNode(Grid grid, Region mbr) {
        this.mbr = new Region(mbr);
        //this.parent = parent;
        //this.node_data = new HashMap();
        this.num_data = 0;
        this.data = new LinkedHashSet<GridData>();
        //this.data_ids = new int[10];
        this.grid = grid;
    }

    /**Post-deserialization initialization.
     *
     * @param grid
     */
    public void init(SpatialIndex grid) {
        this.grid = (Grid) grid;
    }

    public NodeIdentifier getChildIdentifier(int index)
        throws IndexOutOfBoundsException {
        throw new UnsupportedOperationException("GridNode have no children.");
    }

    public Shape getChildShape(int index) throws IndexOutOfBoundsException {
        throw new UnsupportedOperationException("GridNode have no children.");
    }

    public int getChildrenCount() {
        return 0;
    }

    public int getLevel() {
        return 0;
    }

    public boolean isIndex() {
        return false;
    }

    public boolean isLeaf() {
        return true;
    }

    public NodeIdentifier getIdentifier() {
        if (id == null) {
            id = new RegionNodeIdentifier(this);

            //            if (grid.containsKey(id)) {
            //                id = grid.node_ids.get(id);
            //            } else {
            //                grid.node_ids.put(id, id);
            //            }
            id = (RegionNodeIdentifier) grid.findUniqueInstance(id);
        }

        return id;
    }

    public Shape getShape() {
        return mbr;
    }

    /** Insert new data in this node.
     *
     * @param id of data
     * @param data
     */
    protected boolean insertData(GridData data) {
        if (this.data.contains(data)) {
            return false;
        } else {
            this.data.add(data);
            num_data++;

            return true;
        }
    }

    /** Delete blindly data at the given index.
     * Index is not the id of the data, the search should be performed by the Grid class,
     * which determines the index of data to delete, and then call this method.
     *
     * @param index
     */
    protected void deleteData(int index) {
        if ((index < 0) || (index > (num_data - 1))) {
            throw new IndexOutOfBoundsException();
        }

        Iterator<GridData> it = data.iterator();

        for (int i = 0; i < index; i++) {
            it.next();
        }

        it.remove();

        num_data--;
    }

    protected void deleteData(GridData data) {
        if (this.data.remove(data)) {
            num_data--;
        }
    }

    /** Erase all data referenced by this node.
     *
     */
    public void clear() {
        this.num_data = 0;
        this.data.clear();
        getIdentifier().setValid(false);

        //this.data_ids = new int[10];
    }

    public int getDataCount() {
        return this.num_data;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("GridNode: MBR:" + mbr);

        return sb.toString();
    }

    public String toReadableText() {
        StringBuffer sb = new StringBuffer();
        sb.append("GridNode ******");
        sb.append("\tMBR= " + mbr + "\n");
        sb.append("\t#Data= " + num_data + "\n");

        for (Iterator<GridData> it = data.iterator(); it.hasNext();) {
            sb.append("\t\t" + it.next().data.toString() + "\n");
        }

        return sb.toString();
    }
}
