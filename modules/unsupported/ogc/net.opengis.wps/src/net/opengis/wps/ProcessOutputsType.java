/**
 * <copyright>
 * </copyright>
 *
 * $Id: ProcessOutputsType.java 30488 2008-06-02 23:31:41Z gdavis $
 */
package net.opengis.wps;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Process Outputs Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link net.opengis.wps.ProcessOutputsType#getOutput <em>Output</em>}</li>
 * </ul>
 * </p>
 *
 * @see net.opengis.wps.WpsPackage#getProcessOutputsType()
 * @model extendedMetaData="name='ProcessOutputs_._type' kind='elementOnly'"
 * @generated
 */
public interface ProcessOutputsType extends EObject {
	/**
	 * Returns the value of the '<em><b>Output</b></em>' containment reference list.
	 * The list contents are of type {@link net.opengis.wps.OutputDescriptionType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Unordered list of one or more descriptions of all the outputs that can result from executing this process. At least one output is required from each process.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Output</em>' containment reference list.
	 * @see net.opengis.wps.WpsPackage#getProcessOutputsType_Output()
	 * @model type="net.opengis.wps.OutputDescriptionType" containment="true" required="true"
	 *        extendedMetaData="kind='element' name='Output'"
	 * @generated
	 */
	EList getOutput();

} // ProcessOutputsType
