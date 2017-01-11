package com.sptci.mail;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;

/**
 * The base class of all {@link java.util.concurrent.Callable} that is used
 * to queue callable tasks for updating the search index.
 *
 * <p>&copy; Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-06-08
 * @version $Id: Callable.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
abstract class Callable implements java.util.concurrent.Callable
{
  /**
   * The logger to use to log errors or messages to.
   */
  static final Logger logger = MailSession.logger;
  
  /**
   * The date formatter to use to store dates.
   */
  final SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMddHHmmss" );
  
  /**
   * The controller indexer from which all indexer tasks are initiated.
   */
  final Indexer indexer;
  
  /**
   * The searcher used to search through the search indices.
   */
  final MessageSearcher searcher;

  /**
   * Create a new instance of Callable using the specified {@link Indexer}.
   *
   * @param indexer The {@link #indexer} to use.
   * @param searcher The {@link #searcher} to use.
   */
  Callable( final Indexer indexer, final MessageSearcher searcher )
  {
    this.indexer = indexer;
    this.searcher = searcher;
  }
  
  /**
   * Convenience method for {@link Indexer#getWriter}.
   *
   * @return The writer to use to update the search indices.
   */
  IndexWriter getWriter()
  {
    return indexer.getWriter();
  }

  /**
   * Convenience method to access {@link Indexer#flush}.
   */
  void flush()
  {
    indexer.flush();
  }
  
  /**
   * Index the specified message.  Used by the concrete sub-class
   * instances to perform the actual indexing work.
   *
   * <p><b>Note:</b> This method expects that the {@link Indexer#writer} has
   * been properly initialised using {@link Indexer#getWriter} method
   * before being invoked.</p>
   *
   * @param message The message that is to be indexed.
   * @throws MessagingException If errors are encountered while indexing
   *   the message
   * @throws ParseException If errors are encountered while checking the
   *   index for preexisting message.
   * @throws IOException If errors are encountered while reading/writing the
   *   indices.
   */
  void indexMessage( final Message message )
    throws MessagingException, ParseException, IOException
  {
    String messageId = MessageHandler.getMessageId( message );

    if ( ! message.isSet( Flags.Flag.DELETED ) )
    {
      MessageHandler handler = new MessageHandler( message, indexer.session );
      Document document = new Document();
      String uid = indexer.session.getFolderManager().impl.getUID( message );
      if ( uid == null ) uid = "";
      document.add( new Field( Fields.uniqueId.toString(), uid,
            Field.Store.YES, Field.Index.TOKENIZED ) );
      document.add( new Field( Fields.messageId.toString(), messageId,
            Field.Store.YES, Field.Index.TOKENIZED ) );
      document.add( new Field( Fields.folder.toString(),
            message.getFolder().getFullName(),
            Field.Store.YES, Field.Index.TOKENIZED ) );
      
      String subject = "";
      if ( handler.getSubject() != null )
      {
        subject = handler.getSubject();
      }
      document.add( new Field( Fields.subject.toString(), subject,
            Field.Store.YES, Field.Index.TOKENIZED ) );
      document.add( new Field( SortFields.subjectSort.toString(), subject,
            Field.Store.NO, Field.Index.UN_TOKENIZED ) );

      StringBuilder builder = new StringBuilder( 1024 );
      for ( String body : handler.getText() )
      {
        builder.append( body ).append( " " );
      }
      document.add( new Field( Fields.content.toString(),
            builder.toString(),
            Field.Store.NO, Field.Index.TOKENIZED) );

      String from = handler.getSender();
      from = ( from == null ) ? "Unknown" : from;
      document.add( new Field( Fields.from.toString(), from,
            Field.Store.YES, Field.Index.TOKENIZED ) );
      document.add( new Field( SortFields.fromSort.toString(), from,
            Field.Store.NO, Field.Index.UN_TOKENIZED ) );

      String value = "";
      Date date = handler.getDate();
      if ( date != null )
      {
        value = sdf.format( date );
      }
      document.add( new Field( Fields.date.toString(), value,
            Field.Store.YES, Field.Index.TOKENIZED ) );
      document.add( new Field( SortFields.dateSort.toString(), value,
            Field.Store.NO, Field.Index.UN_TOKENIZED ) );

      indexer.writer.addDocument( document ); 
      logger.info( "added index for message: " + messageId );
    }
  }
  
  /**
   * Create the unique identifier for the specified message.  This is
   * created as a concatenation of the <code>message-id</code> and
   * <code>folder</code> name.
   *
   * @see Indexer#getUniqueId( String, String )
   * @param message The message for which the unique identifier is to be
   *   generated.
   * @return The unique identifier for the message.
   * @throws MessagingException If errors are encountered while fetching
   *   properties from the message.
   */
  String getUniqueId( final Message message ) throws MessagingException
  {
    return ( getUniqueId( MessageHandler.getMessageId( message ),
          message.getFolder().getFullName() ) );
  }

  /**
   * Convenience method around {@link Indexer#getUniqueId}.
   *
   * @param messageId The unique id for the message.
   * @param folder The full name of the folder in which the message belongs.
   * @return The unique identifier for the message.
   */
  String getUniqueId( final String messageId, final String folder )
  {
    return indexer.getUniqueId( messageId, folder );
  }
}
