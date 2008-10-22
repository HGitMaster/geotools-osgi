/**
 * <copyright>
 * </copyright>
 *
 * $Id: ProcessDescriptionsType.java 30488 2008-06-02 23:31:41Z gdavis $
 */
package net.opengis.wps;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Process Descriptions Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link net.opengis.wps.ProcessDescriptionsType#getProcessDescription <em>Process Description</em>}</li>
 * </ul>
 * </p>
 *
 * @see net.opengis.wps.WpsPackage#getProcessDescriptionsType()
 * @model extendedMetaData="name='ProcessDescriptions_._type' kind='elementOnly'"
 * @generated
 */
public interface ProcessDescriptionsType extends ResponseBaseType {
	/**
	 * Returns the value of the '<em><b>Process Description</b></em>' containment reference list.
	 * The list contents are of type {@link net.opengis.wps.ProcessDescriptionType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Ordered list of one or more full Process descriptions, listed in the order in which they were requested in the DescribeProcess operation request.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Process Description</em>' containment reference list.
	 * @see net.opengis.wps.WpsPackage#getProcessDescriptionsType_ProcessDescription()
	 * @model type="net.opengis.wps.ProcessDescriptionType" containment="true" required="true"
	 *        extendedMetaData="kind='element' name='ProcessDescription'"
	 * @generated
	 */
	EList getProcessDescription();

} // ProcessDescriptionsType
