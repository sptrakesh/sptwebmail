package com.sptci.mail;

import static junit.framework.Assert.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for creating a new instance of the {@link 
 * com.sptci.mail.MailSession} class.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-16
 * @version $Id: CreateMailSessionTest.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class CreateMailSessionTest extends TestCase
{
  static MailSession session;

  public static Test suite()
  {
    return new TestSuite( CreateMailSessionTest.class );
  }

  /**
   * Test creation of a new instance of {@link com.sptci.mail.MailSession}.
   */
  public void testCreate() throws Exception
  {
    session = new MailSession();
    assertNotNull( "Ensure proper initialisation", session.getSession() );
  }
}
