package com.sptci.mail;

import java.util.logging.Level;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

/**
 * The <code>Callable</code> instance used to queue a folder rename
 * operation for execution on a folder.
 *
 * <p>&copy; Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2008-03-03
 * @version $Id: FolderMover.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
class FolderMover extends Callable
{
  /** The full name of the folder that is to be renamed. */
  private final String oldName;

  /** The full name of the renamed folder. */
  private final String newName;

  /**
   * Create a new instance of the object using the specified full names.
   * 
   * @param indexer The {@link #indexer} to use.
   * @param searcher The {@link #searcher} to use.
   * @param oldName The {@link #oldName} value to use.
   * @param newName The {@link #newName} value to use.
   */
  FolderMover( final Indexer indexer, final MessageSearcher searcher,
      final String oldName, final String newName )
  {
    super( indexer, searcher );
    this.oldName = oldName;
    this.newName = newName;
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

      for ( Document document : searcher.fetchDocuments( oldName ) )
      {
        Term term = new Term( Fields.uniqueId.toString(),
              document.get( Fields.uniqueId.toString() ) );
        document.removeField( Fields.folder.toString() );
        document.add( new Field( Fields.folder.toString(),
            newName, Field.Store.YES, Field.Index.TOKENIZED ) );
        writer.updateDocument( term, document );
      }

      int end = writer.docCount();
      logger.info( "Updated folderName from: " + oldName +
          " to: " + newName + " in " + ( end - start ) + " documents" );
    }
    catch ( Throwable t )
    {
      result = false;
      logger.log( Level.WARNING,
          "Error updating indexes for renaming folder: " + oldName, t );
    }
    finally
    {
      flush();
    }

    return result;
  }
}
