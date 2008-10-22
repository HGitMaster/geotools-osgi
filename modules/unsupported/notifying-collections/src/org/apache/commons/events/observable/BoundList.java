package org.apache.commons.events.observable ; 

import java.beans.PropertyChangeEvent ; 
import java.beans.PropertyChangeListener ; 

import java.util.List; 
import java.util.ListIterator; 
import java.util.Collection ; 

import org.apache.commons.collections.iterators.AbstractListIteratorDecorator;


/**
 * Decorates a List interface with a <b>bound property</b> called 
 * &quot;collection&quot;.
 * <p> 
 * Each modifying method call made on this <code>List</code> is 
 * handled as a change to the &quot;collection&quot; property.  This 
 * facility serves to notify subscribers of a change to the collection
 * but does not allow users the option of vetoing the change.  To 
 * gain the ability to veto the change, use a {@link ConstrainedList}
 * decorater.
 * </p>
 * <p>
 * Registered listeners must implement the {@link 
 * java.beans.PropertyChangeListener} interface.  Each change request causes a 
 * {@link CollectionChangeEvent} to be fired <i>after</i> the request 
 * has been executed.  The {@link CollectionChangeEvent} provides an 
 * indication of the type of change, the element participating in the 
 * change, and whether or not the <code>List</code> was actually affected by the
 * change request.  As such, receiving a <code>CollectionChangeEvent</code>
 * is merely an indication that a change was attempted, not that the 
 * List is actually different.
 * </p>
 * @see BoundCollection
 * @see CollectionChangeEvent
 * @since Commons Events 1.0
 * @version $Revision$ $Date$
 * @author Stephen Colebourne, Bryce Nordgren
 */
public class BoundList extends BoundCollection implements List {
    // Constructors
    //-----------------------------------------------------------------------
    /**
     * Constructor that wraps (not copies.)
     * @param source  the List to decorate, must not be null
     * @param eventFactory the factory which instantiates 
     *    {@link CollectionChangeEvent}s.  
     * @throws IllegalArgumentException if the collection is null
     * @throws UnsupportedOperationException if the <code>eventFactory</code>
     *     has already been used with another collection decorator.
     */
    protected BoundList(
            final List source, 
            final CollectionChangeEventFactory eventFactory) { 
        super(source, eventFactory) ; 
    }

    /**
     * Constructor that wraps (not copies.)
     * @param source  the List to decorate, must not be null
     * @throws IllegalArgumentException if the collection is null
     */
    protected BoundList(final List source) { 
        super(source) ; 
    }


    // Factory methods
    //-----------------------------------------------------------------------
    public static BoundList decorate(final List source) { 
        return new BoundList(source) ; 
    }

    public static BoundList decorate(
            final List source, 
            final CollectionChangeEventFactory eventFactory) { 

        return new BoundList(source, eventFactory) ; 
    }


    // Utility methods
    //-----------------------------------------------------------------------
    /**
     * Typecast the collection to a List.
     * 
     * @return the wrapped collection as a List
     */
    private List getList() {
        return (List) getCollection();
    }


    // List API (these methods don't change the List) 
    //-----------------------------------------------------------------------
    public Object get(int index) {
        return getList().get(index);
    }

    public int indexOf(Object object) {
        return getList().indexOf(object);
    }

    public int lastIndexOf(Object object) {
        return getList().lastIndexOf(object);
    }

    // Decorator methods
    //-----------------------------------------------------------------------
    /**
     * <p>
     * Inserts an object at the specified index (optional operation).  
     * This method is assumed 
     * to succeed if no exceptions are thrown.  It is important to note that
     * it is impossible to document all cases where an exception is 
     * thrown, as this is totally determined by the implementation class
     * of the decorated List.  See the documentation for the decorated
     * List for details.
     * </p>
     * <p>
     * This method will only fire a <code>CollectionChangeEvent</code>
     * if the operation succeeds.  Therefore, calling this method will 
     * always result in a <code>CollectionChangeEvent</code> or an
     * exception from the decorated list.  An exception prevents the 
     * <code>CollectionChangeEvent</code> from being fired.
     * </p>
     * 
     * @param index The index at which to insert the object.
     * @param object The object to insert in the list.
     * @throws IndexOutOfBoundsException if the index does not represent a 
     *      position in the List.
     * @throws UnsupportedOperationException if the decorated list does
     *      not support this optional operation.
     */ 
    public void add(int index, Object object) {
        getList().add(index, object) ; 
        CollectionChangeEvent evt = eventFactory.createAddIndexed(
            index, object, true) ; 
        firePropertyChange(evt) ; 
    }    

    /**
     * <p>
     * Inserts all of the elements in the specified collection into this 
     * list at the specified position (optional operation). 
     * </p>
     *
     * <p>
     * This method will only fire a <code>CollectionChangeEvent</code>
     * if the decorated list completes the call normally.  Therefore, 
     * calling this method will 
     * always result in a <code>CollectionChangeEvent</code> or an
     * exception from the decorated list.  An exception prevents the 
     * <code>CollectionChangeEvent</code> from being fired.  Unlike the 
     * {@link #add(int index, Object object)} method, however, an event 
     * may be fired when the collection has not changed.  It is therefore
     * important to check the {@link CollectionChangeEvent.isChanged()} 
     * flag.
     * </p>
     * @param index The index at which to insert the collection.
     * @param coll The collection of objects to insert in the list.
     * 
     * @throws IndexOutOfBoundsException if the index does not represent a 
     *      position in the List.
     * @throws UnsupportedOperationException if the decorated list does
     *      not support this optional operation.
     */
    public boolean addAll(int index, Collection coll) {
        boolean changed = getList().addAll(index, coll) ; 
        CollectionChangeEvent evt = eventFactory.createAddAllIndexed(
            index, coll, changed) ; 
        firePropertyChange(evt) ; 

        return changed ; 
    }

    /** 
     * <p>
     * Removes the element at the specified position in this list 
     * (optional operation). Shifts any subsequent elements to the left 
     * (subtracts one from their indices). Returns the element that was 
     * removed from the list.
     * </p>
     * <p>
     * This method will only fire a <code>CollectionChangeEvent</code>
     * if the operation succeeds.  Therefore, calling this method will 
     * always result in a <code>CollectionChangeEvent</code> or an
     * exception from the decorated list.  An exception prevents the 
     * <code>CollectionChangeEvent</code> from being fired.
     * </p>
     *
     * @param index The index of the element to remove.
     * @return The object which was removed from the list.
     * @throws IndexOutOfBoundsException if the index does not represent a 
     *      position in the List.
     * @throws UnsupportedOperationException if the decorated list does
     *      not support this optional operation.
     */
    public Object remove(int index) {
        Object element = getList().remove(index) ; 
        CollectionChangeEvent evt = eventFactory.createRemoveIndexed(
            index, element, true) ; 
        firePropertyChange(evt) ; 

        return element ; 
    }

    /**
     * <p>
     * Replaces the element at the specified position in this list with 
     * the specified element (optional operation).  
     * </p>
     * <p>
     * This method will only fire a <code>CollectionChangeEvent</code>
     * if the operation succeeds.  Therefore, calling this method will 
     * always result in a <code>CollectionChangeEvent</code> or an
     * exception from the decorated list.  An exception prevents the 
     * <code>CollectionChangeEvent</code> from being fired.
     * </p>
     *
     * @param index The index of the element to change.
     * @param object The new value to place at location <code>index</code>.
     * @return The object formerly at location <code>index</code>.
     * @throws IndexOutOfBoundsException if the index does not represent a 
     *      position in the List.
     * @throws UnsupportedOperationException if the decorated list does
     *      not support this optional operation.
     */
    public Object set(int index, Object object) {
        Object oldElement = getList().set(index, object) ; 
        CollectionChangeEvent evt = eventFactory.createSetIndexed(
            index, oldElement, object, true) ; 
        firePropertyChange(evt) ; 

        return oldElement ; 
    }

    public ListIterator listIterator() {
        return new BoundListIterator(getList().listIterator());
    }

    public ListIterator listIterator(int index) {
        return new BoundListIterator(getList().listIterator(index));
    }


    /**
     * <p>
     * Returns a view of the portion of this list between fromIndex, 
     * inclusive, and toIndex, exclusive. (If fromIndex and toIndex are equal, 
     * the returned list is empty.) The returned list is backed by this list, 
     * so changes in the returned list are reflected in this list, and 
     * vice-versa. The returned list supports all of the optional list 
     * operations supported by this list.
     * </p>
     * <p>
     * Because changes in the subList are reflected in the list, this 
     * decorator implementation ensures that listeners registered with this
     * list are notified of changes made through the sublist interface.
     * Changes caused by the sub list view will create events which have the 
     * sub list as the <code>source</code> property.  This is likely to 
     * be important as the view causing the change will impact the 
     * indicies of any indexed events.  The indices are consistent with the 
     * List view specified as the source object.
     * </p>
     * <p>
     * Listeners may be added or removed
     * from the main list before or after the sub list is created.  Events 
     * will be fired to listeners registered at the time of the event, not 
     * at the time the sublist was created.  Listeners may also be added 
     * separately to the sublist, if desired.
     * </p>
     * @param fromIndex first index included in the sublist view
     * @param toIndex first index <i>not</i> included in the sublist view.
     * @return The sublist view backed by this list.
     */
    public List subList(int fromIndex, int toIndex) {
        // make the subList view
        List subList = getList().subList(fromIndex, toIndex) ; 

        // clone the event factory we're using.
        CollectionChangeEventFactory factoryCopy = 
            (CollectionChangeEventFactory)(eventFactory.clone()) ; 

        // decorate the sub list view.
        BoundList boundSubList = BoundList.decorate(subList, factoryCopy) ;  

        // relay events from sublist to our subscribers
        boundSubList.addPropertyChangeListener(createEventRepeater()) ; 

        // relay events from main list to sublist subscribers
        addPropertyChangeListener(boundSubList.createEventRepeater()) ; 

        return boundSubList ; 
    }     


    /**
     * Inner class ListIterator for the BoundList.
     */
    protected class BoundListIterator extends AbstractListIteratorDecorator {
        
        protected Object last;
        
        protected BoundListIterator(ListIterator iterator) {
            super(iterator);
        }
        
        public Object next() {
            last = super.next();
            return last;
        }

        public Object previous() {
            last = iterator.previous();
            return last;
        }

        public void remove() {
            int index = iterator.previousIndex();
            iterator.remove();
            CollectionChangeEvent evt = eventFactory.createRemoveIterated(
                index, last, true) ; 
            firePropertyChange(evt) ;
        }
        
        public void add(Object object) {
            int index = iterator.nextIndex();
            iterator.add(object);
            CollectionChangeEvent evt = eventFactory.createAddIterated(
                index, object, true) ; 
            firePropertyChange(evt) ;
        }

        public void set(Object object) {
            int index = iterator.previousIndex();
            iterator.set(object);
            CollectionChangeEvent evt = eventFactory.createSetIterated(
                index, last, object, true) ; 
            firePropertyChange(evt) ;
        }
    }
}
