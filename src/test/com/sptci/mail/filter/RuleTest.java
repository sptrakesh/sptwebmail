package com.sptci.mail.filter;

import java.util.LinkedList;

import static junit.framework.Assert.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for the {@link com.sptci.mail.filter.Rule} class.  Creates
 * new instances of the class to be used by other tests in this package.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-02-26
 * @version $Id: RuleTest.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class RuleTest extends TestCase
{
  static Rule rule1 = new Rule();
  static Rule rule2 = new Rule();

  public RuleTest(String testName)
  {
    super(testName);
  }

  public static Test suite()
  {
    return new TestSuite( RuleTest.class );
  }

  public void testSetName()
  {
    String name1 = "Rule 1";
    String name2 = "Rule 2";
    rule1.setName( name1 );
    rule2.setName( name2 );

    assertEquals( name1, rule1.getName() );
    assertEquals( name2, rule2.getName() );
  }

  public void testSetClauses()
  {
    rule1.setClauses( ClausesTest.clauses );
    rule2.setClauses( ClausesTest.clauses );
    assertEquals( rule1.getClauses(), ClausesTest.clauses );
    assertEquals( rule2.getClauses(), ClausesTest.clauses );
  }

  public void testSetActions()
  {
    LinkedList<Action> list = new LinkedList<Action>();
    list.add( ActionTest.action1 );
    list.add( ActionTest.action2 );

    rule1.setActions( list );
    rule2.setActions( list );
    assertEquals( rule1.getActions(), list );
    assertEquals( rule2.getActions(), list );
  }
}
