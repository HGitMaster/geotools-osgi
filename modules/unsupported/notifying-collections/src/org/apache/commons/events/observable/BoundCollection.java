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

import java.beans.PropertyChangeSupport ; 
import java.beans.PropertyChangeListener ; 
import java.beans.PropertyChangeEvent ; 
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections.collection.AbstractCollectionDecorator;
import org.apache.commons.collections.iterators.AbstractIteratorDecorator;

/**
 * <p>
 * Decorates a <code>Collection</code> implementation with a <b>bound 
 * property</b> named &quot;collection&quot;.
 * </p>
 * <p>
 * Each modifying method call made on this <code>Collection</code> is 
 * handled as a change to the &quot;collection&quot; property.  This 
 * facility serves to notify subscribers of a change to the collection
 * but does not allow users the option of vetoing the change.  To 
 * gain the ability to veto the change, use a {@link ConstrainedCollection}
 * decorater.
 * </p>
 * <p>
 * Registered listeners must implement the {@link 
 * java.beans.PropertyChangeListener} interface.  Each change request causes a 
 * {@link CollectionChangeEvent} to be fired <i>after</i> the request 
 * has been executed.  The {@link CollectionChangeEvent} provides an 
 * indication of the type of change, the element participating in the 
 * change, and whether or not the collection was actually affected by the
 * change request.  As such, receiving a <code>CollectionChangeEvent</code>
 * is merely an indication that a change was attempted, not that the 
 * Collection is actually different.
 *
 * @see java.beans.PropertyChangeListener
 * @since Commons Events 1.0
 * @version $Revision: 155443 $ $Date: 2005-02-26 06:19:51 -0700 (Sat, 26 Feb 2005) $
 * 
 * @author Stephen Colebourne, Bryce Nordgren
 */
public class BoundCollection extends AbstractCollectionDecorator {
    
    /** 
     * Child-accessible factory used to construct {@link CollectionChangeEvent}s. 
     * This field is <code>final</code> and is set by the constructor, so 
     * while the children may <i>use</i> it to instantiate events, they 
     * may not <i>change</i> it.  
     */
    protected final CollectionChangeEventFactory eventFactory ; 
    /** Utility to support listener registry and event dispatch. */
    private final PropertyChangeSupport changeSupport ; 

    // Constructors
    //-----------------------------------------------------------------------
    /**
     * Constructor that wraps (not copies) and takes a
     * {@link CollectionChangeEventFactory}.
     * <p>
     * This should be used if the client wants to provide a user-specific
     * CollectionChangeEventFactory implementation.  Note that the 
     * same instance of the factory may not be used with multiple 
     * collection decorators.
     * 
     * @param coll  the collection to decorate, must not be null
     * @param eventFactory the factory which instantiates 
     *    {@link CollectionChangeEvent}s.  
     * @throws IllegalArgumentException if the collection or event factory
     *     is null.
     * @throws UnsupportedOperationException if the <code>eventFactory</code>
     *     has already been used with another collection decorator.
     */
    protected BoundCollection(Collection coll, 
        CollectionChangeEventFactory eventFactory) { 

        // initialize parent
        super(coll) ; 

        // Make a default event factory if necessary
        if (eventFactory == null) { 
            eventFactory = new DefaultCollectionChangeEventFactory() ; 
        }
            
        // install the event factory
        eventFactory.setCollection(this) ; 
        this.eventFactory = eventFactory ; 

        // initialize property change support.
        changeSupport = new PropertyChangeSupport(this) ; 
    }

    /**
     * Constructor that wraps (not copies) and uses the 
     * {@link DefaultCollectionChangeEventFactory}.
     * <p>
     * This should be used if the default change events are considered 
     * adequate to the task of monitoring changes to the collection.
     * 
     * @param coll  the collection to decorate, must not be null
     * @throws IllegalArgumentException if the collection is null
     */
    protected BoundCollection(Collection coll) { 
        this(coll, null) ; 
    }
        
    
    // BoundCollection factories
    //-----------------------------------------------------------------------
    /**
     * Factory method to create a bound collection using the default 
     * events.
     * <p>
     * A {@link DefaultCollectionChangeEventFactory} will be created.
     *
     * @param coll  the collection to decorate, must not be null
     * @return the observed collection
     * @throws IllegalArgumentException if the collection is null
     */
    public static BoundCollection decorate(final Collection coll) {
        return new BoundCollection(coll);
    }

    /**
     * Factory method to create an observable collection using user-supplied
     * events.
     * <p>
     * To implement user-supplied events, extend {@link CollectionChangeEvent},
     * implement the {@link CollectionChangeEventFactory} interface.  Your
     * event factory will be called when the collection is changed.  Likewise,
     * your event will be fired to all registered listeners.
     *
     * @param coll  the collection to decorate, must not be null
     * @param eventFactory, the factory to create user-defined events.
     * @return the observed collection
     * @throws IllegalArgumentException if the collection is null
     */
    public static BoundCollection decorate(
            final Collection coll,
            final CollectionChangeEventFactory eventFactory) {
        
        return new BoundCollection(coll, eventFactory);
    }

    // Listener Management
    //-----------------------------------------------------------------------
    /**
     * Registers a listener with this decorator.  The Listener must 
     * implement the <code>PropertyChangeListener</code> interface.
     * Adding a listener more than once will result in more than 
     * one notification for each change event.
     *
     * @param l The listener to register with this decorator.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) { 
        changeSupport.addPropertyChangeListener(l) ; 
    }

    /**
     * Unregisters a listener from this decorator.  The Listener must 
     * implement the <code>PropertyChangeListener</code> interface.
     * If the listener was registered more than once, calling this method 
     * cancels out a single registration.  If the listener is not 
     * registered with this object, no action is taken.
     *
     * @param l The listener to register with this decorator.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) { 
        changeSupport.removePropertyChangeListener(l) ; 
    } 

    /**
     * This is a utility method to allow subclasses to fire property change 
     * events.  
     * @param evt The pre-constructed event.
     */
    protected void firePropertyChange(PropertyChangeEvent evt) { 
        changeSupport.firePropertyChange(evt) ; 
    }

    /**
     * Package private method to create an EventRepeater from within the 
     * context of a particular BoundCollection object.  This event repeater 
     * will relay events to all property change listeners subscribed to this
     * bound collection.
     * @return the event repeater object.
     */
    EventRepeater createEventRepeater() { 
        return new EventRepeater(this) ; 
    }


    // Decoration of Collection methods.
    //-----------------------------------------------------------------------
    public boolean add(Object element) { 
        boolean changed = collection.add(element) ; 
        CollectionChangeEvent evt = eventFactory.createAdd(element, changed);
        firePropertyChange(evt) ; 
        return changed ; 
    }
    
    public boolean addAll(Collection element) { 
        boolean changed = collection.addAll(element) ; 
        CollectionChangeEvent evt = eventFactory.createAddAll(element, changed);
        firePropertyChange(evt) ; 
        return changed ; 
    }
    
    public void clear() { 
        boolean changed = !(collection.isEmpty()) ; 
        collection.clear() ; 
        CollectionChangeEvent evt = eventFactory.createClear(changed);
        firePropertyChange(evt) ; 
    }

    public Iterator iterator() { 
        return new BoundIterator(collection.iterator()) ; 
    }

    public boolean remove(Object element) { 
        boolean changed = collection.remove(element) ; 
        CollectionChangeEvent evt = eventFactory.createRemove(element, changed);
        firePropertyChange(evt) ; 
        return changed ; 
    }
    
    public boolean removeAll(Collection element) { 
        boolean changed = collection.removeAll(element) ; 
        CollectionChangeEvent evt = eventFactory.createRemoveAll(
                element, changed);
        firePropertyChange(evt) ; 
        return changed ; 
    }

    public boolean retainAll(Collection element) { 
        boolean changed = collection.retainAll(element) ; 
        CollectionChangeEvent evt = eventFactory.createRetainAll(
                element, changed);
        firePropertyChange(evt) ; 
        return changed ; 
    }

    // Utility classes
    //-----------------------------------------------------------------------
    /**
     * This class subscribes to events from another collection and fires them 
     * to all the subscribers of this collection. 
     */
    private class EventRepeater implements PropertyChangeListener { 
        private Object myself ; 
        public EventRepeater(Object me) { myself = me ; }
        /** 
         * Relay events which did not originate with my list.  This 
         * prevents an infinite loop.
         */
        public void propertyChange(PropertyChangeEvent evt) { 
            if ((evt != null) && (evt.getSource() != myself)) { 
                firePropertyChange(evt) ; 
            }
        }
    }


    protected class BoundIterator extends AbstractIteratorDecorator {
        
        protected int lastIndex = -1;
        protected Object last;
        
        protected BoundIterator(Iterator iterator) {
            super(iterator);
        }
        
        public Object next() {
            last = super.next();
            lastIndex++;
            return last;
        }

        public void remove() {
            iterator.remove() ; 
            CollectionChangeEvent evt = eventFactory.createRemoveIterated(
                lastIndex, last, true) ; 
            firePropertyChange(evt) ; 
            lastIndex--; 
        }
    }
}
