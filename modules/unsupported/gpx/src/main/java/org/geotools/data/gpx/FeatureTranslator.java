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
package org.geotools.data.gpx;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

import org.geotools.data.gpx.temporal.TemporalCoordinate;
import org.geotools.data.gpx.temporal.TemporalCoordinateSequence;
import org.geotools.data.gpx.temporal.TemporalCoordinateSequenceFactory;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.gpx.bean.RteType;
import org.geotools.gpx.bean.TrkType;
import org.geotools.gpx.bean.TrksegType;
import org.geotools.gpx.bean.WptType;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

/**
 * This class is no thread safe. It uses a SimpleFeatureBuilder,
 * which is not thread safe.
 * 
 * @author Peter Bolla
 *
 */
public class FeatureTranslator {

    private final SimpleFeatureBuilder builder;
    private final GeometryFactory geomFactory;
    private final Object[] attrs;
    private final SimpleFeatureType featureType;
    
    FeatureTranslator(SimpleFeatureType featureType) {
        builder = new SimpleFeatureBuilder(featureType);
        
        geomFactory = new GeometryFactory(new TemporalCoordinateSequenceFactory());

        attrs = new Object[featureType.getAttributeCount()];
        
        this.featureType = featureType;
    }

    public SimpleFeature convertFeature(WptType type) {
        TemporalCoordinate ptCoord;
        if(type.getTime() == null)
            ptCoord = new TemporalCoordinate(type.getLon(), type.getLat(), type.getEle());
        else
            ptCoord = new TemporalCoordinate(type.getLon(), type.getLat(), type.getEle(), toValue(type.getTime()));
            
        Point pt = geomFactory.createPoint(ptCoord);
        
        attrs[0]=pt;
        attrs[1]=type.getName();
        attrs[2]=type.getDesc();
        attrs[3]=type.getCmt();
        
        try {
            return SimpleFeatureBuilder.build((SimpleFeatureType)featureType, attrs, type.getName());
        } catch (IllegalAttributeException e) {
            throw new RuntimeException("illegal attributes", e);
        }
    }
    
    public SimpleFeature convertFeature(TrkType type) {
        ArrayList<LineString> lineStrings = new ArrayList<LineString>();
        Iterator<TrksegType> it = type.getTrkseg().iterator();

        while (it.hasNext()) {
            TrksegType segment = it.next();

            ArrayList<Coordinate> lineStringCoords = new ArrayList<Coordinate>();

            Iterator<WptType> it2 = segment.getTrkpt().iterator();

            while (it2.hasNext()) {
                WptType node = it2.next();
                TemporalCoordinate coord;
                if(node.getTime() == null)
                    coord = new TemporalCoordinate(node.getLon(), node.getLat(), node.getEle());
                else
                    coord = new TemporalCoordinate(node.getLon(), node.getLat(), node.getEle(), toValue(node.getTime()));
                lineStringCoords.add(coord);
            }

            LineString line = geomFactory.createLineString(lineStringCoords.toArray(
                        new Coordinate[lineStringCoords.size()]));
            lineStrings.add(line);
        }

        MultiLineString geom = geomFactory.createMultiLineString(lineStrings.toArray(
                        new LineString[lineStrings.size()]));

        attrs[0]=geom;
        attrs[1]=type.getName();
        attrs[2]=type.getDesc();
        attrs[3]=type.getCmt();
        
        try {
            return SimpleFeatureBuilder.build((SimpleFeatureType)featureType, attrs, type.getName());
        } catch (IllegalAttributeException e) {
            throw new RuntimeException("illegal attributes", e);
        }
    }
    
    public SimpleFeature convertFeature(RteType type) {

        ArrayList<Coordinate> lineStringCoords = new ArrayList<Coordinate>();

        Iterator<WptType> it2 = type.getRtept().iterator();
        
        while (it2.hasNext()) {
            WptType coord = it2.next();
            lineStringCoords.add(new Coordinate(coord.getLon(), coord.getLat(), coord.getEle()));
        }

        LineString geom = geomFactory.createLineString(lineStringCoords.toArray(
                    new Coordinate[lineStringCoords.size()]));

        attrs[0]=geom;
        attrs[1]=type.getName();
        attrs[2]=type.getDesc();
        attrs[3]=type.getCmt();
        
        try {
            return SimpleFeatureBuilder.build((SimpleFeatureType)featureType, attrs, type.getName());
        } catch (IllegalAttributeException e) {
            throw new RuntimeException("illegal attributes", e);
        }
    }
    
    public WptType createWpt(SimpleFeature f) {
        WptType wpt = new WptType();
        convertFeature(f, wpt);
        return wpt;
    }
    
    public boolean convertFeature(SimpleFeature f, WptType wpt) {
        boolean changed = false;
        
        
        CoordinateSequence cs = ((Point) f.getDefaultGeometry()).getCoordinateSequence();
        
        double lon = cs.getOrdinate(0, 0);
        double lat = cs.getOrdinate(0, 1);
        double ele;
        if(cs.getDimension() >=3)
            ele = cs.getOrdinate(0, 2);
        else
            ele = Double.NaN;
        Calendar cal;
        if(cs instanceof TemporalCoordinateSequence)
            cal = toDate( ((TemporalCoordinateSequence)cs).getOrdinate(0, 3) );
        else
            cal = null;
        
        if(wpt.getLon() != lon) {
            wpt.setLon(lon);
            changed = true;
        }
        if(wpt.getLat() != lat) {
            wpt.setLat(lat);
            changed = true;
        }
        if(wpt.getEle() != ele) {
            wpt.setEle(ele);
            changed = true;
        }
        if( (cal != null || wpt.getTime() != null) && ( cal == null || !cal.equals(wpt.getTime())) ) {
            wpt.setTime(cal);
            changed = true;
        }
        
        String name = (String) f.getAttribute(1);
        if( (name != null || wpt.getName() != null) && ( name == null || !name.equals(wpt.getName()))) {
            wpt.setName(name);
            changed = true;
        }
        
        String desc = (String) f.getAttribute(2);
        if( (desc != null || wpt.getName() != null) && ( desc == null || !desc.equals(wpt.getName())) ) {
            wpt.setName(desc);
            changed = true;
        }
        
        String cmt = (String) f.getAttribute(3);
        if( (cmt != null || wpt.getCmt() != null) && (cmt == null || !cmt.equals(wpt.getCmt())) ) {
            wpt.setCmt(cmt);
            changed = true;
        }
        
        return changed;
    }
    
    public TrkType createTrk(SimpleFeature f) {
        TrkType trk = new TrkType();
        convertFeature(f, trk);
        return trk;
    }
    
    public boolean convertFeature(SimpleFeature f, TrkType trk) {
        boolean changed = false;
        
        boolean geometryChanged = false;
        MultiLineString geom = ((MultiLineString) f.getAttribute(0));
        int segCnt = trk.getTrkseg().size();
        if(segCnt != geom.getNumGeometries())
            geometryChanged = true;
        else {
            Iterator<TrksegType> it = trk.getTrkseg().iterator();
            
outer:
            for (int i = 0; it.hasNext(); i++) {
                TrksegType trkSeg = it.next();
                CoordinateSequence cs = ((LineString)geom.getGeometryN(i)).getCoordinateSequence();
                
                if(trkSeg.getTrkpt().size() != cs.size()) {
                    geometryChanged = true;
                    break;
                }
                
                Iterator<WptType> pts = trkSeg.getTrkpt().iterator();
                for(int j = 0; pts.hasNext(); j++) {
                    WptType pt = (WptType) pts.next();
                    
                    double lon = cs.getOrdinate(j, 0);
                    double lat = cs.getOrdinate(j, 1);
                    double ele;
                    if(cs.getDimension() >=3)
                        ele = cs.getOrdinate(j, 2);
                    else
                        ele = Double.NaN;
                    Calendar cal;
                    if(cs instanceof TemporalCoordinateSequence)
                        cal = toDate( ((TemporalCoordinateSequence)cs).getOrdinate(j, 3) );
                    else
                        cal = null;
                    
                    if(pt.getLon() != lon) {
                        geometryChanged = true;
                        break outer;
                    }
                    if(pt.getLat() != lat) {
                        geometryChanged = true;
                        break outer;
                    }
                    if(pt.getEle() != ele) {
                        geometryChanged = true;
                        break outer;
                    }
                    if( (cal != null || pt.getTime() != null) && ( cal == null || !cal.equals(pt.getTime())) ) {
                        geometryChanged = true;
                        break outer;
                    }
                    
                    
                }
                
            }
        }
        
        if(geometryChanged) {
            // totally rebuild the geometry representation of the track. With this we may loose data...
            
            trk.getTrkseg().clear();
            segCnt = geom.getNumGeometries();
            for(int i = 0; i < segCnt; i++) {
                LineString segment = (LineString) geom.getGeometryN(i);
                TrksegType trkSeg = new TrksegType();

                CoordinateSequence cs = segment.getCoordinateSequence();
                for(int j = 0; j < cs.size(); j++) {

                    double lon = cs.getOrdinate(j, 0);
                    double lat = cs.getOrdinate(j, 1);
                    double ele;
                    if(cs.getDimension() >=3)
                        ele = cs.getOrdinate(j, 2);
                    else
                        ele = Double.NaN;
                    Calendar cal;
                    if(cs instanceof TemporalCoordinateSequence)
                        cal = toDate( ((TemporalCoordinateSequence)cs).getOrdinate(j, 3) );
                    else
                        cal = null;
                    
                    WptType pt = new WptType();
                    pt.setLon(lon);
                    pt.setLat(lat);
                    pt.setEle(ele);
                    pt.setTime(cal);
                    
                    trkSeg.getTrkpt().add(pt);
                    
                }
                
                trk.getTrkseg().add(trkSeg);
                
            }
            
            changed = true;
        }
        
        String name = (String) f.getAttribute(1);
        if( (name != null || trk.getName() != null) && ( name == null || !name.equals(trk.getName()))) {
            trk.setName(name);
            changed = true;
        }
        
        String desc = (String) f.getAttribute(2);
        if( (desc != null || trk.getName() != null) && ( desc == null || !desc.equals(trk.getName())) ) {
            trk.setName(desc);
            changed = true;
        }
        
        String cmt = (String) f.getAttribute(3);
        if( (cmt != null || trk.getCmt() != null) && (cmt == null || !cmt.equals(trk.getCmt())) ) {
            trk.setCmt(cmt);
            changed = true;
        }
        
        return changed;
    }

    public RteType createRte(SimpleFeature f) {
        RteType rte = new RteType();
        convertFeature(f, rte);
        return rte;
    }
    
    public boolean convertFeature(SimpleFeature f, RteType rte) {
        return false;
    }

    private Calendar toDate(double time) {
        if(time == Double.NaN)
            return null;
        
        Calendar cal = new GregorianCalendar();
        cal.setTime(GpxDataStore.gpxTemporalCRS.toDate(time));
        return cal;
    }
    
    private double toValue(Calendar cal) {
        if(cal == null)
            return Double.NaN;
        
        return GpxDataStore.gpxTemporalCRS.toValue(cal.getTime());
    }
    
}
