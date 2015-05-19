package com.example.cryptox;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseCrashReporting;
import com.parse.ParseUser;

public class ParseApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		// crash reporting
		ParseCrashReporting.enable(this);

		// parse intialization
		Parse.initialize(this, "sySEqSLbaBatO2kDN5SQQ2dIzg10520ZcIgVw2FD",
				"4zh5KLdN6VvmNHu0syoS598lBItha9ejDXNwTZdU");
	}
}
