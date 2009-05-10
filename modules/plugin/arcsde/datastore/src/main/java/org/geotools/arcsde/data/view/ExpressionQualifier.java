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

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SubSelect;

import org.geotools.arcsde.pool.ISession;

/**
 * Qualifies the column references (aliased or not) the ArcSDE "table.user." prefix as required by
 * the ArcSDE java api to not get confused when using joined tables.
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: ExpressionQualifier.java 32195 2009-01-09 19:00:35Z groldan $
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java
 *         /org/geotools/arcsde/data/view/ExpressionQualifier.java $
 * @since 2.3.x
 */
class ExpressionQualifier implements ExpressionVisitor {
    /** DOCUMENT ME! */
    private Expression _qualifiedExpression;

    /** DOCUMENT ME! */
    private ISession session;

    private Map tableAliases;

    /**
     * Creates a new ExpressionQualifier object.
     * 
     * @param session
     *            DOCUMENT ME!
     */
    private ExpressionQualifier(ISession session, Map tableAliases) {
        this.session = session;
        this.tableAliases = tableAliases;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param session
     *            DOCUMENT ME!
     * @param exp
     *            DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    public static Expression qualify(ISession session, Map tableAliases, Expression exp) {
        if (exp == null) {
            return null;
        }

        ExpressionQualifier qualifier = new ExpressionQualifier(session, tableAliases);

        exp.accept(qualifier);

        return qualifier._qualifiedExpression;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param nullValue
     *            DOCUMENT ME!
     */
    public void visit(NullValue nullValue) {
        _qualifiedExpression = nullValue;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param function
     *            DOCUMENT ME!
     */
    public void visit(Function function) {
        Function qfunction = new Function();
        qfunction.setAllColumns(function.isAllColumns());
        qfunction.setEscaped(function.isEscaped());
        qfunction.setName(function.getName());

        ExpressionList parameters = function.getParameters();
        ExpressionList qualifiedParams;

        qualifiedParams = (ExpressionList) ItemsListQualifier.qualify(session, tableAliases,
                parameters);

        qfunction.setParameters(qualifiedParams);

        this._qualifiedExpression = qfunction;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param inverseExpression
     *            DOCUMENT ME!
     */
    public void visit(InverseExpression inverseExpression) {
        InverseExpression qInv = new InverseExpression();

        Expression exp = inverseExpression.getExpression();
        Expression qExp = ExpressionQualifier.qualify(session, tableAliases, exp);

        qInv.setExpression(qExp);
        this._qualifiedExpression = qInv;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param jdbcParameter
     *            DOCUMENT ME!
     */
    public void visit(JdbcParameter jdbcParameter) {
        this._qualifiedExpression = jdbcParameter;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param doubleValue
     *            DOCUMENT ME!
     */
    public void visit(DoubleValue doubleValue) {
        this._qualifiedExpression = doubleValue;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param longValue
     *            DOCUMENT ME!
     */
    public void visit(LongValue longValue) {
        this._qualifiedExpression = longValue;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param dateValue
     *            DOCUMENT ME!
     */
    public void visit(DateValue dateValue) {
        this._qualifiedExpression = dateValue;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param timeValue
     *            DOCUMENT ME!
     */
    public void visit(TimeValue timeValue) {
        this._qualifiedExpression = timeValue;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param timestampValue
     *            DOCUMENT ME!
     */
    public void visit(TimestampValue timestampValue) {
        this._qualifiedExpression = timestampValue;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param parenthesis
     *            DOCUMENT ME!
     */
    public void visit(Parenthesis parenthesis) {
        Expression pExp = parenthesis.getExpression();
        Expression qualifiedExpression;
        qualifiedExpression = qualify(session, tableAliases, pExp);

        Parenthesis qualified = new Parenthesis();
        qualified.setExpression(qualifiedExpression);
        this._qualifiedExpression = qualified;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param stringValue
     *            DOCUMENT ME!
     */
    public void visit(StringValue stringValue) {
        this._qualifiedExpression = stringValue;
    }

    private void visitBinaryExpression(BinaryExpression exp) {

        Expression left = ExpressionQualifier.qualify(session, tableAliases, exp
                .getLeftExpression());
        Expression right = ExpressionQualifier.qualify(session, tableAliases, exp
                .getRightExpression());

        BinaryExpression qualified;

        if (exp instanceof Addition)
            qualified = new Addition();
        else if (exp instanceof Division)
            qualified = new Division();
        else if (exp instanceof Multiplication)
            qualified = new Multiplication();
        else if (exp instanceof Subtraction)
            qualified = new Subtraction();
        else if (exp instanceof EqualsTo)
            qualified = new EqualsTo();
        else if (exp instanceof GreaterThan)
            qualified = new GreaterThan();
        else if (exp instanceof GreaterThanEquals)
            qualified = new GreaterThanEquals();
        else if (exp instanceof LikeExpression)
            qualified = new LikeExpression();
        else if (exp instanceof MinorThan)
            qualified = new MinorThan();
        else if (exp instanceof MinorThanEquals)
            qualified = new MinorThanEquals();
        else if (exp instanceof NotEqualsTo)
            qualified = new NotEqualsTo();
        else
            throw new IllegalArgumentException("Unkown binary expression: " + exp);

        qualified.setLeftExpression(left);
        qualified.setRightExpression(right);

        this._qualifiedExpression = qualified;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param addition
     *            DOCUMENT ME!
     */
    public void visit(Addition addition) {
        visitBinaryExpression(addition);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param division
     *            DOCUMENT ME!
     */
    public void visit(Division division) {
        visitBinaryExpression(division);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param multiplication
     *            DOCUMENT ME!
     */
    public void visit(Multiplication multiplication) {
        visitBinaryExpression(multiplication);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param subtraction
     *            DOCUMENT ME!
     */
    public void visit(Subtraction subtraction) {
        visitBinaryExpression(subtraction);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param andExpression
     *            DOCUMENT ME!
     */
    public void visit(AndExpression andExpression) {
        Expression left = qualify(session, tableAliases, andExpression.getLeftExpression());
        Expression rigth = qualify(session, tableAliases, andExpression.getRightExpression());

        AndExpression and = new AndExpression(left, rigth);
        this._qualifiedExpression = and;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param orExpression
     *            DOCUMENT ME!
     */
    public void visit(OrExpression orExpression) {
        Expression left = qualify(session, tableAliases, orExpression.getLeftExpression());
        Expression rigth = qualify(session, tableAliases, orExpression.getRightExpression());

        OrExpression or = new OrExpression(left, rigth);
        this._qualifiedExpression = or;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param between
     *            DOCUMENT ME!
     */
    public void visit(Between between) {
        Between qualified = new Between();

        Expression start = qualify(session, tableAliases, between.getBetweenExpressionStart());
        Expression end = qualify(session, tableAliases, between.getBetweenExpressionEnd());
        Expression left = qualify(session, tableAliases, between.getLeftExpression());

        qualified.setBetweenExpressionStart(start);
        qualified.setBetweenExpressionEnd(end);
        qualified.setLeftExpression(left);
        this._qualifiedExpression = qualified;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param equalsTo
     *            DOCUMENT ME!
     */
    public void visit(EqualsTo equalsTo) {
        visitBinaryExpression(equalsTo);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param greaterThan
     *            DOCUMENT ME!
     */
    public void visit(GreaterThan greaterThan) {
        visitBinaryExpression(greaterThan);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param greaterThanEquals
     *            DOCUMENT ME!
     */
    public void visit(GreaterThanEquals greaterThanEquals) {
        visitBinaryExpression(greaterThanEquals);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param inExpression
     *            DOCUMENT ME!
     */
    public void visit(InExpression inExpression) {
        Expression left = qualify(session, tableAliases, inExpression.getLeftExpression());
        ItemsList itemsList = ItemsListQualifier.qualify(session, tableAliases, inExpression
                .getItemsList());

        InExpression qualified = new InExpression();
        qualified.setLeftExpression(left);
        qualified.setItemsList(itemsList);
        this._qualifiedExpression = qualified;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param isNullExpression
     *            DOCUMENT ME!
     */
    public void visit(IsNullExpression isNullExpression) {
        IsNullExpression qualified = new IsNullExpression();
        Expression left = qualify(session, tableAliases, isNullExpression.getLeftExpression());

        qualified.setLeftExpression(left);
        qualified.setNot(isNullExpression.isNot());
        this._qualifiedExpression = qualified;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param likeExpression
     *            DOCUMENT ME!
     */
    public void visit(LikeExpression likeExpression) {
        visitBinaryExpression(likeExpression);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param minorThan
     *            DOCUMENT ME!
     */
    public void visit(MinorThan minorThan) {
        visitBinaryExpression(minorThan);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param minorThanEquals
     *            DOCUMENT ME!
     */
    public void visit(MinorThanEquals minorThanEquals) {
        visitBinaryExpression(minorThanEquals);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param notEqualsTo
     *            DOCUMENT ME!
     */
    public void visit(NotEqualsTo notEqualsTo) {
        visitBinaryExpression(notEqualsTo);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param tableColumn
     *            DOCUMENT ME!
     */
    public void visit(Column tableColumn) {

        Column qualified = ColumnQualifier.qualify(session, tableAliases, tableColumn);
        this._qualifiedExpression = qualified;

    }

    /**
     * DOCUMENT ME!
     * 
     * @param subSelect
     *            DOCUMENT ME!
     */
    public void visit(SubSelect subSelect) {
        SubSelect qualified = SubSelectQualifier.qualify(session, subSelect);
        this._qualifiedExpression = qualified;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param caseExpression
     *            DOCUMENT ME!
     */
    public void visit(CaseExpression caseExpression) {
        Expression switchExpr = qualify(session, tableAliases, caseExpression.getSwitchExpression());
        Expression elseExpr = qualify(session, tableAliases, caseExpression.getElseExpression());

        List whenClauses = null;
        if (caseExpression.getWhenClauses() != null) {
            whenClauses = new ArrayList();
            for (Iterator it = caseExpression.getWhenClauses().iterator(); it.hasNext();) {
                WhenClause whenClause = (WhenClause) it.next();
                WhenClause qWhen = (WhenClause) qualify(session, tableAliases, whenClause);
                whenClauses.add(qWhen);
            }
        }

        CaseExpression qualifiedWhen = new CaseExpression();
        qualifiedWhen.setElseExpression(elseExpr);
        qualifiedWhen.setSwitchExpression(switchExpr);
        qualifiedWhen.setWhenClauses(whenClauses);
        this._qualifiedExpression = qualifiedWhen;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param whenClause
     *            DOCUMENT ME!
     */
    public void visit(WhenClause whenClause) {
        Expression whenExpr = qualify(session, tableAliases, whenClause.getWhenExpression());
        Expression thenExpr = qualify(session, tableAliases, whenClause.getThenExpression());

        WhenClause q = new WhenClause();
        q.setWhenExpression(whenExpr);
        q.setThenExpression(thenExpr);
        this._qualifiedExpression = q;
    }
}
