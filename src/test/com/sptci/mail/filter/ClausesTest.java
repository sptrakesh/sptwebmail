package com.sptci.mail.filter;

import java.util.LinkedList;

import static junit.framework.Assert.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for the {@link com.sptci.mail.filter.Clauses} class.  Creates
 * new instances of the class to be used by other tests in this package.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-02-26
 * @version $Id: ClausesTest.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class ClausesTest extends TestCase
{
  static Clauses clauses = new Clauses();
  
  public ClausesTest(String testName)
  {
    super(testName);
  }

  public static Test suite()
  {
    return new TestSuite( ClausesTest.class );
  }

  /**
   * Test of setOperator method, of class com.sptci.mail.filter.Clauses.
   */
  public void testSetOperator()
  {
    clauses.setOperator( Clauses.Operator.any );
    assertEquals( clauses.getOperator(), Clauses.Operator.any );
  }

  /**
   * Test of setClause method, of class com.sptci.mail.filter.Clauses.
   */
  public void testSetClause()
  {
    LinkedList<Clause> list = new LinkedList<Clause>();
    list.add( ClauseTest.clause1 );
    list.add( ClauseTest.clause2 );
    clauses.setClause( list );
    assertEquals( clauses.getClause(), list );
  }
}
