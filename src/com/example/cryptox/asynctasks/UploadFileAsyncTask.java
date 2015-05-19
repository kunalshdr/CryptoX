package com.example.cryptox.asynctasks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.example.cryptox.FileListActivity;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class UploadFileAsyncTask extends AsyncTask<String, Void, Void>
{

	FileListActivity fActivity;

	public UploadFileAsyncTask(FileListActivity fActivity)
	{
		this.fActivity = fActivity;
	}

	@Override
	protected void onPreExecute()
	{
		Toast.makeText(fActivity, "File Upload Started", Toast.LENGTH_SHORT).show();
		return;
	}

	@Override
	protected void onPostExecute(Void result)
	{
		Toast.makeText(fActivity, "File Upload Complete", Toast.LENGTH_SHORT).show();
		fActivity.refreshListView();
	}

	@Override
	protected Void doInBackground(String... params)
	{
		File file = new File(params[0]);
		FileInputStream inputStream = null;
		Entry response;

		try
		{
			inputStream = new FileInputStream(file);
			response = FileListActivity.mDBApi.putFile("/CryptoX/" + file.getName(), inputStream, file.length(), null, null);
			Log.i("UploadFileAsyncTask::", response.rev);
		}
		catch (FileNotFoundException e)
		{
			Log.d("UploadFileAsyncTask::", e.getMessage());
		}
		catch (DropboxException e)
		{
			Log.d("UploadFileAsyncTask::", e.getMessage());

		}
		return null;
	}

}
