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
 *  Last Modified 2/8/18 10:33 PM
 */

package org.goflex.wp2.foa.interfaces;

import org.goflex.wp2.core.entities.MessageCode;
import org.goflex.wp2.core.models.UserMessage;

import java.util.List;


/**
 * All message to push to user
 */
public interface UserMessageService {


    UserMessage save(UserMessage message);

    List<UserMessage> getAllMessages(long organizationId, String username);

    List<UserMessage> getAllMessages(long organizationId);

    List<UserMessage> getLatestMessages(long organizationId, String username);

    List<UserMessage> getLatestMessages(String userName);

    List<UserMessage> getAllMessagesAdmin(List<String> userNames);

    List<UserMessage> getLatestMessages(long organizationId);

    void UpdateStatusMessages(String userName);

    void UpdateStatusMessages();

    boolean similarMessageExists(String deviceId, String userName, MessageCode messageCode);

    void deleteAllMessages(long organizationId);

    void deleteAllMessages(long organizationId, String userName);


}
