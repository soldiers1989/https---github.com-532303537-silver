package org.silver.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * 文件操作工具类
 */
public class FileUtils {
	public static void copyFileUsingFileChannels(File source, File dest) throws IOException {
		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		try {
			inputChannel = new FileInputStream(source).getChannel();
			outputChannel = new FileOutputStream(dest).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
		} finally {
			inputChannel.close();
			outputChannel.close();
		}
	}

	/**
	 * 删除文件夹下的所有文件
	 * 
	 * @param oldPath
	 */
	public static void deleteFile(File oldPath) {
		if (oldPath.isDirectory()) {
			System.out.println(oldPath + "是文件夹--");
			File[] files = oldPath.listFiles();
			for (File file : files) {
				deleteFile(file);
			}
		} else {
			oldPath.delete();
		}
	}

	public static void main(String[] args) {
		deleteFile(new File("E:/gadd-excel/"));
	}
}
