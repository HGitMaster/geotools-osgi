package org.apache.commons.events.observable ; 

import java.util.Collection;  
import java.util.Map ; 

/**
 * <p>
 * This interface defines a factory for the production of 
 * CollectionChangeEvents.  The purpose of this factory is to provide a means
 * of instantiating custom, user-defined CollectionChangeEvents without 
 * modification to the BoundCollection or ConstrainedCollection classes.
 * Should users decide to write their own CollectionChangeEvents which 
 * provide more information than the supplied classes, they would need to 
 * implement this interface and supply an instance to the appropriate 
 * ObservedCollection class.
 * </p>
 *
 * @see CollectionChangeEvent
 * @see CollectionChangeType
 * @see BoundCollection
 * @see ConstrainedCollection
 * @author Bryce Nordgren / USDA Forest Service
 * @since 0.1
 */
public interface CollectionChangeEventFactory extends Cloneable { 

    /**
     * Returns the collection responsible for events constructed by this 
     * factory.  This must be one of the decorator classes defined in 
     * this package.  
     * @return source of CollectionChangeEvents.
     */
    public Object getCollection() ; 

    /**
     * Sets the collection responsible for events constructed by this 
     * factory.  This must be one of the decorator classes defined in 
     * this package.  The type of this property is <code>Object</code> 
     * because <code>java.util.Map</code>, while part of the Collections
     * framework, does not share a common parentage with 
     * <code>java.util.Collection</code>.  Legal values are instances or 
     * subclasses of: 
     * <ul>
     * <li> BoundCollection </li>
     * <li> ConstrainedCollection </li>
     * <li> BoundMap </li>
     * </ul>
     * This method is only allowed to be called
     * once.  Subsequent calls should result in an 
     * UnsupportedOperationException.
     * @param source The source of CollectionChangeEvents.
     * @throws IllegalArgumentException if <code>source</code> is not 
     *    a BoundCollection or a ConstrainedCollection.
     * @throws UnsupportedOperationException if the source has already 
     *    been set.
     */
    public void setCollection(Object source) ; 

    /**
     * <p>
     * Clones a <code>CollectionChangeEventFactory</code> by
     * constructing a copy of the existing factory, <i>without</i> copying the 
     * event source.  The user <i>must</i> call <code>setCollection()</code>
     * on the returned factory prior to use.  In effect, this event factory
     * suffers from inadequate separation of concerns because it is required to 
     * know the details of event construction as well as the specific object
     * which caused the event.  This clone method causes duplication of the 
     * event construction logic while permitting the caller to specify a 
     * different event source.  As such, implementors should ensure that all 
     * configuration and setup from the original object is duplicated in the 
     * returned object <i>except for the event source</i>.
     * </p>
     * <p>
     * This method exists so that sub lists (and other derivative collections) 
     * can use the same event factory as the original list.
     * </p>
     * @return A duplicate of this factory, except the event source is not set.
     */
    public Object clone() ; 

    /**
     * Instantiates and returns a <code>CollectionChangeEvent</code>
     * of type <code>ADD</code>.  
     * 
     * @param element The element added to the collection.
     * @param changed <code>True</code> if the element was added to the 
     *    collecton, <code>false</code> otherwise.
     * @return A new event, properly initialized and ready to fire.
     */
    public CollectionChangeEvent createAdd(Object element, boolean changed) ; 



    /**
     * Instantiates and returns a <code>CollectionChangeEvent</code>
     * of type <code>ADD_INDEXED</code>.  The &quot;parameter&quot;
     * property is initialized to contain the index.
     * 
     * @param element The element added to the collection.
     * @param index The index at which the item is added.
     * @param changed <code>True</code> if the element was added to the 
     *    collecton, <code>false</code> otherwise.
     * @return A new event, properly initialized and ready to fire.
     */
    public CollectionChangeEvent createAddIndexed(
        int index, Object element, boolean changed) ; 



    /**
     * Instantiates and returns a <code>CollectionChangeEvent</code>
     * of type <code>ADD_NCOPIES</code>.  The &quot;parameter&quot;
     * property is initialized to contain the specified number of copies.
     * 
     * @param element The element added to the collection.
     * @param copies How many copies to add.
     * @param changed <code>True</code> if the element was added to the 
     *    collecton, <code>false</code> otherwise.
     * @return A new event, properly initialized and ready to fire.
     */
    public CollectionChangeEvent createAddNCopies(
        int copies, Object element, boolean changed) ; 


    /**
     * Instantiates and returns a <code>CollectionChangeEvent</code>
     * of type <code>ADD_ITERATED</code>.  The &quot;parameter&quot;
     * property is initialized to contain the index.
     * 
     * @param element The element added to the collection.
     * @param index The index at which the item is added.
     * @param changed <code>True</code> if the element was added to the 
     *    collecton, <code>false</code> otherwise.
     * @return A new event, properly initialized and ready to fire.
     */
    public CollectionChangeEvent createAddIterated(
        int index, Object element, boolean changed) ; 


    /**
     * Instantiates and returns a <code>CollectionChangeEvent</code>
     * of type <code>ADD_ALL</code>.  
     * 
     * @param element The collection containing all the elements to be added. 
     * @param changed <code>True</code> if the element was added to the 
     *    collecton, <code>false</code> otherwise.
     * @return A new event, properly initialized and ready to fire.
     */
    public CollectionChangeEvent createAddAll(
        Collection element, boolean changed) ; 


    /**
     * Instantiates and returns a <code>CollectionChangeEvent</code>
     * of type <code>ADD_ALL_INDEXED</code>.  The &quot;parameter&quot;
     * property is initialized to contain the index.
     * 
     * @param element The collection containing all the elements to be added.
     * @param index The index at which the collection is added.
     * @param changed <code>True</code> if the element was added to the 
     *    collecton, <code>false</code> otherwise.
     * @return A new event, properly initialized and ready to fire.
     */
    public CollectionChangeEvent createAddAllIndexed(
        int index, Collection element, boolean changed) ; 



    /**
     * Instantiates and returns a <code>CollectionChangeEvent</code>
     * of type <code>CLEAR</code>.  
     * 
     * @param changed <code>True</code> if the collection was nonempty 
     *    before it was cleared, <code>false</code> otherwise.
     * @return A new event, properly initialized and ready to fire.
     */
    public CollectionChangeEvent createClear(boolean changed) ; 



    /**
     * Instantiates and returns a <code>CollectionChangeEvent</code>
     * of type <code>REMOVE</code>.  
     * 
     * @param element The element removed from the collection.
     * @param changed <code>True</code> if the element was removed from the 
     *    collecton, <code>false</code> otherwise.
     * @return A new event, properly initialized and ready to fire.
     */
    public CollectionChangeEvent createRemove(Object element, boolean changed) ; 


    /**
     * Instantiates and returns a <code>CollectionChangeEvent</code>
     * of type <code>REMOVE_INDEXED</code>.  The &quot;parameter&quot;
     * property is initialized to contain the index.
     * 
     * @param element The element removed from the collection.
     * @param index The index of the removed item.
     * @param changed <code>True</code> if the element was removed from the 
     *    collecton, <code>false</code> otherwise.
     * @return A new event, properly initialized and ready to fire.
     */
    public CollectionChangeEvent createRemoveIndexed(
        int index, Object element, boolean changed) ; 



    /**
     * Instantiates and returns a <code>CollectionChangeEvent</code>
     * of type <code>REMOVE_NCOPIES</code>.  The &quot;parameter&quot;
     * property is initialized to contain the index.
     * 
     * @param element The element removed from the collection.
     * @param copies The number of copies to remove.
     * @param changed <code>True</code> if the element was removed from the 
     *    collecton, <code>false</code> otherwise.
     * @return A new event, properly initialized and ready to fire.
     */
    public CollectionChangeEvent createRemoveNCopies(
        int copies, Object element, boolean changed) ; 


    /**
     * Instantiates and returns a <code>CollectionChangeEvent</code>
     * of type <code>REMOVE_NEXT</code>.  The &quot;parameter&quot;
     * property is initialized to contain the index.  This event 
     * applies to a buffer.
     * 
     * @param element The element removed from the buffer.
     * @param changed <code>True</code> if the element was removed from the 
     *    collecton, <code>false</code> otherwise.
     * @return A new event, properly initialized and ready to fire.
     */
    public CollectionChangeEvent createRemoveNext(
        Object element, boolean changed) ; 



    /**
     * Instantiates and returns a <code>CollectionChangeEvent</code>
     * of type <code>REMOVE_ITERATED</code>.  The &quot;parameter&quot;
     * property is initialized to contain the index.
     * 
     * @param element The element removed from the collection.
     * @param index The index of the iterator when remove was called.
     * @param changed <code>True</code> if the element was removed from the 
     *    collecton, <code>false</code> otherwise.
     * @return A new event, properly initialized and ready to fire.
     */
    public CollectionChangeEvent createRemoveIterated(
        int index, Object element, boolean changed) ; 



    /**
     * Instantiates and returns a <code>CollectionChangeEvent</code>
     * of type <code>REMOVE_ALL</code>.  
     * 
     * @param element The collection containing all items to remove from
     *    this collection.
     * @param changed <code>True</code> if the element was removed from the 
     *    collecton, <code>false</code> otherwise.
     * @return A new event, properly initialized and ready to fire.
     */
    public CollectionChangeEvent createRemoveAll(
        Collection element, boolean changed) ; 



    /**
     * Instantiates and returns a <code>CollectionChangeEvent</code>
     * of type <code>RETAIN_ALL</code>.  
     * 
     * @param element The Collection containing all elements to be retained.
     * @param changed <code>True</code> if the observed collection was 
     *    changed.
     * @return A new event, properly initialized and ready to fire.
     */
    public CollectionChangeEvent createRetainAll(
        Collection element, boolean changed) ; 


    /**
     * Instantiates and returns a <code>CollectionChangeEvent</code>
     * of type <code>SET_INDEXED</code>.  The &quot;parameter&quot;
     * property is initialized to contain the index.  Additionally, the 
     * <code>oldValue</code> and <code>newValue</code> properties are 
     * set to the former and current values of the element at the index,
     * respectively.
     * 
     * @param element The new value of the element at <code>index</code>.  
     *    This is stored in the <code>element</code> and <code>newValue</code>
     *    properties.
     * @param oldValue The former value of the element at <code>index</code>.
     *    This is stored in the <code>oldValue</code> property.
     * @param index The index of the changed item.
     * @param changed <code>True</code> if the element was changed,
     *    <code>false</code> otherwise.
     * @return A new event, properly initialized and ready to fire.
     */
    public CollectionChangeEvent createSetIndexed(
        int index, Object oldValue, Object element, boolean changed) ; 



    /**
     * Instantiates and returns a <code>CollectionChangeEvent</code>
     * of type <code>SET_ITERATED</code>.  The &quot;parameter&quot;
     * property is initialized to contain the index.  Additionally, the 
     * <code>oldValue</code> and <code>newValue</code> properties are 
     * set to the former and current values of the element at the index,
     * respectively.
     * 
     * @param element The new value of the element at <code>index</code>.  
     *    This is stored in the <code>element</code> and <code>newValue</code>
     *    properties.
     * @param oldValue The former value of the element at <code>index</code>.
     *    This is stored in the <code>oldValue</code> property.
     * @param index The index of the changed item.
     * @param changed <code>True</code> if the element was changed,
     *    <code>false</code> otherwise.
     * @return A new event, properly initialized and ready to fire.
     */
    public CollectionChangeEvent createSetIterated(
        int index, Object oldValue, Object element, boolean changed) ; 

    /**
     * Instantiates and returns a <code>CollectionChangeEvent</code>
     * of type <code>PUT</code>.  This is unique among the 
     * <code>CollectionChangeEvents</code> in that a single addition
     * involves two objects, namely a key-value pair.  Additionally,
     * the if the key was already present in the map, the former 
     * value is returned.  The former and present &quot;values&quot; are 
     * stored in <code>oldValue</code> and <code>newValue</code> properties, 
     * respectively.  The <code>element</code> property is set to the key.
     * 
     * @param key The key provided to the <code>put()</code> method.
     * @param value The value provided to the <code>put()</code> method.
     * @param oldValue The value returned by the call to <code>put()</code>.
     * @param changed <code>True</code> if the element was changed,
     *    <code>false</code> otherwise.
     * @return A new event, properly initialized and ready to fire.
     */
    public CollectionChangeEvent createPut(
        Object key, Object value, Object oldValue, boolean changed) ; 

    /**
     * Instantiates and returns a <code>CollectionChangeEvent</code>
     * of type <code>PUT_ALL</code>.  The <code>element</code> property
     * is set to the Map containing all the entries to be added.
     * 
     * @param newElements The map containing the entries to add.
     * @param changed <code>True</code> if the element was changed,
     *    <code>false</code> otherwise.
     * @return A new event, properly initialized and ready to fire.
     */
    public CollectionChangeEvent createPutAll(
        Map newElements, boolean changed) ; 

}
