package com.sptci.mail;

import java.util.Collection;

import static junit.framework.Assert.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import static com.sptci.io.FileUtilities.FILE_SEPARATOR;

/**
 * Unit test for search features supported by the system.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-06-04
 * @version $Id: MessageSearcherTest.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class MessageSearcherTest extends TestCase
{
  static int messageCount;
  static int start = 0;
  static int end = 5;

  public static Test suite()
  {
    return new TestSuite( MessageSearcherTest.class );
  }

  /**
   * Test fetching messages without sorting.
   */
  public void testFetchMessages() throws Exception
  {
    MailSession session = CreateMailSessionTest.session;
    FolderManager manager = session.getFolderManager();
    MessageSearcher searcher = session.getMessageSearcher();

    try{ Thread.sleep( 5000 ); } catch ( Throwable t ) {}
    Collection docs = searcher.fetchMessages(
        manager.getInbox().getFullName() );
    messageCount = docs.size();

    assertTrue( "Ensure messages found in inbox", docs.size() > 0 );
    System.out.format( "Found %d messages in INBOX%n", docs.size() );
  }

  /**
   * Test fetching messages without sorting within a specified range.
   */
  public void testFetchMessagesByRange() throws Exception
  {
    MailSession session = CreateMailSessionTest.session;
    FolderManager manager = session.getFolderManager();
    MessageSearcher searcher = session.getMessageSearcher();

    Collection docs = searcher.fetchMessages(
        manager.getInbox().getFullName(), start, end );

    assertTrue( "Ensure messages found in inbox", docs.size() > 0 );
    assertTrue( "Ensure messages within range",
        docs.size() <= ( end - start ) );
  }

  /**
   * Test fetching messages sorted by sender address.
   */
  public void testFetchMessagesByFrom() throws Exception
  {
    MailSession session = CreateMailSessionTest.session;
    FolderManager manager = session.getFolderManager();
    MessageSearcher searcher = session.getMessageSearcher();

    Collection docs = searcher.fetchMessages(
        manager.getInbox().getFullName(), SortFields.fromSort );

    assertTrue( "Ensure messages found in inbox", docs.size() > 0 );
    assertEquals( "Ensure message count same in sorted view",
        docs.size(), messageCount );
  }

  /**
   * Test fetching messages sorted by sender address and within specified
   * range.
   */
  public void testFetchMessagesByFromAndRange() throws Exception
  {
    MailSession session = CreateMailSessionTest.session;
    FolderManager manager = session.getFolderManager();
    MessageSearcher searcher = session.getMessageSearcher();

    Collection docs = searcher.fetchMessages(
        manager.getInbox().getFullName(),
        SortFields.fromSort, start, end );

    assertTrue( "Ensure messages found in inbox", docs.size() > 0 );
    assertTrue( "Ensure messages within range",
        docs.size() <= ( end - start ) );
  }

  /**
   * Test fetching messages sorted by subject.
   */
  public void testFetchMessagesBySubject() throws Exception
  {
    MailSession session = CreateMailSessionTest.session;
    FolderManager manager = session.getFolderManager();
    MessageSearcher searcher = session.getMessageSearcher(); 
    Collection docs = searcher.fetchMessages(
        manager.getInbox().getFullName(), SortFields.subjectSort );

    assertTrue( "Ensure messages found in inbox", docs.size() > 0 );
    assertEquals( "Ensure message count same in sorted view",
        docs.size(), messageCount );
  }

  /**
   * Test fetching messages sorted by subject and within range.
   */
  public void testFetchMessagesBySubjectAndRange() throws Exception
  {
    MailSession session = CreateMailSessionTest.session;
    FolderManager manager = session.getFolderManager();
    MessageSearcher searcher = session.getMessageSearcher(); 
    Collection docs = searcher.fetchMessages(
        manager.getInbox().getFullName(),
        SortFields.subjectSort, start, end );

    assertTrue( "Ensure messages found in inbox", docs.size() > 0 );
    assertTrue( "Ensure messages within range",
        docs.size() <= ( end - start ) );
  }

  /**
   * Test fetching messages sorted by subject.
   */
  public void testFetchMessagesByDate() throws Exception
  {
    MailSession session = CreateMailSessionTest.session;
    FolderManager manager = session.getFolderManager();
    MessageSearcher searcher = session.getMessageSearcher(); 
    Collection docs = searcher.fetchMessages(
        manager.getInbox().getFullName(), SortFields.dateSort );

    assertTrue( "Ensure messages found in inbox", docs.size() > 0 );
    assertEquals( "Ensure message count same in sorted view",
        docs.size(), messageCount );
  }

  /**
   * Test fetching messages sorted by subject and within range.
   */
  public void testFetchMessagesByDateAndRange() throws Exception
  {
    MailSession session = CreateMailSessionTest.session;
    FolderManager manager = session.getFolderManager();
    MessageSearcher searcher = session.getMessageSearcher(); 
    Collection docs = searcher.fetchMessages(
        manager.getInbox().getFullName(),
        SortFields.dateSort, start, end );

    assertTrue( "Ensure messages found in inbox", docs.size() > 0 );
    assertTrue( "Ensure messages within range",
        docs.size() <= ( end - start ) );
  }
}
