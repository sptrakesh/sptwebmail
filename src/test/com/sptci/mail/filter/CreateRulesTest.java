package com.sptci.mail.filter;

import java.util.LinkedList;

import static junit.framework.Assert.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for the {@link com.sptci.mail.filter.Rules} class.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-02-26
 * @version $Id: CreateRulesTest.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class CreateRulesTest extends TestCase
{
  static final String file = "/tmp/rules.xml";
  static Rules rules;

  public CreateRulesTest( String testName )
  {
    super( testName );
  }

  public static Test suite()
  {
    return new TestSuite( CreateRulesTest.class );
  }

  /**
   * Test of creation of a new instance of {@link com.sptci.mail.filter.Rules}
   * using the {@link com.sptci.mail.filter.Rules#getInstance} method.
   */
  public void testGetInstance() throws Exception
  {
    rules = Rules.getInstance( file );
    assertNotNull( "Checking non-null rules instance", rules );
    assertTrue( "Checking empty Rules.rule object", rules.getRule().isEmpty() );
  }
}
