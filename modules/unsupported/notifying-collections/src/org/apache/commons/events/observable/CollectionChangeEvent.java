package org.apache.commons.events.observable ; 

import java.beans.PropertyChangeEvent ; 
import java.util.Collection; 

/**
 * <p>
 * This event is fired to all registered listeners whenever an observed 
 * collection is altered.  The inherited <code>oldValue</code> and 
 * <code>newValue</code> properties are almost always null, as populating 
 * them normally requires that two nearly identical copies of the collection 
 * be maintained.  The sole exception to this rule are events which 
 * replace a value in the collection with a different value (e.g., setting
 * a particular element of a <code>List</code>.)
 * </p>
 * <p>Instead, this event maintains 
 * information about <i>how</i> the collection was changed.  This information
 * includes: 
 * </p>
 *
 * <ol>
 * <li>The <i>type</i> of action performed on the collection (e.g., add, 
 *   remove, clear, etc.). </li>
 * <li>The <i>element</i> participating in the action (e.g., the thing which 
 *   was added, removed, etc.)</li>
 * <li>Whether or not the collection was changed as a result of the action.
 *   For instance, attempting to remove an element which is not in a set does
 *   not change the set.</li>
 * <li>An optional <i>parameter</i> which serves as an index or as an 
 *   indicator of the number of copies affected, as appropriate for the 
 *   event type.</li>
 * </ol>
 *
 * <p>
 * This event is always fired <i>after</i> the proposed action has taken place.
 * As such, it serves as a notification of an action, not 
 * <i>pre</i>-notification of the action.  This same event is fired to 
 * subscribers of <b><i>bound propertes</i></b> and <b><i>constrained
 * properties</i></b>.  Those who wish to affect whether or not the change 
 * actually occurs should use a ConstrainedCollection.  Those who merely wish
 * to be notified of changes should use an BoundCollection. 
 * </p>
 *
 * <p>
 * The parent of this event is designed to monitor arbitrary properties of 
 * java beans.  This class is designed to monitor only changes to 
 * collections.  Therefore, the &quot;property&quot; field of this 
 * event is always set to &quot;collection&quot;.  Likewise, the 
 * &quot;source&quot; property must always be a <code>BoundCollection</code>
 * or a <code>ConstrainedCollection</code>.
 * </p>
 * 
 * 
 * @see java.beans.PropertyChangeEvent
 * @see BoundCollection
 * @see ConstrainedCollection
 * @see CollectionChangeType
 * @author Bryce Nordgren / USDA Forest Service
 */ 
public class CollectionChangeEvent extends PropertyChangeEvent { 

    /** The value to which every &quot;property&quot; field is set. */
    public final static String PROPERTY = "collection" ; 

    /** A value for the &quot;parameter&quot; property which means 
     * that the property has not been set. */
    public final static int NOT_SET = -1 ; 

    /** Describes the action which caused this event. */
    private final CollectionChangeType type ; 

    /** Indicates whether the collection was changed as a result of the 
        action.*/
    private final boolean changed ; 

    /** A reference to the element participating in the action. */
    private final Object element ; 

    /** An integer parameter used to convey either the index or the 
     *  number of copies.
     */
    private final int parameter ; 

    /**
     * Public constructor for an event which monitors changes to 
     * collections.  This constructor ensures that the old and 
     * new properties are set to null.  It also ensures that the 
     * property name is set to &quot;collection&quot;, as specified
     * by the static <code>PROPERTY</code> field of this class.
     * @param source The collection which was modified.
     * @param type   The type of modification.
     * @param changed Was the collection actually changed?
     * @param element The element participating in the modification.
     * @param parameter Used to indicate number of copies or index.
     */
    public CollectionChangeEvent(Object source,
        final CollectionChangeType type, final boolean changed, 
        final Object element, final int parameter) {

        super(source, PROPERTY, null, null) ; 
        this.type = type ; 
        this.changed = changed ; 
        this.element = element ; 
        this.parameter = parameter ; 
    }

    public CollectionChangeEvent(Object source,
        final CollectionChangeType type, final boolean changed, 
        final Object element) {

        this(source, type, changed, element, NOT_SET) ; 
    }

    public CollectionChangeEvent(Object source,
        final CollectionChangeType type, final boolean changed) {

        this(source, type, changed, null, NOT_SET) ; 
    }

    /**
     * <p>
     * Public constructor which sets the oldValue and newValue properties
     * of the parent object.  This should be used only when an individual 
     * element of a collection is changed from one value to another.  This 
     * is applicable, for instance, when a map entry is changed or a list 
     * entry is set.  The constructed object will have identical values for
     * the &quot;element&quot; property and the &quot;newValue&quot; 
     * property.
     * </p>
     * @param source The collection which was modified.
     * @param type   The type of modification.
     * @param changed Was the collection actually changed?
     * @param element The new value of the element.
     * @param oldElement the old value of the element.
     * @param parameter The index of the changed element.
     */
    public CollectionChangeEvent(Object source,
        final CollectionChangeType type, final boolean changed, 
        final Object element, final Object oldElement, final int parameter) {

        super(source, PROPERTY, oldElement, element) ; 
        this.type = type ; 
        this.changed = changed ; 
        this.element = element ; 
        this.parameter = parameter ; 
    }

    /**
     * <p>
     * Public constructor which initializes the event appropriately for 
     * reporting a <code>put()</code> event on a Map.  The oldValue and 
     * newValue properties are set to the former and current values mapped
     * to the key.  The element property is set to the key.
     * </p>
     * @param source The collection which was modified.
     * @param type   The type of modification.
     * @param changed Was the collection actually changed?
     * @param oldValue The old value mapped to the key.
     * @param newValue The new value mapped to the key.
     * @param key The key which is being put in the Map.
     */
    public CollectionChangeEvent(Object source,
        final CollectionChangeType type, final boolean changed, 
        final Object key, final Object oldValue, final Object newValue) {

        super(source, PROPERTY, oldValue, newValue) ; 
        this.type = type ; 
        this.changed = changed ; 
        this.element = key ; 
        this.parameter = NOT_SET ; 
    }


    /** Describes the action which caused this event.
     * @return type of action performed on the collection
     */
    public CollectionChangeType getType() { 
        return type ; 
    }

    /** A reference to the element participating in the action.  This 
     * may be <code>null</code> if the change was an operation like 
     * <code>clear()</code>.
     * @return the element added or removed from the collection.
     */
    public Object getElement() {
        return element ; 
    }

    /** Indicates whether the collection was changed as a result of the 
     *  action.
     * @return <code>true</code> if the collection was changed, 
     *         <code>false</code> otherwise.
     */
    public boolean isChanged() {
        return changed ;
    }

    /**
     * The &quot;parameter&quot; is an indication of the index into the 
     * collection or the number of copies added or removed by the operation. 
     * In many cases, the &quot;parameter&quot; will not be set, in which 
     * case it takes on the value <code>NOT_SET</code>.
     * @return the index, number of copies, or <code>NOT_SET</code>
     */
    public int getParameter() {
        return parameter ; 
    }
}
