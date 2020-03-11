
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
 *  Last Modified 1/13/18 12:05 PM
 */

package org.goflex.wp2.core.models;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * This class is used for API security to validate user
 * Created by bijay on 12/11/17.
 */
public class FOAUserPrincipal implements UserDetails {

    private final List<GrantedAuthority> authorities;
    private UserT user;


    public FOAUserPrincipal(UserT user) {
        this.user = user;
        this.authorities = Arrays.asList(user.getRole());
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //final List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        return this.authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    //

    public UserT getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "FOAUserPrincipal{" +
                "authorities=" + authorities +
                ", user=" + user +
                '}';
    }
}
