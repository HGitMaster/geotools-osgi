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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.collections.collection.AbstractTestCollection;

/**
 * Extension of {@link TestCollection} for exercising the
 * {@link ObservedCollection} implementation.
 *
 * @since Commons Events 1.0
 * @version $Revision: 155443 $ $Date: 2005-02-26 06:19:51 -0700 (Sat, 26 Feb 2005) $
 * 
 * @author Stephen Colebourne
 */
public class TestObservableCollection extends AbstractTestCollection implements ObservedTestHelper.ObservedFactory {
    
    public TestObservableCollection(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestObservableCollection.class);
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestObservableCollection.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    //-----------------------------------------------------------------------
    public Collection makeConfirmedCollection() {
        return new ArrayList();
    }

    public Collection makeConfirmedFullCollection() {
        List list = new ArrayList();
        list.addAll(Arrays.asList(getFullElements()));
        return list;
    }
    
    public Collection makeCollection() {
        return ObservableCollection.decorate(new ArrayList(), ObservedTestHelper.LISTENER);
    }

    public Collection makeFullCollection() {
        List list = new ArrayList();
        list.addAll(Arrays.asList(getFullElements()));
        return ObservableCollection.decorate(list, ObservedTestHelper.LISTENER);
    }
    
    //-----------------------------------------------------------------------
    public void testObservedCollection() {
        ObservedTestHelper.bulkTestObservedCollection(this);
    }

    //-----------------------------------------------------------------------
    public ObservableCollection createObservedCollection() {
        return ObservableCollection.decorate(new ArrayList());
    }

    public ObservableCollection createObservedCollection(Object listener) {
        return ObservableCollection.decorate(new ArrayList(), listener);
    }

//  public void testFactoryWithMasks() {
//      ObservedCollection coll = ObservedCollection.decorate(new ArrayList(), LISTENER, -1, 0);
//      LISTENER.preEvent = null;
//      LISTENER.postEvent = null;
//      coll.add(SIX);
//      assertTrue(LISTENER.preEvent != null);
//      assertTrue(LISTENER.postEvent == null);
//        
//      coll = ObservedCollection.decorate(new ArrayList(), LISTENER, 0, -1);
//      LISTENER.preEvent = null;
//      LISTENER.postEvent = null;
//      coll.add(SIX);
//      assertTrue(LISTENER.preEvent == null);
//      assertTrue(LISTENER.postEvent != null);
//        
//      coll = ObservedCollection.decorate(new ArrayList(), LISTENER, -1, -1);
//      LISTENER.preEvent = null;
//      LISTENER.postEvent = null;
//      coll.add(SIX);
//      assertTrue(LISTENER.preEvent != null);
//      assertTrue(LISTENER.postEvent != null);
//        
//      coll = ObservedCollection.decorate(new ArrayList(), LISTENER, 0, 0);
//      LISTENER.preEvent = null;
//      LISTENER.postEvent = null;
//      coll.add(SIX);
//      assertTrue(LISTENER.preEvent == null);
//      assertTrue(LISTENER.postEvent == null);
//        
//      coll = ObservedCollection.decorate(new ArrayList(), LISTENER, ModificationEventType.ADD, ModificationEventType.ADD_ALL);
//      LISTENER.preEvent = null;
//      LISTENER.postEvent = null;
//      coll.add(SIX);
//      assertTrue(LISTENER.preEvent != null);
//      assertTrue(LISTENER.postEvent == null);
//  }
//    
}
