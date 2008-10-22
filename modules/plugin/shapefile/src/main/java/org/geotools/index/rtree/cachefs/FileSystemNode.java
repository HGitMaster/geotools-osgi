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
 */
package org.geotools.index.rtree.cachefs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.util.EmptyStackException;

import org.geotools.index.Data;
import org.geotools.index.DataDefinition;
import org.geotools.index.TreeException;
import org.geotools.index.DataDefinition.Field;
import org.geotools.index.rtree.Entry;
import org.geotools.index.rtree.Node;

import com.vividsolutions.jts.geom.Envelope;

/**
 * DOCUMENT ME!
 * 
 * @author Tommaso Nolli
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/shapefile/src/main/java/org/geotools/index/rtree/cachefs/FileSystemNode.java $
 */
public class FileSystemNode extends Node {
    static final int ENTRY_SIZE = 40;
    private static int pageLen = 0;
    private Parameters params = null;
    private boolean flushNeeded;
    private long parentOffset = -1;
    private long offset = -1;

    private FileSystemNode(Parameters params, boolean getFromFree) {
        super(params.getMaxNodeEntries());

        this.params = params;

        if (pageLen == 0) {
            pageLen = (params.getMaxNodeEntries() * ENTRY_SIZE) + 9; // Flag
                                                                        // (leaf
                                                                        // or
                                                                        // not)
        }

        Long oOffset = null;

        if (getFromFree) {
            try {
                oOffset = (Long) this.params.getFreePages().pop();
            } catch (EmptyStackException e) {
                // The stack is empty
            }
        }

        this.offset = (oOffset == null) ? (-1) : oOffset.longValue();
        this.flushNeeded = false;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param params
     */
    public FileSystemNode(Parameters params) {
        this(params, true);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param params
     * @param offset
     *                DOCUMENT ME!
     * 
     * @throws IOException
     *                 DOCUMENT ME!
     * @throws TreeException
     *                 DOCUMENT ME!
     */
    public FileSystemNode(Parameters params, long offset) throws IOException,
            TreeException {
        this(params, false);
        this.offset = offset;

        FileChannel channel = this.params.getChannel();
        ByteBuffer buf = this.getEmptyByteBuffer();
        ByteBuffer dataBuf = null;

        synchronized (channel) {
            channel.position(offset);
            channel.read(buf);

            // Check if I'm a leaf
            buf.position(0);
            this.setLeaf(buf.get() == (byte) 1);

            // If I'm a leaf, read the data
            if (this.isLeaf()) {
                dataBuf = this.getEmptyByteBuffer(this.params.getDataDef());
                channel.read(dataBuf);
                dataBuf.position(0);
            }
        }

        this.parentOffset = buf.getLong();

        double x1;
        double x2;
        double y1;
        double y2;
        long p;
        Entry entry = null;

        for (int i = 0; i < this.params.getMaxNodeEntries(); i++) {
            x1 = buf.getDouble();
            x2 = buf.getDouble();
            y1 = buf.getDouble();
            y2 = buf.getDouble();
            p = buf.getLong();

            if ((x1 == 0) && (x2 == 0) && (y1 == 0) && (y2 == 0) && (p == 0)) {
                // This is an empty entry
                break;
            }

            if (this.isLeaf()) {
                entry = new Entry(new Envelope(x1, x2, y1, y2), this.loadData(
                        dataBuf, this.params.getDataDef()));
            } else {
                entry = new Entry(new Envelope(x1, x2, y1, y2), new Long(p));
            }

            this.addEntry(entry);
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param buf
     * @param def
     * 
     * 
     * @throws TreeException
     */
    private Data loadData(ByteBuffer buf, DataDefinition def)
            throws TreeException {
        Data data = new Data(def);

        Field field = null;

        for (int i = 0; i < def.getFieldsCount(); i++) {
            field = def.getField(i);

            if (field.getFieldClass().equals(Short.class)) {
                data.addValue(new Short(buf.getShort()));
            } else if (field.getFieldClass().equals(Integer.class)) {
                data.addValue(new Integer(buf.getInt()));
            } else if (field.getFieldClass().equals(Long.class)) {
                data.addValue(new Long(buf.getLong()));
            } else if (field.getFieldClass().equals(Float.class)) {
                data.addValue(new Float(buf.getFloat()));
            } else if (field.getFieldClass().equals(Double.class)) {
                data.addValue(new Double(buf.getDouble()));
            } else if (field.getFieldClass().equals(String.class)) {
                byte[] bytes = new byte[field.getEncodedLen()];
                buf.get(bytes);

                CharBuffer cb = def.getCharset().decode(ByteBuffer.wrap(bytes));
                cb.position(0);
                data.addValue(cb.toString().trim());
            }
        }

        return data;
    }

    /**
     * DOCUMENT ME!
     * 
     */
    private ByteBuffer getEmptyByteBuffer() {
        return ByteBuffer.allocate(pageLen);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param dataDef
     * 
     */
    private ByteBuffer getEmptyByteBuffer(DataDefinition dataDef) {
        int bufLen = dataDef.getEncodedLen() * this.params.getMaxNodeEntries();

        return ByteBuffer.allocate(bufLen);
    }

    /**
     * DOCUMENT ME!
     * 
     */
    long getOffset() {
        return this.offset;
    }

    /**
     * @see org.geotools.rtree.Node#getParent()
     */
    public Node getParent() throws TreeException {
        FileSystemNode node = null;

        try {
            node = this.params.getFromCache(this.parentOffset);
        } catch (IOException e) {
            throw new TreeException(e);
        }

        return node;
    }

    /**
     * Flushes this node to disk
     * 
     * <pre>
     * Node page structure:
     * 1 * byte         --&gt; 1 = leaf, 2 = non leaf
     * 1 * long         --&gt; parent offset
     * entries len * 40 --&gt; the entries
     * 
     * each entry is as follow
     * 4 * double --&gt; the bounding box (x1, x2, y1, y2)
     * 1 * long   --&gt; the pointer (-1 if leaf)
     * 
     * Data pages are immediatly after leaf Node pages.
     * 
     * </pre>
     * 
     * @throws TreeException
     *                 DOCUMENT ME!
     */
    public void flush() throws TreeException {
        if (!this.flushNeeded) {
            return;
        }

        FileChannel channel = this.params.getChannel();

        try {
            // Prepare buffers...
            ByteBuffer buf = this.getEmptyByteBuffer();
            ByteBuffer dataBuf = null;

            if (this.isLeaf()) {
                dataBuf = this.getEmptyByteBuffer(this.params.getDataDef());
            }

            buf.put(this.isLeaf() ? (byte) 1 : (byte) 2);
            buf.putLong(this.parentOffset);

            long pointOffset = 0;

            if (this.getEntriesCount() > 0) {
                Envelope env = null;

                for (int i = 0; i < this.getEntriesCount(); i++) {
                    env = this.entries[i].getBounds();
                    buf.putDouble(env.getMinX());
                    buf.putDouble(env.getMaxX());
                    buf.putDouble(env.getMinY());
                    buf.putDouble(env.getMaxY());

                    Object objData = this.entries[i].getData();

                    if (this.isLeaf()) {
                        this.storeKeyData(dataBuf, (Data) objData);
                        pointOffset = -1;
                    } else {
                        pointOffset = ((Long) objData).longValue();
                    }

                    buf.putLong(pointOffset);
                }
            }

            synchronized (channel) {
                if (this.offset == -1) {
                    throw new TreeException("Cannot flush a new node!");
                }

                buf.position(0);
                channel.position(this.offset);
                channel.write(buf);

                // If I'm a leaf, then store my Data
                if (this.isLeaf()) {
                    dataBuf.position(0);
                    channel.write(dataBuf);
                }

                if (this.params.getForceChannel()) {
                    channel.force(false);
                }
            }

            this.flushNeeded = false;
        } catch (IOException e) {
            throw new TreeException(e);
        }
    }

    /**
     * @see org.geotools.rtree.Node#save()
     */
    protected void doSave() throws TreeException {
        FileChannel channel = this.params.getChannel();

        try {
            // Allocate needed space for this node
            if (this.offset == -1) {
                /*
                 * synchronized (channel) { this.offset = channel.size();
                 * channel.position(this.offset);
                 * 
                 * ByteBuffer buf = this.getEmptyByteBuffer(); buf.position(0);
                 * channel.write(buf);
                 * 
                 * if (this.isLeaf()) { buf =
                 * this.getEmptyByteBuffer(this.params.getDataDef());
                 * buf.position(0); channel.write(buf); } if
                 * (this.params.getForceChannel()) { channel.force(false); } }
                 */
                int len = pageLen;

                if (this.isLeaf()) {
                    len += (this.params.getDataDef().getEncodedLen() * this.params
                            .getMaxNodeEntries());
                }

                this.offset = this.params.getNewNodeOffset(len);

                this.flushNeeded = true;
            }

            // Change parentOffset of my childrens
            if (this.isChanged && !this.isLeaf()) {
                FileSystemNode child = null;

                for (int i = 0; i < this.entriesCount; i++) {
                    child = this.params.getFromCache(((Long) this.entries[i]
                            .getData()).longValue());

                    child.setParent(this);
                }
            }

            this.params.putToCache(this);
        } catch (IOException e) {
            throw new TreeException(e);
        }

        /*
         * try { // Prepare buffers... ByteBuffer buf =
         * this.getEmptyByteBuffer(); ByteBuffer dataBuf = null; if
         * (this.isLeaf()) { dataBuf =
         * this.getEmptyByteBuffer(this.params.getDataDef()); }
         * 
         * buf.put(this.isLeaf() ? (byte)1 : (byte)2);
         * buf.putLong(this.parentOffset);
         * 
         * long pointOffset = 0; if (this.getEntriesCount() > 0) { Envelope env =
         * null; for (int i = 0; i < this.getEntriesCount(); i++) { env =
         * this.entries[i].getBounds(); buf.putDouble(env.getMinX());
         * buf.putDouble(env.getMaxX()); buf.putDouble(env.getMinY());
         * buf.putDouble(env.getMaxY());
         * 
         * Object objData = this.entries[i].getData(); if (this.isLeaf()) {
         * this.storeKeyData(dataBuf, (Data)objData); pointOffset = -1; } else {
         * pointOffset = ((Long)objData).longValue(); }
         * 
         * buf.putLong(pointOffset); } } synchronized (channel) { if
         * (this.offset == -1) { // I'm a new Node this.offset = channel.size(); }
         * 
         * buf.position(0); channel.position(this.offset); channel.write(buf);
         *  // If I'm a leaf, then store my Data if (this.isLeaf()) {
         * dataBuf.position(0); channel.write(dataBuf); }
         *  // Change parentOffset of my childrens if (this.isChanged &&
         * !this.isLeaf()) { ByteBuffer childBuf = ByteBuffer.allocate(8);
         * childBuf.putLong(this.offset); Long pos = null; for (int i = 0; i <
         * this.entriesCount; i++) { pos = (Long)this.entries[i].getData();
         * childBuf.flip(); channel.position(pos.longValue() + 1);
         * channel.write(childBuf); } }
         * 
         * if (this.params.getForceChannel()) { channel.force(false); } }
         *  } catch (IOException e) { throw new TreeException(e); }
         */
    }

    /**
     * Force this node flush
     * 
     * @throws Throwable
     *                 DOCUMENT ME!
     */
    protected void finalize() throws Throwable {
        this.flush();
    }

    /**
     * DOCUMENT ME!
     * 
     * @param buf
     * @param data
     * 
     * @throws IOException
     */
    private void storeKeyData(ByteBuffer buf, Data data) throws IOException {
        Object val = null;
        Field field = null;

        for (int i = 0; i < data.getValuesCount(); i++) {
            val = data.getValue(i);
            field = data.getDefinition().getField(i);

            if (val instanceof Short) {
                buf.putShort(((Short) val).shortValue());
            } else if (val instanceof Integer) {
                buf.putInt(((Integer) val).intValue());
            } else if (val instanceof Long) {
                buf.putLong(((Long) val).longValue());
            } else if (val instanceof Float) {
                buf.putFloat(((Float) val).floatValue());
            } else if (val instanceof Double) {
                buf.putDouble(((Double) val).doubleValue());
            } else if (val instanceof String) {
                ByteBuffer strBuffer = ByteBuffer.allocate(field
                        .getEncodedLen());

                ByteBuffer enc = data.getDefinition().getCharset().encode(
                        val.toString());

                enc.position(0);
                strBuffer.put(enc);
                strBuffer.position(0);
                buf.put(strBuffer);
            }
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @throws IOException
     */
    void free() throws IOException {
        if (this.offset < 0) {
            return;
        }

        /*
         * FileChannel channel = this.params.getChannel();
         * 
         * ByteBuffer buf = this.getEmptyByteBuffer();
         * 
         * synchronized (channel) { channel.position(this.offset);
         * channel.write(buf);
         * 
         * if (this.isLeaf()) { buf =
         * this.getEmptyByteBuffer(this.params.getDataDef());
         * channel.write(buf); }
         * 
         * if (this.params.getForceChannel()) { channel.force(false); } }
         */
        this.flushNeeded = false;
        this.params.removeFromCache(this);
        this.params.getFreePages().push(new Long(this.offset));
    }

    /**
     * @see org.geotools.rtree.Node#getEntry(org.geotools.rtree.Node)
     */
    protected Entry getEntry(Node node) {
        FileSystemNode fn = (FileSystemNode) node;

        Entry ret = null;
        Long l = null;

        for (int i = 0; i < this.getEntriesCount(); i++) {
            l = (Long) this.entries[i].getData();

            if (l.longValue() == fn.getOffset()) {
                ret = this.entries[i];

                break;
            }
        }

        return ret;
    }

    /**
     * @see org.geotools.rtree.Node#setParent(org.geotools.rtree.Node)
     */
    public void setParent(Node node) {
        if (node == null) {
            this.parentOffset = -1;
        } else {
            FileSystemNode fn = (FileSystemNode) node;
            this.parentOffset = fn.getOffset();
        }

        this.flushNeeded = true;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        FileSystemNode comp = (FileSystemNode) obj;

        return this.getOffset() == comp.getOffset();
    }
}
