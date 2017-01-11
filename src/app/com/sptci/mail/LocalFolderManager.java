package com.sptci.mail;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.mail.internet.MimeMessage;

/**
 * A class that is used to manage all interactions with {@link Folder}
 * objects.
 *
 * <p>&copy; Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-15
 * @version $Id: LocalFolderManager.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
class LocalFolderManager
{
  /**
   * The map used to maintain a cache of all the local folder
   * representations.
   */
  final Map<String,Folder> folders =
    new ConcurrentHashMap<String,Folder>();

  /**
   * Create a new instance of the manager for the specified session.
   */
  LocalFolderManager() {}

  /**
   * Returns the folder associated with the full name specified.  If the
   * specified folder does not exist, it is created.
   * 
   * @param name  The fully qualified name of the folder.
   * @throws MessagingException If errors are encountered while fetching
   *   or creating the folder.
   */
  private Folder getFolder( String name ) throws MessagingException
  {
    return folders.get( name );
  }

  /**
   * Fetch a {@link Folder} instance that represents the
   * <code>java.mail.Folder</code> specified.
   *
   * @see #updateCounts
   * @param imapFolder The folder whose representation is to be added to the
   *   local repository.
   * @throws MessagingException If errors are encountered while creating
   *   the local representation.
   */
  Folder getFolder( javax.mail.Folder imapFolder ) throws MessagingException
  {
    Folder folder = folders.get( imapFolder.getFullName() );
    if ( folder != null )
    {
      updateCounts( folder, imapFolder );
      return folder;
    }

    try
    {
      javax.mail.Folder parent = imapFolder.getParent();
      if ( parent != null && ! ( parent.getFullName().length() == 0 ) )
      {
        if ( ! folders.containsKey( parent.getFullName() ) )
        {
          folders.put( parent.getFullName(), getFolder( parent ) );
        }

        folder = new Folder(
            imapFolder.getName(), folders.get( parent.getFullName() ) );
      }
      else
      {
        folder = new Folder( imapFolder.getName() );
      }

      folder.setFullName( imapFolder.getFullName() );
      updateCounts( folder, imapFolder );
      folders.put( imapFolder.getFullName(), folder );

      if ( ! Properties.getInstance().protocol.toLowerCase().contains( "pop" ) )
      {
        for ( javax.mail.Folder child : imapFolder.list() )
        {
          folder.addChild( getFolder( child ) );
        }
      }
    }
    catch ( MessagingException mex )
    {
      throw mex;
    }
    catch ( Throwable t )
    {
      throw new MessagingException( t );
    }

    return folder;
  }

  /**
   * Delete the folder identified by the specified full name from
   * {@link #folders}.
   *
   * @param folder The full name of the folder that is to be deleted.
   */
  void delete( String folder )
  {
    Folder parent = folders.get( folder );
    if ( parent == null ) return;

    for ( Folder f : parent.getChildren() )
    {
      folders.remove( f.getFullName() );
    }

    folders.remove( folder );
  }

  /**
   * Update the message count fields in {@link Folder} with the counts
   * from the specified <code>javax.mail.Folder</code>.
   *
   * @param folder The local folder representation of the store folder.
   * @param imapfolder The store folder.
   * @throws MessagingException If errors are encountered while fetching
   *   the message counts.
   */
  void updateCounts( Folder folder, javax.mail.Folder imapFolder )
    throws MessagingException
  {
    try
    {
      boolean close = false;
      if ( ! imapFolder.isOpen() )
      {
        close = true;
        imapFolder.open( javax.mail.Folder.READ_ONLY );
      }

      folder.setMessageCount( imapFolder.getMessageCount() );
      folder.setNewMessageCount( imapFolder.getNewMessageCount() );
      folder.setUnreadMessageCount( imapFolder.getUnreadMessageCount() );
      folder.setDeletedMessageCount( imapFolder.getDeletedMessageCount() );

      if ( close ) imapFolder.close( false );
    }
    catch ( Throwable t )
    {
      throw new MessagingException( t );
    }
  }

  /**
   * Create a new {@link Message} instance out of the specified {@link
   * javax.mail.Message}.
   *
   * @param message The message whose representation is to be returned.
   * @return The message data object.
   * @throws MessagingException If errors are encountered while processing
   *   the message.
   */
  Message getMessage( javax.mail.Message message,
      ProtocolFolderManager manager ) throws MessagingException
  {
    Message msg = null;

    try
    {
      MimeMessage mimeMessage = (MimeMessage) message;
      msg = new Message( manager.getUID( mimeMessage ),
          getFolder( mimeMessage.getFolder() ) );

      MessageHeaders headers = new MessageHeaders();
      headers.setMessageId( mimeMessage.getMessageID() );
      headers.setReceivedDate( mimeMessage.getReceivedDate() );
      headers.setSentDate( mimeMessage.getSentDate() );
      headers.setSubject( mimeMessage.getSubject() );

      msg.setHeaders( headers );
    }
    catch ( Throwable t )
    {
      throw new MessagingException( t );
    }

    throw new RuntimeException( "Incomplete implementation" );
    //return msg;
  }
}
