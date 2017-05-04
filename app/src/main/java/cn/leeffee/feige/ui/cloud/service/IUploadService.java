package cn.leeffee.feige.ui.cloud.service;


import android.os.RemoteException;


public interface IUploadService {
    boolean cancelTask(int id) throws RemoteException;

    boolean cancelAllTask() throws RemoteException;

    void unlock();

    UploadTask getExcTask();
}
