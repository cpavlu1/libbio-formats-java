//
// ICSReader.java
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

import java.io.*;
import java.util.StringTokenizer;
import java.util.zip.*;
import loci.formats.*;
import loci.formats.codec.ByteVector;
import loci.formats.meta.FilterMetadata;
import loci.formats.meta.MetadataStore;

/**
 * ICSReader is the file format reader for ICS (Image Cytometry Standard)
 * files. More information on ICS can be found at http://libics.sourceforge.net
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/components/bio-formats/src/loci/formats/in/ICSReader.java">Trac</a>,
 * <a href="https://skyking.microscopy.wisc.edu/svn/java/trunk/components/bio-formats/src/loci/formats/in/ICSReader.java">SVN</a></dd></dl>
 *
 * @author Melissa Linkert linkert at wisc.edu
 */
public class ICSReader extends FormatReader {

  // -- Constants --

  /** Metadata field categories. */
  private static final String[] CATEGORIES = new String[] {
    "ics_version", "filename", "source", "layout", "representation",
    "parameter", "sensor", "history", "document", "view", "end"
  };

  /** Metadata field subcategories. */
  private static final String[] SUB_CATEGORIES = new String[] {
    "file", "offset", "parameters", "order", "sizes", "coordinates",
    "significant_bits", "format", "sign", "compression", "byte_order",
    "origin", "scale", "units", "labels", "SCIL_TYPE", "type", "model",
    "s_params", "laser", "gain1", "gain2", "gain3", "gain4", "dwell",
    "shutter1", "shutter2", "shutter3", "pinhole", "laser1", "laser2",
    "laser3", "objective", "PassCount", "step1", "step2", "step3", "view",
    "view1", "date", "GMTdate", "label", "software"
  };

  /** Metadata field sub-subcategories. */
  private static final String[] SUB_SUB_CATEGORIES = new String[] {
    "Channels", "PinholeRadius", "LambdaEx", "LambdaEm", "ExPhotonCnt",
    "RefInxMedium", "NumAperture", "RefInxLensMedium", "PinholeSpacing",
    "power", "wavelength", "name", "Type", "Magnification", "NA",
    "WorkingDistance", "Immersion", "Pinhole", "Channel 1", "Channel 2",
    "Channel 3", "Channel 4", "Gain 1", "Gain 2", "Gain 3", "Gain 4",
    "Shutter 1", "Shutter 2", "Shutter 3", "Position", "Size", "Port",
    "Cursor", "Color", "BlackLevel", "Saturation", "Gamma", "IntZoom",
    "Live", "Synchronize", "ShowIndex", "AutoResize", "UseUnits", "Zoom",
    "IgnoreAspect", "ShowCursor", "ShowAll", "Axis", "Order", "Tile", "scale",
    "DimViewOption"
  };

  // -- Fields --

  /** Current filename. */
  private String currentIcsId;
  private String currentIdsId;

  /** Current ICS file. */
  private Location icsIn;

  /** Number of bits per pixel. */
  private int bitsPerPixel;

  /** Flag indicating whether current file is v2.0. */
  private boolean versionTwo;

  /** Image data. */
  private byte[] data;

  /** Emission and excitation wavelength. */
  private String em, ex;

  private long offset;
  private boolean gzip;

  private boolean invertY;

  // -- Constructor --

  /** Constructs a new ICSReader. */
  public ICSReader() {
    super("Image Cytometry Standard", new String[] {"ics", "ids"});
  }

  // -- IFormatReader API methods --

  /* @see loci.formats.IFormatReader#isThisType(RandomAccessStream) */
  public boolean isThisType(RandomAccessStream stream) throws IOException {
    return false;
  }

  /* @see loci.formats.IFormatReader#fileGroupOption(String) */
  public int fileGroupOption(String id) throws FormatException, IOException {
    return FormatTools.MUST_GROUP;
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

    int bpp = bitsPerPixel / 8;
    int len = getSizeX() * getSizeY() * bpp * getRGBChannelCount();
    int pixel = bpp * getRGBChannelCount();
    int rowLen = w * pixel;

    in.seek(offset + no * len);

    if (!isRGB() && getSizeC() > 4) {
      // channels are stored interleaved, but because there are more than we
      // can display as RGB, we need to separate them
      if (!gzip && data == null) {
        data = new byte[len * getSizeC()];
        in.read(data);
      }

      for (int row=y; row<h + y; row++) {
        for (int col=x; col<w + x; col++) {
          System.arraycopy(data, bpp * (no + getSizeC() *
            (row * getSizeX() + col)), buf, bpp * (row * w + col), bpp);
        }
      }
    }
    else if (gzip) {
      if (x == 0 && getSizeX() == w) {
        System.arraycopy(data, len * no + y * rowLen, buf, 0, h * rowLen);
      }
      else {
        for (int row=y; row<h + y; row++) {
          System.arraycopy(data, len * no + row * getSizeX() * pixel +
            x * pixel, buf, row * rowLen, rowLen);
        }
      }
    }
    else {
      DataTools.readPlane(in, x, y, w, h, this, buf);
    }

    if (invertY) {
      byte[] row = new byte[rowLen];
      for (int r=0; r<h/2; r++) {
        System.arraycopy(buf, r*rowLen, row, 0, rowLen);
        System.arraycopy(buf, (h - r - 1)*rowLen, buf, r*rowLen, rowLen);
        System.arraycopy(row, 0, buf, (h - r - 1)*rowLen, rowLen);
      }
    }

    return buf;
  }

  /* @see loci.formats.IFormatReader#getUsedFiles() */
  public String[] getUsedFiles() {
    FormatTools.assertId(currentId, true, 1);
    if (versionTwo) {
      return new String[] {currentId};
    }
    return new String[] {currentIdsId, currentIcsId};
  }

  // -- IFormatHandler API methods --

  /* @see loci.formats.IFormatHandler#close() */
  public void close() throws IOException {
    super.close();
    icsIn = null;
    currentIcsId = null;
    currentIdsId = null;
    data = null;
    bitsPerPixel = 0;
    versionTwo = false;
    gzip = false;
    invertY = false;
  }

  // -- Internal FormatReader API methods --

  /* @see loci.formats.FormatReader#initFile(String) */
  protected void initFile(String id) throws FormatException, IOException {
    if (debug) debug("ICSReader.initFile(" + id + ")");
    super.initFile(id);

    status("Finding companion file");

    String icsId = id, idsId = id;
    int dot = id.lastIndexOf(".");
    String ext = dot < 0 ? "" : id.substring(dot + 1).toLowerCase();
    if (ext.equals("ics")) {
      // convert C to D regardless of case
      char[] c = idsId.toCharArray();
      c[c.length - 2]++;
      idsId = new String(c);
    }
    else if (ext.equals("ids")) {
      // convert D to C regardless of case
      char[] c = icsId.toCharArray();
      c[c.length - 2]--;
      icsId = new String(c);
    }

    if (icsId == null) throw new FormatException("No ICS file found.");
    Location icsFile = new Location(icsId);
    if (!icsFile.exists()) throw new FormatException("ICS file not found.");

    status("Checking file version");

    // check if we have a v2 ICS file - means there is no companion IDS file
    RandomAccessStream f = new RandomAccessStream(icsId);
    if (f.readString(17).trim().equals("ics_version\t2.0")) {
      in = new RandomAccessStream(icsId);
      versionTwo = true;
      f.close();
      f = null;
    }
    else {
      if (idsId == null) throw new FormatException("No IDS file found.");
      Location idsFile = new Location(idsId);
      if (!idsFile.exists()) throw new FormatException("IDS file not found.");
      currentIdsId = idsId;
      in = new RandomAccessStream(idsId);
    }

    currentIcsId = icsId;

    icsIn = icsFile;

    status("Reading metadata");

    String layoutSizes = null, layoutOrder = null, byteOrder = null;
    String rFormat = null, compression = null, scale = null;

    // parse key/value pairs from beginning of ICS file

    RandomAccessStream reader = new RandomAccessStream(icsIn.getAbsolutePath());
    StringTokenizer t;
    String token;
    String s = reader.readString((int) reader.length());
    reader.close();
    StringTokenizer st = new StringTokenizer(s, "\n");
    String line = st.nextToken();
    line = st.nextToken();
    boolean signed = false;
    while (line != null && !line.trim().equals("end")) {
      t = new StringTokenizer(line);
      StringBuffer key = new StringBuffer();
      while (t.hasMoreTokens()) {
        token = t.nextToken();
        boolean foundValue = true;
        for (int i=0; i<CATEGORIES.length; i++) {
          if (token.equals(CATEGORIES[i])) foundValue = false;
        }
        for (int i=0; i<SUB_CATEGORIES.length; i++) {
          if (token.equals(SUB_CATEGORIES[i])) foundValue = false;
        }
        for (int i=0; i<SUB_SUB_CATEGORIES.length; i++) {
          if (token.equals(SUB_SUB_CATEGORIES[i])) foundValue = false;
        }

        if (foundValue) {
          StringBuffer value = new StringBuffer();
          value.append(token);
          while (t.hasMoreTokens()) {
            value.append(" ");
            value.append(t.nextToken());
          }
          String k = key.toString().trim();
          String v = value.toString().trim();
          addMeta(k, v);

          if (k.equals("layout sizes")) layoutSizes = v;
          else if (k.equals("layout order")) layoutOrder = v;
          else if (k.equals("representation byte_order")) byteOrder = v;
          else if (k.equals("representation format")) rFormat = v;
          else if (k.equals("representation compression")) compression = v;
          else if (k.equals("parameter scale")) scale = v;
          else if (k.equals("representation sign")) signed = v.equals("signed");
          else if (k.equals("sensor s_params LambdaEm")) em = v;
          else if (k.equals("sensor s_params LambdaEx")) ex = v;
          else if (k.equals("history software") && v.indexOf("SVI") != -1) {
            // ICS files written by SVI Huygens are inverted on the Y axis
            invertY = true;
          }
        }
        else {
          key.append(token);
          key.append(" ");
        }
      }
      if (st.hasMoreTokens()) line = st.nextToken();
      else line = null;
    }

    status("Populating metadata");

    layoutOrder = layoutOrder.trim();
    StringTokenizer t1 = new StringTokenizer(layoutSizes);
    StringTokenizer t2 = new StringTokenizer(layoutOrder);

    core[0].rgb = layoutOrder.indexOf("ch") >= 0 &&
      layoutOrder.indexOf("ch") < layoutOrder.indexOf("x");
    core[0].dimensionOrder = "XY";

    // find axis sizes

    String imageToken;
    String orderToken;
    while (t1.hasMoreTokens() && t2.hasMoreTokens()) {
      imageToken = t1.nextToken().trim();
      orderToken = t2.nextToken().trim();
      if (orderToken.equals("bits")) {
        bitsPerPixel = Integer.parseInt(imageToken);
      }
      else if (orderToken.equals("x")) {
        core[0].sizeX = Integer.parseInt(imageToken);
      }
      else if (orderToken.equals("y")) {
        core[0].sizeY = Integer.parseInt(imageToken);
      }
      else if (orderToken.equals("z")) {
        core[0].sizeZ = Integer.parseInt(imageToken);
        core[0].dimensionOrder += "Z";
      }
      else if (orderToken.equals("ch")) {
        core[0].sizeC = Integer.parseInt(imageToken);
        if (getSizeC() > 4) core[0].rgb = false;
        core[0].dimensionOrder += "C";
      }
      else {
        core[0].sizeT = Integer.parseInt(imageToken);
        core[0].dimensionOrder += "T";
      }
    }

    if (getDimensionOrder().indexOf("Z") == -1) {
      core[0].dimensionOrder += "Z";
    }
    if (getDimensionOrder().indexOf("T") == -1) {
      core[0].dimensionOrder += "T";
    }
    if (getDimensionOrder().indexOf("C") == -1) {
      core[0].dimensionOrder += "C";
    }

    if (getSizeZ() == 0) core[0].sizeZ = 1;
    if (getSizeC() == 0) core[0].sizeC = 1;
    if (getSizeT() == 0) core[0].sizeT = 1;

    if (getImageCount() == 0) core[0].imageCount = 1;
    core[0].rgb = isRGB() && getSizeC() > 1;
    core[0].interleaved = isRGB();
    core[0].imageCount = getSizeZ() * getSizeT();
    if (!isRGB()) core[0].imageCount *= getSizeC();
    core[0].indexed = false;
    core[0].falseColor = false;
    core[0].metadataComplete = true;

    String endian = byteOrder;
    core[0].littleEndian = true;

    if (endian != null) {
      StringTokenizer endianness = new StringTokenizer(endian);
      String firstByte = endianness.nextToken();
      int first = Integer.parseInt(firstByte);
      core[0].littleEndian = rFormat.equals("real") ? first == 1 : first != 1;
    }

    String test = compression;
    gzip = (test == null) ? false : test.equals("gzip");

    if (versionTwo) {
      s = in.readLine();
      while(!s.trim().equals("end")) s = in.readLine();
    }

    offset = in.getFilePointer();

    // extra check is because some of our datasets are labeled as 'gzip', and
    // have a valid GZIP header, but are actually uncompressed
    if (gzip && (((in.length() - in.getFilePointer()) / (getImageCount()) <
      (getSizeX() * getSizeY() * bitsPerPixel / 8))))
    {
      data = new byte[(int) (in.length() - in.getFilePointer())];
      status("Decompressing pixel data");
      in.read(data);
      byte[] buf = new byte[8192];
      ByteVector v = new ByteVector();
      try {
        GZIPInputStream decompressor =
          new GZIPInputStream(new ByteArrayInputStream(data));
        int r = decompressor.read(buf, 0, buf.length);
        while (r > 0) {
          v.add(buf, 0, r);
          r = decompressor.read(buf, 0, buf.length);
        }
        data = v.toByteArray();
      }
      catch (IOException dfe) {
        throw new FormatException("Error uncompressing gzip'ed data", dfe);
      }
    }
    else gzip = false;

    status("Populating metadata");

    // Populate metadata store

    // The metadata store we're working with.
    MetadataStore store =
      new FilterMetadata(getMetadataStore(), isMetadataFiltered());
    store.setImageName("", 0);
    MetadataTools.setDefaultCreationDate(store, id, 0);

    // populate Pixels element

    String fmt = rFormat;

    if (bitsPerPixel < 32) core[0].littleEndian = !isLittleEndian();

    if (fmt.equals("real")) core[0].pixelType = FormatTools.FLOAT;
    else if (fmt.equals("integer")) {
      while (bitsPerPixel % 8 != 0) bitsPerPixel++;
      if (bitsPerPixel == 24 || bitsPerPixel == 48) bitsPerPixel /= 3;

      switch (bitsPerPixel) {
        case 8:
          core[0].pixelType = signed ? FormatTools.INT8 : FormatTools.UINT8;
          break;
        case 16:
          core[0].pixelType = signed ? FormatTools.INT16 : FormatTools.UINT16;
          break;
        case 32:
          core[0].pixelType = signed ? FormatTools.INT32 : FormatTools.UINT32;
          break;
      }
    }
    else {
      throw new RuntimeException("Unknown pixel format: " + format);
    }

    MetadataTools.populatePixels(store, this);

    String pixelSizes = scale;
    String o = layoutOrder;
    if (pixelSizes != null) {
      StringTokenizer pixelSizeTokens = new StringTokenizer(pixelSizes);
      StringTokenizer axisTokens = new StringTokenizer(o);

      Float pixX = null, pixY = null, pixZ = null, pixT = null;
      Integer pixC = null;

      while (pixelSizeTokens.hasMoreTokens()) {
        String axis = axisTokens.nextToken().trim().toLowerCase();
        String size = pixelSizeTokens.nextToken().trim();
        if (axis.equals("x")) pixX = new Float(size);
        else if (axis.equals("y")) pixY = new Float(size);
        else if (axis.equals("z")) pixZ = new Float(size);
        else if (axis.equals("t")) pixT = new Float(size);
        else if (axis.equals("ch")) {
          pixC = new Integer(new Float(size).intValue());
        }
      }
      store.setDimensionsPhysicalSizeX(pixX, 0, 0);
      store.setDimensionsPhysicalSizeY(pixY, 0, 0);
      store.setDimensionsPhysicalSizeZ(pixZ, 0, 0);
      store.setDimensionsTimeIncrement(pixT, 0, 0);
      store.setDimensionsWaveIncrement(pixC, 0, 0);
    }

    int[] emWave = new int[getSizeC()];
    int[] exWave = new int[getSizeC()];
    if (em != null) {
      StringTokenizer emTokens = new StringTokenizer(em);
      for (int i=0; i<getSizeC(); i++) {
        if (emTokens.hasMoreTokens()) {
          emWave[i] = (int) Float.parseFloat(emTokens.nextToken().trim());
        }
      }
    }
    if (ex != null) {
      StringTokenizer exTokens = new StringTokenizer(ex);
      for (int i=0; i<getSizeC(); i++) {
        if (exTokens.hasMoreTokens()) {
          exWave[i] = (int) Float.parseFloat(exTokens.nextToken().trim());
        }
      }
    }
  }

}