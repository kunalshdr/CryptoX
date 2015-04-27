package com.example.cryptox.utils;

import java.util.Comparator;

import com.example.cryptox.models.CryptoXFile;

public class SizeComparatorDescUtil implements Comparator<CryptoXFile>
{

	@Override
	public int compare(CryptoXFile lhs, CryptoXFile rhs)
	{
		if (lhs.getSizeBytes() <= rhs.getSizeBytes())
			return 1;
		else
			return -1;
	}

}
