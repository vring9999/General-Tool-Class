package com.hrcx.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class TokenUtil {
	/**
	 * 根据请求地址获取token-key
	 */
	public static String getTokenKey(HttpServletRequest request){
		String key = null;
		try {
			MessageDigest mDigest = MessageDigest.getInstance("MD5");//摘要算法可以自己选择
			byte[] result = mDigest.digest(request.getRequestURL().toString().getBytes());
			key = printHexString(result);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} 
		return key;
	}

	public static   String printHexString( byte[] b) {
		String a = "";
		for (int i = 0; i < b.length; i++) { 
			String hex = Integer.toHexString(b[i] & 0xFF); 
			if (hex.length() == 1) { 
				hex = '0' + hex; 
			}

			a = a+hex;
		} 

		return a;
	}


	/**
	 * 获取token-value并存储在session中
	 */
	public static String getTokenValue(HttpServletRequest request){
		String key = getTokenKey(request);
		Map<String,String> tokenMap = null;
		Object obj = request.getSession().getAttribute("tokenMap");
		if(obj == null){
			tokenMap = new HashMap<String,String>();
			request.getSession().setAttribute("tokenMap", tokenMap);
		} else {
			tokenMap = (Map<String,String>)obj;
		}
		if(tokenMap.containsKey(key)){
			return tokenMap.get(key);
		}
		String value = StringUtil.getUuid();
		tokenMap.put(key,value);
		return value;
	}

	/**
	 * 验证token
	 */
	public static boolean verify(String key ,String value ,HttpServletRequest request){
		boolean result = false;
		if (StringUtil.isEmpty(key) || StringUtil.isEmpty(value)) {//key或value只要有一个不存在就验证不通过
			return result;
		}

		if (request.getSession() != null) {
			Map<String,String> tokenMap = getTokenMaps(request);
			if(value.equals(tokenMap.get(key))){
				result = true;
				tokenMap.remove(key);//成功一次就失效
			}
		}
		return result;

	}
	
	public static Map<String,String> getTokenMaps(HttpServletRequest request){
		Object obj = request.getSession().getAttribute("tokenMap");
		Map<String,String> tokenMap = null;
		if(obj == null){
			tokenMap = new HashMap<String,String>();
			request.getSession().setAttribute("tokenMap", tokenMap);
		} else {
			tokenMap = (Map<String,String>)obj;
		}
		return tokenMap;
	}
}
