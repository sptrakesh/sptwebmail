package com.sptci.mail;

import java.lang.reflect.Method;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import com.sptci.ReflectionUtility;

/**
 * An abstract base class for managing interactions with
 * {@link Folder} instances and their backing {@link javax.mail.Folder}
 * instances (backing only for IMAP stores).
 *
 * <p>&copy; Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-01-16
 * @version $Id: ProtocolFolderManager.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
abstract class ProtocolFolderManager implements Serializable
{
  /**
   * The logger to use to log errors/messages to.
   */
  protected static final Logger logger = MailSession.logger;

  /**
   * The folder manager instance through which this instance is
   * instantiated.
   */
  protected FolderManager manager;

  /**
   * The special <code>INBOX</code> folder for the user.
   */
  protected Folder inbox;

  /**
   * The map used to maintain a cache of all the folders available.
   */
  protected final Map<String,Folder> folders =
    new ConcurrentHashMap<String,Folder>();

  /**
   * Create a new instance of the manager with the specified parent manager.
   * Initialises {@link #local}.
   *
   * @param manager The {@link #manager} to use.
   * @throws MessagingException If errors are encountered while initialising
   *   {@link #local}.
   */
  protected ProtocolFolderManager( FolderManager manager )
    throws MessagingException, com.sptci.mail.MessagingException
  {
    this.manager = manager;
  }

  /**
   * Create a new instance of the manager for the specified parent manager
   * and protocol.
   *
   * @see #initFolders
   * @param manager The {@link #manager} to use.
   * @param protocol The protocol used to interact with the mail store.
   * @throws MessagingException If errors are encountered while fetching
   *   the folders from the store.
   */
  protected static ProtocolFolderManager getInstance(
      FolderManager manager, String protocol )
    throws MessagingException, com.sptci.mail.MessagingException
  {
    ProtocolFolderManager impl = null;
    logger.fine( "Initialising ProtocolFolderManager" );

    if ( protocol.contains( "imap" ) )
    {
      impl = new IMAPFolderManager( manager );
    }
    else if ( protocol.contains( "pop" ) )
    {
      impl = new POP3FolderManager( manager );
    }
    else
    {
      impl = new MboxFolderManager( manager );
    }

    return impl;
  }

  /**
   * Return the delimiter character that separates a Folder's pathname 
   * from the names of immediate subfolders.
   *
   * @return The separator character.
   * @throws MessagingException If errors are encountered while looking
   *   up the separator.
   */
  protected abstract char getSeparator() throws MessagingException;

  /**
   * Convenience method to fetch the <code>INBOX Folder</code>.
   *
   * @return The folder object.
   * @throws MessagingException If errors are encountered while fetching the
   *   folder.
   */
  protected Folder getInbox() throws MessagingException
  {
    return inbox;
  }

  /**
   * Method to fetch the <code>Default Folder</code> for the mail store.
   *
   * @return The folder object.
   * @throws MessagingException If errors are encountered while fetching the
   *   folder.
   */
  protected Folder getDefaultFolder() throws MessagingException
  {
    return manager.getDefaultFolder();
  }

  /**
   * Returns the folder associated with the full name specified.  If the
   * specified folder does not exist, it is created.
   * 
   * @param name  The fully qualified name of the folder.
   * @see #create
   * @throws MessagingException If errors are encountered while fetching
   *   or creating the folder.
   */
  protected abstract Folder getFolder( String name ) throws MessagingException;

  /**
   * Returns the folder associated with the full name specified.  This
   * differs from {@link #getFolder} in that a new copy of the
   * folder is returned instead of the cached instance.
   * 
   * @param name  The fully qualified name of the folder.
   * @throws MessagingException If errors are encountered while fetching
   *   or creating the folder.
   */
  protected abstract Folder getUncachedFolder( String name )
    throws MessagingException;

  /**
   * Return the folders that are available to the current user.  Returns
   * a sorted view of {@link #folders}.
   *
   * <p><b>Note:</b> Modifying the returned collection will not affect the
   * mail store.</p>
   *
   * @return The sorted map of folders.
   */
  protected Collection<Folder> getFolders()
  {
    Map<String,Folder> map = new TreeMap<String,Folder>();
    map.putAll( folders );

    Collection<Folder> collection = new ArrayList<Folder>( map.size() );
    collection.addAll( map.values() );

    return collection;
  }

  /**
   * Return the unique identifier for the message added by the store.
   * <code>IMAP</code> stores assign a <code>long</code> value, while
   * <code>POP3</code> stores assign a {@link java.lang.String} value.
   * For consistency, this method returns a <code>String</code> value.
   *
   * @param message The message whose unique identifier is to be retrieved.
   * @return The string representation of the unique identified.  Returns
   *   <code>null</code> if the store does not return a value.
   * @throws MessagingException If errors are encountered while fetching
   *   the UID.
   */
  protected String getUID( Message message ) throws MessagingException
  {
    String value = null;

    Method method = ReflectionUtility.fetchMethod( message.getFolder(),
        "getUID", Message.class );
    if ( method != null )
    {
      try
      {
        value = "" + method.invoke( message.getFolder(), message );
      }
      catch ( Throwable t )
      {
        logger.log( Level.SEVERE, "Error invoking Folder.getUID( Message )", t );
      }
    }

    return value;
  }

  /**
   * Rename the folder to the new name specified.  The location of the
   * folder in the over-all folder hierarchy is maintained.
   *
   * @param name The new name to assign to the folder.  It is assumed
   *   that the folder hierarchy is not modified.
   * @param folder The fully qualified name of the folder that is to be
   *   renamed.
   * @return The renamed folder instance.
   * @throws MessagingException If errors are encountered while renaming the
   *   folder.  Also thrown if the rename operation fails.  JavaMail API
   *   does not throw an exception, it just returns <code>false</code>.
   */
  protected Folder renameTo( final String name, final String folder )
    throws MessagingException
  {
    Folder newFolder = null;

    try
    {
      final Folder oldFolder = folders.get( folder );
      final Folder parent = oldFolder.getParent();
      final String fullName = parent.getFullName() + getSeparator() + name;
      newFolder = getDefaultFolder().getFolder( fullName );

      if ( ! newFolder.exists() )
      {
        boolean open = false;

        if ( oldFolder.isOpen() )
        {
          oldFolder.close( false );
          open = true;
        }

        boolean result = oldFolder.renameTo( newFolder );
        if ( ! result )
        {
          throw new MessagingException( "Unable to rename folder: " +
              oldFolder.getName() + " to: " + name );
        }

        folders.remove( folder );
        folders.put( fullName, newFolder );

        if ( open ) newFolder.open( Folder.READ_ONLY );
      }
    }
    catch( MessagingException mex )
    {
      logger.log( Level.SEVERE,
          "Error renaming folder: " + folder + " to: " + name, mex );
      throw mex;
    }

    return newFolder;
  }

  /**
   * Perform any clean up action required to cleanly disconnect from the
   * mail store.
   */
  public void destroy() {}

  /**
   * Return the sub folders for the specified folder.  Returns a sorted
   * view of any available sub folders.
   *
   * @param  parent folder The folder whose children are to be returned.
   * @return The sorted map of sub folders.  The <code>key</code> to the
   *   map contains the name of the folder, while the <code>value</code>
   *   represents the sub folder.
   * @throws MessagingException If errors are encountered while fetching
   *   the sub folders.
   */
  protected abstract Collection<Folder> getSubFolders( Folder parent )
    throws MessagingException;

  /**
   * Create a new top-level folder with the specified name.
   *
   * @param name The name of the new folder that is to be created.
   * @return The newly created folder.
   * @throws MessagingException If errors are encountered while creating
   *   the folder.
   */
  protected abstract Folder create( String name ) throws MessagingException;

  /**
   * Create a new folder with the specified name as the child of the
   * specified parent folder.
   *
   * @param name The name of the new folder that is to be created.
   * @param parent The folder under which the new child is to be created.
   * @return The newly created folder.
   * @throws MessagingException If errors are encountered while creating
   *   the folder.
   */
  protected abstract Folder create( String name, Folder parent )
    throws MessagingException;

  /**
   * Delete the folder identified by the specified full name from the
   * store.  This method does not fail if the specified folder does not
   * exist.
   *
   * @see #delete( Folder )
   * @param folder The full name of the folder that is to be deleted.
   * @throws MessagingException If errors are encountered while deleting
   *   the folder from the store.
   */
  protected abstract void delete( String folder ) throws MessagingException;

  /**
   * Delete the specified folder from the cache and from the store.
   *
   * @param folder The folder that is to be deleted.
   * @throws MessagingException If errors are encountered while deleting
   *   the folder from the store.
   */
  protected abstract void delete( Folder folder ) throws MessagingException;

  /**
   * Fetch the message uniquely identified by its <code>UID</code> specified.
   *
   * @param uid The <code>UID</code> to use to fetch the message from the
   *   store.
   * @param folder The full name of the folder in which the message exists.
   * @throws MessagingException If errors are encountered while fetching
   *   the message from the store.
   */
  protected abstract Message getMessage( String uid, String folder )
    throws MessagingException;
}
