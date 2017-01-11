package com.sptci.mail;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.mail.Folder;
import javax.mail.MessagingException;

/**
 * A class that is used to manage all interactions with <code>IMAP 
 * Folder</code> objects.  Also used to maintain a cache of folders to
 * ensure that the same folder instances are used by the various components 
 * of the application.
 *
 * <p>&copy; Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-11
 * @version $Id: IMAPFolderManager.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
class IMAPFolderManager extends UIDFolderManager
{
  /**
   * The folder manager used to manage a local cache if configured.
   * 
   * @see Properties#localCache
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
  protected IMAPFolderManager( FolderManager manager )
    throws MessagingException, com.sptci.mail.MessagingException
  {
    super( manager );

    if ( Properties.getInstance().localCache )
    {
      local = new MboxFolderManager( manager );
    }

    initFolders();
    if ( local != null ) synchroniseFolders();
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
    return getDefaultFolder().getSeparator();
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

    folder = getDefaultFolder().getFolder( name );
    folder.create( Folder.HOLDS_MESSAGES );
    folders.put( name, folder );
    if ( local != null ) local.create( name );

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
    if ( parent == null ) return create( name );

    if ( ! folders.containsKey( parent.getFullName() ) )
    {
      create( parent.getName(), parent.getParent() );
    }

    String fullName = parent.getFullName() + getSeparator() + name;
    Folder folder = folders.get( fullName );

    if ( folder == null )
    {
      folder = getDefaultFolder().getFolder( fullName );
      folder.create( javax.mail.Folder.HOLDS_MESSAGES );
      folders.put( name, folder );
      if ( local != null ) local.create( name, parent );
    }

    return folder;
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
    if ( folder.isOpen() ) folder.close( false );
    folder.delete( true );
    folders.remove( key );
    if ( local != null ) local.delete( folder );
  }

  /**
   * Add the specified folder and any child folders associated with it 
   * to {@link #folders}.
   *
   * @param folder The parent folder that is to be added.
   * @throws MessagingException If errors are encountered while fetching
   *   the folders from the store.
   */
  protected void addFolder( Folder folder ) throws MessagingException
  {
    if ( folder.getName() != null && folder.getName().length() > 0 )
    {
      logger.fine( "Added folder: " + folder.getFullName() );
      folders.put( folder.getFullName(), folder );
      if ( local != null )
      {
        local.create( folder.getName(), folder.getParent() );
      }
      manager.session.getMessageIndexer().index( folder, this );
    }

    if ( FolderManager.INBOX.equalsIgnoreCase( folder.getFullName() ) )
    {
      inbox = folder;
    }

    for ( Folder child : folder.list() )
    {
      addFolder( child );
    }
  }

  /**
   * Synchronise the store folders with the local folders.
   *
   * @see MaildirFolderManager#folders
   * @see MaildirFolderManager#create
   * @see MaildirFolderManager#delete
   * @see MaildirFolderManager#synchronise
   * @throws MessagingException If errors are encountered while
   *   synchronising the folders.
   */
  protected void synchroniseFolders() throws MessagingException
  {
    // Ensure all store folders exist in local repository
    for ( Map.Entry<String,Folder> entry : folders.entrySet() )
    {
      if ( entry.getKey().length() > 0 &&
          ! local.folders.containsKey( entry.getKey() ) )
      {
        manager.session.getMessageIndexer().index( entry.getValue(), this );
        logger.fine( "Adding and synchronisinglocal folder: " + entry.getKey() );
        local.create( entry.getValue().getName(), entry.getValue().getParent() );
      }

      local.synchronise( entry.getValue() );
    }

    // Delete any folders that have been removed from the store.
    for ( String key : local.folders.keySet() )
    {
      if ( key.length() > 0 && key.charAt( 0 ) == local.getSeparator() )
      {
        key = key.substring( 1 );
      }

      if ( folders.containsKey( key ) )
      {
        Folder f = local.folders.get( key );
        logger.fine( "Synchronising local folder: " + key );
        local.synchronise( f );
      }
      else if ( key.length() > 0 && ! FolderManager.INBOX.equalsIgnoreCase( key ) )
      {
        logger.fine( "Deleting local folder: " + key );
        local.delete( key );
      }
    }
  }

  /**
   * If local maildir caching is enabled, destroy any tasks executing.
   *
   * @see MaildirFolderManager#destroy
   */
  @Override
  public void destroy()
  {
    if ( local != null ) local.destroy();
  }
}
