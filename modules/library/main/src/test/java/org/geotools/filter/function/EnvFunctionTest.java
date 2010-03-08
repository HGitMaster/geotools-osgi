/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2009, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.filter.function;

import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import org.geotools.factory.CommonFactoryFinder;
import org.junit.After;
import org.junit.Test;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import static org.junit.Assert.*;

/**
 * @author Andrea Aime
 * @author Michael Bedward
 * @since 2.6
 * @source $URL: $
 * @version $Id $
 */
public class EnvFunctionTest {

    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

    @After
    public void tearDown() {
        EnvFunction.clearGlobalValues();
        EnvFunction.clearLocalValues();
    }

    /**
     * Tests the use of two thread-local tables with same var names and different values
     */
    @Test
    public void testSetLocalValues() throws Exception {
        System.out.println("   setLocalValues");

        final Map<String, Object> thread0Values = new LinkedHashMap<String, Object>();
        thread0Values.put("foo", 1);
        thread0Values.put("bar", 2);

        final Map<String, Object> thread1Values = new LinkedHashMap<String, Object>();
        thread1Values.put("foo", 10);
        thread1Values.put("bar", 20);

        final CountDownLatch[] latch = new CountDownLatch[2];
        latch[0] = new CountDownLatch(1);
        latch[1] = new CountDownLatch(1);

        Runnable r0 = new Runnable() {
            public void run() {
                EnvFunction.setLocalValues(thread0Values);
                try {
                    latch[0].await();
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }

                for (String name : thread0Values.keySet()) {
                    Object result = ff.function("env", ff.literal(name)).evaluate(null);
                    int value = ((Number) result).intValue();
                    assertEquals(thread0Values.get(name), value);
                }
            }
        };

        Runnable r1 = new Runnable() {
            public void run() {
                EnvFunction.setLocalValues(thread1Values);
                try {
                    latch[1].await();
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }

                for (String name : thread1Values.keySet()) {
                    Object result = ff.function("env", ff.literal(name)).evaluate(null);
                    int value = ((Number) result).intValue();
                    assertEquals(thread1Values.get(name), value);
                }
            }
        };

        executor.submit(r0);
        executor.submit(r1);
        latch[0].countDown();
        latch[1].countDown();
    }

    /**
     * Tests the use of a single var name with two thread-local values
     */
    @Test
    public void testSetLocalValue() {
        System.out.println("   setLocalValue");

        final String varName = "foo";
        final int thread0Value = 0;
        final int thread1Value = 1;

        final CountDownLatch[] latch = new CountDownLatch[2];
        latch[0] = new CountDownLatch(1);
        latch[1] = new CountDownLatch(1);

        Runnable r0 = new Runnable() {
            public void run() {
                EnvFunction.setLocalValue(varName, thread0Value);
                try {
                    latch[0].await();
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }

                Object result = ff.function("env", ff.literal(varName)).evaluate(null);
                int value = ((Number) result).intValue();
                assertEquals(thread0Value, value);
            }
        };

        Runnable r1 = new Runnable() {
            public void run() {
                EnvFunction.setLocalValue(varName, thread1Value);
                try {
                    latch[1].await();
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }

                Object result = ff.function("env", ff.literal(varName)).evaluate(null);
                int value = ((Number) result).intValue();
                assertEquals(thread1Value, value);
            }
        };

        executor.submit(r0);
        executor.submit(r1);
        latch[0].countDown();
        latch[1].countDown();
    }

    /**
     * Tests setting global values and accessing them from different threads
     */
    @Test
    public void testSetGlobalValues() {
        System.out.println("   setGlobalValues");

        final Map<String, Object> table = new LinkedHashMap<String, Object>();
        table.put("foo", 1);
        table.put("bar", 2);

        final CountDownLatch[] latch = new CountDownLatch[2];
        latch[0] = new CountDownLatch(1);
        latch[1] = new CountDownLatch(1);

        class Runner implements Runnable {
            int index;

            Runner(int index) {
                this.index = index;
            }

            public void run() {
                EnvFunction.setGlobalValues(table);
                try {
                    latch[index].await();
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }

                for (String name : table.keySet()) {
                    Object result = ff.function("env", ff.literal(name)).evaluate(null);
                    int value = ((Number) result).intValue();
                    assertEquals(table.get(name), value);
                }
            }
        }

        executor.submit(new Runner(0));
        executor.submit(new Runner(1));
        latch[0].countDown();
        latch[1].countDown();
    }

    /**
     * Tests setting a global value and accessing it from different threads
     */
    @Test
    public void testSetGlobalValue() {
        System.out.println("   setGlobalValue");

        final String varName = "foo";
        final int varValue = 1;

        final CountDownLatch[] latch = new CountDownLatch[2];
        latch[0] = new CountDownLatch(1);
        latch[1] = new CountDownLatch(1);

        class Runner implements Runnable {
            int index;

            Runner(int index) {
                this.index = index;
            }

            public void run() {
                EnvFunction.setGlobalValue(varName, varValue);
                try {
                    latch[index].await();
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }

                Object result = ff.function("env", ff.literal(varName)).evaluate(null);
                int value = ((Number) result).intValue();
                assertEquals(varValue, value);
            }
        }

        executor.submit(new Runner(0));
        executor.submit(new Runner(1));
        latch[0].countDown();
        latch[1].countDown();
    }

    @Test
    public void testCaseInsensitiveGlobalLookup() {
        System.out.println("   test case-insensitive global lookup");

        final String varName = "foo";
        final String altVarName = "FoO";
        final String varValue = "globalCaseTest";

        EnvFunction.setGlobalValue(varName, varValue);
        Object result = ff.function("env", ff.literal(altVarName)).evaluate(null);
        assertEquals(varValue, result.toString());
    }

    @Test
    public void testCaseInsensitiveLocalLookup() {
        System.out.println("   test case-insensitive local lookup");

        final String varName = "foo";
        final String altVarName = "FoO";
        final String varValue = "localCaseTest";

        EnvFunction.setLocalValue(varName, varValue);
        Object result = ff.function("env", ff.literal(altVarName)).evaluate(null);
        assertEquals(varValue, result.toString());
    }

    @Test
    public void testClearGlobal() {
        System.out.println("   clearGlobalValues");

        final String varName = "foo";
        final String varValue = "clearGlobal";

        EnvFunction.setGlobalValue(varName, varValue);
        EnvFunction.clearGlobalValues();
        Object result = ff.function("env", ff.literal(varName)).evaluate(null);
        assertNull(result);
    }

    @Test
    public void testClearLocal() {
        System.out.println("   clearLocalValues");

        final String varName = "foo";
        final String varValue = "clearLocal";

        EnvFunction.setLocalValue(varName, varValue);
        EnvFunction.clearLocalValues();
        Object result = ff.function("env", ff.literal(varName)).evaluate(null);
        assertNull(result);
    }

    @Test
    public void testGetArgCount() {
        System.out.println("   getArgCount");
        EnvFunction fn = new EnvFunction();
        assertEquals(1, fn.getArgCount());
    }

    @Test
    public void testLiteralDefaultValue() {
        System.out.println("   literal default value");

        int defaultValue = 42;

        Object result = ff.function("env", ff.literal("doesnotexist"), ff.literal(defaultValue)).evaluate(null);
        int value = ((Number) result).intValue();
        assertEquals(defaultValue, value);
    }

    @Test
    public void testNonLiteralDefaultValue() {
        System.out.println("   non-literal default value");

        int x = 21;
        Expression defaultExpr = ff.add(ff.literal(x), ff.literal(x));

        Object result = ff.function("env", ff.literal("doesnotexist"), defaultExpr).evaluate(null);
        int value = ((Number) result).intValue();
        assertEquals(x + x, value);
    }

    /**
     * The setFallback method should log a warning and ignore 
     * the argument.
     */
    @Test
    public void testSetFallbackNotAllowed() {
        Logger logger = Logger.getLogger(EnvFunction.class.getName());

        Formatter formatter = new SimpleFormatter();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Handler handler = new StreamHandler(out, formatter);
        logger.addHandler(handler);

        try {
            EnvFunction function = new EnvFunction();
            function.setFallbackValue(ff.literal(0));

            handler.flush();
            String logMsg = out.toString();
            assertNotNull(logMsg);
            assertTrue(logMsg.toLowerCase().contains("setfallbackvalue"));

        } finally {
            logger.removeHandler(handler);
        }
    }
}