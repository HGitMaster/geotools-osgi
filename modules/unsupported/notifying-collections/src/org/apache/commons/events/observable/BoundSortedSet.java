package org.apache.commons.events.observable ; 

import java.util.SortedSet ; 
import java.util.Comparator ; 

/**
 * <p>
 * Decorates a <code>SortedSet</code> implementation with a <b>bound 
 * property</b> named &quot;collection&quot;.
 * </p>
 * <p>
 * Each modifying method call made on this <code>SortedSet</code> is 
 * handled as a change to the &quot;collection&quot; property.  This 
 * facility serves to notify subscribers of a change to the collection
 * but does not allow users the option of vetoing the change.  To 
 * gain the ability to veto the change, use a {@link ConstrainedSortedSet}
 * decorater.
 * </p>
 * <p>
 * The SortedSet interface supports three methods which provide 
 * views into the data maintained by this object.  These views are 
 * &quot;backed by&quot; this set, and therefore changes in the main object
 * or in any view are reflected by any other view.  This decorater 
 * ensures that changes made to any of the views are propagated to 
 * listeners registered in all of the views.
 * </p>
 * @since Commons Events 1.0
 * @author Stephen Colebourne, Bryce Nordgren
 */
public class BoundSortedSet extends BoundSet implements SortedSet { 

    // Constructors
    //-----------------------------------------------------------------------
    protected BoundSortedSet(
        final SortedSet source , 
        final CollectionChangeEventFactory eventFactory) { 

        super(source, eventFactory) ; 
    }

    protected BoundSortedSet(final SortedSet source) { 
        super(source) ;
    }

    // Factory methods
    //-----------------------------------------------------------------------
    public static BoundSortedSet decorate(
        final SortedSet source , 
        final CollectionChangeEventFactory eventFactory) { 
        return new BoundSortedSet(source, eventFactory) ; 
    }

    public static BoundSortedSet decorate(final SortedSet source) { 
        return new BoundSortedSet(source) ; 
    }

    // Utility methods
    //-----------------------------------------------------------------------
    /**
     * Typecasting method to get the sorted set functionality.
     * @return the decorated collection as a SortedSet.
     */
    private SortedSet getSortedSet() {
        return (SortedSet)(getCollection()) ; 
    }

    private SortedSet bindSortedSet(SortedSet unbound) { 
        // clone the event factory
        CollectionChangeEventFactory factoryCopy = 
            (CollectionChangeEventFactory)(eventFactory.clone()) ;

        // bind the sortedset
        BoundSortedSet boundSet = BoundSortedSet.decorate(
            unbound, factoryCopy);

        // send "boundSet's" events to our listeners
        boundSet.addPropertyChangeListener(createEventRepeater()) ; 

        // send our events to "boundSet's" listeners
        addPropertyChangeListener(boundSet.createEventRepeater()) ; 

        return boundSet ;
    }


    // SortedSet API (methods which don't change the set.)
    //-----------------------------------------------------------------------
    public Comparator comparator() { 
        return getSortedSet().comparator() ; 
    }
    public Object first() { 
        return getSortedSet().first() ; 
    }
    public Object last() { 
        return getSortedSet().last() ; 
    }

    // Decorated methods
    //-----------------------------------------------------------------------
    public SortedSet subSet(Object from, Object to) {
        SortedSet unbound = getSortedSet().subSet(from, to) ;
        return bindSortedSet(unbound) ; 
    }

    public SortedSet headSet(Object to) {
        SortedSet unbound = getSortedSet().headSet(to) ;
        return bindSortedSet(unbound) ; 
    }

    public SortedSet tailSet(Object from) { 
        SortedSet unbound = getSortedSet().tailSet(from) ;
        return bindSortedSet(unbound) ; 
    }
}
