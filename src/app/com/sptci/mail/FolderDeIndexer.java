package com.sptci.mail;

import java.util.logging.Level;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

/**
 * The <code>Callable</code> instance used to queue a de-indexing
 * operation for execution on a folder.
 *
 * <p>&copy; Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-04
 * @version $Id: FolderDeIndexer.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
class FolderDeIndexer extends Callable
{
  /** The full name of the folder that is to be de-indexed. */
  private final String name;

  /**
   * Create a new instance of the object using the specified full name.
   * 
   * @param indexer The {@link #indexer} to use.
   * @param searcher The {@link #searcher} to use.
   * @param name The {@link #name} to use.
   */
  FolderDeIndexer( final Indexer indexer, final MessageSearcher searcher,
      final String name )
  {
    super( indexer, searcher );
    this.name = name;
  }

  /**
   * De-index the messages that belong to the folder identified by
   * {@link #name}.
   */
  public Boolean call()
  {
    boolean result = true;

    try
    {
      IndexWriter writer = getWriter();
      int start = writer.docCount();

      for ( Document document : searcher.fetchDocuments( name ) )
      {
        Term term = new Term( Fields.uniqueId.toString(),
              document.get( Fields.uniqueId.toString() ) );
        writer.deleteDocuments( term );
      }

      int end = writer.docCount();
      logger.info( "Deleted documents in folder: " + name +
            " leading to deleting " + ( end - start ) + " indices" );
    }
    catch ( Throwable t )
    {
      result = false;
      logger.log( Level.WARNING, "Error deleting indexes for folder: " +
            name, t );
    }
    finally
    {
      flush();
    }

    return result;
  }
}
