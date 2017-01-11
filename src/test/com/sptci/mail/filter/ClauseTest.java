package com.sptci.mail.filter;

import static junit.framework.Assert.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for the {@link com.sptci.mail.filter.Clause} class.  Creates
 * new instances of the class to be used by other tests in this package.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-02-26
 * @version $Id: ClauseTest.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class ClauseTest extends TestCase
{
  static Clause clause1 = new Clause();
  static Clause clause2 = new Clause();

  public ClauseTest(String testName)
  {
    super(testName);
  }

  public static Test suite()
  {
    return new TestSuite( ClauseTest.class );
  }

  /**
   * Test of setProperty method, of class com.sptci.mail.filter.Clause.
   */
  public void testSetProperty()
  {
    clause1.setProperty( Clause.Property.To );
    clause2.setProperty( Clause.Property.Subject );
    assertEquals( clause1.getProperty(), Clause.Property.To );
    assertEquals( clause2.getProperty(), Clause.Property.Subject );
  }

  /**
   * Test of setCondition method, of class com.sptci.mail.filter.Clause.
   */
  public void testSetCondition()
  {
    clause1.setCondition( Clause.Condition.IsEqualTo );
    clause2.setCondition( Clause.Condition.Contains );
    assertEquals( clause1.getCondition(), Clause.Condition.IsEqualTo );
    assertEquals( clause2.getCondition(), Clause.Condition.Contains );
  }

  /**
   * Test of setValue method, of class com.sptci.mail.filter.Clause.
   */
  public void testSetValue()
  {
    String value = "value";
    clause1.setValue( value );
    clause2.setValue( value );
    assertEquals( clause1.getValue(), value );
    assertEquals( clause2.getValue(), value );
  }
}
