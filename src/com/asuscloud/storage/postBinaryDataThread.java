package com.asuscloud.storage;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class postBinaryDataThread implements Callable <String> {

	private String mUrl, mSID, mProgkey, mToken;
	private byte[] binaryData;
	
	public postBinaryDataThread (String url, String sSID, String sProgKey, String sToken, byte[] data) throws IOException
	{
		this.mUrl = url;
		
		this.binaryData = data.clone();
		
		this.mSID = sSID;
		this.mProgkey = sProgKey;
		this.mToken = sToken;
	}
	
	@Override
	public String call() throws Exception {

		URL postURL = new URL(this.mUrl);
		//HttpsURLConnection conn = (HttpsURLConnection) postURL.openConnection();
		HttpURLConnection conn = (HttpURLConnection) postURL.openConnection();
		
		conn.setConnectTimeout(60000);
		conn.setReadTimeout(60000);
		conn.setRequestMethod("POST");
		String authorization = null;
		boolean isHbase = false;
		
		
		try {
			authorization = composeAuthorizationHeader(this.mProgkey);
		} catch (Exception e) {
			StringBuilder msg = new StringBuilder();
			msg.append("Composing developer authorization string error:")
					.append(e.getMessage());
			throw new MalformedURLException(msg.toString());
		}
		conn.addRequestProperty("Authorization", authorization);
		
		if ( mUrl.indexOf("tsdbase") > 0)
		{
			conn.addRequestProperty("Content-Type", "text/x-omni-json-1.0");
			conn.addRequestProperty("X-Omni-Token", this.mToken);
			conn.addRequestProperty("X-Omni-Sid", this.mSID);
			isHbase = true;
		}

		StringBuilder cookie = new StringBuilder();
		cookie.append("sid=").append(mSID).append(";").append("c=")
				.append("android").append(";").append("v=")
				.append("1.0").append(";");
		conn.setRequestProperty("cookie", cookie.toString());

		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setReadTimeout(180000);
		
		try {
			conn.connect();
		} catch (IOException ioe) {
			
			throw ioe;
		}

		
		OutputStream out = conn.getOutputStream();
		
		out.write(this.binaryData);
		out.flush();
		out.close();	
		

		
		InputStream in =null;
		try{
			in = conn.getInputStream();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}

		if (mUrl.indexOf("webrelay") > 0)
		{
		  int i =0;
		  i= i+1;
		}
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(in);	//以樹狀格式存於記憶體中﹐比較耗記憶體
		Element root = document.getDocumentElement();	//取得檔案的"根"標籤


		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		//initialize StreamResult with File object to save to file
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(document);
		transformer.transform(source, result);
		String xmlString = null;
		
		if (isHbase)
		{//如果是Hbase, 要先抓取 Header內的 X-Omni-Status, 如果狀態不為0, 則要把輸出的xml轉成 status
			Map<String, List<String>> headerFields = conn.getHeaderFields();

			Set<String> headerFieldsSet = headerFields.keySet();
			Iterator<String> hearerFieldsIter = headerFieldsSet.iterator();
			
			while (hearerFieldsIter.hasNext()) {
				
				 String headerFieldKey = hearerFieldsIter.next();
				 List<String> headerFieldValue = headerFields.get(headerFieldKey);
				 
				 StringBuilder sb = new StringBuilder();
				 if (headerFieldKey != null)
				 for (String value : headerFieldValue) {
					 if (headerFieldKey.equals("X-Omni-Status") )
					 {
						 if (!value.equals("0") )
						 {
							 xmlString = "X-Omni-Status:" + value;
						 }
					 }
				 	 
				}
				 
			}
		}

		if ( xmlString == null )
			xmlString = result.getWriter().toString();
		
		return xmlString;

		
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

