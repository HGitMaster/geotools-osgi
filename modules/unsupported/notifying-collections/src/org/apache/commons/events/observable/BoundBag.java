package org.apache.commons.events.observable ; 

import java.util.Set ; 
import org.apache.commons.collections.Bag ; 


public class BoundBag extends BoundCollection implements Bag  {

    // Constructors
    //-----------------------------------------------------------------------
    protected BoundBag(
        final Bag source, 
        final CollectionChangeEventFactory eventFactory) { 

        super(source, eventFactory) ; 
    }

    protected BoundBag(final Bag source) { 
        super(source) ; 
    }

    // Factory methods
    //-----------------------------------------------------------------------
    public static BoundBag decorate(
        final Bag source, 
        final CollectionChangeEventFactory eventFactory) { 

        return new BoundBag(source, eventFactory) ; 
    }

    public static BoundBag decorate(final Bag source) { 
        return new BoundBag(source) ; 
    }

    // Utility methods
    //-----------------------------------------------------------------------
    private Bag getBag() { 
        return (Bag)(getCollection()) ; 
    }

    
    // Bag API (methods which don't change the collection)
    //-----------------------------------------------------------------------
    public int getCount(Object object) {
        return getBag().getCount(object);
    }

    public Set uniqueSet() {
        return getBag().uniqueSet();
    }
    

    // Decorated methods
    //-----------------------------------------------------------------------
    // overridden because of the contract violation w.r.t. Collections.
    public boolean add(Object object) {
        boolean result = getBag().add(object) ; 
        eventFactory.createAdd(object, true) ; 
        return result ; 
    }

    public boolean add(Object object, int nCopies) {
        boolean changed = (nCopies != 0) ; 
        boolean result = getBag().add(object,nCopies) ; 
        eventFactory.createAddNCopies(nCopies, object, changed) ; 
        return result ; 
    }

    public boolean remove(Object object, int nCopies) {
        boolean changed = getBag().remove(object, nCopies) ; 
        eventFactory.createRemoveNCopies(nCopies, object, changed) ; 
        return changed ; 
    }
}
