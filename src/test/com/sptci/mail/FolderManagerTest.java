package com.sptci.mail;

import java.util.Collection;

import static junit.framework.Assert.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for testing the methods in {@link
 * com.sptci.mail.FolderManager} class.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-16
 * @version $Id: FolderManagerTest.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class FolderManagerTest extends TestCase
{ 
  public static Test suite()
  {
    return new TestSuite( FolderManagerTest.class );
  }

  /**
   * Test retrieval of all folders available for the user.
   */
  public void testGetFolders() throws Exception
  {
    MailSession session = CreateMailSessionTest.session;
    FolderManager manager = session.getFolderManager();
    
    assertNotNull( "FolderManager cannot be null", manager );
    assertTrue( "Test for non-zero number of folders",
        manager.getFolders().size() > 0 );
  }

  /**
   * Test retrieval of a folder by full name.
   */
  public void testGetFolder() throws Exception
  {
    MailSession session = CreateMailSessionTest.session;
    FolderManager manager = session.getFolderManager();
    
    Folder folder = CreateFolderTest.parent;
    Folder f = manager.getFolder( folder.getFullName() );
    assertEquals( "Test for same folder retrieved", f, folder );
  }

  /**
   * Test retrieval of the inbox folder.
   *
   * <p><b>Note:</b> This method checks for non-zero messages in INBOX.
   * This may not be true for your test account.</p>
   */
  public void testGetInbox() throws Exception
  {
    MailSession session = CreateMailSessionTest.session;
    FolderManager manager = session.getFolderManager();
    
    Folder inbox = manager.getInbox();
    assertTrue( "Non-critical test for messages in inbox",
        inbox.getMessageCount() > 0 );
  }

  /**
   * Test retrieval of sub-folders of the specified folder.
   */
  public void testGetSubFolders() throws Exception
  {
    MailSession session = CreateMailSessionTest.session;
    FolderManager manager = session.getFolderManager();
    
    Collection<Folder> folders = manager.getSubFolders( CreateFolderTest.parent );
    assertEquals(
        "Check to ensure CreateFolderTest.parent has two children in getSubFolders",
        folders.size(), 2 );
  }
  
  /** Test renaming a folder. */
  public void testRenameTo() throws Exception
  {
    MailSession session = CreateMailSessionTest.session;
    FolderManager manager = session.getFolderManager();
    
    final String name = "Renamed Folder";
    Folder folder = CreateFolderTest.rename;
    folder = manager.rename( name, folder );
    assertTrue( "Checking that name has changed",
        ! CreateFolderTest.renameName.equals( folder.getName() ) );
  }
}
