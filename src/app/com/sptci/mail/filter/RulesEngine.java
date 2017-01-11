package com.sptci.mail.filter;

import java.util.ArrayList;
import java.util.Collection;

import javax.mail.Message;
import javax.mail.MessagingException;

import com.sptci.mail.Preferences;
import com.sptci.addressbook.AddressBook;

/**
 * The rules engine that is used to apply the configured rules for a 
 * user to a message.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-01
 * @version $Id: RulesEngine.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class RulesEngine
{
  /**
   * The rules that are configured for the user.
   */
  private Rules rules;

  /**
   * The message to which the rules are to be applied.
   */
  private Message message;

  /**
   * The {@link com.sptci.mail.Preferences} object associated with
   * the current user.
   */
  private Preferences preferences;

  /**
   * The {@link com.sptci.mail.addressBook.AddressBook} object
   * associated with the current user.
   */
  private AddressBook addressBook;

  /**
   * Create a new instance of the engine for the specified values.
   *
   * @param rules The {@link #rules} object to use.
   * @param preferences The {@link #preferences} object to use.
   * @param addressBook The {@link #addressBook} object to use.
   */
  public RulesEngine( Rules rules, Preferences preferences,
      AddressBook addressBook )
  {
    this.rules = rules;
    this.preferences = preferences;
    this.addressBook = addressBook;
  }

  /**
   * Create a new instance of the engine for the specified values.
   *
   * @param rules The {@link #rules} object to use.
   * @param preferences The {@link #preferences} object to use.
   * @param addressBook The {@link #addressBook} object to use.
   * @param message The {@link #message} to use.
   */
  public RulesEngine( Rules rules, Preferences preferences,
      AddressBook addressBook, Message message )
  {
    this( rules, preferences, addressBook );
    setMessage( message );
  }

  /**
   * Apply the configured rules to the message and perform any actions
   * configured.
   */
  public void apply() throws MessagingException
  {
    if ( message == null ) return;

    for ( Rule rule : rules.getRule() )
    {
      switch ( rule.getClauses().getOperator() )
      {
        case any:
          new AnyOperator( rule ).apply();
          break;
        case all:
          new AllOperator( rule ).apply();
          break;
      }
    }
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
  
  /**
   * Set {@link #message}.
   *
   * @param message The value to set.
   */
  public void setMessage( Message message )
  {
    this.message = message;
  }

  /**
   * The engine used to apply the <code>clauses</code> to the message
   * to determine whether any of the rules match the message.
   */
  private abstract class Operator
  {
    /**
     * The rule that is to be applied to the message if applicable.
     */
    final Rule rule;

    /**
     * Create a new instance of the class for the specified rule.
     *
     * @param rule The {@link RulesEngine.AnyOperator#rule} value to set.
     */
    Operator( Rule rule )
    {
      this.rule = rule;
    }

    /**
     * Apply the <code>clauses</code> against the <code>values</code> in
     * the message properties and determine whether actions are to be
     * applied to the message or not.
     *
     * @return The collection of <code>boolean</code> values that are the
     *   result of applying the {@link Clauses} to the {@link #message}.
     * @throws MessagingException If errors are encountered while operating
     *   on the {@link #message}.
     */
    Collection<Boolean> applyClauses() throws MessagingException
    {
      ArrayList<Boolean> results = new ArrayList<Boolean>();

      for ( Clause clause : rule.getClauses().getClause() )
      {
        switch ( clause.getProperty() )
        {
          case To:
            results.add( new ToRule( clause, message ).apply() );
            break;
          case Cc:
            results.add( new CcRule( clause, message ).apply() );
            break;
          case Subject:
            results.add( new SubjectRule( clause, message ).apply() );
            break;
          case DateSent:
            results.add( new DateSentRule( clause, message ).apply() );
            break;
          case DateReceived:
            results.add( new DateReceivedRule( clause, message ).apply() );
            break;
          case SenderInAddressBook:
            results.add( new SenderInAddressBookRule(
                  clause, message, addressBook ).apply() );
            break;
          case SenderNotInAddressBook:
            results.add( new SenderNotInAddressBookRule(
                  clause, message, addressBook ).apply() );
            break;
          case SenderInRecipients:
            results.add( new SenderInRecipientsRule(
                  clause, message, addressBook ).apply() );
            break;
          case SenderNotInRecipients:
            results.add( new SenderNotInRecipientsRule(
                  clause, message, addressBook ).apply() );
            break;
        }
      }

      return results;
    }

    /**
     * Apply the operator to the results of {@link #applyClauses} method.
     *
     * @return The results of applying the operator to the collection
     *   of results.
     */
    abstract boolean apply() throws MessagingException;
  }

  /**
   * The engine used to apply the <code>clauses</code> to the message
   * to determine whether any of the rules match the message.
   */
  private class AnyOperator extends Operator
  {
    /**
     * Create a new instance of the class for the specified rule.
     *
     * @param rule The {@link RulesEngine.AnyOperator#rule} value to set.
     */
    AnyOperator( Rule rule )
    {
      super( rule );
    }

    /**
     * Apply the operator to the results of {@link #applyClauses} method.
     *
     * @return The results of applying the operator to the collection
     *   of results.
     */
    @Override
    boolean apply() throws MessagingException
    {
      boolean result = false;

      for ( boolean value : applyClauses() )
      {
        result = result || value;
      }

      return result;
    }
  }

  /**
   * The engine used to apply all the <code>clauses</code> to the message
   * to determine whether the rules match the message.
   */
  private class AllOperator extends Operator
  {
    /**
     * Create a new instance of the class for the specified rule.
     *
     * @param rule The {@link RulesEngine.AllOperator#rule} value to set.
     */
    private AllOperator( Rule rule )
    {
      super( rule );
    }

    /**
     * Apply the operator to the results of {@link #applyClauses} method.
     *
     * @return The results of applying the operator to the collection
     *   of results.
     */
    @Override
    boolean apply() throws MessagingException
    {
      boolean result = true;

      for ( boolean value : applyClauses() )
      {
        result = result && value;
      }

      return result;
    }
  }
}
