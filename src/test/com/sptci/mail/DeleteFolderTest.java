package com.sptci.mail;

import java.io.File;

import static junit.framework.Assert.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for deleing a {@link Folder} instance.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-18
 * @version $Id: DeleteFolderTest.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class DeleteFolderTest extends TestCase
{
  public static Test suite()
  {
    return new TestSuite( DeleteFolderTest.class );
  }

  /**
   * Test deletion of {@link CreateFolderTest#parent} which should lead
   * to deletion of {@link CreateFolderTest#child} as well.
   */
  public void testDelete() throws Exception
  {
    Folder folder = CreateFolderTest.parent;
    CreateMailSessionTest.session.getFolderManager().delete( folder );
    Thread.sleep( 2000 ); // Give enough time for deindexer to run
  }
}
