package org.geotools.jdbc;

import org.geotools.data.QueryCapabilities;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.expression.PropertyName;
import org.opengis.filter.sort.SortBy;
import org.opengis.filter.sort.SortOrder;

import com.vividsolutions.jts.geom.Geometry;

/**
 * A default QueryCapabilities implementation for JDBCFeatureSource.
 * 
 * @author Gabriel Roldan (TOPP)
 * @author Andrea Aime (OpenGeo)
 * @version $Id: JDBCQueryCapabilities.java 32368 2009-01-29 10:45:55Z aaime $
 * @since 2.5.4
 */
class JDBCQueryCapabilities extends QueryCapabilities {

    JDBCFeatureSource source;

    public JDBCQueryCapabilities(JDBCFeatureSource source) {
        this.source = source;
    }

    /**
     * Overrides to delegate to the three template methods in order to check for sorting
     * capabilities over the natural and reverse order, and each specific attribute type.
     */
    @Override
    public boolean supportsSorting(final SortBy[] sortAttributes) {
        if(super.supportsSorting(sortAttributes))
            return true;
        
        for (int i = 0; i < sortAttributes.length; i++) {
            SortBy sortBy = sortAttributes[i];
            if (SortBy.NATURAL_ORDER == sortBy) {
                if(!supportsNaturalOrderSorting()){
                    return false;
                }
            }else if (SortBy.REVERSE_ORDER == sortBy) {
                if(!supportsReverseOrderSorting()){
                    return false;
                }
            }else{
                PropertyName propertyName = sortBy.getPropertyName();
                SortOrder sortOrder = sortBy.getSortOrder();
                if (!supportsPropertySorting(propertyName, sortOrder)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Indicates whether sorting by {@link SortBy#NATURAL_ORDER} is supported; defaults to
     * <code>false</code>.
     * 
     * @return false, override if NATURAL_ORDER sorting is supported.
     */
    protected boolean supportsNaturalOrderSorting() {
        return false;
    }

    /**
     * Indicates whether sorting by {@link SortBy#REVERSE_ORDER} is supported; defaults to
     * <code>false</code>.
     * 
     * @return false, override if REVERSE_ORDER sorting is supported.
     */
    protected boolean supportsReverseOrderSorting() {
        return false;
    }

    /**
     * Template method to check for sorting support in the given sort order for a specific
     * attribute type, given by a PropertyName expression.
     * <p>
     * This default implementation assumes both orders are supported as long as the property
     * name corresponds to the name of one of the attribute types in the complete FeatureType,
     * and that the attribute is not a geometry.
     * </p>
     * 
     * @param propertyName the expression holding the property name to check for sortability
     *            support
     * @param sortOrder the order, ascending or descending, to check for sortability support
     *            over the given property name.
     * @return true if propertyName refers to one of the FeatureType attributes
     */
    protected boolean supportsPropertySorting(PropertyName propertyName, SortOrder sortOrder) {
        AttributeDescriptor descriptor = (AttributeDescriptor) propertyName.evaluate(source.getSchema());
        if(descriptor == null) {
            String attName = propertyName.getPropertyName();
            descriptor = source.getSchema().getDescriptor(attName);
        }
        return descriptor != null && !(Geometry.class.isAssignableFrom(descriptor.getType().getBinding()));
    }
    
    /**
     * Consults the fid mapper for the feature source, if the null feature map reliable fids
     * not supported. 
     */
    @Override
    public boolean isReliableFIDSupported() {
        return !(source.getPrimaryKey() instanceof NullPrimaryKey);
    }
}
