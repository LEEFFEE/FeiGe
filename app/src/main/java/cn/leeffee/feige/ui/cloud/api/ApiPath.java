package cn.leeffee.feige.ui.cloud.api;

/**
 * Created by lhfei on 2017/5/9.
 */

public class ApiPath {
    private String path;
    private int version;

    public ApiPath(String path, int version) {
        this.path = path;
        this.version = version;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
