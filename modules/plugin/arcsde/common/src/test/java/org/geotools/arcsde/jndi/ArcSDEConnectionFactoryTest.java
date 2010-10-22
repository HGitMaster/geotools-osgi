/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2009, Open Source Geospatial Foundation (OSGeo)
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
 *
 */
package org.geotools.arcsde.jndi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.geotools.arcsde.session.ArcSDEConnectionConfig;
import org.geotools.arcsde.session.ISession;
import org.geotools.arcsde.session.ISessionPool;
import org.geotools.arcsde.session.ISessionPoolFactory;
import org.geotools.arcsde.session.UnavailableConnectionException;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test suite for {@link ArcSDEConnectionFactory}
 * 
 * @author Gabriel Roldan (OpenGeo)
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/plugin/arcsde/common/src/test/java/org/geotools/arcsde/jndi/ArcSDEConnectionFactoryTest.java $
 * @version $Id: ArcSDEConnectionFactoryTest.java 34172 2009-10-19 20:49:25Z groldan $
 * @since 2.5.7
 */
public class ArcSDEConnectionFactoryTest {

    private ArcSDEConnectionFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = new ArcSDEConnectionFactory();
    }

    @Test
    public void getObjectInstance_NotASupportedType() throws Exception {
        String className = "not.a.supported.Class";
        String factoryName = ArcSDEConnectionFactory.class.getName();
        String factoryLocation = null;
        Reference ref = createRef(className, factoryName, factoryLocation);

        Name name = null;
        Context nameCtx = null;
        Hashtable<?, ?> environment = null;

        Object object = factory.getObjectInstance(ref, name, nameCtx, environment);
        assertNull(object);
    }

    @Test
    public void getObjectInstance_MandatoryParams() throws Exception {
        String className = ArcSDEConnectionConfig.class.getName();
        String factoryName = ArcSDEConnectionFactory.class.getName();
        String factoryLocation = null;
        Reference ref = new Reference(className, factoryName, factoryLocation);

        Name name = null;
        Context nameCtx = null;
        Hashtable<?, ?> environment = null;

        assertMandatory(ref, name, nameCtx, environment);

        ref.add(new StringRefAddr(ArcSDEConnectionConfig.SERVER_NAME_PARAM_NAME, "localhost"));
        assertMandatory(ref, name, nameCtx, environment);

        ref.add(new StringRefAddr(ArcSDEConnectionConfig.PORT_NUMBER_PARAM_NAME, "5151"));
        assertMandatory(ref, name, nameCtx, environment);

        ref.add(new StringRefAddr(ArcSDEConnectionConfig.USER_NAME_PARAM_NAME, "me"));
        assertMandatory(ref, name, nameCtx, environment);

        ref.add(new StringRefAddr(ArcSDEConnectionConfig.PASSWORD_PARAM_NAME, "mine"));

        Object object = factory.getObjectInstance(ref, name, nameCtx, environment);
        assertNotNull("We're done with mandatory params, should have worked!", object);
    }

    private void assertMandatory(Reference ref, Name name, Context nameCtx,
            Hashtable<?, ?> environment) throws Exception {
        try {
            factory.getObjectInstance(ref, name, nameCtx, environment);
            fail("Expected IAE");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    @Test
    public void getObjectInstance_ArcSDEConnectionConfig_Minimum() throws Exception {
        String className = ArcSDEConnectionConfig.class.getName();
        String factoryName = ArcSDEConnectionFactory.class.getName();
        String factoryLocation = null;
        Reference ref = createRef(className, factoryName, factoryLocation);

        Name name = null;
        Context nameCtx = null;
        Hashtable<?, ?> environment = null;

        Object object = factory.getObjectInstance(ref, name, nameCtx, environment);
        assertNotNull(object);
        assertTrue(object instanceof ArcSDEConnectionConfig);

        Object object2 = factory.getObjectInstance(ref, name, nameCtx, environment);

        assertFalse(object == object2);
        assertEquals(object, object2);
        ArcSDEConnectionConfig config = (ArcSDEConnectionConfig) object;
        assertEquals("localhost", config.getServerName());
        assertEquals(Integer.valueOf(5151), config.getPortNumber());
        assertEquals("sde", config.getDatabaseName());
        assertEquals("sdeusr", config.getUserName());
        assertEquals("s3cr3t", config.getPassword());
    }

    @Test
    public void getObjectInstance_ArcSDEConnectionConfig_Full() throws Exception {
        String className = ArcSDEConnectionConfig.class.getName();
        String factoryName = ArcSDEConnectionFactory.class.getName();
        String factoryLocation = null;
        Reference ref = createRef(className, factoryName, factoryLocation);
        // add the non mandatory params
        ref.add(new StringRefAddr(ArcSDEConnectionConfig.MIN_CONNECTIONS_PARAM_NAME, "2"));
        ref.add(new StringRefAddr(ArcSDEConnectionConfig.MAX_CONNECTIONS_PARAM_NAME, "6"));
        ref.add(new StringRefAddr(ArcSDEConnectionConfig.CONNECTION_TIMEOUT_PARAM_NAME, "2000"));

        Name name = null;
        Context nameCtx = null;
        Hashtable<?, ?> environment = null;

        Object object = factory.getObjectInstance(ref, name, nameCtx, environment);
        assertNotNull(object);
        assertTrue(object instanceof ArcSDEConnectionConfig);

        Object object2 = factory.getObjectInstance(ref, name, nameCtx, environment);

        assertFalse(object == object2);
        assertEquals(object, object2);

        ArcSDEConnectionConfig config = (ArcSDEConnectionConfig) object;
        assertEquals(Integer.valueOf(2), config.getMinConnections());
        assertEquals(Integer.valueOf(6), config.getMaxConnections());
        assertEquals(Integer.valueOf(2000), config.getConnTimeOut());

    }

    @Test
    public void getObjectInstance_ISessionPool() throws Exception {
        String className = ISessionPool.class.getName();
        String factoryName = ArcSDEConnectionFactory.class.getName();
        String factoryLocation = null;
        Reference ref = createRef(className, factoryName, factoryLocation);

        Name name = null;
        Context nameCtx = null;
        Hashtable<?, ?> environment = null;

        factory.setClosableSessionPoolFactory(new MockSessionPoolFactory());

        Object object = factory.getObjectInstance(ref, name, nameCtx, environment);
        assertNotNull(object);
        assertTrue(object instanceof SharedSessionPool);
        ArcSDEConnectionConfig config = ((SharedSessionPool) object).getConfig();
        assertNotNull(config);
        assertEquals("localhost", config.getServerName());

        Reference ref2 = createRef(className, factoryName, factoryLocation);
        Object object2 = factory.getObjectInstance(ref2, name, nameCtx, environment);
        assertSame(object, object2);
    }

    private Reference createRef(String className, String factoryName, String factoryLocation) {
        Reference ref = new Reference(className, factoryName, factoryLocation);
        ref.add(new StringRefAddr(ArcSDEConnectionConfig.SERVER_NAME_PARAM_NAME, "localhost"));
        ref.add(new StringRefAddr(ArcSDEConnectionConfig.PORT_NUMBER_PARAM_NAME, "5151"));
        ref.add(new StringRefAddr(ArcSDEConnectionConfig.INSTANCE_NAME_PARAM_NAME, "sde"));
        ref.add(new StringRefAddr(ArcSDEConnectionConfig.USER_NAME_PARAM_NAME, "sdeusr"));
        ref.add(new StringRefAddr(ArcSDEConnectionConfig.PASSWORD_PARAM_NAME, "s3cr3t"));
        return ref;
    }

    private static class MockSessionPoolFactory implements ISessionPoolFactory {

        public ISessionPool createPool(ArcSDEConnectionConfig config) throws IOException {
            return new MockSessionPool(config);
        }
    }

    private static class MockSessionPool implements ISessionPool {

        private ArcSDEConnectionConfig config;

        public MockSessionPool(ArcSDEConnectionConfig config) {
            this.config = config;
        }

        public ArcSDEConnectionConfig getConfig() {
            return config;
        }

        public void close() {
        }

        public int getAvailableCount() {
            return 0;
        }

        public int getInUseCount() {
            return 0;
        }

        public int getPoolSize() {
            return 0;
        }

        public ISession getSession() throws IOException, UnavailableConnectionException {
            return null;
        }

        public ISession getSession(boolean transactional) throws IOException, UnavailableConnectionException {
            return null;
        }

        public boolean isClosed() {
            return false;
        }
    }
}
