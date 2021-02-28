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
 *  Last Modified 2/22/18 2:31 AM
 */

package org.goflex.wp2.fmanproxy.common;

import org.springframework.http.HttpStatus;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;

/**
 * Created by bijay on 1/5/18.
 */
public class ResponseMessage implements Serializable {


    private static final long serialVersionUID = 5496357639753292843L;
    @XmlAttribute
    private HttpStatus status;

    @XmlAttribute
    private String message;

    @XmlAttribute
    private Object Data = "";


    public ResponseMessage() {

    }

    public ResponseMessage(HttpStatus status, String msg, Object data) {
        this.status = status;
        this.message = msg;
        this.Data = data;
    }


    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public Object getData() {
        return Data;
    }

    public void setData(Object data) {
        Data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResponseMessage)) return false;

        ResponseMessage that = (ResponseMessage) o;

        if (status != that.status) return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        return Data != null ? Data.equals(that.Data) : that.Data == null;
    }

    @Override
    public int hashCode() {
        int result = status != null ? status.hashCode() : 0;
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (Data != null ? Data.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ResponseMessage{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", Data=" + Data +
                '}';
    }
}
