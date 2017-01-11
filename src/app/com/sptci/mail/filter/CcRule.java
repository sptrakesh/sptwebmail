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
 * @version $Id: CcRule.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class CcRule extends ToRule
{
  /**
   * Create a new instance of the class using the specified value.
   *
   * @param clause The {@link #clause} to use.
   * @param message The {@link #message} to use.
   */
  public CcRule( Clause clause, Message message )
  {
    super( clause, message );
  }

  /**
   * Return the <code>Message.RecipientType</code> to use to fetch the
   * message recipients.
   *
   * @return The type to use.
   */
  @Override
  protected Message.RecipientType getRecipientType()
  {
    return Message.RecipientType.CC;
  }
}
