package com.example.cryptox.fragments;

import com.example.cryptox.R;
import com.example.cryptox.R.id;
import com.example.cryptox.R.layout;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * 
 */
public class SignUpFragment extends Fragment
{

	EditText passwd;
	EditText passwdConfirm;
	EditText email;
	EditText name;

	public SignUpFragment()
	{
		// Required empty public constructor
	}

	public interface LoadFilesScreenFromSignUp
	{
		void loadFileListActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_sign_up, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		passwd = (EditText) getActivity().findViewById(R.id.editTextPassword);
		passwdConfirm = (EditText) getActivity().findViewById(R.id.editTextPasswordConfirm);
		email = (EditText) getActivity().findViewById(R.id.editTextEmail);
		name = (EditText) getActivity().findViewById(R.id.editTextUserName);

	

		getActivity().findViewById(R.id.buttonSignup).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v)
			{
				if (!isConnected())
				{
					Toast.makeText(getActivity(), "No Internet connection", Toast.LENGTH_SHORT).show();
					return;
				}

				if (TextUtils.isEmpty(name.getText().toString().trim()))
				{
					Toast.makeText(getActivity(), "Name can't be empty", Toast.LENGTH_SHORT).show();
					return;
				}
				if (TextUtils.isEmpty(email.getText().toString().trim()))
				{
					Toast.makeText(getActivity(), "E-mail can't be empty", Toast.LENGTH_SHORT).show();
					return;
				}
				if (TextUtils.isEmpty(passwd.getText().toString().trim()))
				{
					Toast.makeText(getActivity(), "Password can't be empty", Toast.LENGTH_SHORT).show();
					return;
				}
				if (TextUtils.isEmpty(passwdConfirm.getText().toString().trim()))
				{
					Toast.makeText(getActivity(), "Confirm password can't be empty", Toast.LENGTH_SHORT).show();
					return;
				}

				else if (passwd.getText().toString().equals(passwdConfirm.getText().toString()))
				{
					ParseUser user = new ParseUser();
					user.setUsername(email.getText().toString());
					user.setPassword(passwd.getText().toString());
					user.setEmail(email.getText().toString());
					user.put("name", name.getText().toString());

					user.signUpInBackground(new SignUpCallback() {
						public void done(ParseException e)
						{
							if (e == null)
							{
								f.loadFileListActivity();
							}
							else
							{
								Toast toast = Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT);
								toast.show();
							}
						}
					});
				}
				else
				{
					Toast toast = Toast.makeText(getActivity(), "Passwords do not match", Toast.LENGTH_SHORT);
					toast.show();
				}

			}
		});
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		try
		{
			f = (LoadFilesScreenFromSignUp) activity;
		}
		catch (ClassCastException e)
		{
			Log.d("SignUpFragment::", e.getMessage());
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	protected boolean isConnected()
	{
		ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo no = cm.getActiveNetworkInfo();
		if (no != null && no.isConnected())
			return true;
		return false;
	}

	LoadFilesScreenFromSignUp f;

}
