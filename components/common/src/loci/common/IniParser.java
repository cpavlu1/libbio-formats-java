//
// IniParser.java
//

/*
LOCI Common package: utilities for I/O, reflection and miscellaneous tasks.
Copyright (C) 2005-@year@ Melissa Linkert, Curtis Rueden and Chris Allan.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package loci.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple parser for INI configuration files. Supports pound (#) as comments,
 * and backslash (\) to continue values across multiple lines.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/common/src/loci/common/IniParser.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/common/src/loci/common/IniParser.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @author Curtis Rueden ctrueden at wisc.edu
 */
public class IniParser {

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(IniParser.class);

  private String commentDelimiter = "#";

  private boolean slashContinues = true;

  // -- IniParser API methods --

  /**
   * Set the String that identifies a comment.  Defaults to "#".
   */
  public void setCommentDelimiter(String delimiter) {
    commentDelimiter = delimiter;
  }

  /**
   * Set whether or not a '\' at the end of a line signifies that the
   * line continues on the following line.
   *
   * By default, a '\' does continue the line.
   */
  public void setBackslashContinuesLine(boolean slashContinues) {
    this.slashContinues = slashContinues;
  }

  /** Parses the INI-style configuration data from the given resource. */
  public IniList parseINI(String path)
    throws IOException
  {
    return parseINI(openTextResource(path));
  }

  /**
   * Parses the INI-style configuration data from the given resource,
   * using the given class to find the resource.
   */
  public IniList parseINI(String path, Class<?> c)
    throws IOException
  {
    return parseINI(openTextResource(path, c));
  }

  /** Parses the INI-style configuration data from the given input stream. */
  public IniList parseINI(BufferedReader in)
    throws IOException
  {
    IniList list = new IniList();
    IniTable attrs = null;
    String chapter = null;
    int no = 1;
    StringBuffer sb = new StringBuffer();
    while (true) {
      int num = readLine(in, sb);
      if (num == 0) break; // eof
      String line = sb.toString();
      LOGGER.debug("Line {}: {}", no, line);

      // ignore blank lines
      if (line.equals("")) {
        no += num;
        continue;
      }

      // check for chapter header
      if (line.startsWith("{")) {
        // strip curly braces
        int end = line.length();
        if (line.endsWith("}")) end--;
        chapter = line.substring(1, end);
        continue;
      }

      // check for section header
      if (line.startsWith("[")) {
        attrs = new IniTable();
        list.add(attrs);

        // strip brackets
        int end = line.length();
        if (line.endsWith("]")) end--;
        String header = line.substring(1, end);
        if (chapter != null) header = chapter + ": " + header;

        attrs.put(IniTable.HEADER_KEY, header);
        no += num;
        continue;
      }

      // parse key/value pair
      int equals = line.indexOf("=");
      if (equals < 0) throw new IOException(no + ": bad line");
      String key = line.substring(0, equals).trim();
      String value = line.substring(equals + 1).trim();
      attrs.put(key, value);
      no += num;
    }
    return list;
  }

  // -- Utility methods --

  /** Opens a buffered reader for the given resource. */
  public static BufferedReader openTextResource(String path) {
    return openTextResource(path, IniParser.class);
  }

  /** Opens a buffered reader for the given resource. */
  public static BufferedReader openTextResource(String path, Class<?> c) {
    return new BufferedReader(new InputStreamReader(
      c.getResourceAsStream(path)));
  }

  // -- Helper methods --

  /**
   * Reads (at least) one line from the given input stream
   * into the specified string buffer.
   *
   * @return number of lines read
   */
  private int readLine(BufferedReader in, StringBuffer sb) throws IOException {
    int no = 0;
    sb.setLength(0);
    while (true) {
      String line = in.readLine();
      if (line == null) break;
      no++;

      // strip comments
      if (commentDelimiter != null) {
        int comment = line.indexOf(commentDelimiter);
        if (comment >= 0) line = line.substring(0, comment);
      }

      // kill whitespace
      line = line.trim();

      // backslash signifies data continues to next line
      boolean slash = slashContinues && line.endsWith("\\");
      if (slash) line = line.substring(0, line.length() - 1).trim() + " ";
      sb.append(line);
      if (!slash) break;
    }
    return no;
  }

}
