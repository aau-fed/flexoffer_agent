package org.goflex.wp2.core.wrappers;

/*
 * Created by Ivan Bizaca on 13/07/2017.
 */

/* FLEX OFFER MESSAGE FROM FOA TO FMAN */


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;


@XmlAccessorType(XmlAccessType.FIELD)
public class IneaHeatBeatRequestWrapper {

    private IneaHeartBeatMessage heartbeat;


    public IneaHeartBeatMessage getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(IneaHeartBeatMessage heartbeat) {
        this.heartbeat = heartbeat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IneaHeatBeatRequestWrapper)) return false;

        IneaHeatBeatRequestWrapper that = (IneaHeatBeatRequestWrapper) o;

        return heartbeat.equals(that.heartbeat);
    }

    @Override
    public int hashCode() {
        return heartbeat.hashCode();
    }

    @Override
    public String toString() {
        return "IneaHeatBeatRequestWrapper{" +
                "heartbeat=" + heartbeat +
                '}';
    }
}
