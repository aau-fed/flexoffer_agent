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
 *  Last Modified 8/26/17 7:42 PM
 */

package org.goflex.wp2.foa.events;

import org.springframework.context.ApplicationEvent;

/**
 * Created by Ivan Bizaca on 19/08/2017.
 */
public class FoaHeartbeatEvent extends ApplicationEvent {
    public String securityToken = "eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJGTUFORk1BUi5pbyIsImV4cCI6MTUwNDAwNzY4NCwiaWF0IjoxNTAzMTQzNjg0MDA1LCJUaGVTZWNyZXRLZXlGb3JGT0EiOiJhZG1pbiJ9.GBumKt6WjyYCV_SGyaB7bPzUBnTM9o0iNUdJN5jl4bkBlkUYLy62u6DXVBgYSRE1FNNLPpMJP9O6o8_s-o92SQ";

    public FoaHeartbeatEvent(Object source) {
        super(source);
    }

}
