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

import java.util.Stack;
import org.geotools.caching.EvictableTree;
import org.geotools.caching.EvictionPolicy;
import org.geotools.caching.LRUEvictionPolicy;
import org.geotools.caching.spatialindex.Node;
import org.geotools.caching.spatialindex.NodeIdentifier;
import org.geotools.caching.spatialindex.Region;
import org.geotools.caching.spatialindex.Shape;
import org.geotools.caching.spatialindex.Storage;
import org.geotools.caching.spatialindex.grid.Grid;
import org.geotools.caching.spatialindex.grid.GridNode;


public class GridTracker extends Grid implements EvictableTree {
    GridTrackerStatistics stats;
    EvictionPolicy policy;
    boolean doRecordAccess = true;

    public GridTracker(Region mbr, int capacity, Storage store) {
        this.mbr = mbr;
        this.dimension = mbr.getDimension();
        this.store = store;
        store.setParent(this);
        this.policy = new LRUEvictionPolicy(this);

        GridCacheRootNode root = new GridCacheRootNode(this, mbr, capacity);
        this.root = root.getIdentifier();
        root.split();
        writeNode(root);
        this.stats = new GridTrackerStatistics();
        super.stats = this.stats;
        this.stats.addToNodesCounter(root.getCapacity() + 1); // root has root.capacity nodes, +1 for root itself :)
    }

    NodeIdentifier getRoot() {
        return this.root;
    }

    Stack searchMissingTiles(Region search) { // search must be within root mbr !

        Stack<Shape> missing = new Stack<Shape>();
        boolean foundValid = false;

        if (!this.root.isValid()) {
            int[] cursor = new int[this.dimension];
            int[] mins = new int[this.dimension];
            int[] maxs = new int[this.dimension];
            findMatchingTiles(search, cursor, mins, maxs);

            GridCacheRootNode root = (GridCacheRootNode) readNode(this.root);

            do {
                int nextid = root.gridIndexToNodeId(cursor);
                NodeIdentifier nextnode = root.getChildIdentifier(nextid);
                if (!nextnode.isValid()) {
                    missing.add(nextnode.getShape());
                } else if (!foundValid) {
                    foundValid = true;
                }
            } while (increment(cursor, mins, maxs));
        }

        if (!foundValid && (missing.size() > 1)) {
            Region r1 = (Region) missing.pop();
            Region r2 = (Region) missing.get(0);
            missing = new Stack<Shape>();
            missing.add(r1.combinedRegion(r2));
        }

        return missing;
    }

    //    public void flush() {
    //        GridCacheRootNode oldroot = (GridCacheRootNode) readNode(this.root);
    //        int capacity = oldroot.getCapacity();
    //        Region mbr = new Region((Region) oldroot.getShape());
    //        GridCacheRootNode root = new GridCacheRootNode(this, mbr, capacity);
    //        this.store.clear();
    //        this.root = root.getIdentifier();
    //        root.split();
    //        writeNode(root);
    //        this.stats.reset();
    //        this.stats.addToNodesCounter(root.getCapacity() + 1);
    //    }
    public int getEvictions() {
        return stats.getEvictions();
    }

    public void evict(NodeIdentifier node) {
        //    	System.out.println("evicting : " + node);
        GridNode nodeToEvict = (GridNode) readNode(node); // FIXME: avoid to read node before eviction
        int ret = nodeToEvict.getDataCount();
        nodeToEvict.clear();
        nodeToEvict.getIdentifier().setValid(false);
        writeNode(nodeToEvict);
        this.stats.addToDataCounter(-ret);
        this.stats.addToEvictionCounter(1);
    }

    @Override
    protected Node readNode(NodeIdentifier id) {
        if (doRecordAccess) {
            policy.access(id);
        }

        return super.readNode(id);
    }

    @Override
    protected void writeNode(Node node) {
        super.writeNode(node);

        if (doRecordAccess) {
            policy.access(node.getIdentifier());
        }
    }

    void setDoRecordAccess(boolean b) {
        doRecordAccess = b;
    }

    class GridTrackerStatistics extends ThisStatistics {
        int stats_evictions = 0;

        public void addToEvictionCounter(int count) {
            stats_evictions += count;
        }

        public int getEvictions() {
            return stats_evictions;
        }

        @Override
        public void reset() {
            stats_evictions = 0;
            super.reset();
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer(super.toString());
            sb.append(" ; Evictions = " + stats_evictions);

            return sb.toString();
        }
    }
}
