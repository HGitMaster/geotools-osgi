/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.gpx;

class MemoryLock {

    private int readCount = 0;
    private boolean writeLocked = false;
    
    /**
     * The caller gets a read lock, whenever there is no open write lock.
     *
     */
    synchronized void acquireReadLock() {
        while(writeLocked) {
            try {
                GpxDataStore.LOGGER.fine("acquireReadLock: waiting for write lock. Thread: " + Thread.currentThread().getName());
                wait();
            } catch (InterruptedException e) {
            }
        }
        readCount++;
        GpxDataStore.LOGGER.fine("acquireReadLock: read lock aquired");
    }
    
    synchronized void releaseReadLock() {
        
        if(writeLocked || readCount == 0) {
            GpxDataStore.LOGGER.warning("releaseReadLock: inconsistent locks"); 
        }
        
        readCount--;
        notify();
        GpxDataStore.LOGGER.fine("releaseReadLock: read lock released");
    }
    
    /**
     * The caller gets a write lock, if there is no open read or write lock.
     *
     */
    synchronized void acquireWriteLock() {
        while(writeLocked || readCount != 0) {
            try {
                GpxDataStore.LOGGER.fine("acquireWriteLock: waiting for read or write lock. Thread: " + Thread.currentThread().getName());
                wait();
            } catch (InterruptedException e) {
            }
        }
        writeLocked = true;
        GpxDataStore.LOGGER.fine("acquireWriteLock: write lock aquired");
    }
    
    synchronized void releaseWriteLock() {

        if(!writeLocked || readCount != 0) {
            GpxDataStore.LOGGER.warning("releaseWriteLock: inconsistent locks"); 
        }
        
        writeLocked = false;
        notify();
        GpxDataStore.LOGGER.fine("releaseWriteLock: write lock released");
    }
    
}
