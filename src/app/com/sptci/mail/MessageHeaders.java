package com.sptci.mail;

import java.io.Serializable;
import java.util.Date;

import javax.mail.Address;
import javax.mail.Flags;

import com.thoughtworks.xstream.XStream;

/**
 * A representation of the metadata available in a <code>Message</code> that
 * can be stored to a local repository.  This is useful to maintain a local 
 * cache of messages as well as to support organising messages into a local 
 * tree for <code>POP3</code> stores.
 *
 * <p><b>Note:</b> This object uses <a href='http://xstream.codehaus.org/'>XStream</a>
 * to marshall and unmarshall instances to and from the local repository.</p>
 *
 *<p>&copy; Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-10
 * @version $Id: MessageHeaders.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class MessageHeaders implements Serializable
{
  /**
   * The name of the file in which the contents will be stored.
   */
  public static final String FILE_NAME = "com.sptci.mail.MessageHeaders.xml";

  /**
   * The unique <code>message-id</code> that is assigned to the message.
   * Note that not all messages have this property.  Only messages that
   * have a <code>message-id</code> can be stored in the local repository.
   */
  private String messageId;

  /**
   * The sender of the message.
   */
  private Address sender;

  /**
   * The <code>reply-to</code> value(s) specified by the {@link #sender}.
   */
  private Address[] replyTo;

  /**
   * The primary recipients of the message.
   */
  private Address[] to;

  /**
   * The carbon-copied recipients of the message.
   */
  private Address[] cc;

  /**
   * The blind carbon copied recipients of the message.
   */
  private Address[] bcc;

  /**
   * The date on which the message was received by the mail store.  This
   * may be <code>null</code>.
   */
  private Date receivedDate;

  /**
   * The date on which the message was sent.  This usually applies only
   * to messages sent by the user.  This may be <code>null</code>.
   */
  private Date sentDate;

  /**
   * The subject of the message.  This may be <code>null</code>.
   */
  private String subject;

  /**
   * The size in bytes of the message.
   */
  private int size;

  /**
   * The flags associated with the message.
   */
  private Flags flags;

  /**
   * Default constructor.  Should be constructed only by {@link
   * MessageHandler} or {@link Message}.
   */
  MessageHeaders() {}

  /**
   * Return the string representation of the message headers.  Delegates
   * to {@link Message#xstream}.
   *
   * @return The string representation of the message headers.
   */
  @Override
  public String toString()
  {
    return Message.xstream.toXML( this );
  }
  
  /**
   * Returns {@link #messageId}.
   *
   * @return The value/reference of/to messageId.
   */
  public String getMessageId()
  {
    return messageId;
  }
  
  /**
   * Set {@link #messageId}.
   *
   * @param messageId The value to set.
   */
  public void setMessageId( String messageId )
  {
    this.messageId = messageId;
  }
  
  /**
   * Returns {@link #sender}.
   *
   * @return The value/reference of/to sender.
   */
  public Address getSender()
  {
    return sender;
  }
  
  /**
   * Set {@link #sender}.
   *
   * @param sender The value to set.
   */
  public void setSender( Address sender )
  {
    this.sender = sender;
  }
  
  /**
   * Returns {@link #to}.
   *
   * @return The value/reference of/to to.
   */
  public Address[] getTo()
  {
    return to;
  }
  
  /**
   * Set {@link #to}.
   *
   * @param to The value to set.
   */
  public void setTo( Address[] to )
  {
    this.to = to;
  }
  
  /**
   * Returns {@link #cc}.
   *
   * @return The value/reference of/to cc.
   */
  public Address[] getCc()
  {
    return cc;
  }
  
  /**
   * Set {@link #cc}.
   *
   * @param cc The value to set.
   */
  public void setCc( Address[] cc )
  {
    this.cc = cc;
  }
  
  /**
   * Returns {@link #bcc}.
   *
   * @return The value/reference of/to bcc.
   */
  public Address[] getBcc()
  {
    return bcc;
  }
  
  /**
   * Set {@link #bcc}.
   *
   * @param bcc The value to set.
   */
  public void setBcc( Address[] bcc )
  {
    this.bcc = bcc;
  }
  
  /**
   * Returns {@link #receivedDate}.
   *
   * @return The value/reference of/to receivedDate.
   */
  public Date getReceivedDate()
  {
    return receivedDate;
  }
  
  /**
   * Set {@link #receivedDate}.
   *
   * @param receivedDate The value to set.
   */
  public void setReceivedDate( Date receivedDate )
  {
    this.receivedDate = receivedDate;
  }
  
  /**
   * Returns {@link #sentDate}.
   *
   * @return The value/reference of/to sentDate.
   */
  public Date getSentDate()
  {
    return sentDate;
  }
  
  /**
   * Set {@link #sentDate}.
   *
   * @param sentDate The value to set.
   */
  public void setSentDate( Date sentDate )
  {
    this.sentDate = sentDate;
  }
  
  /**
   * Returns {@link #subject}.
   *
   * @return The value/reference of/to subject.
   */
  public String getSubject()
  {
    return subject;
  }
  
  /**
   * Set {@link #subject}.
   *
   * @param subject The value to set.
   */
  public void setSubject( String subject )
  {
    this.subject = subject;
  }
  
  /**
   * Returns {@link #size}.
   *
   * @return The value/reference of/to size.
   */
  public int getSize()
  {
    return size;
  }
  
  /**
   * Set {@link #size}.
   *
   * @param size The value to set.
   */
  public void setSize( int size )
  {
    this.size = size;
  }
  
  /**
   * Returns {@link #flags}.
   *
   * @return The value/reference of/to flags.
   */
  public Flags getFlags()
  {
    return flags;
  }
  
  /**
   * Set {@link #flags}.
   *
   * @param flags The value to set.
   */
  public void setFlags( Flags flags )
  {
    this.flags = flags;
  }
}
