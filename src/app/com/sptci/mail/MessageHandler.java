package com.sptci.mail;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Transport;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimePartDataSource;

import com.mirrorworlds.lifestreams.mail.tnef.DefaultContentTypeImpl;
import com.mirrorworlds.lifestreams.mail.tnef.TnefBuilder;
import com.mirrorworlds.lifestreams.mail.tnef.TnefContentTypes;
import com.mirrorworlds.lifestreams.mail.tnef.TnefMessageBuilder;
import com.mirrorworlds.lifestreams.mail.tnef.TnefMessage;
import com.mirrorworlds.lifestreams.mail.tnef.TnefStreamParser;
import com.mirrorworlds.lifestreams.mail.tnef.TnefStreamParserImpl;
import com.mirrorworlds.lifestreams.mail.tnef.internet.TnefMultipart;
import com.mirrorworlds.lifestreams.mail.tnef.internet.TnefMultipartDataSource;

import static com.sptci.io.FileUtilities.FILE_SEPARATOR;

/**
 * A handler for <code>javax.mail.Message</code> objects.  Contains methods
 * to retrieve displayable text, and other common utilities necessary to
 * build the display.
 *
 *<p>&copy; Copyright 2006 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2006-11-26
 * @version $Id: MessageHandler.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class MessageHandler
{
  /**
   * A constant for <code>multipart/alternative</code> messages.
   *
   * {@value}
   */
  public static final String MULTIPART_ALTERNATIVE = 
    "multipart/alternative";

  /**
   * A constant for <code>text/html</code> content type.
   *
   * {@value}
   */
  public static final String TEXT_HTML = "text/html";

  /**
   * A constant for <code>text/plain</code> content type.
   *
   * {@value}
   */
  public static final String TEXT_PLAIN = "text/plain";

  /**
   * A constant for <code>MS-TNEF</code> content type.
   *
   * {@value}
   */
  public static final String MS_TNEF = "application/ms-tnef";

  /**
   * A constant for the format in which dates will be displayed for 
   * messages.
   * 
   * {@value}
   */
  public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss z Z";

  /**
   * A constant used to indicate that message should not be saved.
   *
   * {@value}
   */
  public static final String DEFAULT_NAME = "Do not save";

  /**
   * The pattern that is used to parse and make url's in plain
   * text messages hyper-linked.
   */
  private static final Pattern httpPattern = Pattern.compile( "(http[^\\s]*)" );

  /**
   * The message from which information is to be retrieved.
   */
  final Message message;

  /**
   * The mail session to which the current user is bound.
   */
  final MailSession session;

  /**
   * A collection of attachments that are associated with the {@link
   * #message}.
  private final Collection<PartDownloadProvider> attachments;
   */

  /**
   * A collection of text content that are associated with the {@link
   * #message}.
   */
  private final ArrayList<String> text;

  /**
   * A collection of error message that are encountered while retrieving
   * data from the {@link #message}.
   */
  private final Collection<String> errors;

  /**
   * A counter used to keep an index to the {@link #text} collection.
   * This is used to try and suppress display of plain text content in
   * <code>multipart/alternative</code> parts.
   */
  private int textIndex;
  
  /**
   * Create a new handler for the specified message.
   *
   * @param message The message to use as the model for this handler.
   * @throws MessagingException If errors are encountered while retrieving
   *   data from the message store.
   * @throws IOException If errors are encountered while reading the
   *   part content.
   */
  public MessageHandler( Message message, MailSession session ) 
    throws MessagingException, IOException
  {
    this.message = message;
    this.session = session;

    //attachments = new ArrayList<PartDownloadProvider>();
    text = new ArrayList<String>();
    errors = new ArrayList<String>();
    process( message );
  }

  /**
   * Returns the recipients of a message as a comma-delimited String.
   * 
   * @param type The recipient type
   * @return A delimited string with all the recipients.
   * @throws MessagingException If errors are encountered while retrieving
   *   data from the message store.
   */
  public String getRecipients( Message.RecipientType type )
    throws MessagingException 
  {
    return getRecipients( type, false );
  }

  /**
   * Returns the recipients of a message as a comma-delimited String.
   * Over-ridden to enable omitting the current user from the returned
   * list.
   * 
   * @see #processAddress
   * @param type The recipient type
   * @param omitUser A flag to indicate whether the current user should be
   *   omitted from the list.  Specify <code>true</code> to omit the
   *   user.
   * @return A delimited string with all the recipients.
   * @throws MessagingException If errors are encountered while retrieving
   *   data from the message store.
   */
  public String getRecipients( Message.RecipientType type, boolean omitUser )
    throws MessagingException 
  {
    return processAddress( message.getRecipients( type ), omitUser );
  }

  /**
   * Returns the sender of the message.  If no sender is found (eg. when
   * sent from a mailing list manager), parse the message headers to 
   * retrieve the information.  This method differs from {@link #getFrom}
   * in that this method returns only a single address.
   * 
   * @return The sender of the message.
   * @throws MessagingException If errors are encountered while retrieving
   *   data from the message store.
   */
  public String getSender() throws MessagingException
  {
    String sender = null;
    Address[] address = message.getFrom();
    if ( address != null )
    {
      sender = address[0].toString();
    }

    return sender;
  }

  /**
   * Returns the sender(s) of the message.  If no sender is found (eg. when
   * sent from a mailing list manager), parse the message headers to 
   * retrieve the information.
   *
   * @return The senders of the message.
   * @throws MessagingException If errors are encountered while retrieving
   *   data from the message store.
   */
  public String getFrom() throws MessagingException
  {
    return processAddress( message.getFrom() );
  }

  /**
   * Return the date at which the message was received by the store.  If
   * this is not available attempt to fetch the date at which the message
   * was sent by the originating client.
   *
   * @return The date at which the message was received or sent.  The
   *   returned value may be <code>null</code>.
   * @throws MessagingException If errors are encountered while retrieving
   *   data from the message store.
   */
  public Date getDate() throws MessagingException
  {
    Date date = message.getReceivedDate();
    if ( date == null ) date = message.getSentDate();
    return date;
  }

  /**
   * Returns a comma delimited representation of the addresses contained
   * in the specified array
   *
   * @see #processAddress( Address[], boolean )
   * @param address The array of internet addresses.
   * @return The delimited string representation.
   * @throws MessagingException If errors are encountered while retrieving
   *   data from the message store.
   */
  private String processAddress( Address[] address )
    throws MessagingException
  {
    return processAddress( address, false );
  }

  /**
   * Returns a comma delimited representation of the addresses contained
   * in the specified array
   *
   * <p><b>Note:</b> Manually build the list as opposed to
   * <code>InternetAddress.toString( Address[] )</code> since that method
   * seems to return only the first item in the array.</p>
   *
   * @param address The array of internet addresses.
   * @param omitUser A flag to indicate whether the current user should be
   *   omitted from the list.  Specify <code>true</code> to omit the
   *   user.
   * @return The delimited string representation.
   * @throws MessagingException If errors are encountered while retrieving
   *   data from the message store.
   */
  private String processAddress( Address[] address, boolean omitUser )
    throws MessagingException
  {
    StringBuilder builder = new StringBuilder();
    String email = session.getEmailAddress();

    if ( address != null && address.length > 0 ) 
    {
      for ( int i = 0; i < address.length; ++i )
      {
        InternetAddress inetAddress = (InternetAddress) address[i];
        if ( ! omitUser || ! email.equals( inetAddress.getAddress() ) )
        {
          builder.append( inetAddress.toString() );
          if ( i < address.length - 1 ) 
          {
            builder.append( ", " );
          }
        }
      }
    }

    return builder.toString();
  }

  /**
   * Returns the <code>message-id</code> property of the {@link #message}.
   *
   * @return The message-id of the message.
   * @throws MessagingException If errors are encountered while retrieving
   *   data from the message store.
   */
  public String getMessageId() throws MessagingException
  {
    return ( (MimeMessage) message ).getMessageID();
  }

  /**
   * Returns the <code>message-id</code> property of the {@link #message}.
   *
   * @param message The message whose <code>message-id</code> is to be
   *   returned.
   * @return The message-id of the message.
   * @throws MessagingException If errors are encountered while retrieving
   *   data from the message store.
   */
  public static String getMessageId( Message message )
    throws MessagingException
  {
    return ( (MimeMessage) message ).getMessageID();
  }

  /**
   * Returns the subject of the message.
   *
   * @return The subject of the message.
   * @throws MessagingException If errors are encountered while retrieving
   *   data from the message store.
   */
  public String getSubject() throws MessagingException
  {
    return message.getSubject();
  }

  /**
   * Process {@link #message} and extract all the content (text and
   * binary) from it and populate {@link #attachments}, {@link
   * #text} and {@link #errors}.
   *
   * @param message The  message from which content is to be extracted.
   * @throws MessagingException If errors are encountered while retrieving
   *   data from the message store.
   * @throws IOException If errors are encountered while reading the
   *   part content.
   */
  private void process( Message message ) 
    throws MessagingException, IOException
  {
    Object content = message.getContent();
    if ( content instanceof String )
    {
      text.add( textIndex++, 
          fetchHtml( (String) content, message.getContentType() ) );
    } 
    else if ( content instanceof Multipart )
    {
      process( (Multipart) content, message.getContentType() );
    } 
    else 
    {
      // Unhandled type, should not generally occur.
      StringBuilder builder = new StringBuilder();
      builder.append( "Unparsable message" );
      builder.append( " content-id: " );
      builder.append( ( (MimeMessage) message ).getContentID() );
      errors.add( builder.toString() );
    }
  }
  
  /**
   * Fetches the <code>HTML</code> content if available from the
   * specified text.  If <code>contentType</code> is {@link #TEXT_PLAIN}
   * replace all newline characters with &lt;br/&gt;.
   * 
   * @param text The text to convert to HTML.
   * @param contentType The <code>content-type</code>
   *
   * @return a <code>Component</code> representation of the text
   */
  private String fetchHtml( String text, String contentType ) 
  {
    String result = null;

    if ( contentType != null && contentType.toLowerCase().contains( TEXT_HTML ) )
    {
      result = text;
    }
    else
    {
      Matcher matcher = httpPattern.matcher( text );
      int index = 0;
      StringBuilder builder = new StringBuilder( text.length() + 128 );
      while ( matcher.find( index ) )
      {
        String found = matcher.group();

        builder.append( text.substring( index, matcher.start() ) );
        builder.append( "<a href='" );
        builder.append( found );
        builder.append( "'>" );
        builder.append( found );
        builder.append( "</a>" );

        index = matcher.start() + found.length();
      }

      builder.append( text.substring( index ) );
      result = builder.toString();

      builder = new StringBuilder( result.length() + 64 );
      char chars[] = result.toCharArray();
      for ( int i = 0; i < chars.length; ++i )
      {
        if ( chars[i] == '\n' )
        {
          builder.append( "<br/>" );
        } 
        else if ( chars[i] >= 0x20 )
        {
          builder.append( chars[i] );
        }
      }

      result = builder.toString();
    }

    return result;
  }

  /**
   * An index used to keep track of previous plain text content.  This 
   * is used to blank out the plain text entry from {@link #text} if a 
   * HTML content is found.  This is unsafe to keep in class scope, but
   * seems to work better especially when dealing with TNEF content.  This
   * variable was moved from being a local variable in the {@link
   * #process( Multipart, String )} method.
   */
  private int plainIndex = -1;

  /**
   * Process the parts of the specified <code>multipart</code> message.
   * Retrieve text content as well as binary content and populate the
   * instance fields as appropriate.  Iterate recursively thorugh the body 
   * parts and fetch content as appropriate.
   *
   * @see #parseTnef
   * @param multipart The multipart content that is to be rendered.
   * @param contentType The content type of the parent of 
   *   <code>multipart</code>.  The parent may be another multipart or
   *   the original message.
   * @throws MessagingException If errors are encountered while retrieving
   *   data from the message store.
   * @throws IOException If errors are encountered while readin the
   *   part content.
   */
  private void process( Multipart multipart, String contentType ) 
    throws MessagingException, IOException
  {
    for ( int i = 0; i < multipart.getCount(); ++i )
    {
      BodyPart part = multipart.getBodyPart( i );
      Object partContent = part.getContent();

      if ( partContent instanceof String )
      {
        if ( part.getContentType() != null )
        {
          if ( part.getContentType().toLowerCase().contains( TEXT_PLAIN ) &&
              contentType.toLowerCase().contains( MULTIPART_ALTERNATIVE ) )
          {
            plainIndex = textIndex;
          }
          else if ( part.getContentType().toLowerCase().contains( TEXT_HTML ) && 
              ( plainIndex != -1 ) )
          {
            text.set( plainIndex, "<div></div>" );
            plainIndex = -1;
          }
        }

        text.add( textIndex++,
            fetchHtml( (String) partContent, part.getContentType() ) );
      }
      else if ( partContent instanceof Multipart )
      {
        Multipart mp = (Multipart) partContent;
        process( mp, mp.getContentType() );
      }
      else if ( part.getContent() instanceof Message )
      {
        process( (Message) part.getContent() );
      }
      else if ( ( part.getContentType() != null ) &&
          part.getContentType().contains( MS_TNEF ) )
      {
        TnefMultipartDataSource tnef = 
          new TnefMultipartDataSource( (MimePart) part );
        MimeMultipart mp = new TnefMultipart( tnef );

        if ( mp.getCount() == 0 )
        {
          // Probably a winmail.dat file with no parts
          String body = parseTnef( part );
          if ( body.length() > 0 ) text.add( textIndex++, body );
        }
        else
        {
          process( mp, mp.getContentType() );
        }
      }
      else if ( part.getFileName() != null )
      {
        //attachments.add( new PartDownloadProvider( part ) );
      }
      else
      {
        errors.add( part.getContentType() );
      }
    }
  }

  /**
   * Parse the specified TNEF part using LS-TNEF library.
   *
   * @see #wrapContent
   * @param bodyPart The body part that is to be parsed.
   * @throws MessagingException If errors are encountered while retrieving
   *   data from the message store.
   * @throws IOException If errors are encountered while readin the
   *   part content.
   */
  private String parseTnef( BodyPart bodyPart )
    throws MessagingException, IOException
  {
    TnefStreamParser parser = new TnefStreamParserImpl();
    TnefBuilder builder = new TnefMessageBuilder();
    parser.setBuilder(builder);
    TnefContentTypes contentTypes = new DefaultContentTypeImpl();
    builder.setContentTypes(contentTypes);
    parser.parse( bodyPart.getInputStream() );
    TnefMessage message = builder.getMessage();

    StringBuilder content = new StringBuilder();
    String text = message.getAttribute( "from" );
    if ( text != null && text.length() > 0 )
    {
      content.append( "<b>From:</b> " );
      content.append( text.trim() );
      content.append( "<br/>" );
    }

    text = message.getAttribute( "to" );
    if ( text != null && text.length() > 0 )
    {
      content.append( "<b>To:</b> " );
      content.append( text.trim() );
      content.append( "<br/>" );
    }

    text = message.getAttribute( "cc" );
    if ( text != null && text.length() > 0 )
    {
      content.append( "<b>Cc:</b> " );
      content.append( text.trim() );
      content.append( "<br/>" );
    }

    text = message.getAttribute( "subject" );
    if ( text != null && text.length() > 0 )
    {
      content.append( "<b>Subject:</b> " );
      content.append( text.trim() );
      content.append( "<br/>" );
    }

    text = message.getText();
    if ( text != null && text.length() > 0 )
    {
      content.append( "<br/><br/>" );
      content.append( text.trim() );
      content.append( "<br/>" );
    }

    return content.toString();
    //return wrapContent( content.toString() );
  }

  /**
   * Wrap the specified string in a <code>div</div> block to visually
   * separate the original content that is being quoted in the forwarded
   * message.
   *
   * @param content The content that is to be wrapped.
   * @return The wrapped content.
  public String wrapContent( String content )
  {
    StringBuilder builder = new StringBuilder();

    if ( ( content != null ) && ( content.length() > 0 ) )
    {
      builder = new StringBuilder( content.length() + 32 );
      builder.append( "<br/>" );
      builder.append( "<div style='display:none; color:white; background-color: white; height: 0; width: 0;'>" );
      builder.append( RichTextAreaPeer.START_IGNORE );
      builder.append( "</div>" );
      builder.append( "<br/>" );
      builder.append(
          "<div style='border-left: 1px blue solid; padding-left: 5px'>" );
      builder.append( content );
      builder.append( "</div>" );
      builder.append( "<br/>" );
      builder.append( "<div style='display:none; color:white; background-color: white; height: 0; width: 0;'>" );
      builder.append( RichTextAreaPeer.END_IGNORE );
      builder.append( "</div>" );
      builder.append( "<br/>" );
    }

    return builder.toString();
  }
   */

  /**
   * Send the {@link #message}.
   * 
   * @see #getFolder
   * @param message The message instance that is to be sent.
   * @param session The mail session to use to send the message.
   * @throws MessagingException If errors are encountered while sending
   *   the message.
   */
  public static void send( Message message, MailSession session )
    throws MessagingException, com.sptci.mail.MessagingException
  {
    if ( Properties.getInstance().protocol.contains( "imap" ) )
    {
      String folderName = session.getPreferences().getSentFolder();
      if ( DEFAULT_NAME.equals( folderName ) ||
          Properties.getInstance().protocol.contains( "pop" ) ) return;

      Folder folder = getFolder( folderName, session );
      if ( folder != null )
      {
        message.setFlag( Flags.Flag.SEEN, true );
        //folder.appendMessages( new Message[]{ message } );
        //folder.close( false );
      }
    }

    Transport.send( message );
  }

  /**
   * Save the {@link #message} as a draft.
   * 
   * 
   * @see #getFolder
   * @param message The message instance that is to be saved as a draft.
   * @param session The mail session to use to save the message.
   * @throws MessagingException If errors are encountered while saving
   *   the message.
   */
  public static void draft( Message message, MailSession session )
    throws MessagingException, com.sptci.mail.MessagingException
  {
    String folderName = session.getPreferences().getDraftFolder();
    if ( DEFAULT_NAME.equals( folderName ) ||
        Properties.getInstance().protocol.contains( "pop" ) ) return;

    Folder folder = getFolder( folderName, session );
    if ( folder != null )
    {
      //folder.appendMessages( new Message[]{ message } );
      //folder.close( false );
    }
  }

  /**
   * Fetch and open the specified folder.  A modified version of the
   * method in {@link FolderManager} to open it for <code>read-write</code>.
   * 
   * @param name The full name of the folder to open.
   * @param session The mail session to use to fetch the folder.
   * @return The appropriate folder instance or <code>null</code> if the
   *   name specified is <code>null</code> or empty.
   * @see FolderManager#getFolder
   * @throws MessagingException If errors are encountered while deleting
   *   the message.
   */
  private static Folder getFolder( String name, MailSession session )
    throws MessagingException, com.sptci.mail.MessagingException
  {
    if ( name == null || name.length() == 0 ) return null;

    Folder folder = session.getFolderManager().getFolder( name );

    //if ( folder.isOpen() ) folder.close( false );
    //folder.open( Folder.READ_WRITE );
    return folder;
  }
  
  /**
   * Returns {@link #attachments}.
   *
   * @return Collection The value/reference of/to attachments.
  public Collection<PartDownloadProvider> getAttachments()
  {
    return attachments;
  }
   */
  
  /**
   * Returns {@link #attachments} as a collection of files.
   *
   * @return Collection The collection of file attachments.
   */
  public Collection<File> getFiles() throws IOException
  {
    /*
    ArrayList<File> files = new ArrayList<File>( attachments.size() );
    for ( PartDownloadProvider attachment : attachments )
    {
      File file = new File( session.getTempDirectory() +
          FILE_SEPARATOR + attachment.getFileName() );
      attachment.writeFile( 
          new BufferedOutputStream( new FileOutputStream( file ) ) );
      files.add( file );
    }

    return files;
    */
    throw new IOException( "not implemented" );
  }
  
  /**
   * Returns {@link #text}.
   *
   * @return Collection The value/reference of/to text.
   */
  public Collection<String> getText()
  {
    return text;
  }
  
  /**
   * Returns {@link #errors}.
   *
   * @return Collection The value/reference of/to errors.
   */
  public Collection<String> getErrors()
  {
    return errors;
  }
  
  /**
   * Returns {@link #message}.
   *
   * @return Message The value/reference of/to message.
   */
  public Message getMessage()
  {
    return message;
  }
}
