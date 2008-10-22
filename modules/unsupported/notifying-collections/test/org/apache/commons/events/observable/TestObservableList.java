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
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.collections.list.AbstractTestList;

/**
 * Extension of {@link TestList} for exercising the
 * {@link ObservedList} implementation.
 *
 * @since Commons Events 1.0
 * @version $Revision: 155443 $ $Date: 2005-02-26 06:19:51 -0700 (Sat, 26 Feb 2005) $
 * 
 * @author Stephen Colebourne
 */
public class TestObservableList extends AbstractTestList implements ObservedTestHelper.ObservedFactory {
    
    public TestObservableList(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestObservableList.class);
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestObservableList.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    //-----------------------------------------------------------------------
    public List makeEmptyList() {
        return ObservableList.decorate(new ArrayList(), ObservedTestHelper.LISTENER);
    }

    public List makeFullList() {
        List set = new ArrayList();
        set.addAll(Arrays.asList(getFullElements()));
        return ObservableList.decorate(set, ObservedTestHelper.LISTENER);
    }
    
    //-----------------------------------------------------------------------
    public void testObservedList() {
        ObservedTestHelper.bulkTestObservedList(this);
    }

    //-----------------------------------------------------------------------
    public ObservableCollection createObservedCollection() {
        return ObservableList.decorate(new ArrayList());
    }

    public ObservableCollection createObservedCollection(Object listener) {
        return ObservableList.decorate(new ArrayList(), listener);
    }

}
