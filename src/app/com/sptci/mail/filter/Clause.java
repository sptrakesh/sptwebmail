package com.sptci.mail.filter;

import java.io.Serializable;

/**
 * An object used to represent a clause used to filter a message.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-02-25
 * @version $Id: Clause.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class Clause implements Serializable
{
  /**
   * Enumeration of {@link #property} values.
   */
  public static enum Property
  {
    To,
    Cc,
    Subject,
    DateSent { public String toString() { return "Date sent"; } },
    DateReceived { public String toString() { return "Date received"; } },
    SenderInAddressBook { public String toString() { return "Sender is in address book"; } },
    SenderNotInAddressBook { public String toString() { return "Sender is not in address book"; } },
    SenderInRecipients { public String toString() { return "Sender is in previous recipients"; } },
    SenderNotInRecipients { public String toString() { return "Sender is not in previous recipients"; } }
  }

  /**
   * Enumeration of {@link #condition} values.
   */
  public static enum Condition
  {
    Contains,
    DoesNotContain { public String toString() { return "Does not contain"; } },
    BeginsWith { public String toString() { return "Begins with"; } },
    EndsWith { public String toString() { return "Ends with"; } },
    IsEqualTo { public String toString() { return "Is equal to"; } },
    IsLessThan { public String toString() { return "Is less than"; } },
    IsGreaterThan { public String toString() { return "Is greater than"; } }
  }

  /**
   * The property of the message that is to be matched against this clause.
   */
  private Property property;

  /**
   * The condition used to implement the match against {@link #property}
   * on {@link #value}.
   */
  private Condition condition;

  /**
   * The value against which {@link #property} is matched.
   */
  private String value;

  /**
   * Default constructor.  No special actions required.
   */
  public Clause() {}

  /**
   * Create a new instance using the specified values.
   *
   * @param property The {@link #property} value to use.
   * @param condition The {@link #condition} value to use.
   * @param value The {@link #value} value to use.
   */
  public Clause( Property property, Condition condition, String value )
  {
    setProperty( property );
    setCondition( condition );
    setValue( value );
  }

  /**
   * Return a string representation of this object.  Returns the XML
   * representation returned by XStream.
   *
   * @return The string representation of this object.
   */
  @Override
  public String toString()
  {
    return Rules.xstream.toXML( this );
  }
  
  /**
   * Returns {@link #property}.
   *
   * @return The value/reference of/to property.
   */
  public Property getProperty()
  {
    return property;
  }
  
  /**
   * Set {@link #property}.
   *
   * @param property The value to set.
   */
  public void setProperty( Property property )
  {
    this.property = property;
  }
  
  /**
   * Returns {@link #condition}.
   *
   * @return The value/reference of/to condition.
   */
  public Condition getCondition()
  {
    return condition;
  }
  
  /**
   * Set {@link #condition}.
   *
   * @param condition The value to set.
   */
  public void setCondition( Condition condition )
  {
    this.condition = condition;
  }
  
  /**
   * Returns {@link #value}.
   *
   * @return String The value/reference of/to value.
   */
  public String getValue()
  {
    return value;
  }
  
  /**
   * Set {@link #value}.
   *
   * @param value The value to set.
   */
  public void setValue( String value )
  {
    this.value = value;
  }
}
