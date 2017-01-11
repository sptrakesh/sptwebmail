package com.sptci.mail;

import static junit.framework.Assert.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for testing the connect method in the {@link
 * com.sptci.mail.StoreManager} class.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-16
 * @version $Id: StoreManagerConnectTest.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class StoreManagerConnectTest extends TestCase
{
  static String user = System.getProperty( "mail.username" );
  static String password = System.getProperty( "mail.password" );

  public static Test suite()
  {
    return new TestSuite( StoreManagerConnectTest.class );
  }

  /**
   * Test connection to mail store using a new instance of StoreManager.
   */
  public void testConnect() throws Exception
  {
    MailSession session = CreateMailSessionTest.session;
    StoreManager manager = new StoreManager( session );
    manager.connect( user, password );
    assertNotNull( "Ensure store set in session", session.getStore() );
    assertNotNull( "Ensure user set in session", session.getSession() );
  }
}
