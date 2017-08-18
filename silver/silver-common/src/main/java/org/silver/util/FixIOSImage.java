package org.silver.util;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
//import com.drew.metadata.exif.ExifDirectory;

public class FixIOSImage {

	//BufferedImage image = ImageIO.read(new File("C://Users/Administrator/Desktop/1.jpg")); 
	//int width = image.getWidth();  //图片的宽
	//int height = image.getHeight(); 
	
	
	public static int getRotateAngleForPhoto(File file){
		  int angle = 0;
	        
	        Metadata metadata = null;
	        try {
	            try {
					metadata = JpegMetadataReader.readMetadata(file);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	           
	            Directory directory = metadata.getDirectory(ExifIFD0Directory.class);
	                if(directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)){ 
	                  // Exif信息中方向　　
	                   int orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION); 
	                   // 原图片的方向信息
	                   if(6 == orientation ){
	                       //6旋转90
	                       angle = 90;
	                   }else if( 3 == orientation){
	                      //3旋转180
	                       angle = 180;
	                   }else if( 8 == orientation){
	                      //8旋转90
	                       angle = 270;
	                   }
	                }  
	        } catch (JpegProcessingException e) {
	            e.printStackTrace();
	        } catch (MetadataException e) {
	            e.printStackTrace();
	        }
	        return angle;
	}
	
	  public static int getRotateAngleForPhoto(String filePath){
	        
	        File file = new File(filePath);
	        int angle = 0;
	        Metadata metadata = null;
	        try {
	            try {
					metadata = JpegMetadataReader.readMetadata(file);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            Directory directory = metadata.getDirectory(ExifIFD0Directory.class);
	                if(directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)){ 
	                  // Exif信息中方向　　
	                   int orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION); 
	                   
	                   // 原图片的方向信息
	                   if(6 == orientation ){
	                       //6旋转90
	                       angle = 90;
	                   }else if( 3 == orientation){
	                      //3旋转180
	                       angle = 180;
	                   }else if( 8 == orientation){
	                      //8旋转90
	                       angle = 270;
	                   }
	                }  
	        } catch (JpegProcessingException e) {
	            e.printStackTrace();
	        } catch (MetadataException e) {
	            e.printStackTrace();
	        }
	       
	        return angle;
	    }
	
	  
	  public static String rotatePhonePhoto(String fullPath, int angel){
	        
	        BufferedImage src;
	        try {
	            src = ImageIO.read(new File(fullPath));
	            
	            int src_width = src.getWidth(null);
	            int src_height = src.getHeight(null);
	            
	            Rectangle rect_des = CalcRotatedSize(new Rectangle(new Dimension(src_width, src_height)), angel);

	            BufferedImage res = new BufferedImage(rect_des.width, rect_des.height,BufferedImage.TYPE_INT_RGB);
	            Graphics2D g2 = res.createGraphics();

	            g2.translate((rect_des.width - src_width) / 2,
	                    (rect_des.height - src_height) / 2);
	            g2.rotate(Math.toRadians(angel), src_width / 2, src_height / 2);

	            g2.drawImage(src, null, null);
	            
	            ImageIO.write(res, "jpg", new File(fullPath));
	            
	        } catch (IOException e) {
	            
	            e.printStackTrace();
	        }  
	        
	        return fullPath;
	        
	    }
	  public static Rectangle CalcRotatedSize(Rectangle src, int angel) {  
	        // if angel is greater than 90 degree, we need to do some conversion  
	        if (angel >= 90) {  
	            if(angel / 90 % 2 == 1){  
	                int temp = src.height;  
	                src.height = src.width;  
	                src.width = temp;  
	            }  
	            angel = angel % 90;  
	        }  
	        double r = Math.sqrt(src.height * src.height + src.width * src.width) / 2;  
	        double len = 2 * Math.sin(Math.toRadians(angel) / 2) * r;  
	        double angel_alpha = (Math.PI - Math.toRadians(angel)) / 2;  
	        double angel_dalta_width = Math.atan((double) src.height / src.width);  
	        double angel_dalta_height = Math.atan((double) src.width / src.height);  
	  
	        int len_dalta_width = (int) (len * Math.cos(Math.PI - angel_alpha  
	                - angel_dalta_width));  
	        int len_dalta_height = (int) (len * Math.cos(Math.PI - angel_alpha  
	                - angel_dalta_height));  
	        int des_width = src.width + len_dalta_width * 2;  
	        int des_height = src.height + len_dalta_height * 2;  
	        return new java.awt.Rectangle(new Dimension(des_width, des_height));  
	    }  
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(FixIOSImage.rotatePhonePhoto("C://Users/Administrator/Desktop/1.jpg",270));
	}

}
