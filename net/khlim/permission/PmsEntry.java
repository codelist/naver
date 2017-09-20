package net.khlim.permission;

public class PmsEntry {

    String permission;      // 퍼미션
    String notice;          // 권한을 사용하는 목적

    public PmsEntry(String permission){
        this.permission = permission;
        this.notice = null;
    }

    public PmsEntry(String permission, String notice){
        this.permission = permission;
        this.notice = notice;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    @Override
    public String toString(){
        return permission;
    }
}
