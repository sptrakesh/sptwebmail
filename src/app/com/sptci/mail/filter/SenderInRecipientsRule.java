package com.sptci.mail.filter;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;

import com.sptci.addressbook.AddressBook;
import com.sptci.addressbook.Group;
import com.sptci.addressbook.Member;
import com.sptci.addressbook.Person;
import com.sptci.addressbook.TypeValue;

/**
 * The class for rules that involve checking whether the sender of the
 * message exists in the users address book.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-04
 * @version $Id: SenderInRecipientsRule.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class SenderInRecipientsRule extends AddressBookRule
{
  /**
   * Create a new instance of the class with the specified values.
   *
   * @param clause The {@link #clause} to use.
   * @param message The {@link #message} to use.
   * @param addressBook The {@link #addressBook} to use.
   */
  public SenderInRecipientsRule( Clause clause, Message message,
      AddressBook addressBook )
  {
    super( clause, message, addressBook );
  }

  /**
   * Apply the rule to the clause.
   *
   * @see #getEmail
   * @throws MessagingException If errors are encountered while operating
   *   on the {@link #message}.
   */
  @Override
  public boolean apply() throws MessagingException
  {
    boolean result = false;

    for ( String email : getEmail() )
    {
      Group group = addressBook.getGroups().get( "Recipients" );
      if ( group == null ) break;

      for ( Member member : group.getMember() )
      {
        if ( member.getMail() != null )
        {
          result = result || member.getMail().equals( email );
        }
        else if ( member.getNickname() != null )
        {
          Person person =
            addressBook.getPersons().get( member.getNickname() );
          if ( person != null )
          {
            for ( TypeValue typeValue : person.getMail() )
            {
              result = result || typeValue.getValue().equals( email );
            }
          }
        }
      }
    }

    return result;
  }
}
