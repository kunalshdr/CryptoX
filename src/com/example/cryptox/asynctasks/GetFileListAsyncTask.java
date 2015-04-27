package com.example.cryptox.asynctasks;

import java.io.File;
import java.util.ArrayList;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.example.cryptox.FileListActivity;
import com.example.cryptox.models.CryptoXFile;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class GetFileListAsyncTask extends AsyncTask<Void, Void, ArrayList<CryptoXFile>>
{

	@Override
	protected void onPreExecute()
	{
		pb = new ProgressDialog(fActivity);
		pb.setMessage("Loading...");
		pb.setCancelable(false);
		pb.show();

	}

	public GetFileListAsyncTask(FileListActivity fActivity)
	{
		this.fActivity = fActivity;
	}

	@Override
	protected ArrayList<CryptoXFile> doInBackground(Void... params)
	{
		Entry entries = null;
		try
		{
			// TODO (kshridha): get the file list from parse and display
			// populate array list of crypto files
			// remove the metadata call
			entries = FileListActivity.mDBApi.metadata("/CryptoX/", 100, null, true, null);
		}
		catch (DropboxException e)
		{
			Log.d("GetFileListAsyncTask::", e.getMessage());
		}

		files = new ArrayList<CryptoXFile>();
		for (Entry e : entries.contents)
		{
			if (!e.isDeleted)
			{
				CryptoXFile f = new CryptoXFile();
				f.setDateModified(e.modified);
				f.setName(e.fileName());
				f.setSize(e.size);
				f.setSizeBytes(e.bytes);
				f.setType(e.mimeType);
				f.setPath(e.parentPath());
				files.add(f);
			}
		}
		return files;
	}

	@Override
	protected void onPostExecute(ArrayList<CryptoXFile> result)
	{
		fActivity.setListViewAdapter(result);
		pb.dismiss();
	}

	ArrayList<CryptoXFile> files;
	FileListActivity fActivity;
	ProgressDialog pb;

}
