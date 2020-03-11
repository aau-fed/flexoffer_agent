/**
 *
 */
package org.goflex.wp2.core.wrappers;

/*-
 * #%L
 * ARROWHEAD::WP5::Core Data Structures
 * %%
 * Copyright (C) 2016 The ARROWHEAD Consortium
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import com.fasterxml.jackson.annotation.JsonFormat;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;
import java.util.Date;


@XmlAccessorType(XmlAccessType.FIELD)
public class IneaHeartBeatMessage implements Serializable {
    public static final String dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private String offeredById;
    private Date foaTime;
    private OperationInformation operationInfo;

    public String getOfferedById() {
        return offeredById;
    }

    public void setOfferedById(String offeredById) {
        this.offeredById = offeredById;
    }

    @XmlAttribute
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateFormat)
    public Date getFoaTime() {
        return foaTime;
    }


    public void setFoaTime(Date foaTime) {
        this.foaTime = foaTime;
    }


    public OperationInformation getOperationInfo() {
        return operationInfo;
    }

    public void setOperationInfo(OperationInformation operationInfo) {
        this.operationInfo = operationInfo;
    }

    @Override
    public String toString() {
        return "IneaHeartBeatMessage{" +
                "offeredById='" + offeredById + '\'' +
                ", foaTime=" + foaTime +
                ", operationInfo=" + operationInfo +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IneaHeartBeatMessage)) return false;

        IneaHeartBeatMessage that = (IneaHeartBeatMessage) o;

        if (!offeredById.equals(that.offeredById)) return false;
        if (!foaTime.equals(that.foaTime)) return false;
        return operationInfo.equals(that.operationInfo);
    }

    @Override
    public int hashCode() {
        int result = offeredById.hashCode();
        result = 31 * result + foaTime.hashCode();
        result = 31 * result + operationInfo.hashCode();
        return result;
    }


}
