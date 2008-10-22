/**
 * <copyright>
 * </copyright>
 *
 * $Id: OutputDefinitionsType.java 30488 2008-06-02 23:31:41Z gdavis $
 */
package net.opengis.wps;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Output Definitions Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Definition of a format, encoding,  schema, and unit-of-measure for an output to be returned from a process.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link net.opengis.wps.OutputDefinitionsType#getOutput <em>Output</em>}</li>
 * </ul>
 * </p>
 *
 * @see net.opengis.wps.WpsPackage#getOutputDefinitionsType()
 * @model extendedMetaData="name='OutputDefinitionsType' kind='elementOnly'"
 * @generated
 */
public interface OutputDefinitionsType extends EObject {
	/**
	 * Returns the value of the '<em><b>Output</b></em>' containment reference list.
	 * The list contents are of type {@link net.opengis.wps.DocumentOutputDefinitionType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Output definition as provided in the execute request
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Output</em>' containment reference list.
	 * @see net.opengis.wps.WpsPackage#getOutputDefinitionsType_Output()
	 * @model type="net.opengis.wps.DocumentOutputDefinitionType" containment="true" required="true"
	 *        extendedMetaData="kind='element' name='Output' namespace='##targetNamespace'"
	 * @generated
	 */
	EList getOutput();

} // OutputDefinitionsType
