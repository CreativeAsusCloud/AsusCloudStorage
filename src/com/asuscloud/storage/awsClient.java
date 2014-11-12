package com.asuscloud.storage;
/**
 * 
 */


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.util.Log;
import android.util.Base64;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * @author Mirage Lin
 *
 */

public class awsClient {

	/**
	 * 
	 */
	private String tsdBaseURL;
	private String servicePortalURL;
	private String serviceGatewayURL;
	private String infoRelayURL;
	private String webRelayURL;
	private String searchServer;
	private String SID;
	private String progKey;
	private String token;
	private String UserName;
	private String Password;
	
	public String ExceptionMessage;
	public int lastError;
	private String currentTransactionID;
	private String currentChecksum;
	
	
	public awsClient( String sSID, String sProgKey, String sUserName, String sPassword) throws ACSException, TimeoutException {
		// TODO Auto-generated constructor stub
		super();
		this.progKey = sProgKey;
		this.SID = sSID;
		this.UserName = sUserName;
		this.Password = HashPassword(sPassword);
		//this.Password = tripleDesPassword(sPassword);
		this.servicePortalURL = "https://sp.yostore.net";
		boolean isAcquire = acquireToken();
		if (!isAcquire) {
			throw new ACSException("Acqurie Token Failed!!");
		}
	}
	
	/*
	 * put data to time series server.
	 * 使用XML的tsdbase
	 */
	public Boolean PostToHBase(String rawData, String apiDirectory) throws ACSException, TimeoutException {
		boolean OK = true;
        this.ExceptionMessage = null;
        this.lastError = 0;
 
		String params[] = new String[2];
	    params[0] = "https://"+this.tsdBaseURL + apiDirectory;
	    params[1] = rawData;
	    
	    String responseXML = sendRequest(params);
	    
		if ( responseXML != null){
			if (responseXML.indexOf("X-Omni-Status") >= 0) {
				String [] temp = responseXML.split(":");
				this.ExceptionMessage = this.getErrorMsg(Integer.valueOf(temp[1]).intValue());
				OK  = false;
			}
		} else {
			OK = false;
			this.ExceptionMessage = this.getErrorMsg(990);
		}
		return OK;
	}
	/*
	 * Query data from time series server
	 * 使用XML格式
	 */
	public String QueryHBase(String rawData, String apiDirectory) throws ACSException, TimeoutException {
        this.ExceptionMessage = null;
        this.lastError = 0;
 
		String params[] = new String[2];
	    params[0] = "https://"+this.tsdBaseURL + apiDirectory;
	    params[1] = rawData;
	    
	    String responseXML = sendRequest(params);
	    
		if (responseXML.indexOf("X-Omni-Status") >= 0) {
			String [] temp = responseXML.split(":");
			this.ExceptionMessage = this.getErrorMsg(Integer.valueOf(temp[1]).intValue());
			return null;
		}
		return responseXML;
	}
	
	/* 
	 * 取得MySyncFolder的folderID
	 */
	public String getMySyncFolder() throws ACSException, TimeoutException {
		String getMysyncFolderXML = new StringBuilder().append("<getmysyncfolder><token>").append(this.token).append("</token><userid>").append(this.UserName).append("</userid></getmysyncfolder>").toString();
		String params[] = new String[2];
	    params[0] = "https://"+this.infoRelayURL + "/folder/getmysyncfolder/";
	    params[1] = getMysyncFolderXML;
	    
		String responseXML = sendRequest(params);
		Document dom = this.becomeDom(responseXML);
		
		String status = dom.getElementsByTagName("status").item(0).getTextContent();
		
		String syncFolderID = null;
		
		if (status.equals("0")) {
			syncFolderID = dom.getElementsByTagName("id").item(0).getTextContent();
		} else {
			throw new ACSException("getMySyncFolder failed, status = " + status + ", Message = " + getErrorMsg(Integer.valueOf(status)));
		}
		return syncFolderID;
	}
	
	/*
	 * 上傳檔案
	 * @param1: 上傳的字串
	 * @param2: 新增的檔案名稱
	 * @param3: Parent Folder ID
	 * @param4: 若檔案已存在，fileID, 若檔案不存為null
	 */
	public boolean uploadFileFromMemoryStream(String data, String fileName, String folderID, String fileID) throws ACSException, TimeoutException {
		boolean isOK = true;
		try {
			isOK = initbinaryupload(fileName, folderID, data, false, this.currentTransactionID, fileID);
			isOK = resumebinaryupload(data);
			isOK = finishbinaryupload();
		} catch ( UnsupportedEncodingException e) {
			throw new ACSException("upload file from memory stream, encoding failed.");
		}
		return isOK;
	}
	
	/*
	 * 上傳檔案
	 * @param1: 上傳的InputStream
	 * @param2: 新增的檔案名稱
	 * @param3: Parent Folder ID
	 * @param4: 若檔案已存在，fileID, 若檔案不存為null
	 */
	public boolean uploadFileFromMemoryStream(InputStream stream, String fileName, String folderID, String fileID) throws ACSException, TimeoutException {
		boolean isOK = true;
        byte[] buff = new byte[4096];
        int bytesRead = 0;
        ByteArrayOutputStream bao = new ByteArrayOutputStream();

		try {
			while((bytesRead = stream.read(buff)) != -1) {
				bao.write(buff, 0, bytesRead);
			}
			stream.close();
		} catch (IOException e1) {
			this.ExceptionMessage = e1.getMessage();
			this.lastError = -1;
			return false;
		}

        byte[] data = bao.toByteArray();
		
		try {
			isOK = initbinaryupload(fileName, folderID, data, false, this.currentTransactionID, fileID);
			isOK = resumebinaryupload(data);
			isOK = finishbinaryupload();
		} catch (UnsupportedEncodingException e) {
			throw new ACSException("upload file from memory stream, encoding failed.");
		} catch (IOException e) {
			throw new ACSException("upload file from memory stream, IOException.");
		}
		return isOK;
	}
	
	/*
	 * upload file 一部曲
	 * 上傳String
	 */
	private boolean initbinaryupload(String fileName, String folderID, String data, boolean isResume, String transactionID, String fileID) throws ACSException, TimeoutException, UnsupportedEncodingException {
		this.currentChecksum = null;
		this.currentTransactionID = null;
		
		Long tsLong = System.currentTimeMillis()/1000;
		String timeAttribute =  tsLong.toString();
		
		String atPlainText = new StringBuilder().append("<creationtime>").append(timeAttribute).append("</creationtime>").append("<lastaccesstime>").append(timeAttribute).append("</lastaccesstime>").append("<lastwritetime>").append(timeAttribute).append("</lastwritetime>").toString();
		String at = URLEncoder.encode(atPlainText,"UTF-8");
		String na = Base64.encodeToString(fileName.getBytes("UTF-8"), Base64.NO_WRAP);

		String tx ="";//續傳時才要使用的transcationID
		if (transactionID != null && transactionID.length() > 0) {
			tx = "&tx=" + transactionID;
		}

		String fi = "";//要複寫的的fileID
		if (fileID != null && fileID.length() > 0)	{
			fi = "&fi=" +fileID;
		}		

		String queryString = new StringBuilder().append("?dis=").append(this.SID).append("&tk=").append(this.token).append("&na=").append(na).append("&pa=").append(folderID).append("&sg=G6243JWEW").append("&at=").append(at).append("&fs=").append(data.length()).append(tx).append(fi).toString();

		String responseXML = null;
		String params[] = new String[5];
	    params[0] = "https://"+this.webRelayURL +"/webrelay/initbinaryupload/" + queryString;
	    params[1] = null;
	    params[2] = this.SID;
	    params[3] = this.progKey;
	    params[4] = this.token;
	    
	    FutureTask <String> postTask = new FutureTask<String>(new downloadDataTask(params[0], params[1],params[2],params[3], params[4]) );
	    
	    ExecutorService es = Executors.newSingleThreadExecutor();
	    es.submit(postTask);		
	    
	    try{
	    	responseXML = postTask.get(180000, TimeUnit.MILLISECONDS);
	    } catch (Exception e) {
	    	throw new ACSException("init binary upload failed, post payload failed.");
	    }

		if ( responseXML == null) {
			throw new ACSException("init binary  upload failed, response is null.");
		}
		
		Document dom = becomeDom(responseXML);
		String status = dom.getElementsByTagName("status").item(0).getTextContent();
		if (status.equals("0")) {
			this.currentTransactionID = dom.getElementsByTagName("transid").item(0).getTextContent();
			if (fileID != null && fileID.length() > 0) {
				this.currentChecksum = dom.getElementsByTagName("latestchecksum").item(0).getTextContent();
			}
		} else {
			throw new ACSException("init binary upload failed, status = " + status);
		}
		return true;
	}
	/*
	 * upload file 一部曲
	 * 上傳byte[]
	 */
	private boolean initbinaryupload(String fileName, String folderID, byte[] data, boolean isResume, String transactionID, String fileID)throws ACSException, TimeoutException, UnsupportedEncodingException {
		this.currentChecksum = null;
		this.currentTransactionID = null;
		
		Long tsLong = System.currentTimeMillis()/1000;
		String timeAttribute =  tsLong.toString();
		
		String atPlainText = new StringBuilder().append("<creationtime>").append(timeAttribute).append("</creationtime>").append("<lastaccesstime>").append(timeAttribute).append("</lastaccesstime>").append("<lastwritetime>").append(timeAttribute).append("</lastwritetime>").toString();
		String at = URLEncoder.encode(atPlainText,"UTF-8");
		String na = Base64.encodeToString(fileName.getBytes("UTF-8"), Base64.NO_WRAP);

		String tx ="";
		if (transactionID != null && transactionID.length() > 0){
			tx = "&tx=" +transactionID;
		}
		
		String fi = "";
		if (fileID != null && fileID.length() > 0)	{
			fi = "&fi=" + fileID;
		}		
		
        String queryString = new StringBuilder().append("?dis=").append(this.SID).append("&tk=").append(this.token).append("&na=").append(na).append("&pa=").append(folderID).append("&sg=G6243JWEW").append("&at=").append(at).append("&fs=").append(Integer.valueOf(data.length)).append(tx).append(fi).toString();
		String responseXML = null;
		String params[] = new String[5];
	    params[0] = "https://"+this.webRelayURL +"/webrelay/initbinaryupload/" + queryString;
	    params[1] = null;
	    params[2] = this.SID;
	    params[3] = this.progKey;
	    params[4] = this.token;
	    
	    FutureTask <String> postTask = new FutureTask<String>(new downloadDataTask(params[0], params[1],params[2],params[3], params[4]) );
	    
	    ExecutorService es = Executors.newSingleThreadExecutor();
	    es.submit(postTask);		
	    
	    try {
	    	responseXML = postTask.get(180000, TimeUnit.MILLISECONDS);
	    } catch (Exception e) {
	    	throw new ACSException("init binary upload failed, post payload failed.");
	    }

		if ( responseXML == null) {
			throw new ACSException("init binary  upload failed, response is null.");
		}
		
		Document dom = becomeDom(responseXML);
		
		String status = dom.getElementsByTagName("status").item(0).getTextContent();
		
		if (status.equals("0"))	{
			this.currentTransactionID = dom.getElementsByTagName("transid").item(0).getTextContent();
			if (fileID != null && fileID.length() > 0) {
				this.currentChecksum = dom.getElementsByTagName("latestchecksum").item(0).getTextContent();
			}
		} else {
			throw new ACSException("init binary upload failed, status = " + status);
		}
		return true;
	}
	
	/*
	 * upload file 二部曲
	 * 上傳String
	 */
	private boolean resumebinaryupload(String data) throws ACSException, TimeoutException {
		String queryString = new StringBuilder().append("?dis=").append(this.SID).append("&tk=").append(this.token).append("&tx=").append(this.currentTransactionID).toString();
		String responseXML = null;
		String params[] = new String[5];
	    params[0] = "https://"+this.webRelayURL + "/webrelay/resumebinaryupload/" + queryString;
	    params[1] = data;
	    params[2] = this.SID;
	    params[3] = this.progKey;
	    params[4] = this.token;
	    
	    FutureTask <String> postTask = new FutureTask<String>(new postDataThread(params[0], params[1],params[2],params[3], params[4]) );
	    ExecutorService es = Executors.newSingleThreadExecutor();
	    es.submit(postTask);		
	    
	    try {
	    	responseXML = postTask.get(180000, TimeUnit.MILLISECONDS);
	    } catch (Exception e){
	    	this.ExceptionMessage = e.getMessage();
	    }

		if (responseXML == null) {
			return false;
		}
		
		Document dom = becomeDom(responseXML);
		String status = dom.getElementsByTagName("status").item(0).getTextContent();
				
		if (!status.equals("0")) {
			throw new ACSException("resume binary upload failed, status = " + status);
		}
		return true;
	}

	/*
	 * upload file 二部曲
	 * 上傳byte[]
	 */
	private boolean resumebinaryupload(byte[] data) throws ACSException, TimeoutException, IOException {
		String queryString = new StringBuilder().append("?dis=").append(this.SID).append("&tk=").append(this.token).append("&tx=").append(this.currentTransactionID).toString();
		String responseXML = null;
		
		String params[] = new String[4];
	    params[0] = "https://"+this.webRelayURL + "/webrelay/resumebinaryupload/" + queryString;
	    params[1] = this.SID;
	    params[2] = this.progKey;
	    params[3] = this.token;
	    
	    FutureTask <String> postTask = new FutureTask<String>(new postBinaryDataThread(params[0], params[1],params[2],params[3], data) );
	    ExecutorService es = Executors.newSingleThreadExecutor();
	    es.submit(postTask);		
	    
	    try {
	    	responseXML = postTask.get(180000, TimeUnit.MILLISECONDS);
	    } catch (Exception e) {
	    	this.ExceptionMessage = e.getMessage();
	    }

		if ( responseXML == null) {
			return false;
		}
		
		Document dom = becomeDom(responseXML);
		String status = dom.getElementsByTagName("status").item(0).getTextContent();
		if (!status.equals("0")) {
			throw new ACSException("resume binary upload failed, status = " + status);
		}
		return true;
	}
	
	
	/*
	 * upload file 第三部曲
	 */
	public boolean finishbinaryupload() throws ACSException, TimeoutException {
		if (this.currentTransactionID == null)
			return false;
		if (this.currentTransactionID.length() == 0 )
			return false;
		
		String lsg = "";
		if (this.currentChecksum != null)
		{
			if (this.currentChecksum.length() > 0)
				lsg = "&lsg=" + this.currentChecksum;
		}
		String queryString = new StringBuilder().append("?dis=").append(this.SID).append("&tk=").append(this.token).append("&tx=").append(this.currentTransactionID).append(lsg).toString();
	
		String responseXML = null;
		String params[] = new String[5];
	    params[0] = "https://"+this.webRelayURL +  "/webrelay/finishbinaryupload/" + queryString;
	    params[1] = "";
	    params[2] = this.SID;
	    params[3] = this.progKey;
	    params[4] = this.token;
	    
	    FutureTask <String> postTask = new FutureTask<String>(new postDataThread(params[0], params[1],params[2],params[3], params[4]) );
	    ExecutorService es = Executors.newSingleThreadExecutor();
	    es.submit(postTask);		
	    
	    try {
	    	responseXML = postTask.get(180000, TimeUnit.MILLISECONDS);
	    } catch (Exception e) {
	    	throw new ACSException("finish binary upload failed, post payload failed.");
	    }

		if (responseXML == null) {
			return false;
		}
		
		Document dom = becomeDom(responseXML);
		String status = dom.getElementsByTagName("status").item(0).getTextContent();
				
		if (status.equals("0")) {
			this.currentChecksum = "";
			this.currentTransactionID = "";	
		} else {
			throw new ACSException("finish binary upload failed, status = " + status);
		}
		return true;
	}
	/*
	 * DownloadFileToString
	 * @param1 : the file id
	 * @return: string
	 */
	public String DownloadFileToString(String fileID) throws ACSException, TimeoutException {
		String queryString = "&fi=" + fileID;
		String responseXML = null;
		String params[] = new String[5];
	    params[0] = "https://"+this.webRelayURL +  "/webrelay/directdownload/" + this.token+"/"+ "?dis=" + this.SID + queryString;
	    params[1] = "";
	    params[2] = this.SID;
	    params[3] = this.progKey;
	    params[4] = this.token;

	    /*使用downloadDataTask*/
	    FutureTask <String> postTask = new FutureTask<String>(new downloadDataTask(params[0], params[1],params[2],params[3], params[4]) );
	    ExecutorService es = Executors.newSingleThreadExecutor();
	    es.submit(postTask);		
	    
	    try {
	    	responseXML = postTask.get(180000, TimeUnit.MILLISECONDS);
	    } catch (Exception e) {
	    	throw new ACSException("download file to string failed, post payload failed.");
	    }
		return responseXML;
	}
	/*
	 * DownloadFileToStream
	 * @param1 : the file id
	 * @return: byte[]
	 */
	public byte[] DownloadFileToStream(String fileID) throws ACSException, TimeoutException {
		String queryString = "&fi=" + fileID;

		String params[] = new String[5];
	    params[0] = "https://"+this.webRelayURL +  "/webrelay/directdownload/" + this.token+"/"+ "?dis=" + this.SID + queryString;
	    params[1] = "";
	    params[2] = this.SID;
	    params[3] = this.progKey;
	    params[4] = this.token;
	    /*使用downloadBinaryDataTask*/
	    FutureTask <byte[]> postTask = new FutureTask<byte[]>(new downloadBinaryDataTask(params[0], params[1],params[2],params[3], params[4]) );
	    ExecutorService es = Executors.newSingleThreadExecutor();
	    es.submit(postTask);		
	    byte[] data = null;
	    try{
	    	data = postTask.get(180000, TimeUnit.MILLISECONDS);
	    } catch (Exception e) {
	    	throw new ACSException("download file to string failed, post payload failed.");
	    }
		return data.clone();
	}
	
	/*
	 * 發browse folder的API
	 * @return 回傳server傳回的XML string
	 */
	
	public String browseFolder(String folderID) throws ACSException, TimeoutException {
		String browseFolderXML = new StringBuilder().append("<browse><token>").append(this.token).append("</token><userid>").append(this.UserName).append("</userid><folderid>").append(folderID).append("</folderid><sortby>2</sortby><sortdirection>1</sortdirection></browse>").toString();
		String params[] = new String[2];
	    params[0] = "https://"+this.infoRelayURL + "/folder/browse/";
	    params[1] = browseFolderXML;

	    String responseXML = sendRequest(params);
	    
		if ( responseXML == null) {
			throw new ACSException("browser Folder failed, response is null.");
		}
		
		Document dom = becomeDom(responseXML);
		String status = dom.getElementsByTagName("status").item(0).getTextContent();
		
		if (!status.equals("0")) {
			throw new ACSException("browseFolder failed, status = " + status + ", Message = " + getErrorMsg(Integer.valueOf(status)));
		}
		return responseXML;
	}
	
	public String getLatestChangeFiles() throws ACSException, TimeoutException {
		String getLatestChangeFiles = new StringBuilder().append("<getlatestchangefiles><token>").append(this.token).append("</token><userid>").append(this.UserName).append("</userid><top>1</top><targetroot>-5</targetroot><sortdirection>1</sortdirection></getlatestchangefiles>").toString();
		String params[] = new String[2];
	    params[0] = "https://"+this.infoRelayURL + "/file/getlatestchangefiles/";
	    params[1] = getLatestChangeFiles;
	    
	    String responseXML = sendRequest(params);
	    
		if (responseXML == null) {
			throw new ACSException("get last change files failed, response is null.");
		}
		
		Document dom = this.becomeDom(responseXML);
		
		String status = dom.getElementsByTagName("status").item(0).getTextContent();
		if (!status.equals("0")) {
			throw new ACSException("get last change files failed, status = " + status + ", Message = " + getErrorMsg(Integer.valueOf(status)));
		}
		return responseXML;
	}
	
	/*public String getFileID(String fileName, String folderID)
	{
		String xmlBrowseResult = "";
		String fileID = "";
		try{
			xmlBrowseResult = this.browseFolder(folderID);
		}
		catch (ParserConfigurationException pce)
		{
			this.ExceptionMessage = pce.getMessage();
			return null;
		}
		catch (SAXException sax)
		{
			this.ExceptionMessage = sax.getMessage();
			return null;				
		}
		catch (IOException ioe)
		{
			this.ExceptionMessage = ioe.getMessage();
			return null;				
		}
		
		Document dom;
		
		try {
			dom = this.becomeDom(xmlBrowseResult);
		} catch (ParserConfigurationException e) {
			this.ExceptionMessage = e.getMessage();
			return null;
		} catch (SAXException e) {
			this.ExceptionMessage = e.getMessage();
			return null;
		} catch (IOException e) {
			this.ExceptionMessage = e.getMessage();
			return null;
		}
		
		String status = dom.getElementsByTagName("status").item(0).getTextContent();
					
		if ( status.equals("0") )
		{
			
			NodeList files = dom.getElementsByTagName("file");
			String displayName = null;
			String text = null;
			
			for (int i= 0; i< files.getLength(); i++)
			{
				Node fileNode = files.item(i);
				Element el = (Element)fileNode;
				displayName = el.getElementsByTagName("display").item(0).getTextContent();
				
				byte[] data = Base64.decode(displayName, Base64.NO_WRAP);
				try {
					text = new String(data, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if ( text.equalsIgnoreCase(fileName) )
				{
					fileID = el.getElementsByTagName("id").item(0).getTextContent();
				}
			}
			
			
		}
		
		return fileID;
		
	}*/
	
	/*
	 * Create Folder
	 * 建立新資料夾
	 * @param1: 欲建立的資料夾名稱
	 * @param2: 母資料夾的folder ID
	 */
	public boolean createFolder(String folderName, String ParentFolderID) throws ACSException, TimeoutException {
		String timestamp = String.valueOf((long)Calendar.getInstance().getTimeInMillis());
		String attrib = "<creationtime>"+"timestamp"+"</creationtime><lastaccesstime>"+timestamp+"</lastaccesstime><lastwritetime>"+timestamp+"</lastwritetime>";
		
		String na;
		try {
			na = Base64.encodeToString(folderName.getBytes("UTF-8"), Base64.NO_WRAP);
		} catch (UnsupportedEncodingException e) {
			throw new ACSException("create folder failed, encoding foldername(UTF-8) failed.");
		}
		
		String xmlcreatefolder = "<create><token>" + this.token + "</token><userid>" + this.UserName + "</userid>" +
                "<parent>" + ParentFolderID + "</parent><isencrypted>0</isencrypted><attribute>"+attrib+"</attribute><display>"+na+"</display><isgroupaware>0</isgroupaware></create>";

		String params[] = new String[2];
	    params[0] = "https://"+this.infoRelayURL + "/folder/create/";
	    params[1] = xmlcreatefolder;
		
		String responseXML = sendRequest(params);

		if (responseXML == null) {
			throw new ACSException("create folder failed, response is null.");
		}

		Document dom = becomeDom(responseXML);
		
		String status = dom.getElementsByTagName("status").item(0).getTextContent();

		if (!status.equals("0")) {
			throw new ACSException("create folder failed, status = " + status + ",Message = "+getErrorMsg(Integer.valueOf(status)));
		}
		return true;
	}
	
	/*
	 * remove folder 
	 * 刪除Folder
	 * @param1: 欲刪除的folderID
	 */
	public boolean removeFolder(String folderID) throws ACSException, TimeoutException {
		String xmlRemovefolder = "<remove><token>" + this.token + "</token><userid>" + this.UserName + "</userid>" +
                "<id>" + folderID + "</id><ischildonly>0</ischildonly><isgroupaware>0</isgroupaware></remove>";

		String params[] = new String[2];
	    params[0] = "https://"+this.infoRelayURL + "/folder/remove/";
	    params[1] = xmlRemovefolder;
	    String responseXML = sendRequest(params);
	    
		if ( responseXML == null) {
			throw new ACSException("remove folder failed, response is null.");
		}

		Document dom = becomeDom(responseXML);
		String status = dom.getElementsByTagName("status").item(0).getTextContent();

		if (!status.equals("0")) {
			throw new ACSException("remove folder failed, status = " + status);
		}
		return true;
	}
	

	/*
	 * remove File 刪除檔案
	 * @param1: 欲刪除的fileID陣列
	 */
	public boolean removeFile(String[] fileIDs) throws ACSException, TimeoutException {
		if (fileIDs == null || fileIDs.length == 0) {
			throw new ACSException("remove file failed, file id array is null.");
		}
		
		/*產生file id的payload字串*/
		String fileIDList = "";	
		if (fileIDs.length > 1)	{
			for (int i = 0; i< fileIDs.length; i++)	{
				fileIDList = fileIDList + fileIDs[i];
				if (i < (fileIDs.length - 1)) {
					fileIDList = fileIDList + ",";
				}
			}
		} else {
			fileIDList = fileIDs[0];
		}
		
		String xmlRemoveFile = "<remove><token>" + this.token + "</token><userid>" + this.UserName + "</userid>" +
                "<id>" + fileIDList + "</id></remove>";
		Log.d("melissa","remove payload = "+xmlRemoveFile);
		String params[] = new String[2];
	    params[0] = "https://"+this.infoRelayURL + "/file/remove/";
	    params[1] = xmlRemoveFile;
	    
	    String responseXML = sendRequest(params);

		if ( responseXML == null) {
			throw new ACSException("remove file failed, response is null.");
		}

		Document dom= becomeDom(responseXML);
		
		String status = dom.getElementsByTagName("status").item(0).getTextContent();
		
		if (!status.equals("0")) {
			throw new ACSException("remove file failed, status = " + status);
		}
		return true;
	}
	
	protected String deletesharecode(String id, String isFile) throws ACSException, TimeoutException {
		String params[] = new String[2];
		params[0] = "https://" + this.infoRelayURL + "/fsentry/deletesharecode/";
		params[1] = "<deletesharecode><token>"+this.token+"</token><scrip>"+getScrip()+
				    "</scrip><userid>"+this.UserName+"</userid><entrytype>" + isFile + "</entrytype><entryid>"+id+
				    "</entryid><password></password></deletesharecode>";
		return sendRequest(params);
	}
	/*
	 * 分享檔案或目錄
	 * @param1: File ID or Folder ID
	 * @param2: 透過分享碼存取該檔案/目錄所需的密碼
	 * @param3: 是否為folder
	 */
	protected String setadvancedsharecode(String id, String password, String isFolder) throws ACSException, TimeoutException {
		String params[] = new String[2];
		params[0] = "https://" + this.infoRelayURL + "/fsentry/setadvancedsharecode/";
		params[1] = "<setadvancedsharecode><token>"+this.token+"</token><userid>"+this.UserName+
				    "</userid><isfolder>"+isFolder+"</isfolder><entryid>"+id+"</entryid><clearshare>0</clearshare>"+
				    "<clearpassword>0</clearpassword><clearvalidityduration>0</clearvalidityduration>"+
				    "<releasequotalimit>0</releasequotalimit><password>"+HashPassword(password)+
				    "</password></setadvancedsharecode>";
		return sendRequest(params);
	}
	
	/*
	 * Author by Melissa.
	 * 用目錄ID查詢folder or file的分享狀態
	 */
	protected String getShareStatus(String id, String isFolder) throws ACSException, TimeoutException {
		String params[] = new String[2];
		params[0] = "https://" + this.infoRelayURL + "/fsentry/getadvancedsharecode/";
		params[1] = "<getadvancedsharecode><token>"+ this.token +"</token><userid>"
		             +this.UserName+"</userid><isfolder>"+isFolder+"</isfolder><entryid>"+id+"</entryid></getadvancedsharecode>";
		return sendRequest(params);
	}
	/*
	 * Author by Melissa.
	 * 用目錄ID查詢folder下的分享狀態
	 */
	protected String getshareentry(String id) throws ACSException, TimeoutException {
		String API = "/fsentry/getchildrensharedentries/";
		String params[] = new String[2];
		params[0] = "https://" + this.infoRelayURL + API;
		params[1] = "<getchildrensharedentries><token>"+ this.token +"</token><userid>"
		             +this.UserName+"</userid><ffid>"+id+"</ffid></getchildrensharedentries>";
		return sendRequest(params);
	}
		
	/*
	 * Author by Melissa.
	 * Add getMediaMetaData protected method to get photo's EXIF data.
	 */
	/*protected String getMediaMetaData(String filepath, String folderID) throws ACSException, TimeoutException {
		String queryString = "&fi=" + this.getFileID(filepath, folderID);
		String params[] = new String[2];
	    params[0] = "https://"+this.webRelayURL + "/webrelay/getmediametadata/" + this.token + "/" + "?dis=" + this.SID + queryString;
	    params[1] = "";
	    return sendRequest(params);
	}*/
	
	/*omni seartch*/
	protected String searchKeyword(String keyword, String folderID) throws ACSException, TimeoutException {
		String API = "/fulltext/sqlquery/";
		String params[] = new String[2];
		params[0] = "https://"+this.searchServer + API + this.token;
		params[1] = "<sqlquery><userid>"+this.UserName+"</userid><keyword>"+keyword+"</keyword><kind>"
					+"1"+"</kind><ancestorid>"+folderID+"</ancestorid></sqlquery>";
		return sendRequest(params);
	}
	
	protected String getPersonalSystemFolder() throws ACSException, TimeoutException {
		String API = "/folder/getpersonalsystemfolder/";
		String params[] = new String[2];
		params[0] = "https://"+this.infoRelayURL + API;
		params[1] = "<getpersonalsystemfolder><token>"+this.token+"</token><userid>"+this.UserName+"</userid><rawfoldername>MyRecycleBin</rawfoldername></getpersonalsystemfolder>";
		return sendRequest(params);
	}
	
	/*
	 * 取得status所代表的error message
	 */
	private String getErrorMsg(int status) {
		String msg;
		switch (status)
		{
        case 2:
            msg = "Authentication Fail";
            break;
        case 5:
            msg = "Authorization Fail, SID or Progkey fail";
            break;
        case 201:
            msg = "AUTH Input Data FAIL";
            break;
        case 214:
        	msg = "File is existed";
        	break;
        case 300:
            msg = "Stream Exception";
            break;
        case 301:
            msg = "Xml Stream Exception";
            break;
        case 310:
            msg = "Required Field Validator Exception";
            break;
        case 311:
            msg = "Field Format Exception";
            break;       
        case 404:
            msg = "Schema Not Found";
            break;
        case 405:
            msg = "Action Not Support";
            break;
        case 990:
        	msg = "Time Out";
        	break;
        case 999:
            msg = "A general Erro";
            break;
        default:
            msg = "UnExpected Error";
            break;
		}
		return msg;
	}
	
	/*
	 * 公有雲密碼使用md5加密(不可逆)
	 */
    private String HashPassword(String sPassword){
    	if (sPassword == null)
    		return null;
    	
        MessageDigest md = null;
        sPassword = sPassword.toLowerCase();
        try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
        byte[] md5hash = new byte[32];
        try {
			md.update(sPassword.getBytes("utf-8"), 0, sPassword.length());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        md5hash = md.digest();
        return convertToHex(md5hash);
    	
    }
    
    /*
     * 私有雲密碼使用3des加密(可逆)
     */
    private String tripleDesPassword(String sPassword) {
    	DESedeEncoder tDes = new DESedeEncoder();
    	String encryptStr = tDes.encryptThreeDESECB(sPassword, this.progKey);
    	
    	return encryptStr;
    }
    
	private static String convertToHex(byte[] data) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9))
                    buf.append((char) ('0' + halfbyte));
                else
                    buf.append((char) ('a' + (halfbyte - 10)));
                halfbyte = data[i] & 0x0F;
            } while(two_halfs++ < 1);
        }
        return buf.toString();
    }

	/*
	 * 使用者帳號密碼身份認證，取得token
	 */
	private Boolean acquireToken() throws ACSException, TimeoutException{
		if (this.serviceGatewayURL == null)	{
			requestGateway();
			if (this.serviceGatewayURL == null)
				throw new ACSException("Acquire Token Failed, service gateway is null.");
		}
		String API = "/member/acquiretoken/";
		String acquireTokenXML = "<aaa><userid>" + this.UserName + "</userid><password>" + this.Password + "</password><time>2008/1/1</time></aaa>";
		//String acquireTokenXML = "<aaa><userid>" + this.UserName + "</userid><symmetrypwd>" + this.Password + "</symmetrypwd><time>2008/1/1</time></aaa>";
		
		String params[] = new String[2];
	    params[0] = "https://"+this.serviceGatewayURL + API;
	    params[1] = acquireTokenXML;

	    String responseXML = sendRequest(params);
	    
		if (responseXML == null) {
			return false;
		}
		
		Document dom = becomeDom(responseXML);
		String status = dom.getElementsByTagName("status").item(0).getTextContent();
		if (!status.equals("0")) {
			throw new ACSException("Acquire Token Failed, status = " + status + ", msg = " +getErrorMsg(Integer.valueOf(status)));
		}
		this.token = dom.getElementsByTagName("token").item(0).getTextContent();
		this.tsdBaseURL = dom.getElementsByTagName("tsdbase").item(0).getTextContent();
		this.infoRelayURL = dom.getElementsByTagName("inforelay").item(0).getTextContent();
		this.webRelayURL = dom.getElementsByTagName("webrelay").item(0).getTextContent();
		this.searchServer = dom.getElementsByTagName("searchserver").item(0).getTextContent();
		
		return true;
	}

	
	/*
	 * 取得使用者帳號的服務區
	 * 根據使用者帳號所在地區(台灣、大陸、美國...)，取得不同的service gateway位置
	 */
 	private boolean requestGateway() throws ACSException, TimeoutException {
 		String API = "/member/requestservicegateway/";
		String xmlRequestGateway = "<requestservicegateway><userid>" + this.UserName + "</userid><password>" + this.Password + "</password><language></language><service>1</service>></requestservicegateway>";
		String params[] = new String[2];
	    params[0] = this.servicePortalURL + API;
	    params[1] = xmlRequestGateway;
	    
	    String responseXML = sendRequest(params);
	    Document dom = becomeDom(responseXML);
	    
	    /*get servicegateway element start*/
		Element el = (Element)(dom.getElementsByTagName("servicegateway").item(0));
		this.serviceGatewayURL = el.getTextContent();
		/*get servicegateway element end*/
		
		return true;
	}
 	
 	/* Author by Melissa
 	 * 
 	 * Add a sendRequest method to module code. 
 	 */
 	private String sendRequest(String[] array) throws ACSException, TimeoutException {
 		String params[] = new String[5];
 		params[0] = array[0];
 		params[1] = array[1];
	    params[2] = this.SID;
	    params[3] = this.progKey;
	    params[4] = this.token;
	    
 		String responseXML = null;
 		FutureTask <String> postTask = new FutureTask<String>(new postDataThread(params[0], params[1],params[2],params[3], params[4]) );
	    ExecutorService es = Executors.newSingleThreadExecutor();
	    es.submit(postTask);		
	    
	    try {
	    	responseXML = postTask.get(180000, TimeUnit.MILLISECONDS);
	    } catch (TimeoutException e) {
	    	throw e;
	    } catch (Exception e) {
	    	throw new ACSException("Exception occurs while post payload.");
	    }
		return responseXML;
 	}
 	
 	/*
 	 * Parse XML string to XML object
 	 */
	protected static Document becomeDom(String sourceXML) throws ACSException {
		try {
			InputStream is = new ByteArrayInputStream(sourceXML.getBytes("UTF-8"));
			DocumentBuilder builder;
			
			DocumentBuilderFactory  factory = DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			Document dom = builder.parse(is);
			return dom;
		} catch (ParserConfigurationException e) {
			throw new ACSException("Parse XML string error, ParserConfigurationException");
		} catch (SAXException e) {
			throw new ACSException("Parse XML string error, SAXException");
		} catch (IOException e) {
			throw new ACSException("Parse XML string error, IOException");
		}
	}
	
	/*
	 * 取得Scrip的值
	 * 有些API會使用到，Scrip = 現在的timestamp值
	 */
 	private String getScrip(){
 		return String.valueOf(System.currentTimeMillis());
 	}
	
	
}
