package com.sptci.mail.filter;

import java.util.Date;
import java.util.Calendar;

import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * The base class for rules that involve date comparison on properties
 * of a message.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-02
 * @version $Id: DateComparisonRule.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public abstract class DateComparisonRule extends ComparisonRule
{
  /**
   * Create a new instance of the class using the specified value.
   *
   * @param clause The {@link #clause} to use.
   * @param message The {@link #message} to use.
   */
  public DateComparisonRule( Clause clause, Message message )
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
  protected abstract Date getProperty() throws MessagingException;

  /**
   * Apply the rule to the clause.
   *
   * @throws MessagingException If errors are encountered while operating
   *   on the {@link #message}.
   */
  public boolean apply() throws MessagingException
  {
    boolean result = false;

    Calendar value = Calendar.getInstance();
    value.add( Calendar.DAY_OF_YEAR, - Integer.parseInt( clause.getValue() ) );

    switch ( clause.getCondition() )
    {
      case IsLessThan:
        result = result || getProperty().after( value.getTime() );
        break;
      case IsGreaterThan:
        result = result || getProperty().before( value.getTime() );
        break;
    }

    return result;
  }
}
