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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.Union;

import org.geotools.arcsde.pool.ISession;

/**
 * Visitor on a PlainSelect that produces another one but with all the table names and field names
 * fully qualified as expected by ArcSDE.
 * <p>
 * At any time may throw an IllegalArgumentException if a table or field name stated in the
 * PlainSelect is not found on the arcsde instance.
 * </p>
 * <p>
 * Usage:
 * 
 * <pre>
 * <code>
 *   PlainSelect unqualifiedSelect = ...
 *   SeConnection conn = ...
 *   SelectVisitor visitor = new SelectVisitor(conn);
 *   visitor.accept(unqualifiedSelect);
 *   
 *   PlainSelect qualifiedSelect = visitor.getQualifiedQuery();
 * </code>
 * </pre>
 * 
 * </p>
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: SelectQualifier.java 30722 2008-06-13 18:15:42Z acuster $
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java/org/geotools/arcsde/data/view/SelectQualifier.java $
 * @since 2.3.x
 */
public class SelectQualifier implements net.sf.jsqlparser.statement.select.SelectVisitor {
    /** DOCUMENT ME! */
    private ISession session;

    /** DOCUMENT ME! */
    private PlainSelect qualifiedSelect;

    /**
     * Creates a new SelectQualifier object.
     * 
     * @param session DOCUMENT ME!
     */
    public SelectQualifier(ISession session) {
        this.session = session;
    }

    public static PlainSelect qualify(ISession conn, PlainSelect select) {
        SelectQualifier q = new SelectQualifier(conn);
        select.accept(q);
        return q.qualifiedSelect;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return DOCUMENT ME!
     * @throws IllegalStateException DOCUMENT ME!
     */
    public PlainSelect getQualifiedQuery() {
        if (qualifiedSelect == null) {
            throw new IllegalStateException("not created yet");
        }

        return qualifiedSelect;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param plainSelect DOCUMENT ME!
     * @throws IllegalStateException DOCUMENT ME!
     */
    public void visit(PlainSelect plainSelect) throws IllegalStateException {
        qualifiedSelect = new PlainSelect();

        List fromItems = qualifyFromItems(plainSelect.getFromItems());
        qualifiedSelect.setFromItems(fromItems);

        Map /* <String,Table> */aliases = extractTableAliases(fromItems);

        // @todo: REVISIT, looks like a bug here, fromItems is not being read after assigned
        fromItems = removeTableAliases(fromItems);

        List selectItems = qualifySelectItems(aliases, plainSelect.getSelectItems());
        qualifiedSelect.setSelectItems(selectItems);

        Expression where = qualifyWhere(aliases, plainSelect.getWhere());
        qualifiedSelect.setWhere(where);

        List orderByItems = qualifyOrderBy(aliases, plainSelect.getOrderByElements());
        qualifiedSelect.setOrderByElements(orderByItems);
    }

    private Map extractTableAliases(List fromItems) {
        Map aliases = new HashMap();
        for (Iterator it = fromItems.iterator(); it.hasNext();) {
            FromItem fromItem = (FromItem) it.next();
            if (fromItem instanceof Table) {
                Table table = (Table) fromItem;
                String alias = table.getAlias();
                if (alias != null) {
                    aliases.put(alias, table);
                }
            }
        }
        return aliases;
    }

    private List removeTableAliases(final List fromItems) {
        List items = new ArrayList(fromItems);

        for (Iterator it = items.iterator(); it.hasNext();) {

            FromItem fromItem = (FromItem) it.next();

            if (fromItem instanceof Table) {
                Table table = (Table) fromItem;
                table.setAlias(null);
            }
        }
        return items;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param where DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    private Expression qualifyWhere(Map tableAliases, Expression where) {
        if (where == null) {
            return null;
        }

        Expression qualifiedWhere = ExpressionQualifier.qualify(session, tableAliases, where);

        return qualifiedWhere;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param orderByElements DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    private List qualifyOrderBy(Map tableAliases, List orderByElements) {
        if (orderByElements == null) {
            return null;
        }

        List qualifiedOrderElems = new ArrayList();

        for (Iterator it = orderByElements.iterator(); it.hasNext();) {
            OrderByElement orderByElem = (OrderByElement) it.next();

            OrderByElement qualified = OrderByElementQualifier.qualify(session, tableAliases,
                    orderByElem);

            qualifiedOrderElems.add(qualified);
        }

        return qualifiedOrderElems;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param selectItems List&lt;{@link SelectItem}&gt;
     * @return DOCUMENT ME!
     */
    private List qualifySelectItems(Map tableAlias, List selectItems) {
        if (selectItems == null) {
            return null;
        }

        List qualifiedItems = new ArrayList();

        for (Iterator it = selectItems.iterator(); it.hasNext();) {
            SelectItem selectItem = (SelectItem) it.next();

            List items = SelectItemQualifier.qualify(session, tableAlias, selectItem);

            qualifiedItems.addAll(items);
        }

        return qualifiedItems;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param fromItems List&lt;{@link FromItem}&gt;
     * @return DOCUMENT ME!
     */
    private List qualifyFromItems(List fromItems) {
        if (fromItems == null) {
            return null;
        }

        List qualifiedFromItems = new ArrayList();

        for (Iterator it = fromItems.iterator(); it.hasNext();) {
            FromItem fromItem = (FromItem) it.next();

            FromItem qualifiedItem = FromItemQualifier.qualify(session, fromItem);

            qualifiedFromItems.add(qualifiedItem);
        }

        return qualifiedFromItems;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param union DOCUMENT ME!
     * @throws UnsupportedOperationException DOCUMENT ME!
     */
    public void visit(Union union) {
        throw new UnsupportedOperationException();
    }
}
