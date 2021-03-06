//
// WATOPReader.java
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

import loci.common.DateTools;
import loci.common.RandomAccessInputStream;
import loci.formats.FormatException;
import loci.formats.FormatReader;
import loci.formats.FormatTools;
import loci.formats.MetadataTools;
import loci.formats.meta.MetadataStore;
import ome.xml.model.primitives.PositiveFloat;

/**
 * WATOPReader is the file format reader for WA Technology .wat files.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/in/WATOPReader.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/in/WATOPReader.java;hb=HEAD">Gitweb</a></dd></dl>
 */
public class WATOPReader extends FormatReader {

  // -- Constants --

  private static final int HEADER_SIZE = 4864;
  private static final String WAT_MAGIC_STRING = "0TOPSystem W.A.Technology";

  // -- Fields --

  // -- Constructor --

  /** Constructs a new WA Technology reader. */
  public WATOPReader() {
    super("WA Technology TOP", "wat");
    domains = new String[] {FormatTools.SEM_DOMAIN};
  }

  // -- IFormatReader API methods --

  /* @see loci.formats.IFormatReader#isThisType(RandomAccessInputStream) */
  public boolean isThisType(RandomAccessInputStream stream) throws IOException {
    final int blockLen = 25;
    if (!FormatTools.validStream(stream, blockLen, false)) return false;
    return stream.readString(blockLen).equals(WAT_MAGIC_STRING);
  }

  /**
   * @see loci.formats.IFormatReader#openBytes(int, byte[], int, int, int, int)
   */
  public byte[] openBytes(int no, byte[] buf, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    FormatTools.checkPlaneParameters(this, no, buf.length, x, y, w, h);

    in.seek(HEADER_SIZE);
    readPlane(in, x, y, w, h, buf);
    return buf;
  }

  // -- Internal FormatReader API methods --

  /* @see loci.formats.FormatReader#initFile(String) */
  protected void initFile(String id) throws FormatException, IOException {
    super.initFile(id);
    in = new RandomAccessInputStream(id);
    core[0].littleEndian = true;
    in.order(isLittleEndian());

    String comment = null;

    MetadataLevel level = getMetadataOptions().getMetadataLevel();
    if (level != MetadataLevel.MINIMUM) {
      in.seek(49);
      comment = in.readString(33);
    }

    in.seek(211);
    int year = in.readInt();
    int month = in.readInt();
    int day = in.readInt();
    int hour = in.readInt();
    int min = in.readInt();
    String date = year + "-" + month + "-" + day + "T" + hour + ":" + min;
    date = DateTools.formatDate(date, "yyyy-MM-dd'T'HH:mm");

    in.skipBytes(8);

    double xSize = in.readInt() / 100d;
    double ySize = in.readInt() / 100d;
    double zSize = in.readInt() / 100d;

    core[0].sizeX = in.readInt();
    core[0].sizeY = in.readInt();

    if (level != MetadataLevel.MINIMUM) {
      double tunnelCurrent = in.readInt() / 1000d;
      double sampleVolts = in.readInt() / 1000d;

      in.skipBytes(180);

      int originalZMax = in.readInt();
      int originalZMin = in.readInt();
      int zMax = in.readInt();
      int zMin = in.readInt();

      addGlobalMeta("Comment", comment);
      addGlobalMeta("X size (in um)", xSize);
      addGlobalMeta("Y size (in um)", ySize);
      addGlobalMeta("Z size (in um)", zSize);
      addGlobalMeta("Tunnel current (in amps)", tunnelCurrent);
      addGlobalMeta("Sample volts", sampleVolts);
      addGlobalMeta("Acquisition date", date);
    }

    core[0].pixelType = FormatTools.INT16;
    core[0].sizeC = 1;
    core[0].sizeZ = 1;
    core[0].sizeT = 1;
    core[0].imageCount = 1;
    core[0].dimensionOrder = "XYZCT";
    core[0].rgb = false;

    MetadataStore store = makeFilterMetadata();
    MetadataTools.populatePixels(store, this);

    store.setImageAcquiredDate(date, 0);

    if (level != MetadataLevel.MINIMUM) {
      store.setImageDescription(comment, 0);
      store.setPixelsPhysicalSizeX(
        new PositiveFloat((double) xSize / getSizeX()), 0);
      store.setPixelsPhysicalSizeY(
        new PositiveFloat((double) ySize / getSizeY()), 0);
    }
  }

}
