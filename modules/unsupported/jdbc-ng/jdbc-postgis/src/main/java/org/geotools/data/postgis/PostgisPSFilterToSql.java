package org.geotools.data.postgis;

import org.geotools.filter.FilterCapabilities;
import org.geotools.jdbc.PreparedFilterToSQL;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.spatial.BinarySpatialOperator;

public class PostgisPSFilterToSql extends PreparedFilterToSQL {
    
    FilterToSqlHelper helper;
    
    public PostgisPSFilterToSql(PostGISPSDialect dialect) {
        super(dialect);
        helper = new FilterToSqlHelper(this);
    }

    public boolean isLooseBBOXEnabled() {
        return helper.looseBBOXEnabled;
    }

    public void setLooseBBOXEnabled(boolean looseBBOXEnabled) {
        helper.looseBBOXEnabled = looseBBOXEnabled;
    }

    @Override
    protected FilterCapabilities createFilterCapabilities() {
        return helper.createFilterCapabilities();
    }

    @Override
    protected Object visitBinarySpatialOperator(BinarySpatialOperator filter,
            PropertyName property, Literal geometry, boolean swapped,
            Object extraData) {
        helper.out = out;
        return helper.visitBinarySpatialOperator(filter, property, geometry, swapped, extraData);
    }

}
