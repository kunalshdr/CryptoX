package com.example.cryptox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnSwipeListener;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;
import com.example.cryptox.asynctasks.DownloadFileAsyncTask;
import com.example.cryptox.asynctasks.GetFileListAsyncTask;
import com.example.cryptox.asynctasks.UploadFileAsyncTask;
import com.example.cryptox.fragments.LogInFragment;
import com.example.cryptox.models.CryptoXFile;
import com.example.cryptox.models.User;
import com.example.cryptox.utils.DateComparatorAscUtil;
import com.example.cryptox.utils.DateComparatorDescUtil;
import com.example.cryptox.utils.NameComparatorAscUtil;
import com.example.cryptox.utils.NameComparatorDescUtil;
import com.example.cryptox.utils.SizeComparatorAscUtil;
import com.example.cryptox.utils.SizeComparatorDescUtil;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterViewFlipper;
import android.widget.ListView;
import android.widget.Toast;

public class FileListActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_files);

		filesListView = (SwipeMenuListView) findViewById(R.id.listViewFiles);
		filesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{

				// FileDetailsFragment fileDetailsFragment =
				// (FileDetailsFragment)
				// getFragmentManager().findFragmentById(R.id.fragment1);
				// fileDetailsFragment.displayCryptoXFileDetail(cryptoXFilesCopy.get(position));
			}

		});

		// using the MaterialDesign library for floating add button
		// https://github.com/navasmdc/MaterialDesignLibrary
		findViewById(R.id.buttonUploadFiles).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v)
			{
				// using the file picker library
				// https://github.com/iPaulPro/aFileChooser
				Intent getContentIntent = FileUtils.createGetContentIntent();
				Intent intent = Intent.createChooser(getContentIntent, "Select a file");
				startActivityForResult(intent, REQ_CODE_GET_FILE);
			}
		});

	}

	@Override
	protected void onResume()
	{
		super.onResume();

		if (mDBApi != null && mDBApi.getSession().authenticationSuccessful())
		{
			try
			{
				mDBApi.getSession().finishAuthentication();
				accessToken = mDBApi.getSession().getOAuth2AccessToken();
				uploadFile();
			}
			catch (IllegalStateException e)
			{
				Log.i("DbAuthLog", "Error authenticating", e);
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == REQ_CODE_GET_FILE)
		{
			if (resultCode == RESULT_OK)
			{
				Uri uri = data.getData();

				// using the file picker library
				// https://github.com/iPaulPro/aFileChooser
				filePath = FileUtils.getPath(this, uri);
				if (filePath != null && FileUtils.isLocal(filePath))
				{
					if (TextUtils.isEmpty(filePath))
					{
						Toast.makeText(FileListActivity.this, "Please select a file", Toast.LENGTH_SHORT).show();
						return;
					}
					authenticateDropbox();
				}
			}
			else
			{
				Log.d("OnActivityResult file chooser::", "Error while getting file");
			}
		}
	}

	protected void authenticateDropbox()
	{
		AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
		AndroidAuthSession session = new AndroidAuthSession(appKeys);
		mDBApi = new DropboxAPI<AndroidAuthSession>(session);
		// TODO (kshridha): Only one authentication per session
		if (!mDBApi.getSession().isLinked())
			mDBApi.getSession().startOAuth2Authentication(FileListActivity.this);
		else
			uploadFile();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.file_details, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.sortByNameAscending)
		{
			adapter.clear();
			Collections.sort(cachedFileList, new NameComparatorAscUtil());
			adapter.addAll(cachedFileList);
			adapter.notifyDataSetChanged();
		}
		else if (id == R.id.sortByNameDescending)
		{
			adapter.clear();
			Collections.sort(cachedFileList, new NameComparatorDescUtil());
			adapter.addAll(cachedFileList);
			adapter.notifyDataSetChanged();
		}
		else if (id == R.id.sortByDateModifiedAscending)
		{
			adapter.clear();
			Collections.sort(cachedFileList, new DateComparatorAscUtil());
			adapter.addAll(cachedFileList);
			adapter.notifyDataSetChanged();

		}
		else if (id == R.id.sortByDateModifiedDescending)
		{
			adapter.clear();
			Collections.sort(cachedFileList, new DateComparatorDescUtil());
			adapter.addAll(cachedFileList);
			adapter.notifyDataSetChanged();
		}
		else if (id == R.id.sortBySizeAscending)
		{
			adapter.clear();
			Collections.sort(cachedFileList, new SizeComparatorAscUtil());
			adapter.addAll(cachedFileList);
			adapter.notifyDataSetChanged();
		}
		else if (id == R.id.sortBySizeDescending)
		{
			adapter.clear();
			Collections.sort(cachedFileList, new SizeComparatorDescUtil());
			adapter.addAll(cachedFileList);
			adapter.notifyDataSetChanged();
		}
		else if (id == R.id.original_lst)
		{
			adapter.clear();
			adapter.addAll(cryptoXFilesCopy);
			adapter.notifyDataSetChanged();
		}
		else if (id == R.id.logout)
		{
			if (ParseUser.getCurrentUser() != null)
			{
				ParseUser.logOut();
				finish();
			}
		}
		return super.onOptionsItemSelected(item);
	}

	private void uploadFile()
	{
		new UploadFileAsyncTask(FileListActivity.this).execute(filePath);
		new GetFileListAsyncTask(this).execute();
	}

	public void setListViewAdapter(ArrayList<CryptoXFile> result)
	{
		cryptoXFiles = result;

		// copy crytpoX in two places
		for (int i = 0; i < cryptoXFiles.size(); i++)
		{
			cachedFileList.add(cryptoXFiles.get(i));
			cryptoXFilesCopy.add(cryptoXFiles.get(i));

		}

		adapter = new FileAdapter(this, R.layout.files_layout, result);
		filesListView.setAdapter(adapter);
		setupListViewSwipe();
	}

	// using the SwipeMenuListView library
	// https://github.com/baoyongzhang/SwipeMenuListView
	private void setupListViewSwipe()
	{
		SwipeMenuCreator creator = new SwipeMenuCreator() {

			@Override
			public void create(SwipeMenu menu)
			{
				// the favorite icon
				favButton = new SwipeMenuItem(getApplicationContext());
				favButton.setBackground(new ColorDrawable(Color.rgb(0x17, 0x88, 0xCE)));
				favButton.setWidth(150);
				favButton.setIcon(R.drawable.ic_action_not_important);
				menu.addMenuItem(favButton);

				// share button
				SwipeMenuItem shareButton = new SwipeMenuItem(getApplicationContext());
				shareButton.setBackground(new ColorDrawable(Color.rgb(0xD9, 0xA1, 0xCA)));
				shareButton.setWidth(150);
				shareButton.setIcon(R.drawable.ic_action_share);
				menu.addMenuItem(shareButton);

				// download button
				SwipeMenuItem downloadButton = new SwipeMenuItem(getApplicationContext());
				downloadButton.setBackground(new ColorDrawable(Color.rgb(0xAA, 0xBC, 0x1E)));
				downloadButton.setWidth(150);
				downloadButton.setIcon(R.drawable.ic_action_download);
				menu.addMenuItem(downloadButton);
			}
		};
		filesListView.setMenuCreator(creator);

		filesListView.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(int position, SwipeMenu menu, int index)
			{
				CryptoXFile file = cryptoXFilesCopy.get(position);

				switch (index)
				{
				case 0:
					toggleFav(file);
					break;
				case 1:
					shareFile(file);
					break;
				case 2:
					downloadFile(file);
					break;
				}
				return false;
			}

		});

		filesListView.setOnSwipeListener(new OnSwipeListener() {

			@Override
			public void onSwipeStart(int position)
			{
				Log.d("SwipeTest::", "Swipe Open working");
				CryptoXFile file = cryptoXFilesCopy.get(position);
				if (file.isFavorite())
				{
					favButton.setIcon(R.drawable.ic_action_important);
				}
			}

			@Override
			public void onSwipeEnd(int position)
			{
				Log.d("SwipeTest::", "Swipe Close working");
			}
		});
	}

	protected void toggleFav(CryptoXFile file)
	{
		if (file.isFavorite())
		{
			file.setFavorite(false);
			Toast.makeText(getApplicationContext(), "File removed from favorites", Toast.LENGTH_SHORT).show();
			// TODO (kshridha): Add favorites columns in parse, then implement
			// fav functionality
		}
		else
		{
			file.setFavorite(true);
			Toast.makeText(getApplicationContext(), "File added to favorites", Toast.LENGTH_SHORT).show();
			// TODO (kshridha): Add favorites columns in parse, then implement
			// fav functionality
		}
	}

	protected void shareFile(CryptoXFile file)
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
					AlertDialog.Builder userListAlert = new AlertDialog.Builder(FileListActivity.this);
					userListAlert.setTitle("Share with").setIcon(R.drawable.ic_action_share);
					userListAlert.setItems(userNames, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							String[] userNameSplit = userNameList.get(which).trim().split(" ");
							String fName = userNameSplit[0];
							String lName = userNameSplit[1];
							// TODO (kshridha): Add code to share with
							// other users

							// TODO (kshridha): Clear dialog before loading..
							// entried appending each time dialog is opnend
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

	protected void downloadFile(CryptoXFile cryptoXFile)
	{
		String path = cryptoXFile.getPath() + cryptoXFile.getName();
		new DownloadFileAsyncTask(FileListActivity.this).execute(path);

	}

	ArrayList<CryptoXFile> cryptoXFiles;

	// copy of cryptoXFiles for sorting functions
	ArrayList<CryptoXFile> cachedFileList = new ArrayList<CryptoXFile>();

	// copy of cryptoXFiles in case original list is cleared
	ArrayList<CryptoXFile> cryptoXFilesCopy = new ArrayList<CryptoXFile>();

	SwipeMenuListView filesListView;
	String filePath;
	FileAdapter adapter;
	ArrayList<User> userList;
	ArrayList<String> userNameList;
	SwipeMenuItem favButton;

	// request codes for startActivityForResult
	protected static final int REQ_CODE_GET_FILE = 1002;

	// dropbox keys and variables
	final static private String APP_KEY = "6knyn43b3wga8ri";
	final static private String APP_SECRET = "zt9p544duz26e51";
	public static DropboxAPI<AndroidAuthSession> mDBApi;
	String accessToken;

}
