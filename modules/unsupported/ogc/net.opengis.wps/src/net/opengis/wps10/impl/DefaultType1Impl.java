/**
 * <copyright>
 * </copyright>
 *
 * $Id: DefaultType1Impl.java 31841 2008-11-14 13:21:26Z jdeolive $
 */
package net.opengis.wps10.impl;

import net.opengis.ows11.DomainMetadataType;

import net.opengis.wps10.DefaultType1;
import net.opengis.wps10.Wps10Package;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.EObjectImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Default Type1</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link net.opengis.wps10.impl.DefaultType1Impl#getUOM <em>UOM</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class DefaultType1Impl extends EObjectImpl implements DefaultType1 {
    /**
     * The cached value of the '{@link #getUOM() <em>UOM</em>}' containment reference.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #getUOM()
     * @generated
     * @ordered
     */
    protected DomainMetadataType uOM;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected DefaultType1Impl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected EClass eStaticClass() {
        return Wps10Package.Literals.DEFAULT_TYPE1;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public DomainMetadataType getUOM() {
        return uOM;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain basicSetUOM(DomainMetadataType newUOM, NotificationChain msgs) {
        DomainMetadataType oldUOM = uOM;
        uOM = newUOM;
        if (eNotificationRequired()) {
            ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, Wps10Package.DEFAULT_TYPE1__UOM, oldUOM, newUOM);
            if (msgs == null) msgs = notification; else msgs.add(notification);
        }
        return msgs;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void setUOM(DomainMetadataType newUOM) {
        if (newUOM != uOM) {
            NotificationChain msgs = null;
            if (uOM != null)
                msgs = ((InternalEObject)uOM).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - Wps10Package.DEFAULT_TYPE1__UOM, null, msgs);
            if (newUOM != null)
                msgs = ((InternalEObject)newUOM).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - Wps10Package.DEFAULT_TYPE1__UOM, null, msgs);
            msgs = basicSetUOM(newUOM, msgs);
            if (msgs != null) msgs.dispatch();
        }
        else if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, Wps10Package.DEFAULT_TYPE1__UOM, newUOM, newUOM));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case Wps10Package.DEFAULT_TYPE1__UOM:
                return basicSetUOM(null, msgs);
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
            case Wps10Package.DEFAULT_TYPE1__UOM:
                return getUOM();
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
            case Wps10Package.DEFAULT_TYPE1__UOM:
                setUOM((DomainMetadataType)newValue);
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
            case Wps10Package.DEFAULT_TYPE1__UOM:
                setUOM((DomainMetadataType)null);
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
            case Wps10Package.DEFAULT_TYPE1__UOM:
                return uOM != null;
        }
        return super.eIsSet(featureID);
    }

} //DefaultType1Impl
