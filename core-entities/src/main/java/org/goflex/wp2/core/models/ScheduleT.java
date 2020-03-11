
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
 *  Last Modified 2/22/18 3:04 AM
 */

package org.goflex.wp2.core.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Created by bijay on 12/2/17.
 * The table to store schedule details
 */
@Entity
@Table(name = "scheduleTable")
public class ScheduleT implements Serializable {

    private static final long serialVersionUID = 6410946071190455958L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long scheduleId;
    private Date scheduleDate;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "scheduleTable", fetch = FetchType.EAGER)
    @JsonManagedReference
    private Set<OnOffSchedule> onOffSchedules = new HashSet<>();

    public ScheduleT() {

    }


    public ScheduleT(Date date) {
        this.scheduleDate = date;

    }

    public long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public Set<OnOffSchedule> getOnOffSchedules() {
        return onOffSchedules;
    }

    public void setOnOffSchedules(Set<OnOffSchedule> onOffSchedules) {
        this.onOffSchedules = onOffSchedules;
    }

    public Date getScheduleDate() {
        return scheduleDate;
    }

    public void setScheduleDate(Date scheduleDate) {
        this.scheduleDate = scheduleDate;
    }

    public void addOnOffSchedule(OnOffSchedule schedule) {
        schedule.setScheduleTable(this);
        this.onOffSchedules.add(schedule);
    }


    public void removeSchedule(OnOffSchedule schedule) {
        this.onOffSchedules.remove(schedule);
        if (schedule != null) {
            schedule.setScheduleTable(null);
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScheduleT)) return false;
        ScheduleT scheduleT = (ScheduleT) o;
        return scheduleId == scheduleT.scheduleId &&
                Objects.equals(scheduleDate, scheduleT.scheduleDate) &&
                Objects.equals(onOffSchedules, scheduleT.onOffSchedules);
    }

}
