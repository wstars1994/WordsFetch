package com.saclass.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author wxc
 *
 */
public class WordsFrameMain extends JFrame implements Runnable{
	
	private String[] btnText={"导入词汇","开始抓取"};
	private JButton importBtn,startBtn;
	private JPanel mainPanel;
	private JTable wordsTable;
	private JLabel wordsNumLabel,fetchNumLabel;
	private Map<String, Object> wordsMap=null;
	List<Map<String, String>> mapData=new ArrayList<>();
	public  WordsFrameMain() {
		this.init();
	}
	private void init(){
		this.setTitle("单词抓取工具");
		this.setSize(400, 100);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		wordsNumLabel=new JLabel();
		fetchNumLabel=new JLabel();
		wordsNumLabel.setVisible(false);
		fetchNumLabel.setVisible(false);
		mainPanel=new JPanel();
		importBtn=new JButton(btnText[0]);
		startBtn=new JButton(btnText[1]);
//		wordsTable=new JTable(rowData,columnNames);
//		JScrollPane scrollPane = new JScrollPane(wordsTable);
		//未加载则不显示开始按钮
		startBtn.setEnabled(false);
		mainPanel.add(importBtn);
		mainPanel.add(startBtn);
//		mainPanel.add(wordsTable);
//		mainPanel.add(scrollPane);
		mainPanel.add(wordsNumLabel);
		mainPanel.add(fetchNumLabel);
		this.addBtnListener(importBtn);
		this.addBtnListener(startBtn);
		
		this.add(mainPanel);
	}
	private void addBtnListener(JButton btn){
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
				if(e.getActionCommand().equals(btnText[0])){
					importExcel();
				}
				if(e.getActionCommand().equals(btnText[1])){
					startFetch();
				}
				
			}
		});
	}
	
	private File importExcel(){
		JFileChooser jfc=new JFileChooser();  
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES );  
        jfc.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				String s = f.getName().toLowerCase();
                if(s.endsWith(".xls")){
                    return true;
                }
				return false;
			}
			@Override
			public String getDescription() {
				return null;
			}
		});
        jfc.showDialog(new JLabel(), "选择");
        File file=jfc.getSelectedFile();
        if(file!=null){
        	if(file.isDirectory()){
        		JOptionPane.showMessageDialog(this.getContentPane(), "请选择一个xls文件", "系统信息", JOptionPane.WARNING_MESSAGE);
        	}else if(file.isFile()){  
        		wordsMap = ExcelUtil.read(file);
        		if(!wordsMap.get("size").equals("0")){
        			wordsNumLabel.setText("共加载了"+wordsMap.get("size")+"个单词");
        			wordsNumLabel.setVisible(true);
        			startBtn.setEnabled(true);
        			JOptionPane.showMessageDialog(this.getContentPane(), "加载成功", "系统信息", JOptionPane.WARNING_MESSAGE);
        		}else{
        			startBtn.setEnabled(false);
        			wordsNumLabel.setVisible(false);
        			JOptionPane.showMessageDialog(this.getContentPane(), "加载失败,请检查excel格式是否符合要求", "系统信息", JOptionPane.WARNING_MESSAGE);
        		}
        	}  
        }
		return file;
	}

	private void startFetch(){
		startBtn.setEnabled(false);
		List<String> words=(List<String>) wordsMap.get("words");
		for(String str:words){
			Map<String, String> map=new HashMap<>();
			try{
				String url="http://dict.youdao.com/jsonapi?q="+str;
				map.put("name", str);
				String string = HttpClientUtil.get(url= url.replaceAll(" ", "%20"));
				JSONObject object=JSONObject.fromObject(string);
				JSONObject simple=(JSONObject) object.get("simple");
				System.out.println(str);
				JSONObject ec=(JSONObject) object.get("ec");
				if(ec!=null){
					String tras="";
					JSONArray ecWords=(JSONArray) ec.get("word");
					JSONArray trs=(JSONArray) ((JSONObject)ecWords.get(0)).get("trs");
					if(trs!=null){
						for(int t=0;t<trs.size();t++){
							JSONArray tr=(JSONArray)((JSONObject)trs.get(t)).get("tr");
							JSONArray l=(JSONArray)((JSONObject)((JSONObject)tr.get(0)).get("l")).get("i");
							tras+=l.get(0)+"#";
						}
					}
					if(tras.length()>0){
						tras=tras.substring(0, tras.length()-1);
					}
					map.put("tras",tras);
				}
				if(simple!=null){
					JSONArray word=(JSONArray) simple.get("word");
					String usphone="";
					if(((JSONObject)word.get(0)).get("usphone")!=null)
						usphone="["+((JSONObject)word.get(0)).get("usphone").toString()+"]";
					else if(((JSONObject)word.get(0)).get("phone")!=null)
						usphone="["+((JSONObject)word.get(0)).get("phone").toString()+"]";
					else if(((JSONObject)word.get(0)).get("ukphone")!=null)
						usphone="["+((JSONObject)word.get(0)).get("ukphone").toString()+"]";
					map.put("phone",usphone);
					JSONObject collins=(JSONObject) object.get("collins");
					if(collins!=null){
						JSONArray collins_entries=(JSONArray) collins.get("collins_entries");
						JSONObject entriess=(JSONObject) collins_entries.get(0);
						JSONObject entries=(JSONObject)entriess.get("entries");
						if(entries!=null){
							JSONArray entry=(JSONArray) entries.get("entry");
							JSONObject tran_entry=(JSONObject)entry.get(0);
							JSONArray tran_entry2=(JSONArray)tran_entry.get("tran_entry");

							JSONObject tran_entry_object=(JSONObject)tran_entry2.get(0);
							String tran=tran_entry_object.get("tran").toString();
//							System.out.println("1."+HtmlUtil.delHTMLTag(tran));
							
							JSONObject exam_sents=(JSONObject)tran_entry_object.get("exam_sents");
							map.put("para", "1."+HtmlUtil.delHTMLTag(tran));
							if(exam_sents!=null){
								JSONArray sent=(JSONArray) exam_sents.get("sent");
								for(int i=0;i<sent.size();i++){
									map=new HashMap<>();
									map.put("name", str);
									map.put("phone",usphone);
									map.put("para", "1."+HtmlUtil.delHTMLTag(tran));
									JSONObject en=(JSONObject)sent.get(i);
									String enString=en.get("eng_sent").toString();
									String zhString=en.get("chn_sent").toString();
									map.put("sentence_en", HtmlUtil.delHTMLTag(enString));
									map.put("sentence_zh", HtmlUtil.delHTMLTag(zhString));
//									System.out.println(HtmlUtil.delHTMLTag(enString));
//									System.out.println(HtmlUtil.delHTMLTag(zhString));
									mapData.add(map);
								}
								continue;
							}
						}
					
					}
				}
				mapData.add(map);
			}catch(Exception e){
				mapData.add(map);
			}
		}
		ExcelUtil.written(mapData);
		JOptionPane.showMessageDialog(this.getContentPane(), "抓取完成", "系统信息", JOptionPane.WARNING_MESSAGE);
	}
	
	public static void main(String[] args) {
		WordsFrameMain frameMain=new WordsFrameMain();
		frameMain.setVisible(true);
	}
	@Override
	public void run() {
		startFetch();
	}
}
