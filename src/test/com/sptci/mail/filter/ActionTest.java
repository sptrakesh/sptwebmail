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
 * @version $Id: ActionTest.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class ActionTest extends TestCase
{
  static Action action1 = new Action();
  static Action action2 = new Action();
  
  public ActionTest(String testName)
  {
    super(testName);
  }

  public static Test suite()
  {
    return new TestSuite( ActionTest.class );
  }

  /**
   * Test of setPerform method, of class com.sptci.mail.filter.Action.
   */
  public void testSetPerform()
  {
    action1.setPerform( Action.Perform.MoveMessage );
    action2.setPerform( Action.Perform.DeleteMessage );
    assertEquals( action1.getPerform(), Action.Perform.MoveMessage );
    assertEquals( action2.getPerform(), Action.Perform.DeleteMessage );
  }

  /**
   * Test of setValue method, of class com.sptci.mail.filter.Action.
   */
  public void testSetValue()
  {
    String value = "value";
    action1.setValue( value );
    action2.setValue( value );
    assertEquals( value, action1.getValue() );
    assertEquals( value, action2.getValue() );
  }
}
