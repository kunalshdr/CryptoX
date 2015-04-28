package com.example.cryptox.models;

public class CryptoXFile
{

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDateModified()
	{
		return dateModified;
	}

	public void setDateModified(String dateModified)
	{
		this.dateModified = dateModified;
	}

	public String getSize()
	{
		return size;
	}

	public void setSize(String size)
	{
		this.size = size;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public long getSizeBytes()
	{
		return sizeBytes;
	}

	public void setSizeBytes(long sizeBytes)
	{
		this.sizeBytes = sizeBytes;
	}

	public boolean isFavorite()
	{
		return favorite;
	}

	public void setFavorite(boolean favorite)
	{
		this.favorite = favorite;
	}

	private String name;
	private String dateModified;
	private String size;
	private String type;
	private String path;
	private boolean favorite;
	private long sizeBytes;

}
