/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.arcsde.data.view;

import java.util.Map;

import net.sf.jsqlparser.statement.select.ColumnReference;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.OrderByVisitor;

import org.geotools.arcsde.pool.ISession;

/**
 * Qualifies a column reference in an order by clause
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: OrderByElementQualifier.java 32195 2009-01-09 19:00:35Z groldan $
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java
 *         /org/geotools/arcsde/data/view/OrderByElementQualifier.java $
 * @since 2.3.x
 */
public class OrderByElementQualifier implements OrderByVisitor {
    /** DOCUMENT ME! */
    private OrderByElement _qualifiedOrderBy;

    /** DOCUMENT ME! */
    private ISession session;

    private Map tableAliases;

    /**
     * Creates a new OrderByElementQualifier object.
     * 
     * @param session
     *            DOCUMENT ME!
     */
    private OrderByElementQualifier(ISession session, Map tableAliases) {
        this.session = session;
        this.tableAliases = tableAliases;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param session
     *            DOCUMENT ME!
     * @param orderBy
     *            DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public static OrderByElement qualify(ISession session, Map tableAliases, OrderByElement orderBy) {
        if (orderBy == null) {
            return null;
        }

        OrderByElementQualifier qualifier = new OrderByElementQualifier(session, tableAliases);
        orderBy.accept(qualifier);

        return qualifier._qualifiedOrderBy;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param orderBy
     *            DOCUMENT ME!
     */
    public void visit(OrderByElement orderBy) {
        OrderByElement qualifiedOrderBy = new OrderByElement();
        qualifiedOrderBy.setAsc(orderBy.isAsc());

        ColumnReference colRef = orderBy.getColumnReference();

        ColumnReference qualifiedColRef = ColumnReferenceQualifier.qualify(session, tableAliases,
                colRef);

        qualifiedOrderBy.setColumnReference(qualifiedColRef);

        this._qualifiedOrderBy = qualifiedOrderBy;
    }
}
