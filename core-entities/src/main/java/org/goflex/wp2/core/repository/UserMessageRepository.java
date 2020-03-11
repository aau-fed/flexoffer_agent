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
 *  Last Modified 2/8/18 9:00 PM
 */

package org.goflex.wp2.core.repository;

import org.goflex.wp2.core.entities.MessageCode;
import org.goflex.wp2.core.models.UserMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;


/**
 * Created by bijay on 11/27/17.
 * This repository stores all message to user
 */
@Repository
@Transactional
public interface UserMessageRepository extends JpaRepository<UserMessage, Long> {

    /**
     * Find  all messages
     */
    @Query("SELECT m FROM UserMessage m WHERE m.userName = :userName")
    List<UserMessage> findByUserName(@Param("userName") String userName);

    /**
     * Find  all messages
     */
    //@Query("SELECT m FROM UserMessage m WHERE m.userName in :userNames")
    List<UserMessage> findAllByUserNameIn(List<String> userNames);


    /**
     * Find latest message
     */
    //@Query("SELECT m FROM UserMessage m WHERE m.userName = :userName")
    UserMessage findTop1ByUserName(String userName);

    @Query("SELECT m FROM UserMessage m WHERE m.messageStatus = 0 and m.userName = :userName")
    List<UserMessage> findUndeliveredMessage(@Param("userName") String userName);

    @Query("SELECT m FROM UserMessage m WHERE m.messageStatus = 0 and m.userName = :userName and m.notifiedToAdmin = 0")
    List<UserMessage> findAdminUndeliveredMessage(@Param("userName") String userName);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserMessage m set m.messageStatus = 1 where m.userName = :userName")
    void updateStatus(@Param("userName") String userName);

    @Modifying(clearAutomatically = true)
        //@Query("UPDATE UserMessage m set m.messageStatus = 1 where m.userName = :userName")
    void deleteAllByUserName(@Param("userName") String userName);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserMessage m set m.messageStatus = 1")
    void updateStatus();


    @Query("SELECT m FROM UserMessage m where m.messageStatus = 0")
    List<UserMessage> findUndeliveredMessage();

    /**
     * Find count of all active user
     */
    @Query("SELECT count(m) FROM UserMessage m where userName =:userName and messageCode=:messageCode and " +
            "m.messageDate > :yesterday")
    int findSimilarMessages(@Param("userName") String userName, @Param("messageCode") MessageCode messageCode, @Param("yesterday") Date yesterday);


}
