package com.sptci.mail;

import java.io.FileInputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.sptci.io.FileUtilities.FILE_SEPARATOR;

/**
 * An data object that represents the application configuration properties.
 * This object contains information pertaining to the mail store, the
 * protocol to use etc.  This class contains all the logic necessary to
 * load the properties automatically.
 *
 * <p><b>Note:</b>This class depends upon the following JVM system property
 * being set:</p>
 * <pre>
 *   sptmail.data.directory
 *   For eg. this may be set in your JVM startup script as
 *   -Dsptmail.data.directory=/var/data/webmail
 *
 *   -OR-
 *
 *   you may set it in your application initialisation code.  Note that
 *   this needs to happen prior to the Properties class getting loaded
 *   by the JVM
 *   System.getProperties().put( "sptmail.data.directory", "/var/data/webmail" );
 * </pre>
 *
 * <p>&copy; Copyright 2006 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2006-11-23
 * @version $Id: Properties.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class Properties implements Serializable
{
  /**
   * The name of the JVM system property that holds the directory under
   * which the property file is stored.
   *
   * {@value}
   */
  public static final String DATA_DIRECTORY_PROPERTY = "sptmail.data.directory";

  /**
   * The data directory value as configured in {@link 
   * #DATA_DIRECTORY_PROPERTY} for easy access from different parts of the
   * application.
   */
  public static final String dataDirectory;

  /**
   * The name of the file used to store the properties.
   *
   * {@value}
   */
  public static final String PROPERTIES_FILE = "properties.xml";

  /**
   * The property name used to specify {@link #domain} in the property
   * file.
   *
   * {@value}
   */
  public static final String DOMAIN = "domain";

  /**
   * The property name used to specify {@link #protocol} in the property
   * file.
   *
   * {@value}
   */
  public static final String PROTOCOL = "protocol";

  /**
   * The property name used to specify {@link #port} in the property
   * file.
   *
   * {@value}
   */
  public static final String PORT = "port";

  /**
   * The property name used to specify {@link #incomingServer} in the 
   * property file.
   *
   * {@value}
   */
  public static final String INCOMING_SERVER = "incomingServer";

  /**
   * The property name used to specify {@link #localCache} in the 
   * property file.
   *
   * {@value}
   */
  public static final String LOCAL_CACHE = "localCache";

  /**
   * The property name used to specify {@link #smtpServer} in the 
   * property file.
   *
   * {@value}
   */
  public static final String SMTP_SERVER = "smtpServer";

  /**
   * The singleton instance of this class.
   */
  private static Properties singleton;

  /**
   * Static initialiser for the {@link #singleton} and {@link #dataDirectory}.
   *
   * @see #init
   */
  static
  {
    dataDirectory = System.getProperty( DATA_DIRECTORY_PROPERTY );
    init();
  }

  /**
   * The logger to use to log errors or messages to.
   */
  private static final Logger logger = MailSession.logger;

  /**
   * The domain name to use in e-mail addresses.
   */
  public final String domain;

  /**
   * The protocol to use to retrieve messages.  JavaMail supports only
   * <code>IMAP</code> and <code>POP3</code> and their secure variants.
   */
  public final String protocol;

  /**
   * The port to use to retrieve messages.
   */
  public final int port;

  /**
   * The incoming mail server to use.
   */
  public final String incomingServer;

  /**
   * A boolean flag used to indicate that a local cache of the folders and
   * messages are to be maintained.  Applies only to <code>IMAP</code>
   * message stores.
   */
  public final boolean localCache;

  /**
   * The SMTP server to use to send messages.
   */
  public final String smtpServer;

  /**
   * Return the {@link #singleton} instance of this class.
   *
   * @return The {@link #singleton} instance.
   * @throws MessagingException If errors were encountered while
   *   initialising {@link #singleton}.
   */
  public static final Properties getInstance() throws MessagingException
  {
    if ( singleton == null )
    {
      throw new MessagingException( "Properties not initialised.  Please consult system log for cause." );
    }

    return singleton;
  }

  /**
   * Create a new instance of the class with the specified values for
   * the instance members.  Cannot be instantiated.
   *
   * @see #getInstance
   * @param domain The {@link #domain} value to use.
   * @param protocol The {@link #protocol} value to use.
   * @param incomingServer The {@link #incomingServer} value to use.
   * @param smtpServer The {@link #smtpServer} value to use.
   */
  private Properties( String domain, String protocol, int port,
      String incomingServer, String localCache, String smtpServer )
  {
    this.domain = domain;
    this.protocol = protocol;
    this.port = port;
    this.incomingServer = incomingServer;
    this.localCache = Boolean.valueOf( localCache ).booleanValue();
    this.smtpServer = smtpServer;
  }

  /**
   * Initialise the {@link #singleton} instance from the properties file.
   */
  private static void init()
  {
    java.util.Properties properties = new java.util.Properties();
    String file = dataDirectory + FILE_SEPARATOR + PROPERTIES_FILE;

    try
    {
      FileInputStream fis = new FileInputStream( file );
      properties.loadFromXML( fis );

      singleton = new Properties( 
          properties.getProperty( DOMAIN ),
          properties.getProperty( PROTOCOL ),
          Integer.parseInt( properties.getProperty( PORT ) ),
          properties.getProperty( INCOMING_SERVER ),
          properties.getProperty( LOCAL_CACHE, "false" ),
          properties.getProperty( SMTP_SERVER ) );
    }
    catch ( Throwable t )
    {
      logger.log( Level.SEVERE,
          "Error reading environment properties file: " + file, t );
    }
  }
}
