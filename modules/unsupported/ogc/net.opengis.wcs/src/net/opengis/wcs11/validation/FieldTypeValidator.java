/**
 * <copyright>
 * </copyright>
 *
 * $Id: FieldTypeValidator.java 29859 2008-04-09 04:42:44Z jdeolive $
 */
package net.opengis.wcs11.validation;

import net.opengis.ows11.UnNamedDomainType;

import net.opengis.wcs11.InterpolationMethodsType;

import org.eclipse.emf.common.util.EList;

/**
 * A sample validator interface for {@link net.opengis.wcs11.FieldType}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface FieldTypeValidator {
    boolean validate();

    boolean validateIdentifier(String value);
    boolean validateDefinition(UnNamedDomainType value);
    boolean validateNullValue(EList value);
    boolean validateInterpolationMethods(InterpolationMethodsType value);
    boolean validateAxis(EList value);
}
