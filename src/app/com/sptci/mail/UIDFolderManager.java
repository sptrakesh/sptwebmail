package com.sptci.mail;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;

/**
 * An abstract base class for dealing with <code>IMAP</code> or similar
 * Folder objects.
 * 
 * <p>&copy; Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-04-30
 * @version $Id: UIDFolderManager.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
abstract class UIDFolderManager extends ProtocolFolderManager
{
  /**
   * Create a new instance of the manager for the specified store.
   *
   * @see #initFolders
   * @param manager The {@link #manager} to use.
   * @throws MessagingException If errors are encountered while fetching
   *   the folders from the store.
   */
  protected UIDFolderManager( FolderManager manager )
    throws MessagingException, com.sptci.mail.MessagingException
  {
    super( manager );
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
  @Override
  protected Folder getFolder( String name ) throws MessagingException
  {
    if ( name != null && name.length() == 0 ) name = FolderManager.INBOX;
    Folder folder = folders.get( name );
    if ( folder == null )
    {
      folder = create( name );
    }

    return folder;
  }

  /**
   * Returns an uncached folder associated with the full name specified.
   * 
   * @see #getFolder
   * @param name  The fully qualified name of the folder.
   * @throws MessagingException If errors are encountered while fetching
   *   or creating the folder.
   */
  @Override
  protected Folder getUncachedFolder( String name )
    throws MessagingException
  {
    Folder folder = getFolder( name );
    if ( name.length() == 0 ) name = FolderManager.INBOX;
    return getDefaultFolder().getFolder( name );
  }

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
  @Override
  protected Collection<Folder> getSubFolders( Folder parent )
    throws MessagingException
  {
    TreeMap<String,Folder> map = new TreeMap<String,Folder>();
    TreeMap<String,Folder> results = new TreeMap<String,Folder>();

    Folder[] list = folders.get( parent.getFullName() ).list();
    for ( Folder child : list )
    {
      if ( ! folders.containsKey( child.getFullName() ) )
      {
        folders.put( child.getFullName(), child );
      }

      map.put( child.getName().toUpperCase(), child );
    }

    Collection<Folder> values = new ArrayList<Folder>( map.size() );
    values.addAll( map.values() );

    return values;
  }

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
  @Override
  protected void delete( String folder ) throws MessagingException
  {
    if ( folders.containsKey( folder ) )
    {
      delete( folders.get( folder ) );
    }
  }

  /**
   * Initialise {@link #folders} with the full list of folders available
   * in the {@link #store}.
   *
   * @see #addFolder
   * @throws MessagingException If errors are encountered while fetching
   *   the folders from the store.
   */
  protected void initFolders() throws MessagingException
  {
    addFolder( getDefaultFolder() );
  }

  /**
   * Fetch the message uniquely identified by its <code>UID</code> specified.
   *
   * @param uid The <code>UID</code> to use to fetch the message from the
   *   store.
   * @param folder The full name of the folder in which the message exists.
   * @throws MessagingException If errors are encountered while fetching
   *   the message from the store.
   */
  protected Message getMessage( String uid, String folder )
    throws MessagingException
  {
    UIDFolder uidFolder = (UIDFolder) getFolder( folder );
    long id = Long.parseLong( uid );
    return uidFolder.getMessageByUID( id );
  }

  /**
   * Add the specified folder and any child folders associated with it 
   * to {@link #folders}.
   *
   * @param folder The parent folder that is to be added.
   * @throws MessagingException If errors are encountered while fetching
   *   the folders from the store.
   */
  protected abstract void addFolder( Folder folder ) throws MessagingException;
}
