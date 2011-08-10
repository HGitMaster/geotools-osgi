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

<<<<<<< local
=======
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

>>>>>>> other
import java.io.ByteArrayOutputStream;
<<<<<<< local
import java.util.LinkedHashMap;
=======
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
>>>>>>> other
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
<<<<<<< local
=======
import java.util.concurrent.Future;
>>>>>>> other
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
<<<<<<< local
=======

>>>>>>> other
import org.geotools.factory.CommonFactoryFinder;
import org.junit.After;
import org.junit.Test;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
<<<<<<< local
import static org.junit.Assert.*;
=======
>>>>>>> other

/**
 * @author Andrea Aime
 * @author Michael Bedward
 * @since 2.6
<<<<<<< local
 * @source $URL: $
=======
 *
 * @source $URL: http://svn.osgeo.org/geotools/trunk/modules/library/main/src/test/java/org/geotools/filter/function/EnvFunctionTest.java $
>>>>>>> other
 * @version $Id $
 */
<<<<<<< local
=======

>>>>>>> other
public class EnvFunctionTest {

    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

    @After
    public void tearDown() {
        EnvFunction.clearGlobalValues();
        EnvFunction.clearLocalValues();
    }

<<<<<<< local
=======
    public EnvFunctionTest() {}

>>>>>>> other
    /**
     * Tests the use of two thread-local tables with same var names and different values
     */
    @Test
    public void testSetLocalValues() throws Exception {
        System.out.println("   setLocalValues");

<<<<<<< local
        final Map<String, Object> thread0Values = new LinkedHashMap<String, Object>();
        thread0Values.put("foo", 1);
        thread0Values.put("bar", 2);
=======
        final String key1 = "foo";
        final String key2 = "bar";
>>>>>>> other

<<<<<<< local
        final Map<String, Object> thread1Values = new LinkedHashMap<String, Object>();
        thread1Values.put("foo", 10);
        thread1Values.put("bar", 20);
=======
        final Map<String, Object> table0 = new HashMap<String, Object>();
        table0.put(key1, 1);
        table0.put(key2, 2);
>>>>>>> other

<<<<<<< local
        final CountDownLatch[] latch = new CountDownLatch[2];
        latch[0] = new CountDownLatch(1);
        latch[1] = new CountDownLatch(1);
=======
        final Map<String, Object> table1 = new HashMap<String, Object>();
        table1.put(key1, 10);
        table1.put(key2, 20);
>>>>>>> other

<<<<<<< local
        Runnable r0 = new Runnable() {
=======
        final List<Map<String, Object>> tables = new ArrayList<Map<String, Object>>();
        tables.add(table0);
        tables.add(table1);

        final CountDownLatch latch = new CountDownLatch(2);

        class Task implements Runnable {
            private final int threadIndex;

            public Task(int threadIndex) {
                this.threadIndex = threadIndex;
            }

>>>>>>> other
            public void run() {
<<<<<<< local
                EnvFunction.setLocalValues(thread0Values);
=======
                // set the local values for this thread and then wait for
                // the other thread to do the same before testing
                EnvFunction.setLocalValues(tables.get(threadIndex));
                latch.countDown();
>>>>>>> other
                try {
<<<<<<< local
                    latch[0].await();
=======
                    latch.await();
>>>>>>> other
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }

<<<<<<< local
                for (String name : thread0Values.keySet()) {
=======
                Map<String, Object> table = tables.get(threadIndex);
                for (String name : table.keySet()) {
>>>>>>> other
                    Object result = ff.function("env", ff.literal(name)).evaluate(null);
                    int value = ((Number) result).intValue();
<<<<<<< local
                    assertEquals(thread0Values.get(name), value);
=======
                    assertEquals(table.get(name), value);
>>>>>>> other
                }
            }
<<<<<<< local
        };
=======
        }
>>>>>>> other

<<<<<<< local
        Runnable r1 = new Runnable() {
            public void run() {
                EnvFunction.setLocalValues(thread1Values);
                try {
                    latch[1].await();
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }
=======
        Future f1 = executor.submit(new Task(0));
        Future f2 = executor.submit(new Task(1));
>>>>>>> other

<<<<<<< local
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
=======
        // calling get on the Futures ensures that this test method
        // completes before another starts
        f1.get();
        f2.get();
>>>>>>> other
    }

    /**
     * Tests the use of a single var name with two thread-local values
     */
    @Test
<<<<<<< local
    public void testSetLocalValue() {
=======
    public void testSetLocalValue() throws Exception {
>>>>>>> other
        System.out.println("   setLocalValue");

        final String varName = "foo";
<<<<<<< local
        final int thread0Value = 0;
        final int thread1Value = 1;
=======
        final int[] values = {1, 2};
>>>>>>> other

<<<<<<< local
        final CountDownLatch[] latch = new CountDownLatch[2];
        latch[0] = new CountDownLatch(1);
        latch[1] = new CountDownLatch(1);
=======
        final CountDownLatch latch = new CountDownLatch(2);
>>>>>>> other

<<<<<<< local
        Runnable r0 = new Runnable() {
=======
        class Task implements Runnable {
            private final int threadIndex;

            public Task(int threadIndex) {
                this.threadIndex = threadIndex;
            }

>>>>>>> other
            public void run() {
<<<<<<< local
                EnvFunction.setLocalValue(varName, thread0Value);
=======
                // set the local var then wait for the other thread
                // to do the same before testing
                EnvFunction.setLocalValue(varName, values[threadIndex]);
                latch.countDown();
>>>>>>> other
                try {
<<<<<<< local
                    latch[0].await();
=======
                    latch.await();
>>>>>>> other
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }

                Object result = ff.function("env", ff.literal(varName)).evaluate(null);
                int value = ((Number) result).intValue();
<<<<<<< local
                assertEquals(thread0Value, value);
=======
                assertEquals(values[threadIndex], value);
>>>>>>> other
            }
<<<<<<< local
        };
=======
        }
>>>>>>> other

<<<<<<< local
        Runnable r1 = new Runnable() {
            public void run() {
                EnvFunction.setLocalValue(varName, thread1Value);
                try {
                    latch[1].await();
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }
=======
        Future f1 = executor.submit(new Task(0));
        Future f2 = executor.submit(new Task(1));
>>>>>>> other

<<<<<<< local
                Object result = ff.function("env", ff.literal(varName)).evaluate(null);
                int value = ((Number) result).intValue();
                assertEquals(thread1Value, value);
            }
        };

        executor.submit(r0);
        executor.submit(r1);
        latch[0].countDown();
        latch[1].countDown();
=======
        // calling get on the Futures ensures that this test method
        // completes before another starts
        f1.get();
        f2.get();
>>>>>>> other
    }

    /**
     * Tests setting global values and accessing them from different threads
     */
    @Test
<<<<<<< local
    public void testSetGlobalValues() {
=======
    public void testSetGlobalValues() throws Exception {
>>>>>>> other
        System.out.println("   setGlobalValues");

<<<<<<< local
        final Map<String, Object> table = new LinkedHashMap<String, Object>();
=======
        final Map<String, Object> table = new HashMap<String, Object>();
>>>>>>> other
        table.put("foo", 1);
        table.put("bar", 2);
<<<<<<< local
=======
        EnvFunction.setGlobalValues(table);
>>>>>>> other

<<<<<<< local
        final CountDownLatch[] latch = new CountDownLatch[2];
        latch[0] = new CountDownLatch(1);
        latch[1] = new CountDownLatch(1);
=======
        final CountDownLatch latch = new CountDownLatch(2);
>>>>>>> other

<<<<<<< local
        class Runner implements Runnable {
            int index;
=======
        class Task implements Runnable {
            final String key;
>>>>>>> other

<<<<<<< local
            Runner(int index) {
                this.index = index;
=======
            Task(String key) {
                if (!table.containsKey(key)) {
                    throw new IllegalArgumentException("Invalid arg " + key);
                }
                this.key = key;
>>>>>>> other
            }

            public void run() {
<<<<<<< local
                EnvFunction.setGlobalValues(table);
=======
                // set the global value assigned to this thread then wait for the other
                // thread to do the same
                EnvFunction.setGlobalValue(key, table.get(key));
                latch.countDown();
>>>>>>> other
                try {
<<<<<<< local
                    latch[index].await();
=======
                    latch.await();
>>>>>>> other
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

<<<<<<< local
        executor.submit(new Runner(0));
        executor.submit(new Runner(1));
        latch[0].countDown();
        latch[1].countDown();
=======
        Future f1 = executor.submit(new Task("foo"));
        Future f2 = executor.submit(new Task("bar"));

        // calling get on the Futures ensures that this test method
        // completes before another starts
        f1.get();
        f2.get();
>>>>>>> other
    }

    /**
     * Tests setting a global value and accessing it from different threads
     */
    @Test
<<<<<<< local
    public void testSetGlobalValue() {
=======
    public void testSetGlobalValue() throws Exception {
>>>>>>> other
        System.out.println("   setGlobalValue");

        final String varName = "foo";
<<<<<<< local
        final int varValue = 1;
=======
        final String varValue = "a global value";
        EnvFunction.setGlobalValue(varName, varValue);
>>>>>>> other

<<<<<<< local
        final CountDownLatch[] latch = new CountDownLatch[2];
        latch[0] = new CountDownLatch(1);
        latch[1] = new CountDownLatch(1);

        class Runner implements Runnable {
            int index;

            Runner(int index) {
                this.index = index;
            }
=======
        class Task implements Runnable {
>>>>>>> other

            public void run() {
<<<<<<< local
                EnvFunction.setGlobalValue(varName, varValue);
                try {
                    latch[index].await();
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }

=======
>>>>>>> other
                Object result = ff.function("env", ff.literal(varName)).evaluate(null);
<<<<<<< local
                int value = ((Number) result).intValue();
                assertEquals(varValue, value);
=======
                assertEquals(varValue, result.toString());
>>>>>>> other
            }
        }

<<<<<<< local
        executor.submit(new Runner(0));
        executor.submit(new Runner(1));
        latch[0].countDown();
        latch[1].countDown();
=======
        Future f1 = executor.submit(new Task());
        Future f2 = executor.submit(new Task());

        // calling get on the Futures ensures that this test method
        // completes before another starts
        f1.get();
        f2.get();
>>>>>>> other
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
<<<<<<< local
}=======
}
>>>>>>> other
