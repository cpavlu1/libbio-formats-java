//
// CodecOptions.java
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

/**
 * Options for compressing and decompressing data.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/components/bio-formats/src/loci/formats/codec/CodecOptions.java">Trac</a>,
 * <a href="https://skyking.microscopy.wisc.edu/svn/java/trunk/components/bio-formats/src/loci/formats/codec/CodecOptions.java">SVN</a></dd></dl>
 */
public class CodecOptions {

  /** Width, in pixels, of the image. */
  public int width;

  /** Height, in pixels, of the image. */
  public int height;

  /** Number of channels. */
  public int channels;

  /** Number of bits per channel. */
  public int bitsPerSample;

  /** Indicates endianness of pixel data. */
  public boolean littleEndian;

  /** Indicates whether or not channels are interleaved. */
  public boolean interleaved;

  /**
   * If compressing, this is the maximum number of raw bytes to compress.
   * If decompressing, this is the maximum number of raw bytes to return.
   */
  public int maxBytes;

  /** Pixels for preceding image. */
  public byte[] previousImage;

  /** Return CodecOptions with reasonable default values. */
  public static CodecOptions getDefaultOptions() {
    CodecOptions options = new CodecOptions();
    options.littleEndian = false;
    options.interleaved = false;
    return options;
  }

}