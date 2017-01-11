package com.sptci.mail.filter;

import java.util.ArrayList;
import java.util.Collection;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import com.sptci.addressbook.AddressBook;

/**
 * The base class for rules that involve interactions with the users
 * address book.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-04
 * @version $Id: AddressBookRule.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public abstract class AddressBookRule extends ComparisonRule
{
  /**
   * The address book for the user.
   */
  protected final AddressBook addressBook;

  /**
   * Create a new instance of the class with the specified values.
   *
   * @param clause The {@link #clause} to use.
   * @param message The {@link #message} to use.
   * @param addressBook The {@link #addressBook} to use.
   */
  protected AddressBookRule( Clause clause, Message message,
      AddressBook addressBook )
  {
    super( clause, message );
    this.addressBook = addressBook;
  }

  /**
   * Return the <code>e-mail</code> address of the sender(s) of the message.
   *
   * @return The <code>e-mail</code> address values.
   * @throws MessagingException If errors are encountered while fetching
   *   the sender.
   */
  protected Collection<String> getEmail() throws MessagingException
  {
    Collection<String> list = new ArrayList<String>();

    if ( message.getFrom() != null )
    {
      for ( Address address : message.getFrom() )
      {
        if ( address instanceof InternetAddress )
        {
          list.add( ( (InternetAddress) address ).getAddress() );
        }
      }
    }

    return list;
  }
}
