package com.sptci.mail.filter;

import java.util.Collection;

import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * The base class for rules that involve string comparison on properties
 * of a message.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-02
 * @version $Id: StringComparisonRule.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public abstract class StringComparisonRule extends ComparisonRule
{
  /**
   * Create a new instance of the class using the specified value.
   *
   * @param clause The {@link #clause} to use.
   * @param message The {@link #message} to use.
   */
  public StringComparisonRule( Clause clause, Message message )
  {
    super( clause, message );
  }

  /**
   * The method that must be implemented by sub-classes to retrieve a
   * collection of strings against which the {@link Clause#condition}
   * and {@link Clause#value} are to be applied.
   *
   * @return Collection<String> The collection of string values against
   *   which the clause is to be applied.
   * @throws MessagingException If errors are encountered while operating
   *   on the {@link #message}.
   */
  protected abstract Collection<String> getProperties()
    throws MessagingException;

  /**
   * Apply the rule to the clause.
   *
   * @see #getProperties
   * @throws MessagingException If errors are encountered while operating
   *   on the {@link #message}.
   */
  public boolean apply() throws MessagingException
  {
    boolean result = false;

    for ( String property : getProperties() )
    {
      switch ( clause.getCondition() )
      {
        case Contains:
          result = result || property.contains( clause.getValue() );
          break;
        case DoesNotContain:
          result = result || ( ! property.contains( clause.getValue() ) );
          break;
        case BeginsWith:
          result = result || property.startsWith( clause.getValue() );
          break;
        case EndsWith:
          result = result || property.endsWith( clause.getValue() );
          break;
        case IsEqualTo:
          result = result || property.equals( clause.getValue() );
          break;
      }
    }

    return result;
  }
}
