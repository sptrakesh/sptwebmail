package com.sptci.mail.filter;

import java.io.Serializable;

/**
 * An object used to represent an action applied to a matching message.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-02-26
 * @version $Id: Action.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class Action implements Serializable
{
  /**
   * Enumeration of valid values for {@link #perform}.
   */
  public static enum Perform
  {
    MoveMessage { public String toString() { return "Move message"; } },
    CopyMessage { public String toString() { return "Copy message"; } },
    DeleteMessage { public String toString() { return "Delete message"; } },
    MarkAsRead { public String toString() { return "Mark as read"; } },
    ForwardMessage { public String toString() { return "Forward message"; } },
    ReplyToMessage { public String toString() { return "Reply to message"; } },
  }

  /**
   * The type of action that is to be performed on the message.
   */
  private Perform perform;

  /**
   * The value to use for performing the action.
   */
  private String value;

  /**
   * Default constructor.  No special actions required.
   */
  public Action() {}

  /**
   * Create a new instance with the specified values.
   *
   * @param perform The {@link #perform} value to use.
   */
  public Action( Perform perform )
  {
    this( perform, null );
  }

  /**
   * Create a new instance with the specified values.
   *
   * @param perform The {@link #perform} value to use.
   * @param value The {@link #value} value to use.
   */
  public Action( Perform perform, String value )
  {
    setPerform( perform );
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
   * Returns {@link #perform}.
   *
   * @return Perform The value/reference of/to perform.
   */
  public Perform getPerform()
  {
    return perform;
  }
  
  /**
   * Set {@link #perform}.
   *
   * @param perform The value to set.
   */
  public void setPerform( Perform perform )
  {
    this.perform = perform;
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
