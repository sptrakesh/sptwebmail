package com.sptci.mail;

/**
 * A custom exception that is used to indicate that the user
 * could not authenticate successfully against the mail store.
 *
 * <p>&copy; Copyright 2007 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2007-03-16
 * @version $Id: AuthenticationFailedException.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class AuthenticationFailedException extends MessagingException
{
  /**
   * Constructs a new exception with null as its detail message. The 
   * cause is not initialized, and may subsequently be initialized by 
   * a call to Throwable.initCause(java.lang.Throwable).  Just uses
   * the super-class constructor.
   */
  public AuthenticationFailedException()
  {
    super();
  }

  /**
   * Constructs a new exception with the specified detail message. The 
   * cause is not initialized, and may subsequently be initialized by 
   * a call to Throwable.initCause(java.lang.Throwable).  Just calls
   * the corresponding super-class constructor.
   *
   * @param message The detail message. The detail message is 
   *   saved for later retrieval by the Throwable.getMessage() method.
   */
  public AuthenticationFailedException( String message )
  {
    super( message );
  }

  /**
   * Constructs a new exception with the specified detail message and 
   * cause.  Just uses the appropriate super-class constructor.
   *
   * <p>Note that the detail message associated with cause is not 
   * automatically incorporated in this exception's detail message.</p>
   *
   * @param message The detail message. The detail message is 
   *   saved for later retrieval by the Throwable.getMessage() method.
   * @param cause The cause (which is saved for later 
   *   retrieval by the Throwable.getCause() method). (A null value is 
   *   permitted, and indicates that the cause is nonexistent or 
   *   unknown.)
   */
  public AuthenticationFailedException( String message, Throwable cause )
  {
    super( message, cause );
  }

  /**
   * Constructs a new exception with the specified cause. The detail 
   * message is set as <code>( cause==null ? null : cause.toString() )
   * </code> (which typically contains the class and detail message of 
   * cause).  This constructor is useful for exceptions that are little 
   * more than wrappers for other throwables (for example, 
   * PrivilegedActionException).  Just uses the appropriate super-class
   * constructor.
   *
   * @param cause The cause (which is saved for later 
   *   retrieval by the Throwable.getCause() method). (A null value is 
   *   permitted, and indicates that the cause is nonexistent or 
   *   unknown.)
   */
  public AuthenticationFailedException( Throwable cause )
  {
    super( cause );
  }
}
