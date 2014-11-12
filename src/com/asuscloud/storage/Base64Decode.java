package com.asuscloud.storage;


import java.io.IOException;

//import com.ecareme.api.filerelay.FileBase;
//import com.ecareme.api.filerelay.FolderBase;

//import net.yostore.oeo.bean.FileBean;
//import net.yostore.oeo.bean.FolderBean;

public class Base64Decode
{
	public String Base64Decode(String str) throws IOException{
		return new String(Base64.decodeFast(str), "UTF8");
	}
	
	public String Base64Encode(String str) throws IOException{
		return new String(Base64.encodeToBase64String(str));
	}
}
