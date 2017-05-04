package cn.leeffee.feige.ui.cloud.service;

public interface ITransferConstants {
    // status : 0暂停， 1等待运行，2运行，3 完成，4下载异常未完成 ,,
    // 5取消下载（未完成删除已下载的文件.tmp），6删除已完成下载记录（不删除已下载文件），7提取码错误
    // 8 sdcard 异常，9未知异常, 10服务器响应错误，12 上传（下载）读取文件失败
    // 13  //文件不存在

    int STATUS_PAUSE = 0;

    int STATUS_WAIT = 1;

    int STATUS_RUN = 2;

    int STATUS_FINISH = 3;

    int NET_EXCEPTION = 4;

    int STATUS_CANCEL = 5;

    int STATUE_DELETE = 6;//删除已完成下载记录（不删除已下载文件）

    int GET_CODE_ERROR = 7;

    int SDCARD_IO_EXCEPTION_ERROR = 8; //sdcard 输入输出流错误

    int UNKNOWN_ERROR = 9; //未知异常

    int SERVER_RESPONSE_ERROR = 10; //服务器响应错误

    int SERVER_RESPONSE_PARSE_ERROR = 11;//未知解析错误

    int TRANSFER_FAIL_ERROR = 12;//上传（下载）读取文件失败

    int FILE_NOT_EXIST = 13; //文件不存在

    int WIFI_EXCEPTION = 14; //没有WIFI

    String SERVER_MESSAGE = "server.message";
}
