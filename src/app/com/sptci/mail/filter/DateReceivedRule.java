package com.sptci.mail.filter;

import java.util.Date;

import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * The class for rules that involve the date the message was received.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-02
 * @version $Id: DateReceivedRule.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class DateReceivedRule extends DateComparisonRule
{
  /**
   * Create a new instance of the class using the specified value.
   *
   * @param clause The {@link #clause} to use.
   * @param message The {@link #message} to use.
   */
  public DateReceivedRule( Clause clause, Message message )
  {
    super( clause, message );
  }

  /**
   * The method that must be implemented by sub-classes to retrieve the
   * relevant date property from the message.
   *
   * @return The date value against which the clause is to be applied.
   * @throws MessagingException If errors are encountered while operating
   *   on the {@link #message}.
   */
  @Override
  protected Date getProperty() throws MessagingException
  {
    return ( message.getReceivedDate() == null ) ?
      new Date() : message.getReceivedDate();
  }
}
