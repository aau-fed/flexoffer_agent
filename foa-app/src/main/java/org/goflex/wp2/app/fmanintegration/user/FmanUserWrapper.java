package org.goflex.wp2.app.fmanintegration.user;


import java.util.List;
import java.util.Map;

public class FmanUserWrapper {

    private List<Map<String, String>> fmanUserList;

    public FmanUserWrapper() {
    }

    public List<Map<String, String>> getFmanUserList() {
        return fmanUserList;
    }

    public void setFmanUserList(List<Map<String, String>> fmanUserList) {
        this.fmanUserList = fmanUserList;
    }
}
