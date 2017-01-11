package com.sptci.mail;

import java.io.IOException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.internet.InternetAddress;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;

/**
 * Class used to search across the message indices.
 *
 *<p>&copy; Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-05
 * @version $Id: MessageSearcher.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
class MessageSearcher
{
  /**
   * The logger to use for logging messages.
   */
  private final Logger logger = MailSession.logger;

  /**
   * The path at which the search indices are stored.
   */
  private final String path;

  /**
   * The folder manager to use to retrieve the appropriate folder instances
   * when recreating the message objects from the indexed data.
   */
  private FolderManager manager;

  /**
   * The analyser to use to analyse the text.
   */
  private final Analyzer analyser = new StandardAnalyzer();

  /**
   * The date formatter to use to parse stored dates.
   */
  private final SimpleDateFormat sdf = new SimpleDateFormat( "yyyyMMddHHmmss" );

  /**
   * Create a new instance of the searcher for the specified path.
   *
   * @param path The path on which to execute searches.
   * @param manager The {@link #manager} to use.
   */
  MessageSearcher( final String path, final FolderManager manager )
  {
    this.path = path;
    this.manager = manager;
  }

  /**
   * Check to see if the specified message exists in the specified folder.
   * This method is usually used to ensure that duplicates index entries are
   * not created.
   *
   * @param messageId The <code>message-id</code> used to uniquely identify
   *   the message.
   * @param folder The full name of the folder in which the message resides.
   * @return Returns <code>true</code> if the matching message was found in
   *   the index.
   * @throws MessagingException If errors are encountered while parsing the
   *   query, searching the indices or converting the indexed documents
   *   into messages.
   */
  public boolean checkMessage( final String messageId, final String folder )
    throws MessagingException
  {
    boolean result = false;
    Searcher searcher = null;

    try
    {
      searcher = new IndexSearcher( path );
      BooleanQuery query = new BooleanQuery();
      QueryParser parser = new QueryParser(
          Fields.messageId.toString(), analyser );
      query.add( parser.parse( messageId ), BooleanClause.Occur.MUST );

      parser = new QueryParser( Fields.folder.toString(), analyser );
      query.add( parser.parse( folder ), BooleanClause.Occur.MUST );

      Hits hits = searcher.search( query );
      for ( int i = 0; i < hits.length(); ++i )
      {
        if ( hits.doc( i ).get( Fields.folder.toString() ).equals( folder ) )
        {
          result = true;
        }
      }
    }
    catch ( Throwable t )
    {
      throw new MessagingException( t );
    }
    finally
    {
      close( searcher );
    }

    return result;
  }
  
  /**
   * Return a count of all the documents that exist in the specified
   * folder.
   *
   * @param folder The full name of the folder in which to find messages.
   * @return Returns the number of matching documents.
   * @throws MessagingException If errors are encountered while parsing the
   *   query or searching the indices.
   */
  protected int fetchMessageCount( final String folder )
    throws MessagingException
  {
    int count = 0;
    Searcher searcher = null;
    
    try
    {
      searcher = new IndexSearcher( path );
      QueryParser parser =
        new QueryParser( Fields.folder.toString(), analyser );
      Query query = parser.parse( folder );
      
      Hits hits = searcher.search( query );
      count = hits.length();
    }
    catch ( Throwable t )
    {
      throw new MessagingException( t );
    }
    finally
    {
      close( searcher );
    }
    
    return count;
  }

  /**
   * Return a collection of all the documents that exist in the specified
   * folder.
   *
   * @param folder The full name of the folder in which to find messages.
   * @return Returns the collection of matching documents.
   * @throws ParseException If errors are encountered while parsing the
   *   query.
   * @throws IOException If errors are encountered while searching the
   *   indices.
   */
  protected Collection<Document> fetchDocuments( final String folder )
    throws ParseException, IOException
  {
    Collection<Document> messages = new ArrayList<Document>();
    Searcher searcher = null;

    try
    {
      searcher = new IndexSearcher( path );
      QueryParser parser =
        new QueryParser( Fields.folder.toString(), analyser );
      Query query = parser.parse( folder );

      Hits hits = searcher.search( query );
      for ( int i = 0; i < hits.length(); ++i )
      {
        Document document = hits.doc( i );
        // check to get around fact that tokenised folder names lead to
        // imprecise match
        if ( document.get( Fields.folder.toString() ).equals( folder ) )
        {
          messages.add( hits.doc( i ) );
        }
      }
    }
    finally
    {
      close( searcher );
    }

    return messages;
  }

  /**
   * Return a collection of all the messages that exist in the specified
   * folder.
   *
   * @see #fetchDocuments( String )
   * @see #createMessage
   * @param folder The full name of the folder in which to find messages.
   * @return Returns the collection of matching messages.
   * @throws MessagingException If errors are encountered while parsing the
   *   query, searching the indices or converting the indexed documents
   *   into messages.
   */
  public Collection<Message> fetchMessages( final String folder )
    throws MessagingException
  {
    Collection<Message> messages = new ArrayList<Message>();
    Searcher searcher = null;

    try
    {
      searcher = new IndexSearcher( path );
      for ( Document document : fetchDocuments( folder ) )
      {
        messages.add( createMessage( document ) );
      }
    }
    catch ( MessagingException mex ) { throw mex; }
    catch ( Throwable t )
    {
      throw new MessagingException( t );
    }
    finally
    {
      close( searcher );
    }

    return messages;
  }

  /**
   * Return a collection of all the messages that exist in the specified
   * folder.  Returns only results within the specified range of values
   * in the default ordering used in the search index.  The range parameters
   * behave similar to <code>JDO Query.setRange</code>.
   *
   * @see #fetchDocuments( String )
   * @see #createMessage
   * @param folder The full name of the folder in which to find messages.
   * @param start The starting index within the results from which to fetch
   *   results.  This index is included in the results.  Indexes start
   *   from <b>0</b> and not <b>1</b>
   * @param end The ending index within the results till which to fetch
   *   results.  This index is excluded in the results.
   * @return Returns the collection of matching messages.
   * @throws MessagingException If errors are encountered while parsing the
   *   query, searching the indices or converting the indexed documents
   *   into messages.
   */
  public Collection<Message> fetchMessages( final String folder,
      final int start, final int end ) throws MessagingException
  {
    final Collection<Message> messages = new ArrayList<Message>();
    Searcher searcher = null;

    try
    {
      searcher = new IndexSearcher( path );
      int count = 0;
      for ( Document document : fetchDocuments( folder ) )
      {
        if ( ( count >= start ) && ( count < end ) )
        {
          messages.add( createMessage( document ) );
        }

        if ( ++count >= end ) break;
      }
    }
    catch ( MessagingException mex ) { throw mex; }
    catch ( Throwable t )
    {
      throw new MessagingException( t );
    }
    finally
    {
      close( searcher );
    }

    return messages;
  }

  /**
   * Return a collection of all the documents that exist in the specified
   * folder.
   *
   * @param folder The full name of the folder in which to find messages.
   * @param sortField The sort field to use to sort the search results.
   * @return Returns the collection of matching documents.
   * @throws MessagingException If errors are encountered while parsing the
   *   query, searching the indices or converting the indexed documents
   *   into messages.
   */
  protected Collection<Document> fetchDocuments( final String folder,
      final SortFields sortField ) throws MessagingException
  {
    final Collection<Document> messages = new ArrayList<Document>();
    Searcher searcher = null;

    try
    {
      searcher = new IndexSearcher( path );
      QueryParser parser =
        new QueryParser( Fields.folder.toString(), analyser );
      Query query = parser.parse( folder );
      Sort sort = new Sort( sortField.toString() );

      Hits hits = searcher.search( query, sort );
      for ( int i = 0; i < hits.length(); ++i )
      {
        Document document = hits.doc( i );
        // check to get around fact that tokenised folder names lead to
        // imprecise match
        if ( document.get( Fields.folder.toString() ).equals( folder ) )
        {
          messages.add( hits.doc( i ) );
        }
      }
    }
    catch ( Throwable t )
    {
      throw new MessagingException( t );
    }
    finally
    {
      close( searcher );
    }

    return messages;
  }

  /**
   * Return a collection of all the messages that exist in the specified
   * folder.
   *
   * @see #fetchDocuments( String, SortFields )
   * @param folder The full name of the folder in which to find messages.
   * @param sortField The sort field to use to sort the search results.
   * @return Returns the collection of matching documents.
   * @throws MessagingException If errors are encountered while parsing the
   *   query, searching the indices or converting the indexed documents
   *   into messages.
   */
  public Collection<Message> fetchMessages( final String folder,
      final SortFields sortField ) throws MessagingException
  {
    final Collection<Message> messages = new ArrayList<Message>();
    Searcher searcher = null;

    try
    {
      searcher = new IndexSearcher( path );
      for ( Document document : fetchDocuments( folder, sortField ) )
      {
        messages.add( createMessage( document ) );
      }
    }
    catch ( IOException ciex )
    {
      throw new MessagingException( ciex );
    }
    finally
    {
      close( searcher );
    }

    return messages;
  }

  /**
   * Return a collection of all the messages that exist in the specified
   * folder.
   *
   * @see #fetchDocuments( String, SortFields )
   * @param folder The full name of the folder in which to find messages.
   * @param sortField The sort field to use to sort the search results.
   * @param start The starting index within the results from which to fetch
   *   results.  This index is included in the results.  Indexes start
   *   from <b>0</b> and not <b>1</b>
   * @param end The ending index within the results till which to fetch
   *   results.  This index is excluded in the results.
   * @return Returns the collection of matching documents.
   * @throws MessagingException If errors are encountered while parsing the
   *   query, searching the indices or converting the indexed documents
   *   into messages.
   */
  public Collection<Message> fetchMessages( final String folder,
      final SortFields sortField, final int start, final int end )
    throws MessagingException
  {
    final Collection<Message> messages = new ArrayList<Message>();
    Searcher searcher = null;

    try
    {
      searcher = new IndexSearcher( path );
      int count = 0;
      for ( Document document : fetchDocuments( folder, sortField ) )
      {
        if ( ( count >= start ) && ( count < end ) )
        {
          messages.add( createMessage( document ) );
        }

        if ( ++count >= end ) break;
      }
    }
    catch ( IOException ciex )
    {
      throw new MessagingException( ciex );
    }
    finally
    {
      close( searcher );
    }

    return messages;
  }
  
  /**
   * Search for messages that match the conditions specified in the map.
   * The keys of the map are the enums defined in {@link Indexer}.
   * For eg. {@link Indexer.Fields#subject}.
   * 
   * @param conditions The conditions object that specifies the Fields
   *   that are to be searched.
   * @return The document instances that represent the matching messages.
   * @throws MessagingException If errors are encountered while parsing the
   *   query, searching the indices or converting the indexed documents
   *   into messages.
   */
  protected Collection<Document> searchDocuments(
      final SearchConditions conditions ) throws MessagingException
  {
    final Collection<Document> results = new ArrayList<Document>();
    Searcher searcher = null;

    try
    {
      searcher = new IndexSearcher( path );
      BooleanQuery query = new BooleanQuery();

      if ( conditions.getFolder() != null )
      {
        QueryParser parser =
          new QueryParser( Fields.folder.toString(), analyser );
        query.add( parser.parse( conditions.getFolder() ),
            BooleanClause.Occur.MUST );
      }

      ArrayList<String> list = new ArrayList<String>();
      if ( conditions.getSubject() )
      {
        list.add( Fields.subject.toString() );
      }
      if ( conditions.getContent() )
      {
        list.add( Fields.content.toString() );
      }
      if ( conditions.getFrom() )
      {
        list.add( Fields.from.toString() );
      }

      MultiFieldQueryParser mparser = new MultiFieldQueryParser(
          (String[]) list.toArray( new String[]{} ), analyser );
      query.add(
          mparser.parse( conditions.getText() ), BooleanClause.Occur.MUST );
      logger.fine( "Search query: " + query );

      Sort sort = null;
      Hits hits = null;

      if ( conditions.getSort() != null )
      {
        sort = new Sort( conditions.getSort().toString() );
        hits = searcher.search( query, sort );
      }
      else
      {
        hits = searcher.search( query );
      }

      for ( int i = 0; i < hits.length(); ++i )
      {
        Document document = hits.doc( i );
        if ( conditions.getFolder() == null || document.get(
              Fields.folder.toString() ).equals(
              conditions.getFolder() ) )
        {
          results.add( hits.doc( i ) );
        }
      }
    }
    catch ( Throwable t )
    {
      throw new MessagingException( t );
    }
    finally
    {
      close( searcher );
    }

    return results;
  }
  
  /**
   * Search for messages that match the conditions specified in the map.
   * The keys of the map are the contants defined in {@link Indexer}.
   * For eg. {@link Indexer#SUBJECT_KEY}.
   * 
   * @param conditions The conditions object that specifies the Fields
   *   that are to be searched.
   * @return The message instances that represent the matching documents.
   * @see #searchDocuments
   * @throws MessagingException If errors are encountered while parsing the
   *   query, searching the indices or converting the indexed documents
   *   into messages.
   */
  public Collection<Message> search( final SearchConditions conditions )
    throws MessagingException
  {
    final Collection<Message> results = new ArrayList<Message>();
    Searcher searcher = null;

    try
    {
      searcher = new IndexSearcher( path );
      for ( Document document : searchDocuments( conditions ) )
      {
        results.add( createMessage( document ) );
      }
    }
    catch ( IOException ciex )
    {
      throw new MessagingException( ciex );
    }
    finally
    {
      close( searcher );
    }

    return results;
  }
  
  /**
   * Search for messages that match the conditions specified in the map.
   * The keys of the map are the contants defined in {@link Indexer}.
   * For eg. {@link Indexer#SUBJECT_KEY}.
   * 
   * @param conditions The conditions object that specifies the Fields
   *   that are to be searched.
   * @param start The starting index within the results from which to fetch
   *   results.  This index is included in the results.  Indexes start
   *   from <b>0</b> and not <b>1</b>
   * @param end The ending index within the results till which to fetch
   *   results.  This index is excluded in the results.
   * @return The message instances that represent the matching documents.
   * @see #searchDocuments
   * @throws MessagingException If errors are encountered while parsing the
   *   query, searching the indices or converting the indexed documents
   *   into messages.
   */
  public Collection<Message> search( final SearchConditions conditions,
      final int start, final int end ) throws MessagingException
  {
    Collection<Message> results = new ArrayList<Message>();
    Searcher searcher = null;

    try
    {
      searcher = new IndexSearcher( path );
      int count = 0;

      for ( Document document : searchDocuments( conditions ) )
      {
        if ( ( count >= start ) && ( count < end ) )
        {
          results.add( createMessage( document ) );
        }

        if ( ++count >= end ) break;
      }
    }
    catch ( IOException ciex )
    {
      throw new MessagingException( ciex );
    }
    finally
    {
      close( searcher );
    }

    return results;
  }

  /**
   * Destroy this instance of the searcher.  Normally invoked when user
   * logs out of the mail store.
   */
  public void destroy()
  {
    // Not necessary since we are not using a global searcher.
  }

  /**
   * Create a {@link Message} object out of the {@link
   * org.apache.lucene.document.Document} retrieved from the index.
   *
   * @param document The document instance from which the message object
   *   is to be constructed.
   * @return The message object that represents the indexed message.
   * @throws MessagingException If errors are encountered while recreating
   *   the message object from the indexed document.
   */
  protected Message createMessage( final Document document )
    throws MessagingException
  {
    final String uniqueId = document.get( Fields.uniqueId.toString() );
    final String messageId = document.get( Fields.messageId.toString() );
    final String folderName = document.get( Fields.folder.toString() );
    final Folder folder = manager.getFolder( folderName );
    final String from = document.get( Fields.from.toString() );
    final String date = document.get( Fields.date.toString() );

    final Message message = new Message( uniqueId, folder );
    final MessageHeaders headers = new MessageHeaders();
    message.setHeaders( headers );

    try
    {
      headers.setMessageId( messageId );
      headers.setSender( new InternetAddress( from ) );
      headers.setSubject( document.get( Fields.subject.toString() ) );

      if ( date.length() > 0 )
      {
        headers.setReceivedDate( sdf.parse( date ) );
      }
    }
    catch ( Throwable t )
    {
      throw new MessagingException(
          "Error creating message from indexed document", t );
    }

    return message;
  }
  
  /**
   * Set {@link #manager}.
   *
   * @param manager The value to set.
   */
  protected void setManager( final FolderManager manager )
  {
    this.manager = manager;
  }

  /**
   * Close the specified searcher instance.
   *
   * @param searcher The searcher instance to close.
   */
  private void close( final Searcher searcher )
  {
    if ( searcher == null ) return;

    try
    {
      searcher.close();
    }
    catch ( Throwable t )
    {
      logger.log( Level.INFO, "Error closing searcher for path: " +
          path, t );
    }
  }

  /**
   * The data object that is used to capture the search conditions specified
   * by the user.
   */
  public static class SearchConditions
  {
    /**
     * The full name of the folder within which messages are to be searched.
     * Leave empty to search across all folders.
     */
    private String folder;

    /**
     * The search text specified by the user.
     */
    private String text;

    /**
     * A flag indicating that the user wishes to search in the subject field.
     */
    private boolean subject;

    /**
     * A flag indicating that the user wishes to search in the content field.
     */
    private boolean content;

    /**
     * A flag indicating that the user wishes to search in the from field.
     */
    private boolean from;

    /**
     * The field to use to sort the results.  Leave <code>null</code> to
     * use the default relevance based ordering used by search engine.
     */
    private SortFields sort;
    
    /**
     * Returns {@link #folder}.
     *
     * @return String The value/reference of/to folder.
     */
    public String getFolder()
    {
      return folder;
    }
    
    /**
     * Set {@link #folder}.
     *
     * @param folder The value to set.
     */
    public void setFolder( String folder )
    {
      this.folder = folder;
    }
    
    /**
     * Returns {@link #text}.
     *
     * @return String The value/reference of/to text.
     */
    public String getText()
    {
      return text;
    }
    
    /**
     * Set {@link #text}.
     *
     * @param text The value to set.
     */
    public void setText( String text )
    {
      this.text = text;
    }
    
    /**
     * Returns {@link #subject}.
     *
     * @return boolean The value/reference of/to subject.
     */
    public boolean getSubject()
    {
      return subject;
    }
    
    /**
     * Set {@link #subject}.
     *
     * @param subject The value to set.
     */
    public void setSubject( boolean subject )
    {
      this.subject = subject;
    }
    
    /**
     * Returns {@link #content}.
     *
     * @return boolean The value/reference of/to content.
     */
    public boolean getContent()
    {
      return content;
    }
    
    /**
     * Set {@link #content}.
     *
     * @param content The value to set.
     */
    public void setContent( boolean content )
    {
      this.content = content;
    }
    
    /**
     * Returns {@link #from}.
     *
     * @return boolean The value/reference of/to from.
     */
    public boolean getFrom()
    {
      return from;
    }
    
    /**
     * Set {@link #from}.
     *
     * @param from The value to set.
     */
    public void setFrom( boolean from )
    {
      this.from = from;
    }
    
    /**
     * Returns {@link #sort}.
     *
     * @return The value/reference of/to sort.
     */
    public SortFields getSort()
    {
      return sort;
    }
    
    /**
     * Set {@link #sort}.
     *
     * @param sort The value to set.
     */
    public void setSort( final SortFields sort )
    {
      this.sort = sort;
    }
  }
}
