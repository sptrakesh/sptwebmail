package com.sptci.mail;

import java.io.File;
import java.io.IOException;

import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Session;
import javax.mail.Store;

import com.sptci.io.FileUtilities;
import static com.sptci.io.FileUtilities.FILE_SEPARATOR;

/**
 * A utility class to handle interactions with the mail store.  Primarily
 * used to connect to and disconnect from the store.
 *
 * <p>&copy; Copyright 2006 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2006-11-25
 * @version $Id: StoreManager.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class StoreManager
{
  /**
   * The logger to use to log errors/messages to.
   */
  private static final Logger logger = MailSession.logger;

  /**
   * A reference to the custom object that represents a mail session.
   */
  private final MailSession mailSession;

  /**
   * Create a new instance of the class using the specified session.
   *
   * @param mailSession The {@link #mailSession} value to use.
   */
  public StoreManager( MailSession mailSession )
  {
    this.mailSession = mailSession;
  }

  /**
   * Connects to the mail server with the given user name and
   * password.
   *
   * @see Properties
   * @see MailSession#getSession
   * @see MailSession#setStore
   * @see #initDirectory
   * @see MailSession#setUser
   * @param userName The account/login name to use to connect
   * @param password The password to use to connect
   * @throws AuthenticationFailedException If authentication failed due
   *   to improper credentials.
   * @throws MessagingException If the connection failed due to reasons
   *   other than credentials.
   */
  public void connect( String userName, String password ) 
    throws AuthenticationFailedException, MessagingException
  {
    try
    {
      logger.fine( "Initialising store" );
      Store store = mailSession.getSession().getStore( 
          Properties.getInstance().protocol );
      logger.fine( "Connecting to store" );
      store.connect(
          Properties.getInstance().incomingServer,
          Properties.getInstance().port, userName, password );

      logger.fine( "Initialising user directory" );
      initDirectory( userName );
      logger.fine( "Initialising mail session user" );
      mailSession.setUser( userName );
      logger.fine( "Initialising mail session store" );
      mailSession.setStore( store );
    }
    catch ( javax.mail.AuthenticationFailedException aex )
    {
      logger.info( "Unsuccessful login attempt by user: " + userName );
      throw new AuthenticationFailedException( aex );
    }
    catch ( Throwable t )
    {
      throw new MessagingException( t );
    }
  }
  
  /**
   * Disconnects the session with the mail server.  Also perform any other
   * clean up activities required.
   *
   * @see MessageIndexer#destroy
   * @see MessageSearcher#destroy
   * @see MailSession#getStore
   * @see MailSession#setStore
   */
  public void disconnect() 
  {
    if ( mailSession.getStore() != null )
    {
      try 
      {
        //mailSession.getFolderManager().save();
        logger.fine( "Destroying message indexer" );
        mailSession.getMessageIndexer().destroy();
        logger.fine( "Destroying message searcher" );
        mailSession.getMessageSearcher().destroy();
        logger.fine( "Destroying folder manager" );
        mailSession.getFolderManager().destroy();
        logger.info( "Closing connection to store" );
        mailSession.getStore().close();
        mailSession.setStore( null );
      } 
      catch ( Throwable t ) 
      {
        logger.log( Level.WARNING, 
            "Error closing mail store connection", t );
      }
    }
    
    logger.info( "Deleting temporary cached files" );
    FileUtilities.delete( mailSession.getTempDirectory(), false );
  }

  /**
   * Initialise the user specific data directory under application root
   * data directory.
   *
   * @see MailSession#dataDirectory
   * @param userName The name of the user whose data directory is to be
   *   initialised.
   */
  private void initDirectory( String userName ) throws IOException
  {
    File file = new File( mailSession.getDataDirectory() + 
        FILE_SEPARATOR + userName );
    if ( ! file.exists() )
    {
      file.mkdirs();
    }

    if ( file.isFile() )
    {
      throw new IOException( "Unable to create directory " +
          file.getAbsolutePath() );
    }
  }
  
  /**
   * Returns {@link #mailSession}.
   *
   * @return The value/reference of/to mailSession.
   */
  public MailSession getMailSession()
  {
    return mailSession;
  }
}
