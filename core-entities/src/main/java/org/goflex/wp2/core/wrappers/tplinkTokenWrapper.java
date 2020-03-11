/*
 * Created by bijay
 * GOFLEX :: WP2 :: foa-core
 * Copyright (c) 2018.
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining  a copy of this software and associated documentation
 *  files (the "Software") to deal in the Software without restriction,
 *  including without limitation the rights to use, copy, modify, merge,
 *  publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions: The above copyright notice and
 *  this permission notice shall be included in all copies or substantial
 *  portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON
 *  INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 *
 *  Last Modified 2/22/18 2:43 AM
 */

package org.goflex.wp2.core.wrappers;


import java.io.Serializable;
import java.util.UUID;

/**
 * Created by bijay on 7/22/17.
 */
public class tplinkTokenWrapper implements Serializable {


    private static final long serialVersionUID = 3346255524281894521L;
    private String method;
    private String appType;
    private String cloudUserName;
    private String cloudPassword;
    private UUID terminalUUID;


    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getCloudUserName() {
        return cloudUserName;
    }

    public void setCloudUserName(String cloudUserName) {
        this.cloudUserName = cloudUserName;
    }

    public String getCloudPassword() {
        return cloudPassword;
    }

    public void setCloudPassword(String cloudPassword) {
        this.cloudPassword = cloudPassword;
    }

    public UUID getTerminalUUID() {
        return terminalUUID;
    }

    public void setTerminalUUID(UUID terminalUUID) {
        this.terminalUUID = terminalUUID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof tplinkTokenWrapper)) return false;

        tplinkTokenWrapper that = (tplinkTokenWrapper) o;

        if (method != null ? !method.equals(that.method) : that.method != null) return false;
        if (appType != null ? !appType.equals(that.appType) : that.appType != null) return false;
        if (cloudUserName != null ? !cloudUserName.equals(that.cloudUserName) : that.cloudUserName != null)
            return false;
        if (cloudPassword != null ? !cloudPassword.equals(that.cloudPassword) : that.cloudPassword != null)
            return false;
        return terminalUUID != null ? terminalUUID.equals(that.terminalUUID) : that.terminalUUID == null;
    }

    @Override
    public int hashCode() {
        int result = method != null ? method.hashCode() : 0;
        result = 31 * result + (appType != null ? appType.hashCode() : 0);
        result = 31 * result + (cloudUserName != null ? cloudUserName.hashCode() : 0);
        result = 31 * result + (cloudPassword != null ? cloudPassword.hashCode() : 0);
        result = 31 * result + (terminalUUID != null ? terminalUUID.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "tplinkTokenWrapper{" +
                "method='" + method + '\'' +
                ", appType='" + appType + '\'' +
                ", cloudUserName='" + cloudUserName + '\'' +
                ", cloudPassword='" + cloudPassword + '\'' +
                ", terminalUUID=" + terminalUUID +
                '}';
    }
}
