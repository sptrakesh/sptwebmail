package com.sptci.mail;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thoughtworks.xstream.XStream;

import static com.sptci.io.FileUtilities.FILE_SEPARATOR;

/**
 * A representation of a message folder that can be stored on a local
 * repository.  This is useful to maintain a local cache of folders and messages
 * as well as to support organising messages into a local tree for
 * <code>POP3</code> stores.
 *
 * <p>&copy; Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-10
 * @version $Id: Folder.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class Folder implements Serializable, Comparable<Folder>
{
  /**
   * The logger to use to log errors or messages to.
   */
  protected static final Logger logger = MailSession.logger;

  /**
   * The name of the folder.
   */
  protected String name;

  /**
   * The full name of the folder.  This is used by <code>IMAP</code> stores
   * to designate the folder hierarchy.
   */
  protected String fullName;

  /**
   * The parent folder of this folder.  This is <code>null</code> for top
   * level folders.
   */
  protected Folder parent;

  /**
   * The total number of messages in the folder.
   */
  protected int messageCount;

  /**
   * The total number of new messages in the folder.
   */
  protected int newMessageCount;

  /**
   * The total number of unread messages in the folder.
   */
  protected int unreadMessageCount;

  /**
   * The total number of deleted messages in the folder.
   */
  protected int deletedMessageCount;

  /**
   * The collection of immediate children of this folder.
   */
  protected Collection<Folder> children = new ArrayList<Folder>();

  /**
   * Create a new top level folder with the specified name.  Cannot be
   * instantiated.  Use {@link FolderManager#create( String )} instead
   * to create a new folder.
   *
   * @param name The {@link #name} to use.
   * @throws MessagingException If errors are encountered while creating
   *   the folder.
   */
  protected Folder( String name ) throws MessagingException
  {
    this( name, null );
  }

  /**
   * Create a new folder with the specified name as a child of the specified
   * folder.  Cannot be instantiated.  Use {@link FolderManager#create(
   * String, Folder )} instead to create the child folder.
   *u
   * @param name The {@link #name} to use.
   * @param parent The {@link #parent} to use.
   * @throws MessagingException If errors are encountered while creating
   *   the folder.
   */
  protected Folder( String name, Folder parent ) throws MessagingException
  {
    setName( name );
    setParent( parent );
  }

  /**
   * Returns a string representation of the object.  Returns {@link #name}.
   *
   * @return The string representation of the folder.
   */
  @Override
  public String toString()
  {
    return name;
  }
  
  /**
   * Returns a XML representation of the object.  Delegates to {@link
   * #xstream}.
   *
   * @return The xml representation of the folder.
   */
  public String toXML()
  {
    return Message.xstream.toXML( this );
  }

  /**
   * Check the specified object for equality with this folder.  Checks
   * the {@link #fullName} with that of the specified object.
   *
   * @return boolean Returns <code>true</code> if the specified object is
   *   a folder and has the same full name.
   */
  @Override
  public boolean equals( Object object )
  {
    if ( this == object ) return true;
    boolean result = false;
    if ( object instanceof Folder )
    {
      Folder f = (Folder) object;
      result = getFullName().equals( ( (Folder) object ).getFullName() );
    }

    return result;
  }

  /**
   * Return a hash code for the folder.  Returns the hash code for
   * {@link #fullName}.
   *
   * @return int The hash code value for this folder.
   */
  @Override
  public int hashCode()
  {
    return getFullName().hashCode();
  }

  /**
   * Implementation of the <code>Comparable</code> interface.  Lexically
   * compares the values of {@link #fullName}.
   *
   * @return int Returns a positive, zero or negative number depending
   *   upon whether {@link #fullName} is lexically greater than, equal
   *   to or less than the value in the specified object.
   */
  public int compareTo( Folder folder )
  {
    return getFullName().compareTo( folder.getFullName() );
  }
  
  /**
   * Returns {@link #name}.
   *
   * @return The value/reference of/to name.
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
   * Returns {@link #fullName}.
   *
   * @see FolderManager#getSeparator
   * @see #getRelativePath
   * @return The value/reference of/to fullName.
   */
  public String getFullName()
  {
    return ( fullName == null ) ? name : fullName;
  }
  
  /**
   * Set {@link #fullName}.
   *
   * @param fullName The value to set.
   */
  protected void setFullName( String fullName )
  {
    this.fullName = fullName;
  }
  
  /**
   * Returns {@link #parent}.
   *
   * @return The value/reference of/to parent.
   */
  public Folder getParent()
  {
    return parent;
  }

  /**
   * Return whether the folder has a parent or not.  This can be used to 
   * check whether a folder is a top level folder or not.
   *
   * @return Return <code>true</code> if this folder is a child of another
   *   folder.
   */
  public boolean hasParent()
  {
    return ( parent == null ) ? false : true;
  }
  
  /**
   * Set {@link #parent}.  Moves the folder and update search indices as
   * appropriate.
   *
   * @param parent The value to set.
   */
  protected void setParent( Folder parent )
  {
    if ( this.parent != null && ! this.parent.equals( parent ) )
    {
      this.parent.removeChild( this );
      if ( parent != null ) parent.addChild( this );
    }

    this.parent = parent;
  }
  
  /**
   * Returns {@link #children}.
   *
   * @return The value/reference of/to children.
   */
  public Collection<Folder> getChildren()
  {
    return children;
  }
  
  /**
   * Set {@link #children}.
   *
   * @param children The value to set.
   */
  public void setChildren( Collection<Folder> children )
  {
    this.children = ( children == null ) ? new ArrayList<Folder>() : children;
  }

  /**
   * Add the specified folder as a child of this folder.
   *
   * @param folder The new child folder to add.
   */
  public void addChild( Folder folder )
  {
    if ( ! children.contains( folder ) )
    {
      children.add( folder );

      if ( ! folder.getParent().equals( this ) )
      {
        folder.setParent( this );
      }
    }
  }

  /**
   * Remove the specified folder as a child of this folder.
   *
   * @param folder The new child folder to remove.
   */
  public void removeChild( Folder folder )
  {
    children.remove( folder );
    if ( folder.getParent().equals( this ) ) folder.setParent( null );
  }
  
  /**
   * Returns {@link #messageCount}.
   *
   * @return The value/reference of/to messageCount.
   */
  public int getMessageCount()
  {
    return messageCount;
  }
  
  /**
   * Set {@link #messageCount}.
   *
   * @param messageCount The value to set.
   */
  protected void setMessageCount( int messageCount )
  {
    this.messageCount = messageCount;
  }
  
  /**
   * Returns {@link #newMessageCount}.
   *
   * @return The value/reference of/to newMessageCount.
   */
  public int getNewMessageCount()
  {
    return newMessageCount;
  }
  
  /**
   * Set {@link #newMessageCount}.
   *
   * @param newMessageCount The value to set.
   */
  protected void setNewMessageCount( int newMessageCount )
  {
    this.newMessageCount = newMessageCount;
  }
  
  /**
   * Returns {@link #unreadMessageCount}.
   *
   * @return The value/reference of/to unreadMessageCount.
   */
  public int getUnreadMessageCount()
  {
    return unreadMessageCount;
  }
  
  /**
   * Set {@link #unreadMessageCount}.
   *
   * @param unreadMessageCount The value to set.
   */
  protected void setUnreadMessageCount( int unreadMessageCount )
  {
    this.unreadMessageCount = unreadMessageCount;
  }
  
  /**
   * Returns {@link #deletedMessageCount}.
   *
   * @return The value/reference of/to deletedMessageCount.
   */
  public int getDeletedMessageCount()
  {
    return deletedMessageCount;
  }
  
  /**
   * Set {@link #deletedMessageCount}.
   *
   * @param deletedMessageCount The value to set.
   */
  protected void setDeletedMessageCount( int deletedMessageCount )
  {
    this.deletedMessageCount = deletedMessageCount;
  }
}
