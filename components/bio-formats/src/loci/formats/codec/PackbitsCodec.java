//
// PackbitsCodec.java
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

package loci.formats.codec;

import java.io.IOException;
import loci.formats.FormatException;
import loci.formats.RandomAccessStream;

/**
 * This class implements packbits decompression. Compression is not yet
 * implemented.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/components/bio-formats/src/loci/formats/codec/PackbitsCodec.java">Trac</a>,
 * <a href="https://skyking.microscopy.wisc.edu/svn/java/trunk/components/bio-formats/src/loci/formats/codec/PackbitsCodec.java">SVN</a></dd></dl>
 *
 * @author Melissa Linkert linkert at wisc.edu
 */
public class PackbitsCodec extends BaseCodec implements Codec {

  /**
   * Compresses a block of Packbits data. Currently not supported.
   *
   * @param data the data to be compressed
   * @param x length of the x dimension of the image data, if appropriate
   * @param y length of the y dimension of the image data, if appropriate
   * @param dims the dimensions of the image data, if appropriate
   * @param options options to be used during compression, if appropriate
   * @return The compressed data
   * @throws FormatException If input is not an Adobe data block.
   */
  public byte[] compress(byte[] data, int x, int y,
      int[] dims, Object options) throws FormatException
  {
    // TODO: Add compression support.
    throw new FormatException("Packbits Compression not currently supported");
  }

  /**
   * Decodes a PackBits (Macintosh RLE) compressed image.
   * Adapted from the TIFF 6.0 specification, page 42.
   *
   * @param in stream containing input data to be decompressed
   * @param options options to be used during compression, if appropriate
   * @return The decompressed data
   * @throws FormatException if data is not valid
   *   compressed data for this decompressor
   */
  public byte[] decompress(RandomAccessStream in, Object options)
    throws FormatException
  {
    int expected = ((Integer) options).intValue();
    ByteVector output = new ByteVector(1024);
    try {
      while (output.size() < expected && in.getFilePointer() < in.length()) {
        byte n = in.readByte();
        if (n >= 0) { // 0 <= n <= 127
          byte[] b = new byte[n + 1];
          in.read(b);
          output.add(b);
          b = null;
        }
        else if (n != -128) { // -127 <= n <= -1
          int len = -n + 1;
          byte inp = in.readByte();
          for (int i=0; i<len; i++) output.add(inp);
        }
      }
    }
    catch (IOException exc) {
      throw new FormatException(exc);
    }
    return output.toByteArray();
  }
}