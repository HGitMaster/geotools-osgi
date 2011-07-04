package org.geotools.data.csv;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureReader;
import org.geotools.data.store.ContentState;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.csvreader.CsvReader;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;

public class CSVFeatureReader implements FeatureReader<SimpleFeatureType, SimpleFeature> {

    protected ContentState state;
    protected CsvReader reader;
    private SimpleFeature next;
    protected SimpleFeatureBuilder builder;
    private int row;
    private GeometryFactory geometryFactory;

    public CSVFeatureReader(ContentState contentState) throws IOException {
        this.state = contentState;
        CSVDataStore csv = (CSVDataStore) contentState.getEntry().getDataStore();
        reader = csv.read(); // this may throw an IOException if it could not connect
        boolean header = reader.readHeaders();
        if (! header ){
            throw new IOException("Unable to read csv header");
        }
        builder = new SimpleFeatureBuilder( state.getFeatureType() );
        geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        row = 0;
    }

    public SimpleFeatureType getFeatureType() {
        return (SimpleFeatureType) state.getFeatureType();
    }

    public SimpleFeature next() throws IOException, IllegalArgumentException,
            NoSuchElementException {
        SimpleFeature feature;
        if( next != null ){
            feature = next;
            next = null;
        }
        else {
            feature = readFeature();
        }
        return feature;
    }
    
    SimpleFeature readFeature() throws IOException {
        if( reader == null ){
            throw new IOException("FeatureReader is closed; no additional features can be read");
        }
        boolean read = reader.readRecord(); // read the "next" record
        if( read == false ){
            close(); // automatic close to be nice
            return null; // no additional features are available
        }
        Coordinate coordinate = new Coordinate();
        for( String column : reader.getHeaders() ){
            String value = reader.get(column);
            if( "lat".equalsIgnoreCase(column)){
                coordinate.y = Double.valueOf( value.trim() );
            }
            else if( "lon".equalsIgnoreCase(column)){
                coordinate.x = Double.valueOf( value.trim() );
            }
            else {
                builder.set(column, value );
            }
        }
        builder.set("Location", geometryFactory.createPoint( coordinate ) );
        
        return this.buildFeature();
    }
    
    protected SimpleFeature buildFeature() {
        row += 1;
        return builder.buildFeature( state.getEntry().getTypeName()+"."+row );
    }

    public boolean hasNext() throws IOException {
        if( next != null ){
            return true;
        }
        else {
            next = readFeature(); // read next feature so we can check
            return next != null;
        }
    }

    public void close() throws IOException {
        if( reader != null ){
            reader.close();
            reader = null;
        }
        builder = null;
        geometryFactory = null;
        next = null;
    }

}
