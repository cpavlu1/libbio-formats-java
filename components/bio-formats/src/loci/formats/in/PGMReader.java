//
// PGMReader.java
//

/*
OME Bio-Formats package for reading and converting biological file formats.
Copyright (C) 2005-@year@ UW-Madison LOCI and Glencoe Software, Inc.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
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
import loci.formats.*;
import loci.formats.meta.FilterMetadata;
import loci.formats.meta.MetadataStore;

/**
 * PGMReader is the file format reader for Portable Gray Map (PGM) images.
 *
 * Much of this code was adapted from ImageJ (http://rsb.info.nih.gov/ij).
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/components/bio-formats/src/loci/formats/in/PGMReader.java">Trac</a>,
 * <a href="https://skyking.microscopy.wisc.edu/svn/java/trunk/components/bio-formats/src/loci/formats/in/PGMReader.java">SVN</a></dd></dl>
 */
public class PGMReader extends FormatReader {

  // -- Fields --

  private boolean rawBits;

  /** Offset to pixel data. */
  private long offset;

  // -- Constructor --

  /** Constructs a new PGMReader. */
  public PGMReader() {
    super("Portable Gray Map", "pgm");
    blockCheckLen = 1;
  }

  // -- IFormatReader API methods --

  /* @see loci.formats.IFormatReader#isThisType(RandomAccessStream) */
  public boolean isThisType(RandomAccessStream stream) throws IOException {
    if (!FormatTools.validStream(stream, blockCheckLen, false)) return false;
    return stream.read() == 'P';
  }

  /**
   * @see loci.formats.IFormatReader#openBytes(int, byte[], int, int, int, int)
   */
  public byte[] openBytes(int no, byte[] buf, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    FormatTools.assertId(currentId, true, 1);
    FormatTools.checkPlaneNumber(this, no);
    FormatTools.checkBufferSize(this, buf.length, w, h);

    in.seek(offset);
    if (rawBits) {
      DataTools.readPlane(in, x, y, w, h, this, buf);
    }
    else {
      int pt = 0;
      while (true) {
        String line = in.readLine().trim();
        line = line.replaceAll("[^0-9]", " ");
        StringTokenizer t = new StringTokenizer(line, " ");
        while (t.hasMoreTokens()) {
          int q = Integer.parseInt(t.nextToken().trim());
          if (getPixelType() == FormatTools.UINT16) {
            DataTools.unpackShort((short) q, buf, pt, isLittleEndian());
            pt += 2;
          }
          else {
            buf[pt++] = (byte) q;
          }
        }
      }
    }

    return buf;
  }

  // -- IFormatHandler API methods --

  /* @see loci.formats.IFormatHandler#close() */
  public void close() throws IOException {
    super.close();
    rawBits = false;
    offset = 0;
  }

  // -- Internal FormatReader API methods --

  /* @see loci.formats.FormatReader#initFile(String) */
  protected void initFile(String id) throws FormatException, IOException {
    super.initFile(id);
    in = new RandomAccessStream(id);

    String magic = in.readLine().trim();

    boolean isBlackAndWhite = false;

    rawBits = magic.equals("P4") || magic.equals("P5") || magic.equals("P6");
    core[0].sizeC = (magic.equals("P3") || magic.equals("P6")) ? 3 : 1;
    isBlackAndWhite = magic.equals("P1") || magic.equals("P4");

    String line = in.readLine().trim();
    while (line.startsWith("#") || line.length() == 0) line = in.readLine();

    line = line.replaceAll("[^0-9]", " ");
    core[0].sizeX =
      Integer.parseInt(line.substring(0, line.indexOf(" ")).trim());
    core[0].sizeY =
      Integer.parseInt(line.substring(line.indexOf(" ") + 1).trim());

    if (!isBlackAndWhite) {
      int max = Integer.parseInt(in.readLine().trim());
      if (max > 255) core[0].pixelType = FormatTools.UINT16;
      else core[0].pixelType = FormatTools.UINT8;
    }

    offset = in.getFilePointer();

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

    MetadataStore store =
      new FilterMetadata(getMetadataStore(), isMetadataFiltered());
    store.setImageName("", 0);
    MetadataTools.setDefaultCreationDate(store, id, 0);
    MetadataTools.populatePixels(store, this);
  }

}