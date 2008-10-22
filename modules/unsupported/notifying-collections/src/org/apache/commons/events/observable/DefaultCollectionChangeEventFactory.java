package org.apache.commons.events.observable ; 

import java.util.Collection ; 
import java.util.Map ; 

public class DefaultCollectionChangeEventFactory 
    implements CollectionChangeEventFactory { 

    private Object source = null ; 


    /**
     * Creates a new <code>CollectionChangeEventFactory</code> initialized
     * to produce events from the provided source.  The source must be 
     * one of the observable collection types (e.g., 
     * <code>BoundCollection</code> or <code>ConstrainedCollection</code>.)
     *
     * @param source The observed collection which acts as the source of all
     *   events produced by this factory.
     * @throws IllegalArgumentException if <code>source</code> is not 
     *   one of the observable collection types.
     */
    public DefaultCollectionChangeEventFactory(Object source)
        throws IllegalArgumentException { 

        setCollection(source) ; 
    }

    public DefaultCollectionChangeEventFactory() { 
    }

    /**
     * This method creates and returns a new 
     * DefaultCollectionChangeEventFactory.  As this class does not maintain
     * any state information other than the event source, and the 
     * event source is the only thing which is not supposed to be 
     * replicated according to the interface contract, this is entirely 
     * proper.
     * @return A new DefaultCollectionChangeEventFactory with the 
     *    source term uninitialized.
     */
    public Object clone() { 
        return new DefaultCollectionChangeEventFactory() ; 
    }

    public Object getCollection() { 
        return source ; 
    }

    /**
     * <p>
     * Sets the source of events produced by this factory.  This method may 
     * only be called once.  Attempting to change the source of the factory
     * when it has previously been set will cause this method to 
     * throw an <code>UnsupportedOperationException</code>.  This method 
     * is provided so that users may explicitly supply a factory to a 
     * bound or constrained collection decorator.  
     * </p>
     *
     * @param source The observed collection which acts as the source of all
     *   events produced by this factory.
     * @throws IllegalArgumentException if <code>source</code> is not 
     *   one of the observable collection types.
     * @throws UnsupportedOperationException if <code>source</code> has 
     *   previously been set.
     */
    public void setCollection(Object source) 
        throws IllegalArgumentException, UnsupportedOperationException { 

        // check for an existing collection
        if (this.source != null) {
            throw new UnsupportedOperationException(
               "Changing the event source is not allowed.") ; 
        }

        // check that the collection is the correct type.
        if ( !(source instanceof BoundCollection) && 
             !(source instanceof ConstrainedCollection) &&
             !(source instanceof BoundMap))  {

            throw new IllegalArgumentException(
              "Source must be a BoundCollection, ConstrainedCollection, " + 
              "or BoundMap.");
        }

        this.source = source ; 
    }

    public CollectionChangeEvent createAdd(Object element, boolean changed) {
        return new CollectionChangeEvent(
            source, CollectionChangeType.ADD, changed, element) ;
    }

    public CollectionChangeEvent createAddIndexed(
        int index, Object element, boolean changed) {

        return new CollectionChangeEvent(
            source, CollectionChangeType.ADD_INDEXED, changed, element, index) ;
    }

    public CollectionChangeEvent createAddNCopies(
        int copies, Object element, boolean changed) {

        return new CollectionChangeEvent(
            source, CollectionChangeType.ADD_NCOPIES, changed, element, copies);
    }

    public CollectionChangeEvent createAddIterated(
        int index, Object element, boolean changed) {

        return new CollectionChangeEvent(
            source, CollectionChangeType.ADD_ITERATED, changed, element, index);
    }

    public CollectionChangeEvent createAddAll(
        Collection element, boolean changed) {
        return new CollectionChangeEvent(
            source, CollectionChangeType.ADD_ALL, changed, element) ;
    }

    public CollectionChangeEvent createAddAllIndexed(
        int index, Collection element, boolean changed) {

        return new CollectionChangeEvent(
            source, CollectionChangeType.ADD_ALL_INDEXED, 
            changed, element, index);
    }


    public CollectionChangeEvent createClear(boolean changed) {

        return new CollectionChangeEvent(
            source, CollectionChangeType.CLEAR, changed) ;
    }
    
    public CollectionChangeEvent createRemove(Object element, boolean changed) {
        return new CollectionChangeEvent(
            source, CollectionChangeType.REMOVE, changed, element) ;
    }


    public CollectionChangeEvent createRemoveIndexed(
        int index, Object element, boolean changed) {

        return new CollectionChangeEvent(
            source, CollectionChangeType.REMOVE_INDEXED, 
            changed, element, index);
    }

    public CollectionChangeEvent createRemoveNCopies(
        int copies, Object element, boolean changed) {

        return new CollectionChangeEvent(
            source, CollectionChangeType.REMOVE_NCOPIES, 
            changed, element, copies);
    }

    public CollectionChangeEvent createRemoveNext(
        Object element, boolean changed) {

        return new CollectionChangeEvent(
            source, CollectionChangeType.REMOVE_NEXT, changed, element) ;
    }


    public CollectionChangeEvent createRemoveIterated(
        int index, Object element, boolean changed) {

        return new CollectionChangeEvent(
            source, CollectionChangeType.REMOVE_ITERATED, 
            changed, element, index);
    }

    public CollectionChangeEvent createRemoveAll(
        Collection element, boolean changed) {

        return new CollectionChangeEvent(
            source, CollectionChangeType.REMOVE_ALL, changed, element) ;
    }


    public CollectionChangeEvent createRetainAll(
        Collection element, boolean changed) {

        return new CollectionChangeEvent(
            source, CollectionChangeType.RETAIN_ALL, changed, element) ;
    }

    public CollectionChangeEvent createSetIndexed(
        int index, Object oldValue, Object element, boolean changed) {

        return new CollectionChangeEvent(
            source, CollectionChangeType.SET_INDEXED, 
            changed, element, oldValue, index) ;
    }

    public CollectionChangeEvent createSetIterated(
        int index, Object oldValue, Object element, boolean changed) {

        return new CollectionChangeEvent(
            source, CollectionChangeType.SET_ITERATED, 
            changed, element, oldValue, index) ;
    }

    public CollectionChangeEvent createPut(
        Object key, Object value, Object oldValue, boolean changed) {

        return new CollectionChangeEvent(
            source, CollectionChangeType.PUT, 
            changed, key, oldValue, value) ;
    }

    public CollectionChangeEvent createPutAll(
        Map map, boolean changed) {

        return new CollectionChangeEvent(
            source, CollectionChangeType.PUT_ALL, changed, map) ;
    }


}
