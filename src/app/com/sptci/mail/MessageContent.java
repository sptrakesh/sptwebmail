package com.sptci.mail;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Address;
import javax.mail.Flags;

import com.thoughtworks.xstream.XStream;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.util.NodeIterator;

import static com.sptci.io.FileUtilities.FILE_SEPARATOR;

/**
 * A representation of the content available in a <code>Message</code> that
 * can be stored to a local repository.  This is useful to maintain a local 
 * cache of messages as well as to support organising messages into a local 
 * tree for <code>POP3</code> stores.
 *
 * <p><b>Note:</b> This object uses <a href='http://xstream.codehaus.org/'>XStream</a>
 * to marshall and unmarshall instances to and from the local repository.</p>
 *
 *<p>&copy; Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-10
 * @version $Id: MessageContent.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class MessageContent implements Serializable
{
  /**
   * The name of the file in which the contents will be stored.
   */
  public static final String FILE_NAME = "com.sptci.mail.MessageContent.xml";

  /**
   * The end of line character to use to delimit lines of text.
   */
  public static final String EOL = System.getProperties().getProperty( "line.separator" );

  /**
   * The unique <code>message-id</code> that is assigned to the message.
   * Note that not all messages have this property.  Only messages that
   * have a <code>message-id</code> can be stored in the local repository.
   */
  private String messageId;

  /**
   * A map of the individual <code>text</code> parts of the message.
   * The <code>key</code> of the map indicates the part number within
   * the message.  This can be used to determine whether the part contains
   * an <code>HTML</code> alternative.
   */
  private Map<Integer,String> text = new TreeMap<Integer,String>();

  /**
   * A map of the individual <code>HTML</code> parts of the message.
   * The <code>key</code> of the map indicates the part number within
   * the message.  This can be used to determine whether the part contains
   * a <code>text</code> alternative.
   */
  private Map<Integer,String> html = new TreeMap<Integer,String>();

  /**
   * A collection of the individual attachment <code>parts</code> of the
   * message.  The values correspond to the fully qualified file names
   * for the attachments relative to the root directory under which the
   * files are stored.  The root <code>dataDirectory</code> is configured
   * as a JVM setting.
   */
  private Collection<String> attachments = new ArrayList<String>();

  /**
   * Default constructor.  Should be constructed only by {@link
   * MessageHandler} or {@link Message}.
   */
  MessageContent() {}

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
   * Return the <code>text</code> and <code>HTML</code> content parts of
   * the message with all <code>HTML</code> parts converted to plain text.
   *
   * @see #getTotalTextParts
   * @see #htmlToText
   * @return The collection of <code>text</code> parts.
   */
  public Collection<String> getBodyText()
  {
    int max = getTotalTextParts();
    Collection<String> parts = new ArrayList<String>( max );

    for ( int i = 0; i < max; ++i )
    {
      if ( text.containsKey( i ) )
      {
        parts.add( text.get( i ) );
      }
      else
      {
        parts.add( htmlToText( html.get( i ) ) );
      }
    }

    return parts;
  }

  /**
   * Return the <code>text</code> and <code>HTML</code> content parts of
   * the message with precedence given to the <code>HTML</code> parts when
   * both are available.
   *
   * @see #getTotalTextParts
   * @return The collection of <code>HTML</code> parts.
   */
  public Collection<String> getBodyHtml()
  {
    int max = getTotalTextParts();
    Collection<String> parts = new ArrayList<String>( max );

    for ( int i = 0; i < max; ++i )
    {
      if ( html.containsKey( i ) )
      {
        parts.add( html.get( i ) );
      }
      else
      {
        parts.add( text.get( i ) );
      }
    }

    return parts;
  }

  /**
   * Return the total number of textual body parts in the message.
   *
   * @return The total number of parts.
   */
  private int getTotalTextParts()
  {
    int max = text.size();

    for ( int key : text.keySet() )
    {
      max = ( key > max ) ? key : max;
    }

    for ( int key : html.keySet() )
    {
      max = ( key > max ) ? key : max;
    }

    return max;
  }

  /**
   * Convert the content of the HTML to plain text.
   *
   * @param html The html content that is to be converted.
   * @return The plain text version of the HTML.
   */
  private String htmlToText( String html )
  {
    StringBuilder builder = new StringBuilder();

    try
    {
      Parser parser = Parser.createParser( html, null );
      for ( NodeIterator iterator = parser.elements(); iterator.hasMoreNodes(); )
      {
        Node node = iterator.nextNode();
        builder.append( node.toPlainTextString() ).append( EOL );
      }
    }
    catch ( Throwable t )
    {
      MailSession.logger.log( Level.FINE,
          "Error parsing HTML for message: " + messageId, t );
    }

    return builder.toString();
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
   * Returns {@link #text}.
   *
   * @return The value/reference of/to text.
   */
  public Map<Integer,String> getText()
  {
    return text;
  }
  
  /**
   * Set {@link #text}.
   *
   * @param text The value to set.
   */
  public void setText( Map<Integer,String> text )
  {
    this.text = text;
  }
  
  /**
   * Returns {@link #html}.
   *
   * @return The value/reference of/to html.
   */
  public Map<Integer,String> getHtml()
  {
    return html;
  }
  
  /**
   * Set {@link #html}.
   *
   * @param html The value to set.
   */
  public void setHtml( Map<Integer,String> html )
  {
    this.html = html;
  }
  
  /**
   * Returns {@link #attachments}.
   *
   * @return The value/reference of/to attachments.
   */
  public Collection<String> getAttachments()
  {
    return attachments;
  }
  
  /**
   * Set {@link #attachments}.
   *
   * @param attachments The value to set.
   */
  public void setAttachments( Collection<String> attachments )
  {
    this.attachments = attachments;
  }

  /**
   * Return the contents of {@link #attachments} as a collection of file
   * objects.
   *
   * @param directory The directory under which the attachments are stored.
   * @throws IOException If errors are encountered while loading the files.
   */
  public Collection<File> getFiles( String directory )
    throws IOException
  {
    Collection<File> files = new ArrayList<File>( attachments.size() );
    for ( String file : attachments )
    {
      files.add( new File( directory + FILE_SEPARATOR + file ) );
    }

    return files;
  }
}
