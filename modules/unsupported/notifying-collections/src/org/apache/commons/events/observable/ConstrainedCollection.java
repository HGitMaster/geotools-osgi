package org.apache.commons.events.observable;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections.collection.AbstractCollectionDecorator;
import org.apache.commons.collections.iterators.AbstractIteratorDecorator;

/**
 * Decorates a <code>Collection</code> implementation to observe modifications.
 * <p>
 * Each modifying method call made on this <code>Collection</code> is forwarded to a
 * {@link ModificationHandler}.
 * The handler manages the event, notifying listeners and optionally vetoing changes.
 * The default handler is {@link StandardModificationHandler}.
 * See this class for details of configuration available.
 *
 * @since Commons Events 1.0
 * @version $Revision: 155443 $ $Date: 2005-02-26 06:19:51 -0700 (Sat, 26 Feb 2005) $
 * 
 * @author Stephen Colebourne
 */
public class ConstrainedCollection extends AbstractCollectionDecorator {
    
    
    // ObservableCollection factories
    //-----------------------------------------------------------------------
    /**
     * Factory method to create an observable collection.
     * <p>
     * A {@link StandardModificationHandler} will be created.
     * This can be accessed by {@link #getHandler()} to add listeners.
     *
     * @param coll  the collection to decorate, must not be null
     * @return the observed collection
     * @throws IllegalArgumentException if the collection is null
     */
    public static ConstrainedCollection decorate(final Collection coll) {
        return new ConstrainedCollection(coll, null);
    }

    /**
     * Factory method to create an observable collection using a listener or a handler.
     * <p>
     * A lot of functionality is available through this method.
     * If you don't need the extra functionality, simply implement the
     * {@link org.apache.commons.events.observable.standard.StandardModificationListener}
     * interface and pass it in as the second parameter.
     * <p>
     * Internally, an <code>ObservableCollection</code> relies on a {@link ModificationHandler}.
     * The handler receives all the events and processes them, typically by
     * calling listeners. Different handler implementations can be plugged in
     * to provide a flexible event system.
     * <p>
     * The handler implementation is determined by the listener parameter via
     * the registered factories. The listener may be a manually configured 
     * <code>ModificationHandler</code> instance.
     * <p>
     * The listener is defined as an Object for maximum flexibility.
     * It does not have to be a listener in the classic JavaBean sense.
     * It is entirely up to the factory and handler as to how the parameter
     * is interpretted. An IllegalArgumentException is thrown if no suitable
     * handler can be found for this listener.
     * <p>
     * A <code>null</code> listener will create a {@link StandardModificationHandler}.
     *
     * @param coll  the collection to decorate, must not be null
     * @param listener  collection listener, may be null
     * @return the observed collection
     * @throws IllegalArgumentException if the collection is null
     * @throws IllegalArgumentException if there is no valid handler for the listener
     */
    public static ConstrainedCollection decorate(
            final Collection coll,
            final Object listener) {
        
        if (coll == null) {
            throw new IllegalArgumentException("Collection must not be null");
        }
        return new ConstrainedCollection(coll, listener);
    }


    // Constructors
    //-----------------------------------------------------------------------
    /**
     * Constructor that wraps (not copies) and takes a handler.
     * <p>
     * The handler implementation is determined by the listener parameter via
     * the registered factories. The listener may be a manually configured 
     * <code>ModificationHandler</code> instance.
     * 
     * @param coll  the collection to decorate, must not be null
     * @param listener  the observing handler, may be null
     * @throws IllegalArgumentException if the collection is null
     */
    protected ConstrainedCollection(
            final Collection coll,
            final Object listener) {
        super(coll);
    }

    /**
     * Constructor used by subclass views, such as subList.
     * 
     * @param coll  the collection to decorate, must not be null
     * @throws IllegalArgumentException if the collection is null
     */
    protected ConstrainedCollection(
            final Collection coll) {
        super(coll);
    }

}
