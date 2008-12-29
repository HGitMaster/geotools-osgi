package org.geotools.data.postgis;

import java.io.IOException;

import org.geotools.filter.FilterCapabilities;
import org.geotools.jdbc.PreparedFilterToSQL;
import org.geotools.jdbc.SQLDialect;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.BBOX;
import org.opengis.filter.spatial.Beyond;
import org.opengis.filter.spatial.BinarySpatialOperator;
import org.opengis.filter.spatial.Contains;
import org.opengis.filter.spatial.Crosses;
import org.opengis.filter.spatial.DWithin;
import org.opengis.filter.spatial.Disjoint;
import org.opengis.filter.spatial.DistanceBufferOperator;
import org.opengis.filter.spatial.Equals;
import org.opengis.filter.spatial.Intersects;
import org.opengis.filter.spatial.Overlaps;
import org.opengis.filter.spatial.Touches;
import org.opengis.filter.spatial.Within;

public class PostgisFilterToSql extends PreparedFilterToSQL {
    
    public PostgisFilterToSql(PostGISDialect dialect) {
        super(dialect);
    }

    boolean looseBBOXEnabled;

    public boolean isLooseBBOXEnabled() {
        return looseBBOXEnabled;
    }

    public void setLooseBBOXEnabled(boolean looseBBOXEnabled) {
        this.looseBBOXEnabled = looseBBOXEnabled;
    }

    @Override
    protected FilterCapabilities createFilterCapabilities() {
        FilterCapabilities caps = new FilterCapabilities();
        caps.addAll(SQLDialect.BASE_DBMS_CAPABILITIES);

        // adding the spatial filters support
        caps.addType(BBOX.class);
        caps.addType(Contains.class);
        caps.addType(Crosses.class);
        caps.addType(Disjoint.class);
        caps.addType(Equals.class);
        caps.addType(Intersects.class);
        caps.addType(Overlaps.class);
        caps.addType(Touches.class);
        caps.addType(Within.class);
        caps.addType(DWithin.class);
        caps.addType(Beyond.class);

        return caps;
    }

    @Override
    protected Object visitBinarySpatialOperator(BinarySpatialOperator filter,
            PropertyName property, Literal geometry, boolean swapped,
            Object extraData) {
        try {
            if (filter instanceof DistanceBufferOperator) {
                visitDistanceSpatialOperator((DistanceBufferOperator) filter,
                        property, geometry, swapped, extraData);
            } else {
                visitComparisonSpatialOperator(filter, property, geometry,
                        swapped, extraData);
            }
        } catch (IOException e) {
            throw new RuntimeException(IO_ERROR, e);
        }
        return extraData;
    }

    private void visitDistanceSpatialOperator(DistanceBufferOperator filter,
            PropertyName property, Literal geometry, boolean swapped,
            Object extraData) throws IOException {
        if ((filter instanceof DWithin && !swapped)
                || (filter instanceof Beyond && swapped)) {
            out.write("ST_DWITHIN(");
            property.accept(this, extraData);
            out.write(",");
            geometry.accept(this, extraData);
            out.write(",");
            out.write(Double.toString(filter.getDistance()));
            out.write(")");
        }
        if ((filter instanceof DWithin && swapped)
                || (filter instanceof Beyond && !swapped)) {
            out.write("ST_DISTANCE(");
            property.accept(this, extraData);
            out.write(",");
            geometry.accept(this, extraData);
            out.write(") > ");
            out.write(Double.toString(filter.getDistance()));
        }
    }

    void visitComparisonSpatialOperator(BinarySpatialOperator filter,
            PropertyName property, Literal geometry, boolean swapped, Object extraData)
            throws IOException {
        
        if(!(filter instanceof Disjoint)) {
            property.accept(this, extraData);
            out.write(" && ");
            geometry.accept(this, extraData);
    
            // if we're just encoding a bbox in loose mode, we're done 
            if(filter instanceof BBOX && looseBBOXEnabled)
                return;
                
            out.write(" AND ");
        }

        String closingParenthesis = ")";
        if (filter instanceof Equals) {
            out.write("equals");
        } else if (filter instanceof Disjoint) {
            out.write("NOT (intersects");
            closingParenthesis += ")";
        } else if (filter instanceof Intersects || filter instanceof BBOX) {
            out.write("intersects");
        } else if (filter instanceof Crosses) {
            out.write("crosses");
        } else if (filter instanceof Within) {
            if(swapped)
                out.write("contains");
            else
                out.write("within");
        } else if (filter instanceof Contains) {
            if(swapped)
                out.write("within");
            else
                out.write("contains");
        } else if (filter instanceof Overlaps) {
            out.write("overlaps");
        } else if (filter instanceof Touches) {
            out.write("touches");
        } else {
            throw new RuntimeException("Unsupported filter type " + filter.getClass());
        }
        out.write("(");

        property.accept(this, extraData);
        out.write(", ");
        geometry.accept(this, extraData);

        out.write(closingParenthesis);

    }

}
