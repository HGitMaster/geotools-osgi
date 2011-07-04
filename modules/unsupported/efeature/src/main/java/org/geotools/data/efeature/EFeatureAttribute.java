/**
 * <copyright>
 * </copyright>
 *
 * $Id: EFeatureAttribute.java 37552 2011-07-03 18:08:06Z kengu $
 */
package org.geotools.data.efeature;

import org.opengis.feature.Attribute;
import org.opengis.feature.Property;

/**
 * Generic interface for accessing {@link Attribute feature attribute} data.
 * 
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link EFeatureAttribute#getStructure <em>Structure</em>}</li>
 * </ul>
 * </p>
 * This class extends {@link EFeatureProperty}. </p>
 * 
 * @param <V> - Actual {@link Property#getValue() property value} class.
 * 
 * @see EFeatureProperty
 * @see EFeaturePackage#getEFeatureProperty()
 * @see EFeaturePackage#getEFeatureAttribute()
 * 
 * @author kengu
 */
public interface EFeatureAttribute<V> extends EFeatureProperty<V, Attribute> {

    /**
     * Get the attribute {@link EFeatureAttributeInfo structure} instance.
     * 
     * @return the value of the '<em>Structure</em>' attribute.
     */
    @Override
    public EFeatureAttributeInfo getStructure();

} // EFeatureAttribute
