package com.asuscloud.storage.app;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeoutException;

import com.asuscloud.storage.*;
import com.asuscloud.storage.app.R;

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static String TAG = "AsusCloudStorage";
	private ACS_DocumentManager acs = null;
	
	private Button button1;
	private EditText edit_sid;
	private EditText edit_progkey;
	private EditText edit_uid;
	private EditText edit_pwd;
	private TextView mResult;
	
	/*
	 *  設定連接參數
	 *  SID:創意雲金鑰
	 *  progkey:創意雲金鑰
	 *  username:webstorage ID
	 *  password: webstorage password
	 */
	private String sid="29660478";
	private String progKey="0A6CBBD8584B47F5A0A070E9A16C1BA3";
	private String username="melissa.lin@asuscloud.com";
	private String password="19870418@asus";
	
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		button1 = (Button)findViewById(R.id.button1);
		//設定editText的預設值
		edit_sid = (EditText)findViewById(R.id.sid);
		edit_progkey = (EditText)findViewById(R.id.progkey);
		edit_uid = (EditText)findViewById(R.id.aws_uid);
		edit_pwd = (EditText)findViewById(R.id.aws_pwd);
		
		mResult = (TextView)findViewById(R.id.result);
		
		if (sid != null) {
			edit_sid.setText(sid);
		}
		if (progKey != null) {
			edit_progkey.setText(progKey);
		}
		if (username != null) {
			edit_uid.setText(username);
		}
		if (password != null) {
			edit_pwd.setText(password);
		}
		
		button1.setOnClickListener(new Button.OnClickListener()
		{
			@Override
			public void onClick(View arg0) {
				// 讀取EditText輸入的參數資料
				sid = edit_sid.getText().toString();
				progKey = edit_progkey.getText().toString();
				username = edit_uid.getText().toString();
				password = edit_pwd.getText().toString();
				
				
				/* Connect to the cloud
				 * 連接雲端來取得ACS_DocumentManager物件，來呼叫之後所有的功能
				 * @return : ACS_DocmentManager
				 * @param1 : SID
				 * @param2 : progKey
				 * @param3 : username, webstorage ID.
				 * @param4 : passwrod, webstorage's password.
				 */
				try {
					acs = new ACS_DocumentManager(sid, progKey, username, password);
					
				} catch(ACSException e) {
					mResult.setText("Construct ACS_DocumentManager Failed!!\n\n");
					e.printStackTrace();
					return;
				} catch (TimeoutException e) {
					mResult.setText("Construct ACS_DocumentManager Failed!!\n\n");
					e.printStackTrace();
					return;
				}
				mResult.setText("Construct ACS_DocumentManager successful!!\n\n");
				
				
				/*
				 * 建立新資料夾
				 * @param1: 欲建立的資料夾路徑
				 */
				/*try {
					mResult.append("\n\ncreate folder is "+ acs.createFolder("/mysync/newnewfolder/"));
				} catch (ACSException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					e.printStackTrace();
				}*/
				
				/*
				 * 刪除資料夾
				 * @param1: 欲刪除的資料夾路徑
				 */
				/*try {
					mResult.append("\n\nremove folder is "+ acs.removeFolder("/mysync/newnewfolder/"));
				} catch (ACSException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					e.printStackTrace();
				}*/
				
				/*
				 * 刪除在/mysync 目錄下名為 test.txt 的文字型態檔案
				 * @param1 :  file path
				 */
				/*try {
					mResult.append("\nremove file is " + acs.removeFile("/mysync/5.mp4"));
				} catch (ACSException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					e.printStackTrace();
				}*/
				

				//取得雲端上指定的目錄下的檔案及目錄
				//參數1: 指定目錄路徑, 在雲端上每一位使用者的根目錄是 "/mysync", 所以指定路徑時一律是/mysync開頭
				//參數2: ACS_LIST_OPTION.ALL 指定列出搜尋目錄下 所有檔案及目錄
				//      ACS_LIST_OPTION.FILE_ONLY 指定列出搜尋目錄下 所有檔案
				//      ACS_LIST_OPTION.DIRECTORY_ONLY 指定列出搜尋目錄下 所有目錄
				//      呼叫成功: 傳回 ACSObject 陣列，內含目錄內的所有物件, 呼叫失敗則傳回null
				/*try {
					ACS_Object[] acsobj = acs.browseFolder("/mysync/newnewfolder/", ACS_LIST_OPTION.ALL);	
					if (acsobj == null) {
						mResult.append("browse result is null");
						return;
					}
					mResult.append("browse result is :\n");
					for (int i =0 ; i< acsobj.length; i++) {
						mResult.append("\n"+acsobj[i].getObjName());
					}
				} catch (ACSException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					e.printStackTrace();
				}*/

				/*
				 * 讀取檔案
				 * @param1: 欲讀取的檔案路徑
				 * @return: 可選擇byte[] or String回傳
				 */
				/*try {
					mResult.append("\n\nread file to string is : "+ (acs.readTextFile("/mysync/test.txt")));
					byte[] bytes = acs.read("/mysync/abc.txt");
					mResult.append("\n\nread file to byte is : "+ new String(bytes,"UTF-8"));
				} catch (ACSException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					e.printStackTrace();
				} catch (UnsupportedEncodingException e){
					e.printStackTrace();
				}*/
				
				
				//讀取本地磁碟的文字檔案存到String text內
				/*AssetManager assetManager = getAssets();					
				InputStream input; 
				String text="";
				try { 
					input = assetManager.open("test.txt"); 
				    int size = input.available(); 
				    byte[] buffer = new byte[size]; 
				    input.read(buffer); 
				    input.close(); 
				    text = new String(buffer,"UTF-8"); 
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}*/
				
				//讀取一個本地的影像檔案存至InputStream pic內
				/*InputStream pic;
				try { 
					pic = assetManager.open("ASUS.png"); 
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}*/
				
				//將 二進位資料 或 文字資料寫入雲端的目錄中
				//參數1: 如果是二進位資料請用 InputStream的型態傳入，如果是文字資料請用String型態傳入
				//參數2: 寫入遠端的完整路徑與檔案名稱
				//參數3: 若檔案名稱已經存在，是否要將其複寫
				//成功傳回 true, 失敗傳回false
				/*try {
					mResult.append("\n\nupload stream file is " + acs.write(pic, "/mysync/asus.png", true));
					mResult.append("\n\nupload string file is " + acs.write(text, "/mysync/test.txt", true));
				} catch (ACSException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					e.printStackTrace();
				} */
				
				/*
				 * 分享檔案及資料夾
				 * @param1: file or folder name
				 * @param2: password to protect the shared file.
				 */
				/*try {
					mResult.append("\n\nshare folder is " + acs.shareFolder("/mysync/newnewfolder/", "123456"));
					mResult.append("\n\nshare file is " + acs.shareFile("/mysync/asus.png", "123456"));
				} catch (ACSException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					e.printStackTrace();
				} */

				/*
				 * 停止分享資料夾
				 * @param1: folder or file name.
				 */
				/*try {
					mResult.append("\n\nstop share folder is " + acs.stopSharefolder("/mysync/newnewfolder/"));
					mResult.append("\n\nstop share file is " + acs.stopSharefile("/mysync/asus.png"));
				} catch (ACSException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					e.printStackTrace();
				}*/
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
