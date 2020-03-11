
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
 *  Last Modified 2/2/18 2:27 PM
 */

package org.goflex.wp2.foa.listeners;


import org.goflex.wp2.foa.events.ContractUpdateEvent;
import org.goflex.wp2.foa.implementation.ContactServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Created by Ivan Bizaca on 21/07/2017.
 */

@Component
public class ContractUpdateListener implements ApplicationListener<ContractUpdateEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ContractUpdateListener.class);

    @Autowired
    private Environment env;

    @Autowired
    private ContactServiceImpl contactService;


    @Override
    public void onApplicationEvent(ContractUpdateEvent event) {
        if (contactService.getContract(event.getContract().getUserName()) != null) {
            contactService.inValidateContract(event.getContract().getUserName());
            contactService.saveContract(event.getContract());
        } else {
            contactService.saveContract(event.getContract());
        }

    }
}
