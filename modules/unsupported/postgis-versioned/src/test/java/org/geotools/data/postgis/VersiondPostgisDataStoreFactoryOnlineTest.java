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
package org.geotools.data.postgis;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.geotools.data.DataSourceException;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;

/**
 * Test Params used by PostgisDataStoreFactory.
 * 
 * @author aaime
 * @author $Author: jive $ (last modification)
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/postgis-versioned/src/test/java/org/geotools/data/postgis/VersiondPostgisDataStoreFactoryOnlineTest.java $
 * @version $Id: VersiondPostgisDataStoreFactoryOnlineTest.java 30682 2008-06-13 10:30:18Z acuster $
 */
public class VersiondPostgisDataStoreFactoryOnlineTest extends TestCase {
    static VersionedPostgisDataStoreFactory factory
        = new VersionedPostgisDataStoreFactory();
    
    Map remote;
    Map local;

	private PostgisTests.Fixture f;
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
         
    	f = PostgisTests.newFixture("versioned.properties");
		remote = new HashMap();
        remote.put("dbtype","postgis-versioned");        
        remote.put("charset", "");
        remote.put("host",f.host);
        remote.put("port", f.port);
        remote.put("database", f.database);
        remote.put("user", f.user);
        remote.put("passwd", f.password);
        remote.put("namespace", f.namespace);
        
        super.setUp();
    }

    public void testRemote() throws Exception {
        Map map = remote;
        
        assertEquals( f.database, factory.DATABASE.lookUp(map) );        
        assertEquals( "postgis-versioned", factory.DBTYPE.lookUp(map) );
        assertEquals( f.host, factory.HOST.lookUp(map) );
        assertEquals( f.namespace, factory.NAMESPACE.lookUp(map) );
        assertEquals( f.password, factory.PASSWD.lookUp(map) );
        assertEquals( f.port, factory.PORT.lookUp(map) );
        assertEquals( f.user, factory.USER.lookUp(map) );
        
        assertTrue( "canProcess", factory.canProcess(map));
        try {
            DataStore temp = factory.createDataStore(map);
            assertNotNull( "created", temp );
        }
        catch( DataSourceException expected){
        	assertTrue( expected.getMessage().startsWith("Connection failed:"));
        }               
    }    
    
    public void testLookup() throws Exception {
        DataStore ds = DataStoreFinder.getDataStore(remote);
        assertNotNull(ds);
        assertTrue(ds instanceof VersionedPostgisDataStore);
    }
    
    public void testVersioned() throws Exception {
        remote.put("version enable all", Boolean.TRUE);
        VersionedPostgisDataStore ds = (VersionedPostgisDataStore) DataStoreFinder.getDataStore(remote);
        assertTrue(ds.isVersioned("road"));
        assertTrue(ds.isVersioned("river"));
    }
}
