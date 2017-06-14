/**
 * 
 * 项目名称:[WordsFetch]
 * 包:	 [com.boomzz.main]
 * 类名称: [WordUtil]
 * 类描述: [一句话描述该类的功能]
 * 创建人: [王新晨]
 * 创建时间:[2017年6月12日 下午5:07:05]
 * 修改人: [王新晨]
 * 修改时间:[2017年6月12日 下午5:07:05]
 * 修改备注:[说明本次修改内容]  
 * 版本:	 [v1.0]   
 * 
 */
package com.boomzz.main;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTShd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSpacing;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STShd;

/**
 * @author Duo Nuo
 *
 */
public class WordUtil {

	private static XWPFParagraph curParagraph=null;
	
	public WordUtil() {
	}
	
	public static void write(String filePath,List<Map<String, Object>> data) throws IOException{
		
		XWPFDocument doc = new XWPFDocument(new FileInputStream(filePath));
		for(Map<String, Object> map:data){
			//创建标题
			createNewTitle(doc,map.get("name").toString());
			//创建音标行
			createNewText(doc,map.get("phone").toString());
			//创建英义行
			if(map.get("enList")!=null){
				List<Map<String, Object>> enList = (List<Map<String, Object>>) map.get("enList");
				for(Map<String, Object> m:enList){
					createNewText(doc,m.get("en").toString());
					if(m.get("example")!=null){
						List<String> example =(List<String>) m.get("example");
						for(String str:example){
							//创建例句行
							createNewTextExample(doc,str);
						}
					}
				}
			}
		}
		//写入文件
		doc.write(new FileOutputStream(filePath));
		//关闭
		doc.close();
	}
	
	private static XWPFRun createNewXWPFRun(XWPFDocument doc){
		XWPFParagraph createParagraph = doc.createParagraph();
		CTSpacing spacing = createParagraph.getCTP().addNewPPr().addNewSpacing();
		spacing.setLine(new BigInteger("300"));
		curParagraph = createParagraph;
		XWPFRun createRun = createParagraph.createRun();
		return createRun;
	}
	private static void createNewTitle(XWPFDocument doc,String text){
		XWPFRun createRun = createNewXWPFRun(doc);
		createRun.setFontSize(10);
		CTShd shd = createRun.getCTR().getRPr().addNewShd();
		shd.setVal(STShd.Enum.forInt(100));
		shd.setColor("BFBFBF");
		createRun.setFontFamily("Times New Roman");
		createRun.setBold(true);
		createRun.setText(text);
		XWPFRun createRun2 = curParagraph.createRun();
		createRun2.setFontSize(10);
		createRun2.setBold(false);
		createRun2.setFontFamily("宋体");
		createRun2.setText("  □ □ □ □ □");
	}
	private static void createNewText(XWPFDocument doc,String text){
		XWPFRun createRun = createNewXWPFRun(doc);
		createRun.setFontSize(8);
		createRun.setText(text);
		createRun.setFontFamily("Times New Roman");
		createRun.setBold(false);
	}
	private static void createNewTextExample(XWPFDocument doc,String text){
		XWPFRun createRun = createNewXWPFRun(doc);
		createRun.setFontSize(8);
		createRun.setText("[例]");
		createRun.setFontFamily("宋体");
		createRun.setBold(true);
		XWPFRun createRun2 = curParagraph.createRun();
		createRun2.setFontSize(8);
		createRun2.setText(text);
		createRun2.setFontFamily("Times New Roman");
		createRun2.setBold(false);
	}
}
