package com.asuscloud.storage;


public enum ACS_LIST_OPTION{
    ALL (0x0),
    FILE_ONLY (0x1),
    DIRECTORY_ONLY ( 0x10);	
    
    private int value = 0;

	private ACS_LIST_OPTION (int value)
    {
    	this.value = value;
    }
	
	public int value()
	{
		return this.value;
	}
}
