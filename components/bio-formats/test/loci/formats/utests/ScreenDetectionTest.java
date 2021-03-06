//
// ScreenDetectionTest.java
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

package loci.formats.utests;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;

import loci.common.Location;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.ClassList;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.in.ScreenReader;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.services.OMEXMLService;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/test/loci/formats/utests/ScreenDetectionTest.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/test/loci/formats/utests/ScreenDetectionTest.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @author Melissa Linkert melissa at glencoesoftware.com
 */
public class ScreenDetectionTest {

  private static String[][] SCREENS = new String[][] {
    {
      "screen1/plate/well_a1",
      "screen1/plate/well_a2",
      "screen1/plate/well_c6",
      "screen1/plate/well_f10",
    },
    {
      "screen2/plate1/b05",
      "screen2/plate1/e10",
      "screen2/plate1/p14",
      "screen2/plate2/f09",
      "screen2/plate2/g03"
    },
    {
      "screen3/plate1/test_A1_0",
      "screen3/plate1/test_A2_1",
      "screen3/plate1/test_A3_2",
      "screen3/plate1/test_A4_3",
    }
  };

  private static final int[] PLATE_COUNTS = new int[] {1, 1, 1};
  private static final int[] SERIES_COUNTS = new int[] {4, 3, 4};

  private ImageReader[] readers;
  private OMEXMLMetadata[] omexml;

  @BeforeClass
  public void setUp()
    throws DependencyException, FormatException, IOException, ServiceException
  {
    readers = new ImageReader[SCREENS.length];
    omexml = new OMEXMLMetadata[SCREENS.length];

    ServiceFactory factory = new ServiceFactory();
    OMEXMLService service = factory.getInstance(OMEXMLService.class);

    ClassList<IFormatReader> readerClasses =
      ImageReader.getDefaultReaderClasses();
    Class<IFormatReader>[] c =
      (Class<IFormatReader>[]) readerClasses.getClasses();

    ClassList<IFormatReader> validReaderClasses =
      new ClassList<IFormatReader>(IFormatReader.class);
    validReaderClasses.addClass(ScreenReader.class);
    for (Class<IFormatReader> readerClass : c) {
      validReaderClasses.addClass(readerClass);
    }

    for (int i=0; i<SCREENS.length; i++) {
      setupScreen(SCREENS[i]);

      readers[i] = new ImageReader(validReaderClasses);

      omexml[i] = service.createOMEXMLMetadata();
      readers[i].setMetadataStore(omexml[i]);
      readers[i].setId(SCREENS[i][0]);
    }
  }

  @Test
  public void testTypeDetection() throws FormatException, IOException {
    for (ImageReader reader : readers) {
      assertTrue(reader.getReader() instanceof ScreenReader);
    }
  }

  @Test
  public void testSeriesCounts() throws FormatException, IOException {
    for (int i=0; i<SCREENS.length; i++) {
      assertEquals(readers[i].getSeriesCount(), SERIES_COUNTS[i]);
    }
  }

  @Test
  public void testPlateCounts() {
    for (int i=0; i<SCREENS.length; i++) {
      int plateCount = omexml[i].getPlateCount();
      assertEquals(plateCount, PLATE_COUNTS[i]);
    }
  }

  // -- Helper methods --

  private void setupScreen(String[] screen) throws IOException {
    for (int i=0; i<screen.length; i++) {
      int lastSeparator = screen[i].lastIndexOf(File.separator);
      String dirPath = screen[i].substring(0, lastSeparator);
      String filename = screen[i].substring(lastSeparator + 1);

      String[] dirs = dirPath.split(File.separator);
      File dir = new File(System.getProperty("java.io.tmpdir"));
      for (String dirName : dirs) {
        dir = new File(dir, dirName);
        dir.mkdir();
        dir.deleteOnExit();
      }

      File file = new File(dir, filename + ".fake");
      file.createNewFile();
      screen[i] = file.getAbsolutePath();
      file.deleteOnExit();
    }
  }

}
