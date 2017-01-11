package com.sptci.mail;

import static junit.framework.Assert.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for creating a new {@link Folder} instance.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-16
 * @version $Id: CreateFolderTest.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class CreateFolderTest extends TestCase
{
  static final String parentName = "JUnitTestFolderTest";
  static final String childName = "Child One";
  static final String renameName = "Rename Me";
  static Folder parent;
  static Folder child;
  static Folder rename;

  public static Test suite()
  {
    return new TestSuite( CreateFolderTest.class );
  }

  /**
   * Test creation of {@link #parent} and {@link #child}.
   */
  public void testCreate() throws Exception
  {
    MailSession session = CreateMailSessionTest.session;
    FolderManager manager = session.getFolderManager();
    parent = manager.create( parentName );
    child = manager.create( childName, parent );
    rename = manager.create( renameName, parent );
    manager.create( childName, rename );

    assertTrue( "Ensure child folder child of parent",
        child.getParent().equals( parent ) );
  }
}
