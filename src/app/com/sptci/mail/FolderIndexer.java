package com.sptci.mail;

import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.logging.Level;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * The <code>Callable</code> instance used to queue an indexing operation
 * of all messages in a folder for execution.
 *
 * <p>&copy; Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-04
 * @version $Id: FolderIndexer.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
class FolderIndexer extends Callable
{
  /**
   * The folder whose messageas are to be indexed.
   */
  private Folder folder;

  /**
   * Create a new instance of the object using the specified folder.
   * 
   * @param folder The {@link Indexer.FolderIndexer#folder} value
   *   to use.
   * @param manager The {@link Indexer.FolderIndexer#manager} value
   *   to use.
   */
  FolderIndexer ( final Indexer indexer, final MessageSearcher searcher,
      final Folder folder, final ProtocolFolderManager manager )
  {
    super( indexer, searcher );
    
    try
    {
      this.folder = manager.getUncachedFolder( folder.getFullName() );
    }
    catch ( MessagingException mex )
    {
      logger.log( Level.SEVERE,
          "Error fetching folder: " + folder.getFullName(), mex );
    }
  }

  /**
   * Index the {@link Indexer.FolderIndexer#folder}.
   */
  public Boolean call()
  {
    boolean result = true;
    String name = null;

    try
    {
      logger.fine( "Begin indexing folder: " + folder.getFullName() );
      getWriter();
      long start = System.currentTimeMillis();
      name = folder.getFullName();
      folder.open( Folder.READ_ONLY );

      HashMap<String, Document> map = new HashMap<String,Document>();
      for ( Document document : searcher.fetchDocuments( folder.getFullName() ) )
      {
        map.put( document.get( Fields.messageId.toString()  ), document );
      }

      for( Message message : folder.getMessages() )
      {
        String messageId = MessageHandler.getMessageId( message );
        if ( map.containsKey( messageId ) )
        {
          map.remove( messageId );
        }
        else
        {
          indexMessage( message );
        }
      }

      for ( Document document : map.values() )
      {
        Future<Boolean> future = indexer.executor.submit( new DeIndexer(
            indexer, searcher, document.get( Fields.uniqueId.toString() ) ) );
        indexer.tasks.put( future, "" );
      }

      folder.close( false );
      long end = System.currentTimeMillis();
      logger.fine( "Finished indexing messages in folder: " + name +
            ".  Indexing took: " + ( end - start ) / 1000.0 + " seconds." );
    }
    catch (Throwable t)
    {
      logger.log( Level.FINE, "Error indexing folder: " + name, t );
      result = false;
    }
    finally
    {
      flush();
    }

    return result;
  }
}