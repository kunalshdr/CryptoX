package com.example.cryptox.utils;

public class FileSizeUtil
{

	public static String getfileSize(long fileSizeBytes)
	{
		if (fileSizeBytes <= 1024)
			return fileSizeBytes + " bytes";
		else if (fileSizeBytes > 1024 && fileSizeBytes <=1024*1024)
		{
			return fileSizeBytes/1024 + " KB";
		} else {
			return fileSizeBytes/(1024*1024) + " MB";
		}
	}

}
