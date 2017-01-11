package com.sptci.mail;

import java.util.logging.Level;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

/**
 * The {@link java.util.concurrent.Callable} instance used to queue a
 * de-indexing operation for execution.
 *
 * <p>&copy; Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-04
 * @version $Id: DeIndexer.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
class DeIndexer extends Callable
{
  /**
   * The unique id that is a concatenation of the <code>folder name</code>
   * and <code>message-id</code> values for a message.
   */
  private final String uniqueId;

  /**
   * Create a new instance of the object using the specified unique id.
   * 
   * @param indexer The {@link #indexer} to use.
   * @param searcher The {@link #searcher} to use.
   * @param uniqueId The {@link #uniqueId} to use.
   */
  DeIndexer( final Indexer indexer, final MessageSearcher searcher,
      final String uniqueId )
  {
    super( indexer, searcher );
    this.uniqueId = uniqueId;
  }

  /**
   * De-index the message represented by {@link 
   * Indexer.DeIndexer#uniqueId}.
   */
  public Boolean call()
  {
    boolean result = true;

    try
    {
      IndexWriter writer = getWriter();
      int start = writer.docCount();
      Term term = new Term( Fields.uniqueId.toString(), uniqueId );
      writer.deleteDocuments( term );
      int end = writer.docCount();
      logger.info( "Deleted document: " + uniqueId +
            " leading to deleting " + ( end - start ) + " indices" );
    }
    catch (Throwable t)
    {
      result = false;
      logger.log( Level.WARNING, "Error deleting indexes for message: " +
            uniqueId, t );
    }
    finally
    {
      flush();
    }

    return result;
  }
}