package cn.leeffee.feige.ui.cloud.service;

import android.os.RemoteException;

public interface IDownloadService {
	boolean cancelTask(int id);
	
	void unlock();
	
	DownloadTask getExcTask();

	boolean cancelAllTask() throws RemoteException;
}
