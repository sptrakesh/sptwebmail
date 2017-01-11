package com.sptci.mail;

import java.io.Serializable;

import java.util.Collection;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;

import com.sun.mail.pop3.POP3Folder;

import static com.sptci.io.FileUtilities.FILE_SEPARATOR;

/**
 * A class that is used to manage all interactions with <code>POP3 
 * Folder</code> objects.  <code>POP3</code> allows only the <code>INBOX</code>
 * folder.  All other folders managed by the application are instances of
 * {@link Folder}.
 *
 * <p>&copy; Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-23
 * @version $Id: POP3FolderManager.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
class POP3FolderManager extends ProtocolFolderManager
{
  /**
   * The local folder manager used to simulate IMAP style folders that are
   * used to organise messages.
   */
  protected MboxFolderManager local;

  /**
   * Create a new instance of the manager for the specified store.
   *
   * @see #initFolders
   * @param manager The {@link #manager} to use.
   * @throws MessagingException If errors are encountered while fetching
   *   the folders from the store.
   */
  protected POP3FolderManager( FolderManager manager )
    throws MessagingException, com.sptci.mail.MessagingException
  {
    super( manager );
    Folder in = manager.getDefaultFolder().getFolder( FolderManager.INBOX );

    local = new MboxFolderManager( manager );
    local.synchronise( in );
    inbox = local.getInbox();

    folders.put( inbox.getFullName(), inbox );
    manager.session.getMessageIndexer().index( inbox, this );

    for ( Folder folder : local.getFolders() )
    {
      folders.put( folder.getFullName(), folder );
      manager.session.getMessageIndexer().index( folder, this );
    }
  }

  /**
   * Return the delimiter character that separates a Folder's pathname 
   * from the names of immediate subfolders.
   *
   * @return The separator character.
   * @throws MessagingException If errors are encountered while fetching
   *   the separator character.
   */
  @Override
  protected char getSeparator() throws MessagingException
  {
    return local.getSeparator();
  }

  /**
   * Returns the folder associated with the full name specified.  If the
   * specified folder does not exist, it is created.
   * 
   * @see MaildirFolderManager#getFolder
   * @param name  The fully qualified name of the folder.
   * @see #create
   * @throws MessagingException If errors are encountered while fetching
   *   or creating the folder.
   */
  @Override
  protected Folder getFolder( String name ) throws MessagingException
  {
    if ( FolderManager.INBOX.equalsIgnoreCase( name ) )
    {
      return inbox;
    }

    return local.getFolder( name );
  }

  /**
   * Returns an uncached folder associated with the full name specified.
   * 
   * @param name  The fully qualified name of the folder.
   * @throws MessagingException If errors are encountered while fetching
   *   or creating the folder.
   */
  @Override
  protected Folder getUncachedFolder( String name )
    throws MessagingException
  {
    Folder folder = null;

    if ( FolderManager.INBOX.equalsIgnoreCase( name ) )
    {
      folder = manager.getDefaultFolder().getFolder( FolderManager.INBOX );
    }
    else
    {
      folder = local.getUncachedFolder( name );
    }

    return folder;
  }

  /**
   * Return the sub folders for the specified folder.  Returns a sorted
   * view of any available sub folders.
   *
   * @see MaildirFolderManager#getSubFolders
   * @param  parent folder The folder whose children are to be returned.
   * @return The sorted map of sub folders.  The <code>key</code> to the
   *   map contains the name of the folder, while the <code>value</code>
   *   represents the sub folder.
   * @throws MessagingException If errors are encountered while fetching
   *   the sub folders.
   */
  @Override
  protected Collection<Folder> getSubFolders( Folder parent )
    throws MessagingException
  {
    return local.getSubFolders( parent );
  }

  /**
   * Create a new top-level folder with the specified name.
   *
   * @see MaildirFolderManager.create( String, Folder )
   * @param name The name of the new folder that is to be created.
   * @return The newly created folder.
   * @throws MessagingException If errors are encountered while creating
   *   the folder.
   */
  @Override
  protected Folder create( String name ) throws MessagingException
  {
    return local.create( name );
  }

  /**
   * Create a new folder with the specified name as a child of the
   * specified parent folder.
   *
   * @see MaildirFolderManager.create( String, Folder )
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
    return local.create( name, parent );
  }

  /**
   * Delete the folder identified by the specified full name from the
   * store.  This method does not fail if the specified folder does not
   * exist.
   *
   * @see MaildirFolderManager#delete( String )
   * @param folder The full name of the folder that is to be deleted.
   * @throws MessagingException If errors are encountered while deleting
   *   the folder from the store.
   */
  @Override
  protected void delete( String folder ) throws MessagingException
  {
    local.delete( folder );
  }

  /**
   * Delete the specified folder from the cache and from the store.
   *
   * @see MaildirFolderManager#delete( Folder )
   * @param folder The folder that is to be deleted.
   * @throws MessagingException If errors are encountered while deleting
   *   the folder from the store.
   */
  @Override
  protected void delete( Folder folder ) throws MessagingException
  {
    local.delete( folder );
  }

  /**
   * Rename the folder to the new name specified.
   *
   * @param name The new name to assign to the folder.  It is assumed
   *   that the folder hierarchy is not modified.
   * @param folder The full name of the folder that is to be renamed.
   * @return The renamed folder instance.
   * @throws MessagingException If errors are encountered while renaming the
   *   folder.
   */
  @Override
  protected Folder renameTo( final String name, final String folder )
    throws MessagingException
  {
    return local.renameTo( name, folder );
  } 

  /**
   * Fetch the message uniquely identified by its <code>UID</code> specified.
   *
   * @see MaildirFolderManager#getMessage
   * @param uid The <code>UID</code> to use to fetch the message from the
   *   store.
   * @param folder The full name of the folder in which the message exists.
   * @throws MessagingException If errors are encountered while fetching
   *   the message from the store.
   */
  protected Message getMessage( String uid, String folder )
    throws MessagingException
  {
    return local.getMessage( uid, folder );
  }
}
