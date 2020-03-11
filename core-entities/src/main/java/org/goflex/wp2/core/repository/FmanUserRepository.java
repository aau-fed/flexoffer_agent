package org.goflex.wp2.core.repository;

import org.goflex.wp2.core.models.FmanUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface FmanUserRepository extends JpaRepository<FmanUser, Long> {

    FmanUser findByOrganizationId(long organizationID);

    List<FmanUser> findByFoaUserName(String foaUserName);

    FmanUser findByUserName(String userName);


}