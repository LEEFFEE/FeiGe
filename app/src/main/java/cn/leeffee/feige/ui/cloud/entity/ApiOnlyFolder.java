package cn.leeffee.feige.ui.cloud.entity;

/**
 * Created by lhfei on 2017/4/6.
 */

public class ApiOnlyFolder {

    /**
     * attrs : {}
     * id : /123456
     * isfolder : true
     * parentId : null
     * text : 123456
     */

    private AttrsBean attrs;
    private String id;
    private boolean isfolder;
    private String parentId;
    private String text;

    public AttrsBean getAttrs() {
        return attrs;
    }

    public void setAttrs(AttrsBean attrs) {
        this.attrs = attrs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isIsfolder() {
        return isfolder;
    }

    public void setIsfolder(boolean isfolder) {
        this.isfolder = isfolder;
    }

    public Object getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public static class AttrsBean {
    }
}
