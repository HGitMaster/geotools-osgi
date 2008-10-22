/**
 * <copyright>
 * </copyright>
 *
 * $Id: AxisTypeValidator.java 29859 2008-04-09 04:42:44Z jdeolive $
 */
package net.opengis.wcs11.validation;

import net.opengis.ows11.DomainMetadataType;

import net.opengis.wcs11.AvailableKeysType;

import org.eclipse.emf.common.util.EList;

/**
 * A sample validator interface for {@link net.opengis.wcs11.AxisType}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface AxisTypeValidator {
    boolean validate();

    boolean validateAvailableKeys(AvailableKeysType value);
    boolean validateMeaning(DomainMetadataType value);
    boolean validateDataType(DomainMetadataType value);
    boolean validateUOM(DomainMetadataType value);
    boolean validateReferenceSystem(DomainMetadataType value);
    boolean validateMetadata(EList value);
    boolean validateIdentifier(String value);
}
