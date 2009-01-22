// Spatial Index Library
//
// Copyright (C) 2002  Navel Ltd.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation;
// version 2.1 of the License.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
// Contact information:
//  Mailing address:
//    Marios Hadjieleftheriou
//    University of California, Riverside
//    Department of Computer Science
//    Surge Building, Room 310
//    Riverside, CA 92521
//
//  Email:
//    marioh@cs.ucr.edu
package org.geotools.caching.spatialindex;

import java.util.Properties;


/** 
 * A generic contract for spatial indexes, such as quadtrees or r-trees.
 * Provides methods to insert, delete and query the index.
 * Note that implementations may be n-dimensional.
 *
 * @author Marios Hadjieleftheriou, marioh@cs.ucr.edu
 * @copyright Copyright (C) 2002  Navel Ltd.
 * Modified by Christophe Rousson
 * Modified by Emily Gouge
 */
public interface SpatialIndex {
    public static final String INDEX_TYPE_PROPERTY = "SpatialIndex.Type";

    /**
     * This constant is used to check if two doubles are nearly equal.
     * Copied from original code by Marios Hadjieleftheriou.
     */
    public static final double EPSILON = 1.192092896e-07;

    /** Empty the index.
     * @throws IllegalStateException
     */
    public void clear() throws IllegalStateException;

    /** Insert new data in the index.
     *
     * @param data to insert
     * @param a n-dims shape
     */
    public void insertData(final Object data, final Shape shape);

    /** Delete data both identified by its shape 
     *
     * @param data to find and delete
     * @param shape
     * @return <code>true</code> if data has been found and deleted
     */
    public boolean deleteData(final Object data, final Shape shape);

    /** Traverse index to match data such as :
     *  <code>query.contains(Data.getShape())</code>
     *
     * @param query, a n-dims shape
     * @param visitor implementing visit() callback method
     */
    public void containmentQuery(final Shape query, final Visitor v);

    /** Traverse index to match data such as :
     *  <code>query.intersects(Data.getShape())</code>
     *
     * @param query, a n-dims shape
     * @param visitor implementing visit() callback method
     */
    public void intersectionQuery(final Shape query, final Visitor v);

    /** Traverse index to match data having query falling inside its shape, ie :
     * <code>Data.getShape().contains(query)</code>
     *
     * @param query, a n-dims point
     * @param visitor implementing visit() callback method
     */
    public void pointLocationQuery(final Point query, final Visitor v);

    /**
     * @param k
     * @param query
     * @param v
     * @param nnc
     */
    public void nearestNeighborQuery(int k, final Shape query, final Visitor v,
        NearestNeighborComparator nnc);

    /**
     * @param k
     * @param query
     * @param v
     */
    public void nearestNeighborQuery(int k, final Shape query, final Visitor v);

    /** Provides an alternative way to query the index,
     * and to run customized and optimized query of the index.
     * For example, this is useful for traversing a tree by level,
     * or to get specific information such as the MBR of root node.
     *
     * @param qs
     */
    public void queryStrategy(final QueryStrategy qs);

    /**
     * @return
     */
    public Properties getIndexProperties();

    /** Add a command to be executed before nodes are written.
     *
     * @param nc
     */
    public void addWriteNodeCommand(NodeCommand nc);

    /** Add a command to be executed before nodes are read.
     *
     * @param nc
     */
    public void addReadNodeCommand(NodeCommand nc);

    /** Add a command to be executed before nodes are deleted.
     *
     * @param nc
     */
    public void addDeleteNodeCommand(NodeCommand nc);

    /** Implementations may always return true.
     *
     * @return true if index is valid.
     *
     * TODO: define what is a valid index.
     */
    public boolean isIndexValid();

    /**
     * @return statistics about the index.
     */
    public Statistics getStatistics();

    /** Cause pending write operations to happen immediately.
     * Use this method to persist the index before disposal.
     *
     */
    public void flush();
    
    public void initializeFromStorage(Storage storage);
}
