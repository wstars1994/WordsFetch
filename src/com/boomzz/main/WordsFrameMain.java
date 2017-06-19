package com.boomzz.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
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
import javax.swing.filechooser.FileFilter;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author wxc
 *
 */
public class WordsFrameMain extends JFrame{
	
	private String[] btnText={"导入词汇","存为word","存为excel"};
	private String wordPath=null;
	private JButton importBtn,startBtn,importWordBtn;
	private JPanel mainPanel;
	private static JLabel wordsNumLabel;
	private JLabel fetchNumLabel;
	private Map<String, Object> wordsMap=null;
	List<Map<String, String>> mapData=new ArrayList<>();
	List<Map<String, Object>> wordMap=new ArrayList<>();
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
		importWordBtn=new JButton(btnText[1]);
		startBtn=new JButton(btnText[2]);
		//未加载则不显示开始按钮
		startBtn.setEnabled(false);
		importWordBtn.setEnabled(false);
		mainPanel.add(importBtn);
		mainPanel.add(importWordBtn);
		mainPanel.add(startBtn);
		mainPanel.add(wordsNumLabel);
		mainPanel.add(fetchNumLabel);
		this.addBtnListener(importBtn);
		this.addBtnListener(importWordBtn);
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
					wordPath = importWord();
					new Thread(new Runnable() {
						@Override
						public void run() {
							startFetchForWord();
						}
					}).start();
				}
				if(e.getActionCommand().equals(btnText[2])){
					new Thread(new Runnable() {
						@Override
						public void run() {
							startFetch();
						}
					}).start();
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
        			importWordBtn.setEnabled(true);
        			JOptionPane.showMessageDialog(this.getContentPane(), "加载成功", "系统信息", JOptionPane.WARNING_MESSAGE);
        		}else{
        			startBtn.setEnabled(false);
        			importWordBtn.setEnabled(false);
        			wordsNumLabel.setVisible(false);
        			JOptionPane.showMessageDialog(this.getContentPane(), "加载失败,请检查excel格式是否符合要求", "系统信息", JOptionPane.WARNING_MESSAGE);
        		}
        	}  
        }
		return file;
	}
	private String importWord(){
		JFileChooser jfc=new JFileChooser();  
		jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES );  
		jfc.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				String s = f.getName().toLowerCase();
				if(s.endsWith(".docx")){
					return true;
				}
				return false;
			}
			@Override
			public String getDescription() {
				return null;
			}
		});
		jfc.showDialog(new JLabel(), "选择word(docx)文档");
		File file=jfc.getSelectedFile();
		if(file!=null){
			if(file.isDirectory()){
				JOptionPane.showMessageDialog(this.getContentPane(), "请选择一个docx文件", "系统信息", JOptionPane.WARNING_MESSAGE);
			}else if(file.isFile()){  
				return file.getPath();
			}  
		}
		return null;
	}

	private void startFetch(){
		startBtn.setEnabled(false);
		List<String> words=(List<String>) wordsMap.get("words");
		int sum=1;
		for(String str:words){
			wordsNumLabel.setText("抓取第"+sum+"个/共"+wordsMap.get("size")+"个");
			sum++;
			Map<String, String> map=new HashMap<>();
			try{
				String url="http://dict.youdao.com/jsonapi?q="+str;
				map.put("name", str);
				String string = HttpClientUtil.get(url= url.replaceAll(" ", "%20"));
				JSONObject object=JSONObject.fromObject(string);
				JSONObject simple=(JSONObject) object.get("simple");
				JSONObject ec=(JSONObject) object.get("ec");
				String tras="";
				if(ec!=null){
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
				}
				map.put("tras",tras);
				if(simple!=null){
					String usphone=getPhonic(simple);
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
							JSONObject exam_sents=(JSONObject)tran_entry_object.get("exam_sents");
							map.put("para", "1."+HtmlUtil.delHTMLTag(tran));
							if(exam_sents!=null){
								JSONArray sent=(JSONArray) exam_sents.get("sent");
								for(int i=0;i<sent.size();i++){
									map=new HashMap<>();
									map.put("name", str);
									map.put("tras",tras);
									map.put("phone",usphone);
									map.put("para", "1."+HtmlUtil.delHTMLTag(tran));
									JSONObject en=(JSONObject)sent.get(i);
									String enString=en.get("eng_sent").toString();
									String zhString=en.get("chn_sent").toString();
									map.put("sentence_en", HtmlUtil.delHTMLTag(enString));
									map.put("sentence_zh", HtmlUtil.delHTMLTag(zhString));
									mapData.add(map);
								}
								continue;
							}
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}finally {
				mapData.add(map);
			}
		}
		ExcelUtil.written(mapData);
		JOptionPane.showMessageDialog(this.getContentPane(), "抓取完成", "系统信息", JOptionPane.WARNING_MESSAGE);
	}
	private void startFetchForWord(){
		startBtn.setEnabled(false);
		List<String> words=(List<String>) wordsMap.get("words");
		int sum=1;
		System.out.println(wordPath);
		if(wordPath==null){
			JOptionPane.showMessageDialog(this.getContentPane(), "请选择一个输出文件(docx)", "系统信息", JOptionPane.WARNING_MESSAGE);
			return;
		}
		for(String str:words){
			wordsNumLabel.setText("抓取第"+sum+"个/共"+wordsMap.get("size")+"个");
			Map<String, Object> map=new HashMap<>();
			try{
				String url="http://dict.youdao.com/jsonapi?q="+str;
				map.put("name", str);
				
				String string = HttpClientUtil.get(url= url.replaceAll(" ", "%20"));
				JSONObject object=JSONObject.fromObject(string);
				JSONObject simple=(JSONObject) object.get("simple");
				JSONObject ec=(JSONObject) object.get("ec");
				if(simple!=null){
					String usphone=getPhonic(simple);
					map.put("phone","美"+usphone);
					
					JSONObject collins=(JSONObject) object.get("collins");
					if(collins!=null){
						JSONArray collins_entries=(JSONArray) collins.get("collins_entries");
						JSONObject entriess=(JSONObject) collins_entries.get(0);
						JSONObject entries=(JSONObject)entriess.get("entries");
						if(entries!=null){
							JSONArray entry=(JSONArray) entries.get("entry");
							List<Map<String, Object>> enList = new ArrayList<>();
							for(int i=0;i<entry.size();i++){
								Map<String, Object> enMap=new HashMap<>();
								JSONObject tran_entry=(JSONObject)entry.get(i);
								JSONArray tran_entry2=(JSONArray)tran_entry.get("tran_entry");
								JSONObject tran_entry_object=(JSONObject)tran_entry2.get(0);
								if(tran_entry_object.get("tran")!=null){
									String tran=tran_entry_object.get("tran").toString();
									String pos="";
									if(tran_entry_object.get("pos_entry")!=null){
										if(((JSONObject)tran_entry_object.get("pos_entry")).get("pos")!=null){
											pos += ((JSONObject)tran_entry_object.get("pos_entry")).get("pos").toString()+"  ";
										}
									}
									if(!HtmlUtil.delHTMLTag(tran).equals(""))
										enMap.put("en",(i+1)+"."+pos+HtmlUtil.delHTMLTag(tran));
									List<String> example = new ArrayList<>();
									JSONObject exam_sents=(JSONObject)tran_entry_object.get("exam_sents");
									if(exam_sents!=null){
										JSONArray sent=(JSONArray) exam_sents.get("sent");
										for(int j=0;j<sent.size();j++){
											JSONObject en=(JSONObject)sent.get(j);
											String enString=en.get("eng_sent").toString();
											String zhString=en.get("chn_sent").toString();
											example.add(HtmlUtil.delHTMLTag(enString)+HtmlUtil.delHTMLTag(zhString));
										}
									}
									enMap.put("example", example);
									enList.add(enMap);
								}
							}
							map.put("enList", enList);
						}
					}
				}
				wordMap.add(map);
			}catch(Exception e){
				e.printStackTrace();
				wordMap.add(map);
			}
			sum++;
		}
		try {
			WordUtil.write(wordPath, wordMap);
			JOptionPane.showMessageDialog(this.getContentPane(), "抓取完成", "系统信息", JOptionPane.WARNING_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this.getContentPane(), "文档不存在或已打开,请关闭文档", "系统信息", JOptionPane.WARNING_MESSAGE);
		}
	}
	public static void updateUI(int num,int count){
		wordsNumLabel.setText("正在保存到word:"+num+"/"+count);
	}
	private String getPhonic(JSONObject simple){
		JSONArray word=(JSONArray) simple.get("word");
		String usphone="";
		if(((JSONObject)word.get(0)).get("usphone")!=null&&!"".equals(((JSONObject)word.get(0)).get("usphone").toString()))
			usphone="["+((JSONObject)word.get(0)).get("usphone").toString()+"]";
		else if(((JSONObject)word.get(0)).get("phone")!=null&&!"".equals(((JSONObject)word.get(0)).get("phone").toString()))
			usphone="["+((JSONObject)word.get(0)).get("phone").toString()+"]";
		else if(((JSONObject)word.get(0)).get("ukphone")!=null&&!"".equals(((JSONObject)word.get(0)).get("ukphone").toString()))
			usphone="["+((JSONObject)word.get(0)).get("ukphone").toString()+"]";
		return usphone;
	}
	public static void main(String[] args) {
		WordsFrameMain frameMain=new WordsFrameMain();
		frameMain.setVisible(true);
	}
}
