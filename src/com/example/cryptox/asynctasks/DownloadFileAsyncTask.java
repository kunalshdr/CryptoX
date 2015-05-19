package com.example.cryptox.asynctasks;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.NoSuchPaddingException;

import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.exception.DropboxException;
import com.example.cryptox.FileListActivity;
import com.example.cryptox.models.CryptoXFile;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class DownloadFileAsyncTask extends AsyncTask<CryptoXFile, Void, CryptoXFile>
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
	protected void onPostExecute(CryptoXFile result)
	{
		try
		{
			FileListActivity.decrypt(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + result.getHashValue(),
					Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"  + result.getName());
		}
		catch (InvalidKeyException e)
		{
			Log.d("Decryptfile::", e.getMessage());
		}
		catch (NoSuchAlgorithmException e)
		{
			Log.d("Decryptfile::", e.getMessage());

		}
		catch (NoSuchPaddingException e)
		{
			Log.d("Decryptfile::", e.getMessage());

		}
		catch (IOException e)
		{
			Log.d("Decryptfile::", e.getMessage());

		}
		Toast.makeText(fActivity, "File Download Completed", Toast.LENGTH_SHORT).show();
		return;
	}

	@Override
	protected CryptoXFile doInBackground(CryptoXFile... params)
	{
		String hash = params[0].getHashValue();

		File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + hash);
		OutputStream outputStream = null;
		DropboxFileInfo info = null;

		try
		{
			outputStream = new BufferedOutputStream(new FileOutputStream(file));

			info = FileListActivity.mDBApi.getFile("/CryptoX/" + hash, null, outputStream, null);
			Log.i("DownloadFileAsyncTask::", info.getFileSize() + "");
			Log.i("DbExampleLog", "The file's rev is: " + info.getMetadata().rev);

		}
		catch (FileNotFoundException e)
		{
			Log.d("DownloadFileAsyncTask::", e.getMessage());
		}
		catch (DropboxException e)
		{
			Log.d("DownloadFileAsyncTask::", "Download Download Error");
		}
		return params[0];
	}

}
