package org.apache.commons.events.observable ; 


import java.util.Set ; 

/**
 * <p>
 * Decorates a <code>Set</code> implementation with a <b>bound 
 * property</b> named &quot;collection&quot;.
 * </p>
 * <p>
 * Each modifying method call made on this <code>Set</code> is 
 * handled as a change to the &quot;collection&quot; property.  This 
 * facility serves to notify subscribers of a change to the set
 * but does not allow users the option of vetoing the change.  To 
 * gain the ability to veto the change, use a {@link ConstrainedSet}
 * decorater.
 * </p>
 * @see BoundCollection
 * @since Commons Events 1.0 
 * @author Stephen Colebourne, Bryce Nordgren
 */
public class BoundSet extends BoundCollection implements Set { 
    // Constructors
    //-----------------------------------------------------------------------
    protected BoundSet(
            final Set source , 
            final CollectionChangeEventFactory eventFactory ) { 

        super(source, eventFactory) ; 
    }

    protected BoundSet(final Set source) { 
        super(source) ; 
    }

    // Factory methods
    //-----------------------------------------------------------------------
    /**
     * Factory method to decorate an existing Set with a bound 
     * &quot;collection&quot; property and a user-specified 
     * event factory. 
     * @param source The Set to decorate by wrapping (not copying). 
     * @param eventFactory The event factory to use when producing events.
     * @return The bound set.
     * @throws IllegalArgumentException if the source null
     */
    public static BoundSet decorate(
            final Set source,  
            final CollectionChangeEventFactory eventFactory)  {
        return new BoundSet(source,eventFactory) ; 
    }

    /**
     * Factory method to decorate an existing Set with a bound 
     * &quot;collection&quot; property and the default 
     * event factory. 
     * @param source The Set to decorate by wrapping (not copying). 
     * @return The bound set.
     * @throws IllegalArgumentException if the source null
     */
    public static BoundSet decorate(final Set source) { 
        return new BoundSet(source) ; 
    }
} 
