package com.zhihuitech.qualitymanagementsystem.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.List;

public class HttpUtil {
	public static String sendPostRequest(String url, List<NameValuePair> pairs) {
		HttpEntity requestHttpEntity;
		try {
			requestHttpEntity = new UrlEncodedFormEntity(
					pairs, HTTP.UTF_8);
			HttpPost httpPost = new HttpPost(url);
			// 将请求体内容加入请求中
			httpPost.setEntity(requestHttpEntity);
			HttpParams params = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(params, 30 * 1000); //设置连接超时
			HttpConnectionParams.setSoTimeout(params, 30 * 1000); //设置请求超时
			// 需要客户端对象来发送请求
			HttpClient httpClient = new DefaultHttpClient(params);
			// 发送请求
			HttpResponse response = httpClient.execute(httpPost);
			if (null == response) {
				return "";
			}
			HttpEntity httpEntity = response.getEntity();
			InputStream inputStream = httpEntity.getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			String result = "";
			String line = "";
			while (null != (line = reader.readLine())) {
				result += line;
			}
			return result;
		} catch (ConnectTimeoutException e) {
			return "连接超时，请稍后再试！";
		} catch (SocketTimeoutException e) {
			return "请求超时，请稍后再试！";
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String sendGetRequest(String url) {
		try {
			HttpGet httpGet = new HttpGet(url);
			//第二步，使用execute方法发送HTTP GET请求，并返回HttpResponse对象
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				//第三步，使用getEntity方法活得返回结果
				String result = EntityUtils.toString(httpResponse.getEntity());
				return result;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean checkConnection(Context context) {
		ConnectivityManager con = (ConnectivityManager) context.getSystemService(Activity.CONNECTIVITY_SERVICE);
		boolean wifi = con.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
		if (wifi) {
			return true;
		} else {
			return false;
		}
	}
}
