/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.hsql;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.Statement;
import java.util.logging.Logger;

import org.geotools.data.AbstractDataStoreTest;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.Transaction;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.filter.FidFilter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;


/**
 * DOCUMENT ME!
 *
 * @author Jody Garnett, Refractions Research
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/hsql/src/test/java/org/geotools/data/hsql/HsqlDataStoreTest.java $
 */
public class HsqlDataStoreTest extends AbstractDataStoreTest {
	private HsqlConnectionFactory connFactory;
    /** The logger for the filter module. */
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.hsql");

    /**
     * Constructor for MemoryDataStoreTest.
     *
     * @param arg0
     */
    public HsqlDataStoreTest(String arg0) {
        super(arg0);
    }

    
    
    protected void dataSetUp() throws Exception {
    	String namespace = getName();
        roadType = DataUtilities.createType(namespace+".ROAD",
                "ID:0,GEOM:LineString,NAME:String");
        subRoadType = DataUtilities.createType(namespace+"ROAD",
                "ID:0,GEOM:LineString");
        gf = new GeometryFactory();

        roadFeatures = new SimpleFeature[3];

        //           3,2
        //  2,2 +-----+-----+ 4,2
        //     /     rd1     \
        // 1,1+               +5,1
        roadFeatures[0] = SimpleFeatureBuilder.build(roadType,new Object[] {
                new Integer(1),
                line(new int[] { 1, 1, 2, 2, 4, 2, 5, 1 }),
                "r1",
            },
            "road.rd1"
        );

        //       + 3,4
        //       + 3,3
        //  rd2  + 3,2
        //       |
        //    3,0+
        roadFeatures[1] = SimpleFeatureBuilder.build(roadType,new Object[] {
                new Integer(2), line(new int[] { 3, 0, 3, 2, 3, 3, 3, 4 }),
                "r2"
            },
            "road.rd2"
        );

        //     rd3     + 5,3
        //            / 
        //  3,2 +----+ 4,2
        roadFeatures[2] = SimpleFeatureBuilder.build(roadType,new Object[] {
                new Integer(3),
                line(new int[] { 3, 2, 4, 2, 5, 3 }), "r3"
            },
            "road.rd3"
        );
        roadBounds = new ReferencedEnvelope();
        roadBounds.include( roadFeatures[0].getBounds() );
        roadBounds.include( roadFeatures[1].getBounds() );
        roadBounds.include( roadFeatures[2].getBounds() );
        
        FilterFactory factory = FilterFactoryFinder.createFilterFactory();
        rd1Filter = factory.createFidFilter("0");
        rd2Filter = factory.createFidFilter("1");

        FidFilter create = factory.createFidFilter();
        create.addFid("0");
        create.addFid("1");
        
        rd12Filter = create;
        
        rd12Bounds = new ReferencedEnvelope();
        rd12Bounds.include(roadFeatures[0].getBounds());
        rd12Bounds.include(roadFeatures[1].getBounds());        
        //   + 2,3
        //  / rd4
        // + 1,2
        newRoad = SimpleFeatureBuilder.build(roadType,new Object[] {
                    new Integer(4), line(new int[] { 1, 2, 2, 3 }), "r4"
                }, "road.rd4");

        riverType = DataUtilities.createType(namespace+".RIVER",
                "ID:0,GEOM:MultiLineString,RIVER:String,FLOW:0.0");
        subRiverType = DataUtilities.createType(namespace+".RIVER",
                "RIVER:String,FLOW:0.0");
        gf = new GeometryFactory();
        riverFeatures = new SimpleFeature[2];

        //       9,7     13,7
        //        +------+
        //  5,5  /
        //  +---+ rv1
        //   7,5 \
        //    9,3 +----+ 11,3
        riverFeatures[0] = SimpleFeatureBuilder.build(riverType,new Object[] {
                    new Integer(1),
                    lines(new int[][] {
                            { 5, 5, 7, 4 },
                            { 7, 5, 9, 7, 13, 7 },
                            { 7, 5, 9, 3, 11, 3 }
                        }), "rv1", new Double(4.5)
                }, "river.rv1");

        //         + 6,10    
        //        /
        //    rv2+ 4,8
        //       |
        //   4,6 +
        riverFeatures[1] = SimpleFeatureBuilder.build(riverType,new Object[] {
                    new Integer(2),
                    lines(new int[][] {
                            { 4, 6, 4, 8, 6, 10 }
                        }), "rv2", new Double(3.0)
                }, "river.rv2");
        riverBounds = new ReferencedEnvelope();
        riverBounds.include( riverFeatures[0].getBounds());
        riverBounds.include( riverFeatures[1].getBounds());
                
        rv1Filter = FilterFactoryFinder.createFilterFactory().createFidFilter("0");

        //  9,5   11,5   
        //   +-----+
        //     rv3  \ 
        //           + 13,3
        //                     
        newRiver = SimpleFeatureBuilder.build(riverType,new Object[] {
                new Integer(3),
                lines(new int[][] {
                        { 9, 5, 11, 5, 13, 3 }
                    }), "rv3", new Double(1.5)
            },
            "river.rv3"
        );
        
        lakeType = DataUtilities.createType(namespace+".LAKE",
                    "ID:0,GEOM:Polygon:nillable,NAME:String");
        lakeFeatures = new SimpleFeature[1];
        //             + 14,8
        //            / \
        //      12,6 +   + 16,6
        //            \  | 
        //        14,4 +-+ 16,4
        //
        lakeFeatures[0] = SimpleFeatureBuilder.build(lakeType, new Object[]{
                new Integer(0),
                polygon( new int[]{ 12,6, 14,8, 16,6, 16,4, 14,4, 12,6} ),
                "muddy"
            },
            "lake.lk1"
        );
        lakeBounds = new ReferencedEnvelope();
        lakeBounds.include(lakeFeatures[0].getBounds());
		
	}



    public DataStore createDataStore() throws Exception {      
        connFactory = new HsqlConnectionFactory("tempDB", "sa", "");

        DataStore hsql=null;
        hsql = new HsqlDataStore(connFactory, null, getName());

        File dir = new File(".");
        String found[] = dir.list( new FilenameFilter(){
            public boolean accept( File dir, String name ) {
                return name.startsWith("tempDB");
            }            
        });
        for( int i=0; i < found.length; i++ ){
            new File( found[i] ).deleteOnExit();
        }
        return hsql;
    }



    public DataStore tearDownDataStore(DataStore data) throws Exception {
        ((HsqlDataStore)data).removeSchema(roadType);
        ((HsqlDataStore)data).removeSchema(riverType);
        Connection conn = ((HsqlDataStore)data).getConnection(Transaction.AUTO_COMMIT);
        Statement st = conn.createStatement();
        st.execute("SHUTDOWN");
        st.close();
        conn.close();
        File file=new File("tempDB.log");
        if( file.exists())
            file.delete();
        file=new File("tempDB.properties");
        if( file.exists())
            file.delete();
        file=new File("tempDB.data");
        if( file.exists())
            file.delete();
        file=new File("tempDB.script");
        if( file.exists())
            file.delete();
        file=new File("tempDB.backup");
        if( file.exists())
            file.delete();
        file=new File("tempDB.lck");
        if( file.exists())
            file.delete();
        return null;
    }

}
