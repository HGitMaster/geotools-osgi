/*
 * Copyright 2003-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.events.observable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.SortedSet;

import junit.framework.Assert;

import org.apache.commons.collections.SortedBag;
import org.apache.commons.events.observable.standard.StandardModificationHandler;
import org.apache.commons.events.observable.standard.StandardModificationListener;
import org.apache.commons.events.observable.standard.StandardPostModificationEvent;
import org.apache.commons.events.observable.standard.StandardPostModificationListener;
import org.apache.commons.events.observable.standard.StandardPreModificationEvent;
import org.apache.commons.events.observable.standard.StandardPreModificationListener;

/**
 * Helper for testing
 * {@link ObservedCollection} implementations.
 *
 * @since Commons Events 1.0
 * @version $Revision: 155443 $ $Date: 2005-02-26 06:19:51 -0700 (Sat, 26 Feb 2005) $
 * 
 * @author Stephen Colebourne
 */
public class ObservedTestHelper extends Assert {
    
    public static Object NONE = new Object();
    
    public static Integer FIVE = new Integer(5);
    public static Integer SIX = new Integer(6);
    public static Integer SEVEN = new Integer(7);
    public static Integer EIGHT = new Integer(8);
    public static Integer NINE = new Integer(9);
    public static List SIX_SEVEN_LIST = new ArrayList();
    static {
        SIX_SEVEN_LIST.add(SIX);
        SIX_SEVEN_LIST.add(SEVEN);
    }
    
    public static class Listener implements StandardModificationListener {
        public StandardPreModificationEvent preEvent = null;
        public StandardPostModificationEvent postEvent = null;
        
        public void modificationOccurring(StandardPreModificationEvent event) {
            this.preEvent = event;
        }

        public void modificationOccurred(StandardPostModificationEvent event) {
            this.postEvent = event;
        }
    }
    
    public static class PreListener implements StandardPreModificationListener {
        public StandardPreModificationEvent preEvent = null;
        
        public void modificationOccurring(StandardPreModificationEvent event) {
            this.preEvent = event;
        }
    }
    
    public static class PostListener implements StandardPostModificationListener {
        public StandardPostModificationEvent postEvent = null;
        
        public void modificationOccurred(StandardPostModificationEvent event) {
            this.postEvent = event;
        }
    }
    
    public static interface ObservedFactory {
        ObservableCollection createObservedCollection();
        ObservableCollection createObservedCollection(Object listener);
    }
    
    public static final Listener LISTENER = new Listener();
    public static final Listener LISTENER2 = new Listener();
    public static final PreListener PRE_LISTENER = new PreListener();
    public static final PostListener POST_LISTENER = new PostListener();
    
    public ObservedTestHelper() {
        super();
    }

    //-----------------------------------------------------------------------
    public static void bulkTestObservedCollection(ObservedFactory factory) {
        doTestFactoryPlain(factory);
        doTestFactoryWithListener(factory);
        doTestFactoryWithPreListener(factory);
        doTestFactoryWithPostListener(factory);
        doTestFactoryWithHandler(factory);
        doTestFactoryWithObject(factory);
        doTestFactoryWithNull(factory);
        
        doTestAddRemoveGetPreListeners(factory);
        doTestAddRemoveGetPostListeners(factory);
        
        doTestAdd(factory);
        doTestAddAll(factory);
        doTestClear(factory);
        doTestRemove(factory);
        doTestRemoveAll(factory);
        doTestRetainAll(factory);
        doTestRemoveIterated(factory);
    }
    
    public static void bulkTestObservedSet(ObservedFactory factory) {
        assertTrue(factory.createObservedCollection() instanceof ObservableSet);
        assertTrue(factory.createObservedCollection(LISTENER) instanceof ObservableSet);
        assertTrue(factory.createObservedCollection(new StandardModificationHandler()) instanceof ObservableSet);
        
        bulkTestObservedCollection(factory);
    }
    
    public static void bulkTestObservedSortedSet(ObservedFactory factory) {
        assertTrue(factory.createObservedCollection() instanceof ObservableSortedSet);
        assertTrue(factory.createObservedCollection(LISTENER) instanceof ObservableSortedSet);
        assertTrue(factory.createObservedCollection(new StandardModificationHandler()) instanceof ObservableSortedSet);
        
        bulkTestObservedCollection(factory);
        doTestSubSet(factory);
        doTestHeadSet(factory);
        doTestTailSet(factory);
    }
    
    public static void bulkTestObservedList(ObservedFactory factory) {
        assertTrue(factory.createObservedCollection() instanceof ObservableList);
        assertTrue(factory.createObservedCollection(LISTENER) instanceof ObservableList);
        assertTrue(factory.createObservedCollection(new StandardModificationHandler()) instanceof ObservableList);
        
        bulkTestObservedCollection(factory);
        doTestAddIndexed(factory);
        doTestAddAllIndexed(factory);
        doTestRemoveIndexed(factory);
        doTestSetIndexed(factory);
        doTestAddIterated(factory);
        doTestSetIterated(factory);
        doTestRemoveListIterated(factory);
        doTestSubList(factory);
    }
    
    public static void bulkTestObservedBag(ObservedFactory factory) {
        assertTrue(factory.createObservedCollection() instanceof ObservableBag);
        assertTrue(factory.createObservedCollection(LISTENER) instanceof ObservableBag);
        assertTrue(factory.createObservedCollection(new StandardModificationHandler()) instanceof ObservableBag);
        
        bulkTestObservedCollection(factory);
        doTestAddNCopies(factory);
        doTestRemoveNCopies(factory);
    }
    
    public static void bulkTestObservedSortedBag(ObservedFactory factory) {
        assertTrue(factory.createObservedCollection() instanceof ObservableSortedBag);
        assertTrue(factory.createObservedCollection(LISTENER) instanceof ObservableSortedBag);
        assertTrue(factory.createObservedCollection(new StandardModificationHandler()) instanceof ObservableSortedBag);
        
        bulkTestObservedCollection(factory);
        doTestAddNCopies(factory);
        doTestRemoveNCopies(factory);
    }
    
    public static void bulkTestObservedBuffer(ObservedFactory factory) {
        assertTrue(factory.createObservedCollection() instanceof ObservableBuffer);
        assertTrue(factory.createObservedCollection(LISTENER) instanceof ObservableBuffer);
        assertTrue(factory.createObservedCollection(new StandardModificationHandler()) instanceof ObservableBuffer);
        
        bulkTestObservedCollection(factory);
        doTestRemoveNext(factory);
    }
    
    //-----------------------------------------------------------------------
    public static void doTestFactoryPlain(ObservedFactory factory) {
        ObservableCollection coll = factory.createObservedCollection();
        
        assertNotNull(coll.getHandler());
        assertEquals(StandardModificationHandler.class, coll.getHandler().getClass());
        assertEquals(0, coll.getHandler().getPreModificationListeners().length);
        assertEquals(0, coll.getHandler().getPostModificationListeners().length);
    }
    
    public static void doTestFactoryWithPreListener(ObservedFactory factory) {
        ObservableCollection coll = factory.createObservedCollection(PRE_LISTENER);
        
        assertNotNull(coll.getHandler());
        assertEquals(StandardModificationHandler.class, coll.getHandler().getClass());
        assertEquals(1, coll.getHandler().getPreModificationListeners().length);
        assertEquals(0, coll.getHandler().getPostModificationListeners().length);
        assertSame(PRE_LISTENER, coll.getHandler().getPreModificationListeners()[0]);
        
        PRE_LISTENER.preEvent = null;
        coll.add(SIX);
        assertTrue(PRE_LISTENER.preEvent != null);
    }
    
    public static void doTestFactoryWithPostListener(ObservedFactory factory) {
        ObservableCollection coll = factory.createObservedCollection(POST_LISTENER);
        
        assertNotNull(coll.getHandler());
        assertEquals(StandardModificationHandler.class, coll.getHandler().getClass());
        assertEquals(0, coll.getHandler().getPreModificationListeners().length);
        assertEquals(1, coll.getHandler().getPostModificationListeners().length);
        assertSame(POST_LISTENER, coll.getHandler().getPostModificationListeners()[0]);
        
        POST_LISTENER.postEvent = null;
        coll.add(SIX);
        assertTrue(POST_LISTENER.postEvent != null);
    }
    
    public static void doTestFactoryWithListener(ObservedFactory factory) {
        ObservableCollection coll = factory.createObservedCollection(LISTENER);
        
        assertNotNull(coll.getHandler());
        assertEquals(StandardModificationHandler.class, coll.getHandler().getClass());
        assertEquals(1, coll.getHandler().getPreModificationListeners().length);
        assertEquals(1, coll.getHandler().getPostModificationListeners().length);
        assertSame(LISTENER, coll.getHandler().getPreModificationListeners()[0]);
        assertSame(LISTENER, coll.getHandler().getPostModificationListeners()[0]);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        coll.add(SIX);
        assertTrue(LISTENER.preEvent != null);
        assertTrue(LISTENER.postEvent != null);
    }
    
    public static void doTestFactoryWithHandler(ObservedFactory factory) {
        StandardModificationHandler handler = new StandardModificationHandler();
        ObservableCollection coll = factory.createObservedCollection(handler);
        
        assertNotNull(coll.getHandler());
        assertSame(handler, coll.getHandler());
        assertEquals(0, coll.getHandler().getPreModificationListeners().length);
        assertEquals(0, coll.getHandler().getPostModificationListeners().length);
    }
    
    public static void doTestFactoryWithObject(ObservedFactory factory) {
        try {
            factory.createObservedCollection(new Object());
            fail();
        } catch (IllegalArgumentException ex) {}
    }
    
    public static void doTestFactoryWithNull(ObservedFactory factory) {
        ObservableCollection coll = factory.createObservedCollection(null);
        
        assertNotNull(coll.getHandler());
        assertEquals(StandardModificationHandler.class, coll.getHandler().getClass());
        assertEquals(0, coll.getHandler().getPreModificationListeners().length);
        assertEquals(0, coll.getHandler().getPostModificationListeners().length);
    }
    
    //-----------------------------------------------------------------------
    public static void doTestAddRemoveGetPreListeners(ObservedFactory factory) {
        ObservableCollection coll = factory.createObservedCollection();
        
        assertEquals(0, coll.getHandler().getPreModificationListeners().length);
        coll.getHandler().addPreModificationListener(LISTENER);
        assertEquals(1, coll.getHandler().getPreModificationListeners().length);
        assertSame(LISTENER, coll.getHandler().getPreModificationListeners()[0]);
        
        coll.getHandler().addPreModificationListener(LISTENER2);
        assertEquals(2, coll.getHandler().getPreModificationListeners().length);
        assertSame(LISTENER, coll.getHandler().getPreModificationListeners()[0]);
        assertSame(LISTENER2, coll.getHandler().getPreModificationListeners()[1]);
        
        coll.getHandler().removePreModificationListener(LISTENER);
        assertEquals(1, coll.getHandler().getPreModificationListeners().length);
        assertSame(LISTENER2, coll.getHandler().getPreModificationListeners()[0]);
        
        coll.getHandler().removePreModificationListener(LISTENER);  // check no error if not present
        assertEquals(1, coll.getHandler().getPreModificationListeners().length);
        assertSame(LISTENER2, coll.getHandler().getPreModificationListeners()[0]);
        
        coll.getHandler().removePreModificationListener(LISTENER2);
        assertEquals(0, coll.getHandler().getPreModificationListeners().length);
        
        try {
            coll.getHandler().addPreModificationListener(new Object());
            fail();
        } catch (ClassCastException ex) {
        }
    }
    
    public static void doTestAddRemoveGetPostListeners(ObservedFactory factory) {
        ObservableCollection coll = factory.createObservedCollection();
        
        assertEquals(0, coll.getHandler().getPostModificationListeners().length);
        coll.getHandler().addPostModificationListener(LISTENER);
        assertEquals(1, coll.getHandler().getPostModificationListeners().length);
        assertSame(LISTENER, coll.getHandler().getPostModificationListeners()[0]);
        
        coll.getHandler().addPostModificationListener(LISTENER2);
        assertEquals(2, coll.getHandler().getPostModificationListeners().length);
        assertSame(LISTENER, coll.getHandler().getPostModificationListeners()[0]);
        assertSame(LISTENER2, coll.getHandler().getPostModificationListeners()[1]);
        
        coll.getHandler().removePostModificationListener(LISTENER);
        assertEquals(1, coll.getHandler().getPostModificationListeners().length);
        assertSame(LISTENER2, coll.getHandler().getPostModificationListeners()[0]);
        
        coll.getHandler().removePostModificationListener(LISTENER);  // check no error if not present
        assertEquals(1, coll.getHandler().getPostModificationListeners().length);
        assertSame(LISTENER2, coll.getHandler().getPostModificationListeners()[0]);
        
        coll.getHandler().removePostModificationListener(LISTENER2);
        assertEquals(0, coll.getHandler().getPostModificationListeners().length);
        
        try {
            coll.getHandler().addPostModificationListener(new Object());
            fail();
        } catch (ClassCastException ex) {
        }
    }
    
    //-----------------------------------------------------------------------
    public static void doTestAdd(ObservedFactory factory) {
        ObservableCollection coll = factory.createObservedCollection(LISTENER);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(0, coll.size());
        coll.add(SIX);
        assertEquals(1, coll.size());
        checkPrePost(coll, ModificationEventType.ADD, -1, 1, SIX, null, false, 0, 1);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(1, coll.size());
        coll.add(SEVEN);
        assertEquals(2, coll.size());
        checkPrePost(coll, ModificationEventType.ADD, -1, 1, SEVEN, null, false, 1, 2);
        
        if (coll instanceof SortedSet == false && coll instanceof SortedBag == false) {
            LISTENER.preEvent = null;
            LISTENER.postEvent = null;
            assertEquals(2, coll.size());
            coll.add(SIX_SEVEN_LIST);
            assertEquals(3, coll.size());
            // pre - don't use checkPrePost as it checks based on Collection
            assertSame(SIX_SEVEN_LIST, LISTENER.preEvent.getChangeObject());
            assertEquals(1, LISTENER.preEvent.getChangeCollection().size());
            assertSame(SIX_SEVEN_LIST, LISTENER.preEvent.getChangeCollection().iterator().next());
            // post - don't use checkPrePost as it checks based on Collection
            assertSame(SIX_SEVEN_LIST, LISTENER.postEvent.getChangeObject());
            assertEquals(1, LISTENER.postEvent.getChangeCollection().size());
            assertSame(SIX_SEVEN_LIST, LISTENER.postEvent.getChangeCollection().iterator().next());
            assertEquals(2, LISTENER.postEvent.getPreSize());
            assertEquals(3, LISTENER.postEvent.getPostSize());
        }
    }

    //-----------------------------------------------------------------------
    public static void doTestAddIndexed(ObservedFactory factory) {
        ObservableList coll = (ObservableList) factory.createObservedCollection(LISTENER);
        
        coll.addAll(SIX_SEVEN_LIST);
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(2, coll.size());
        coll.add(1, EIGHT);
        assertEquals(3, coll.size());
        checkPrePost(coll, ModificationEventType.ADD_INDEXED, 1, 1, EIGHT, null, false, 2, 3);
    }

    //-----------------------------------------------------------------------
    public static void doTestAddNCopies(ObservedFactory factory) {
        ObservableBag coll = (ObservableBag) factory.createObservedCollection(LISTENER);
        
        coll.addAll(SIX_SEVEN_LIST);
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(2, coll.size());
        coll.add(EIGHT, 3);
        assertEquals(5, coll.size());
        checkPrePost(coll, ModificationEventType.ADD_NCOPIES, -1, 3, EIGHT, null, false, 2, 5);
    }

    //-----------------------------------------------------------------------
    public static void doTestAddIterated(ObservedFactory factory) {
        ObservableList coll = (ObservableList) factory.createObservedCollection(LISTENER);
        
        coll.addAll(SIX_SEVEN_LIST);
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        ListIterator it = coll.listIterator();
        assertEquals(2, coll.size());
        it.next();
        it.add(EIGHT);
        assertEquals(3, coll.size());
        checkPrePost(coll, ModificationEventType.ADD_ITERATED, 1, 1, EIGHT, null, false, 2, 3);
    }

    //-----------------------------------------------------------------------
    public static void doTestAddAll(ObservedFactory factory) {
        ObservableCollection coll = factory.createObservedCollection(LISTENER);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(0, coll.size());
        coll.addAll(SIX_SEVEN_LIST);
        assertEquals(2, coll.size());
        checkPrePost(coll, ModificationEventType.ADD_ALL, -1, 1, SIX_SEVEN_LIST, null, false, 0, 2);
    }

    //-----------------------------------------------------------------------
    public static void doTestAddAllIndexed(ObservedFactory factory) {
        ObservableList coll = (ObservableList) factory.createObservedCollection(LISTENER);
        
        coll.addAll(SIX_SEVEN_LIST);
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(2, coll.size());
        coll.addAll(1, SIX_SEVEN_LIST);
        assertEquals(4, coll.size());
        checkPrePost(coll, ModificationEventType.ADD_ALL_INDEXED, 1, 1, SIX_SEVEN_LIST, null, false, 2, 4);
    }

    //-----------------------------------------------------------------------
    public static void doTestClear(ObservedFactory factory) {
        ObservableCollection coll = factory.createObservedCollection(LISTENER);
        
        coll.addAll(SIX_SEVEN_LIST);
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(2, coll.size());
        coll.clear();
        assertEquals(0, coll.size());
        checkPrePost(coll, ModificationEventType.CLEAR, -1, 1, NONE, null, false, 2, 0);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(0, coll.size());
        coll.clear();  // already done this
        assertEquals(0, coll.size());
        assertTrue(LISTENER.preEvent != null);
        assertTrue(LISTENER.postEvent == null);
    }

    //-----------------------------------------------------------------------
    public static void doTestRemove(ObservedFactory factory) {
        ObservableCollection coll = factory.createObservedCollection(LISTENER);
        
        coll.addAll(SIX_SEVEN_LIST);
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(2, coll.size());
        coll.remove(SEVEN);
        assertEquals(1, coll.size());
        checkPrePost(coll, ModificationEventType.REMOVE, -1, 1, SEVEN, SEVEN, false, 2, 1);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(1, coll.size());
        coll.remove(SEVEN);  // already removed
        assertEquals(1, coll.size());
        assertTrue(LISTENER.preEvent != null);
        assertTrue(LISTENER.postEvent == null);
    }

    //-----------------------------------------------------------------------
    public static void doTestRemoveIndexed(ObservedFactory factory) {
        ObservableList coll = (ObservableList) factory.createObservedCollection(LISTENER);
        
        coll.addAll(SIX_SEVEN_LIST);
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(2, coll.size());
        coll.remove(0);
        assertEquals(1, coll.size());
        checkPrePost(coll, ModificationEventType.REMOVE_INDEXED, 0, 1, NONE, SIX, false, 2, 1);
    }

    //-----------------------------------------------------------------------
    public static void doTestRemoveNCopies(ObservedFactory factory) {
        ObservableBag coll = (ObservableBag) factory.createObservedCollection(LISTENER);
        
        coll.add(SIX, 6);
        coll.add(SEVEN, 7);
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(13, coll.size());
        coll.remove(SEVEN, 3);
        assertEquals(10, coll.size());
        checkPrePost(coll, ModificationEventType.REMOVE_NCOPIES, -1, 3, SEVEN, SEVEN, false, 13, 10);
    }

    //-----------------------------------------------------------------------
    public static void doTestRemoveNext(ObservedFactory factory) {
        ObservableBuffer coll = (ObservableBuffer) factory.createObservedCollection(LISTENER);
        
        coll.add(SIX);
        coll.add(SEVEN);
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(2, coll.size());
        coll.remove();
        assertEquals(1, coll.size());
        checkPre (coll, ModificationEventType.REMOVE_NEXT, -1, 1, NONE, null, 2, null, -1);
        checkPost(coll, ModificationEventType.REMOVE_NEXT, -1, 1, SEVEN, SEVEN, 2, 1, null, -1);
    }

    //-----------------------------------------------------------------------
    public static void doTestRemoveIterated(ObservedFactory factory) {
        ObservableCollection coll = factory.createObservedCollection(LISTENER);
        
        coll.addAll(SIX_SEVEN_LIST);
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(2, coll.size());
        Iterator it = coll.iterator();
        it.next();
        Object removed = it.next();  // store remove as iterator order may vary
        it.remove();
        assertEquals(1, coll.size());
        checkPrePost(coll, ModificationEventType.REMOVE_ITERATED, 1, 1, removed, removed, true, 2, 1);

        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(1, coll.size());
        coll.remove(removed);  // already removed
        assertEquals(1, coll.size());
        assertTrue(LISTENER.preEvent != null);
        assertTrue(LISTENER.postEvent == null);
    }

    public static void doTestRemoveListIterated(ObservedFactory factory) {
        ObservableList coll = (ObservableList) factory.createObservedCollection(LISTENER);
        
        coll.addAll(SIX_SEVEN_LIST);
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(2, coll.size());
        ListIterator it = coll.listIterator();
        it.next();
        it.next();
        it.remove();
        assertEquals(1, coll.size());
        checkPrePost(coll, ModificationEventType.REMOVE_ITERATED, 1, 1, SEVEN, SEVEN, true, 2, 1);

        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(1, coll.size());
        coll.remove(SEVEN);  // already removed
        assertEquals(1, coll.size());
        assertTrue(LISTENER.preEvent != null);
        assertTrue(LISTENER.postEvent == null);
    }

    //-----------------------------------------------------------------------
    public static void doTestRemoveAll(ObservedFactory factory) {
        ObservableCollection coll = factory.createObservedCollection(LISTENER);
        
        coll.add(EIGHT);
        coll.addAll(SIX_SEVEN_LIST);
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(3, coll.size());
        coll.removeAll(SIX_SEVEN_LIST);
        assertEquals(1, coll.size());
        checkPrePost(coll, ModificationEventType.REMOVE_ALL, -1, 1, SIX_SEVEN_LIST, null, false, 3, 1);

        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(1, coll.size());
        coll.removeAll(SIX_SEVEN_LIST);  // already done this
        assertEquals(1, coll.size());
        assertTrue(LISTENER.preEvent != null);
        assertTrue(LISTENER.postEvent == null);
    }

    //-----------------------------------------------------------------------
    public static void doTestRetainAll(ObservedFactory factory) {
        ObservableCollection coll = factory.createObservedCollection(LISTENER);
        
        coll.add(EIGHT);
        coll.addAll(SIX_SEVEN_LIST);
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(3, coll.size());
        coll.retainAll(SIX_SEVEN_LIST);
        assertEquals(2, coll.size());
        checkPrePost(coll, ModificationEventType.RETAIN_ALL, -1, 1, SIX_SEVEN_LIST, null, false, 3, 2);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(2, coll.size());
        coll.retainAll(SIX_SEVEN_LIST);  // already done this
        assertEquals(2, coll.size());
        assertTrue(LISTENER.preEvent != null);
        assertTrue(LISTENER.postEvent == null);
    }

    //-----------------------------------------------------------------------
    public static void doTestSetIndexed(ObservedFactory factory) {
        ObservableList coll = (ObservableList) factory.createObservedCollection(LISTENER);
        
        coll.addAll(SIX_SEVEN_LIST);
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(2, coll.size());
        coll.set(0, EIGHT);
        assertEquals(2, coll.size());
        checkPrePost(coll, ModificationEventType.SET_INDEXED, 0, 1, EIGHT, SIX, false, 2, 2);
    }

    //-----------------------------------------------------------------------
    public static void doTestSetIterated(ObservedFactory factory) {
        ObservableList coll = (ObservableList) factory.createObservedCollection(LISTENER);
        
        coll.addAll(SIX_SEVEN_LIST);
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        ListIterator it = coll.listIterator();
        assertEquals(2, coll.size());
        it.next();
        it.next();
        it.set(EIGHT);
        assertEquals(2, coll.size());
        checkPrePost(coll, ModificationEventType.SET_ITERATED, 1, 1, EIGHT, SEVEN, true, 2, 2);
    }

    //-----------------------------------------------------------------------
    public static void doTestSubList(ObservedFactory factory) {
        ObservableList coll = (ObservableList) factory.createObservedCollection(LISTENER);
        
        coll.addAll(SIX_SEVEN_LIST);
        coll.add(EIGHT);
        coll.addAll(SIX_SEVEN_LIST);
        List subList = coll.subList(1, 4);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(3, subList.size());
        subList.add(EIGHT);
        assertEquals(4, subList.size());
        checkPrePost(coll, ModificationEventType.ADD, -1, 1, EIGHT, null, false, 5, 6, subList, 1);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(4, subList.size());
        subList.add(1, EIGHT);
        assertEquals(5, subList.size());
        checkPrePost(coll, ModificationEventType.ADD_INDEXED, 2, 1, EIGHT, null, false, 6, 7, subList, 1);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(5, subList.size());
        subList.set(3, SEVEN);
        assertEquals(5, subList.size());
        checkPrePost(coll, ModificationEventType.SET_INDEXED, 4, 1, SEVEN, SIX, false, 7, 7, subList, 1);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(5, subList.size());
        ListIterator it = subList.listIterator();
        it.next();
        it.remove();
        assertEquals(4, subList.size());
        checkPrePost(coll, ModificationEventType.REMOVE_ITERATED, 1, 1, SEVEN, SEVEN, true, 7, 6, subList, 1);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(4, subList.size());
        it = subList.listIterator();
        it.next();
        it.next();
        it.next();
        it.set(EIGHT);
        assertEquals(4, subList.size());
        checkPrePost(coll, ModificationEventType.SET_ITERATED, 3, 1, EIGHT, SEVEN, true, 6, 6, subList, 1);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(4, subList.size());
        subList.clear();
        assertEquals(0, subList.size());
        checkPrePost(coll, ModificationEventType.CLEAR, -1, 1, NONE, null, false, 6, 2, subList, 1);
    }

    //-----------------------------------------------------------------------
    public static void doTestSubSet(ObservedFactory factory) {
        ObservableSortedSet coll = (ObservableSortedSet) factory.createObservedCollection(LISTENER);
        
        coll.add(SIX);
        coll.add(EIGHT);
        coll.add(NINE);
        SortedSet subSet = coll.subSet(SIX, NINE);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(2, subSet.size());
        subSet.add(SEVEN);
        assertEquals(3, subSet.size());
        checkPrePost(coll, ModificationEventType.ADD, -1, 1, SEVEN, null, false, 3, 4, subSet, -1);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(3, subSet.size());
        subSet.add(SEVEN);
        assertEquals(3, subSet.size());
        // post
        assertSame(null, LISTENER.postEvent);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(3, subSet.size());
        subSet.remove(SEVEN);
        assertEquals(2, subSet.size());
        checkPrePost(coll, ModificationEventType.REMOVE, -1, 1, SEVEN, SEVEN, false, 4, 3, subSet, -1);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(2, subSet.size());
        Iterator it = subSet.iterator();
        it.next();
        it.remove();
        assertEquals(1, subSet.size());
        checkPrePost(coll, ModificationEventType.REMOVE_ITERATED, 0, 1, SIX, SIX, true, 3, 2, subSet, -1);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(1, subSet.size());
        subSet.clear();
        assertEquals(0, subSet.size());
        checkPrePost(coll, ModificationEventType.CLEAR, -1, 1, NONE, null, false, 2, 1, subSet, -1);
    }

    //-----------------------------------------------------------------------
    public static void doTestHeadSet(ObservedFactory factory) {
        ObservableSortedSet coll = (ObservableSortedSet) factory.createObservedCollection(LISTENER);
        
        coll.add(SIX);
        coll.add(EIGHT);
        coll.add(NINE);
        SortedSet subSet = coll.headSet(NINE);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(2, subSet.size());
        subSet.add(SEVEN);
        assertEquals(3, subSet.size());
        checkPrePost(coll, ModificationEventType.ADD, -1, 1, SEVEN, null, false, 3, 4, subSet, -1);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(3, subSet.size());
        subSet.add(SEVEN);
        assertEquals(3, subSet.size());
        // post
        assertSame(null, LISTENER.postEvent);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(3, subSet.size());
        subSet.remove(SEVEN);
        assertEquals(2, subSet.size());
        checkPrePost(coll, ModificationEventType.REMOVE, -1, 1, SEVEN, SEVEN, false, 4, 3, subSet, -1);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(2, subSet.size());
        Iterator it = subSet.iterator();
        it.next();
        it.remove();
        assertEquals(1, subSet.size());
        checkPrePost(coll, ModificationEventType.REMOVE_ITERATED, 0, 1, SIX, SIX, true, 3, 2, subSet, -1);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(1, subSet.size());
        subSet.clear();
        assertEquals(0, subSet.size());
        checkPrePost(coll, ModificationEventType.CLEAR, -1, 1, NONE, null, false, 2, 1, subSet, -1);
    }

    //-----------------------------------------------------------------------
    public static void doTestTailSet(ObservedFactory factory) {
        ObservableSortedSet coll = (ObservableSortedSet) factory.createObservedCollection(LISTENER);
        
        coll.add(FIVE);
        coll.add(SIX);
        coll.add(EIGHT);
        SortedSet subSet = coll.tailSet(SIX);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(2, subSet.size());
        subSet.add(SEVEN);
        assertEquals(3, subSet.size());
        checkPrePost(coll, ModificationEventType.ADD, -1, 1, SEVEN, null, false, 3, 4, subSet, -1);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(3, subSet.size());
        subSet.add(SEVEN);
        assertEquals(3, subSet.size());
        // post
        assertSame(null, LISTENER.postEvent);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(3, subSet.size());
        subSet.remove(SEVEN);
        assertEquals(2, subSet.size());
        checkPrePost(coll, ModificationEventType.REMOVE, -1, 1, SEVEN, SEVEN, false, 4, 3, subSet, -1);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(2, subSet.size());
        Iterator it = subSet.iterator();
        it.next();
        it.remove();
        assertEquals(1, subSet.size());
        checkPrePost(coll, ModificationEventType.REMOVE_ITERATED, 0, 1, SIX, SIX, true, 3, 2, subSet, -1);
        
        LISTENER.preEvent = null;
        LISTENER.postEvent = null;
        assertEquals(1, subSet.size());
        subSet.clear();
        assertEquals(0, subSet.size());
        checkPrePost(coll, ModificationEventType.CLEAR, -1, 1, NONE, null, false, 2, 1, subSet, -1);
    }
    
    //-----------------------------------------------------------------------
    protected static void checkPrePost(
            ObservableCollection coll, int type, int changeIndex, int changeRepeat,
            Object changeObject, Object previous, boolean previousPre,
            int preSize, int postSize) {
        checkPrePost(
            coll, type, changeIndex, changeRepeat,
            changeObject, previous, previousPre,
            preSize, postSize, null, -1);
    }
    
    protected static void checkPrePost(
            ObservableCollection coll, int type, int changeIndex, int changeRepeat,
            Object changeObject, Object previous, boolean previousPre,
            int preSize, int postSize, Collection view, int viewOffset) {
                
        checkPre(
            coll, type, changeIndex, changeRepeat, changeObject,
            (previousPre ? previous : null), preSize, view, viewOffset);
        checkPost(
            coll, type, changeIndex, changeRepeat, changeObject,
            previous, preSize, postSize, view, viewOffset);
    }        
    protected static void checkPre(
            ObservableCollection coll, int type, int changeIndex, int changeRepeat,
            Object changeObject, Object previous,
            int preSize, Collection view, int viewOffset) {

        assertSame(coll, LISTENER.preEvent.getObservedCollection());
        assertSame(coll.getHandler(), LISTENER.preEvent.getHandler());
        assertEquals(false, LISTENER.preEvent.getBaseCollection() instanceof ObservableCollection);
        assertEquals(type, LISTENER.preEvent.getType());
        assertEquals(changeIndex, LISTENER.preEvent.getChangeIndex());
        assertEquals(changeRepeat, LISTENER.preEvent.getChangeRepeat());
        if (changeObject == NONE) {
            assertSame(null, LISTENER.preEvent.getChangeObject());
            assertEquals(0, LISTENER.preEvent.getChangeCollection().size());
        } else if (changeObject instanceof Collection) {
            assertSame(changeObject, LISTENER.preEvent.getChangeObject());
            assertSame(changeObject, LISTENER.preEvent.getChangeCollection());
        } else {
            assertSame(changeObject, LISTENER.preEvent.getChangeObject());
            assertEquals(1, LISTENER.preEvent.getChangeCollection().size());
            assertSame(changeObject, LISTENER.preEvent.getChangeCollection().iterator().next());
        }
        assertSame(previous, LISTENER.preEvent.getPrevious());
        assertEquals(preSize, LISTENER.preEvent.getPreSize());
        assertEquals((view != null), LISTENER.preEvent.isView());
        assertEquals(viewOffset, LISTENER.preEvent.getViewOffset());
        assertSame(view, LISTENER.preEvent.getView());
    }        
    protected static void checkPost(
            ObservableCollection coll, int type, int changeIndex, int changeRepeat,
            Object changeObject, Object previous,
            int preSize, int postSize, Collection view, int viewOffset) {

        // post
        assertSame(coll, LISTENER.postEvent.getObservedCollection());
        assertSame(coll.getHandler(), LISTENER.postEvent.getHandler());
        assertEquals(false, LISTENER.postEvent.getBaseCollection() instanceof ObservableCollection);
        assertEquals(type, LISTENER.postEvent.getType());
        assertEquals(changeIndex, LISTENER.postEvent.getChangeIndex());
        assertEquals(changeRepeat, LISTENER.postEvent.getChangeRepeat());
        if (changeObject == NONE) {
            assertSame(null, LISTENER.postEvent.getChangeObject());
            assertEquals(0, LISTENER.postEvent.getChangeCollection().size());
        } else if (changeObject instanceof Collection) {
            assertSame(changeObject, LISTENER.postEvent.getChangeObject());
            assertSame(changeObject, LISTENER.postEvent.getChangeCollection());
        } else {
            assertSame(changeObject, LISTENER.postEvent.getChangeObject());
            assertEquals(1, LISTENER.postEvent.getChangeCollection().size());
            assertSame(changeObject, LISTENER.postEvent.getChangeCollection().iterator().next());
        }
        assertSame(previous, LISTENER.postEvent.getPrevious());
        assertEquals(preSize, LISTENER.postEvent.getPreSize());
        assertEquals(postSize, LISTENER.postEvent.getPostSize());
        assertEquals(postSize != preSize, LISTENER.postEvent.isSizeChanged());
        assertEquals((view != null), LISTENER.postEvent.isView());
        assertEquals(viewOffset, LISTENER.postEvent.getViewOffset());
        assertSame(view, LISTENER.postEvent.getView());
        
        switch (type) {
            case ModificationEventType.ADD:
            case ModificationEventType.ADD_ALL:
            case ModificationEventType.ADD_ALL_INDEXED:
            case ModificationEventType.ADD_INDEXED:
            case ModificationEventType.ADD_ITERATED:
            case ModificationEventType.ADD_NCOPIES:
            assertEquals(true, LISTENER.preEvent.isTypeAdd());
            assertEquals(true, LISTENER.postEvent.isTypeAdd());
            break;
            default:
            assertEquals(false, LISTENER.preEvent.isTypeAdd());
            assertEquals(false, LISTENER.postEvent.isTypeAdd());
            break;
        }
        switch (type) {
            case ModificationEventType.REMOVE:
            case ModificationEventType.REMOVE_ALL:
            case ModificationEventType.REMOVE_INDEXED:
            case ModificationEventType.REMOVE_ITERATED:
            case ModificationEventType.REMOVE_NCOPIES:
            case ModificationEventType.REMOVE_NEXT:
            case ModificationEventType.RETAIN_ALL:
            case ModificationEventType.CLEAR:
            assertEquals(true, LISTENER.preEvent.isTypeReduce());
            assertEquals(true, LISTENER.postEvent.isTypeReduce());
            break;
            default:
            assertEquals(false, LISTENER.preEvent.isTypeReduce());
            assertEquals(false, LISTENER.postEvent.isTypeReduce());
            break;
        }
        switch (type) {
            case ModificationEventType.SET_INDEXED:
            case ModificationEventType.SET_ITERATED:
            assertEquals(true, LISTENER.preEvent.isTypeChange());
            assertEquals(true, LISTENER.postEvent.isTypeChange());
            break;
            default:
            assertEquals(false, LISTENER.preEvent.isTypeChange());
            assertEquals(false, LISTENER.postEvent.isTypeChange());
            break;
        }
        switch (type) {
            case ModificationEventType.ADD_ALL:
            case ModificationEventType.ADD_ALL_INDEXED:
            case ModificationEventType.REMOVE_ALL:
            case ModificationEventType.RETAIN_ALL:
            case ModificationEventType.CLEAR:
            assertEquals(true, LISTENER.preEvent.isTypeBulk());
            assertEquals(true, LISTENER.postEvent.isTypeBulk());
            break;
            default:
            assertEquals(false, LISTENER.preEvent.isTypeBulk());
            assertEquals(false, LISTENER.postEvent.isTypeBulk());
            break;
        }
    }

}
