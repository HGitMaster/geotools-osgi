/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data;


/**
 * Used to lock features when used with LockingDataSource.
 *
 * <p>
 * A FeatureLockFactory is used to generate FeatureLocks.
 * </p>
 *
 * <p>
 * A FeatureLock representing the Current Transaction has been provided.
 * </p>
 * <h2>
 *
 * <p>
 * Jody - I have a slightly more consistent specification of this idea in
 * GeoAPI that should be ported back over.
 * </p>
 *
 * @author Jody Garnett, Refractions Research, Inc.
 * @source $URL: http://gtsvn.refractions.net/trunk/modules/library/api/src/main/java/org/geotools/data/FeatureLock.java $
 * @version $Id: FeatureLock.java 30642 2008-06-12 17:52:06Z acuster $
 *
 * @see <a
 *      href="http://vwfs.refractions.net/docs/Database_Research.pdf">Database
 *      Reseach</a>
 * @see <a
 *      href="http://vwfs.refractions.net/docs/Transactional_WFS_Design.pdf">Transactional
 *      WFS Design</a>
 * @see <a
 *      href="http://vwfs.refractions.net/docs/Design_Implications.pdf">Design
 *      Implications</a>
 * @see FeatureLockFactory
 */
public interface FeatureLock {
    /**
     * FeatureLock representing Transaction duration locking
     *
     * <p>
     * When this FeatureLock is used locks are expected to last until the
     * current Transasction ends with a commit() or rollback().
     * </p>
     */
    public static final FeatureLock TRANSACTION = new CurrentTransactionLock();

    /**
     * LockId used for transaction authorization.
     *
     * @return A string of the LockId.
     */
    String getAuthorization();

    /**
     * Time from now the lock will expire
     *
     * @return A long of the time till the lock expires.
     */
    long getDuration();
}
