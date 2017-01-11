package com.sptci.mail;

import static junit.framework.Assert.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for the {@link com.sptci.mail.Properties} class.  Ensures
 * that the class loads the property values from the configured property
 * file.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-10
 * @version $Id: PropertiesTest.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class PropertiesTest extends TestCase
{
  public static Test suite()
  {
    return new TestSuite( PropertiesTest.class );
  }

  /**
   * Test initialisation of properties object.  Test succeeds if the
   * {@link com.sptci.mail.Properties#getInstance} method does not
   * throw an exception.
   */
  public void testInitialisation() throws Exception
  {
    Properties properties = Properties.getInstance();
    assertNotNull( "Ensure that properties object is valid", properties );
    assertNotNull( "Ensure that domain is read", properties.domain );
    assertNotNull( "Ensure that protocol is read", properties.protocol );
    assertNotNull( "Ensure that incoming server is read", properties.incomingServer );
    assertNotNull( "Ensure that smtp server is read", properties.smtpServer );
  }
}
