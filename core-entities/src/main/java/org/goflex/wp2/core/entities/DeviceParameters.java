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
 *  Last Modified 2/22/18 2:36 AM
 */

package org.goflex.wp2.core.entities;

import java.io.Serializable;
import java.util.UUID;

/**
 * Class to store TpLink plug parameters
 * Created by bijay on 1/8/18.
 */
public class DeviceParameters implements Serializable {


    private static final long serialVersionUID = 6052838949682187459L;
    private String cloudAPIUrl;

    private String APIKey = "";

    private String cloudUserName;

    private String cloudPassword;

    private String appType = "Kasa_Android";

    private UUID terminalUUID = UUID.randomUUID();

    public DeviceParameters() {

    }

    public DeviceParameters(String cloudUserName, String cloudPassword,
                            String APIKey, String cloudAPIUrl) {
        this.cloudUserName = cloudUserName;
        this.cloudPassword = cloudPassword;
        this.cloudAPIUrl = cloudAPIUrl;
        this.APIKey = APIKey;
    }

    public String getAPIKey() {
        return APIKey;
    }

    public void setAPIKey(String APIKey) {
        this.APIKey = APIKey;
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

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public UUID getTerminalUUID() {
        return terminalUUID;
    }

    public void setTerminalUUID(UUID terminalUUID) {
        this.terminalUUID = terminalUUID;
    }

    public String getCloudAPIUrl() {
        return cloudAPIUrl;
    }

    public void setCloudAPIUrl(String cloudAPIUrl) {
        this.cloudAPIUrl = cloudAPIUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceParameters)) return false;

        DeviceParameters that = (DeviceParameters) o;

        if (cloudAPIUrl != null ? !cloudAPIUrl.equals(that.cloudAPIUrl) : that.cloudAPIUrl != null) return false;
        if (APIKey != null ? !APIKey.equals(that.APIKey) : that.APIKey != null) return false;
        if (cloudUserName != null ? !cloudUserName.equals(that.cloudUserName) : that.cloudUserName != null)
            return false;
        if (cloudPassword != null ? !cloudPassword.equals(that.cloudPassword) : that.cloudPassword != null)
            return false;
        if (appType != null ? !appType.equals(that.appType) : that.appType != null) return false;
        return terminalUUID != null ? terminalUUID.equals(that.terminalUUID) : that.terminalUUID == null;
    }

    @Override
    public int hashCode() {
        int result = cloudAPIUrl != null ? cloudAPIUrl.hashCode() : 0;
        result = 31 * result + (APIKey != null ? APIKey.hashCode() : 0);
        result = 31 * result + (cloudUserName != null ? cloudUserName.hashCode() : 0);
        result = 31 * result + (cloudPassword != null ? cloudPassword.hashCode() : 0);
        result = 31 * result + (appType != null ? appType.hashCode() : 0);
        result = 31 * result + (terminalUUID != null ? terminalUUID.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DeviceParameters{" +
                "cloudAPIUrl='" + cloudAPIUrl + '\'' +
                ", APIKey='" + APIKey + '\'' +
                ", cloudUserName='" + cloudUserName + '\'' +
                ", cloudPassword='" + cloudPassword + '\'' +
                ", appType='" + appType + '\'' +
                ", terminalUUID=" + terminalUUID +
                '}';
    }
}
