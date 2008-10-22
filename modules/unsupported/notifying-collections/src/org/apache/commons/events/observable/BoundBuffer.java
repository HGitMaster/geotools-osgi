package org.apache.commons.events.observable ; 

import org.apache.commons.collections.Buffer ; 

/**
 * <p>
 * Decorates a <code>Buffer</code> implementation with a <b>bound 
 * property</b> named &quot;collection&quot;.
 * </p>
 * <p>
 * Each modifying method call made on this <code>Buffer</code> is 
 * handled as a change to the &quot;collection&quot; property.  This 
 * facility serves to notify subscribers of a change to the buffer
 * but does not allow users the option of vetoing the change.  To 
 * gain the ability to veto the change, use a {@link ConstrainedBuffer}
 * decorater.
 * </p>
 * @since Commons Events 1.0
 * @author Stephen Colebourne, Bryce Nordgren
 */
public class BoundBuffer extends BoundCollection implements Buffer { 

    // Constructors
    //-----------------------------------------------------------------------
    protected BoundBuffer(
        final Buffer source, 
        final CollectionChangeEventFactory eventFactory) { 

        super(source,eventFactory) ; 
    }


    protected BoundBuffer(final Buffer source) { 
        super(source) ; 
    }

    // Factory methods
    //-----------------------------------------------------------------------
    public static BoundBuffer decorate(
        final Buffer source, 
        final CollectionChangeEventFactory eventFactory) { 

        return new BoundBuffer(source,eventFactory) ; 
    }

    public static BoundBuffer decorate(final Buffer source) { 
        return new BoundBuffer(source) ; 
    }


    // Utility methods
    //-----------------------------------------------------------------------
    private Buffer getBuffer() {
        return (Buffer)(getCollection()) ; 
    }


    // Buffer API (methods which do not change the collection)
    //-----------------------------------------------------------------------
    public Object get() { 
        return getBuffer().get() ; 
    }


    // Decorated Buffer methods
    //-----------------------------------------------------------------------
    public Object remove() {
        // relay request to wrapped buffer
        Object element = getBuffer().remove() ; 

        // construct and fire event.
        CollectionChangeEvent evt = eventFactory.createRemoveNext(
            element, true) ; 
        firePropertyChange(evt) ; 

        return element ;
    }
}
