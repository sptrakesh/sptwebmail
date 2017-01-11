package com.sptci;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.sptci.mail.*;
import com.sptci.mail.filter.*;

/**
 * Execute all the tests for the project.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-02-26
 * @version $Id: AllTests.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class AllTests
{
  public static Test suite () 
  {
    TestSuite suite= new TestSuite( "All JUnit Tests for sptwebmail project" );
    suite.addTest( PropertiesTest.suite() );
    suite.addTest( CreateMailSessionTest.suite() );
    suite.addTest( StoreManagerConnectTest.suite() );
    suite.addTest( CreateFolderTest.suite() );
    suite.addTest( FolderManagerTest.suite() );

    suite.addTest( MessageSearcherTest.suite() );

    suite.addTest( ActionTest.suite() );
    suite.addTest( ClauseTest.suite() );
    suite.addTest( ClausesTest.suite() );
    suite.addTest( RuleTest.suite() );
    suite.addTest( CreateRulesTest.suite() );
    suite.addTest( RulesTest.suite() );
    
    suite.addTest( DeleteFolderTest.suite() );
    suite.addTest( StoreManagerDisconnectTest.suite() );

    return suite;
  }

  public static void main (String[] args) 
  {
    junit.textui.TestRunner.run (suite());
    System.exit( 0 );
  }
}
