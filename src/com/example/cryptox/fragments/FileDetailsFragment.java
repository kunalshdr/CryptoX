package com.example.cryptox.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.cryptox.FileListActivity;
import com.example.cryptox.R;
import com.example.cryptox.R.drawable;
import com.example.cryptox.R.id;
import com.example.cryptox.R.layout;
import com.example.cryptox.asynctasks.DownloadFileAsyncTask;
import com.example.cryptox.models.CryptoXFile;
import com.example.cryptox.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * 
 */
public class FileDetailsFragment extends Fragment
{

	ArrayList<User> userList;
	ArrayList<String> userNameList;
	ImageView favImageView;

	public FileDetailsFragment()
	{
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_file_details, container, false);
	}

	public void displayCryptoXFileDetail(final CryptoXFile cryptoXFile)
	{
		// View v = getActivity().findViewById(R.id.fragment1);
		View v = null;
		TextView fileTitleTextView = (TextView) v.findViewById(R.id.textViewFileTitle);
		fileTitleTextView.setText(cryptoXFile.getName());

		TextView fileTypeTextView = (TextView) v.findViewById(R.id.textViewFileType);
		fileTypeTextView.setText(cryptoXFile.getType());

		// make the view visible on list view row touch
		v.findViewById(R.id.linearLayoutButtons).setVisibility(View.VISIBLE);
		v.findViewById(R.id.textViewUserInfo).setVisibility(View.VISIBLE);

		favImageView = (ImageView) v.findViewById(R.id.imageViewFavorite);
		favImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v)
			{
				if (favImageView.getTag().toString().equalsIgnoreCase("Favorite"))
				{
					favImageView.setTag("NotFavorite");
					favImageView.setImageResource(R.drawable.ic_action_not_important);

					Toast.makeText(getActivity(), "Removed from favorites", Toast.LENGTH_SHORT).show();
				}
				else
				{
					favImageView.setTag("Favorite");
					favImageView.setImageResource(R.drawable.ic_action_important);

					Toast.makeText(getActivity(), "Added to favorites", Toast.LENGTH_SHORT).show();

				}
			}
		});

		ImageView sharedImageView = (ImageView) v.findViewById(R.id.imageViewShared);
		sharedImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v)
			{
				userList = new ArrayList<User>();
				userNameList = new ArrayList<String>();

				ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
				query.findInBackground(new FindCallback<ParseObject>() {

					@Override
					public void done(List<ParseObject> users, ParseException e)
					{
						if (e == null)
						{
							for (int i = 0; i < users.size(); i++)
							{
								User user = new User();
								user.setName(users.get(i).getString("name"));
								userList.add(user);
							}
							Collections.sort(userList);

							for (int i = 0; i < userList.size(); i++)
							{
								userNameList.add(userList.get(i).getName());
							}
							CharSequence[] userNames = new CharSequence[userNameList.size()];
							for (int i = 0; i < userNameList.size(); i++)
							{
								userNames[i] = userNameList.get(i);
							}
							AlertDialog.Builder userListAlert = new AlertDialog.Builder(getActivity());
							userListAlert.setTitle("Share with").setIcon(R.drawable.ic_action_share);
							userListAlert.setItems(userNames, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which)
								{
									String[] userNameSplit = userNameList.get(which).trim().split(" ");
									String fName = userNameSplit[0];
									String lName = userNameSplit[1];

								}
							});
							userListAlert.create().show();
						}
						else
						{
							Log.d("UsersListLoad:::", e.getMessage());
						}
					}

				});

			}
		});

		ImageView downloadImageView = (ImageView) v.findViewById(R.id.imageViewDownload);
		downloadImageView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v)
			{
				String path = cryptoXFile.getPath() + cryptoXFile.getName();
				new DownloadFileAsyncTask((FileListActivity) getActivity()).execute(cryptoXFile);

			}
		});

	}

}
