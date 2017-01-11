package com.sptci.mail.filestore;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.UIDFolder;

import com.sptci.mail.MailSession;
import com.sptci.mail.Properties;
import static com.sptci.mail.FolderManager.ROOT_DIRECTORY;

/**
 * A mail store implementation that used a file system directory tree to
 * represent mail folders.
 *
 * <p>The {@link javax.mail.URLName} used to instantiate the store should
 * contain the following information:
 * <ol>
 *   <li><b>protocol</b> - Always <code>filestore</code></li>
 *   <li><b>file</b> - The base directory under which this store should
 *   store folders and messages.</li>
 *   <li><b>username</b> - The username of the logged in user.  There is
 *   no real need for this information, but may be useful for logging
 *   purposes.</li>
 * </ol>
 * </p>
 *
 * <p>The following shows sample use of this class:</p>
 * <pre>
 *   import javax.mail.Session;
 *   import javax.mail.URLName;
 *   import com.sptci.mail.filestore.FileStore;
 *
 *     ...
 *     Session session = ...;
 *     String user = "test";
 *     String baseDirectory = "/var/data/webmail/" + user + "/mail";
 *     URLName url = new URLName( "filestore", null, -1, baseDirectory, user, null );
 *     FileStore store = new FileStore( session, url );
 *
 *     url = new URLName( "filestore://" + user + "@/" + baseDirectory );
 *     store = new FileStore( session, url );
 * </pre>
 *
 * <p>&copy; Copyright 2008 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2008-03-09
 * @version $Id: FileStore.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class FileStore extends Store
{
  /**
   * The url name that was used to initialise this instance.  Stored
   * locally since the super class implementation does not return the
   * file part of the url name.
   */
  private URLName url;

  /**
   * Create a new instance of the store for the specified session and base
   * URL.
   *
   * @param session The login session for the user.
   * @param url The URL that represents the store for the logged in user.
   */
  public FileStore( final Session session, final URLName url )
  {
    super( session, url );
    this.url = url;
  }

  /**
   * Returns a Folder object that represents the 'root' of the default
   * namespace presented to the user by the Store.
   *
   * @see javax.mail.Store#getDefaultFolder
   * @return The root of the default namespace for the user.
   * @throws MessagingException If errors are encountered while initialising
   *   the default namespace.
   */
  @Override
  public Folder getDefaultFolder() throws MessagingException
  {
    return new FileFolder( ".", this );
  }

  /**
   * Return the Folder object corresponding to the given name.
   *
   * @see javax.mail.Folder#getFolder( String )
   * @param name The fully qualified name of the folder.  If no path
   *   delimiter is present, then the folder is assumed to be under the
   *   default namespace for the user.
   * @return The folder instance corresponding to specified name.  If the
   *   folder exists, the returned folder is in <code>closed</code> state.
   * @throws MessagingException If errors are encountered while initialising
   *   the folder if the name represents an existing folder.
   */
  @Override
  public Folder getFolder( final String name ) throws MessagingException
  {
    return new FileFolder( name, this );
  }

  /**
   * Return the folder identified by the specified urlname.
   *
   * @see javax.mail.Folder#getFolder( URLName )
   * @param url The URLName that denotes a folder.
   * @return The folder instance corresponding to specified name.  If the
   *   folder exists, the returned folder is in <code>closed</code> state.
   * @throws MessagingException If errors are encountered while initialising
   *   the folder if the name represents an existing folder.
   */
  @Override
  public Folder getFolder( final URLName url ) throws MessagingException
  {
    return new FileFolder( url.getFile() );
  }

  /**
   * Return {@link #url}.  Over-ridden to return the file part of the
   * url name, which is stripped in the default implementation.
   *
   * @return The url name with which this store was initialised.
   */
  @Override
  public URLName getURLName()
  {
    return url;
  }

  /**
   * The implementation of the <code>connect</code> methods supported.
   * Since we are dealing with a local (or network mounted) file system
   * based repository with no authentication requirements, this method
   * always returns <code>true</code>.
   *
   * @see javax.mail.Service#protocolConnect
   */
  @Override
  protected boolean protocolConnect( final String host, final int port,
      final String user, final String password)
  {
    return true;
  }
}
