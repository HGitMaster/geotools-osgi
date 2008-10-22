/**
 * <copyright>
 * </copyright>
 *
 * $Id: DataTypeImpl.java 29861 2008-04-09 04:55:54Z jdeolive $
 */
package net.opengis.wps.impl;

import net.opengis.ows11.BoundingBoxType;

import net.opengis.wps.ComplexDataType;
import net.opengis.wps.DataType;
import net.opengis.wps.LiteralDataType;
import net.opengis.wps.WpsPackage;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Data Type</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link net.opengis.wps.impl.DataTypeImpl#getComplexData <em>Complex Data</em>}</li>
 *   <li>{@link net.opengis.wps.impl.DataTypeImpl#getLiteralData <em>Literal Data</em>}</li>
 *   <li>{@link net.opengis.wps.impl.DataTypeImpl#getBoundingBoxData <em>Bounding Box Data</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class DataTypeImpl extends EObjectImpl implements DataType {
	/**
	 * The cached value of the '{@link #getComplexData() <em>Complex Data</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComplexData()
	 * @generated
	 * @ordered
	 */
	protected ComplexDataType complexData;

	/**
	 * The cached value of the '{@link #getLiteralData() <em>Literal Data</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLiteralData()
	 * @generated
	 * @ordered
	 */
	protected LiteralDataType literalData;

	/**
	 * The cached value of the '{@link #getBoundingBoxData() <em>Bounding Box Data</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getBoundingBoxData()
	 * @generated
	 * @ordered
	 */
	protected BoundingBoxType boundingBoxData;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DataTypeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected EClass eStaticClass() {
		return WpsPackage.eINSTANCE.getDataType();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ComplexDataType getComplexData() {
		return complexData;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetComplexData(ComplexDataType newComplexData, NotificationChain msgs) {
		ComplexDataType oldComplexData = complexData;
		complexData = newComplexData;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, WpsPackage.DATA_TYPE__COMPLEX_DATA, oldComplexData, newComplexData);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setComplexData(ComplexDataType newComplexData) {
		if (newComplexData != complexData) {
			NotificationChain msgs = null;
			if (complexData != null)
				msgs = ((InternalEObject)complexData).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - WpsPackage.DATA_TYPE__COMPLEX_DATA, null, msgs);
			if (newComplexData != null)
				msgs = ((InternalEObject)newComplexData).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - WpsPackage.DATA_TYPE__COMPLEX_DATA, null, msgs);
			msgs = basicSetComplexData(newComplexData, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WpsPackage.DATA_TYPE__COMPLEX_DATA, newComplexData, newComplexData));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public LiteralDataType getLiteralData() {
		return literalData;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetLiteralData(LiteralDataType newLiteralData, NotificationChain msgs) {
		LiteralDataType oldLiteralData = literalData;
		literalData = newLiteralData;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, WpsPackage.DATA_TYPE__LITERAL_DATA, oldLiteralData, newLiteralData);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setLiteralData(LiteralDataType newLiteralData) {
		if (newLiteralData != literalData) {
			NotificationChain msgs = null;
			if (literalData != null)
				msgs = ((InternalEObject)literalData).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - WpsPackage.DATA_TYPE__LITERAL_DATA, null, msgs);
			if (newLiteralData != null)
				msgs = ((InternalEObject)newLiteralData).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - WpsPackage.DATA_TYPE__LITERAL_DATA, null, msgs);
			msgs = basicSetLiteralData(newLiteralData, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WpsPackage.DATA_TYPE__LITERAL_DATA, newLiteralData, newLiteralData));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public BoundingBoxType getBoundingBoxData() {
		return boundingBoxData;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetBoundingBoxData(BoundingBoxType newBoundingBoxData, NotificationChain msgs) {
		BoundingBoxType oldBoundingBoxData = boundingBoxData;
		boundingBoxData = newBoundingBoxData;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, WpsPackage.DATA_TYPE__BOUNDING_BOX_DATA, oldBoundingBoxData, newBoundingBoxData);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setBoundingBoxData(BoundingBoxType newBoundingBoxData) {
		if (newBoundingBoxData != boundingBoxData) {
			NotificationChain msgs = null;
			if (boundingBoxData != null)
				msgs = ((InternalEObject)boundingBoxData).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - WpsPackage.DATA_TYPE__BOUNDING_BOX_DATA, null, msgs);
			if (newBoundingBoxData != null)
				msgs = ((InternalEObject)newBoundingBoxData).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - WpsPackage.DATA_TYPE__BOUNDING_BOX_DATA, null, msgs);
			msgs = basicSetBoundingBoxData(newBoundingBoxData, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, WpsPackage.DATA_TYPE__BOUNDING_BOX_DATA, newBoundingBoxData, newBoundingBoxData));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case WpsPackage.DATA_TYPE__COMPLEX_DATA:
				return basicSetComplexData(null, msgs);
			case WpsPackage.DATA_TYPE__LITERAL_DATA:
				return basicSetLiteralData(null, msgs);
			case WpsPackage.DATA_TYPE__BOUNDING_BOX_DATA:
				return basicSetBoundingBoxData(null, msgs);
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
			case WpsPackage.DATA_TYPE__COMPLEX_DATA:
				return getComplexData();
			case WpsPackage.DATA_TYPE__LITERAL_DATA:
				return getLiteralData();
			case WpsPackage.DATA_TYPE__BOUNDING_BOX_DATA:
				return getBoundingBoxData();
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
			case WpsPackage.DATA_TYPE__COMPLEX_DATA:
				setComplexData((ComplexDataType)newValue);
				return;
			case WpsPackage.DATA_TYPE__LITERAL_DATA:
				setLiteralData((LiteralDataType)newValue);
				return;
			case WpsPackage.DATA_TYPE__BOUNDING_BOX_DATA:
				setBoundingBoxData((BoundingBoxType)newValue);
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
			case WpsPackage.DATA_TYPE__COMPLEX_DATA:
				setComplexData((ComplexDataType)null);
				return;
			case WpsPackage.DATA_TYPE__LITERAL_DATA:
				setLiteralData((LiteralDataType)null);
				return;
			case WpsPackage.DATA_TYPE__BOUNDING_BOX_DATA:
				setBoundingBoxData((BoundingBoxType)null);
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
			case WpsPackage.DATA_TYPE__COMPLEX_DATA:
				return complexData != null;
			case WpsPackage.DATA_TYPE__LITERAL_DATA:
				return literalData != null;
			case WpsPackage.DATA_TYPE__BOUNDING_BOX_DATA:
				return boundingBoxData != null;
		}
		return super.eIsSet(featureID);
	}

} //DataTypeImpl
