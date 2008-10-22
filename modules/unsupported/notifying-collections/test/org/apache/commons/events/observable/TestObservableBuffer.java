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
import java.util.Collection;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.collections.ArrayStack;
import org.apache.commons.collections.collection.AbstractTestCollection;

/**
 * Extension of {@link TestCollection} for exercising the
 * {@link ObservableBuffer} implementation.
 *
 * @since Commons Events 1.0
 * @version $Revision: 155443 $ $Date: 2005-02-26 06:19:51 -0700 (Sat, 26 Feb 2005) $
 * 
 * @author Stephen Colebourne
 */
public class TestObservableBuffer extends AbstractTestCollection implements ObservedTestHelper.ObservedFactory {
    
    public TestObservableBuffer(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TestObservableBuffer.class);
    }

    public static void main(String args[]) {
        String[] testCaseName = { TestObservableBuffer.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    //-----------------------------------------------------------------------
    public Collection makeConfirmedCollection() {
        return new ArrayStack();
    }

    public Collection makeConfirmedFullCollection() {
        ArrayStack stack = new ArrayStack();
        stack.addAll(Arrays.asList(getFullElements()));
        return stack;
    }
    
    public Collection makeCollection() {
        return ObservableBuffer.decorate(new ArrayStack(), ObservedTestHelper.LISTENER);
    }

    public Collection makeFullCollection() {
        List stack = new ArrayStack();
        stack.addAll(Arrays.asList(getFullElements()));
        return ObservableBuffer.decorate(stack, ObservedTestHelper.LISTENER);
    }
    
    //-----------------------------------------------------------------------
    public void testObservedBuffer() {
        ObservedTestHelper.bulkTestObservedBuffer(this);
    }

    //-----------------------------------------------------------------------
    public ObservableCollection createObservedCollection() {
        return ObservableBuffer.decorate(new ArrayStack());
    }

    public ObservableCollection createObservedCollection(Object listener) {
        return ObservableBuffer.decorate(new ArrayStack(), listener);
    }

}
