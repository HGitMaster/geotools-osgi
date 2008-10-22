/**
 * <copyright>
 * </copyright>
 *
 * $Id: OutputDescriptionTypeImpl.java 29861 2008-04-09 04:55:54Z jdeolive $
 */
package net.opengis.wps.impl;

import net.opengis.wps.LiteralOutputType;
import net.opengis.wps.OutputDescriptionType;
import net.opengis.wps.SupportedCRSsType;
import net.opengis.wps.SupportedComplexDataType;
import net.opengis.wps.WpsPackage;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Output Description Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link net.opengis.wps.impl.OutputDescriptionTypeImpl#getComplexOutput <em>Complex Output</em>}</li>
 *   <li>{@link net.opengis.wps.impl.OutputDescriptionTypeImpl#getLiteralOutput <em>Literal Output</em>}</li>
 *   <li>{@link net.opengis.wps.impl.OutputDescriptionTypeImpl#getBoundingBoxOutput <em>Bounding Box Output</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class OutputDescriptionTypeImpl extends DescriptionTypeImpl implements OutputDescriptionType {
	/**
	 * The cached value of the '{@link #getComplexOutput() <em>Complex Output</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComplexOutput()
	 * @generated
	 * @ordered
	 */
	protected SupportedComplexDataType complexOutput;

	/**
	 * The cached value of the '{@link #getLiteralOutput() <em>Literal Output</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLiteralOutput()
	 * @generated
	 * @ordered
	 */
	protected LiteralOutputType literalOutput;

	/**
	 * The cached value of the '{@link #getBoundingBoxOutput() <em>Bounding Box Output</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBoundingBoxOutput()
	 * @generated
	 * @ordered
	 */
	protected SupportedCRSsType boundingBoxOutput;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected OutputDescriptionTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected EClass eStaticClass() {
		return WpsPackage.eINSTANCE.getOutputDescriptionType();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SupportedComplexDataType getComplexOutput() {
		return complexOutput;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetComplexOutput(SupportedComplexDataType newComplexOutput, NotificationChain msgs) {
		SupportedComplexDataType oldComplexOutput = complexOutput;
		complexOutput = newComplexOutput;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, WpsPackage.OUTPUT_DESCRIPTION_TYPE__COMPLEX_OUTPUT, oldComplexOutput, newComplexOutput);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setComplexOutput(SupportedComplexDataType newComplexOutput) {
		if (newComplexOutput != complexOutput) {
			NotificationChain msgs = null;
			if (complexOutput != null)
				msgs = ((InternalEObject)complexOutput).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - WpsPackage.OUTPUT_DESCRIPTION_TYPE__COMPLEX_OUTPUT, null, msgs);
			if (newComplexOutput != null)
				msgs = ((InternalEObject)newComplexOutput).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - WpsPackage.OUTPUT_DESCRIPTION_TYPE__COMPLEX_OUTPUT, null, msgs);
			msgs = basicSetComplexOutput(newComplexOutput, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WpsPackage.OUTPUT_DESCRIPTION_TYPE__COMPLEX_OUTPUT, newComplexOutput, newComplexOutput));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public LiteralOutputType getLiteralOutput() {
		return literalOutput;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetLiteralOutput(LiteralOutputType newLiteralOutput, NotificationChain msgs) {
		LiteralOutputType oldLiteralOutput = literalOutput;
		literalOutput = newLiteralOutput;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, WpsPackage.OUTPUT_DESCRIPTION_TYPE__LITERAL_OUTPUT, oldLiteralOutput, newLiteralOutput);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLiteralOutput(LiteralOutputType newLiteralOutput) {
		if (newLiteralOutput != literalOutput) {
			NotificationChain msgs = null;
			if (literalOutput != null)
				msgs = ((InternalEObject)literalOutput).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - WpsPackage.OUTPUT_DESCRIPTION_TYPE__LITERAL_OUTPUT, null, msgs);
			if (newLiteralOutput != null)
				msgs = ((InternalEObject)newLiteralOutput).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - WpsPackage.OUTPUT_DESCRIPTION_TYPE__LITERAL_OUTPUT, null, msgs);
			msgs = basicSetLiteralOutput(newLiteralOutput, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WpsPackage.OUTPUT_DESCRIPTION_TYPE__LITERAL_OUTPUT, newLiteralOutput, newLiteralOutput));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SupportedCRSsType getBoundingBoxOutput() {
		return boundingBoxOutput;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetBoundingBoxOutput(SupportedCRSsType newBoundingBoxOutput, NotificationChain msgs) {
		SupportedCRSsType oldBoundingBoxOutput = boundingBoxOutput;
		boundingBoxOutput = newBoundingBoxOutput;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, WpsPackage.OUTPUT_DESCRIPTION_TYPE__BOUNDING_BOX_OUTPUT, oldBoundingBoxOutput, newBoundingBoxOutput);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setBoundingBoxOutput(SupportedCRSsType newBoundingBoxOutput) {
		if (newBoundingBoxOutput != boundingBoxOutput) {
			NotificationChain msgs = null;
			if (boundingBoxOutput != null)
				msgs = ((InternalEObject)boundingBoxOutput).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - WpsPackage.OUTPUT_DESCRIPTION_TYPE__BOUNDING_BOX_OUTPUT, null, msgs);
			if (newBoundingBoxOutput != null)
				msgs = ((InternalEObject)newBoundingBoxOutput).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - WpsPackage.OUTPUT_DESCRIPTION_TYPE__BOUNDING_BOX_OUTPUT, null, msgs);
			msgs = basicSetBoundingBoxOutput(newBoundingBoxOutput, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WpsPackage.OUTPUT_DESCRIPTION_TYPE__BOUNDING_BOX_OUTPUT, newBoundingBoxOutput, newBoundingBoxOutput));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case WpsPackage.OUTPUT_DESCRIPTION_TYPE__COMPLEX_OUTPUT:
				return basicSetComplexOutput(null, msgs);
			case WpsPackage.OUTPUT_DESCRIPTION_TYPE__LITERAL_OUTPUT:
				return basicSetLiteralOutput(null, msgs);
			case WpsPackage.OUTPUT_DESCRIPTION_TYPE__BOUNDING_BOX_OUTPUT:
				return basicSetBoundingBoxOutput(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case WpsPackage.OUTPUT_DESCRIPTION_TYPE__COMPLEX_OUTPUT:
				return getComplexOutput();
			case WpsPackage.OUTPUT_DESCRIPTION_TYPE__LITERAL_OUTPUT:
				return getLiteralOutput();
			case WpsPackage.OUTPUT_DESCRIPTION_TYPE__BOUNDING_BOX_OUTPUT:
				return getBoundingBoxOutput();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case WpsPackage.OUTPUT_DESCRIPTION_TYPE__COMPLEX_OUTPUT:
				setComplexOutput((SupportedComplexDataType)newValue);
				return;
			case WpsPackage.OUTPUT_DESCRIPTION_TYPE__LITERAL_OUTPUT:
				setLiteralOutput((LiteralOutputType)newValue);
				return;
			case WpsPackage.OUTPUT_DESCRIPTION_TYPE__BOUNDING_BOX_OUTPUT:
				setBoundingBoxOutput((SupportedCRSsType)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void eUnset(int featureID) {
		switch (featureID) {
			case WpsPackage.OUTPUT_DESCRIPTION_TYPE__COMPLEX_OUTPUT:
				setComplexOutput((SupportedComplexDataType)null);
				return;
			case WpsPackage.OUTPUT_DESCRIPTION_TYPE__LITERAL_OUTPUT:
				setLiteralOutput((LiteralOutputType)null);
				return;
			case WpsPackage.OUTPUT_DESCRIPTION_TYPE__BOUNDING_BOX_OUTPUT:
				setBoundingBoxOutput((SupportedCRSsType)null);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case WpsPackage.OUTPUT_DESCRIPTION_TYPE__COMPLEX_OUTPUT:
				return complexOutput != null;
			case WpsPackage.OUTPUT_DESCRIPTION_TYPE__LITERAL_OUTPUT:
				return literalOutput != null;
			case WpsPackage.OUTPUT_DESCRIPTION_TYPE__BOUNDING_BOX_OUTPUT:
				return boundingBoxOutput != null;
		}
		return super.eIsSet(featureID);
	}

} //OutputDescriptionTypeImpl
