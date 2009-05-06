//
// ConvertToOmeTiff.java
//

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;

import loci.common.RandomAccessInputStream;
import loci.formats.ImageReader;
import loci.formats.MetadataTools;
import loci.formats.TiffTools;
import loci.formats.in.FlexReader;
import loci.formats.in.TiffReader;
import loci.formats.meta.MetadataRetrieve;
import loci.formats.meta.MetadataStore;
import loci.formats.out.OMETiffWriter;

/** Converts the given files to OME-TIFF format. */
public class ExtractFlexMetdata {

  public static void main(String[] args) throws Exception {
    File dir;
    if (args.length != 1 || !(dir=new File(args[0])).canRead()) {
      System.out.println("Usage: java ExtractFlexMetdata dir");
      return;
    }
    for(File file:dir.listFiles()) {
      if(file.getName().endsWith(".flex"));{
      String id=file.getPath();
      int dot = id.lastIndexOf(".");
      String outId = (dot >= 0 ? id.substring(0, dot) : id) + ".xml";
      String xml = (String) TiffTools.getIFDValue(TiffTools.getIFDs(new RandomAccessInputStream(id))[0],
          65200, true, String.class);
      FileWriter writer =new FileWriter(new File(outId));
      writer.write(xml);
      writer.close();
      System.out.println("Writing header of: "+id);
      
      }
    }
    System.out.println("Done");
  }
}