/**
 * <copyright>
 * </copyright>
 *
 * $Id: ServiceIdentificationTypeValidator.java 29859 2008-04-09 04:42:44Z jdeolive $
 */
package net.opengis.ows11.validation;

import net.opengis.ows11.CodeType;

import org.eclipse.emf.common.util.EList;

/**
 * A sample validator interface for {@link net.opengis.ows11.ServiceIdentificationType}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface ServiceIdentificationTypeValidator {
    boolean validate();

    boolean validateServiceType(CodeType value);
    boolean validateServiceTypeVersion(EList value);
    boolean validateProfile(EList value);
    boolean validateFees(String value);
    boolean validateAccessConstraints(EList value);
}