/**
 * <copyright>
 * </copyright>
 *
 * $Id: ProcessOutputsType1.java 30488 2008-06-02 23:31:41Z gdavis $
 */
package net.opengis.wps;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Process Outputs Type1</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link net.opengis.wps.ProcessOutputsType1#getOutput <em>Output</em>}</li>
 * </ul>
 * </p>
 *
 * @see net.opengis.wps.WpsPackage#getProcessOutputsType1()
 * @model extendedMetaData="name='ProcessOutputs_._1_._type' kind='elementOnly'"
 * @generated
 */
public interface ProcessOutputsType1 extends EObject {
	/**
	 * Returns the value of the '<em><b>Output</b></em>' containment reference list.
	 * The list contents are of type {@link net.opengis.wps.OutputDataType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Unordered list of values of all the outputs produced by this process. It is not necessary to include an output until the Status is ProcessSucceeded.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Output</em>' containment reference list.
	 * @see net.opengis.wps.WpsPackage#getProcessOutputsType1_Output()
	 * @model type="net.opengis.wps.OutputDataType" containment="true" required="true"
	 *        extendedMetaData="kind='element' name='Output' namespace='##targetNamespace'"
	 * @generated
	 */
	EList getOutput();

} // ProcessOutputsType1
