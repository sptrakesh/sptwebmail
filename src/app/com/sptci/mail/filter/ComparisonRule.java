package com.sptci.mail.filter;

import java.util.Collection;

import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * The base class for rules that involve comparison on properties
 * of a message.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-02
 * @version $Id: ComparisonRule.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public abstract class ComparisonRule
{
  /**
   * The clause to which this rule is to be applied.
   */
  protected final Clause clause;

  /**
   * The message to which the clause is to be applied.
   */
  protected final Message message;

  /**
   * Create a new instance of the class using the specified value.
   *
   * @param clause The {@link #clause} to use.
   * @param message The {@link #message} to use.
   */
  public ComparisonRule( Clause clause, Message message )
  {
    this.clause = clause;
    this.message = message;
  }

  /**
   * Apply the rule to the clause.
   *
   * @throws MessagingException If errors are encountered while operating
   *   on the {@link #message}.
   */
  public abstract boolean apply() throws MessagingException;
}
