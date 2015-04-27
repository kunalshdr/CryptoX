package com.example.cryptox.fragments;

import com.example.cryptox.R;
import com.example.cryptox.R.id;
import com.example.cryptox.R.layout;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

public class LogInFragment extends Fragment
{
	public interface SignUpHandler
	{
		void loadSignUpScreen();

		void loadFileListScreen();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.fragment_login, container, false);
		return rootView;
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		try
		{
			signUp = (SignUpHandler) activity;
		}
		catch (ClassCastException e)
		{
			Log.d("LogInFragment::", e.getMessage());
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		ParseUser user = ParseUser.getCurrentUser();
		if (user == null)
		{

			getActivity().findViewById(R.id.buttonCreateNewAccount).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v)
				{
					signUp.loadSignUpScreen();

				}

			});

			getActivity().findViewById(R.id.buttonLogin).setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v)
				{
					if (!isConnected())
					{
						Toast.makeText(getActivity(), "No Internet connection", Toast.LENGTH_SHORT).show();
						return;
					}

					emailEditText = (EditText) getActivity().findViewById(R.id.editTextEmail);
					String email = emailEditText.getText().toString();
					if (TextUtils.isEmpty(email))
					{
						Toast.makeText(getActivity(), "Enter e-mail address", Toast.LENGTH_SHORT).show();
						return;
					}

					passwordEditText = (EditText) getActivity().findViewById(R.id.editTextPassword);
					String password = passwordEditText.getText().toString();
					if (TextUtils.isEmpty(password))
					{
						Toast.makeText(getActivity(), "Enter password", Toast.LENGTH_SHORT).show();
						return;
					}

					ParseUser.logInInBackground(email, password, new LogInCallback() {

						@Override
						public void done(ParseUser user, ParseException e)
						{
							if (e == null)
							{
								signUp.loadFileListScreen();
							}
							else
							{
								Toast toast = Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT);
								toast.show();
							}

						}
					});

				}
			});
		}
		else
		{
			signUp.loadFileListScreen();
		}
	}

	protected boolean isConnected()
	{
		ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo no = cm.getActiveNetworkInfo();
		if (no != null && no.isConnected())
			return true;
		return false;
	}

	SignUpHandler signUp;
	EditText emailEditText, passwordEditText;
}