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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;

import org.geotools.arcsde.session.ISession;

/**
 * Seems to visit a list and update the entries and fill in the blanks qualifying them.
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: ItemsListQualifier.java 33451 2009-07-02 17:53:36Z groldan $
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java
 *         /org/geotools/arcsde/data/view/ItemsListQualifier.java $
 * @since 2.3.x
 */
class ItemsListQualifier implements ItemsListVisitor {
    /** DOCUMENT ME! */
    ItemsList _qualifiedList;

    /** DOCUMENT ME! */
    private ISession session;

    private Map tableAliases;

    /**
     * Creates a new ItemsListQualifier object.
     * 
     * @param session
     *            DOCUMENT ME!
     */
    public ItemsListQualifier(ISession session, Map tableAliases) {
        this.session = session;
        this.tableAliases = tableAliases;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param session
     *            DOCUMENT ME!
     * @param items
     *            DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public static ItemsList qualify(ISession session, Map tableAliases, ItemsList items) {
        if (items == null) {
            return null;
        }

        ItemsListQualifier q = new ItemsListQualifier(session, tableAliases);
        items.accept(q);

        return q._qualifiedList;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param subSelect
     *            DOCUMENT ME!
     */
    public void visit(SubSelect subSelect) {
        SubSelect qualified = SubSelectQualifier.qualify(session, subSelect);
        this._qualifiedList = qualified;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param expressionList
     *            DOCUMENT ME!
     */
    public void visit(ExpressionList expressionList) {
        List expressions = expressionList.getExpressions();
        List qualifiedList = new ArrayList(expressions.size());

        for (Iterator it = expressions.iterator(); it.hasNext();) {
            Expression exp = (Expression) it.next();
            Expression qExp = ExpressionQualifier.qualify(session, tableAliases, exp);

            qualifiedList.add(qExp);
        }

        ExpressionList qExpList = new ExpressionList();
        qExpList.setExpressions(qualifiedList);
        this._qualifiedList = qExpList;
    }
}
