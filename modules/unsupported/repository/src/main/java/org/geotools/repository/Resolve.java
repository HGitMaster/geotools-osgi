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
import java.net.URI;
import java.util.List;

import org.geotools.util.ProgressListener;


/**
 * Interface for objects which serve has handles to actual data objects.
 *
 * <p>
 * The resolve pattern is based on the IAdaptable pattern used extensivly by
 * the Eclipse framework. Also known as the Extensible Interface pattern,
 * objects implementing the IAdaptable interface morph or adapt themselves
 * into objects implementing a different interface.
 * </p>
 *
 * <p>
 * The resolve pattern is slightly different in that morphing or adapting  (ie.
 * resolving) into a different object involves a blocking call in which I/O
 * is being performed, possibly with the local disk, or with a remote service.
 * </p>
 *
 * <p>
 * The following code illustrates the use of the resolve pattern:
 * <pre>
 *         <code>
 *         Resolve resolve = ....
 *         ProgressListener listener = ....
 *
 *         FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = resolve.resolve(FeatureSource.class,listener);
 *         if (featureSource != null) {
 *                 //do something
 *         }
 *         </code>
 * </pre>
 * As a convenience, the {@link Resolve#canResolve(Class)} method is used to
 * determine if a particular type of object is supported, but not to perform
 * the resolve. This method can be useful in situations where it is not
 * desirable to block.
 * </p>
 *
 * <p>
 * An implementation of resolve supports the notion of resolving into a parent,
 * or into a list of children, called members. Like any other resolve, these
 * are  blocking operations. Parents and members must also implement the
 * Resolve  interface.
 * </p>
 *
 * @author David Zwiers, Refractions Research
 * @author Justin Deoliveira, The Open Planning Project
 *
 * @since 0.7.0
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/unsupported/repository/src/main/java/org/geotools/repository/Resolve.java $
 */
public interface Resolve {
    /**
     * Blocking method which is used to resolve into an instance of a
     * particular class.
     *
     * <p>
     * Required adaptions will be listed in Abstract Classes that implement
     * this interface.
     * </p>
     *
     * @param adaptee Class of object to resolve into.
     * @param monitor Progress monitor used to report status while blocking.
     *        May  be null.
     *
     * @return Instance of type adaptee, or null if the resolve is unsuported.
     *
     * @throws IOException in the result of an I/O error.
     */
    Object resolve(Class adaptee, ProgressListener monitor)
        throws IOException;

    /**
     * Non blocking method which is used to determine if a resolve into an
     * instance of a particular class is supported.
     *
     * @param adaptee Class of object to resolve into.
     *
     * @return true if a resolution for adaptee is avaialble
     *
     * @see IResolve#resolve(Class,ProgressListener)
     */
    boolean canResolve(Class adaptee);

    /**
     * Blocking method which resolves this instance into its parent. This
     * method may return null if the parent can not be determined.
     *
     * @param monitor Progress monitor used to report status while blocking.
     *        May  be null.
     *
     * @return The parent Resolve, or null if the parent can be obtained.
     *
     * @throws IOException in the result of an I/O error.
     */
    Resolve parent(ProgressListener monitor) throws IOException;

    /**
     * Blocking method which resolves this instance into its members
     * (children). This method returns null if the instance does not have any
     * children, or  the children could be determined.
     *
     * @param monitor Progress monitor used to report status while blocking.
     *        May  be null.
     *
     * @return A list (possibly empty) of members, null if the members could
     *         not be obtained or the instance has not members. Objects in the
     *         returned list implement the Resolve interface.
     *
     * @throws IOException in the result of an I/O error.
     */
    List members(ProgressListener monitor) throws IOException;

    /**
     * Status of the resolve. Resolving into other types of objects often
     * involves connecting to a remote service or resource. This method is
     * provided to indicate the state of any connections.
     *
     * @return One of {@link Status#BROKEN},{@link Status#CONNECTED}, or
     *         {@link Status#NOTCONNECTED}.
     */
    Status getStatus();

    /**
     * In the event that an error occurs during a resolve, that error can be
     * reported back with this method. This method returns a value when
     * {@link Resolve#getStatus()} returns {@link Status#BROKEN}, otherwise it
     * return null.
     *
     * @return An exception that occured during a resolve, otherwise null.
     *
     * @see Status
     */
    Throwable getMessage();

    /**
     * Returns a URI which uniqley identifies the Resolve.
     *
     * @return Id of the Resolve, should never be null.
     */
    URI getIdentifier();

    /**
     * Adds a listener to the Resolve. Support for event notification is up to
     * the specific implementation.
     *
     * @param listener The observer.
     *
     * @throws UnsupportedOperationException When event notification is not
     *         supported.
     */
    void addListener(ResolveChangeListener listener) throws UnsupportedOperationException;

    /**
     * Removes a listener from the Resolve. Support for event notification is
     * up  to the specific implementation.
     *
     * @param listener The observer.
     */
    void removeListener(ResolveChangeListener listener);

    /**
     * Fires a change event against the Resolve. Support for event notification
     * is up to the specific implementation.
     *
     * @param event The event describing the change.
     */
    void fire(ResolveChangeEvent event);

    /**
     * Enumeration class for representing the status or state of a Resolve.
     */
    class Status {
        /** Status constant indicates a live connection in use */
        public static final Status CONNECTED = new Status();

        /** Status constant indicates a connection that is not in use */
        public static final Status NOTCONNECTED = new Status();

        /** Status constant indicates a connection that is broken */
        public static final Status BROKEN = new Status();

        private Status() {
        }
    }
}
