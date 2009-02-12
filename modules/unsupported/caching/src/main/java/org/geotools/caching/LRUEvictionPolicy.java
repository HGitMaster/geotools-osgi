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
package org.geotools.caching;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.geotools.caching.spatialindex.NodeIdentifier;

/**
 * Least-Recently Used Eviction Policy
 * <p>
 * Removes the oldest items from the cache.
 * </p>
 */
public class LRUEvictionPolicy implements EvictionPolicy {
    private Map<NodeIdentifier, Object> queue;
    private EvictableTree tree;

    public LRUEvictionPolicy(EvictableTree tree) {
        this.queue = new LinkedHashMap<NodeIdentifier, Object>(100, .75f, true);
        this.tree = tree;
    }

    public boolean evict() {
        Iterator<NodeIdentifier> it = queue.keySet().iterator();
        while(it.hasNext()){
            NodeIdentifier node = it.next();
            if (!node.isLocked()){
                node.writeLock();
                try{
                    tree.evict(node);
                    queue.remove(node);    
                }finally{
                    node.writeUnLock();
                }
                return true;
            }
        }
        return false;
    }

    public void access(NodeIdentifier node) {
        if (queue.containsKey(node)) {
            queue.get(node);
        } else if (node.isValid()) {
            queue.put(node, null);
        }
    }
    
//    public boolean canEvict(){
//    	return queue.size() > 0;
//    }
}
