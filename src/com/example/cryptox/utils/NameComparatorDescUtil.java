package com.example.cryptox.utils;

import java.util.Comparator;

import com.example.cryptox.models.CryptoXFile;

public class NameComparatorDescUtil implements Comparator<CryptoXFile>
{

	@Override
	public int compare(CryptoXFile lhs, CryptoXFile rhs)
	{
		return -lhs.getName().compareToIgnoreCase(rhs.getName());
	}

}
