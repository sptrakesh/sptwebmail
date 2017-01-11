package com.sptci.mail.filter;

import java.util.LinkedList;

import static junit.framework.Assert.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.sptci.io.FileUtilities;

/**
 * Unit test for the {@link com.sptci.mail.filter.Rules} class.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-02-26
 * @version $Id: RulesTest.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class RulesTest extends TestCase
{
  Rules rules;

  public RulesTest( String testName )
  {
    super( testName );
  }

  public static Test suite()
  {
    return new TestSuite( RulesTest.class );
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    rules = CreateRulesTest.rules;

    LinkedList<Rule> list = new LinkedList<Rule>();
    list.add( RuleTest.rule1 );
    list.add( RuleTest.rule2 );
    rules.setRule( list );
  }

  @Override
  protected void tearDown() throws Exception
  {
    FileUtilities.delete( CreateRulesTest.file, false );
  }

  /**
   * Test of save method, of class com.sptci.mail.filter.Rules.
   */
  public void testSave() throws Exception
  {
    String xml = rules.toString();
    rules.save();

    Rules r = Rules.getInstance( CreateRulesTest.file );
    assertEquals( r.toString(), xml );
    assertTrue( ! r.getRule().isEmpty() );
  }
}
