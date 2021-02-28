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
 *  Last Modified 2/21/18 8:09 PM
 */

package org.goflex.wp2.fmanproxy.user;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

/**
 * Created by bijay on 12/2/17.
 */
@Repository
@Transactional
public interface UserRepository extends JpaRepository<UserT, Long> {
    /** Find user by userID*/
    public UserT findByUserId(Long id);

    /** Find  user by Username */
    public UserT findByUserName(String userName);

    /** Find  user by Username */
    @Query("SELECT t.userId FROM UserT t WHERE t.userName = :userName")
    public long getUserIdByUserName(String userName);

    /** Find  user by Username and Password*/
    @Query("SELECT t FROM UserT t WHERE t.userName = :userName AND t.password = :password")
    public UserT findByUserNameAndPassword(@Param("userName") String userName,
                                           @Param("password") String password);



    @Transactional
    void deleteByUserName(String userName);

}
