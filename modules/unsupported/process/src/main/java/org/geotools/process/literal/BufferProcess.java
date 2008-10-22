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
package org.geotools.process.literal;

import java.util.HashMap;
import java.util.Map;

import org.geotools.process.ProcessFactory;
import org.geotools.process.impl.AbstractProcess;
import org.geotools.text.Text;
import org.geotools.util.NullProgressListener;
import org.opengis.util.ProgressListener;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Process for adding a buffer around a geometry
 * 
 * @author gdavis
 */
class BufferProcess extends AbstractProcess {
    private boolean started = false;
    
    public BufferProcess( BufferFactory bufferFactory ) {
        super( bufferFactory );
    }

    public ProcessFactory getFactory() {
        return factory;
    }

	public Map<String,Object> execute(Map<String,Object> input, ProgressListener monitor){
		if (started) throw new IllegalStateException("Process can only be run once");
		started = true;
		
		if( monitor == null ) monitor = new NullProgressListener();		
        try {
            monitor.started();
            monitor.setTask( Text.text("Grabbing arguments") );
            monitor.progress( 10.0f );
            Geometry geom1 = (Geometry) input.get( BufferFactory.GEOM1.key );          
            Double buffer = (Double) input.get( BufferFactory.BUFFER.key );
            
            monitor.setTask( Text.text("Processing Buffer") );
            monitor.progress( 25.0f );
            
            if( monitor.isCanceled() ){
                return null; // user has canceled this operation
            }
            
            Geometry resultGeom = geom1.buffer(buffer);
            
            monitor.setTask( Text.text("Encoding result" ));
            monitor.progress( 90.0f );
            
            Map<String,Object> result = new HashMap<String, Object>();
            result.put( BufferFactory.RESULT.key, resultGeom );
            monitor.complete(); // same as 100.0f
            
            return result;
        }
        catch (Exception eek){
            monitor.exceptionOccurred(eek);
            return null;
        }
        finally {
            monitor.dispose();
        }		
	}
}
