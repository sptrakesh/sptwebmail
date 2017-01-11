package com.sptci.mail.filter;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;

import com.sptci.addressbook.AddressBook;

/**
 * The class for rules that involve checking whether the sender of the
 * message does not exist in the users address book.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-04
 * @version $Id: SenderNotInAddressBookRule.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class SenderNotInAddressBookRule extends SenderInAddressBookRule
{
  /**
   * Create a new instance of the class with the specified values.
   *
   * @param clause The {@link #clause} to use.
   * @param message The {@link #message} to use.
   * @param addressBook The {@link #addressBook} to use.
   */
  public SenderNotInAddressBookRule( Clause clause, Message message,
      AddressBook addressBook )
  {
    super( clause, message, addressBook );
  }

  /**
   * Apply the rule to the clause.  Returns the negative of {@link
   * SenderInAddressBookRule#apply}.
   *
   * @throws MessagingException If errors are encountered while operating
   *   on the {@link #message}.
   */
  @Override
  public boolean apply() throws MessagingException
  {
    return !super.apply();
  }
}
