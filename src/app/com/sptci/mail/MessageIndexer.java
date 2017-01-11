package com.sptci.mail;

import java.util.logging.Level;
import javax.mail.Message;

/**
 * The {@link Callable} instance used to queue an indexing operation
 * on a message for execution.
 *
 * <p>&copy; Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-06-08
 * @version $Id: MessageIndexer.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
class MessageIndexer extends Callable
{
  /**
   * The message that is to be indexed.
   */
  private final Message message;

  /**
   * Create a new instance of the object using the specified message.
   * 
   * @param message The {@link Indexer.Indexer#message} value
   *   to use.
   */
  MessageIndexer( final Indexer indexer, final MessageSearcher searcher,
      final Message message )
  {
    super( indexer, searcher );
    this.message = message;
  }

  /**
   * Index the {@link #message}.
   *
   * @see MessageSearcher#checkMessage
   * @see #indexMessage
   */
  public Boolean call()
  {
    boolean result = true;

    try
    {
      getWriter();
      String messageId = MessageHandler.getMessageId( message );

      if ( searcher.checkMessage(
              messageId, message.getFolder().getFullName() ) )
      {
        //logger.fine( "Skipping indexed message: " + messageId );
        return result;
      }

      indexMessage( message );
    } 
    catch ( Throwable t )
    {
      result = false;
      logger.log( Level.FINE, "Error indexing message", t );
    } 
    finally
    {
      flush();
    }

    return result;
  }
}
