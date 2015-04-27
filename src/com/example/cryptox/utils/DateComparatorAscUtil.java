package com.example.cryptox.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import android.util.Log;

import com.example.cryptox.models.CryptoXFile;

public class DateComparatorAscUtil implements Comparator<CryptoXFile>
{

	@Override
	public int compare(CryptoXFile lhs, CryptoXFile rhs)
	{
		// convert string to date and then use Date class compareTo
		DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
		Date lhsDate = null;
		Date rhsDate = null;
		try
		{
			lhsDate = df.parse(lhs.getDateModified());
			rhsDate = df.parse(rhs.getDateModified());
		}
		catch (ParseException e)
		{
			Log.d("DateParseExcelption::", e.getMessage());
		}
		return lhsDate.compareTo(rhsDate);

	}

}
