package com.sptci.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Serializable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.UIDFolder.FetchProfileItem;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;

import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.SearchTerm;

import net.fortuna.mstor.MStorFolder;
import net.fortuna.mstor.MStorStore;

import com.sptci.io.FileUtilities;
import static com.sptci.io.FileUtilities.FILE_SEPARATOR;

/**
 * A class that is used to manage all interactions with <code>Local 
 * Folder</code> objects.  Also used to maintain a cache of folders to
 * ensure that the same folder instances are used by the various components 
 * of the application.
 *
 * <p>&copy; Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-15
 * @version $Id: MboxFolderManager.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
class MboxFolderManager extends UIDFolderManager
{
  /**
   * The name of the directory that serves as the root of the mbox store.
   *
   * {@value}
   */
  public static final String MBOX = "mbox";

  /**
   * The separator character used to delimit folders.
   */
  protected char separator = ' ';

  /**
   * The root directory under which all the folders are stored in the
   * local cache.
   */
  protected String rootDirectory;

  /**
   * The special <code>MailDir</code> store used to manage the local
   * folders.
   */
  protected MStorStore store;

  /**
   * The executor service used to synchronise the local folders from the
   * mail store.
   */
  protected final ExecutorService executor =
    Executors.newSingleThreadExecutor();

  /**
   * A map of <code>Future</code> tasks that have been queued to {@link
   * #executor} by instance.
   */
  protected final Map<Future<Boolean>,String> tasks =
    new ConcurrentHashMap<Future<Boolean>,String>();

  /**
   * Create a new instance of the manager for the specified session.
   *
   * @param session The {@link #session} value to set.
   * @throws MessagingException If errors are encountered while fetching
   *   the folders from the store.
   */
  protected MboxFolderManager( FolderManager manager )
    throws MessagingException, com.sptci.mail.MessagingException
  {
    super( manager );
    String url = "mstor:" + getRootPath();
    store = (MStorStore) manager.session.session.getStore( new URLName( url ) );
    store.connect();
    initFolders();
  }

  /**
   * Destroy any {@link #tasks} scheduled in the {@link #executor}.
   */
  @Override
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
      store.close();
      logger.fine( "Safely destroyed synchroniser" );
    }
    catch ( Throwable t )
    {
      logger.log( Level.WARNING, "Error destroying folder synchroniser", t );
    }
  }

  /**
   * Return the delimiter character that separates a Folder's pathname 
   * from the names of immediate subfolders.
   *
   * @return The separator character.
   * @throws MessagingException If errors are encountered while looking
   *   up the separator.
   */
  @Override
  protected char getSeparator() throws MessagingException
  {
    if ( separator == ' ' )
    {
      Folder f = store.getFolder( "blah" );
      separator = f.getSeparator();
    }

    return separator;
  }

  /**
   * Method to fetch the <code>Default Folder</code> for the mail store.
   *
   * @return The folder object.
   * @throws MessagingException If errors are encountered while fetching the
   *   folder.
   */
  @Override
  protected Folder getDefaultFolder() throws MessagingException
  {
    return store.getDefaultFolder();
  }

  /**
   * Return the root path under which all folders are stored for the user.
   *
   * @return The fully qualified name of the directory under which all the
   *   folders are stored.
   * @throws MessagingException If errors are encountered while computing
   *   the path.
   */
  protected String getRootPath() throws com.sptci.mail.MessagingException
  {
    if ( rootDirectory == null )
    {
      StringBuilder file = new StringBuilder();
      file.append( Properties.getInstance().dataDirectory );
      file.append( FILE_SEPARATOR );
      file.append( manager.session.getUser() );
      file.append( FILE_SEPARATOR );
      file.append( MBOX );
      file.append( FILE_SEPARATOR );
      rootDirectory = file.toString();

      try
      {
        File f = new File( rootDirectory );
        if ( ! f.exists() )
        {
          f.mkdirs();
        }
      }
      catch ( Throwable t )
      {
        throw new com.sptci.mail.MessagingException(
            "Unable to create root directory: " + rootDirectory, t );
      }

      logger.finer( "rootDirectory: " + rootDirectory );
    }

    return rootDirectory;
  }

  /**
   * Create a new top-level folder with the specified name.
   *
   * @param name The name of the new folder that is to be created.
   * @return The newly created folder.
   * @throws MessagingException If errors are encountered while creating
   *   the folder.
   */
  @Override
  protected Folder create( String name ) throws MessagingException
  {
    Folder folder = folders.get( name );
    if ( folder != null ) return folder;

    logger.fine( "Creating local: " + name );
    folder = store.getFolder( name );
    folder.create( Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS );
    folders.put( name, folder );

    return folder;
  }

  /**
   * Create a new folder with the specified name as a child of the
   * specified parent folder.
   *
   * @param name The name of the new folder that is to be created.
   * @param parent The folder under which the new folder is to be created.
   * @return The newly created folder.
   * @throws MessagingException If errors are encountered while creating
   *   the folder.
   */
  @Override
  protected Folder create( String name, Folder parent )
    throws MessagingException
  {
    if ( parent == null || FolderManager.INBOX.equalsIgnoreCase(
          parent.getName() ) ) return create( name );

    if ( parent.getName().length() > 0 &&
        ! folders.containsKey( parent.getFullName() ) )
    {
      create( parent.getName(), parent.getParent() );
    }

    if ( FolderManager.INBOX.equalsIgnoreCase( name ) )
    {
      if ( inbox == null ) inbox = store.getFolder( name );
      return inbox;
    }

    String fullName = parent.getFullName() + getSeparator() + name;
    Folder folder = folders.get( fullName );
    if ( folder != null ) return folder;

    logger.fine( "Creating local: " + fullName + " with parent: " + parent.getName() );
    folder = store.getFolder( fullName );
    folder.create( Folder.HOLDS_MESSAGES );
    folders.put( fullName, folder );

    return folder;
  }

  /**
   * Add the specified folder and any child folders associated with it 
   * to {@link #folders}.
   *
   * @param folder The parent folder that is to be added.
   * @throws MessagingException If errors are encountered while fetching
   *   the folders from the store.
   */
  @Override
  protected void addFolder( Folder folder ) throws MessagingException
  {
    if ( folder.getFullName() != null && folder.getFullName().length() > 0 
        && ! FolderManager.INBOX.equalsIgnoreCase( folder.getFullName() ) )
    {
      logger.fine( "Added folder name: " + folder.getName() +
          ", fullName: " + folder.getFullName() );
      folders.put( folder.getFullName(), folder );
    }

    inbox = store.getFolder( FolderManager.INBOX );
    if ( ! folders.containsKey( inbox.getName() ) )
    {
      folders.put( inbox.getName(), inbox );
    }

    for ( Folder child : folder.list() )
    {
      addFolder( child );
    }
  }

  /**
   * Synchronise the messages in the specified folder with the cached
   * messages in the local repository.
   *
   * @param imapFolder The folder that is to be synchronised.
   * @throws MessagingException If errors are encountered while fetching
   *   the message counts.
   */
  protected void synchronise( Folder imapFolder ) throws MessagingException
  {
    Future<Boolean> future =
      executor.submit( new FolderSynchroniser( imapFolder ) );
    tasks.put( future, "" );
  }

  /**
   * Synchronise the new message in the specified folder with the local
   * repository.
   *
   * @see #synchMessages( Folder, SearchTerm )
   * @param folder The folder that is to be synchronised.
   * @throws MessagingException If errors are encountered while fetching
   *   the messages or synchronising messages.
   */
  protected void synchNewMessages( Folder imapFolder )
    throws MessagingException
  {
    SearchTerm recent = new FlagTerm( new Flags( Flags.Flag.RECENT ), true );
    SearchTerm seen = new FlagTerm( new Flags( Flags.Flag.SEEN ), false );
    SearchTerm term = new AndTerm( recent, seen );
    synchMessages( imapFolder, term );
  }

  /**
   * Synchronise the new message in the specified folder with the local
   * repository.
   *
   * @see #synchMessages( Folder, SearchTerm )
   * @param folder The folder that is to be synchronised.
   * @throws MessagingException If errors are encountered while fetching
   *   the messages or synchronising messages.
   */
  protected void synchUnreadMessages( Folder imapFolder )
    throws MessagingException
  {
    SearchTerm term = new FlagTerm( new Flags( Flags.Flag.SEEN ), false );
    synchMessages( imapFolder, term );
  }

  /**
   * Synchronise the messages in the specified folder with the local
   * repository.
   *
   * @see #synchMessages( Folder, SearchTerm )
   * @param folder The folder that is to be synchronised.
   * @throws MessagingException If errors are encountered while fetching
   *   the messages or synchronising messages.
   */
  protected void synchMessages( Folder imapFolder )
    throws MessagingException
  {
    SearchTerm term = new FlagTerm( new Flags( Flags.Flag.DELETED ), false );
    synchMessages( imapFolder, term );
  }

  /**
   * Synchronise the messages in the specified folder with the local
   * repository.
   *
   * @see #add( Message[], Folder )
   * @see #delete( Message[], Folder )
   * @param folder The folder that is to be synchronised.
   * @param term The search term to use to fetch messages from the folder.
   * @throws MessagingException If errors are encountered while fetching
   *   the messages or synchronising messages.
   */
  protected void synchMessages( Folder imapFolder, SearchTerm term )
    throws MessagingException
  {
    HashMap<String,MimeMessage> source = new HashMap<String,MimeMessage>();
    HashMap<String,MimeMessage> local = new HashMap<String,MimeMessage>();
    Folder localFolder = create( imapFolder.getName(), imapFolder.getParent() );
    FetchProfile fetchProfile = new FetchProfile();
    fetchProfile.add( FetchProfileItem.UID );
    fetchProfile.add( "Message-Id" );

    boolean close = false;

    if ( ! localFolder.isOpen() )
    {
      localFolder.open( Folder.READ_ONLY );
      close = true;
    }

    Message[] msgs = imapFolder.search( term );
    imapFolder.fetch( msgs, fetchProfile );
    for( Message message : msgs )
    {
      MimeMessage mm = (MimeMessage) message;
      String id = ( mm.getMessageID() != null ) ? mm.getMessageID() : getUID( message );
      source.put( mm.getMessageID(), mm );
    }

    msgs = localFolder.search( term );
    localFolder.fetch( msgs, fetchProfile );
    for ( Message message : msgs )
    {
      MimeMessage mm = (MimeMessage) message;
      local.put( mm.getMessageID(), mm );
    }

    // Add missing messages
    LinkedList<Message> messages = new LinkedList<Message>();
    for ( Map.Entry<String,MimeMessage> entry : source.entrySet() )
    {
      if ( ! local.containsKey( entry.getKey() ) )
      {
        messages.add( entry.getValue() );
      }
    }

    if ( ! messages.isEmpty() )
    {
      logger.fine( "Adding " + messages.size() + " messages" );
      add( messages.toArray( new MimeMessage[]{} ), localFolder );
    }

    // Remove deleted messages
    messages = new LinkedList<Message>();
    for ( Map.Entry<String,MimeMessage> entry : local.entrySet() )
    {
      if ( ! source.containsKey( entry.getKey() ) )
      {
        messages.add( entry.getValue() );
      }
    }

    if ( ! messages.isEmpty() )
    {
      logger.fine( "Deleting " + messages.size() + " messages" );
      delete( messages.toArray( new Message[] {} ), localFolder );
    }

    if ( close ) localFolder.close( true );
  }

  /**
   * Add the specified message to the local repository.
   *
   * <p><b>Note:</b> The folder specified must be a local folder.</p>
   *
   * @param message The message that is to be added.
   * @param folder The folder to which the message is to be added.
   * @throws MessagingException If errors are encountered while adding
   *   the specified message.
   */
  protected void add( Message message, Folder folder )
    throws MessagingException
  {
    boolean open = false;
    if ( folder.isOpen() )
    {
      folder.close( false );
      open = true;
    }

    folder.open( Folder.READ_WRITE );
    folder.appendMessages( new Message[] { message } );
    folder.close( false );

    if ( open ) folder.open( Folder.READ_ONLY );
  }

  /**
   * Add the specified messages to the local repository.
   *
   * <p><b>Note:</b> The folder specified must be a local folder.</p>
   *
   * @param messages The messages that are to be added.
   * @param folder The folder to which the messages are to be added.
   * @throws MessagingException If errors are encountered while adding
   *   the specified messages.
   */
  protected void add( Message[] messages, Folder folder )
    throws MessagingException
  {
    boolean open = false;
    if ( folder.isOpen() )
    {
      folder.close( false );
      open = true;
    }

    folder.open( Folder.READ_WRITE );
    folder.appendMessages( messages );
    folder.close( false );

    if ( open ) folder.open( Folder.READ_ONLY );
  }

  /**
   * Delete the specified messages from the local repository.
   *
   * <p><b>Note:</b> The folder specified must be a local folder.</p>
   *
   * @param messages The messages that are to be deleted.
   * @param folder The folder from which the messages are to be deleted.
   * @throws MessagingException If errors are encountered while deleting
   *   the specified messages.
   */
  protected void delete( Message[] messages, Folder folder )
    throws MessagingException
  {
    boolean open = false;
    if ( folder.isOpen() )
    {
      folder.close( false );
      open = true;
    }

    folder.open( Folder.READ_WRITE );
    folder.setFlags( messages, new Flags( Flags.Flag.DELETED ), true );
    folder.close( true );

    if ( open ) folder.open( Folder.READ_ONLY );
  }

  /**
   * Delete the specified folder from the cache and from the store.
   *
   * @param folder The folder that is to be deleted.
   * @throws MessagingException If errors are encountered while deleting
   *   the folder from the store.
   */
  @Override
  protected void delete( Folder folder ) throws MessagingException
  {
    String key = folder.getFullName();

    if ( folder instanceof MStorFolder )
    {
      if ( folder.isOpen() ) folder.close( false );
      folder.delete( true );
      folders.remove( key );
    }
    else
    {
      Folder f = folders.get( key );
      if ( f != null )
      {
        if ( f.isOpen() ) f.close( false );
        f.delete( true );
        folders.remove( key );
      }
    }
  }

  /**
   * The <code>Callable</code> instance used to queue a folder synching
   * operation of messages in a specified IMAP folder for execution.
   */
  protected class FolderSynchroniser implements Callable
  {
    /**
     * The folder whose messageas are to be indexed.
     */
    protected Folder imapFolder;

    /**
     * Create a new instance of the object using the specified folder.
     *
     * @param imapFolder The {@link
     *   MboxFolderManager.FolderSynchroniser#imapFolder} value to use.
     */
    protected FolderSynchroniser( Folder imapFolder )
    {
      this.imapFolder = imapFolder;
    }

    /**
     * Synchronise the local folder with messages in {@link
     * MboxFolderManager.FolderSynchroniser#folder}.
     *
     * @see #synchNewMessages
     * @see #synchUnreadMessages
     * @see #synchMessages
     */
    public Boolean call()
    {
      String folderName = null;
      boolean result = true;

      try
      {
        if ( imapFolder == null ) return false;
        folderName = imapFolder.getFullName();
        Folder folder = create( imapFolder.getName(), imapFolder.getParent() );
        logger.fine( "Synchronising messages for folder name: " +
            folder.getName() + ", fullName: " + folderName );

        boolean close = false;
        if ( ! imapFolder.isOpen() )
        {
          close = true;
          imapFolder.open( javax.mail.Folder.READ_ONLY );
        }

        if ( FolderManager.INBOX.equalsIgnoreCase( imapFolder.getFullName() ) )
        {
          synchMessages( imapFolder );
        }
        else
        {
          if ( imapFolder.getNewMessageCount() !=
              folder.getNewMessageCount() )
          {
            synchNewMessages( imapFolder );
          }

          if ( imapFolder.getUnreadMessageCount() !=
              folder.getUnreadMessageCount() )
          {
            synchUnreadMessages( imapFolder );
          }

          int imapTotal = imapFolder.getMessageCount() -
            imapFolder.getDeletedMessageCount();
          int total = folder.getMessageCount() -
            folder.getDeletedMessageCount();
          if ( total != imapTotal )
          {
            synchMessages( imapFolder );
          }
        }

        if ( close ) imapFolder.close( false );
        logger.fine( "Finished synchronising messages for folder " + folderName );
      }
      catch ( Throwable t )
      {
        logger.log( Level.SEVERE,
            "Error synchronising messages for folder: " + folderName, t );
        result = false;
      }

      return result;
    }
  }
}
