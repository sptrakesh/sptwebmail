package com.sptci.mail.filter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

import java.util.Collection;
import java.util.LinkedList;

import com.thoughtworks.xstream.XStream;

import com.sptci.util.StringUtilities;

/**
 * An object used to represent the collection of rules that are configured
 * for the user.
 *
 * <p>Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-02-25
 * @version $Id: Rules.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class Rules implements Serializable
{
  /**
   * The class used to marshall/unmarshall instances of this object.
   */
  static final transient XStream xstream;

  /**
   * Static initialiser for {@link #xstream}.
   */
  static
  {
    xstream = new XStream();
    xstream.alias( "Rules", Rules.class );
    xstream.alias( "Rule", Rule.class );
    xstream.alias( "Clauses", Clauses.class );
    xstream.alias( "Clauses.Operator", Clauses.Operator.class );
    xstream.alias( "Clause", Clause.class );
    xstream.alias( "Clause.Condition", Clause.Condition.class );
    xstream.alias( "Clause.Property", Clause.Property.class );
    xstream.alias( "Action", Action.class );
    xstream.alias( "Action.Perform", Action.Perform.class );
  }

  /**
   * The XML file used as the backing store for this instance.
   */
  private String file;

  /**
   * The collection of rule objects for the user.
   */
  private Collection<Rule> rule = new LinkedList<Rule>();

  /**
   * Default constructor.  Cannot be instantiated.
   *
   * @see #getInstance
   */
  private Rules() {}

  /**
   * Create a new instance of the class from the specified XML file.  If
   * the specified file does not exist a new file is created and an empty
   * instance is returned.
   *
   * @param file The XML file from which this instance is to be initialised.
   * @throws IOException If errors are encountered while reading the file.
   */
  public static final Rules getInstance( String file )
    throws IOException
  {
    Rules rules = null;

    try
    {
      rules =
        (Rules) xstream.fromXML( StringUtilities.fromFile( file ) );
    }
    catch ( IOException ioex )
    {
      rules = new Rules();
      rules.file = file;
      rules.save();
    }

    return rules;
  }

  /**
   * Save the instance to the backing XML store.
   *
   * @throws IOException If errors are encountered while saving the
   *   object graph to {@link #file}.
   */
  public void save() throws IOException
  {
    StringUtilities.toFile( xstream.toXML( this ), file );
  }

  /**
   * Return a string representation of this object.  Over-ridden to
   * return the native XML object graph.
   *
   * @return The string representation of this object.
   */
  @Override
  public String toString()
  {
    return xstream.toXML( this );
  }
  
  /**
   * Returns {@link #rule}.
   *
   * @return The value/reference of/to rule.
   */
  public Collection<Rule> getRule()
  {
    return rule;
  }
  
  /**
   * Set {@link #rule}.
   *
   * @param rule The value to set.
   */
  public void setRule( Collection<Rule> rule )
  {
    this.rule = rule;
  }
}
