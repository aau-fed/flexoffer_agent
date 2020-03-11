
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
 *  Last Modified 2/22/18 2:50 AM
 */

package org.goflex.wp2.core.models;

import org.goflex.wp2.core.entities.MessageCode;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by bijay on 1/5/18.
 */
@Entity
public class UserMessage implements Serializable {


    private static final long serialVersionUID = -6098719975987069027L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long messageId;

    private MessageCode messageCode;

    private String message;

    private String userName;

    private int messageStatus;

    private int notifiedToAdmin = 0;

    private String deviceID;

    private Date messageDate;

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(int messageStatus) {
        this.messageStatus = messageStatus;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Date getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(Date messageDate) {
        this.messageDate = messageDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserMessage)) return false;
        UserMessage message1 = (UserMessage) o;
        if (messageId != message1.messageId) return false;
        if (messageStatus != message1.messageStatus) return false;
        if (message != null ? !message.equals(message1.message) : message1.message != null) return false;
        if (userName != null ? !userName.equals(message1.userName) : message1.userName != null) return false;
        return messageDate != null ? messageDate.equals(message1.messageDate) : message1.messageDate == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (messageId ^ (messageId >>> 32));
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        result = 31 * result + messageStatus;
        result = 31 * result + (messageDate != null ? messageDate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UserMessage{" +
                "messageId=" + messageId +
                ", message='" + message + '\'' +
                ", userName='" + userName + '\'' +
                ", messageStatus=" + messageStatus +
                ", messageDate=" + messageDate +
                '}';
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public MessageCode getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(MessageCode messageCode) {
        this.messageCode = messageCode;
    }

    public int getNotifiedToAdmin() {
        return notifiedToAdmin;
    }

    public void setNotifiedToAdmin(int notifiedToAdmin) {
        this.notifiedToAdmin = notifiedToAdmin;
    }
}
