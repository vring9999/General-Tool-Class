package com.hrcx.common.util;

import java.beans.IntrospectionException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;

import com.hrcx.common.util.gsonadapter.JsonDateValueProcessor;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * @ClassName: StringUtil
 * @Description: 字符串处理类
 * @author Administrator
 * @date 2015-12-24 下午2:15:03
 */
public class StringUtil {
	private static String[] chars = new String[] { "a", "b", "c", "d", "e", "f",  
        "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",  
        "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",  
        "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",  
        "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",  
        "W", "X", "Y", "Z" };
	
	
	/**
	 * @Title: isEmpty
	 * @Description: 判断字符串是否为空
	 * @param @param string
	 * @return boolean
	 * @throws
	 */
	public static boolean isEmpty(String string) {
		boolean result = false;
		if (string == null || "".equals(string.trim())) {
			result = true;
		}
		return result;
	}
	/**
	 * 验证Object是否为空,object instanceof String
	 * @param object
	 * @return
	 */
	public static boolean isEmpty(Object object) {
		boolean result = false;
		if (object == null || "".equals(object.toString().trim())) {
			result = true;
		}
		return result;
	}
	/**
	 * @Title: getUuid
	 * @Description: 获取UUID 带-标识
	 * @return String 返回类型
	 * @throws
	 */
	public static String getUuid() {
		UUID uuid = UUID.randomUUID();
		String str = uuid.toString();
		return str;
	}
	/**
	 * @Title: getUUID
	 * @Description: 获取UUID 去掉-标识
	 * @return
	 */
	public static String getUUID() {
		String str = getUuid();
		str = str.replace("-", "");
		return str;
	}
	
	/**
	* @Title: getShortUuid
	* @Description: 获取短UUID
	* @return String 短UUID
	*/ 
	public static String getShortUuid() {
		StringBuffer shortBuffer = new StringBuffer();
		String uuid = getUUID();
		for (int i = 0; i < 8; i++) {
			String str = uuid.substring(i * 4, i * 4 + 4);
			int x = Integer.parseInt(str, 16);
			shortBuffer.append(chars[x % 0x3E]);
		}
		return shortBuffer.toString();
	}
	
	/**
	 * 获取随机数
	 * @param len 随机数长度
	 * @return 返回len长度的随机数
	 */
	public static StringBuffer getRandomCode(int len){
		StringBuffer buffer = new StringBuffer();
		Random random = new Random();
		for(int i = 0;i<len;i++){
			buffer.append(random.nextInt(10));
		}
		return buffer;
	}
	
	
	/**
	 * @Title: isCheckFiledLen
	 * @Description: 校验字段长度
	 * @param @param val
	 * @param @param length
	 * @param @return 设定文件
	 * @return boolean 返回类型
	 * @throws
	 */
	public static boolean isCheckFiledLen(String val, int length) {
		boolean result = false;
		int valLen = val.length();
		if (valLen > length) {
			result = true;
		}
		return result;
	}
	/**
	 * 将字符串为"null"或空对象转化为字符串""
	 * @param obj
	 */
	public static String doNullStr(Object obj) {
		String str = "";
		if (obj != null) {
			str = String.valueOf(obj);
			if (str.equals("null")) {
				str = "";
			}
		}
		return str.trim();
	}
	/**
	 * 将字符串中的中文转化为拼音,其他字符不变
	 * @param inputStr
	 * @return
	 * @throws BadHanyuPinyinOutputFormatCombination 
	 */
	public static String getPingYin(String inputStr) throws BadHanyuPinyinOutputFormatCombination {
		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		format.setVCharType(HanyuPinyinVCharType.WITH_V);
		char[] input = inputStr.trim().toCharArray();
		String output = "";
		for (int i = 0; i < input.length; i++) {
			if (Character.toString(input[i]).matches("[\\u4E00-\\u9FA5]+")) {
				String[] temp = PinyinHelper.toHanyuPinyinStringArray(input[i], format);
				output += temp[0];
			} else
				output += Character.toString(input[i]);
		}
		return output;
	}
	/**
	 * 格式化查询参数
	 * @param filter
	 * @return
	 */
	public static Map<String, Object> formatParam(String filter) {
		filter = InputInjectFilter.decodeInputString(filter);// HTML 反转义
		Map<String, Object> map = new HashMap<String, Object>();
		if (filter != null) {
			Pattern p = Pattern.compile("(\\w*)=([^&]*)");
			Matcher m = p.matcher(filter);
			while (m.find()) {
				if (!isEmpty(m.group(1)) && !isEmpty(m.group(2))) {
					map.put(m.group(1), m.group(2));
				}
			}
		}
		return map;
	}
	public static Map<String, String> formatParamString(String filter) {
		filter = InputInjectFilter.decodeInputString(filter);// HTML 反转义
		Map<String, String> map = new HashMap<String, String>();
		if (filter != null) {
			Pattern p = Pattern.compile("(\\w*)=([^&]*)");
			Matcher m = p.matcher(filter);
			while (m.find()) {
				if (!isEmpty(m.group(1)) && !isEmpty(m.group(2))) {
					map.put(m.group(1), m.group(2));
				}
			}
		}
		return map;
	}
	/**
	 * 2016-11-30
	 * @Title: isNumeric
	 * @Description: 判断字符串是否是数字组成
	 * @param @param str
	 * @param @return 参数说明
	 * @return boolean 返回类型
	 * @throws
	 */
	public static boolean isNumeric(String str) {
		for (int i = str.length(); --i >= 0;) {
			int chr = str.charAt(i);
			if (chr < 48 || chr > 57)
				return false;
		}
		return true;
	}
	/**
	 * @Title: underlineToCamel
	 * @Description: 下划线格式字符转为驼峰式字符规则
	 * @param str
	 * @return
	 */
	public static String underlineToCamel(String str) {
		if (isEmpty(str)) {
			return "";
		}
		int len = str.length();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			char c = str.charAt(i);
			if ('_' == c) {
				if (++i < len) {
					sb.append(Character.toUpperCase(str.charAt(i)));
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	/**
	 * @Title: camelToUnderline
	 * @Description: 驼峰式字符转为下划线格式字符规则
	 * @param str
	 * @return
	 */
	public static String camelToUnderline(String str) {
		if (isEmpty(str)) {
			return "";
		}
		int len = str.length();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			char c = str.charAt(i);
			if (Character.isUpperCase(c)) {
				sb.append("_");
				sb.append(Character.toLowerCase(c));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
	/**
	 * @Title: encode
	 * @Description: 根据指定编码对字符串进行转码
	 * @param @param str
	 * @param @param code
	 * @param @return
	 * @param @throws UnsupportedEncodingException 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	public static String encode(String str, String code) throws UnsupportedEncodingException {
		if (isEmpty(str)) {
			return "";
		}
		return java.net.URLEncoder.encode(str, code);
	}
	/**
	 * @Title: encode
	 * @Description: 对字符转进行UTF-8转码
	 * @param @param str
	 * @param @return
	 * @param @throws UnsupportedEncodingException 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	public static String encode(String str) throws UnsupportedEncodingException {
		if (isEmpty(str)) {
			return "";
		}
		return encode(str, "UTF-8");
	}
	/**
	 * @Title: decode
	 * @Description: 根据指定编码对字符串进行解码
	 * @param @param str
	 * @param @param code
	 * @param @return
	 * @param @throws UnsupportedEncodingException 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	public static String decode(String str, String code) throws UnsupportedEncodingException {
		if (isEmpty(str)) {
			return "";
		}
//		if (str.contains("+"))
//			str = str.replace("+", "%2B");
		return java.net.URLDecoder.decode(str, code);
	}
	/**
	 * @Title: decode
	 * @Description: 对字符转进行UTF-8解码
	 * @param @param str
	 * @param @return
	 * @param @throws UnsupportedEncodingException 参数说明
	 * @return String 返回类型
	 * @throws
	 */
	public static String decode(String str) throws UnsupportedEncodingException {
		if (isEmpty(str)) {
			return "";
		}
		return decode(str, "UTF-8");
	}
	
	/**
	 * 将磁盘的单位为byte转为便于阅读的单位
	 * 1kb = 1024(b)
	 * 1M = 1,048,576(b)
	 * 1G = 1,073,741,824(b)
	 * 1Tb = 1,099,511,627,776(b)
	 * 1Pb = 1125899906842624(b)
	 * @param size
	 * @return
	 */
	public static String changeFileSizeToRead(BigDecimal size){
		String readSize = "";
		if(size.longValue() < 1024){
			readSize = size+" b";
		}else if(size.longValue()>=1024 && size.longValue()< 1048576){
			readSize = size.divide(new BigDecimal(1024)).setScale(1, RoundingMode.HALF_UP)+" Kb";
		}else if(size.longValue() >= 1048576 && size.longValue() < 1073741824){
			readSize = size.divide(new BigDecimal(1024*1024)).setScale(1, RoundingMode.HALF_UP)+" Mb";
		}else if(size.longValue() >= 1073741824 && size.longValue() < 1099511627776l){
			readSize = size.divide(new BigDecimal(1024*1024*1024)).setScale(1, RoundingMode.HALF_UP)+" Gb";
		}else if(size.longValue() >=  1099511627776l && size.longValue() < 1125899906842624l){
			readSize = size.divide(new BigDecimal(1024*1024*1024*1024l)).setScale(1, RoundingMode.HALF_UP)+" Tb";
		}
		return readSize;
	}
	/**
	 * 转义"_"
	 * @param params
	 * @param filterKeys
	 */
	public static void filterFormater(Map<String, Object> params, String[] filterKeys) {
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			if (ArrayUtils.contains(filterKeys, entry.getKey())) {
				String value = (String) entry.getValue();
				if(!StringUtil.isEmpty(value)){
					value = value.replaceAll("_", "\\\\_");
				}
				params.put(entry.getKey(), value);
			}
		}
	}
	
	/**
	 * 转义"_"
	 * @param object
	 * @param filterKeys
	 */
	public static Object filterFormater(Object obj, String[] filterKeys) throws Exception {
		Map<String, Object> params = objectToMap(obj);
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			if (ArrayUtils.contains(filterKeys, entry.getKey())) {
				String value = (String) entry.getValue();
				if(!StringUtil.isEmpty(value)){
					value = value.replaceAll("_", "\\\\_");
				}
				params.put(entry.getKey(), value);
			}
		}
		obj = mapToObject(params, obj.getClass());
		return obj;
	}
	
	/**
	 * Object转Map
	 * @param obj
	 * @return
	 * @throws IntrospectionException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 */
	public static Map<String, Object> objectToMap(Object obj) throws IntrospectionException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		//法一：使用reflect进行转换 
	    if(obj == null){    
            return null;    
        }   
        Map<String, Object> map = new HashMap<String, Object>();    
        Field[] declaredFields = obj.getClass().getDeclaredFields();    
        for (Field field : declaredFields) {    
            field.setAccessible(true);  
            map.put(field.getName(), field.get(obj));  
        }    
        return map;  
        
		//法二：使用Introspector进行转换 
		/*
		if(obj == null) {
			return null; 
		}
        Map<String, Object> map = new HashMap<String, Object>();   
        BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());    
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();    
        for (PropertyDescriptor property : propertyDescriptors) {    
            String key = property.getName();    
            if (key.compareToIgnoreCase("class") == 0) {   
                continue;  
            }  
            Method getter = property.getReadMethod();  
            Object value = getter!=null ? getter.invoke(obj) : null;  
            map.put(key, value);  
        }    
        return map;  
        */
	}    
	
	/**
	 * Map转Object
	 * @param map
	 * @param beanClass
	 * @return
	 * @throws Exception
	 */
	public static Object mapToObject(Map<String, Object> map, Class<?> beanClass) throws Exception {
		//法一：使用reflect进行转换 
		if (map == null) {
			return null;    
		}
        Object obj = beanClass.newInstance();  
        Field[] fields = obj.getClass().getDeclaredFields();   
        for (Field field : fields) {    
            int mod = field.getModifiers();    
            if(Modifier.isStatic(mod) || Modifier.isFinal(mod)){    
                continue;    
            }    
            field.setAccessible(true);    
            field.set(obj, map.get(field.getName()));   
        }   
        return obj;    
        
        //法二：使用Introspector进行转换 
		/*
		if(map == null) {
			return null;    
		}
        Object obj = beanClass.newInstance();  
        BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());    
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();    
        for (PropertyDescriptor property : propertyDescriptors) {  
            Method setter = property.getWriteMethod();    
            if (setter != null) {  
                setter.invoke(obj, map.get(property.getName()));   
            }  
        }  
        return obj;
        */
    }    
	      
	public static String objectToString(Object object) {
		String str = "";
		try {
			if (object != null) {
				if (object instanceof String) {
					str = object.toString();
				} else {
					JsonConfig jsonConfig = new JsonConfig();
					jsonConfig
							.setCycleDetectionStrategy(CycleDetectionStrategy.LENIENT);
					jsonConfig.registerJsonValueProcessor(Date.class,
							new JsonDateValueProcessor());
					if (object.getClass().isArray()) {
						str = JSONArray.fromObject(object, jsonConfig).toString();
					} else if (object instanceof List) {
						str = JSONArray.fromObject(object).toString();
					} else {
						str = JSONObject.fromObject(object, jsonConfig).toString();
					}
				}
			}
		} catch (Exception e) {
			return str;
		}
		return str;
	}
	
	/**
	 * 获取盐值
	 * @param length
	 * @return
	 */
	public static String getSalt(int length){
		String randomStr = "zxcvbnmlkjhgfdsaqwertyuiopQWERTYUIOPASDFGHJKLZXCVBNM1234567890";
		StringBuffer strBuf = new StringBuffer();
		for(int i=0 ; i<length ; i++){
			int random = (int) (Math.round(Math.random()*(62-1))+1);
			strBuf.append(randomStr.charAt(random));
		}
		return strBuf.toString();
	}

}
