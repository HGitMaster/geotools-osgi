/**
 * <copyright>
 * </copyright>
 *
 * $Id: SpatialDomainTypeValidator.java 29859 2008-04-09 04:42:44Z jdeolive $
 */
package net.opengis.wcs11.validation;

import net.opengis.wcs11.GridCrsType;
import net.opengis.wcs11.ImageCRSRefType;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.util.FeatureMap;

/**
 * A sample validator interface for {@link net.opengis.wcs11.SpatialDomainType}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface SpatialDomainTypeValidator {
    boolean validate();

    boolean validateBoundingBoxGroup(FeatureMap value);
    boolean validateBoundingBox(EList value);
    boolean validateGridCRS(GridCrsType value);
    boolean validateTransformation(Object value);
    boolean validateImageCRS(ImageCRSRefType value);
    boolean validatePolygon(EList value);
}
