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

import java.util.Arrays;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.Test;

import org.apache.commons.collections.BulkTest;
import org.apache.commons.collections.set.AbstractTestSortedSet;

/**
 * Extension of {@link TestSortedSet} for exercising the
 * {@link ObservedSortedSet} implementation.
 *
 * @since Commons Events 1.0
 * @version $Revision: 155443 $ $Date: 2005-02-26 06:19:51 -0700 (Sat, 26 Feb 2005) $
 * 
 * @author Stephen Colebourne
 */
public class TestObservableSortedSet extends AbstractTestSortedSet implements ObservedTestHelper.ObservedFactory {
    
    public TestObservableSortedSet(String testName) {
        super(testName);
    }

    public static Test suite() {
        return BulkTest.makeSuite(TestObservableSortedSet.class);
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestObservableSortedSet.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    //-----------------------------------------------------------------------
    public Set makeEmptySet() {
        return ObservableSortedSet.decorate(new TreeSet(), ObservedTestHelper.LISTENER);
    }

    public Set makeFullSet() {
        SortedSet set = new TreeSet();
        set.addAll(Arrays.asList(getFullElements()));
        return ObservableSortedSet.decorate(set, ObservedTestHelper.LISTENER);
    }
    
    //-----------------------------------------------------------------------
    public void testObservedSortedSet() {
        ObservedTestHelper.bulkTestObservedSortedSet(this);
    }

    //-----------------------------------------------------------------------
    public ObservableCollection createObservedCollection() {
        return ObservableSortedSet.decorate(new TreeSet());
    }

    public ObservableCollection createObservedCollection(Object listener) {
        return ObservableSortedSet.decorate(new TreeSet(), listener);
    }

}
