package com.sptci.mail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Folder;
import javax.mail.Message;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;

/**
 * Indexer used to index messages.  Use to index new messages as well as to
 * delete indices for deleted messages.
 * 
 * <p>&copy; Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-04
 * @version $Id: Indexer.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class Indexer
{
  /**
   * The logger to use to log errors and messages.
   */
  static final Logger logger = MailSession.logger;

  /**
   * The executor service used to maintain the indices.
   */
  final ExecutorService executor = Executors.newSingleThreadExecutor();

  /**
   * The analyser to use to analyse the text.
   */
  private final Analyzer analyzer = new StandardAnalyzer();

  /**
   * A map of <code>Future</code> tasks that have been queued to {@link
   * #executor} by instance.
   */
  final Map<Future<Boolean>,String> tasks =
    new ConcurrentHashMap<Future<Boolean>,String>();

  /**
   * The index writer to use to update the search indices.
   */
  IndexWriter writer;

  /**
   * The path under which the index files are stored.
   */
  private final String path;

  /**
   * The mail session associated with the current user.
   */
  final MailSession session;

  /**
   * Create a new instance of the indexer using the specified values.
   * Initialises {@link #writer} using the specified path.
   *
   * @param path The path to use to store the indices.
   * @param session The {@link #session} to use.
   */
  public Indexer( final String path, final MailSession session )
  {
    this.path = path;
    this.session = session;
    initWriter();
  }

  /**
   * Destroy this instance of the indexer.  Normally invoked when user
   * logs out of the mail store.  Cancels any indexing {@link #tasks} in
   * progress and optimises and closes the {@link #writer}.
   * 
   * @see #closeWriter
   */
  public void destroy()
  {
    try
    {
      for ( Future<Boolean> future : tasks.keySet() )
      {
        future.cancel( true );
        tasks.remove( future );
      }

      executor.shutdownNow();
      closeWriter();
      logger.fine( "Safely destroyed indexer" );
    }
    catch ( Throwable t )
    {
      logger.log( Level.WARNING, "Error closing writer", t );
    }
  }

  /**
   * Index the specified folder.
   *
   * @see FolderIndexer
   * @param folder The folder whose messages are to be indexed.
   * @param manager The folder manager to use to interact with the folder.
   */
  public void index( final Folder folder, final ProtocolFolderManager manager )
  {
    Future<Boolean> future = executor.submit(
        new FolderIndexer( this, session.getMessageSearcher(), folder, manager ) );
    tasks.put( future, "" );
  }
  
  /**
   * Index the specified message.
   *
   * @see Indexer
   * @param message The message that is to be indexed.
   */
  public void index( final Message message )
  {
    Future<Boolean> future = executor.submit(
        new MessageIndexer( this, session.getMessageSearcher(), message ) );
    tasks.put( future, "" );
  }
  
  /**
   * Remove the index associated with specified <code>message-id</code>.
   * This method should be used after deleting a message as well as when
   * it is determined that a previously indexed message has been purged
   * from the mail store.
   *
   * @see DeIndexer
   * @see #getUniqueId
   * @param messageId The message-id to use to fetch the index.
   * @param folder The full name of the folder in which this message exists.
   */
  public void deIndex( final String messageId, final String folder )
  {
    Future<Boolean> future = executor.submit( new DeIndexer(
        this, session.getMessageSearcher(), getUniqueId( messageId, folder ) ) );
    tasks.put( future, "" );
  }
  
  /**
   * Remove the indices associated with specified folder name.
   * This method should be used before deleting a folder.  Recursively
   * deletes the indices for all child folders of the specified folder.
   *
   * @see FolderDeIndexer
   * @param folder The folder that along with its child folders and all
   *   included messages are to be deindexed.
   */
  public void deIndex( final Folder folder )
  {
    try
    {
      for ( Folder child : folder.list() )
      {
        deIndex( child );
      }
      
      Future<Boolean> future = executor.submit( new FolderDeIndexer(
          this, session.getMessageSearcher(), folder.getFullName() ) );
      tasks.put( future, "" );
    }
    catch ( Throwable t )
    {
      logger.log( Level.WARNING, "Error deindexing folder: " +
          folder.getFullName() + " or its children", t );
    }
  }

  /**
   * Initialise the {@link #writer} instance that is to be used to maintain
   * the indices.
   */
  private void initWriter()
  {
    try
    {
      File file = new File( path );
      boolean create = ( file.exists() ? false : true );
      writer = new IndexWriter( path, analyzer, create );
    }
    catch ( Throwable t )
    {
      logger.log( Level.SEVERE, "Error initialising writer for path: " +
          path, t );
    }
  }

  /**
   * Return the {@link #writer} instance that is to be used to maintain
   * the indices.
   *
   * @return Returns the opened index writer instance.
   */
  protected IndexWriter getWriter()
  {
    return writer;
  }

  /**
   * Close the {@link #writer} after updating the indices.
   */
  private void closeWriter()
  {
    try
    {
      writer.optimize();
      writer.close();
    }
    catch ( Throwable t )
    {
      logger.log( Level.SEVERE, "Error closing writer for path: " +
          path, t );
    }
  }

  /**
   * Flush the {@link #writer} after updating the indices.  As long as the
   * {@link #writer} is opened in <code>autoCommit</code> mode, readers
   * will pick up flushed updated to the index.
   */
  protected void flush()
  {
    try
    {
      writer.flush();
    }
    catch ( Throwable t )
    {
      logger.log( Level.SEVERE, "Error flushing writer for path: " +
          path, t );
    }
  }

  /**
   * Create the unique identifier for the specified <code>message-id</code>
   * and <code>folder</code> name.
   *
   * @param messageId The unique id for the message.
   * @param folder The full name of the folder in which the message belongs.
   * @return The unique identifier for the message.
   * @throws MessagingException If errors are encountered while fetching
   *   properties from the message.
   */
  protected String getUniqueId( final String messageId, final String folder )
  {
    String result = "";
    int id = ( folder + ":_-_:" + messageId ).hashCode();
    if ( id < 0 )
    {
      result = "0" + Math.abs( id );
    }
    else
    {
      result = String.valueOf( id );
    }

    return result;
  }
}
