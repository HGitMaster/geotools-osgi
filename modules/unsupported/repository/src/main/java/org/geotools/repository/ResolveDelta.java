/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2005-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.repository;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;


/**
 * Constants used to communicate Catalog Deltas.
 * <p>
 * For those familiar with IResourceChangeEvent and IResourceDelta from eclipse development there is
 * one <b>important addition</b>. The constant REPLACE indicates a reaname, or substiution, you
 * will need to replace any references you have to the oldObject with the newObject.
 * </p>
 * <p>
 * For "bit mask" style interation please use: <code>EnumSet.of(Kind.ADDED, Kind.REPLACED)</code>
 * </p>
 *
 * @author Jody Garnett
 * @author Justin Deoliveira, The Open Planning Project
 * @since 0.6.0
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/repository/src/main/java/org/geotools/repository/ResolveDelta.java $
 */
public interface ResolveDelta {
    /** List indicating no children are present */
    static final List NO_CHILDREN = Collections.EMPTY_LIST;

    /**
     * Returns the kind of this delta.
     * <p>
     * Normally, one of <code>ADDED</code>, <code>REMOVED</code>, <code>CHANGED</code> or
     * <code>REPLACED</code>.
     * </p>
     * <p>
     * This set is still open, during shutdown we may throw a few more kinds around. Eclipse makes
     * use of PHANTOM, and NON_PHANTOM not sure we care
     * </p>
     *
     * @return the kind of this resource delta
     * @see Kind.ADDED
     * @see Kind.REMOVED
     * @see Kind.CHANGED
     * @see Kind.REPLACED
     */
    Kind getKind();

    /**
     * Accepts the given visitor.
     * <p>
     * The only kinds of resource delta that our visited are ADDED, REMOVED, CHANGED and REPLACED.
     * </p>
     * <p>
     * This is a convenience method, equivalent to accepts( visitor, IService.NONE )
     * </p>
     *
     * @param visitor
     * @throws CoreException
     */
    void accept(ResolveDeltaVisitor visitor) throws IOException;

    /**
     * Resource deltas for all added, removed, changed, or replaced.
     * <p>
     * This is a short cut for:
     *
     * <pre><code>
     *  finally List list = new ArrayList();
     *  accept( IServiceDeltaVisitor() {
     *  public boolean visit(IResolveDelta delta) {
     *          switch (delta.getKind()) {
     *          case IDelta.ADDED :
     *          case IDelta.REMOVED :
     *          case IDelta.CHANGED :
     *          case IDelta.REPLACED :
     *              list.add( delta );
     *          default: // ignore
     *          }
     *      return true;
     *      }
     *  });
     *  return list.toArray();
     * </code></pre>
     *
     * </p>
     *
     * @return A list of type ResolveDelta.
     */
    List getChildren();

    /**
     * Finds and returns the delta information for a given resource.
     *
     * @param kindMask Set of IDelta.Kind
     * @return List of IGeoResourceDelta
     */
    List getChildren(Set kindMask);

    /**
     * Returns a handle for the affected handle.
     * <p>
     * For additions (<code>ADDED</code>), this handle describes the newly-added resolve; i.e.,
     * the one in the "after" state.
     * <p>
     * For changes (<code>CHANGED</code>), this handle also describes the resource in the
     * "after" state.
     * <p>
     * For removals (<code>REMOVED</code>), this handle describes the resource in the "before"
     * state. Even though this handle not normally exist in the current workspace, the type of
     * resource that was removed can be determined from the handle.
     * <p>
     * For removals (<code>REPLACE</code>), this handle describes the resource in the "before"
     * state. The new handle can be determined with getNewResolve().
     * <p>
     *
     * @return the affected resource (handle)
     */
    Resolve getResolve();

    /**
     * For replacement (<code>REPLACE</code>), this handle describes the resource in the "after"
     * state. The old handle can be determined with getResolve().
     * <p>
     *
     * @return The new resolve replacing the affected handle.
     */
    Resolve getNewResolve();

    /**
     * Kind of Delta, used to indicate change.
     *
     * @author jgarnett
     * @since 0.9.0
     */
    class Kind {
        /**
         * Delta kind constant indicating no change.
         *
         * @see #getKind()
         */
        public static final Kind NO_CHANGE = new Kind();

        /**
         * The resource has been added to the catalog.
         *
         * @see #getKind()
         */
        public static final Kind ADDED = new Kind();

        /**
         * The resource has been removed from the catalog.
         *
         * @see #getKind()
         */
        public static final Kind REMOVED = new Kind();

        /**
         * The resource has been changed.
         *
         * @see #getKind()
         */
        public static final Kind CHANGED = new Kind();

        /**
         * The resource has been replaced with another entry in the catalog.
         *
         * @see #getKind()
         */
        public static final Kind REPLACED = new Kind();
    }
}
