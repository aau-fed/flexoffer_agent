package org.goflex.wp2.fmanproxy.fmaninstance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author muhaftab
 * created: 01/11/18
 */
@Repository
public interface FmanInstanceRepository extends JpaRepository<FmanInstanceT, Long> {

    FmanInstanceT findFmanInstanceTByInstanceName(String instanceName);

    FmanInstanceT findFmanInstanceTByInstanceUrl(String instanceUrl);
}
