package com.asuscloud.storage;
/**
 * 
 */

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;

import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import java.util.concurrent.Callable;

/**
 * @author Mirage Lin
 *
 */
public class downloadBinaryDataTask implements Callable <byte[]> {

	private String mUrl, mPayload, mSID, mProgkey, mToken;

	public downloadBinaryDataTask (String url, String payload, String sSID, String sProgKey, String sToken)
	{
		this.mUrl = url;
		this.mPayload = payload;
		this.mSID = sSID;
		this.mProgkey = sProgKey;
		this.mToken = sToken;
	}
	
	@Override
	public byte[] call() throws Exception {

		byte[] data;
		URL postURL = new URL(this.mUrl);
		//HttpsURLConnection conn = (HttpsURLConnection) postURL.openConnection();
		HttpURLConnection conn = (HttpURLConnection) postURL.openConnection();
		
		conn.setConnectTimeout(60000);
		conn.setReadTimeout(60000);
		conn.setRequestMethod("GET");
		
		if (this.mUrl.indexOf("directdownload") > 0)
		{
			int i= 5;
			i=i+1;
		}
		
		
		String authorization = null;
		
		
		
		
		try {
			authorization = composeAuthorizationHeader(this.mProgkey);
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("Composing developer authorization string error:")
					.append(e.getMessage());
			throw new MalformedURLException(msg.toString());
		}
		conn.addRequestProperty("Authorization", authorization);
		
		StringBuilder cookie = new StringBuilder();
		cookie.append("sid=").append(mSID).append(";").append("c=")
				.append("android").append(";").append("v=")
				.append("1.0").append(";");
		conn.setRequestProperty("cookie", cookie.toString());

		//conn.setDoOutput(false);

		conn.setDoInput(true);
		//conn.setReadTimeout(180000);
		
		try {
			conn.connect();
		} catch (IOException ioe) {
			
			throw ioe;
		}

		InputStream in = null;
		try{
			in =  conn.getInputStream();
		}
		catch (IOException ioe)
		{
			data =  null;
		}
		
        byte[] buff = new byte[4096];

        int bytesRead = 0;

        ByteArrayOutputStream bao = new ByteArrayOutputStream();


			try {
				while((bytesRead = in.read(buff)) != -1) {
				   bao.write(buff, 0, bytesRead);
				}
				
				in.close();
			} catch (IOException e1) {

				return null;
			}

        data = bao.toByteArray();
		
        return data;

		
	}

public String composeAuthorizationHeader(String sProgKey) throws Exception {
		if (sProgKey == null
				|| sProgKey.trim().length() == 0) {
			throw new Exception("There's no program key!");
		}

		String SIGNATURE_METHOD = "HMAC-SHA1";
		StringBuilder authorization = new StringBuilder();

		String nonce = UUID.randomUUID().toString().replaceAll("-", "");
		String timestamp = String.valueOf((long) Calendar.getInstance()
				.getTimeInMillis());
		String signature = null;

		// Step 1, Compose signature string
		StringBuilder signaturePre = new StringBuilder();
		signaturePre.append("nonce=").append(nonce)
				.append("&signature_method=").append(SIGNATURE_METHOD)
				.append("&timestamp=").append(timestamp);

		// Step 2, Doing urlencode before doing hash
		String signatureURLEn = URLEncoder.encode(signaturePre.toString(),
				"UTF-8");

		// Java Only Support HMACSHA1
		String signMethod = SIGNATURE_METHOD.replaceAll("-", "");

		// Step 3, Doing hash signature string by HMAC-SHA1
		SecretKey sk = new SecretKeySpec(sProgKey.getBytes("UTF-8"),
				signMethod);
		Mac m = Mac.getInstance(signMethod);
		m.init(sk);
		byte[] mac = m.doFinal(signatureURLEn.getBytes("UTF-8"));

		// Step 4, Doing base64 encoding & doing urlencode again
		signature = URLEncoder.encode(new String(Base64.encodeToByte(mac),
				"UTF-8"), "UTF-8");

		// Final step, Put all parameters to be authorization header string
		authorization.append("signature_method=\"").append(SIGNATURE_METHOD)
				.append("\",").append("timestamp=\"").append(timestamp)
				.append("\",").append("nonce=\"").append(nonce).append("\",")
				.append("signature=\"").append(signature).append("\"");

		return authorization.toString();
	}


}

