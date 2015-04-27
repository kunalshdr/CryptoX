package com.example.cryptox.asynctasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.exception.DropboxException;
import com.example.cryptox.FileListActivity;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class DownloadFileAsyncTask extends AsyncTask<String, Void, Void>
{
	FileListActivity fActivity;

	public DownloadFileAsyncTask(FileListActivity fActivity)
	{
		this.fActivity = fActivity;
	}

	@Override
	protected void onPreExecute()
	{
		Toast.makeText(fActivity, "File Download Started", Toast.LENGTH_SHORT).show();
		return;
	}

	@Override
	protected void onPostExecute(Void result)
	{
		Toast.makeText(fActivity, "File Download Completed", Toast.LENGTH_SHORT).show();
		return;
	}

	@Override
	protected Void doInBackground(String... params)
	{
		String path = params[0];
		File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "DownloadedFile" + Math.random());
		FileOutputStream outputStream = null;
		DropboxFileInfo info = null;

		try
		{
			outputStream = new FileOutputStream(file);
			info = FileListActivity.mDBApi.getFile("/CryptoX/Sachin.jpg", null, outputStream, null);
			Log.i("DownloadFileAsyncTask::", info.getFileSize() + "");
		}
		catch (FileNotFoundException e)
		{
			Log.d("DownloadFileAsyncTask::", e.getMessage());
		}
		catch (DropboxException e)
		{
			Log.d("DownloadFileAsyncTask::", e.getMessage());
		}
		return null;
	}

}
