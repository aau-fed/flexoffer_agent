
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
 *  Last Modified 2/8/18 10:45 PM
 */

package org.goflex.wp2.foa.implementation;


import org.goflex.wp2.core.entities.MessageCode;
import org.goflex.wp2.core.models.UserMessage;
import org.goflex.wp2.core.models.UserT;
import org.goflex.wp2.core.repository.UserMessageRepository;
import org.goflex.wp2.foa.interfaces.UserMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by bijay on 12/2/17.
 * this class implements the usermessageservice methods
 */
@Service
public class UserMessageServiceImpl implements UserMessageService {

    private static final Logger logger = LoggerFactory.getLogger(UserMessageServiceImpl.class);

    private final UserMessageRepository userMessageRepository;

    @Autowired
    public UserMessageServiceImpl(UserMessageRepository userMessageRepository) {
        this.userMessageRepository = userMessageRepository;
    }

    @Override
    public boolean similarMessageExists(String deviceId, String userName, MessageCode messageCode) {
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();
        return this.userMessageRepository.findSimilarMessages(userName, messageCode, yesterday) > 0 ? true : false;
    }

    @Override
    public UserMessage save(UserMessage message) {
        return userMessageRepository.saveAndFlush(message);

    }

    @Override
    public List<UserMessage> getLatestMessages(long organizationId, String userName) {
        return userMessageRepository.findUndeliveredMessage(userName);
    }

    @Override
    public List<UserMessage> getLatestMessages(String userName) {
        return userMessageRepository.findAdminUndeliveredMessage(userName);
    }


    @Override
    public List<UserMessage> getLatestMessages(long organizationId) {
        return userMessageRepository.findUndeliveredMessage();

    }


    @Override
    public void deleteAllMessages(long organizationId) {
        //userMessageRepository.deleteForOrganization(organizationId);

    }

    @Override
    public void deleteAllMessages(long organizationId, String userName) {
        userMessageRepository.deleteAllByUserName(userName);

    }


    @Override
    @Transactional
    public void UpdateStatusMessages(String userName) {
        userMessageRepository.updateStatus(userName);
    }

    @Override
    @Transactional
    public void UpdateStatusMessages() {
        userMessageRepository.updateStatus();
    }

    @Override
    public List<UserMessage> getAllMessages(long organizationId, String userName) {
        return userMessageRepository.findByUserName(userName);

    }

    @Override
    public List<UserMessage> getAllMessagesAdmin(List<String> userNames) {
        return userMessageRepository.findAllByUserNameIn(userNames);

    }

    @Override
    public List<UserMessage> getAllMessages(long organizationId) {
        return userMessageRepository.findAll();

    }


    /**
     * Added for testing
     */
    //@Scheduled(fixedRate = 10000)
    public void storeTestMessage() {
        UserMessage message = new UserMessage();
        message.setMessage("Test message");
        message.setMessageCode(MessageCode.IncorrectCredential);
        message.setDeviceID("mock-device-id");
        message.setUserName("admin");
        message.setMessageStatus(0);
        message.setMessageDate(new Date());
        this.save(message);
    }
}
