package com.sptci.mail.filestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.UIDFolder;

import com.sptci.io.FileUtilities;
import static com.sptci.io.FileUtilities.FILE_SEPARATOR;

import com.sptci.mail.MailSession;
import com.sptci.mail.Properties;

/**
 * A mail folder implementation that uses a filesystem directory as its
 * basis.  Messages belonging to this folder are stored as files, and
 * sub-folders as child directories.
 *
 * <p>&copy; Copyright 2008 <a href='http://sptci.com/' target='_new'>Sans Pareil Technologies, Inc</a>.</p>
 * @author Rakesh Vidyadharan 2008-03-08
 * @version $Id: FileFolder.java 52 2009-03-10 19:11:21Z sptrakesh $
 */
public class FileFolder extends Folder implements UIDFolder
{
  /**
   * The path separator character used by the mail store to denote folder
   * separators.  Defaults to maildir "." character.
   */
  protected final char separator;

  /** The mail session for the logged in user. */
  protected final MailSession session;

  /**
   * Create a new folder instance of the folder using the specified session.
   * Initialises {@link #separator} to '.'.
   *
   * @param directory The file that represents the folder.
   * @param store The {@link FileStore} instance that represents the local
   *   store.
   * @param session The {@link #session} value to use.
   */
  public FileFolder( final File directory, final Store store,
      final MailSession session )
  {
    super( store );
    this.session = session;
    separator = '.';
  }

  /**
   * Return the delimiter character that separates a Folder's pathname 
   * from the names of immediate subfolders.
   *
   * @return The separator character.
   * @throws MessagingException If errors are encountered while looking
   *   up the separator.
   */
  @Override
  public char getSeparator() throws MessagingException
  {
    return separator;
  }
}
