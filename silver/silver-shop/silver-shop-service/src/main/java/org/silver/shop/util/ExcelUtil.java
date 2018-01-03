package org.silver.shop.util;
/**
 * 名称：Excel读写类
 * 功能：读取和改写已存在的Excel文件，暂不支持创建新文件。
 * 作者：何明发
 * 时间：2013-11-1
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;




public class ExcelUtil {
    
	private Workbook book=null;
	private HSSFWorkbook wb = null;
//	private XSSFWorkbook xb=null;
	private Sheet sheet = null; 
//	private XSSFSheet xsheet = null;

	private Row row = null;
	private XSSFRow xrow = null;
	private int sheetNum = 0;
	private int rowNum = 0;
	private int cellNum = 0;
	private int RowCount = 0;
	private int ColumnCount = 0;
	private FileInputStream fls = null;
	private FileOutputStream fileOut = null;
	private File file = null;

	public ExcelUtil() {
	}

	public ExcelUtil(File file) {
		this.file = file;
	}

	public Workbook getWb() {
		return book;
	}

	public void setWb(Workbook book) {
		this.book = book;
	}

	public Sheet getSheet() {
		return sheet;
	}

	public void setSheet(Sheet sheet) {
		this.sheet = sheet;
	}

	public Row getRow() {
		return row;
	}

	public void setRow(Row row) {
		this.row = row;
	}

	public int getSheetNum() {
		return sheetNum;
	}

	public void setSheetNum(int sheetNum) {
		this.sheetNum = sheetNum;
	}

	public int getRowNum() {
		return rowNum;
	}

	public void setRowNum(int rowNum) {
		this.rowNum = rowNum;
	}

	public int getCellNum() {
		return cellNum;
	}

	public void setCellNum(int cellNum) {
		this.cellNum = cellNum;
	}

	public FileInputStream getFls() {
		return fls;
	}

	public void setFls(FileInputStream fls) {
		this.fls = fls;
	}
	public FileOutputStream getFileOut() {
		return fileOut;
	}

	public void setFileOut(FileOutputStream fileOut) {
		this.fileOut = fileOut;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	// ---------------------------------------
	// -------------加载Excel文件--------------
	// ---------------------------------------
	public void open() {
		open(this.file);
	}

	public void open(File f) {
		try {
			this.file = f;
			if (!f.exists()) {
				System.out.println("文件不存在，创建一个");
				try {
					HSSFWorkbook workbook = new HSSFWorkbook();
					fileOut = new FileOutputStream(f.getPath());
					workbook.write(fileOut);
					fileOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				fls = new FileInputStream(f);
				book = new HSSFWorkbook(new POIFSFileSystem(fls));
				fls.close();
				// wb.createSheet("第一页");
				Sheet sheet = book.createSheet("KvOne");
				// 创建header页
				Header header = sheet.getHeader();
				header.setCenter("标题");
			} else if (f.isDirectory()) {
				System.out.println("文件路径被占用");
			} else {
				fls = new FileInputStream(f);
				try{
					book = new XSSFWorkbook(f); 
				}catch (Exception e){
					//e.printStackTrace();
					book = new HSSFWorkbook(new POIFSFileSystem(fls));
				}
				fls.close();
				System.out.println("文件存在，读取内容");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ---------------------------------------
	// -------------保存Excel文件--------------
	// ---------------------------------------
	public void save() {
		System.out.println(this.file);
		save(this.file);
	}

	public void save(File filePath) {
		try {
			this.file = filePath;
			if (fileOut == null) {
				fileOut = new FileOutputStream(filePath);
			}
			book.write(fileOut);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void closeExcel() {
		if (fileOut != null) {
			try {
				fileOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// ---------------------------------------
	// ---------------写入行内容---------------
	// ---------------------------------------
	public void writRow(int sheetNum, int rowNum, List<Object> l) {
		int n = l.size();
		if (sheetNum < 0 || rowNum < 0 || n <= 0) {
		//	System.out.println("参数出错！");
		} else {
			for (int i = 0; i < n; i++) {
				writCell(sheetNum, rowNum, i, l.get(i));
				save();
			}
		}
	}

	public void writRow(int rowNum, List<Object> l) {
		writRow(this.sheetNum, rowNum, l);
	}

	public void writRow(List<Object> l) {
		writRow(this.sheetNum, this.rowNum, l);
	}
    public void mergeCells(int row,int line,int endRow,int endLine){
    	 CellRangeAddress cra=new CellRangeAddress(row, line, endRow, endLine);     
    	 sheet.addMergedRegion(cra);
    }
   
	// ---------------------------------------
	// ---------------写入列内容---------------
	// ---------------------------------------
	public void writColumn(int sheetNum, int cellNum, List<Object> l) {
		int n = l.size();
		if (sheetNum < 0 || rowNum < 0 || n <= 0) {
			System.out.println("参数出错！");
		} else {
			for (int i = 0; i < n; i++) {
				writCell(sheetNum, i, cellNum, l.get(i));
			}
		}
	}

	public void writColumn(int cellNum, List<Object> l) {
		writColumn(this.sheetNum, cellNum, l);
	}

	public void writColumn(List<Object> l) {
		writColumn(this.sheetNum, this.cellNum, l);
	}

	// ---------------------------------------
	// ---------------写入单元格---------------
	// ---------------------------------------
	public void writCell(int sheetNum, int rowNum, int cellNum, Object message) {
		if (sheetNum < 0 || rowNum < 0 || cellNum < 0) {
		//	System.out.println("参数出错！");
		} else {
			try {
			
				sheet=	book.getSheetAt(sheetNum);
			
			} catch (IllegalArgumentException e) {
				sheet=	book.createSheet("sheet" + (sheetNum + 1));
			
			}
			// System.out.println(rowNum + "," + cellNum);
			if (rowNum > getRowCount() || rowNum == 0) {
					row = sheet.createRow(rowNum);
					RowCount++;
			//	System.out.println("创建行:" + row);
			} else {
				
					row = sheet.getRow(rowNum);
					if (row == null)
						row = sheet.createRow(rowNum);
				
				//System.out.println("读取行:" + row);
			}
			// save();
			   Cell cell = null;
				if (cellNum <= row.getLastCellNum()) {
					cell = row.getCell(cellNum);
					if (cell == null)
						cell = row.createCell(cellNum);
				} else {
					cell = row.createCell(cellNum);
				}
			if (message != null) {
				//System.out.println(message + "message");
				message = message + "";
				if (message.getClass().getName().equals("java.lang.Integer")) {
					cell.setCellValue((Integer) message);
				} else if (message.getClass().getName().equals("java.lang.Long")) {
					cell.setCellValue((Long) message);
				} else if (message.getClass().getName().equals("java.lang.Short")) {
					cell.setCellValue((Short) message);
				} else if (message.getClass().getName().equals("java.lang.String")) {
					cell.setCellValue((String) message);
				} else if (message.getClass().getName().equals("java.lang.Double")) {
					cell.setCellValue((Double) message);
				} else if (message.getClass().getName().equals("java.lang.Float")) {
					cell.setCellValue((Float) message);
				} else if (message.getClass().getName().equals("java.lang.Boolean")) {
					cell.setCellValue((Boolean) message);
				} else {
					cell.setCellValue(message + "");
				}

			}
		}
	}

	public void writCell(int rowNum, int cellNum, Object message) {
		writCell(this.sheetNum, rowNum, cellNum, message);
	}

	public void writCell(int cellNum, Object message) {
		writCell(this.sheetNum, this.rowNum, cellNum, message);
	}

	public void writCell(Object message) {
		writCell(this.sheetNum, this.rowNum, this.cellNum, message);
	}

	// ---------------------------------------
	// ------------读取工作表数值--------------
	// ---------------------------------------
	public int getSheetCount() {
		int sheetcount = -1;
		sheetcount = book.getNumberOfSheets();
		return sheetcount;
	}

	// ---------------------------------------
	// --------------读取行数------------------
	// ---------------------------------------
	public int getRowCount() {
		return getRowCount(this.sheetNum);
	}

	public int getRowCount(int sheetNum) {
		if (book != null) {
			Sheet sheet = book.getSheetAt(sheetNum);
			int rowcount = -1;
			rowcount = sheet.getLastRowNum();
			if (RowCount > rowcount) {
				return RowCount;
			}
			return rowcount;
		} else {
			System.out.println("没有加载文件");
		}
		return 0;
	}

	// ---------------------------------------
	// --------------读取列数------------------
	// ---------------------------------------
	public int getColumnCount() {
		return getColumnCount(this.sheetNum, this.rowNum);
	}

	public int getColumnCount(int rowNum) {
		return getColumnCount(this.sheetNum, rowNum);
	}

	public int getColumnCount(int sheetNum, int rowNum) {
		if (book != null) {
		    Sheet sheet = book.getSheetAt(sheetNum);
			Row row = sheet.getRow(rowNum);
			if(row==null){
				return 0;
			}
			int rowcount = -1;
			rowcount = row.getLastCellNum();
			if (ColumnCount > rowcount) {
				return ColumnCount;
			}
			return rowcount;
		} else {
			System.out.println("没有加载文件");
		}
		return 0;

	}

	// ---------------------------------------
	// ------------读取行内容------------------
	// ---------------------------------------
	public String[] getRowInfo(int rowNum) {
		return getRowInfo(this.sheetNum, rowNum);
	}

	public String[] getRowInfo(int sheetNum, int rowNum) {
		if (sheetNum < 0 || rowNum < 0) {
			return null;
		}

		sheet = book.getSheetAt(sheetNum);
		row = sheet.getRow(rowNum);
		int cell = row.getLastCellNum();
		String[] info = new String[cell];
		for (int i = 0; i < cell; i++) {
			info[i] = getCell(rowNum, i);
		}
		return info;
	}

	// ---------------------------------------
	// --------------读取列内容----------------
	// ---------------------------------------
	public String[] getColumn() {
		return getColumn(this.sheetNum, 0);
	}

	public String[] getColumn(int cellNum) {
		return getColumn(this.sheetNum, cellNum);
	}

	public String[] getColumn(int sheetNum, int cellNum) {
		if (sheetNum < 0 || cellNum < 0) {
			return null;
		}
		sheet = book.getSheetAt(sheetNum);
		int rowNum = sheet.getLastRowNum();
		String[] info = new String[rowNum];
		for (int i = 0; i < rowNum; i++) {
			info[i] = getCell(0, i, cellNum);
		}
		return info;
	}

	// ---------------------------------------
	// ------------读取单元格内容--------------
	// ---------------------------------------
	public String getCell(int cellNum) {
		return getCell(this.sheetNum, this.rowNum, cellNum);
	}

	public String getCell(int rowNum, int cellNum) {
		return getCell(this.sheetNum, rowNum, cellNum);
	}

	public String getCell(int sheetNum, int rowNum, int cellNum)  {
		if (sheetNum < 0 || rowNum < 0) {
			return null;
		}
		String cell = null;
		sheet = book.getSheetAt(sheetNum);
		row = sheet.getRow(rowNum);
	
		if (row.getCell(cellNum) != null) {
			switch (row.getCell(cellNum).getCellType()) {
			case Cell.CELL_TYPE_FORMULA:
				cell = "FORMULA";
				break;
			case Cell.CELL_TYPE_NUMERIC:
				cell = String.valueOf(row.getCell(cellNum).getNumericCellValue());
				break;
			case Cell.CELL_TYPE_STRING:
				cell = row.getCell(cellNum).getStringCellValue();
				break;
			case Cell.CELL_TYPE_BOOLEAN:
				cell = String.valueOf(row.getCell(cellNum).getBooleanCellValue());
				break;
			case Cell.CELL_TYPE_BLANK:
				cell = "";
				break;
			default:
				cell = "";
				break;
			}
		}
		return cell;
	}

	
	 
	
	// ---------------------------------------
	// ---------------------------------------
	/**
	 * @param args
	 * @throws ParseException
	 */
	public static void main(String[] args) throws ParseException {
		//StudentDao std = new StudentDao();
	//	List<Object> stuL = null;//std.findByProperty("school_id", "11010001");
		
			File f = new File("C:\\Users\\Administrator\\Desktop\\ysTest.xls");

			ExcelUtil excel = new ExcelUtil(f);
			excel.open();
			System.out.println(excel.getCell(1, 0));
			/*for (int i = 0; i < 5; i++) {
				
				for (int c = 0; c < 5; c++) {
					
					if (c == 0) {
						excel.writCell(0, i + 1, c, "grade_name");
					} else if (c == 1) {
						excel.writCell(0, i + 1, c, "class_name");
					} else if (c == 2) {
						excel.writCell(0, i + 1, c, "s_uuid");
					} else if (c == 3) {
						excel.writCell(0, i + 1, c, "student_name");
					} else if (c == 4) {
						excel.writCell(0, i + 1, c, "gender");
					}

				}*/
			//}
	//}
			
		//	excel.save();
		//}

		// 第一个sheet 第二个参数 行 第三个参数；列

		// excel.writCell(1, 0, "msg");
		// System.out.println(excel.getCell(0,0)+"获取内容");
		// excel.writCell(1, 1, "1");
		// String s = excel.getCell(0, 1);
		// System.out.println( excel.getColumnCount());//有多少列
		// System.out.println( excel.getRowCount(0));//有多少行
		// System.out.println(s);
		// excel.closeExcel();
	}


}
