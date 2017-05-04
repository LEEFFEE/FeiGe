package cn.leeffee.feige.ui.cloud.service;

import android.os.RemoteException;

public interface IBackupService {

	boolean cancelTask(int id) throws RemoteException;
	
	boolean cancelAllTask() throws RemoteException;

	void unlock();

	BackupTask getExcTask();
	
	void off();
	
	void on();
}
