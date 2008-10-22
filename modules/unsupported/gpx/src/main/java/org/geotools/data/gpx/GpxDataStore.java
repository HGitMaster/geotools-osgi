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
package org.geotools.data.gpx;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.geotools.data.AbstractDataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.gpx.GPXConfiguration;
import org.geotools.gpx.bean.GpxType;
import org.geotools.gpx.bean.RteType;
import org.geotools.gpx.bean.TrkType;
import org.geotools.gpx.bean.WptType;
import org.geotools.referencing.crs.DefaultCompoundCRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.crs.DefaultTemporalCRS;
import org.geotools.referencing.crs.DefaultVerticalCRS;
import org.geotools.referencing.cs.DefaultTimeCS;
import org.geotools.referencing.datum.DefaultTemporalDatum;
import org.geotools.xml.Parser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;


public class GpxDataStore extends AbstractDataStore {
    /** The logger for the GML Data Store */
    static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.data.gpx.GpxDataStore");
    
    public static final String TYPE_NAME_POINT = "waypoint";
    public static final String TYPE_NAME_TRACK = "track";
    public static final String TYPE_NAME_ROUTE = "route";
 
    
    public static final DefaultTemporalCRS gpxTemporalCRS = new DefaultTemporalCRS("UNIX", DefaultTemporalDatum.UNIX, DefaultTimeCS.DAYS);
    public static final CoordinateReferenceSystem gpxCRS = 
        new DefaultCompoundCRS("gpxCrs", 
                DefaultGeographicCRS.WGS84, 
                DefaultVerticalCRS.ELLIPSOIDAL_HEIGHT, 
                gpxTemporalCRS);
    
    private final URL url;
    /**
     * Contains full file path in case of "file://" URL.
     * Otherwise null.
     */
    private final File localFile;
    
    private final String namespace;
    private final GpxType gpxData;
    private SimpleFeatureType pointType;
    private SimpleFeatureType trackType;
    private SimpleFeatureType routeType;

    private MemoryLock lock = new MemoryLock();
    
    public GpxDataStore(URL url, String namespace) throws IOException, SAXException, ParserConfigurationException, URISyntaxException {
        super(true);  //   we are an r/w data store
        
        // decode the URL, if it contains %xx notation
        String filePart = null;

        if (url == null) {
            throw new NullPointerException("Null URL for GpxDataStore");
        }

        try {
            filePart = URLDecoder.decode(url.getFile(), "UTF-8");
        } catch (java.io.UnsupportedEncodingException use) {
            throw new MalformedURLException("Unable to decode " + url + " cause " + use.getMessage());
        }

        this.url = new URL(url.getProtocol(), url.getHost(), url.getPort(), filePart);
        if("file".equals(url.getProtocol())) {
            localFile = new File(this.url.getFile());
            LOGGER.fine("Opening GPX file. Full path: " + filePart);
        } else {
            localFile = null;
            LOGGER.fine("Opening non-file GPX url. Full url: " + url);
        }
        
        //this.namespace = new URI(url.getProtocol(), null, url.getHost(), url.getPort(), filename, null, null);

        // specify namespace. If not supplied, filename used by default
        if(namespace == null) {
            int slashIndex = filePart.lastIndexOf('/');
            if(slashIndex == -1)
                namespace = filePart;
            else
                namespace = filePart.substring(slashIndex+1);
        }
            
        this.namespace = URLEncoder.encode(namespace, "UTF-8");
        
        // inint and load
        init();
        
        // now we check if the file exists. We can open an unexistent file, which produces an empty database
        // which can be filled with features.
        if(localFile != null && !localFile.exists()) {
            LOGGER.info("Given GPX file does not exists. Creating new GPX database.");
            gpxData = new GpxType();
        } else {
            gpxData = loadGpx();
        }
    }
    
    private void init() throws URISyntaxException {
        SimpleFeatureTypeBuilder ftb = new SimpleFeatureTypeBuilder();
        AttributeTypeBuilder atb = new AttributeTypeBuilder();

        /* point type
        protected double lat;
        protected double lon;
        protected double ele;
        protected Calendar time;

        protected String name;
        protected String desc;
        protected String cmt;

        protected double magvar;
        protected double geoidheight;
        protected String src;
        protected List<LinkType> link;
        protected String sym;
        protected String type;
        protected String fix;
        protected int sat;
        protected double hdop;
        protected double vdop;
        protected double pdop;
        protected double ageofdgpsdata;
        protected int dgpsid;
        protected ExtensionsType extensions;
        */
        
        ftb.setName(TYPE_NAME_POINT);

        atb.name("geometry")
            .binding(Point.class)
            .nillable(true)
            .length(0)
            .defaultValue(null)
            .crs(gpxCRS);
        ftb.add(atb.buildDescriptor("geometry"));

        atb.name("name").binding(String.class).nillable(true);
        ftb.add(atb.buildDescriptor("name"));

        atb.name("description").binding(String.class);
        ftb.add(atb.buildDescriptor("description"));

        atb.name("comment").binding(String.class);
        ftb.add(atb.buildDescriptor("comment"));

        ftb.setNamespaceURI(new URI(namespace));

        pointType = ftb.buildFeatureType();

        
        /*
        protected List<TrksegType> trkseg;

        protected String name;
        protected String desc;
        protected String cmt;

        protected String src;
        protected List<LinkType> link;
        protected int number;
        protected String type;
        protected ExtensionsType extensions;
            */
        
        
        // I guess ftb is reuseable... 
        // ftb = new SimpleFeatureTypeBuilder();
        ftb.setName(TYPE_NAME_TRACK);

        atb.name("geometry")
            .binding(MultiLineString.class)
            .nillable(true)
            .length(0)
            .defaultValue(null)
            .crs(gpxCRS);
        ftb.add(atb.buildDescriptor("geometry"));

        atb.name("name").binding(String.class).nillable(true);
        ftb.add(atb.buildDescriptor("name"));

        atb.name("description").binding(String.class);
        ftb.add(atb.buildDescriptor("description"));

        atb.name("comment").binding(String.class);
        ftb.add(atb.buildDescriptor("comment"));


        ftb.setNamespaceURI(new URI(namespace));

        trackType = ftb.buildFeatureType();

        
        
        /*
        protected List<WptType> rtept;

        protected String name;
        protected String desc;
        protected String cmt;

        protected String src;
        protected List<LinkType> link;
        protected int number;
        protected String type;
        protected ExtensionsType extensions;
        */
        
        // I guess ftb is reuseable... 
        // ftb = new SimpleFeatureTypeBuilder();
        ftb.setName(TYPE_NAME_ROUTE);

        atb.name("geometry")
            .binding(LineString.class)
            .nillable(true)
            .length(0)
            .defaultValue(null)
            .crs(gpxCRS);
        ftb.add(atb.buildDescriptor("geometry"));

        atb.name("name").binding(String.class).nillable(true);
        ftb.add(atb.buildDescriptor("name"));

        atb.name("description").binding(String.class);
        ftb.add(atb.buildDescriptor("description"));

        atb.name("comment").binding(String.class);
        ftb.add(atb.buildDescriptor("comment"));


        ftb.setNamespaceURI(new URI(namespace));

        routeType = ftb.buildFeatureType();

    }

    private GpxType loadGpx() throws IOException, SAXException, ParserConfigurationException {
        
        // LOCKING: This is a write to the memory, and a read from the file.
        // first, lock the memory. 
        lock.acquireWriteLock();
        try {
            return loadGpxMemoryLocked();
        } finally {
            lock.releaseWriteLock();
        }
        
    }
    
    private GpxType loadGpxMemoryLocked() throws IOException, SAXException, ParserConfigurationException {
        // this part of the loader is running with the memory locked for writing
        
        InputStream in = null;
        FileLock fileLock = null;
        GpxType gpx;

        try {
            if(localFile != null) {
                // in this csae we are loading from a file, that has to be locked before using
                RandomAccessFile raf = new RandomAccessFile(localFile, "rw"); // "rw" for locking
                FileChannel channel = raf.getChannel();
                fileLock = channel.lock();
                in = new BufferedInputStream(Channels.newInputStream(channel), 8192);
            } else {
                in = url.openStream();
            }
            
            // build a parser, and parse the stream
            GPXConfiguration configuration = new GPXConfiguration();
            Parser parser = new Parser(configuration);
            
            gpx = (GpxType) parser.parse(in);
            
        } finally {
            
            if( in != null )
                in.close();
            
            // closing the stream drops the lock
            // if( fileLock != null )
            //     fileLock.release();

        }
        
        
        // we have to check the fields, that we're going to use as ID.
        // ther is no constraint in the format, that it should be unique,
        // so we have to check for it, and modify if needed.
        
        // 1. - waypoints. ID: name property
        Set<String> wptIds = new TreeSet<String>();
        Iterator<WptType> wpts = gpx.getWpt().iterator();
        while (wpts.hasNext()) {
            WptType wpt = wpts.next();
            
            String id = wpt.getName();
            if(wptIds.contains(id)) {
                id = extendId(wptIds, id);
                wpt.setName(id);
            }
            wptIds.add(id);
        }

        // 2. - tracks. ID: name property
        Set<String> trkIds = new TreeSet<String>();
        Iterator<TrkType> trks = gpx.getTrk().iterator();
        while (trks.hasNext()) {
            TrkType trk = trks.next();
            
            String id = trk.getName();
            if(wptIds.contains(id)) {
                id = extendId(trkIds, id);
                trk.setName(id);
            }
            trkIds.add(id);
        }
        
        // 3. - routes. ID: name property
        Set<String> rteIds = new TreeSet<String>();
        Iterator<RteType> rtes = gpx.getRte().iterator();
        while (rtes.hasNext()) {
            RteType rte = rtes.next();
            
            String id = rte.getName();
            if(rteIds.contains(id)) {
                id = extendId(rteIds, id);
                rte.setName(id);
            }
            rteIds.add(id);
        }
        
        return gpx;
    }
    
    private String extendId(Set<String> ids, String base) {
        String id = base;
        int suffix = 1;
        while(ids.contains(id)) {
            id = base + "_" + suffix;
        }
        return id;
    }

    @Override
    protected  FeatureReader<SimpleFeatureType, SimpleFeature> getFeatureReader(String typeName) {
        return new GpxFeatureReader(this, typeName);
    }

    @Override
    protected FeatureWriter<SimpleFeatureType, SimpleFeature> createFeatureWriter(String typeName, Transaction transaction) throws IOException {
        return new GpxFeatureWriter(this, typeName, transaction);
    }

    @Override
    public SimpleFeatureType getSchema(String typeName) {
        if (TYPE_NAME_POINT.equals(typeName)) {
            return pointType;
        } else if (TYPE_NAME_TRACK.equals(typeName)) {
            return trackType;
        } else if (TYPE_NAME_ROUTE.equals(typeName)) {
            return routeType;
        } else {
            throw new IllegalArgumentException("No such type: " + typeName);
        }
    }

    @Override
    public String[] getTypeNames() throws IOException {
        return new String[] { TYPE_NAME_POINT, TYPE_NAME_TRACK, TYPE_NAME_ROUTE };
    }
    
    GpxType getGpxData() {
        return gpxData;
    }
    
    MemoryLock getMemoryLock() {
        return lock;
    }
}
