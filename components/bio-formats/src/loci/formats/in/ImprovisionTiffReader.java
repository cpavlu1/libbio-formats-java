//
// ImprovisionTiffReader.java
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
import java.util.*;
import loci.formats.*;
import loci.formats.meta.FilterMetadata;
import loci.formats.meta.MetadataStore;

/**
 * ImprovisionTiffReader is the file format reader for
 * Improvision TIFF files.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/components/bio-formats/src/loci/formats/in/ImprovisionTiffReader.java">Trac</a>,
 * <a href="https://skyking.microscopy.wisc.edu/svn/java/trunk/components/bio-formats/src/loci/formats/in/ImprovisionTiffReader.java">SVN</a></dd></dl>
 */
public class ImprovisionTiffReader extends BaseTiffReader {

  // -- Fields --

  private String[] cNames;
  private int pixelSizeT;
  private float pixelSizeX, pixelSizeY, pixelSizeZ;

  // -- Constructor --

  public ImprovisionTiffReader() {
    super("Improvision TIFF", new String[] {"tif", "tiff"});
    suffixSufficient = false;
  }

  // -- IFormatReader API methods --

  /* @see loci.formats.IFormatReader#isThisType(String, boolean) */
  public boolean isThisType(String name, boolean open) {
    if (!open) return false;
    try {
      RandomAccessStream stream = new RandomAccessStream(name);
      boolean isThisType = isThisType(stream);
      stream.close();
      return isThisType;
    }
    catch (IOException e) {
      if (debug) trace(e);
    }
    return false;
  }

  /* @see loci.formats.IFormatReader#isThisType(RandomAccessStream) */
  public boolean isThisType(RandomAccessStream stream) throws IOException {
    Hashtable ifd = TiffTools.getFirstIFD(stream);
    String comment = TiffTools.getComment(ifd);
    return comment != null && comment.indexOf("Improvision") != -1;
  }

  // -- IFormatHandler API methods --

  /* @see loci.formats.IFormatHandler#close() */
  public void close() throws IOException {
    super.close();
    cNames = null;
    pixelSizeT = 1;
  }

  // -- Internal BaseTiffReader API methods --

  /* @see BaseTiffReader#initStandardMetadata() */
  protected void initStandardMetadata() throws FormatException, IOException {
    super.initStandardMetadata();

    put("Improvision", "yes");

    // parse key/value pairs in the comment
    String comment = TiffTools.getComment(ifds[0]);
    String tz = null, tc = null, tt = null;
    if (comment != null) {
      StringTokenizer st = new StringTokenizer(comment, "\n");
      while (st.hasMoreTokens()) {
        String line = st.nextToken();
        int equals = line.indexOf("=");
        if (equals < 0) continue;
        String key = line.substring(0, equals);
        String value = line.substring(equals + 1);
        addMeta(key, value);
        if (key.equals("TotalZPlanes")) tz = value;
        else if (key.equals("TotalChannels")) tc = value;
        else if (key.equals("TotalTimepoints")) tt = value;
        else if (key.equals("XCalibrationMicrons")) {
          pixelSizeX = Float.parseFloat(value);
        }
        else if (key.equals("YCalibrationMicrons")) {
          pixelSizeY = Float.parseFloat(value);
        }
        else if (key.equals("ZCalibrationMicrons")) {
          pixelSizeZ = Float.parseFloat(value);
        }
      }
      metadata.remove("Comment");
    }

    if (tz == null) tz = "1";
    if (tc == null) tc = "1";
    if (tt == null) tt = "1";

    core[0].sizeZ = Integer.parseInt(tz);
    if (!isRGB()) core[0].sizeC = Integer.parseInt(tc);
    core[0].sizeT = Integer.parseInt(tt);

    if (getSizeZ() * getSizeC() * getSizeT() < getImageCount()) {
      core[0].sizeC = getImageCount();
    }

    // parse each of the comments to determine axis ordering

    long[] stamps = new long[ifds.length];
    int[][] coords = new int[ifds.length][3];

    cNames = new String[getSizeC()];

    for (int i=0; i<ifds.length; i++) {
      comment = TiffTools.getComment(ifds[i]);
      comment = comment.replaceAll("\r\n", "\n");
      comment = comment.replaceAll("\r", "\n");
      StringTokenizer st = new StringTokenizer(comment, "\n");
      String channelName = null;
      while (st.hasMoreTokens()) {
        String line = st.nextToken();
        int equals = line.indexOf("=");
        if (equals < 0) continue;
        String key = line.substring(0, equals);
        String value = line.substring(equals + 1);

        if (key.equals("TimeStampMicroSeconds")) {
          stamps[i] = Long.parseLong(value);
        }
        else if (key.equals("ZPlane")) coords[i][0] = Integer.parseInt(value);
        else if (key.equals("ChannelNo")) {
          coords[i][1] = Integer.parseInt(value);
        }
        else if (key.equals("TimepointName")) {
          coords[i][2] = Integer.parseInt(value);
        }
        else if (key.equals("ChannelName")) {
          channelName = value;
        }
        else if (key.equals("ChannelNo")) {
          int ndx = Integer.parseInt(value);
          if (cNames[ndx] == null) cNames[ndx] = channelName;
        }
      }
    }
    // determine average time per plane

    long sum = 0;
    for (int i=1; i<stamps.length; i++) {
      long diff = stamps[i] - stamps[i - 1];
      if (diff > 0) sum += diff;
    }
    pixelSizeT = (int) (sum / getSizeT());

    // determine dimension order

    core[0].dimensionOrder = "XY";
    for (int i=1; i<coords.length; i++) {
      int zDiff = coords[i][0] - coords[i - 1][0];
      int cDiff = coords[i][1] - coords[i - 1][1];
      int tDiff = coords[i][2] - coords[i - 1][2];

      if (zDiff > 0 && getDimensionOrder().indexOf("Z") < 0) {
        core[0].dimensionOrder += "Z";
      }
      if (cDiff > 0 && getDimensionOrder().indexOf("C") < 0) {
        core[0].dimensionOrder += "C";
      }
      if (tDiff > 0 && getDimensionOrder().indexOf("T") < 0) {
        core[0].dimensionOrder += "T";
      }
      if (core[0].dimensionOrder.length() == 5) break;
    }

    if (getDimensionOrder().indexOf("Z") < 0) core[0].dimensionOrder += "Z";
    if (getDimensionOrder().indexOf("C") < 0) core[0].dimensionOrder += "C";
    if (getDimensionOrder().indexOf("T") < 0) core[0].dimensionOrder += "T";
  }

  /* @see BaseTiffReader#initMetadataStore() */
  protected void initMetadataStore() throws FormatException {
    super.initMetadataStore();
    MetadataStore store =
      new FilterMetadata(getMetadataStore(), isMetadataFiltered());
    store.setImageName("", 0);
    MetadataTools.setDefaultCreationDate(store, getCurrentFile(), 0);

    MetadataTools.populatePixels(store, this);

    store.setDimensionsPhysicalSizeX(new Float(pixelSizeX), 0, 0);
    store.setDimensionsPhysicalSizeY(new Float(pixelSizeY), 0, 0);
    store.setDimensionsPhysicalSizeZ(new Float(pixelSizeZ), 0, 0);
    store.setDimensionsTimeIncrement(new Float(pixelSizeT / 1000000.0), 0, 0);
  }

}