package org.goflex.wp2.core.repository;

import org.goflex.wp2.core.models.FoaKPI;
import org.goflex.wp2.core.wrappers.KPIWrapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface FoaKPIRepository extends JpaRepository<FoaKPI, Long> {

    @Query("SELECT new org.goflex.wp2.core.wrappers.KPIWrapper(min(f.dateofAggregation), sum(f.activeDevice) , " +
            "sum(f.activeUser), avg(f.flexibilityRatio), sum(f.foCount)) FROM FoaKPI f where " +
            "f.organizationId = :organizationId and f.userId = :userId and (f.dateofAggregation >= :StartTime and f.dateofAggregation < :endTime)" +
            "group by f.dateofAggregation order by f.dateofAggregation desc ")
    List<KPIWrapper> findLast2KPIForUser(@Param("organizationId") long organizationId,
                                         @Param("userId") long userId,
                                         @Param("StartTime") Date StartTime,
                                         @Param("endTime") Date endTime);


    @Query("SELECT new org.goflex.wp2.core.wrappers.KPIWrapper(min(f.dateofAggregation), sum(f.activeDevice) , " +
            "sum(f.activeUser), avg(f.flexibilityRatio), sum(f.foCount)) FROM FoaKPI f where " +
            "f.organizationId = :organizationId and (f.dateofAggregation >= :StartTime and f.dateofAggregation < :endTime)" +
            "group by f.dateofAggregation order by f.dateofAggregation desc ")
    List<KPIWrapper> findLast2KPIforOrganization(@Param("organizationId") long organizationId,
                                                 @Param("StartTime") Date StartTime,
                                                 @Param("endTime") Date endTime);

    List<FoaKPI> findAllByUserIdAndDateofAggregation(long userId, Date dateofAggregation);


    @Query("SELECT new org.goflex.wp2.core.wrappers.KPIWrapper(f.dateofAggregation, f.activeDevice, " +
            "f.activeUser, f.flexibilityRatio, f.foCount) FROM FoaKPI f  where " +
            "f.organizationId = :organizationId and f.userId = :userId and f.dateofAggregation = :StartTime " +
            "order by f.dateofAggregation desc")
    KPIWrapper findKPIForUser(@Param("organizationId") long organizationId,
                                    @Param("userId") long userId,
                                    @Param("StartTime") Date StartTime);

    @Query("SELECT new org.goflex.wp2.core.wrappers.KPIWrapper(min(f.dateofAggregation), sum(f.activeDevice) , " +
            "sum(f.activeUser), avg(f.flexibilityRatio), sum(f.foCount)) FROM FoaKPI f where " +
            "f.organizationId = :organizationId and f.dateofAggregation = :StartTime " +
            "group by f.dateofAggregation order by f.dateofAggregation desc ")
    KPIWrapper findKPIforOrganization(@Param("organizationId") long organizationId,
                                                 @Param("StartTime") Date StartTime);


}
