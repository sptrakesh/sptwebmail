package com.sptci.mail;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sptci.io.FileUtilities;
import static com.sptci.io.FileUtilities.FILE_SEPARATOR;

/**
 * A class that is used to manage all interactions with {@link Folder}
 * objects.  This class serves as the broker between the local repository
 * and the mail store.
 *
 * <p>&copy; Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-01-11
 * @version $Id: FolderManager.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class FolderManager implements Serializable
{
  /**
   * The logger to use to log errors/messages to.
   */
  protected static final Logger logger = MailSession.logger;

  /**
   * The root directory relative to the user's data directoy under which
   * all folders and message content are stored.
   *
   * {@value}
   */
  public static final String ROOT_DIRECTORY = "data";

  /**
   * The name of the special <code>INBOX</code> folder to fetch from the
   * mail store.
   */
  public static final String INBOX = "INBOX";

  /**
   * The mail session for the current user.
   */
  protected final MailSession session;

  /**
   * The special <code>INBOX</code> folder for the user.
   */
  protected Folder inbox;

  /**
   * The implementation class.  This is initialised based upon the value
   * of the protocol specified in the constructor.
   */
  protected ProtocolFolderManager impl;

  /**
   * The local folder manager that is used to manage all folders.
   */
  protected LocalFolderManager local;

  /**
   * Create a new instance of the manager for the specified session.
   * Cannot be instantiated.
   *
   * @param session The {@link #session} to use.
   */
  protected FolderManager( final MailSession session )
  {
    this.session = session;
  }

  /**
   * Create a new instance of the manager for the specified session and
   * protocol.
   *
   * @param session The {@link #session} to use.
   * @param protocol The protocol used to interact with the mail store.
   * @throws MessagingException If errors are encountered while fetching
   *   the folders from the store.
   */
  public FolderManager( final MailSession session, final String protocol )
    throws MessagingException
  {
    this( session );

    logger.fine( "Initialising " + protocol + " FolderManager" );

    try
    {
      impl = ProtocolFolderManager.getInstance( this, protocol );
    }
    catch ( Throwable t )
    {
      throw new MessagingException( t );
    }

    local = new LocalFolderManager();
  }

  /**
   * Return the delimiter character that separates a Folder's pathname 
   * from the names of immediate subfolders.
   *
   * @return The separator character.
   * @throws MessagingException If errors are encountered while looking
   *   up the separator.
   */
  protected char getSeparator() throws MessagingException
  {
    try
    {
      return impl.getSeparator();
    }
    catch ( Throwable t )
    {
      throw new MessagingException( t );
    }
  }

  /**
   * Convenience method to fetch the <code>INBOX Folder</code>.
   *
   * @return The folder object.
   * @throws MessagingException If errors are encountered while fetching the
   *   folder.
   */
  public Folder getInbox() throws MessagingException
  {
    Folder folder = null;
    
    try
    {
      folder = local.getFolder( impl.getInbox() );
    }
    catch ( Throwable t )
    {
      throw new MessagingException( t );
    }

    return folder;
  }

  /**
   * Returns the folder associated with the full name specified.  If the
   * specified folder does not exist, it is created.
   * 
   * @param name  The fully qualified name of the folder.
   * @return The requested folder object.
   * @throws MessagingException If errors are encountered while fetching
   *   or creating the folder.
   */
  protected javax.mail.Folder getMailFolder( final String name )
    throws javax.mail.MessagingException
  {
    return impl.getFolder( name );
  }

  /**
   * Returns the folder associated with the full name specified.  If the
   * specified folder does not exist, it is created.
   * 
   * @param name  The fully qualified name of the folder.
   * @see #create
   * @return The requested folder object.
   * @throws MessagingException If errors are encountered while fetching
   *   or creating the folder.
   */
  public Folder getFolder( final String name ) throws MessagingException
  {
    Folder folder = null;

    try
    {
      folder = local.getFolder( impl.getFolder( name ) );
    }
    catch ( Throwable t )
    {
      throw new MessagingException( t );
    }

    return folder;
  }

  /**
   * Return the folders that are available to the current user.  Returns
   * the folders returned by {@link #impl}.
   *
   * @return The sorted collection of folders.
   * @throws MessagingException If errors are encountered while fetching
   *   the folders.
   */
  public Collection<Folder> getFolders() throws MessagingException
  {
    Collection<javax.mail.Folder> collection = impl.getFolders();
    Collection<Folder> folders = new ArrayList<Folder>( collection.size() );

    for ( javax.mail.Folder folder : collection )
    {
      folders.add( local.getFolder( folder ) );
    }

    return folders;
  }

  /**
   * Return the sub folders for the specified folder.  Returns a sorted
   * view of any available sub folders.
   *
   * @param  parent folder The folder whose children are to be returned.
   * @return The sorted collection of sub folders.
   * @throws MessagingException If errors are encountered while fetching
   *   the sub folders.
   */
  public Collection<Folder> getSubFolders( final Folder parent )
    throws MessagingException
  {
    Collection<javax.mail.Folder> collection = null;

    try
    {
      collection =
        impl.getSubFolders( impl.getFolder( parent.getFullName() ) );
    }
    catch ( Throwable t )
    {
      throw new MessagingException( t );
    }

    Collection<Folder> folders = new ArrayList<Folder>( collection.size() );

    for ( javax.mail.Folder folder : collection )
    {
      folders.add( local.getFolder( folder ) );
    }

    return folders;
  }

  /**
   * Add the specified folder to the internal cached list of folders.
   *
   * @param folder The folder that is to be added.
   * @throws MessagingException If errors are encountered while adding the
   *   folder.
  public void add( Folder folder ) throws MessagingException
  {
    getFolders().put( folder.getFullName(), folder );
  }
   */

  /**
   * Create a new top-level folder with the specified name.
   *
   * @param name The name of the new folder that is to be created.
   * @return The newly created folder.
   * @throws MessagingException If errors are encountered while creating
   *   the folder.
   */
  public Folder create( final String name ) throws MessagingException
  {
    javax.mail.Folder folder = null;

    try
    {
      folder = impl.create( name );
    }
    catch ( Throwable t )
    {
      throw new MessagingException( t );
    }

    return local.getFolder( folder );
  }

  /**
   * Create a new folder with the specified name which is a child of the
   * specified folder..
   *
   * @param name The name of the new folder that is to be created.
   * @param parent The parent folder under which this folder is to be
   *   created.
   * @return The newly created folder.
   * @throws MessagingException If errors are encountered while creating
   *   the folder.
   */
  public Folder create( final String name, final Folder parent )
    throws MessagingException
  {
    javax.mail.Folder folder = null;

    try
    {
      folder = impl.create( name, impl.getFolder( parent.getFullName() ) );
    }
    catch ( Throwable t )
    {
      throw new MessagingException( t );
    }

    return local.getFolder( folder );
  }

  /**
   * Rename the specified folder as specified.  Note that the location of
   * the folder in the over-all folder hierarchy is maintained.
   *
   * @param name The new name to assign to the folder.
   * @param folder The folder that is to be renamed.
   * @return The renamed folder object.
   * @throws MessagingException If errors are encountered while renaming
   *   the folder.
   */
  public Folder rename( final String name, final Folder folder )
    throws MessagingException
  {
    javax.mail.Folder imapFolder = null;

    try
    {
      local.delete( folder.getFullName() );

      imapFolder = impl.getFolder( folder.getFullName() );
      session.getMessageIndexer().deIndex( imapFolder );

      imapFolder = impl.renameTo( name, folder.getFullName() );
    }
    catch ( Throwable t )
    {
      throw new MessagingException( t );
    }

    session.getMessageIndexer().index( imapFolder, impl );
    return local.getFolder( imapFolder );
  }

  /**
   * Delete the folder identified by the specified full name from the
   * store.  Also deletes all associated messages from the search index.
   * This method does not fail if the specified folder does not exist.
   *
   * @see #delete( Folder )
   * @param folder The full name of the folder that is to be deleted.
   * @throws MessagingException If errors are encountered while deleting
   *   the folder from the store.
   */
  public void delete( final String folder ) throws MessagingException
  {
    try
    {
      javax.mail.Folder imapFolder = impl.getFolder( folder );
      session.getMessageIndexer().deIndex( imapFolder );

      impl.delete( folder );
      local.delete( folder );
    }
    catch ( Throwable t )
    {
      throw new MessagingException( t );
    }
  }

  /**
   * Delete the specified folder from the cache and from the store.
   *
   * @param folder The folder that is to be deleted.
   * @throws MessagingException If errors are encountered while deleting
   *   the folder from the store.
   */
  public void delete( final Folder folder ) throws MessagingException
  {
    delete( folder.getFullName() );
  }

  /**
   * Method to fetch the <code>Default Folder</code> for the mail store.
   *
   * @return The folder object.
   * @throws MessagingException If errors are encountered while fetching the
   *   folder.
   */
  protected javax.mail.Folder getDefaultFolder()
    throws javax.mail.MessagingException
  {
    return session.getStore().getDefaultFolder();
  }
  
  /**
   * Return the total number of messages in the specified folder.
   *
   * @see #getMessageCount( String )
   * @param folder The folder for which message count is to be retrieved.
   * @throws MessagingException If errors are encountered while fetching
   *   the message count for the folder.
   */
  public int getMessageCount( final Folder folder )
    throws MessagingException
  {
    return getMessageCount( folder.getFullName() );
  }
  
  /**
   * Return the total number of messages in the specified folder.
   *
   * @see MessageSearcher#fetchMessageCount
   * @param folder The fully qualified name of the folder for which 
   *   message count is to be retrieved.
   * @throws MessagingException If errors are encountered while fetching
   *   the message count for the folder.
   */
  public int getMessageCount( final String folder )
    throws MessagingException
  {
    final MessageSearcher searcher = session.getMessageSearcher();
    return searcher.fetchMessageCount( folder );
  }

  /**
   * Return the messages in the specified folder.
   *
   * @see #getMessages( String )
   * @param folder The folder from which messages are to be retrieved.
   * @throws MessagingException If errors are encountered while fetching
   *   the messages for the folder.
   */
  public Collection<Message> getMessages( final Folder folder )
    throws MessagingException
  {
    return getMessages( folder.getFullName() );
  }
  
  /**
   * Return the messages in the specified folder.  Return only the messages
   * between the specified numbers.
   *
   * @see #getMessages( String, int, int )
   * @param folder The folder from which messages are to be retrieved.
   * @param start The start index of the messages to retrieve.  Indexing
   *   starts from <code>0</code>.
   * @param end The ending index (non-inclusive) of the messages to retrieve.
   * @throws MessagingException If errors are encountered while fetching
   *   the messages for the folder.
   */
  public Collection<Message> getMessages( final Folder folder,
      final int start, final int end ) throws MessagingException
  {
    return getMessages( folder.getFullName(), start, end );
  }

  /**
   * Return the messages in the specified folder.
   *
   * @see MessageSearcher#fetchMessages( String )
   * @param folder The full name of the folder from which messages are to
   *   be retrieved.
   * @throws MessagingException If errors are encountered while fetching
   *   the messages for the folder.
   */
  public Collection<Message> getMessages( final String folder )
    throws MessagingException
  {
    final MessageSearcher searcher = session.getMessageSearcher();
    return searcher.fetchMessages( folder );
  }
  
  /**
   * Return the messages in the specified folder.  Return only the messages
   * between the specified numbers.
   * 
   * <p><b>Note:</b> It is quite likely that the messages returned by repeated
   * invocations of this method over-lap, since the indices of the messages
   * in the store may have changed between invocations.</p>
   *
   * @see MessageSearcher#fetchMessages( String, int, int )
   * @param folder The full name of the folder from which messages are to
   *   be retrieved.
   * @param start The start index of the messages to retrieve.  Indexing
   *   starts from <code>0</code>.
   * @param end The ending index (non-inclusive) of the messages to retrieve.
   * @throws MessagingException If errors are encountered while fetching
   *   the messages for the folder.
   */
  public Collection<Message> getMessages( final String folder,
      final int start, final int end ) throws MessagingException
  {
    final MessageSearcher searcher = session.getMessageSearcher();
    return searcher.fetchMessages( folder, start, end );
  }

  /**
   * Return the messages in the specified folder.  Return only the messages
   * between the specified numbers.
   *
   * @see #getMessages( String, SortFields, int, int )
   * @param folder The folder from which messages are to be retrieved.
   * @param sortField The {@link com.sptci.mail.SortFields} instance to use
   *   to sort the messages.
   * @param start The start index of the messages to retrieve.  Indexing
   *   starts from <code>0</code>.
   * @param end The ending index (non-inclusive) of the messages to retrieve.
   * @throws MessagingException If errors are encountered while fetching
   *   the messages for the folder.
   */
  public Collection<Message> getMessages( final Folder folder,
      final SortFields sortField, final int start, final int end )
    throws MessagingException
  {
    return getMessages( folder.getFullName(), sortField, start, end );
  }
  
  /**
   * Return the messages in the specified folder.  Return only the messages
   * between the specified numbers.
   * 
   * <p><b>Note:</b> It is quite likely that the messages returned by repeated
   * invocations of this method over-lap, since the indices of the messages
   * in the store may have changed between invocations.</p>
   *
   * @see MessageSearcher#fetchMessages( String, SortFields, int, int )
   * @param folder The full name of the folder from which messages are to
   *   be retrieved.
   * @param sortField The {@link com.sptci.mail.SortFields} instance to use
   *   to sort the messages.
   * @param start The start index of the messages to retrieve.  Indexing
   *   starts from <code>0</code>.
   * @param end The ending index (non-inclusive) of the messages to retrieve.
   * @throws MessagingException If errors are encountered while fetching
   *   the messages for the folder.
   */
  public Collection<Message> getMessages( final String folder,
      final SortFields sortField, final int start, final int end )
    throws MessagingException
  {
    final MessageSearcher searcher = session.getMessageSearcher();
    return searcher.fetchMessages( folder, sortField, start, end );
  }

  /**
   * Return the message identified by its <code>UID</code>.  The UID
   * specified is the normalised string equivalent.
   *
   * @see #getMessage( String, String )
   * @param uid The <code>UID</code> to use to fetch the message from the
   *   store.
   * @param folder The folder in which the message exists.
   * @return The requested message.
   * @throws MessagingException If errors are encountered while fetching
   *   the message.
   */
  public Message getMessage( String uid, Folder folder )
    throws MessagingException
  {
    return getMessage( uid, folder.getFullName() );
  }

  /**
   * Return the message identified by its <code>UID</code>.  The UID
   * specified is the normalised string equivalent.
   *
   * @see #getMessage( String, String )
   * @param uid The <code>UID</code> to use to fetch the message from the
   *   store.
   * @param folder The full name of the folder in which the message exists.
   * @return The requested message.
   * @throws MessagingException If errors are encountered while fetching
   *   the message.
   */
  public Message getMessage( String uid, String folder )
    throws MessagingException
  {
    Message message = null;

    try
    {
      javax.mail.Message msg = impl.getMessage( uid, folder );
      message = local.getMessage( msg, impl );
    }
    catch ( MessagingException mex ) { throw mex; }
    catch ( Throwable t )
    {
      throw new MessagingException(
          "Error fetching message with uid: " + uid + ".", t );
    }

    return message;
  }

  /**
   * Perform any clean up action required to cleanly disconnect from the
   * mail store(s).
   */
  public void destroy()
  {
    impl.destroy();
  }
}
