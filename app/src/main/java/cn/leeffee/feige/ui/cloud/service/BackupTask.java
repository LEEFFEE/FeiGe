package cn.leeffee.feige.ui.cloud.service;

import java.io.Serializable;

public class BackupTask implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private int id = -1;
	//无实际意义，显示目录名1,目录名2
	private String title = "";
	
	private String userName = "";
	
	private int status = -1;
	
	private String localPath = "";
	
	private String remotePath = "";
	
	private String addQueueTime = "";
	
	private String finishTime = "";
	
	//暂时不存放数据库的字段
	private int totalNum = 0;
	private int overNum = 0;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getLocalPath() {
		return localPath;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

	public String getRemotePath() {
		return remotePath;
	}

	public void setRemotePath(String remotePath) {
		this.remotePath = remotePath;
	}

	public String getAddQueueTime() {
		return addQueueTime;
	}

	public void setAddQueueTime(String addQueueTime) {
		this.addQueueTime = addQueueTime;
	}

	public String getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(String finishTime) {
		this.finishTime = finishTime;
	}

	public int getTotalNum() {
		return totalNum;
	}

	public void setTotalNum(int totalNum) {
		this.totalNum = totalNum;
	}

	public int getOverNum() {
		return overNum;
	}

	public void setOverNum(int overNum) {
		this.overNum = overNum;
	}
	
}
