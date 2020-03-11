package org.goflex.wp2.core.repository;

import org.goflex.wp2.core.models.DeviceHierarchy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DeviceHierarchyRepository extends JpaRepository<DeviceHierarchy, Long> {

    DeviceHierarchy findByHierarchyName(String hierarchyName);

    DeviceHierarchy findByHierarchyId(Long hierarchyId);

    void deleteByHierarchyName(String hierarchyName);

    List<DeviceHierarchy> findAllByUserId(@Param("userId") Long userId);

    DeviceHierarchy findByUserId(@Param("userId") Long userId);

    @Query("SELECT h FROM DeviceHierarchy h " +
            "WHERE h.userId IN (SELECT u.userId from UserT u WHERE u.organizationId = :organizationId)")
    List<DeviceHierarchy> findAllByOrganization(@Param("organizationId") Long organizationId);
}
