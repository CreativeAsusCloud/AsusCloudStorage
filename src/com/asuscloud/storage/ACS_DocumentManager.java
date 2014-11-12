package com.asuscloud.storage;


import android.util.Base64;
import android.util.Log;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeoutException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Mirage Lin
 *
 */
public class ACS_DocumentManager {
	
    private awsClient awsObj;
    private ACS_ErrorCode lastError;
    private String mySyncFolderID;
    private String myBackFolderID;
    private String myCollectionFolderID;
    private String lastFilename;
    private static int STATUS_OK = 0;
    
    public int getErrorCode()
    {
    	return lastError.value();
    }
	
    /*
     * 建立ACS_DocumentManager 物件
     */
	public ACS_DocumentManager(String SID, String ProgKey, String Username, String Password)throws ACSException, TimeoutException {
		this.awsObj = null;
		this.lastError = ACS_ErrorCode.OK;
		
        if ((SID.length() == 0) || (ProgKey.length() == 0)) {
        	throw new ACSException("SID or PROGKEY is NULL");
        }
        if ((Username.length() == 0) || ( Password.length() ==0 )) {
            throw new ACSException("Username or Password is NULL");
        }
		this.awsObj = new awsClient(SID, ProgKey, Username, Password);
		this.mySyncFolderID = awsObj.getMySyncFolder();
	}
    
	/*
	 * 瀏覽資料夾 Browse Folder
	 * @param1: folder path
	 * @param2: browse的類別，檔案或資料夾或all
	 */
	public ACS_Object[] browseFolder(String Path, ACS_LIST_OPTION Option) throws ACSException, TimeoutException {
		String folderid = getFolderID(Path, true);
		String xmlBrowseFolder;

		xmlBrowseFolder = awsObj.browseFolder(folderid);

		Document dom = awsClient.becomeDom(xmlBrowseFolder);
		
		NodeList files = dom.getElementsByTagName("file");
		NodeList folders = dom.getElementsByTagName("folder");
		int total_rows = 0;
		
		switch (Option.value()){
			case 0:
				//all
				total_rows = files.getLength() + folders.getLength();
				break;
			case 1:
				//fileonly
				total_rows = files.getLength();
				break;
			case 0x10:
				total_rows = folders.getLength();
				break;
			default:
				total_rows = 0;
				break;
		
		}
		
		if (total_rows == 0)
			return null;
		
		ACS_Object[] returnObjs = new ACS_Object[total_rows];
		int index = 0;
		/*
		 * 將回傳的xml轉成ACS_Object 物件
		 * 處理資料夾
		 */
		if (Option == ACS_LIST_OPTION.ALL || Option == ACS_LIST_OPTION.DIRECTORY_ONLY){
			String displayName = null;
			for (int i = 0; i< folders.getLength(); i++) {
				Node tmpNode = folders.item(i);
				Element el = (Element)tmpNode;
				
				ACS_Object obj = new ACS_Object();
				
				String text=null;
				displayName = el.getElementsByTagName("display").item(0).getTextContent();
				byte[] deCodeData = Base64.decode(displayName, Base64.NO_WRAP);
				try {
					text = new String(deCodeData, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					throw new ACSException("browse folder failed, encoding filename(UTF-8) failed.");
				}
				obj.setObjName(text);
				
				obj.setFolderTreeSize( Integer.valueOf(el.getElementsByTagName("treesize").item(0).getTextContent()) );
				obj.setObjID(el.getElementsByTagName("id").item(0).getTextContent());
				obj.setObjCreatedTime(el.getElementsByTagName("createdtime").item(0).getTextContent());
				
				if ( el.getElementsByTagName("ispublic").item(0).getTextContent().equals("1")) {
					obj.setIsShared(true);
				} else {
					obj.setIsShared(false);
				}
				
				if (el.getElementsByTagName("isbackup").item(0).getTextContent().equals("1")) {
					obj.setIsBackup(true);
				} else {
					obj.setIsBackup(false);
				}
				
				if ( el.getElementsByTagName("isgroupaware").item(0).getTextContent().equals("1")) {
					obj.setIsGroupAware(true);
				} else {
					obj.setIsGroupAware(false);
				}
				obj.setIsFileType(false);
				
                returnObjs[index] = obj;
                index++;
			}			
		}
		/*
		 * 將回傳的xml轉成ACS_Object 物件
		 * 處理檔案類型
		 */
		if (Option == ACS_LIST_OPTION.ALL || Option == ACS_LIST_OPTION.FILE_ONLY) {
			String displayName = null;
			for (int i = 0; i< files.getLength(); i++) {
				Node tmpNode = files.item(i);
				Element el = (Element)tmpNode;
				
				ACS_Object obj = new ACS_Object();
				
				String text=null;
				displayName = el.getElementsByTagName("display").item(0).getTextContent();
				byte[] deCodeData = Base64.decode(displayName, Base64.NO_WRAP);
				try {
					text = new String(deCodeData, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					throw new ACSException("browse folder failed, encoding filename(UTF-8) failed.");
				}
				obj.setObjName(text);
				obj.setFileSize( Integer.valueOf(el.getElementsByTagName("size").item(0).getTextContent()) );
				obj.setObjID(el.getElementsByTagName("id").item(0).getTextContent());
				obj.setObjCreatedTime(el.getElementsByTagName("createdtime").item(0).getTextContent());
				
				if (el.getElementsByTagName("ispublic").item(0).getTextContent().equals("1")) {
					obj.setIsShared(true);
				} else {
					obj.setIsShared(false);
				}
				
				if (el.getElementsByTagName("isbackup").item(0).getTextContent().equals("1")) {
					obj.setIsBackup(true);
				} else {
					obj.setIsBackup(false);
				}
				
				if (el.getElementsByTagName("isgroupaware").item(0).getTextContent().equals("1")) {
					obj.setIsGroupAware(true);
				} else {
					obj.setIsGroupAware(false);
				}
				
				obj.setIsFileType(true);
				
                returnObjs[index] = obj;
                index++;
			}	
		}		
		return returnObjs;
	}
	
	/*
	 * 上傳檔案
	 * @param1: InputStream
	 * @param2: filepath
	 * @param3: OverWrite or not.
	 */
    public boolean write(InputStream stream, String cloudFilename, boolean overWrite) throws ACSException, TimeoutException {	
    	String[] allPath = ACS_File.getPathList(cloudFilename, false);
    	String targetFileName = allPath[allPath.length-1];//取得新增的檔案名稱
    	String ParentFolderName = cloudFilename.substring(0, cloudFilename.lastIndexOf(targetFileName));//取得parent folder路徑
    	/*先取得Parent folder's folderID */
    	String ParentFolderID = getFolderID(ParentFolderName, true);

		String xmlResult = awsObj.browseFolder(ParentFolderID);
		String fileID = getFileIDbyName(xmlResult, targetFileName);
    	/*如果不要覆蓋檔案且檔案已經存在，直接return/*/
    	if (!overWrite && fileID != null){
    		throw new ACSException("write file failed, file is existed, and you don't want to overwrite it.");
    	}
        return awsObj.uploadFileFromMemoryStream(stream, targetFileName, ParentFolderID, fileID);

    }
    
	/*
	 * 上傳檔案
	 * @param1: String
	 * @param2: filepath
	 * @param3: OverWrite or not.
	 */
    public boolean write(String data, String cloudFilename, boolean overWrite) throws ACSException, TimeoutException {
    	String[] allPath = ACS_File.getPathList(cloudFilename, false);
    	String targetFileName = allPath[allPath.length-1];//取得新增的檔案名稱
    	String ParentFolderName = cloudFilename.substring(0, cloudFilename.lastIndexOf(targetFileName));//取得parent folder路徑
    	/*先取得Parent folder's folderID */
    	String ParentFolderID = getFolderID(ParentFolderName, true);

		String xmlResult = awsObj.browseFolder(ParentFolderID);
		String fileID = getFileIDbyName(xmlResult, targetFileName);
    	/*如果不要覆蓋檔案且檔案已經存在，直接return/*/
    	if (!overWrite && fileID != null){
    		throw new ACSException("write file failed, file is existed, and you don't want to overwrite it.");
    	}
        return awsObj.uploadFileFromMemoryStream(data, targetFileName, ParentFolderID, fileID);
    }
	
	/*
	 * remove file
	 * @param: the file path
	 */
    public boolean removeFile(String cloudFilename) throws ACSException, TimeoutException {
    	String[] targetFileID = new String[1];
    	targetFileID[0] = getFileID(cloudFilename);
    	if (targetFileID[0] == null || targetFileID[0].equals("")){
    		throw new ACSException("remove file failed, file id is null.");
    	}
        return awsObj.removeFile(targetFileID);
    }
    
    /* 
     * remove folder
     * @param1: the folder path.
     */
    public boolean removeFolder(String cloudFoldername) throws ACSException, TimeoutException {
    	/*確認folder是否存在*/
    	String targetFolderID = getFolderID(cloudFoldername, true);
    	/*若找不到folder就丟exception*/
    	if (targetFolderID == null)
    		throw new ACSException("remove folder failed. Cannot find this folder's ID");
        
        return awsObj.removeFolder(targetFolderID);

    }
    /*
     * createFolder
     * @param1: the folder path.
     */
    public boolean createFolder(String cloudFoldername) throws ACSException, TimeoutException {
    	String[] allPath = ACS_File.getPathList(cloudFoldername, true);
    	String targetFileName = allPath[allPath.length-1];//取得欲建立的目錄名稱
    	String ParentFolderName = cloudFoldername.substring(0, cloudFoldername.lastIndexOf(targetFileName));//取得parent folder路徑
    	/*先取得Parent folder's folderID */
    	String ParentFolderID = getFolderID(ParentFolderName, true);
    	if (ParentFolderID == null)
    		throw new ACSException("create folder failed, cannot create this folder.");
    	/*確認欲建立的folder是否已經存在*/
    	String browseResult = awsObj.browseFolder(ParentFolderID);
    	if (getFolderIDbyName(browseResult, targetFileName) != null) {
    		throw new ACSException("create folder failed, folder is existed.");
    	}
    	/*確認檔案不存在後開始呼叫create API*/ 	
        return awsObj.createFolder(targetFileName, ParentFolderID);
    }

    public String readTextFile(String cloudFilename) throws ACSException, TimeoutException {
	
    	String fileID = getFileID(cloudFilename);
    	if (fileID == null)
    		throw new ACSException("read file failed, file is not exist.");
    	
    	return awsObj.DownloadFileToString(fileID);
    	
    }

    public byte[] read(String cloudFilename) throws ACSException, TimeoutException {
    	String fileID = getFileID(cloudFilename);
    	if (fileID == null)
    		throw new ACSException("read file failed, file is not exist.");
    	byte[] data = null;
    	data = awsObj.DownloadFileToStream(fileID);
    	return data.clone();
    }
    
	
	/*public boolean hasNewJpeg()
	{
		String xmlLatestChange;
		try {
			xmlLatestChange = awsObj.getLatestChangeFiles();
		} catch (ParserConfigurationException e) {
			this.lastError =  ACS_ErrorCode.EXCEPTION_OCCURS;
			return false;
		} catch (SAXException e) {
			this.lastError =  ACS_ErrorCode.EXCEPTION_OCCURS;
			return false;
		} catch (IOException e) {
			this.lastError =  ACS_ErrorCode.EXCEPTION_OCCURS;
			return false;
		}

		
		Document dom;

			try {
				dom = this.becomeDom(xmlLatestChange);
			} catch (ParserConfigurationException e) {
				this.lastError =  ACS_ErrorCode.EXCEPTION_OCCURS;
				return false;
			} catch (SAXException e) {
				this.lastError =  ACS_ErrorCode.EXCEPTION_OCCURS;
				return false;
			} catch (IOException e) {
				this.lastError =  ACS_ErrorCode.EXCEPTION_OCCURS;
				return false;
			}

		
		NodeList files = dom.getElementsByTagName("entry");
		Node fileNode = files.item(0);
		Element el = (Element)fileNode;
		String filename = el.getElementsByTagName("rawfilename").item(0).getTextContent();
		boolean isFileComing = false;
		
		if (!filename.equals(this.lastFilename) )
		{
			this.lastFilename = filename;
			isFileComing = true;
		}
			
		return isFileComing;
	}
	*/
    /*
     * 停止分享folder
     */
	public boolean stopSharefolder(String folderpath) throws ACSException, TimeoutException {
		String folderid = getFolderID(folderpath);
		String response =  awsObj.deletesharecode(folderid, "0");
		Document dom = awsClient.becomeDom(response);
		int status = checkStatus(dom);
		if (status != 0) {
			throw new ACSException("share file failed, status = " + status);
		}
		return true;
	}
	
    /*
     * 停止分享file
     */
	public boolean stopSharefile(String filepath) throws ACSException, TimeoutException {
		String fileid = getFileID(filepath);
		String response =  awsObj.deletesharecode(fileid, "1");
		Document dom = awsClient.becomeDom(response);
		int status = checkStatus(dom);
		if (status != 0) {
			throw new ACSException("share file failed, status = " + status);
		}
		return true;
	}
	
	/*
	 * Author by Melissa
	 * 分享file(set password)
	 */

	public boolean shareFile(String filepath, String password) throws ACSException, TimeoutException {
		String fileid = getFileID(filepath);
		String response = awsObj.setadvancedsharecode(fileid, password, "0");
		Document dom = awsClient.becomeDom(response);
		int status = checkStatus(dom);
		if (status != 0) {
			throw new ACSException("share file failed, status = " + status);
		}
		return true;
	}
	/*
	 * Author by Melissa
	 * 分享folder(set password)
	 */
	public boolean shareFolder(String folderpath, String password) throws ACSException, TimeoutException {
		String folderid = getFolderID(folderpath);
		String response = awsObj.setadvancedsharecode(folderid, password,"1");
		Document dom = awsClient.becomeDom(response);
		int status = checkStatus(dom);
		if (status != 0) {
			throw new ACSException("share file failed, status = " + status);
		}
		return true;
	}
	
	/*
	 * Author by Melissa
	 * 取得folder的分享狀態
	 */
	public String getFolderShareStatus(String folderpath) throws ACSException, TimeoutException {
		String folderid = getFolderID(folderpath);
		return awsObj.getShareStatus(folderid, "1");
	}
	
	/*
	 * Author by Melissa
	 * getMediaMetaData
	 * @return String 
	 */
	/*public String getMediaMetaData(String filepath) throws ACSException, TimeoutException {
        String folderid = getFolderID(filepath, false);
		return awsObj.getMediaMetaData(filepath.substring(filepath.lastIndexOf("/")+1), folderid);
		//return "1";
	}*/

	public String searchKeyword(String keyword, String foldername) throws ACSException, TimeoutException {
		String folderid = getFolderID(foldername, true);
		return awsObj.searchKeyword(keyword, folderid);
	}
	
	public String getPersonalSystemFolder() throws ACSException, TimeoutException {
		return awsObj.getPersonalSystemFolder();
	}

	/*
	 * Author by Melissa
	 * 取得Status code
	 * @returnType integer
	 */
	private int checkStatus(Document dom) {
		int status = Integer.parseInt(dom.getElementsByTagName("status").item(0).getTextContent());
		return status;
	}
	
	/*
	 * Author by Melissa
	 * 先取得folder ID 最後再用folder id取得file id
	 */
	private String getFileID(String filepath) throws ACSException {
		String[] allPath = ACS_File.getPathList(filepath, false);
    	String targetFileName = allPath[allPath.length-1];//取得欲建立的目錄名稱
    	String ParentFolderName = filepath.substring(0, filepath.lastIndexOf(targetFileName));//取得parent folder路徑
    	/*先取得Parent folder's folderID */
    	String ParentFolderID = getFolderID(ParentFolderName, true);
		String xmlResult = null;
		
		try {
			xmlResult = awsObj.browseFolder(ParentFolderID);
		} catch (Exception e) {
			this.lastError=ACS_ErrorCode.EXCEPTION_OCCURS;
		}
		return getFileIDbyName(xmlResult, targetFileName);
		/*Document dom = awsClient.becomeDom(xmlResult);
		int status = checkStatus(dom);
		if (status == STATUS_OK){
    		NodeList files = dom.getElementsByTagName("file");
    		for (int j = 0; j< files.getLength();j++) {
    			Node tmpNode = files.item(j);
				Element el = (Element)tmpNode;
				if (FilenameDecode(el.getElementsByTagName("display").item(0).getTextContent()).equals(targetFileName)){
					fileID = el.getElementsByTagName("id").item(0).getTextContent();
					return fileID;
				}
    		}
		}
		return null;*/
	}
	
	/*
	 * Author by Melissa
	 * 透過傳入folder名稱，可直接獲得folder ID
	 */
	private String getFolderID(String folderpath) throws ACSException {
		return getFolderID(folderpath, true);
	}
	
	/*
	 * Author by Melissa
	 * 透過傳入folder or file 名稱，可直接獲得folder ID
	 * isFolder 代表此path是folder is true means the "folderpath" end with folder
	 * 約有下列三種情況:
	 * 1. 傳入file path, 要取得file id. 使用getFileID
	 * 2. 傳入file path, 要取得folder id, 使用getFolderID & isFolder = false
	 * 3. 傳入folder  path, 要取得folder id, 使用getFolderID
	 */
	private String getFolderID(String folderpath, boolean isFolder) throws ACSException {
		String pathList[] = ACS_File.getPathList(folderpath, isFolder);
		String lastID = null;
		//如果只有mysync，就直接回傳mysync folder ID
		if (pathList[1].equals("mysync") && pathList.length == 2) {
			return this.mySyncFolderID;
		}
		String xmlResult = null;

		//只browse到最後要查詢folder ID的前一個folder
		for (int i = 0; i< pathList.length-1; i++) {
		    if (pathList[i].equals("mysync")) {
		    	lastID = this.mySyncFolderID;
		    }
		    if (lastID != null) {
		    	try {
		    	    xmlResult = awsObj.browseFolder(lastID);
		    	} catch (Exception e) {
		    		this.lastError=ACS_ErrorCode.EXCEPTION_OCCURS;
		    	}
		    	lastID = getFolderIDbyName(xmlResult, pathList[i+1]);
		    }
		}
		return lastID;
	}
	/*
	 * Author by Melissa
	 * 將經Base64加密過的檔名，解密成string回傳
	 */
	private String FilenameDecode(String displayName) {
		String text = null;
		byte[] deCodeData = Base64.decode(displayName, Base64.NO_WRAP);
		try {
			text = new String(deCodeData, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			this.lastError = ACS_ErrorCode.EXCEPTION_OCCURS;
			return null;
		}
		return text;
	}
	
	private String getFolderIDbyName(String xml, String Name) throws ACSException {
		Document dom = awsClient.becomeDom(xml);
    	int status = checkStatus(dom);
    	if (status == STATUS_OK){
    		NodeList folders = dom.getElementsByTagName("folder");
    		for (int j = 0; j< folders.getLength();j++) {
    			Node tmpNode = folders.item(j);
				Element el = (Element)tmpNode;
				if (FilenameDecode(el.getElementsByTagName("display").item(0).getTextContent()).equals(Name)){
					return el.getElementsByTagName("id").item(0).getTextContent();
				}
    		}
		}
    	Log.e("melissa","沒找到對應的ID");
    	return null;
	}
	
	private String getFileIDbyName(String xmlResult, String Name) throws ACSException {
		Document dom = awsClient.becomeDom(xmlResult);
		int status = checkStatus(dom);
		if (status == STATUS_OK){
    		NodeList files = dom.getElementsByTagName("file");
    		for (int j = 0; j< files.getLength();j++) {
    			Node tmpNode = files.item(j);
				Element el = (Element)tmpNode;
				if (FilenameDecode(el.getElementsByTagName("display").item(0).getTextContent()).equals(Name)){
					return el.getElementsByTagName("id").item(0).getTextContent();
				}
    		}
		}
		return null;
	}
	
}
