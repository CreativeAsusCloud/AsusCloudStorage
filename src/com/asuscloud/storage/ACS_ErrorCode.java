package com.asuscloud.storage;
/**
 * 
 */


/**
 * @author Mirage Lin
 *
 */
public enum ACS_ErrorCode{
    EXCEPTION_OCCURS (-1),
    OK (0),
    USER_AUTHENTICATION_ERROR (2),
    SID_OR_PROGKEY_AUTHENTICATE_ERROR (0x00000101),
    OTP_AUTHENTICATE_ERROR (0x000001F8),
    OTP_CREDENTIAL_ID_LOCKED (0x000001F9),
    CAPTCHA_AUTHENTICATE_ERROR (0x000001FC),
    SID_PROGKEY_IS_NULL (0x00010001),
    USERNAME_PASSWORD_IS_NULL (0x00010002),
    AUTHENTICATION_FAIL (0x00001000),
    UNEXPECTED_ERROR (0x00008000),
    ACQUIRE_TOKEN_FAIL (0x00001001),
    POST_DATA_TO_SERVER_FAIL (0x00002000),
    FILE_IS_EXIST (0x000003FF),
    FILE_NAME_IS_EMPTY (0x000000D3),
    FILE_NAME_TOO_LONG (0x000000D5),
    FILE_NAME_IS_EXIST (0x000000D6),
    FOLDER_NOT_EXIST_OR_DELETED (0x000000DA),
    FILE_NOT_EXIST_OR_DELETED (0x000000DB),
    GENERAL_FILE_ERROR (0x000000DC),
    SINGLE_FILE_SIZE_OVER_LIMIT (0x000000DD),
    USER_SPACE_NOT_ENOUGH (0x000000E0),
    USER_ACCOUNT_FREEZE_OR_CLOSE (0x000000E2),
    FILE_SIGNATURE_NOT_MATCH_WITH_CLOUD_RECORD (0x000000FA),
    TRANSACTION_ID_NOT_EXIST (0x0000FB), 
    TRANSACTION_ID_NOT_MATCH_FILE_ID  (0x000000FC);
    
    private int value = 0;

	private ACS_ErrorCode (int value)
    {
    	this.value = value;
    }
	
	public int value()
	{
		return this.value;
	}
	
	public static ACS_ErrorCode valueOf(int value)
	{
		switch (value){
		case -1:
			return EXCEPTION_OCCURS;
			
		case 0:
			return OK;
			
		case 2:
			return USER_AUTHENTICATION_ERROR;
			
		case 0x00000101:
			return SID_OR_PROGKEY_AUTHENTICATE_ERROR;
		
		case 0x000001F8:
			return OTP_AUTHENTICATE_ERROR;
		
		case 0x000001F9:
		    return OTP_CREDENTIAL_ID_LOCKED;
		    
		case 0x000001FC:    
		    return CAPTCHA_AUTHENTICATE_ERROR;
		  
		case 0x00010001:
		    return SID_PROGKEY_IS_NULL;
		    
		case 0x00010002:
		    return USERNAME_PASSWORD_IS_NULL;
		    
		case 0x00001000:
		    return AUTHENTICATION_FAIL;
		
		case 0x00008000:
		    return UNEXPECTED_ERROR;
		
		case 0x00001001:
		    return ACQUIRE_TOKEN_FAIL;
		    
		case 0x00002000:
		    return POST_DATA_TO_SERVER_FAIL;
		    
		case 0x000003FF:
		    return FILE_IS_EXIST;
		
		case 0x000000D3:
		    return FILE_NAME_IS_EMPTY;
		    
		case 0x000000D5:
			return FILE_NAME_TOO_LONG;
			
		case 0x000000D6:
			return FILE_NAME_IS_EXIST;
			
		case 0x000000DA:
		    return FOLDER_NOT_EXIST_OR_DELETED;
		
		case 0x000000DB:
		    return FILE_NOT_EXIST_OR_DELETED;
		    
		case 0x000000DC:
		    return GENERAL_FILE_ERROR;
		    
		case 0x000000DD:
		    return SINGLE_FILE_SIZE_OVER_LIMIT;
		
		case 0x000000E0:
		    return USER_SPACE_NOT_ENOUGH;
		    
		case 0x000000E2:
		    return USER_ACCOUNT_FREEZE_OR_CLOSE;
		    
		case 0x000000FA:
		    return FILE_SIGNATURE_NOT_MATCH_WITH_CLOUD_RECORD;
		    
		case 0x0000FB:
		    return TRANSACTION_ID_NOT_EXIST;
		
		case 0x000000FC:
		    return TRANSACTION_ID_NOT_MATCH_FILE_ID;
		    
		default:
			return EXCEPTION_OCCURS;
		}
		
	}

}


