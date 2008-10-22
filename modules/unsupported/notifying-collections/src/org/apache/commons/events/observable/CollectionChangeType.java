/*
 * Copyright 2003-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.events.observable;

import java.util.Map ; 
import java.util.HashMap ; 

/**
 * Defines event constants for event handling and matching.
 * <p>
 * The constants in this class are of two types:
 * <ol>
 * <li>Methods - the base definitions (unique bits)
 * <li>Groups - combination definitions (method bits combined)
 * </ol>
 * <p>
 * Only a method constant may be compared using == to an event type.
 * This can include use in a switch statement
 * <p>
 * Any constant may be used for filtering.
 * They may combined using the bitwise OR, <code>|</code>.
 * They may negated using the bitwise NOT, <code>~</code>.
 *
 * @since Commons Events 1.0
 * @version $Revision: 155443 $ $Date: 2005-02-26 06:19:51 -0700 (Sat, 26 Feb 2005) $
 * 
 * @author Stephen Colebourne
 */
public class CollectionChangeType {
    
    /** The method add(Object) */
    public static final CollectionChangeType ADD ;

    /** The method add(int,Object) */
    public static final CollectionChangeType ADD_INDEXED ;

    /** The method add(Object,int) */
    public static final CollectionChangeType ADD_NCOPIES ;

    /** The method iterator.add(Object) */
    public static final CollectionChangeType ADD_ITERATED ;
    
    /** The method addAll(Collection) */
    public static final CollectionChangeType ADD_ALL ;

    /** The method addAll(int,Collection) */
    public static final CollectionChangeType ADD_ALL_INDEXED;
    

    /** The method remove(Object) */
    public static final CollectionChangeType REMOVE ;
    /** The method remove(int) */
    public static final CollectionChangeType REMOVE_INDEXED ;
    /** The method remove(Object,int) */
    public static final CollectionChangeType REMOVE_NCOPIES ;
    /** The method remove() */
    public static final CollectionChangeType REMOVE_NEXT    ;
    /** The method iterator.remove() */
    public static final CollectionChangeType REMOVE_ITERATED;
    
    /** The method removeAll(Collection) */
    public static final CollectionChangeType REMOVE_ALL ;
    /** The method retainAll(Collection) */
    public static final CollectionChangeType RETAIN_ALL ;
    /** The method clear() */
    public static final CollectionChangeType CLEAR ;
    
    /** The method set(int,Object) */
    public static final CollectionChangeType SET_INDEXED ;
    /** The method iterator.set(Object) */
    public static final CollectionChangeType SET_ITERATED ;

    /** The method put(key, value) */
    public static final CollectionChangeType PUT ; 
    /** The method putAll(Map) */
    public static final CollectionChangeType PUT_ALL ; 

    /** All add methods */
    public static final int GROUP_ADD ;
    /** All methods that change without structure modification */
    public static final int GROUP_CHANGE ;
    /** All remove methods */
    public static final int GROUP_REMOVE ;
    /** All retain methods */
    public static final int GROUP_RETAIN ;
    /** All clear methods */
    public static final int GROUP_CLEAR ;
    /** All reducing methods (remove, retain and clear) */
    public static final int GROUP_REDUCE ;

    /** All indexed methods */
    public static final int GROUP_INDEXED ;
    /** All ncopies methods */
    public static final int GROUP_NCOPIES ;
    /** All iterated methods */
    public static final int GROUP_ITERATED ;
    /** All 'next' methods */
    public static final int GROUP_NEXT ;
    /** All bulk methods (xxxAll, clear) */
    public static final int GROUP_BULK ;
    /** All methods that modify the structure */
    public static final int GROUP_STRUCTURE_MODIFIED ;

    /** All methods sent by a Collection */
    public static final int GROUP_FROM_COLLECTION ;
    /** All methods sent by a Set */
    public static final int GROUP_FROM_SET ;
    /** All methods sent by a List */
    public static final int GROUP_FROM_LIST ;
    /** All methods sent by a Bag */
    public static final int GROUP_FROM_BAG ;
    /** All methods sent by a Buffer */
    public static final int GROUP_FROM_BUFFER ;
    /** All methods sent by a Map */
    public static final int GROUP_FROM_MAP ;

    /** No methods */
    public static final int GROUP_NONE = 0x00000000;
    /** All methods */
    public static final int GROUP_ALL = 0xFFFFFFFF;

	private final int code ; // field to store code
	private final String desc ; // field to store description

	private static final Map values ; 

	static { 
        // initialize the map of all values...
		values = new HashMap() ;

        // initialize statically defined event types.
        ADD             = new CollectionChangeType(0x00000001, "Add");
        ADD_INDEXED     = new CollectionChangeType(0x00000002, "AddIndexed");
        ADD_NCOPIES     = new CollectionChangeType(0x00000004, "AddNCopies");
        ADD_ITERATED    = new CollectionChangeType(0x00000008, "AddIterated");
        ADD_ALL         = new CollectionChangeType(0x00000010, "AddAll");
        ADD_ALL_INDEXED = new CollectionChangeType(0x00000020, "AddAllIndexed");
    
        REMOVE         = new CollectionChangeType(0x00000100, "Remove");
        REMOVE_INDEXED = new CollectionChangeType(0x00000200, "RemoveIndexed");
        REMOVE_NCOPIES = new CollectionChangeType(0x00000400, "RemoveNCopies");
        REMOVE_NEXT    = new CollectionChangeType(0x00000800, "RemoveNext");
        REMOVE_ITERATED= new CollectionChangeType(0x00001000, "RemoveIterated");

        REMOVE_ALL =  new CollectionChangeType(0x00002000, "RemoveAll");
        RETAIN_ALL =  new CollectionChangeType(0x00004000, "RetainAll");
        CLEAR      =  new CollectionChangeType(0x00008000, "Clear");

        SET_INDEXED  =  new CollectionChangeType(0x00010000, "SetIndexed");
        SET_ITERATED =  new CollectionChangeType(0x00020000, "SetIterated");

        PUT      =  new CollectionChangeType(0x00040000, "Put");
        PUT_ALL  =  new CollectionChangeType(0x00080000, "PutAll");

        GROUP_ADD = 
            ADD.code | 
            ADD_INDEXED.code | 
            ADD_NCOPIES.code | 
            ADD_ITERATED.code | 
            ADD_ALL.code | 
            ADD_ALL_INDEXED.code | 
            PUT.code | 
            PUT_ALL.code ; 

        GROUP_CHANGE = 
            SET_INDEXED.code | 
            SET_ITERATED.code ;

        GROUP_REMOVE = 
            REMOVE.code | 
            REMOVE_INDEXED.code | 
            REMOVE_NCOPIES.code | 
            REMOVE_ITERATED.code | 
            REMOVE_NEXT.code | 
            REMOVE_ALL.code;

        GROUP_RETAIN = RETAIN_ALL.code;
        GROUP_CLEAR = CLEAR.code;

        GROUP_INDEXED = 
            ADD_INDEXED.code | 
            ADD_ALL_INDEXED.code | 
            REMOVE_INDEXED.code | 
            SET_INDEXED.code;

        GROUP_NCOPIES = 
            ADD_NCOPIES.code | 
            REMOVE_NCOPIES.code;

        GROUP_ITERATED = 
            ADD_ITERATED.code | 
            REMOVE_ITERATED.code |
            SET_ITERATED.code;

        GROUP_NEXT = REMOVE_NEXT.code;

        GROUP_BULK =  
            ADD_ALL.code | 
            ADD_ALL_INDEXED.code | 
            REMOVE_ALL.code | 
            RETAIN_ALL.code | 
            CLEAR.code;

        GROUP_FROM_COLLECTION = 
            ADD.code | 
            ADD_ALL.code | 
            REMOVE.code | 
            REMOVE_ALL.code | 
            RETAIN_ALL.code | 
            CLEAR.code;

        GROUP_FROM_SET = GROUP_FROM_COLLECTION;
        GROUP_FROM_LIST = 
            GROUP_FROM_COLLECTION | 
            ADD_INDEXED.code | 
            ADD_ALL_INDEXED.code | 
            REMOVE_INDEXED.code | 
            SET_INDEXED.code;

        GROUP_FROM_BAG = 
            GROUP_FROM_COLLECTION | 
            ADD_NCOPIES.code | 
            REMOVE_NCOPIES.code;

        GROUP_FROM_BUFFER = 
            GROUP_FROM_COLLECTION | 
            REMOVE_NEXT.code ;

        GROUP_FROM_MAP = 
            PUT.code | 
            PUT_ALL.code | 
            REMOVE.code | 
            CLEAR.code ; 

        // aggregation of group codes.
        GROUP_REDUCE = GROUP_REMOVE | GROUP_CLEAR | GROUP_RETAIN;
        GROUP_STRUCTURE_MODIFIED = GROUP_ADD | GROUP_REDUCE;

	} 

    /**
     * Protected Constructor to make an extensible enumeration.
     */
    protected CollectionChangeType(int code, String desc) {
        this.code = code  ;
        this.desc = desc  ; 
        values.put(Integer.valueOf(code), this) ; 
    }

    public int hashCode() { 
        return code;  
    } 

    /** 
     * Returns the numeric code associated with this event type.
     * @return event code.
     */
    public int getCode() { 
        return code ; 
    } 


    /**
     * Returns a text description of the event.
     * @return text describing the event type
     */
    public String toString() {
        return desc ; 
    }

    /**
     * Checks this enumeration for membership within the specified group.
     * @param groupCode one of the static Group codes defined in this file.
     * @return true if this event type is a member of the specified group
     */
    public boolean isGroupMember(final int groupCode) { 
        return ( (groupCode & code) != 0 ) ; 
    } 

    /**
     * Looks up an event type given the integer code.
     * @param code the integer code to look up
     * @return the event type enumeration object associated with the provided
     *         code.
     */
    public static CollectionChangeType getEventByCode(final int code) { 
        return (CollectionChangeType)(values.get(Integer.valueOf(code))) ; 
    }
    
    /**
     * Gets a string version of a method event type.
     * 
     * @param methodType  the method event type constant
     * @return a string description
     */
    public static String toString(final int methodType) {
        CollectionChangeType val = getEventByCode(methodType) ; 
        if (val == null) { 
            return "Unknown" ; 
        } else { 
            return val.toString() ; 
        } 
    }
}
