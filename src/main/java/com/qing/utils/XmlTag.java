package com.qing.utils;

import static com.qing.utils.StringUtils.isNullOrEmpty;

/**
 * Created by zwq on 2015/04/15 11:28.<br/><br/>
 * 将字符拼接成xml格式文本
 */
public class XmlTag {
	/**
	 * demo:
		XmlTag root = new XmlTag(-1, "adv");
		root.addChildTag("date", "201504071414");
		for (int i = 1; i < 3; i++) {
			XmlTag pos = new XmlTag(root.level, "pos").addAttribute("id", "hp"+i);
			pos.addChildTag("url", "http://www.baidu.com/");
			pos.addChildTag("pic", "/storage/sdcard0/Test.txt");
			root.addChildTag(pos);
		}
		System.out.println(root);
	 */

	private String head = "<?xml version='1.0' encoding='utf-8' standalone='yes' ?>\n";
	private static String str1 = "<";
	private static String str2 = "</";
	private static String str3 = ">";
	
	private XmlTag instance = null;
	private boolean isRoot = false;
	private boolean isParent = false;
	public int level = 0;
	
	private StringBuffer sbuff = null;
	private String parentName = null;
	private String space = "\t";
	private String nextLine = "\n";
	private boolean tagValueToTrim;

	@SuppressWarnings("unused")
	private XmlTag(){}
	/**
	 * 此方法构造一个不带值的标签，用于包含子标签
	 * @param parentLevel 如果是(root)根标签 parentLevel = -1，否则使用 parentTag.level
	 * @param tagName
	 */
	public XmlTag(int parentLevel, String tagName){
		this(parentLevel,tagName, "", true);
	}
	
	/**
	 * @param tagName
	 * @param tagValue	true:1,false:0
	 */
	public XmlTag(String tagName, boolean tagValue){
		this(tagName, tagValue==true?"1":"0");
	}
	public XmlTag(String tagName, int tagValue){
		this(tagName, "" + tagValue);
	}
	public XmlTag(String tagName, String tagValue){
		this(0, tagName, tagValue, false);
	}
	
	private XmlTag(int parentLevel, String tagName, String tagValue, boolean isParent){
		if(instance==null){
			instance = this;
			sbuff = new StringBuffer("");
			if(sbuff==null){
			}
			if(parentLevel==-1){
				this.isRoot = true;
			}
			this.isParent = isParent;
			this.level = parentLevel+1;
		}
		string2Xml(tagName, tagValue, true);
	}
	
	/**
	 * @param attrName
	 * @param arrtValue	true:1,false:0
	 * @return 
	 */
	public XmlTag addAttribute(String attrName, boolean arrtValue){
		return addAttribute(attrName, arrtValue==true?"1":"0");
	}
	public XmlTag addAttribute(String attrName, int arrtValue){
		return addAttribute(attrName, ""+arrtValue);
	}
	public XmlTag addAttribute(String attrName, String arrtValue){
		if(parentName!=null){
			int insert = sbuff.indexOf(parentName) + parentName.length();
			if(isNullOrEmpty(attrName)){
				throw new RuntimeException("tagName is null or empyt");
			}
			if(isNullOrEmpty(arrtValue)){
				arrtValue = "";
			}
			//检查是否有非法字符
			attrName = toValid(attrName);
			arrtValue = toValid(arrtValue);
			String attr = " "+attrName+"=\""+arrtValue+"\"";
			sbuff.insert(insert, attr);
		}
		return instance;
	}

	public XmlTag addChildTag(XmlTag childTag){
		sbuff.append(childTag.toString());
		sbuff.append(nextLine);
		return instance;
	}
	/**
	 * @param tagName
	 * @param tagValue	true:1,false:0
	 * @return	XmlTag
	 */
	public XmlTag addChildTag(String tagName, boolean tagValue){
		return addChildTag(tagName, tagValue==true?"1":"0");
	}
	public XmlTag addChildTag(String tagName, int tagValue){
		return addChildTag(tagName, ""+tagValue);
	}
	public XmlTag addChildTag(String tagName, long tagValue){
		return addChildTag(tagName, ""+tagValue);
	}
	public XmlTag addChildTag(String tagName, float tagValue){
		return addChildTag(tagName, ""+tagValue);
	}
	public XmlTag addChildTag(String tagName, double tagValue){
		return addChildTag(tagName, ""+tagValue);
	}
	public XmlTag addChildTag(String tagName, String tagValue){
		return addChildTag(tagName, tagValue, true);
	}
	
	/**
	 * @param tagName
	 * @param tagValue
	 * @param check (true[默认]:检查是否有非法字符，false:不检查)Ignore the tagValue of the check
	 * @return
	 */
	public XmlTag addChildTag(String tagName, String tagValue, boolean check){
		string2Xml(tagName, tagValue, check);
		return instance;
	}
	
	private int k = 0;
	private void string2Xml(String tagName, String tagValue, boolean check){
		if(isNullOrEmpty(tagName)){
			throw new RuntimeException("tagName is null or empyt");
		}
		if(isNullOrEmpty(tagValue)){
			tagValue = "";
		}
		
		//检查是否有非法字符
		tagName = toValid(tagName);
		if(check){
			tagValue = toValid(tagValue);
		}
		
		if(isParent && parentName==null){
			parentName = tagName;
		}
		k = 0;
		k = tagName.equals(parentName) ? 0 : 1;//0:父标签,1:子标签
		if(sbuff==null){
			sbuff = new StringBuffer("");
		}
		for (int i = 0; i < level+k; i++) {
			sbuff.append(space);//子标签顶格空位
		}
		
		sbuff.append(doStartTag(tagName));
		
		if(tagName.equals(parentName)) {
			sbuff.append(nextLine);//父标签换行
		}
		sbuff.append(tagValue);
		
		if(!tagName.equals(parentName)){
			//标签结尾
			sbuff.append(doEndTag(tagName));
			if(isParent){//父标签标签才加换行
				sbuff.append(nextLine);
			}
		}
	}
	//校验非法字符
	private static String toValid(String text) {
		return text.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
	}
	//开始标签
	private static String doStartTag(String tagName){
		return str1+tagName+str3;
	}
	//结束标签
	private static String doEndTag(String tagName){
		return str2+tagName+str3;
	}

	@Override
	public String toString() {
		return (isRoot==true?head:"") + getContent();
	}
	/**
	 * 去空格和换行符
	 * @return
	 */
	public String toTrimString() {
		return trimAll(toString());
	}

	public String trimAll(String content){
		return content==null?null:content.replaceAll(nextLine, "").replaceAll(space, "").trim();
	}

	/**
	 * 去掉便签内的所有空格，必须在添加标签内容之前调用，否则无效
	 * @param trim
	 */
	public void setTagValueToTrim(boolean trim){
		tagValueToTrim = trim;
	}

	private boolean hasAdd = false;
	private String getContent(){
		if(isParent && !hasAdd){//子标签顶格空位
			for (int i = 0; i < level; i++) {
				sbuff.append(space);
			}
			sbuff.append(doEndTag(parentName));
			if (tagValueToTrim && level > 0){
				StringBuffer temp = new StringBuffer();
				for (int i = 0; i < level; i++) {
					temp.append(space);
				}
				temp.append(trimAll(sbuff.toString()));
				sbuff = temp;
			}
			hasAdd = true;
		}
		return sbuff.toString();
	}

	public static String toXmlTag(String tagName, boolean tagValue){
		return toXmlTag(tagName, tagValue==true?"1":"0");
	}
	public static String toXmlTag(String tagName, int tagValue){
		return toXmlTag(tagName, ""+tagValue);
	}
	public static String toXmlTag(String tagName, long tagValue){
		return toXmlTag(tagName, ""+tagValue);
	}
	public static String toXmlTag(String tagName, float tagValue){
		return toXmlTag(tagName, ""+tagValue);
	}
	public static String toXmlTag(String tagName, double tagValue){
		return toXmlTag(tagName, ""+tagValue);
	}
	public static String toXmlTag(String tagName, String tagValue){
		if(isNullOrEmpty(tagName)){
			throw new RuntimeException("tagName is null or empyt");
		}
		if(isNullOrEmpty(tagValue)){
			tagValue = "";
		}
		//检查是否有非法字符
		tagName = toValid(tagName);
		tagValue = toValid(tagValue);
		
		return doStartTag(tagName)+tagValue+doEndTag(tagName);
	}
}
