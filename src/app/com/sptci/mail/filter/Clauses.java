package com.sptci.mail.filter;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

/**
 * An object used to represent a collection of clauses used to filter
 * a message.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-02-25
 * @version $Id: Clauses.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class Clauses implements Serializable
{
  /**
   * Enumeration of {@link #operator} values.
   */
  public static enum Operator { any, all }

  /**
   * The operator used to relate the {@link #clause} collection of {@link 
   * Clause} instances associated with this {@link Rule}.
   */
  private Operator operator;

  /**
   * The collection of {@link Clause} instances associated with this {@link
   * Rule}.
   */
  private Collection<Clause> clause;

  /**
   * Default constructor.  No special actions required.
   */
  public Clauses() {}

  /**
   * Create a new instance using the specified values.
   *
   * @param operator The {@link #operator} value to use.
   * @param clause The {@link #clause} value to use.
   */
  public Clauses( Operator operator, Collection<Clause> clause )
  {
    setOperator( operator );
    setClause( clause );
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
   * Returns {@link #operator}.
   *
   * @return The value/reference of/to operator.
   */
  public Operator getOperator()
  {
    return operator;
  }
  
  /**
   * Set {@link #operator}.
   *
   * @param operator The value to set.
   */
  public void setOperator( Operator operator )
  {
    this.operator = operator;
  }
  
  /**
   * Returns {@link #clause}.
   *
   * @return The value/reference of/to clause.
   */
  public Collection<Clause> getClause()
  {
    return clause;
  }
  
  /**
   * Set {@link #clause}.
   *
   * @param clause The value to set.
   */
  public void setClause( Collection<Clause> clause )
  {
    this.clause = clause;
  }
}
