package com.saclass.main;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
 * @author wxc
 *
 */
public class ExcelUtil {

	public static Map<String, Object> read(File file){
		jxl.Workbook readwb = null;   
		Map<String, Object> data=new HashMap<>();
		List<String> words=new ArrayList<>();
        try {
        	InputStream instream = new FileInputStream(file);   
			readwb = Workbook.getWorkbook(instream);
	        Sheet readsheet = readwb.getSheet(0);   
	        int rsColumns = readsheet.getColumns();   
	        int rsRows = readsheet.getRows();
	        int i = 0;
	        for (i = 0; i < rsRows; i++){
	        	Cell cell = readsheet.getCell(0, i);
	        	words.add(cell.getContents());
            }
	        data.put("size", i);
	        data.put("words",words);
	        instream.close();
	        readwb.close();
	        
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	public static void written(List<Map<String, String>> mapData){
		try {
			// 1、创建工作簿(WritableWorkbook)对象，打开excel文件，若文件不存在，则创建文件
			String names=new Date().getTime()+"";
			WritableWorkbook writeBook = Workbook.createWorkbook(new File(names+".xls"));
			// 2、新建工作表(sheet)对象，并声明其属于第几页  
	        WritableSheet firstSheet = writeBook.createSheet("第一个工作簿", 1);// 第一个参数为工作簿的名称，第二个参数为页数  
	        // 3、创建单元格(Label)对象，  
	        Label label1 = new Label(0, 0, "名称");// 第一个参数指定单元格的列数、第二个参数指定单元格的行数，第三个指定写的字符串内容  
	        Label label2 = new Label(1, 0, "音标");// 第一个参数指定单元格的列数、第二个参数指定单元格的行数，第三个指定写的字符串内容  
	        Label label3 = new Label(2, 0, "中文释义");// 第一个参数指定单元格的列数、第二个参数指定单元格的行数，第三个指定写的字符串内容  
	        Label label4 = new Label(3, 0, "英文释义");// 第一个参数指定单元格的列数、第二个参数指定单元格的行数，第三个指定写的字符串内容  
	        Label label5 = new Label(4, 0, "例句英文");// 第一个参数指定单元格的列数、第二个参数指定单元格的行数，第三个指定写的字符串内容  
	        Label label6 = new Label(5, 0, "例句中文");// 第一个参数指定单元格的列数、第二个参数指定单元格的行数，第三个指定写的字符串内容  
	        firstSheet.addCell(label1);
	        firstSheet.addCell(label2);
	        firstSheet.addCell(label3);
	        firstSheet.addCell(label4);
	        firstSheet.addCell(label5);
	        firstSheet.addCell(label6);
	        for(int i=0;i<mapData.size();i++){
	        	Label name = new Label(0, i+1, mapData.get(i).get("name"));// 第一个参数指定单元格的列数、第二个参数指定单元格的行数，第三个指定写的字符串内容  
		        Label phonic = new Label(1, i+1, mapData.get(i).get("phone"));// 第一个参数指定单元格的列数、第二个参数指定单元格的行数，第三个指定写的字符串内容  
		        Label tras = new Label(2, i+1, mapData.get(i).get("tras"));// 第一个参数指定单元格的列数、第二个参数指定单元格的行数，第三个指定写的字符串内容  
		        firstSheet.addCell(name);
		        firstSheet.addCell(phonic);
		        firstSheet.addCell(tras);
		        if(mapData.get(i).get("para")!=null){
		        	Label para = new Label(3, i+1,mapData.get(i).get("para"));// 第一个参数指定单元格的列数、第二个参数指定单元格的行数，第三个指定写的字符串内容  
		        	firstSheet.addCell(para);
		        	if(mapData.get(i).get("sentence_en")!=null){
		        		Label sentence_en = new Label(4, i+1, mapData.get(i).get("sentence_en"));// 第一个参数指定单元格的列数、第二个参数指定单元格的行数，第三个指定写的字符串内容  
		        		Label sentence_zh = new Label(5, i+1, mapData.get(i).get("sentence_zh"));// 第一个参数指定单元格的列数、第二个参数指定单元格的行数，第三个指定写的字符串内容  
		        		firstSheet.addCell(sentence_en);
		        		firstSheet.addCell(sentence_zh);
		        	}
		        }
	        }
	        // 4、打开流，开始写文件  
	        writeBook.write();
	        // 5、关闭流
	        writeBook.close();  
		} catch (Exception e) {
			e.printStackTrace();
		}  
		
	}
	
}
