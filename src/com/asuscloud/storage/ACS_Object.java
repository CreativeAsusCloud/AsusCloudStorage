package com.asuscloud.storage;
/**
 * 
 */


/**
 * @author Mirage Lin
 *
 */
public class ACS_Object {
    private String objID;
    private String parentID;
    private String objName;
    private boolean isGroupAware;
    private int fileSize;
    private int foldertreeSize;
    private boolean isBackup;
    private boolean isInfected;
    private boolean isShared;
    private String headVersion;
    private String folderOwner;
    private String objCreatedTime;
    private boolean isFileType;
	/**
	 * @return the objID
	 */
	public String getObjID() {
		return objID;
	}
	/**
	 * @param objID the objID to set
	 */
	public void setObjID(String objID) {
		this.objID = objID;
	}
	/**
	 * @return the parentID
	 */
	public String getParentID() {
		return parentID;
	}
	/**
	 * @param parentID the parentID to set
	 */
	public void setParentID(String parentID) {
		this.parentID = parentID;
	}
	/**
	 * @return the objName
	 */
	public String getObjName() {
		return objName;
	}
	/**
	 * @param objName the objName to set
	 */
	public void setObjName(String objName) {
		this.objName = objName;
	}
	/**
	 * @return the isGroupAware
	 */
	public boolean getIsGroupAware() {
		return isGroupAware;
	}
	/**
	 * @param isGroupAware the isGroupAware to set
	 */
	public void setIsGroupAware(boolean isGroupAware) {
		this.isGroupAware = isGroupAware;
	}
	/**
	 * @return the fileSize
	 */
	public int getFileSize() {
		return fileSize;
	}
	/**
	 * @param fileSize the fileSize to set
	 */
	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}
	/**
	 * @return the treeSize
	 */
	public int getFolderTreeSize() {
		return foldertreeSize;
	}
	/**
	 * @param treeSize the treeSize to set
	 */
	public void setFolderTreeSize(int treeSize) {
		this.foldertreeSize = treeSize;
	}
	/**
	 * @return the isBackup
	 */
	public boolean getIsBackup() {
		return isBackup;
	}
	/**
	 * @param isBackup the isBackup to set
	 */
	public void setIsBackup(boolean isBackup) {
		this.isBackup = isBackup;
	}
	/**
	 * @return the isInfected
	 */
	public boolean getIsInfected() {
		return isInfected;
	}
	/**
	 * @param isInfected the isInfected to set
	 */
	public void setIsInfected(boolean isInfected) {
		this.isInfected = isInfected;
	}
	/**
	 * @return the isShared
	 */
	public boolean getIsShared() {
		return isShared;
	}
	/**
	 * @param isShared the isShared to set
	 */
	public void setIsShared(boolean isShared) {
		this.isShared = isShared;
	}
	/**
	 * @return the headVersion
	 */
	public String getHeadVersion() {
		return headVersion;
	}
	/**
	 * @param headVersion the headVersion to set
	 */
	public void setHeadVersion(String headVersion) {
		this.headVersion = headVersion;
	}
	/**
	 * @return the folderOwner
	 */
	public String getFolderOwner() {
		return folderOwner;
	}
	/**
	 * @param folderOwner the folderOwner to set
	 */
	public void setFolderOwner(String folderOwner) {
		this.folderOwner = folderOwner;
	}
	/**
	 * @return the objCreatedTime
	 */
	public String getObjCreatedTime() {
		return objCreatedTime;
	}
	/**
	 * @param objCreatedTime the objCreatedTime to set
	 */
	public void setObjCreatedTime(String objCreatedTime) {
		this.objCreatedTime = objCreatedTime;
	}
	/**
	 * @return the isFileType
	 */
	public boolean getIsFileType() {
		return isFileType;
	}
	/**
	 * @param isFileType the isFileType to set
	 */
	public void setIsFileType(boolean isFileType) {
		this.isFileType = isFileType;
	}



}
