package com.example.cryptox;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCrashReporting;
import com.parse.ParseUser;

public class ParseApplication extends Application
{

	@Override
	public void onCreate()
	{
		super.onCreate();

		// crash reporting
		ParseCrashReporting.enable(this);

		// parse intialization
		Parse.initialize(this, "zmhJWuDEZpoNpGnrBG7GRVB0zZXhlpcPs6bzZrQP", "NBj5vcvz2h4CbUdBn5YuQ6aOHX5bQESgk9fVxsBw");
	}
}
