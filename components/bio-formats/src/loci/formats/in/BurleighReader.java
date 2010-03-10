//
// BurleighReader.java
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
import loci.formats.meta.FilterMetadata;
import loci.formats.meta.MetadataStore;

/**
 * BurleighReader is the file format reader for Burleigh .img files.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/components/bio-formats/src/loci/formats/in/BurleighReader.java">Trac</a>,
 * <a href="https://skyking.microscopy.wisc.edu/svn/java/trunk/components/bio-formats/src/loci/formats/in/BurleighReader.java">SVN</a></dd></dl>
 */
public class BurleighReader extends FormatReader {

  // -- Fields --

  private int pixelsOffset;

  // -- Constructor --

  /** Constructs a new Burleigh reader. */
  public BurleighReader() {
    super("Burleigh", "img");
    domains = new String[] {FormatTools.SEM_DOMAIN};
    suffixSufficient = false;
    suffixNecessary = false;
  }

  // -- IFormatReader API methods --

  /* @see loci.formats.IFormatReader#isThisType(RandomAccessInputStream) */
  public boolean isThisType(RandomAccessInputStream stream) throws IOException {
    final int blockLen = 4;
    if (!FormatTools.validStream(stream, blockLen, false)) return false;
    byte[] magic = new byte[blockLen];
    stream.read(magic);
    return magic[0] == 0x66 && magic[1] == 0x66 && magic[3] == 0x40 &&
     (magic[2] == 0x46 || magic[2] == 0x06);
  }

  /**
   * @see loci.formats.IFormatReader#openBytes(int, byte[], int, int, int, int)
   */
  public byte[] openBytes(int no, byte[] buf, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    FormatTools.checkPlaneParameters(this, no, buf.length, x, y, w, h);

    in.seek(pixelsOffset);
    readPlane(in, x, y, w, h, buf);
    return buf;
  }

  /* @see loci.formats.IFormatReader#close(boolean) */
  public void close(boolean fileOnly) throws IOException {
    super.close(fileOnly);
    if (!fileOnly) {
      pixelsOffset = 0;
    }
  }

  // -- Internal FormatReader API methods --

  /* @see loci.formats.FormatReader#initFile(String) */
  protected void initFile(String id) throws FormatException, IOException {
    debug("BurleighReader.initFile(" + id + ")");
    super.initFile(id);
    in = new RandomAccessInputStream(id);

    core[0].littleEndian = true;
    in.order(isLittleEndian());

    int version = (int) in.readFloat() - 1;

    core[0].sizeX = in.readShort();
    core[0].sizeY = in.readShort();

    double xSize = 0d, ySize = 0d, zSize = 0d;
    double timePerPixel = 0d;
    int mode = 0, gain = 0, mag = 0;
    double sampleVolts = 0d, tunnelCurrent = 0d;

    if (version == 1) {
      in.seek(in.length() - 40);
      in.skipBytes(12);
      xSize = in.readInt();
      ySize = in.readInt();
      zSize = in.readInt();
      timePerPixel = in.readShort() * 50;
      mag = in.readShort();

      switch (mag) {
        case 3:
          mag = 10;
          break;
        case 4:
          mag = 50;
          break;
        case 5:
          mag = 250;
          break;
      }
      xSize /= mag;
      ySize /= mag;
      zSize /= mag;

      mode = in.readShort();
      gain = in.readShort();
      sampleVolts = in.readFloat() / 1000;
      tunnelCurrent = in.readFloat();
      pixelsOffset = 8;
    }
    else if (version == 2) {
      in.skipBytes(14);
      xSize = in.readInt();
      ySize = in.readInt();
      zSize = in.readInt();
      mode = in.readShort();
      in.skipBytes(4);
      gain = in.readShort();
      timePerPixel = in.readShort() * 50;
      in.skipBytes(12);
      sampleVolts = in.readFloat();
      tunnelCurrent = in.readFloat();
      double force = in.readFloat();

      addGlobalMeta("Force", force);
      pixelsOffset = 260;
    }

    addGlobalMeta("Version", version);
    addGlobalMeta("Image mode", mode);
    addGlobalMeta("Z gain", gain);
    addGlobalMeta("Time per pixel (s)", timePerPixel);
    addGlobalMeta("Sample volts", sampleVolts);
    addGlobalMeta("Tunnel current", tunnelCurrent);
    addGlobalMeta("Magnification", mag);

    core[0].pixelType = FormatTools.UINT16;
    core[0].sizeZ = 1;
    core[0].sizeC = 1;
    core[0].sizeT = 1;
    core[0].imageCount = 1;
    core[0].dimensionOrder = "XYZCT";

    MetadataStore store =
      new FilterMetadata(getMetadataStore(), isMetadataFiltered());
    MetadataTools.populatePixels(store, this);
    MetadataTools.setDefaultCreationDate(store, id, 0);

    store.setDimensionsPhysicalSizeX(new Double(xSize / getSizeX()), 0, 0);
    store.setDimensionsPhysicalSizeY(new Double(ySize / getSizeY()), 0, 0);
    store.setDimensionsPhysicalSizeZ(new Double(zSize / getSizeZ()), 0, 0);
  }

}