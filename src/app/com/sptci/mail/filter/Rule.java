package com.sptci.mail.filter;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

/**
 * An object used to represent a single rule that is to be applied to
 * a message.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-02-25
 * @version $Id: Rule.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class Rule implements Serializable
{
  /**
   * The unique identifier used for the rule.
   */
  private String name;

  /**
   * The collection of clauses associated with this rule.
   */
  private Clauses clauses;

  /**
   * The collection of actions used for this rule.
   */
  private Collection<Action> actions = new LinkedList<Action>();

  /**
   * Default constructor.  No special actions required.
   */
  public Rule() {}

  /**
   * Create a new instance of the class using the specified values.
   *
   * @param name The {@link #name} value to use.
   * @param clauses The {@link #clauses} value to use.
   * @param actions The {@link #actions} value to use.
   */
  public Rule( String name, Clauses clauses, Collection<Action> actions )
  {
    setName( name );
    setClauses( clauses );
    setActions( actions );
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
   * Returns {@link #name}.
   *
   * @return String The value/reference of/to name.
   */
  public String getName()
  {
    return name;
  }
  
  /**
   * Set {@link #name}.
   *
   * @param name The value to set.
   */
  public void setName( String name )
  {
    this.name = name;
  }
  
  /**
   * Returns {@link #clauses}.
   *
   * @return Clauses The value/reference of/to clauses.
   */
  public Clauses getClauses()
  {
    return clauses;
  }
  
  /**
   * Set {@link #clauses}.
   *
   * @param clauses The value to set.
   */
  public void setClauses( Clauses clauses )
  {
    this.clauses = clauses;
  }
  
  /**
   * Returns {@link #actions}.
   *
   * @return The value/reference of/to actions.
   */
  public Collection<Action> getActions()
  {
    return actions;
  }
  
  /**
   * Set {@link #actions}.
   *
   * @param actions The value to set.
   */
  public void setActions( Collection<Action> actions )
  {
    this.actions = actions;
  }
}
