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
 *  Last Modified 2/2/18 4:13 PM
 */

package org.goflex.wp2.foa.implementation;


import org.goflex.wp2.core.models.FOAUserPrincipal;
import org.goflex.wp2.core.models.UserT;
import org.goflex.wp2.core.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * This class is used for API security to load all the available users
 * Created by bijay on 12/11/17.
 */
@Service
public class FOAUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) {
        UserT user = userRepository.findByUserName(username);

        //GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().toString());

        if (user == null) {
            throw new UsernameNotFoundException("User '" + username + "' not found");
        }

        if(!user.isEnabled()){
            throw new UsernameNotFoundException("User '" + username + "' is disabled");
        }

        return new FOAUserPrincipal(user);

        //default userDetail implementation of SpringBoot
        //UserDetails userDetails = new User(user.getUserName(), user.getPassword(), Arrays.asList(authority));
        //return userDetails;
    }
}
