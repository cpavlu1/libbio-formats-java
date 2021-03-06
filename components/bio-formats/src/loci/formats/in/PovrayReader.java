//
// PovrayReader.java
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

import loci.common.RandomAccessInputStream;
import loci.formats.FormatException;
import loci.formats.FormatReader;
import loci.formats.FormatTools;
import loci.formats.MetadataTools;
import loci.formats.meta.MetadataStore;

/**
 * PovrayReader is the file format reader for POV-Ray .df3 files.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/in/PovrayReader.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/in/PovrayReader.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @see http://www.povray.org/documentation/view/3.6.1/374/
 */
public class PovrayReader extends FormatReader {

  // -- Constants --

  private static final int HEADER_SIZE = 6;

  // -- Constructor --

  /** Constructs a new POV-Ray reader. */
  public PovrayReader() {
    super("POV-Ray", "df3");
    domains = new String[] {FormatTools.GRAPHICS_DOMAIN};
  }

  // -- IFormatReader API methods --

  /**
   * @see loci.formats.IFormatReader#openBytes(int, byte[], int, int, int, int)
   */
  public byte[] openBytes(int no, byte[] buf, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    FormatTools.checkPlaneParameters(this, no, buf.length, x, y, w, h);

    in.seek(HEADER_SIZE + FormatTools.getPlaneSize(this) * no);
    readPlane(in, x, y, w, h, buf);
    return buf;
  }

  // -- Internal FormatReader API methods --

  /* @see loci.formats.FormatReader#initFile(String) */
  protected void initFile(String id) throws FormatException, IOException {
    super.initFile(id);

    in = new RandomAccessInputStream(id);

    core[0].littleEndian = false;

    in.order(isLittleEndian());

    core[0].sizeX = in.readShort();
    core[0].sizeY = in.readShort();
    core[0].sizeZ = in.readShort();

    long fileLength = in.length() - HEADER_SIZE;
    int nBytes = (int) (fileLength / (getSizeX() * getSizeY() * getSizeZ()));

    core[0].pixelType = FormatTools.pixelTypeFromBytes(nBytes, false, false);
    core[0].sizeC = 1;
    core[0].sizeT = 1;
    core[0].rgb = false;
    core[0].dimensionOrder = "XYZCT";
    core[0].imageCount = getSizeZ() * getSizeC() * getSizeT();

    MetadataStore store = makeFilterMetadata();
    MetadataTools.populatePixels(store, this);
  }

}
