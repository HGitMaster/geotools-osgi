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
package org.geotools.caching.spatialindex.store;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.logging.Logger;
import org.geotools.caching.spatialindex.Node;
import org.geotools.caching.spatialindex.NodeIdentifier;
import org.geotools.caching.spatialindex.SpatialIndex;
import org.geotools.caching.spatialindex.Storage;
import org.opengis.feature.type.FeatureType;


public class BufferedDiskStorage implements Storage {
    public final static String BUFFER_SIZE_PROPERTY = "BufferedDiskStorage.BufferSize";
    protected static Logger logger = org.geotools.util.logging.Logging.getLogger("org.geotools.caching.spatialindex.store");
    private DiskStorage storage;
    LinkedHashMap<NodeIdentifier, BufferEntry> buffer;
    int buffer_size;

    private BufferedDiskStorage(int buffersize) {
        this.buffer_size = buffersize;
        buffer = new LinkedHashMap<NodeIdentifier, BufferEntry>(buffer_size, .75f, true);
    }

    //    public BufferedDiskStorage(File f, int page_size, int buffer_size)
    //        throws IOException {
    //    	this(buffer_size);
    //        storage = new DiskStorage(f, page_size);
    //    }
    public static Storage createInstance(Properties pset) {
        try {
            int buffer_size = Integer.parseInt(pset.getProperty(BUFFER_SIZE_PROPERTY));
            BufferedDiskStorage instance = new BufferedDiskStorage(buffer_size);
            instance.storage = (DiskStorage) DiskStorage.createInstance(pset);

            return instance;
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("BufferedDiskStorage : invalid property set.");
        }
    }

    public static Storage createInstance() {
        BufferedDiskStorage instance = new BufferedDiskStorage(100);
        instance.storage = (DiskStorage) DiskStorage.createInstance();

        return instance;
    }

    public void clear() {
        buffer.clear();
        storage.clear();
    }

    public Node get(NodeIdentifier id) {
        BufferEntry entry = buffer.get(id);
        Node ret;

        if (entry == null) {
            ret = storage.get(id);

            if (ret != null) {
                put(new BufferEntry(ret));
            }
        } else {
            ret = entry.node;
        }

        return ret;
    }

    void put(BufferEntry entry) {
        if (entry != null) {
            if (buffer.size() == buffer_size) {
                Iterator<NodeIdentifier> it = buffer.keySet().iterator();
                BufferEntry removed = buffer.remove(it.next());

                if (removed != null && removed.dirty) {
                    storage.put(removed.node);
                }

                buffer.put(entry.node.getIdentifier(), entry);
            } else {
                buffer.put(entry.node.getIdentifier(), entry);
            }
        }
    }

    public void put(Node n) {
        if (buffer.containsKey(n.getIdentifier())) {
            buffer.get(n.getIdentifier()).dirty = true;
        } else {
            BufferEntry entry = new BufferEntry(n);
            entry.dirty = true;
            put(entry);
        }
    }

    public void remove(NodeIdentifier id) {
        if (buffer.containsKey(id)) {
            buffer.remove(id);
        } else {
            storage.remove(id);
        }
    }

    public Properties getPropertySet() {
        Properties pset = storage.getPropertySet();
        pset.setProperty(STORAGE_TYPE_PROPERTY, BufferedDiskStorage.class.getCanonicalName());
        pset.setProperty(BUFFER_SIZE_PROPERTY, new Integer(buffer_size).toString());

        return pset;
    }

    public void flush() {
        for (Iterator<BufferEntry> it = buffer.values().iterator(); it.hasNext();) {
            BufferEntry entry = it.next();

            if (entry.dirty) {
                storage.put(entry.node);
            }
        }

        storage.flush();
    }

    public void setParent(SpatialIndex index) {
        storage.setParent(index);
    }

    public NodeIdentifier findUniqueInstance(NodeIdentifier id) {
        if (buffer.containsKey(id)) {
            return buffer.get(id).node.getIdentifier();
        } else {
            return storage.findUniqueInstance(id);
        }
    }

    class BufferEntry {
        Node node;
        boolean dirty = false;

        BufferEntry(Node node) {
            this.node = node;
        }
    }

    public void addFeatureType( FeatureType ft ) {
        this.storage.addFeatureType(ft);
    }

    public Collection<FeatureType> getFeatureTypes() {
        return this.storage.getFeatureTypes();
    }
    
    public void clearFeatureTypes(){
        this.storage.clearFeatureTypes();
    }
}
