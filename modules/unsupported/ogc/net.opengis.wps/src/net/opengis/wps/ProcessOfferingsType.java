/**
 * <copyright>
 * </copyright>
 *
 * $Id: ProcessOfferingsType.java 30488 2008-06-02 23:31:41Z gdavis $
 */
package net.opengis.wps;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Process Offerings Type</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link net.opengis.wps.ProcessOfferingsType#getProcess <em>Process</em>}</li>
 * </ul>
 * </p>
 *
 * @see net.opengis.wps.WpsPackage#getProcessOfferingsType()
 * @model extendedMetaData="name='ProcessOfferings_._type' kind='elementOnly'"
 * @generated
 */
public interface ProcessOfferingsType extends EObject {
	/**
	 * Returns the value of the '<em><b>Process</b></em>' containment reference list.
	 * The list contents are of type {@link net.opengis.wps.ProcessBriefType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Unordered list of one or more brief descriptions of all the processes offered by this WPS server.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Process</em>' containment reference list.
	 * @see net.opengis.wps.WpsPackage#getProcessOfferingsType_Process()
	 * @model type="net.opengis.wps.ProcessBriefType" containment="true" required="true"
	 *        extendedMetaData="kind='element' name='Process' namespace='##targetNamespace'"
	 * @generated
	 */
	EList getProcess();

} // ProcessOfferingsType
