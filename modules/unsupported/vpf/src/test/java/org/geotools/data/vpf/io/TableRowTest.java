/*
 * File is generated by 'Unit Tests Generator' developed under
 * 'Web Test Tools' project at http://sf.net/projects/wttools/
 * Copyright (C) 2001 "Artur Hefczyc" <kobit@users.sourceforge.net>
 * to all 'Web Test Tools' subprojects.
 *
 * No rigths to files and no responsibility for code generated
 * by this tool are belonged to author of 'unittestsgen' utility.
 *
 */
package org.geotools.data.vpf.io;

import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.geotools.data.vpf.ifc.DataTypesDefinition;

/**
 * File <code>TableRowTest.java</code> is automaticaly generated by
 * 'unittestsgen' application. Code generator is created for java
 * sources and for 'junit' package by "Artur Hefczyc"
 * <kobit@users.sourceforge.net><br/>
 * You should fulfil test methods with proper code for testing
 * purpose. All methods where you should put your code are below and
 * their names starts with 'test'.<br/>
 * You can run unit tests in many ways, however prefered are:
 * <ul>
 *   <li>Run tests for one class only, for example for this class you
 *       can run tests with command:
 *     <pre>
 *       java -cp "jar/thisjarfile.jar;lib/junit.jar" org.geotools.vpf.TableRowTest
 *     </pre>
 *   </li>
 *   <li>Run tests for all classes in one command call. Code generator
 *       creates also <code>TestAll.class</code> which runs all
 *       available tests:
 *     <pre>
 *       java -cp "jar/thisjarfile.jar;lib/junit.jar" TestAll
 *     </pre>
 *   </li>
 *   <li>But the most prefered way is to run all tests from
 *     <em>Ant</em> just after compilation process finished.<br/>
 *     To do it. You need:
 *     <ol>
 *       <li>Ant package from
 *         <a href="http://jakarta.apache.org/">Ant</a>
 *       </li>
 *       <li>JUnit package from
 *         <a href="http://www.junit.org/">JUnit</a>
 *       </li>
 *       <li>Put some code in your <code>build.xml</code> file
 *         to tell Ant how to test your package. Sample code for
 *         Ant's <code>build.xml</code> you can find in created file:
 *         <code>sample-junit-build.xml</code>. And remember to have
 *         <code>junit.jar</code> in CLASSPATH <b>before</b> you run Ant.
 *         To generate reports by ant you must have <code>xalan.jar</code>
 *         in your <code>ANT_HOME/lib/</code> directory.
 *       </li>
 *     </ol>
 *   </li>
 * </ul>
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.5/modules/unsupported/vpf/src/test/java/org/geotools/data/vpf/io/TableRowTest.java $
 */
public class TableRowTest extends TestCase
  implements DataTypesDefinition
{
  /**
   * Instance of tested class.
   */
  protected TableRow varTableRow;

  /**
   * Public constructor for creating testing class.
   */
  public TableRowTest(String name) {
    super(name);
  } // end of TableRowTest(String name)
  /**
   * This main method is used for run tests for this class only
   * from command line.
   */
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  } // end of main(Stringp[] args)

  public static final RowField[] TEST_FIELDS = 
  {
    new RowField(new Float(1f), DATA_SHORT_FLOAT),
    new RowField(new Short((short)2), DATA_SHORT_INTEGER),
    new RowField(new VPFDate("200301301149.00000"), DATA_DATE_TIME)
  };

  /**
   * This method is called every time before particular test execution.
   * It creates new instance of tested class and it can perform some more
   * actions which are necessary for performs tests.
   */
  protected void setUp()
  {
    HashMap map = new HashMap();
    map.put("first", TEST_FIELDS[0]);
    map.put("second", TEST_FIELDS[1]);
    map.put("third", TEST_FIELDS[2]);
    varTableRow = new TableRow(TEST_FIELDS, map);
  }
  /**
   * Returns all tests which should be performed for testing class.
   * By default it returns only name of testing class. Instance of this
   * is then created with its constructor.
   */
  public static Test suite() {
    return new TestSuite(TableRowTest.class);
  } // end of suite()

  /**
   * Method for testing original source method:
   * int fieldsCount()
   * from tested class
   */
  public void testFieldsCount()
  {
    assertEquals("Checking row size.",
                 TEST_FIELDS.length, varTableRow.fieldsCount());
  } // end of testFieldsCount()

  /**
   * Method for testing original source method:
   * org.geotools.vpf.RowField get(java.lang.String)
   * from tested class
   */
  public void testGet1195259493()
  {
    assertSame("Checking method get field by name.",
               TEST_FIELDS[0], varTableRow.get("first"));
    assertSame("Checking method get field by name.",
               TEST_FIELDS[1], varTableRow.get("second"));
    assertSame("Checking method get field by name.",
               TEST_FIELDS[2], varTableRow.get("third"));
  } // end of testGet1195259493(java.lang.String)

  /**
   * Method for testing original source method:
   * org.geotools.vpf.RowField get(int)
   * from tested class
   */
  public void testGet104431()
  {
    assertSame("Checking method get field by index.",
               TEST_FIELDS[0], varTableRow.get(0));
    assertSame("Checking method get field by index.",
               TEST_FIELDS[1], varTableRow.get(1));
    assertSame("Checking method get field by index.",
               TEST_FIELDS[2], varTableRow.get(2));
  } // end of testGet104431(int)

} // end of TableRowTest
