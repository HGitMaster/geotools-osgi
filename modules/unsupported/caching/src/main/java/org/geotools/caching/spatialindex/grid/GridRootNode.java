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

import java.util.ArrayList;
import java.util.Iterator;
import org.geotools.caching.spatialindex.AbstractSpatialIndex;
import org.geotools.caching.spatialindex.NodeIdentifier;
import org.geotools.caching.spatialindex.Region;
import org.geotools.caching.spatialindex.Shape;


/** The root node of a grid, which has n GridNodes as children.
 * As GridNodes do, it can store data too.
 *
 * @author Christophe Rousson, SoC 2007, CRG-ULAVAL
 *
 */
public class GridRootNode extends GridNode {
    /**
     *
     */
    private static final long serialVersionUID = 4675163856302389522L;
    protected int capacity;
    protected int[] tiles_number;
    protected double tiles_size;
    protected ArrayList<NodeIdentifier> children; // list of NodeIdentifiers

    /**No-arg constructor for serialization purpose.
     * Deserialized nodes must call init(Grid grid) before any other operation.
     *
     */
    GridRootNode() {
        super();
    }

    /** Create a not yet initialized root node.
     *
     * @param grid
     * @param mbr
     */
    protected GridRootNode(Grid grid, Region mbr) {
        super(grid, mbr);
        this.grid = grid;
        this.children = new ArrayList<NodeIdentifier>();
    }

    protected GridRootNode(Grid grid, Region mbr, int capacity) {
        super(grid, mbr);
        this.grid = grid;
        this.capacity = capacity;
        init();
    }

    void init() {
        int dims = mbr.getDimension();
        tiles_number = new int[dims];

        double area = 1;

        for (int i = 0; i < dims; i++) {
            area *= (mbr.getHigh(i) - mbr.getLow(i));
        }

        tiles_size = Math.pow(area / capacity, 1d / dims);

        int newcapacity = 1;

        for (int i = 0; i < dims; i++) {
            int tmp;
            double dtmp = (mbr.getHigh(i) - mbr.getLow(i)) / tiles_size;
            tmp = (int) dtmp;

            if (tmp < dtmp) {
                tmp += 1;
            }

            tiles_number[i] = tmp;
            newcapacity *= tmp;
        }
        assert (newcapacity >= capacity);
        capacity = newcapacity;
        children = new ArrayList<NodeIdentifier>(capacity);
    }

    /** Creates the grid by appending children to this node.
     *
     */
    protected void split() {
        int dims = tiles_number.length;
        double[] pos = new double[dims];
        double[] nextpos = new double[dims];
        int id = 0;

        for (int i = 0; i < dims; i++) {
            pos[i] = mbr.getLow(i);
            nextpos[i] = pos[i] + tiles_size;
        }

        do {
            Region reg = new Region(pos, nextpos);
            GridNode child = createNode(reg);
            this.grid.writeNode(child);
            this.children.add(child.getIdentifier());
            id++;
        } while (increment(pos, nextpos));
    }

    protected GridNode createNode(Region reg) {
        return new GridNode(grid, reg);
    }

    /** Computes sequentially the corner position of each tile in the grid.
     *
     * @param pos
     * @param nextpos
     * @return false if the upperright corner of the grid has been reached, true otherwise
     */
    boolean increment(double[] pos, double[] nextpos) {
        int dims = pos.length;

        if ((dims != tiles_number.length) || (nextpos.length != tiles_number.length)) {
            throw new IllegalArgumentException("Cursor has not the same dimension as grid.");
        }

        for (int i = 0; i < dims; i++) {
            if (((nextpos[i] - mbr.getHigh(i)) > 0)
                    || (Math.abs(nextpos[i] - mbr.getHigh(i)) < AbstractSpatialIndex.EPSILON)) {
                pos[i] = mbr.getLow(i);
                nextpos[i] = pos[i] + tiles_size;

                if (i == (dims - 1)) {
                    return false;
                }
            } else {
                pos[i] = nextpos[i];
                nextpos[i] = pos[i] + tiles_size;

                break;
            }
        }

        return true;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("GridRootNode: capacity:" + capacity + ", MBR:" + mbr);

        return sb.toString();
    }

    public NodeIdentifier getChildIdentifier(int index)
        throws IndexOutOfBoundsException {
        return (NodeIdentifier) children.get(index);
    }

    public int getChildrenCount() {
        return children.size();
    }

    public Shape getChildShape(int index) throws IndexOutOfBoundsException {
        return getChildIdentifier(index).getShape();
    }

    public int getLevel() {
        return 1;
    }

    public boolean isIndex() {
        if (children.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isLeaf() {
        return !isIndex();
    }

    /** Converts an array of indexes into the id of a node.
     *
     * @param index
     * @return
     */
    public int gridIndexToNodeId(int[] index) {
        if (index.length != tiles_number.length) {
            throw new IllegalArgumentException("Argument has " + index.length
                + " dimensions whereas grid has " + tiles_number.length);
        } else {
            int result = 0;
            int offset = 1;

            for (int i = 0; i < index.length; i++) {
                result += (offset * index[i]);
                offset *= tiles_number[i];
            }

            return result;
        }
    }

    public void clear() {
        //getIdentifier().setValid(false);
        for (Iterator<NodeIdentifier> it = children.iterator(); it.hasNext();) {
            GridNode child = (GridNode) grid.readNode(it.next());
            child.clear();
            grid.writeNode(child);
        }

        super.clear();
    }

    public String toReadableText() {
        StringBuffer sb = new StringBuffer();
        sb.append("RootNode *****");
        sb.append(super.toReadableText());

        return sb.toString();
    }
}
