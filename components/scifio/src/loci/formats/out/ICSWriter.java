//
// ICSWriter.java
//

/*
LOCI Bio-Formats package for reading and converting biological file formats.
Copyright (C) 2005-@year@ Melissa Linkert, Curtis Rueden, Chris Allan,
Eric Kjellman and Brian Loranger.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU Library General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Library General Public License for more details.

You should have received a copy of the GNU Library General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package loci.formats.out;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import loci.common.RandomAccessInputStream;
import loci.common.RandomAccessOutputStream;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.FormatWriter;
import loci.formats.MetadataTools;
import loci.formats.meta.MetadataRetrieve;

/**
 * ICSWriter is the file format writer for ICS files.  It writes ICS version 1
 * and 2 files.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/out/ICSWriter.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/out/ICSWriter.java;hb=HEAD">Gitweb</a></dd></dl>
 */
public class ICSWriter extends FormatWriter {

  // -- Fields --

  private long dimensionOffset;
  private int dimensionLength;
  private long pixelOffset;
  private int lastPlane = -1;
  private RandomAccessOutputStream pixels;

  // -- Constructor --

  public ICSWriter() {
    super("Image Cytometry Standard", new String[] {"ids", "ics"});
  }

  // -- IFormatWriter API methods --

  /**
   * @see loci.formats.IFormatWriter#saveBytes(int, byte[], int, int, int, int)
   */
  public void saveBytes(int no, byte[] buf, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    checkParams(no, buf, x, y, w, h);

    MetadataRetrieve meta = getMetadataRetrieve();
    int sizeX = meta.getPixelsSizeX(series).getValue().intValue();
    int sizeY = meta.getPixelsSizeY(series).getValue().intValue();
    int pixelType =
      FormatTools.pixelTypeFromString(meta.getPixelsType(series).toString());
    int bytesPerPixel = FormatTools.getBytesPerPixel(pixelType);
    int rgbChannels = getSamplesPerPixel();
    int planeSize = sizeX * sizeY * rgbChannels * bytesPerPixel;

    if (!initialized[series][no]) {
      initialized[series][no] = true;

      if (!isFullPlane(x, y, w, h)) {
        // write a dummy plane that will be overwritten in sections
        pixels.seek(pixelOffset + (no + 1) * planeSize);
      }
    }

    pixels.seek(pixelOffset + no * planeSize);
    if (isFullPlane(x, y, w, h) && (interleaved || rgbChannels == 1)) {
      pixels.write(buf);
    }
    else {
      pixels.skipBytes(bytesPerPixel * rgbChannels * sizeX * y);
      for (int row=0; row<h; row++) {
        ByteArrayOutputStream strip = new ByteArrayOutputStream();
        for (int col=0; col<w; col++) {
          for (int c=0; c<rgbChannels; c++) {
            int index = interleaved ? rgbChannels * (row * w + col) + c :
              w * (c * h + row) + col;
            strip.write(buf, index * bytesPerPixel, bytesPerPixel);
          }
        }
        pixels.skipBytes(bytesPerPixel * rgbChannels * x);
        pixels.write(strip.toByteArray());
        pixels.skipBytes(bytesPerPixel * rgbChannels * (sizeX - w - x));
      }
    }
    lastPlane = no;
  }

  /* @see loci.formats.IFormatWriter#canDoStacks() */
  public boolean canDoStacks() { return true; }

  /* @see loci.formats.IFormatWriter#getPixelTypes(String) */
  public int[] getPixelTypes(String codec) {
    return new int[] {FormatTools.INT8, FormatTools.UINT8, FormatTools.INT16,
      FormatTools.UINT16, FormatTools.INT32, FormatTools.UINT32,
      FormatTools.FLOAT};
  }

  // -- IFormatHandler API methods --

  /* @see loci.formats.IFormatHandler#setId(String) */
  public void setId(String id) throws FormatException, IOException {
    super.setId(id);

    if (checkSuffix(currentId, "ids")) {
      String metadataFile = currentId.substring(0, currentId.lastIndexOf("."));
      metadataFile += ".ics";
      out = new RandomAccessOutputStream(metadataFile);
    }

    if (out.length() == 0) {
      out.writeBytes("\t\n");
      if (checkSuffix(id, "ids")) {
        out.writeBytes("ics_version\t1.0\n");
      }
      else {
        out.writeBytes("ics_version\t2.0\n");
      }
      out.writeBytes("filename\t" + currentId + "\n");
      out.writeBytes("layout\tparameters\t6\n");

      MetadataRetrieve meta = getMetadataRetrieve();
      MetadataTools.verifyMinimumPopulated(meta, series);

      int pixelType =
        FormatTools.pixelTypeFromString(meta.getPixelsType(series).toString());

      dimensionOffset = out.getFilePointer();
      int[] sizes = overwriteDimensions(meta);
      dimensionLength = (int) (out.getFilePointer() - dimensionOffset);

      if (validBits != 0) {
        out.writeBytes("layout\tsignificant_bits\t" + validBits + "\n");
      }

      boolean signed = FormatTools.isSigned(pixelType);
      boolean littleEndian =
        !meta.getPixelsBinDataBigEndian(series, 0).booleanValue();

      out.writeBytes("representation\tformat\t" +
        (pixelType == FormatTools.FLOAT ? "real\n" : "integer\n"));
      out.writeBytes("representation\tsign\t" +
        (signed ? "signed\n" : "unsigned\n"));
      out.writeBytes("representation\tcompression\tuncompressed\n");
      out.writeBytes("representation\tbyte_order\t");
      for (int i=0; i<sizes[0]/8; i++) {
        if (littleEndian) {
          out.writeBytes((i + 1) + "\t");
        }
        else {
          out.writeBytes(((sizes[0] / 8) - i) + "\t");
        }
      }

      out.writeBytes("\nparameter\tscale\t1.000000\t");
      String order = meta.getPixelsDimensionOrder(series).getValue();
      StringBuffer units = new StringBuffer();
      for (int i=0; i<order.length(); i++) {
        char dim = order.charAt(i);
        Number value = 1.0;
        if (dim == 'X') {
          if (meta.getPixelsPhysicalSizeX(0) != null) {
            value = meta.getPixelsPhysicalSizeX(0).getValue();
          }
          units.append("micrometers\t");
        }
        else if (dim == 'Y') {
          if (meta.getPixelsPhysicalSizeY(0) != null) {
            value = meta.getPixelsPhysicalSizeY(0).getValue();
          }
          units.append("micrometers\t");
        }
        else if (dim == 'Z') {
          if (meta.getPixelsPhysicalSizeZ(0) != null) {
            value = meta.getPixelsPhysicalSizeZ(0).getValue();
          }
          units.append("micrometers\t");
        }
        else if (dim == 'T') {
          value = meta.getPixelsTimeIncrement(0);
          units.append("seconds\t");
        }
        out.writeBytes(value + "\t");
      }

      out.writeBytes("\nparameter\tunits\tbits\t" + units.toString() + "\n");
      out.writeBytes("\nend\n");
      pixelOffset = out.getFilePointer();
    }
    else if (checkSuffix(currentId, "ics")) {
      RandomAccessInputStream in = new RandomAccessInputStream(currentId);
      in.findString("\nend\n");
      pixelOffset = in.getFilePointer();
      in.close();
    }

    if (checkSuffix(currentId, "ids")) {
      pixelOffset = 0;
    }

    if (pixels == null) {
      pixels = new RandomAccessOutputStream(currentId);
    }
  }

  /* @see loci.formats.IFormatHandler#close() */
  public void close() throws IOException {
    if (lastPlane != getPlaneCount() - 1 && out != null) {
      overwriteDimensions(getMetadataRetrieve());
    }

    super.close();
    pixelOffset = 0;
    lastPlane = -1;
    dimensionOffset = 0;
    dimensionLength = 0;
    if (pixels != null) {
      pixels.close();
    }
    pixels = null;
  }

  // -- Helper methods --

  private int[] overwriteDimensions(MetadataRetrieve meta) throws IOException {
    out.seek(dimensionOffset);
    String order = meta.getPixelsDimensionOrder(series).toString();
    int sizeX = meta.getPixelsSizeX(series).getValue().intValue();
    int sizeY = meta.getPixelsSizeY(series).getValue().intValue();
    int z = meta.getPixelsSizeZ(series).getValue().intValue();
    int c = meta.getPixelsSizeC(series).getValue().intValue();
    int t = meta.getPixelsSizeT(series).getValue().intValue();
    int pixelType =
      FormatTools.pixelTypeFromString(meta.getPixelsType(series).toString());
    int bytesPerPixel = FormatTools.getBytesPerPixel(pixelType);
    int rgbChannels = getSamplesPerPixel();

    if (lastPlane < 0) lastPlane = z * c * t - 1;
    int[] pos = FormatTools.getZCTCoords(order, z, c, t, z * c * t, lastPlane);
    lastPlane = -1;

    StringBuffer dimOrder = new StringBuffer();
    int[] sizes = new int[6];
    int nextSize = 0;
    sizes[nextSize++] = 8 * bytesPerPixel;

    if (rgbChannels > 1) {
      dimOrder.append("ch\t");
      sizes[nextSize++] = pos[1] + 1;
    }

    for (int i=0; i<order.length(); i++) {
      if (order.charAt(i) == 'X') sizes[nextSize++] = sizeX;
      else if (order.charAt(i) == 'Y') sizes[nextSize++] = sizeY;
      else if (order.charAt(i) == 'Z') sizes[nextSize++] = pos[0] + 1;
      else if (order.charAt(i) == 'T') sizes[nextSize++] = pos[2] + 1;
      else if (order.charAt(i) == 'C' && dimOrder.indexOf("ch") == -1) {
        sizes[nextSize++] = pos[1] + 1;
        dimOrder.append("ch");
      }
      if (order.charAt(i) != 'C') {
        dimOrder.append(String.valueOf(order.charAt(i)).toLowerCase());
      }
      dimOrder.append("\t");
    }
    out.writeBytes("layout\torder\tbits\t" + dimOrder.toString() + "\n");
    out.writeBytes("layout\tsizes\t");
    for (int i=0; i<sizes.length; i++) {
      out.writeBytes(sizes[i] + "\t");
    }
    while ((out.getFilePointer() - dimensionOffset) < dimensionLength - 1) {
      out.writeBytes(" ");
    }
    out.writeBytes("\n");

    return sizes;
  }

}
