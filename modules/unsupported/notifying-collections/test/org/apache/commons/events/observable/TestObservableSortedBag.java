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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.collections.Bag;
import org.apache.commons.collections.bag.AbstractTestSortedBag;
import org.apache.commons.collections.bag.TreeBag;

/**
 * Extension of {@link TestSortedBag} for exercising the
 * {@link ObservedSortedBag} implementation.
 *
 * @since Commons Events 1.0
 * @version $Revision: 155443 $ $Date: 2005-02-26 06:19:51 -0700 (Sat, 26 Feb 2005) $
 * 
 * @author Stephen Colebourne
 */
public class TestObservableSortedBag extends AbstractTestSortedBag implements ObservedTestHelper.ObservedFactory {
    
    public TestObservableSortedBag(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestObservableSortedBag.class);
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestObservableSortedBag.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    //-----------------------------------------------------------------------
    public Bag makeBag() {
        return ObservableSortedBag.decorate(new TreeBag(), ObservedTestHelper.LISTENER);
    }

    //-----------------------------------------------------------------------
    public void testObservedSortedBag() {
        ObservedTestHelper.bulkTestObservedSortedBag(this);
    }

    //-----------------------------------------------------------------------
    public ObservableCollection createObservedCollection() {
        return ObservableSortedBag.decorate(new TreeBag());
    }

    public ObservableCollection createObservedCollection(Object listener) {
        return ObservableSortedBag.decorate(new TreeBag(), listener);
    }

}
