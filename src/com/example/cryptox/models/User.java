package com.example.cryptox.models;

public class User implements Comparable<User>
{

	private String name;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Override
	public int compareTo(User another)
	{
		return this.getName().compareToIgnoreCase(another.getName());
	}

}
