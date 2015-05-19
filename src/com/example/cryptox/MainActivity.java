package com.example.cryptox;

import com.example.cryptox.fragments.LogInFragment;
import com.example.cryptox.fragments.LogInFragment.SignUpHandler;
import com.example.cryptox.fragments.SignUpFragment;
import com.example.cryptox.fragments.SignUpFragment.LoadFilesScreenFromSignUp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity implements SignUpHandler, LoadFilesScreenFromSignUp
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null)
		{
			getFragmentManager().beginTransaction().addToBackStack(null).add(R.id.container, new LogInFragment(), "LogInFragment").commit();
		}
	}

	@Override
	public void loadSignUpScreen()
	{
		getFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.container, new SignUpFragment(), "SignUpFragment").commit();

	}

	@Override
	// from file-list screen login screen
	public void loadFileListScreen()
	{
		Intent intent = new Intent(getBaseContext(), FileListActivity.class);
		startActivity(intent);
	}

	@Override
	// go to file-list screen from sign-up screen
	public void loadFileListActivity()
	{
		Intent intent = new Intent(getBaseContext(), FileListActivity.class);
		startActivity(intent);

	}

	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		// to handle back button click when only login fragment is present
		if (getFragmentManager().getBackStackEntryCount() == 0)
		{
			finish();
			System.exit(0);
		}
	}

}
