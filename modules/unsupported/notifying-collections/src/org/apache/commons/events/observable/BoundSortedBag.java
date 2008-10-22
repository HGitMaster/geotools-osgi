package org.apache.commons.events.observable ; 

import java.util.Comparator ; 

import org.apache.commons.collections.SortedBag ; 

public class BoundSortedBag extends BoundBag implements SortedBag { 

    // Constructors
    //-----------------------------------------------------------------------
    protected BoundSortedBag(
        final SortedBag source,
        final CollectionChangeEventFactory eventFactory) { 

        super(source, eventFactory) ; 
    }

    protected BoundSortedBag(final SortedBag source) { 
        super(source) ; 
    }


    // Factory methods
    //-----------------------------------------------------------------------
    public static BoundSortedBag decorate(
        final SortedBag source,
        final CollectionChangeEventFactory eventFactory) { 

        return new BoundSortedBag(source, eventFactory) ; 
    }

    public static BoundSortedBag decorate(final SortedBag source) {
        return new BoundSortedBag(source) ; 
    }

    // Utility methods
    //-----------------------------------------------------------------------
    private SortedBag getSortedBag() { 
        return (SortedBag)(getCollection()) ; 
    }


    // SortedBag API (methods which do not change the collection)
    //-----------------------------------------------------------------------
    public Object first() { 
        return getSortedBag().first() ; 
    } 
    public Object last() { 
        return getSortedBag().last() ; 
    } 
    public Comparator comparator() { 
        return getSortedBag().comparator() ; 
    } 

}
