/**
 * <copyright>
 * </copyright>
 *
 * $Id: RangeTypeValidator.java 29859 2008-04-09 04:42:44Z jdeolive $
 */
package net.opengis.ows11.validation;

import net.opengis.ows11.RangeClosureType;
import net.opengis.ows11.ValueType;

/**
 * A sample validator interface for {@link net.opengis.ows11.RangeType}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface RangeTypeValidator {
    boolean validate();

    boolean validateMinimumValue(ValueType value);
    boolean validateMaximumValue(ValueType value);
    boolean validateSpacing(ValueType value);
    boolean validateRangeClosure(RangeClosureType value);
}
