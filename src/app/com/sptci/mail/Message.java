package com.sptci.mail;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import javax.mail.Address;

import com.thoughtworks.xstream.XStream;

/**
 * A representation of a <code>Message</code> that can be stored to a local
 * repository.  This is useful to maintain a local cache of messages as well
 * as to support organising messages into a local tree for <code>POP3</code>
 * stores.
 *
 *<p>&copy; Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-10
 * @version $Id: Message.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class Message implements Serializable, Comparable<Message>
{
  /**
   * The <code>XStream</code> instance that is used to marshall/unmarshall
   * instances of this class.
   */
  static final XStream xstream = new XStream();

  /**
   * The headers for the message.  The common attributes are also available
   * in the search indices for easy retrieval and searching.
   */
  private MessageHeaders headers;

  /**
   * The content for the message.  The content is stored in a local
   * repository and is also indexed in the search engine.
   */
  private MessageContent content;

  /**
   * The folder to which the message belongs.
   */
  private Folder folder;

  /**
   * The <code>UID</code> assigned to the message by the store.  <code>IMAP
   * </code> stores assign a <code>long</code> value, while <code>POP3</code>
   * stores assign a string value.  For consistency, represent the
   * <code>UID</code> as a string.
   */
  private String uid;

  /**
   * Create a new message representing storing its contents under the
   * {@link #folder}.
   *
   * @param folder The {@link #folder} to use.
   */
  public Message( final Folder folder )
  {
    this.folder = folder;
  }

  /**
   * Create a new message representing the specified <code>UID</code>
   * and storing its contents under the {@link #folder}.
   *
   * @param uid The {@link #uid} unique identifier for the message.
   * @param folder The {@link #folder} to use.
   */
  public Message( final String uid, final Folder folder )
  {
    this( folder );
    this.uid = uid;
  }
  
  /**
   * Returns {@link #headers}.
   *
   * @return The value/reference of/to headers.
   */
  public MessageHeaders getHeaders()
  {
    return headers;
  }

  /**
   * Return a string representation of this object.  Delegates to
   * <code>XStream</code>.
   *
   * @return The string representation of this message.
   */
  @Override
  public String toString()
  {
    return xstream.toXML( this );
  }

  /**
   * Compare the specified message with this for equality.  Compares the
   * values of {@link #messageId}.
   *
   * @param object That object that is to be compared with this message
   *   for equality
   * @return Returns <code>true</code> if the specified object is a 
   *   message with an identical value for message id.
   */
  @Override
  public boolean equals( Object object )
  {
    if ( this == object ) return true;
    boolean result = false;
    if ( object instanceof Message )
    {
      Message message = (Message) object;
      result = ( ( uid == message.uid ) ||
          ( ( uid != null ) && uid.equals( message.uid ) ) );
    }

    return result;
  }

  /**
   * Return a hash code for this message.
   *
   * @return int Returns the hash code of the {@link #messageId}.
   */
  @Override
  public int hashCode()
  {
    int result = super.hashCode();
    if ( uid != null ) result = uid.hashCode();

    return result;
  }

  /**
   * Compare the specified message with this for ordering.
   *
   * @return int Returns a negative, zero or postitive number depending
   *   upon whether the specified object compares lower, equal to or above
   *   this message.  Use {@link #getReceivedDate} for comparison.
   */
  public int compareTo( Message message )
  {
    int result = -1;
    if ( getReceivedDate() != null )
    {
      result = getReceivedDate().compareTo( message.getReceivedDate() );
    }

    return result;
  }
  
  /**
   * Returns {@link #uid}.
   *
   * @return The value/reference of/to uid.
   */
  public String getUid()
  {
    return uid;
  }

  /**
   * Return the <code>message-id</code> header for the message.
   *
   * @return The {@link MessageHeader#getMessageId} value.
   */
  public String getMessageId()
  {
    return headers.getMessageId();
  }

  /**
   * Return the sender of the message.
   *
   * @return The {@link MessageHeader#getSender} value.
   */
  public Address getSender()
  {
    return headers.getSender();
  }

  /**
   * Return the primary recipients of the message.
   *
   * @return The {@link MessageHeader#getTo} value.
   */
  public Address[] getTo()
  {
    return headers.getTo();
  }

  /**
   * Return the carbon copied recipients of the message.
   *
   * @return The {@link MessageHeader#getCc} value.
   */
  public Address[] getCc()
  {
    return headers.getCc();
  }

  /**
   * Return the subject of the message.
   *
   * @return The {@link MessageHeader#getSubject} value.
   */
  public String getSubject()
  {
    return headers.getSubject();
  }

  /**
   * Return the date on which this message was recieved by the store.
   *
   * @return The {@link MessageHeader#receivedDate} property.  Returns
   *   <code>null</code> if this property is not set for the message.
   */
  public Date getReceivedDate()
  {
    return headers.getReceivedDate();
  }
  
  /**
   * Set {@link #headers}.
   *
   * @param headers The value to set.
   */
  public void setHeaders( MessageHeaders headers )
  {
    this.headers = headers;
  }
  
  /**
   * Returns {@link #content}.  Lazily initialises {@link #content} prior
   * to returning the contents.
   *
   * @return The value/reference of/to content.
   * @throws MessagingException If errors are encountered while fetching
   *   the file contents.
   */
  public MessageContent getContent() throws MessagingException
  {
    return content;
  }
  
  /**
   * Set {@link #content}.
   *
   * @param content The value to set.
   */
  public void setContent( MessageContent content )
  {
    this.content = content;
  }
  
  /**
   * Returns {@link #folder}.
   *
   * @return The value/reference of/to folder.
   */
  public Folder getFolder()
  {
    return folder;
  }
  
  /**
   * Set {@link #folder}.
   *
   * <p><b>Note:</b> This method should be used with care.  It should be
   * ensured that the search indices are updated accordingly.</p>
   *
   * @param folder The value to set.
   * @throws MessagingException If errors are encountered while moving the
   *   message.
   */
  protected void setFolder( Folder folder ) throws MessagingException
  {
    this.folder = folder;
  }
}
