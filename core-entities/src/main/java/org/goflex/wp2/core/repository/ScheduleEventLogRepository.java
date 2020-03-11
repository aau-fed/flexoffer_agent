package org.goflex.wp2.core.repository;

import org.goflex.wp2.core.models.ScheduleEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author muhaftab
 * created: 2/12/19
 */
@Repository
public interface ScheduleEventLogRepository extends JpaRepository<ScheduleEventLog, Long> {
}
