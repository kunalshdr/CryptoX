package com.example.cryptox;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Files;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnSwipeListener;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.example.cryptox.asynctasks.DownloadFileAsyncTask;
import com.example.cryptox.asynctasks.GetFileListAsyncTask;
import com.example.cryptox.asynctasks.UploadFileAsyncTask;
import com.example.cryptox.models.CryptoXFile;
import com.example.cryptox.models.User;
import com.example.cryptox.utils.DateComparatorAscUtil;
import com.example.cryptox.utils.DateComparatorDescUtil;
import com.example.cryptox.utils.FileSizeUtil;
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
import com.parse.SaveCallback;

public class FileListActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_files);

		// authenticate dropbox at the beginning
		// no need to do that for every operation
		authenticateDropbox();

		// mDBApi.getSession().finishAuthentication();
		// hard-coding token_key and token_secret to avoid
		// authentication
		token_key = "w4meo7pm98ii6gp9";
		token_secret = "73tm5yhw8wa01af";
		AccessTokenPair tokens = new AccessTokenPair(token_key, token_secret);
		mDBApi.getSession().setAccessTokenPair(tokens);

		filesListView = (SwipeMenuListView) findViewById(R.id.listViewFiles);

		// get the file list from parse and set adapter
		setListViewAdapter();

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
					// authenticateDropbox();
					try
					{
						uploadFile();
					}
					catch (java.text.ParseException e)
					{
						Log.d("DateParseException", e.getMessage());
					}
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
		// mDBApi.getSession().startAuthentication(FileListActivity.this);

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
			cachedFileList = cryptoXFilesCopy;
			adapter.clear();
			adapter.addAll(cryptoXFilesCopy);
			adapter.notifyDataSetChanged();
		}
		else if (id == R.id.favorite_list)
		{
			
			favList = new ArrayList<CryptoXFile>();
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Favorites");
			query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> objects, ParseException e)
				{
					if (e == null)
					{
						for (int i = 0; i < objects.size(); i++)
						{
							CryptoXFile cryptoFile = new CryptoXFile();
							cryptoFile.setDateModified(objects.get(i).getString("lastModified"));
							cryptoFile.setFavorite(objects.get(i).getBoolean("favorite"));
							cryptoFile.setName(objects.get(i).getString("filename"));
							cryptoFile.setSize(objects.get(i).getString("fileSize"));
							cryptoFile.setType(objects.get(i).getString("fileType"));
							cryptoFile.setSizeBytes(objects.get(i).getLong("fileSizeBytes"));
							cryptoFile.setPath(objects.get(i).getString("originalPath"));
							cryptoFile.setHashValue(objects.get(i).getString("encFileName"));
							favList.add(cryptoFile);
						}
						cachedFileList = favList;
						adapter.clear();
						adapter.addAll(new ArrayList<CryptoXFile>(favList));
						adapter.notifyDataSetChanged();
					}
					else
					{
						Log.d("Favorites::", e.getMessage());
					}

				}

			});
		}
		else if (id == R.id.shared_lst)
		{
			sharedList = new ArrayList<CryptoXFile>();
			ParseQuery<ParseObject> query = ParseQuery.getQuery("Shared");
			query.whereEqualTo("sharedWith", ParseUser.getCurrentUser().getUsername());
			query.findInBackground(new FindCallback<ParseObject>() {

				@Override
				public void done(List<ParseObject> objects, ParseException e)
				{
					if (e == null)
					{
						for (int i = 0; i < objects.size(); i++)
						{
							CryptoXFile cryptoFile = new CryptoXFile();
							cryptoFile.setDateModified(objects.get(i).getString("lastModified"));
							cryptoFile.setFavorite(objects.get(i).getBoolean("favorite"));
							cryptoFile.setName(objects.get(i).getString("filename"));
							cryptoFile.setSize(objects.get(i).getString("fileSize"));
							cryptoFile.setType(objects.get(i).getString("fileType"));
							cryptoFile.setSizeBytes(objects.get(i).getLong("fileSizeBytes"));
							cryptoFile.setPath(objects.get(i).getString("originalPath"));
							cryptoFile.setHashValue(objects.get(i).getString("encFileName"));
							sharedList.add(cryptoFile);
						}
						cachedFileList = sharedList;
						adapter.clear();
						adapter.addAll(new ArrayList<CryptoXFile>(sharedList));
						adapter.notifyDataSetChanged();
					}
					else
					{
						Log.d("Shared::", e.getMessage());
					}

				}

			});
		}
		else if (id == R.id.logout)
		{
			if (ParseUser.getCurrentUser() != null)
			{
				ParseUser.logOut();
				Intent i = new Intent(getBaseContext(), MainActivity.class);
				startActivity(i);
			}
		}
		return super.onOptionsItemSelected(item);
	}

	private void uploadFile() throws java.text.ParseException
	{
		String newstr = null;
		String hashedVal = null;

		// create a file from file path
		File file = new File(filePath);

		// get the last modified date
		SimpleDateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
		String lastModified = df.format(new Date(file.lastModified()));

		// get the file size
		long fileSizeBytes = file.length();
		String fileSize = FileSizeUtil.getfileSize(fileSizeBytes);

		// get the file type
		String fileType = FileUtils.getMimeType(file);

		// SHA256
		try
		{
			hashedVal = SHA256(filePath);
		}
		catch (Exception e)
		{
			Log.d("EncrptyFile::", e.getMessage());
		}

		// AES Encryption
		try
		{
			encrypt(filePath, hashedVal);
		}
		catch (InvalidKeyException e)
		{
			Log.d("EncrptyFile::", e.getMessage());

		}
		catch (NoSuchAlgorithmException e)
		{
			Log.d("EncrptyFile::", e.getMessage());

		}
		catch (NoSuchPaddingException e)
		{
			Log.d("EncrptyFile::", e.getMessage());

		}
		catch (IOException e)
		{
			Log.d("EncrptyFile::", e.getMessage());

		}
		if (null != filePath && filePath.length() > 0)
		{
			int endIndex = filePath.lastIndexOf("/");
			if (endIndex != -1)
			{
				newstr = filePath.substring(0, endIndex);
			}
		}

		String[] bits = filePath.split("/");
		String lastOne = bits[bits.length - 1];

		ParseQuery<ParseObject> query = ParseQuery.getQuery("userFiles");
		query.whereEqualTo("fileName", file.getName());
		query.whereEqualTo("encFileName", hashedVal);
		try
		{
			// if same for file for same user already exists, don't add in parse and dropbox  
			if (query.find().size() > 0 && query.find().get(0).getString("username").equalsIgnoreCase(ParseUser.getCurrentUser().getUsername()))
			{
				Toast.makeText(getApplicationContext(), "File Already Exists in your account", Toast.LENGTH_SHORT).show();
				return;
			}
			// if same file having other user exists; only add in parse and not in dropbox  
			else if (query.find().size() > 0 && !query.find().get(0).getString("username").equalsIgnoreCase(ParseUser.getCurrentUser().getUsername()))
			{
				// only add parse entry.. no upload to file
				ParseObject userFile = new ParseObject("userFiles");
				userFile.put("username", ParseUser.getCurrentUser().getUsername());
				userFile.put("encFileName", hashedVal);
				userFile.put("fileName", lastOne);
				userFile.put("favorite", false);
				userFile.put("lastModified", lastModified);
				userFile.put("fileSize", fileSize);
				userFile.put("fileType", fileType);
				userFile.put("fileSizeBytes", fileSizeBytes);
				userFile.put("originalPath", filePath);
				userFile.saveInBackground();
				
				refreshListView();
			}
			// if the file is not already uploaded, then add to parse as well as dropbox 
			else
			{
				ParseObject userFile = new ParseObject("userFiles");
				userFile.put("username", ParseUser.getCurrentUser().getUsername());
				userFile.put("encFileName", hashedVal);
				userFile.put("fileName", lastOne);
				userFile.put("favorite", false);
				userFile.put("lastModified", lastModified);
				userFile.put("fileSize", fileSize);
				userFile.put("fileType", fileType);
				userFile.put("fileSizeBytes", fileSizeBytes);
				userFile.put("originalPath", filePath);
				userFile.saveInBackground();
				
				new UploadFileAsyncTask(FileListActivity.this).execute(newstr + "/" + hashedVal);

			}
		}
		catch (ParseException e1)
		{
			Log.d("FileListActiviy", e1.getMessage());
		}

		// new GetFileListAsyncTask(this).execute();
	}

	public static String SHA256(String file) throws Exception
	{
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		FileInputStream fis = new FileInputStream(file);

		byte[] dataBytes = new byte[1024];

		int nread = 0;
		while ((nread = fis.read(dataBytes)) != -1)
		{
			md.update(dataBytes, 0, nread);
		}

		byte[] mdbytes = md.digest();

		// convert the byte to hex format method 1
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < mdbytes.length; i++)
		{
			sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		}

		System.out.println("Hex format : " + sb.toString());

		// convert the byte to hex format method 2
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < mdbytes.length; i++)
		{
			hexString.append(Integer.toHexString(0xFF & mdbytes[i]));
		}

		return hexString.toString();
	}

	public static void encrypt(String fileName, String hashedVal) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException
	{
		String newstr = null;
		FileInputStream fis = new FileInputStream(fileName);
		if (null != fileName && fileName.length() > 0)
		{
			int endIndex = fileName.lastIndexOf("/");
			if (endIndex != -1)
			{
				newstr = fileName.substring(0, endIndex);
			}
		}
		FileOutputStream fos = new FileOutputStream(newstr + "/" + hashedVal);
		SecretKeySpec sks = new SecretKeySpec("MyDifficultPassw".getBytes(), "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, sks);
		CipherOutputStream cos = new CipherOutputStream(fos, cipher);
		int b;
		byte[] d = new byte[8];
		while ((b = fis.read(d)) != -1)
		{
			cos.write(d, 0, b);
		}
		cos.flush();
		cos.close();
		fis.close();
	}

	public static void decrypt(String fileName, String hashedVal) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException
	{
		FileInputStream fis = new FileInputStream(fileName);
		FileOutputStream fos = new FileOutputStream(hashedVal);
		SecretKeySpec sks = new SecretKeySpec("MyDifficultPassw".getBytes(), "AES");
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.DECRYPT_MODE, sks);
		CipherInputStream cis = new CipherInputStream(fis, cipher);
		int b;
		byte[] d = new byte[8];
		while ((b = cis.read(d)) != -1)
		{
			fos.write(d, 0, b);
		}
		fos.flush();
		fos.close();
		cis.close();

		// delete the encrpted file after decryption
		File file = new File(fileName);
		file.delete();
	}

	private void setListViewAdapter()
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery("userFiles");
		query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> objects, ParseException e)
			{
				if (e == null)
				{
					for (int i = 0; i < objects.size(); i++)
					{
						// create custom crptoX files
						CryptoXFile cryptoFile = new CryptoXFile();
						cryptoFile.setDateModified(objects.get(i).getString("lastModified"));
						cryptoFile.setFavorite(objects.get(i).getBoolean("favorite"));
						cryptoFile.setName(objects.get(i).getString("fileName"));
						cryptoFile.setSize(objects.get(i).getString("fileSize"));
						cryptoFile.setType(objects.get(i).getString("fileType"));
						cryptoFile.setSizeBytes(objects.get(i).getLong("fileSizeBytes"));
						cryptoFile.setPath(objects.get(i).getString("originalPath"));
						cryptoFile.setHashValue(objects.get(i).getString("encFileName"));

						// add to main list
						cryptoXFiles.add(cryptoFile);

						// maintain copies of file list
						// to be used for custom sorting purposes
						cryptoXFilesCopy.add(cryptoFile);
						cachedFileList.add(cryptoFile);
					}
					if (cryptoXFiles.size() == 0)
					{
						Toast.makeText(FileListActivity.this, "No files in your account", Toast.LENGTH_LONG).show();
					}
					// create and set adapter for file list
					adapter = new FileAdapter(FileListActivity.this, R.layout.files_layout, cryptoXFiles);
					filesListView.setAdapter(adapter);
					setupListViewSwipe();
				}
				else
				{
					Log.d("GetParseFileList::", e.getMessage());
				}
			}
		});
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

		filesListView.setOnSwipeListener(new OnSwipeListener() {

			@Override
			public void onSwipeStart(int position)
			{
				Log.d("SwipeTest::", "Swipe Open working");
				CryptoXFile file = cryptoXFilesCopy.get(position);
				if (isFavorite(file))
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
	}

	protected void toggleFav(final CryptoXFile file)
	{

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Favorites");
		query.whereEqualTo("encFileName", file.getHashValue());
		query.findInBackground(new FindCallback<ParseObject>() {

			@Override
			public void done(List<ParseObject> objects, ParseException e)
			{
				if (objects.size() > 0 && objects.get(0).getBoolean("favorite"))
				{
					objects.get(0).deleteInBackground();
					Toast.makeText(getApplicationContext(), "File removed from favorites", Toast.LENGTH_SHORT).show();
				}
				else
				{
					favButton.setIcon(R.drawable.ic_action_important);
					ParseObject fav = new ParseObject("Favorites");
					fav.put("username", ParseUser.getCurrentUser().getUsername());
					fav.put("favorite", true);
					fav.put("filename", file.getName());
					fav.put("encFileName", file.getHashValue());
					fav.put("lastModified", file.getDateModified());
					fav.put("fileSize", file.getSize());
					fav.put("fileType", file.getType());
					fav.put("fileSizeBytes", file.getSizeBytes());
					fav.put("originalPath", file.getPath());
					fav.saveInBackground(new SaveCallback() {
						@Override
						public void done(ParseException e)
						{
							Toast.makeText(getApplicationContext(), "File added to Favorites", Toast.LENGTH_SHORT).show();
						}
					});
				}

			}
		});
	}

	private boolean isFavorite(CryptoXFile file)
	{
		flag = false;
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Favorites");
		query.whereEqualTo("encFileName", file.getHashValue());

		try
		{
			if (query.find().size() > 0)
				flag = true;
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}

		return flag;
	}

	protected void shareFile(final CryptoXFile file)
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
						user.setName(users.get(i).getString("username"));
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
							final String uname = userList.get(which).getName();
							ParseObject sharedApp = new ParseObject("Shared");
							sharedApp.put("sharedWith", uname);
							sharedApp.put("sharedFrom", ParseUser.getCurrentUser().getUsername());
							sharedApp.put("filename", file.getName());
							sharedApp.put("encFileName", file.getHashValue());
							sharedApp.put("lastModified", file.getDateModified());
							sharedApp.put("fileSize", file.getSize());
							sharedApp.put("fileType", file.getType());
							sharedApp.put("fileSizeBytes", file.getSizeBytes());
							sharedApp.put("originalPath", file.getPath());

							sharedApp.saveInBackground(new SaveCallback() {

								@Override
								public void done(ParseException e)
								{
									if (e == null)
									{
										Toast.makeText(FileListActivity.this, "Shared with " + uname, Toast.LENGTH_SHORT).show();
									}
									else
									{
										Log.d("SharedFeature::", e.getMessage());
									}

								}
							});

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

	public void refreshListView()
	{
		// clear all the lists as they will be repopulated in setListViewAdapter
		cryptoXFiles.clear();
		cryptoXFilesCopy.clear();
		cachedFileList.clear();

		// clear list view adapter
		adapter.clear();

		setListViewAdapter();
	}

	protected void downloadFile(CryptoXFile cryptoXFile)
	{
		new DownloadFileAsyncTask(FileListActivity.this).execute(cryptoXFile);

	}

	protected void onDestroy()
	{
		finish();
		System.exit(0);
	}

	ArrayList<CryptoXFile> cryptoXFiles = new ArrayList<CryptoXFile>();;

	// copy of cryptoXFiles for sorting functions
	ArrayList<CryptoXFile> cachedFileList = new ArrayList<CryptoXFile>();

	// copy of cryptoXFiles in case original list is cleared
	ArrayList<CryptoXFile> cryptoXFilesCopy = new ArrayList<CryptoXFile>();

	// fav list
	ArrayList<CryptoXFile> favList;

	// shared list
	ArrayList<CryptoXFile> sharedList;

	SwipeMenuListView filesListView;
	String filePath;
	FileAdapter adapter;
	ArrayList<User> userList;
	ArrayList<String> userNameList;
	SwipeMenuItem favButton;
	String token_key;
	String token_secret;
	boolean flag;

	// request codes for startActivityForResult
	protected static final int REQ_CODE_GET_FILE = 1002;

	// dropbox keys and variables
	final static private String APP_KEY = "6knyn43b3wga8ri";
	final static private String APP_SECRET = "zt9p544duz26e51";
	public static DropboxAPI<AndroidAuthSession> mDBApi;
	String accessToken;

}