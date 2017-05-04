/*    
 * Copyright (c) 2011 CoolCloudz, Inc.
 * All right reserved.
 *
 * 文件名：      FileInfo.java
 * 作者:     Jacky Wang
 * 创建日期： 2011-9-19 上午08:51:12
 * 版本：           
 *
 */
package cn.leeffee.feige.ui.cloud.entity;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author Jacky Wang
 */
public class FileInfo implements Comparable<FileInfo>, Serializable
{
	public final String		filename;
	public final long		size;
	public final boolean	isFile;
	public final boolean	isParent;
	public final String		filepath;
	public final Timestamp	lastModifiedTime;

	public FileInfo(String filename, long size, boolean isFile, boolean isParent, String filePath, Timestamp lastModifiedTime)
	{
		this.filename = filename;
		this.size = size;
		this.isFile = isFile;
		this.isParent = isParent;
		this.filepath = filePath;
		this.lastModifiedTime = lastModifiedTime;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((filepath == null) ? 0 : filepath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileInfo other = (FileInfo) obj;
		if (filepath == null)
		{
			if (other.filepath != null)
				return false;
		} else if (!filepath.equals(other.filepath))
			return false;
		return true;
	}

	public int compareTo(FileInfo other)
	{
		if (isParent)
		{
			return 1;
		}
		if (isFile)
		{
			return other.isFile ? 0 : 1;
		} else
		{
			return other.isFile ? -1 : 0;
		}
	}
}
