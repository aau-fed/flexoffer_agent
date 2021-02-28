package org.goflex.wp2.fmanproxy.security;

import org.goflex.wp2.fmanproxy.user.UserRepository;
import org.goflex.wp2.fmanproxy.user.UserT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  @Autowired
  private UserRepository userRepository;

  @Transactional()
  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    final UserT user = userRepository.findByUserName(username);

    if (user == null) {
      throw new UsernameNotFoundException("User '" + username + "' not found");
    }

    return user;

//            org.springframework.security.core.userdetails.User//
//        .withUsername(username)//
//        .password(user.getPassword())//
//        .authorities(user.getRole().toString())//
//        .accountExpired(false)//
//        .accountLocked(false)//
//        .credentialsExpired(false)//
//        .disabled(!user.isEnabled())//
//        .build();
  }

}
