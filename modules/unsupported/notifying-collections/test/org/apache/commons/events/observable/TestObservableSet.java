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
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.collections.set.AbstractTestSet;

/**
 * Extension of {@link TestSet} for exercising the
 * {@link ObservedSet} implementation.
 *
 * @since Commons Events 1.0
 * @version $Revision: 155443 $ $Date: 2005-02-26 06:19:51 -0700 (Sat, 26 Feb 2005) $
 * 
 * @author Stephen Colebourne
 */
public class TestObservableSet extends AbstractTestSet implements ObservedTestHelper.ObservedFactory {
    
    public TestObservableSet(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestObservableSet.class);
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestObservableSet.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    //-----------------------------------------------------------------------
    public Set makeEmptySet() {
        return ObservableSet.decorate(new HashSet(), ObservedTestHelper.LISTENER);
    }

    public Set makeFullSet() {
        Set set = new HashSet();
        set.addAll(Arrays.asList(getFullElements()));
        return ObservableSet.decorate(set, ObservedTestHelper.LISTENER);
    }
    
    //-----------------------------------------------------------------------
    public void testObservedSet() {
        ObservedTestHelper.bulkTestObservedSet(this);
    }

    //-----------------------------------------------------------------------
    public ObservableCollection createObservedCollection() {
        return ObservableSet.decorate(new HashSet());
    }

    public ObservableCollection createObservedCollection(Object listener) {
        return ObservableSet.decorate(new HashSet(), listener);
    }

}
