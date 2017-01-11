package com.sptci.mail.filter;

import java.util.ArrayList;
import java.util.Collection;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * The rule that is to be applied against the <code>To</code> header
 * of a message.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-02
 * @version $Id: ToRule.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class ToRule extends StringComparisonRule
{
  /**
   * Create a new instance of the class using the specified value.
   *
   * @param clause The {@link #clause} to use.
   * @param message The {@link #message} to use.
   */
  public ToRule( Clause clause, Message message )
  {
    super( clause, message );
  }

  /**
   * Return the <code>To</code> values as a collection of strings.
   *
   * @throws MessagingException If errors are encountered while operating
   *   on the {@link #message}.
   */
  @Override
  public Collection<String> getProperties() throws MessagingException
  {
    Collection<String> collection = new ArrayList<String>();
    Address[] addresses = message.getRecipients( getRecipientType() );

    if ( addresses != null )
    {
      for ( Address address : addresses )
      {
        collection.add( address.toString() );
      }
    }

    return collection;
  }

  /**
   * Return the <code>Message.RecipientType</code> to use to fetch the
   * message recipients.
   *
   * @return The type to use.
   */
  protected Message.RecipientType getRecipientType()
  {
    return Message.RecipientType.TO;
  }
}
