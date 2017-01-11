package com.sptci.mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.mail.Session;
import javax.mail.Store;

import com.sptci.io.FileUtilities;
import static com.sptci.io.FileUtilities.FILE_SEPARATOR;
//import com.sptci.addressbook.AddressBook;
//import com.sptci.addressbook.AddressException;
//import com.sptci.mail.addressbook.AddressLookupModel;

/**
 * An object that maintains session information pertaining to the
 * connection to the mail store.
 *
 * <p>&copy; Copyright 2006 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2006-11-23
 * @version $Id: MailSession.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class MailSession implements Serializable
{
  /**
   * The logger to use to log errors or messages to.
   */
  protected static final Logger logger = Logger.getAnonymousLogger();

  /**
   * The name of the preferences file used to capture user preferences.
   */
  public static final String PREFERENCES_FILE = "preferences.xml";

  /**
   * The name of the file used to store user address book
   */
  public static final String ADDRESS_BOOK_FILE = "addressbook.xml";

  /**
   * The name of the directory under which the global spelling dictionaries
   * are stored.  This has to be relative to the application data
   * directory.
   */
  public static final String DICTIONARY_DIRECTORY = "dictionary";

  /**
   * The user name of the currently logged in user.  This property
   * will be null when no user is logged in.
   */
  protected String user;
  
  /**
   * The <code>javax.mail.Session</code> used to connect to the mail server.
   */
  protected transient final Session session;
  
  /**
   * The <code>javax.mail.Store</code> instance used to retrieve messages 
   * from the mail server.
   */
  protected transient Store store;

  /**
   * The manager class that handles all interactions with folders.
   */
  protected transient FolderManager folderManager;

  /**
   * The preferences object that contains user preferences.
   */
  protected Preferences preferences;

  /**
   * The address book for the current user.
  private AddressBook addressBook;
   */

  /**
   * The <code>AutoLookupModel</code> build around {@link #addressBook}
   * to use to display matching address book entries.
  private AddressLookupModel addressLookupModel;
   */

  /**
   * The indexer used to index messages in the {@link #user}s' mailboxes.
   */
  protected transient Indexer messageIndexer;

  /**
   * The searcher used to search the message indices.
   */
  protected transient MessageSearcher messageSearcher;

  /**
   * Create a new instance of the class.  Set the <code>SMTP Server</code>
   * property for the <code>JavaMail</code> system.
   *
   * @throws MessagingException If errors are encountered while loading
   *   the property file.
   */
  public MailSession() throws MessagingException
  {
    try
    {
      java.util.Properties properties = System.getProperties();
      properties.put( "mail.smtp.host", Properties.getInstance().smtpServer );
      properties.put( "mail.pop3.rsetbeforequit", "true" );
      properties.setProperty( "mail.store.maildir.autocreatedir", "true" );
      properties.setProperty( "mail.store.maildir.cachefolders", "true" );
      session = Session.getInstance( properties );
    }
    catch ( Throwable t )
    {
      throw new MessagingException( t );
    }
  }

  /**
   * Initialise {@link #preferences} from the preferences file for the
   * user.
   *
   * @throws MessagingException If errors are encountered while loading
   *   the property file.
   */
  protected void initPreferences() throws MessagingException
  {
    java.util.Properties properties = new java.util.Properties();
    File file = new File( getDataDirectory() + FILE_SEPARATOR +
        user + FILE_SEPARATOR + PREFERENCES_FILE );
    try
    {
      if ( ! file.exists() )
      {
        file = FileUtilities.copy( new File( getDataDirectory() +
            FILE_SEPARATOR + PREFERENCES_FILE ), file );
      }

      FileInputStream fis = new FileInputStream( file );
      properties.loadFromXML( fis );

      preferences = new Preferences( 
          properties.getProperty( Preferences.NAME ),
          properties.getProperty( Preferences.TRASHFOLDER ),
          properties.getProperty( Preferences.SENTFOLDER ),
          properties.getProperty( Preferences.DRAFTFOLDER ),
          properties.getProperty( Preferences.POLLINGINTERVAL ),
          properties.getProperty( Preferences.MESSAGESPERPAGE ),
          properties.getProperty( Preferences.MESSAGEFORMAT ),
          properties.getProperty( Preferences.MESSAGEORDER ) );
    }
    catch ( Throwable t )
    {
      logger.log( Level.SEVERE,
          "Error reading environment properties file: " + file, t );
      throw new MessagingException( t.getMessage() );
    }
  }

  /**
   * Save the {@link #preferences} back to its underlying file.
   */
  protected void savePreferences() throws IOException
  {
    java.util.Properties properties = new java.util.Properties();
    properties.setProperty( 
        Preferences.NAME, preferences.getName() );
    properties.setProperty( Preferences.TRASHFOLDER,
        preferences.getTrashFolder() );
    properties.setProperty( Preferences.SENTFOLDER,
        preferences.getSentFolder() );
    properties.setProperty( Preferences.DRAFTFOLDER,
        preferences.getDraftFolder() );
    properties.setProperty( Preferences.POLLINGINTERVAL,
        preferences.getPollingInterval() );
    properties.setProperty( Preferences.MESSAGESPERPAGE,
        preferences.getMessagesPerPage() );
    properties.setProperty( Preferences.MESSAGEFORMAT,
        preferences.getMessageFormat() );
    properties.setProperty( Preferences.MESSAGEORDER,
        preferences.getMessageOrder() );

    File file = new File( getDataDirectory() + FILE_SEPARATOR +
        user + FILE_SEPARATOR + PREFERENCES_FILE );
    FileUtilities.mkdirs( file );
    FileOutputStream fos = new FileOutputStream( file );
    properties.storeToXML( fos, "User preferences" );
  }

  /**
   * Initialise the address book for the current user.  Create an
   * empty book if one does not exist.  Otherwise populate {@link
   * #addressBook} from the file.  Also initialise {@link
   * #addressLookupModel} with the address book.
   *
   * @throws AddressException If errors are encountered while loading the
   *   user's address book.
  protected void initAddressBook() throws AddressException
  {
    File file = new File( getDataDirectory() + FILE_SEPARATOR +
        user + FILE_SEPARATOR + ADDRESS_BOOK_FILE );
    if ( ! file.exists() )
    {
      AddressBook.create( file.getAbsolutePath() );
    }

    addressBook = new AddressBook( file.getAbsolutePath() );
    addressLookupModel = new AddressLookupModel( addressBook,
        AddressLookupModel.MATCH_ONLY_FROM_START );
  }
   */
  
  /**
   * Returns {@link #user}.
   *
   * @return String The value/reference of/to user.
   */
  public String getUser()
  {
    return user;
  }
  
  /**
   * Set {@link #user}.
   * 
   * @param user The value to set.
   * @see #initPreferences
   * @see MessageIndexer
   * @see #getSearchIndexDirectory
   * @throws MessagingException If errors are encountered while loading
   *   the property file.
   */
  public void setUser( String user ) 
    //throws MessagingException, AddressException
    throws MessagingException
  {
    this.user = user;
    initPreferences();
    //initAddressBook();

    try
    {
      messageIndexer = new Indexer( getSearchIndexDirectory(), this );
      messageSearcher =
        new MessageSearcher( getSearchIndexDirectory(), folderManager );
    }
    catch ( Throwable t )
    {
      throw new MessagingException( t );
    }
  }

  /**
   * Returns the email address of the active user, or null if none.
   * This is derived by appending {@link #user} and {@link 
   * Properties#domain}.
   * 
   * @return The email address
   */
  public String getEmailAddress() 
  {
    String value = user;
    try
    {
      value += "@" + Properties.getInstance().domain;
    }
    catch ( Throwable t ) {}

    return value;
  }
  
  /**
   * Returns the active JavaMail {@link #session} object.
   * 
   * @return The <code>Session</code>
   */
  public Session getSession() 
  {
    return session;
  }
 
  /**
   * Returns {@link #store}.
   *
   * @return Store The value/reference of/to store.
   */
  public Store getStore()
  {
    return store;
  }
  
  /**
   * Set {@link #store}.  Also initialise {@link #folderManager} with
   * a new instance using the specified store.
   *
   * @param store The value to set.
   * @throws MessagingException If errore are encountered while initialising
   *   {@link #folderManager}.
   */
  public void setStore( Store store ) throws MessagingException
  {
    this.store = store;
    if ( store != null )
    {
      folderManager =
        new FolderManager( this, Properties.getInstance().protocol );
    }
  }

  /**
   * Returns {@link #folderManager}.
   *
   * @return FolderManager The value/reference of/to folderManager.
   */
  public FolderManager getFolderManager()
  {
    return folderManager;
  }
  
  /**
   * Returns the value of {@link Properties#dataDirectory}.
   *
   * @return Returns the value is {@link Properties} is properly
   *   initialised, otherwise returns <code>null</code>.
   */
  public String getDataDirectory()
  {
    String value = null;
    try
    {
      value = Properties.getInstance().dataDirectory;
    }
    catch ( Throwable t ) {}

    return value;
  }
  
  /**
   * Returns {@link #preferences}.
   *
   * @return Preferences The value/reference of/to preferences.
   */
  public Preferences getPreferences()
  {
    return preferences;
  }
  
  /**
   * Returns {@link #addressBook}.
   *
   * @return AddressBook The value/reference of/to addressBook.
  public AddressBook getAddressBook()
  {
    return addressBook;
  }
   */
  
  /**
   * Returns {@link #addressLookupModel}.
   *
   * @return AddressLookupModel The value/reference of/to addressLookupModel.
  public AddressLookupModel getAddressLookupModel()
  {
    return addressLookupModel;
  }
   */

  /**
   * Return the temporary directory that is to be used to store attachments
   * and other temporary files necessary for the user session.
   */
  public String getTempDirectory()
  {
    return getDataDirectory() + FILE_SEPARATOR + user + FILE_SEPARATOR + "tmp";
  }

  /**
   * Return the directory that is to be used to store search indices
   * for messages in the user's mailboxes.
   */
  public String getSearchIndexDirectory()
  {
    return getDataDirectory() + FILE_SEPARATOR + user + FILE_SEPARATOR + "search";
  }
  
  /**
   * Returns {@link #messageIndexer}.
   * 
   * @return MIndexerThe value/reference of/to messageIndexer.
   */
  public Indexer getMessageIndexer()
  {
    return messageIndexer;
  }
  
  /**
   * Returns {@link #messageSearcher}.
   *
   * @return MessageSearcher The value/reference of/to messageSearcher.
   */
  public MessageSearcher getMessageSearcher()
  {
    messageSearcher.setManager( folderManager );
    return messageSearcher;
  }
}
