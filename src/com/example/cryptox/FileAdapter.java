package com.example.cryptox;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.example.cryptox.models.CryptoXFile;
import com.squareup.picasso.Picasso;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileAdapter extends ArrayAdapter<CryptoXFile>
{

	Context context;
	ArrayList<CryptoXFile> object;
	int resId;

	public FileAdapter(Context context, int textViewResourceId, List<CryptoXFile> object)
	{
		super(context, R.layout.files_layout, object);
		this.context = context;
		this.object = (ArrayList<CryptoXFile>) object;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{

		View v = convertView;
		if (v == null)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.files_layout, parent, false);
		}
		TextView fileNameTextView = (TextView) v.findViewById(R.id.textViewFileName);
		TextView fileSizeTextView = (TextView) v.findViewById(R.id.textViewFileSize);
		TextView fileModifiedDateTextView = (TextView) v.findViewById(R.id.textViewlastModified);

		fileNameTextView.setText(object.get(position).getName());
		fileSizeTextView.setText(object.get(position).getSize());

		// remove the +0000 or -0000 part from date
		String date = object.get(position).getDateModified();
		int index = date.indexOf('+'); 
		if (index == -1)
			index = date.indexOf('-');
		date = date.substring(0, index).trim();
		fileModifiedDateTextView.setText(date);

		// set the image icon based on the file type
		ImageView thumbnailImageView = (ImageView) v.findViewById(R.id.imageViewFile);
		String type = object.get(position).getType();
		int width = 70, height = 70;
		if (type.contains("audio"))
		{
			Picasso.with(context).load(R.drawable.ic_audio).resize(width, height).into(thumbnailImageView);
		}
		else if (type.contains("video"))
		{
			Picasso.with(context).load(R.drawable.ic_video).resize(width, height).into(thumbnailImageView);

		}
		else if (type.contains("text") || type.contains("document"))
		{
			Picasso.with(context).load(R.drawable.ic_doc).resize(width, height).into(thumbnailImageView);

		}
		else if (type.contains("pdf"))
		{
			Picasso.with(context).load(R.drawable.ic_pdf).resize(width, height).into(thumbnailImageView);

		}
		else if (type.contains("zip"))
		{
			Picasso.with(context).load(R.drawable.ic_zip).resize(width, height).into(thumbnailImageView);

		}
		else if (type.contains("image"))
		{
			Picasso.with(context).load(R.drawable.ic_image).resize(width, height).into(thumbnailImageView);

		}
		else
		{
			Picasso.with(context).load(R.drawable.ic_file).resize(width, height).into(thumbnailImageView);

		}

		return v;
	}
}