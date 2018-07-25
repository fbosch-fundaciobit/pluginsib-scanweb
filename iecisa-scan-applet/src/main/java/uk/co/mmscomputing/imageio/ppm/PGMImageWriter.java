package uk.co.mmscomputing.imageio.ppm;

import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

import uk.co.mmscomputing.image.operators.GrayScale;

public class PGMImageWriter extends ImageWriter implements PPMConstants{

  protected PGMImageWriter(ImageWriterSpi originatingProvider){
    super(originatingProvider);
  }

  public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType, ImageWriteParam param){
    return null;
  }

  public IIOMetadata convertStreamMetadata(IIOMetadata inData,ImageWriteParam param){
    return null;
  }

  public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType,ImageWriteParam param){
    return null;
  }

  public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param){
    return null;
  }

  public boolean canInsertImage(int imageIndex)throws IOException{
    super.canInsertImage(imageIndex);
    return (imageIndex==0);        //    can deal only with one image
  }

  public void pgm(ImageOutputStream out, BufferedImage image)throws IOException{
    int width  = image.getWidth();
    int height = image.getHeight();
    PixelGrabber grabber = new PixelGrabber(image, 0, 0, width, height, false);
    try{
      grabber.grabPixels();
    }catch(InterruptedException e){ 
      throw new IOException(getClass().getName()+".pgm: couldn't grab pixels from image !");
    }
    String header="P5\n"+width+" "+height+"\n255\n";
    out.write(header.getBytes());

    WritableRaster raster=image.getRaster();
    int k=0;
    byte[] bitmap = new byte[width*height];
    for(int y=0;y<height;y++){
      for(int x=0;x<width;x++){
        bitmap[k++]=(byte)raster.getSample(x,y,0);
      }
    }
    out.write(bitmap);            
  }

  public void write(IIOMetadata streamMetadata,IIOImage img,ImageWriteParam param)throws IOException{
    ImageOutputStream out=(ImageOutputStream)getOutput();
    if(!(img.getRenderedImage() instanceof BufferedImage)){
      throw new IOException(getClass().getName()+"write: Can only write BufferedImage objects");
    }
    BufferedImage image=(BufferedImage)img.getRenderedImage();
    if(image.getType()!=BufferedImage.TYPE_BYTE_GRAY){
      image=new GrayScale().filter(image); 
    }
    pgm(out,image);
  }

  public ImageWriteParam getDefaultWriteParam(){
    return new ImageWriteParam(getLocale());
  }
}
