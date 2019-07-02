package com.hrcx.common.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.FormBody.Builder;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.apache.commons.lang.StringUtils;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

public class HttpUtil {
	private static final String CONTENT_TYPE = "application/json; charset=utf-8";
	private static final String FILE_CONTENT_TYPE = "application/octet-stream";
	private static final String ACCEPT = "application/json";
	private static final MediaType MEDIA_TYPE_JSON = MediaType.parse(CONTENT_TYPE);
	private static final MediaType MEDIA_TYPE_FILE = MediaType.parse(FILE_CONTENT_TYPE);
	private static OkHttpClient client = null;
	private static final Logger logger = ESAPI.getLogger(HttpUtil.class);
	static {
		int cacheSize = 10 * 1024 * 1024; // 10MB
		File cacheDirectory = null;
		try {
			cacheDirectory = Files.createTempDirectory("cache").toFile();
			Cache cache = new Cache(cacheDirectory, cacheSize);
			OkHttpClient.Builder httpBuilder = new OkHttpClient().newBuilder();
			client = httpBuilder.connectTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS)
					.cache(cache) // 设置超时时间
					.build();
		} catch (IOException e) {
			logger.error(Logger.EVENT_FAILURE,"IO异常", e);
		}
	}

	/**
	 * 同步get请求
	 * @param url
	 * @return
	 * @throws IOException 
	 * @
	 */
	public static Response syncGet(String url) throws IOException  {
		Request request = new Request.Builder().url(url).get().build();
		Response result = execute(request);
		return result;
	}
	/**
	 * 异步get请求
	 * @param url
	 * @param callBack
	 * @
	 */
	public static void asyncGet(String url, Callback callBack)  {
		Request request = new Request.Builder().url(url).get().build();
		enqueue(request, callBack);
	}
	public static Response syncPostForm(String url, Map<String, String> params) throws IOException  {
		Request.Builder requestBuilder = new Request.Builder().url(url);
		if (params != null && !params.isEmpty()) {
			Builder formBuilder = new FormBody.Builder();
			for (Map.Entry<String, String> entry : params.entrySet()) {
				formBuilder.add(entry.getKey(), entry.getValue());
			}
			RequestBody formBody = formBuilder.build();
			requestBuilder.post(formBody);
		}
		Request request = requestBuilder.build();
		Response result = execute(request);
		return result;
	}
	/**
	 * post对象
	 * @param url
	 * @param postBody
	 * @return
	 * @throws IOException 
	 * @
	 */
	public static Response syncPostJson(String url, String postBody) throws IOException  {
		Request request = new Request.Builder().url(url).addHeader("Content-type", CONTENT_TYPE).addHeader("Accept", ACCEPT)
				.post(RequestBody.create(MEDIA_TYPE_JSON, postBody)).build();
		Response result = execute(request);
		return result;
	}
	/**
	 * put对象
	 * @param url
	 * @param json
	 * @return
	 * @throws IOException 
	 * @
	 */
	public static Response syncPutJson(String url, String json) throws IOException  {
		Request request = new Request.Builder().url(url).addHeader("Content-type", CONTENT_TYPE).addHeader("Accept", ACCEPT)
				.put(RequestBody.create(MEDIA_TYPE_JSON, json)).build();
		Response result = execute(request);
		return result;
	}
	/**
	 * delete对象
	 * @param url
	 * @return
	 * @throws IOException 
	 * @
	 */
	public static Response syncDelete(String url) throws IOException  {
		Request request = new Request.Builder().url(url).delete().build();
		Response result = execute(request);
		return result;
	}
	/**
	 * 文件上传
	 * @param url 服务端路径
	 * @param files 文件列表
	 * @return
	 * @throws IOException 
	 * @
	 */
	public static Response syncUploadFile(String url, File[] files) throws IOException  {
		MultipartBody.Builder formBuilder = null;
		Response result = null;
		for (File file : files) {
			RequestBody fileBody = RequestBody.create(MEDIA_TYPE_FILE, file);
			formBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
			String fileName = file.getName();
			formBuilder.addFormDataPart("file", fileName, fileBody);
		}
		if (formBuilder != null) {
			Request request = new Request.Builder().url(url).post(formBuilder.build()).build();
			result = execute(request);
		}
		return result;
	}
	/**
	 * 文件下载
	 * @param url 服务端路径
	 * @param path 本地保存位置
	 * @return File 保存后的文件对象
	 * @throws IOException 
	 * @
	 */
	public static File syncDownloadFile(String url, String path) throws IOException  {
		Request request = new Request.Builder().url(url).build();
		Response File = execute(request);
		String disposition = File.header("Content-disposition");
		String fileName = StringUtils.substringAfter(disposition, "attachment;filename=");
		File file = new File(path + fileName);
		// FileUtils.copyInputStreamToFile(File.body().byteStream(), file);
		FileOutputStream fos = null;
		try {
			InputStream is = File.body().byteStream();
			fos = new FileOutputStream(file);
			if(fos!=null){
				byte[] b = new byte[1024];
				while ((is.read(b)) != -1) {
					fos.write(b);
				}
			}
		} catch (IOException e) {
			throw e;
		}finally{
			if(fos!=null){
				fos.close();
			}
		}
		return file;
	}
	
	public static Response syncPostFormWithHeaders(String url, Map<String, String> params, Map<String, String> headerParams)
			throws IOException {
		Request.Builder requestBuilder = new Request.Builder().url(url);
		if (headerParams != null && !headerParams.isEmpty()) {
			for (Map.Entry<String, String> entry : headerParams.entrySet()) {
				requestBuilder.addHeader(entry.getKey(), entry.getValue());
			}
		}
		if (params != null && !params.isEmpty()) {
			Builder formBuilder = new FormBody.Builder();
			for (Map.Entry<String, String> entry : params.entrySet()) {
				formBuilder.add(entry.getKey(), entry.getValue());
			}
			RequestBody formBody = formBuilder.build();
			requestBuilder.post(formBody);
		}
		Request request = requestBuilder.build();
		Response result = execute(request);
		return result;
	}
	
	
	// 同步调用
	private static Response execute(Request request) throws IOException  {
		Response response = client.newCall(request).execute();
		return response;
	}
	// 异步调用
	private static void enqueue(Request request, Callback call)  {
		client.newCall(request).enqueue(call);
	}
}
