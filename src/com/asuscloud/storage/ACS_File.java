package com.asuscloud.storage;


public class ACS_File {

	public static String[] getFolderArray(String foldername) {
		String[] allPath = getPathList(foldername);
		
		if (allPath.length < 3) {
        	allPath = getPathList("/mysync/" + foldername);
		}

        String[] paths = new String[allPath.length -1];
        for (int i=0; i < allPath.length-1; i++) {
        	paths[i] = allPath[i];
        }
        return paths;
	}
	
	public static String [] getPathList(String fullPath){
		return getPathList(fullPath, true);
	}
	
    public static String [] getPathList(String fullPath, boolean isFolder) {
    	fullPath = checkFolderName(fullPath, isFolder);
        String[] paths = fullPath.split("/");
        return paths;
    }
    
    
    private static String checkFolderName(String folderpath, boolean isFolder) {
    	if (folderpath != null) {
    		if (!folderpath.startsWith("/")) {
    			folderpath = "/" + folderpath;
    		}
    		if (isFolder && !folderpath.endsWith("/")){
    			folderpath = folderpath + "/";
    		}
    	}
    	return folderpath;
    }
}