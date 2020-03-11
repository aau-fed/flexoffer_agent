package org.goflex.wp2.core.repository;

import org.goflex.wp2.core.models.GroupingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupingDetailRepository extends JpaRepository<GroupingDetail, Long> {
    GroupingDetail findByGroupName(String groupName);

    GroupingDetail findByGroupId(long groupId);
}
