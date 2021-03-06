//
// PGMReader.java
//

/*
OME Bio-Formats package for reading and converting biological file formats.
Copyright (C) 2005-@year@ UW-Madison LOCI and Glencoe Software, Inc.

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

package loci.formats.in;

import java.io.IOException;
import java.util.StringTokenizer;

import loci.common.ByteArrayHandle;
import loci.common.DataTools;
import loci.common.RandomAccessInputStream;
import loci.common.RandomAccessOutputStream;
import loci.formats.FormatException;
import loci.formats.FormatReader;
import loci.formats.FormatTools;
import loci.formats.MetadataTools;
import loci.formats.meta.MetadataStore;

/**
 * PGMReader is the file format reader for Portable Gray Map (PGM) images.
 *
 * Much of this code was adapted from ImageJ (http://rsb.info.nih.gov/ij).
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/in/PGMReader.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/in/PGMReader.java;hb=HEAD">Gitweb</a></dd></dl>
 */
public class PGMReader extends FormatReader {

  // -- Constants --

  public static final char PGM_MAGIC_CHAR = 'P';

  // -- Fields --

  private boolean rawBits;

  /** Offset to pixel data. */
  private long offset;

  // -- Constructor --

  /** Constructs a new PGMReader. */
  public PGMReader() {
    super("Portable Gray Map", "pgm");
    domains = new String[] {FormatTools.GRAPHICS_DOMAIN};
    suffixNecessary = false;
  }

  // -- IFormatReader API methods --

  /* @see loci.formats.IFormatReader#isThisType(RandomAccessInputStream) */
  public boolean isThisType(RandomAccessInputStream stream) throws IOException {
    final int blockLen = 2;
    if (!FormatTools.validStream(stream, blockLen, false)) return false;
    return stream.read() == PGM_MAGIC_CHAR &&
      Character.isDigit((char) stream.read());
  }

  /**
   * @see loci.formats.IFormatReader#openBytes(int, byte[], int, int, int, int)
   */
  public byte[] openBytes(int no, byte[] buf, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    FormatTools.checkPlaneParameters(this, no, buf.length, x, y, w, h);

    in.seek(offset);
    if (rawBits) {
      readPlane(in, x, y, w, h, buf);
    }
    else {
      ByteArrayHandle handle = new ByteArrayHandle();
      RandomAccessOutputStream out = new RandomAccessOutputStream(handle);
      out.order(isLittleEndian());

      while (in.getFilePointer() < in.length()) {
        String line = in.readLine().trim();
        line = line.replaceAll("[^0-9]", " ");
        StringTokenizer t = new StringTokenizer(line, " ");
        while (t.hasMoreTokens()) {
          int q = Integer.parseInt(t.nextToken().trim());
          if (getPixelType() == FormatTools.UINT16) {
            out.writeShort(q);
          }
          else out.writeByte(q);
        }
      }

      out.close();
      RandomAccessInputStream s = new RandomAccessInputStream(handle);
      s.seek(0);
      readPlane(s, x, y, w, h, buf);
      s.close();
    }

    return buf;
  }

  /* @see loci.formats.IFormatReader#close(boolean) */
  public void close(boolean fileOnly) throws IOException {
    super.close(fileOnly);
    if (!fileOnly) {
      rawBits = false;
      offset = 0;
    }
  }

  // -- Internal FormatReader API methods --

  /* @see loci.formats.FormatReader#initFile(String) */
  protected void initFile(String id) throws FormatException, IOException {
    super.initFile(id);
    in = new RandomAccessInputStream(id);

    String magic = in.readLine().trim();

    boolean isBlackAndWhite = false;

    rawBits = magic.equals("P4") || magic.equals("P5") || magic.equals("P6");
    core[0].sizeC = (magic.equals("P3") || magic.equals("P6")) ? 3 : 1;
    isBlackAndWhite = magic.equals("P1") || magic.equals("P4");

    String line = readNextLine();

    line = line.replaceAll("[^0-9]", " ");
    int space = line.indexOf(" ");
    core[0].sizeX = Integer.parseInt(line.substring(0, space).trim());
    core[0].sizeY = Integer.parseInt(line.substring(space + 1).trim());

    if (!isBlackAndWhite) {
      int max = Integer.parseInt(readNextLine());
      if (max > 255) core[0].pixelType = FormatTools.UINT16;
      else core[0].pixelType = FormatTools.UINT8;
    }

    offset = in.getFilePointer();

    addGlobalMeta("Black and white", isBlackAndWhite);

    core[0].rgb = getSizeC() == 3;
    core[0].dimensionOrder = "XYCZT";
    core[0].littleEndian = true;
    core[0].interleaved = false;
    core[0].sizeZ = 1;
    core[0].sizeT = 1;
    core[0].imageCount = 1;
    core[0].indexed = false;
    core[0].falseColor = false;
    core[0].metadataComplete = true;

    MetadataStore store = makeFilterMetadata();
    MetadataTools.populatePixels(store, this);
    MetadataTools.setDefaultCreationDate(store, id, 0);
  }

  // -- Helper methods --

  private String readNextLine() throws IOException {
    String line = in.readLine().trim();
    while (line.startsWith("#") || line.length() == 0) {
      line = in.readLine().trim();
    }
    return line;
  }

}
