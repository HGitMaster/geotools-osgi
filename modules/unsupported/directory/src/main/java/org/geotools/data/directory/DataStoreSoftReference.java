package org.geotools.data.directory;

import java.lang.ref.SoftReference;

import org.geotools.data.DataStore;

public class DataStoreSoftReference extends SoftReference<DataStore> {

    public DataStoreSoftReference(DataStore referent) {
        super(referent, ReferenceCleaner.DEFAULT.referenceQueue);
    }
    
    @Override
    public void clear() {
        DataStore store = get();
        if(store != null)
            store.dispose();
        super.clear();
    }

}
