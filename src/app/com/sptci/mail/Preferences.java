package com.sptci.mail;

import java.io.Serializable;

/**
 * A data bean that is used to store user preferences that are stored
 * in {@link #PREFERENCES_FILE}.
 *
 * <p>&copy; Copyright 2006 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2006-11-23
 * @version $Id: Preferences.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class Preferences implements Serializable
{
  /**
   * The key to use to identify the distinguished name of the user.
   * This value is used to set the <code>Personal</code> part of the
   * user's e-mail addreess.
   */
  public static final String NAME = "distinguishedName";

  /**
   * The key to use to identify the folder to use to store deleted
   * messages.
   */
  public static final String TRASHFOLDER = "trashFolder";

  /**
   * The key to use to identify the folder to use to store sent
   * messages.
   */
  public static final String SENTFOLDER = "sentFolder";

  /**
   * The key to use to identify the folder to use to store draft
   * messages.
   */
  public static final String DRAFTFOLDER = "draftFolder";

  /**
   * The key to use to identify user preference on polling interval to
   * automatically check for new messages.
   */
  public static final String POLLINGINTERVAL = "pollingInterval";

  /**
   * The key to use to identify user preference on number of messages to
   * display per page.
   */
  public static final String MESSAGESPERPAGE = "messagesPerPage";

  /**
   * The key to use to capture user preference on composing plain text
   * (<code>text/plain</code>) or HTML (<code>text/html</code> messages.
   */
  public static final String MESSAGEFORMAT = "messageFormat";

  /**
   * The key to use to identify user preference on whether new messages
   * are displayed first, or last.
   */
  public static final String MESSAGEORDER = "messageOrder";

  /**
   * The name of the user to display along with {@link #getEmailAddress}
   */
  private String name;

  /**
   * The name of the folder to use to store deleted messages.
   */
  private String trashFolder;

  /**
   * The name of the folder to use to store sent messages.
   */
  private String sentFolder;

  /**
   * The name of the folder to use to store draft messages.
   */
  private String draftFolder;

  /**
   * The interval (in minutes) at which the mail store is to be polled
   * for new messages.
   */
  private String pollingInterval;

  /**
   * The number of messages to display per page.
   */
  private String messagesPerPage;

  /**
   * The format in which to compose messages.
   */
  private String messageFormat;

  /**
   * The order in which messages are to be sorted.
   */
  private String messageOrder;

  /**
   * Default constructor.
   */
  private Preferences() {}

  /**
   * Create a new instance of the bean with the specified data.
   *
   * @param name The {@link #name} value to use.
   * @param trashFolder The {@link #trashFolder} value to use.
   * @param sentFolder The {@link #sentFolder} value to use.
   * @param draftFolder The {@link #draftFolder} value to use.
   * @param pollingInterval The {@link #pollingInterval} value to use.
   * @param messagesPerPage The {@link #messagesPerPage} value to use.
   * @param messageFormat The {@link #messageFormat} value to use.
   * @param messageOrder The {@link #messageOrder} value to use.
   */
  Preferences( String name, String trashFolder, String sentFolder,
      String draftFolder, String pollingInterval, String messagesPerPage,
      String messageFormat, String messageOrder )
  {
    setName( name );
    setTrashFolder( trashFolder );
    setSentFolder( sentFolder );
    setDraftFolder( draftFolder );
    setPollingInterval( pollingInterval );
    setMessagesPerPage( messagesPerPage );
    setMessageFormat( messageFormat );
    setMessageOrder( messageOrder );
  }
  
  /**
   * Returns {@link #name}.
   *
   * @return String The value/reference of/to name.
   */
  public String getName()
  {
    return name;
  }
  
  /**
   * Set {@link #name}.
   *
   * @param name The value to set.
   */
  public void setName( String name )
  {
    this.name = name;
  }
  
  /**
   * Returns {@link #trashFolder}.
   *
   * @return String The value/reference of/to trashFolder.
   */
  public String getTrashFolder()
  {
    return trashFolder;
  }
  
  /**
   * Set {@link #trashFolder}.
   *
   * @param trashFolder The value to set.
   */
  public void setTrashFolder( String trashFolder )
  {
    this.trashFolder = trashFolder;
  }
  
  /**
   * Returns {@link #sentFolder}.
   *
   * @return String The value/reference of/to sentFolder.
   */
  public String getSentFolder()
  {
    return sentFolder;
  }
  
  /**
   * Set {@link #sentFolder}.
   *
   * @param sentFolder The value to set.
   */
  public void setSentFolder( String sentFolder )
  {
    this.sentFolder = sentFolder;
  }
  
  /**
   * Returns {@link #draftFolder}.
   *
   * @return String The value/reference of/to draftFolder.
   */
  public String getDraftFolder()
  {
    return draftFolder;
  }
  
  /**
   * Set {@link #draftFolder}.
   *
   * @param draftFolder The value to set.
   */
  public void setDraftFolder( String draftFolder )
  {
    this.draftFolder = draftFolder;
  }
  
  /**
   * Returns {@link #pollingInterval}.
   *
   * @return String The value/reference of/to pollingInterval.
   */
  public String getPollingInterval()
  {
    return pollingInterval;
  }
  
  /**
   * Set {@link #pollingInterval}.
   *
   * @param pollingInterval The value to set.
   */
  public void setPollingInterval( String pollingInterval )
  {
    this.pollingInterval = pollingInterval;
  }
  
  /**
   * Returns {@link #messagesPerPage}.
   *
   * @return String The value/reference of/to messagesPerPage.
   */
  public String getMessagesPerPage()
  {
    return messagesPerPage;
  }
  
  /**
   * Set {@link #messagesPerPage}.
   *
   * @param messagesPerPage The value to set.
   */
  public void setMessagesPerPage( String messagesPerPage )
  {
    this.messagesPerPage = messagesPerPage;
  }
  
  /**
   * Returns {@link #messageFormat}.
   *
   * @return String The value/reference of/to messageFormat.
   */
  public String getMessageFormat()
  {
    return messageFormat;
  }
  
  /**
   * Set {@link #messageFormat}.
   *
   * @param messageFormat The value to set.
   */
  public void setMessageFormat( String messageFormat )
  {
    this.messageFormat = messageFormat;
  }
  
  /**
   * Returns {@link #messageOrder}.
   *
   * @return String The value/reference of/to messageOrder.
   */
  public String getMessageOrder()
  {
    return messageOrder;
  }
  
  /**
   * Set {@link #messageOrder}.
   *
   * @param messageOrder The value to set.
   */
  public void setMessageOrder( String messageOrder )
  {
    this.messageOrder = messageOrder;
  }
}
