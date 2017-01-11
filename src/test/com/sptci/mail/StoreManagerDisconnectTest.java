package com.sptci.mail;

import static junit.framework.Assert.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for testing the disconnect method in the {@link
 * com.sptci.mail.StoreManager} class.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-16
 * @version $Id: StoreManagerDisconnectTest.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class StoreManagerDisconnectTest extends TestCase
{
  public static Test suite()
  {
    return new TestSuite( StoreManagerDisconnectTest.class );
  }

  /**
   * Test disconnection from mail store using a new instance of StoreManager.
   */
  public void testDisconnect() throws Exception
  {
    MailSession session = CreateMailSessionTest.session;
    StoreManager manager = new StoreManager( session );
    manager.disconnect();
    assertNull( "Ensure store destroyed in session", session.getStore() );
  }
}
