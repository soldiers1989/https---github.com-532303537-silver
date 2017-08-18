package org.silver.sys.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class XMLTest {
	
	
	public static void main(String[] args) {
		Element root = new Element("Root");
		Element head = new Element("Head");
		Element body = new Element("Body");
		root.addContent(head);
		root.addContent(body);
		
		Document Doc = new Document(root);
	
		Format format = Format.getPrettyFormat();
		XMLOutputter XMLOut = new XMLOutputter(format);
	
		String fileName = "001.xml";
		String path = "D://work/";
		File uploadFile = new File(path); //
	
		path = uploadFile.getPath() + "\\" + fileName;
		System.out.println(("生成的路径为：+" + path));
		
			try {
				XMLOut.output(Doc, new FileOutputStream(path));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}
