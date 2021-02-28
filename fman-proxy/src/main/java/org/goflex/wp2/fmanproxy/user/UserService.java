package org.goflex.wp2.fmanproxy.user;

import org.goflex.wp2.fmanproxy.common.exception.CustomException;
import org.goflex.wp2.fmanproxy.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * Created by bijay on 4/3/18.
 */
@Service
public class UserService  {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private AuthenticationManager authenticationManager;

    public UserT getUserByUserName(String userName){
        return userRepository.findByUserName(userName);
    }

    public long getUserIdByUserName(String userName) {
        return userRepository.getUserIdByUserName(userName);
    }

    public UserT getUser(String userName, String password){
        UserT user = userRepository.findByUserNameAndPassword(userName, password);
        return user;
    }

    private boolean isMeAdmin() {
        Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
        return currentUser.getAuthorities().stream().anyMatch(r -> r == UserRole.ROLE_ADMIN);
    }

    public String signup(UserT user) {
        if (userRepository.findByUserName(user.getUserName()) == null) {

            /* Check whether we can set this role*/
            if (user.getRole() == UserRole.ROLE_ADMIN) {
                if (!isMeAdmin()) {
                    throw new CustomException("Only admin users are allowed to create admin users.", HttpStatus.FORBIDDEN);
                }
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            return jwtTokenProvider.createToken(user.getUserName(), Arrays.asList(user.getRole()));
        } else {
            throw new CustomException("Username is already in use", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public UserT update(UserT user)
    {
        Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();

        UserT oldUser = userRepository.findByUserId(user.getUserId());
        if (oldUser == null) {
            throw new CustomException("User not found.", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if (!oldUser.getUserName().equals(user.getUserName())){
            throw new CustomException("Username cannot be changed.", HttpStatus.FORBIDDEN);
        }

        // Update the password
        if(!user.getPassword().equals(oldUser.getPassword())){
            oldUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Update location
        if (oldUser.getLocation().getLatitude() != user.getLocation().getLatitude() ||
            oldUser.getLocation().getLongitude() != user.getLocation().getLongitude()) {
            oldUser.setLocation(user.getLocation());
        }

        if (isMeAdmin()) {
            userRepository.save(user);
            return user;
        } else {
            /* The user is only allowed to change password, location, and the following: */
            oldUser.setFirstName(user.getFirstName());
            oldUser.setLastName(user.getLastName());
            oldUser.setEmail(user.getEmail());
            userRepository.save(oldUser);
            return oldUser;
        }
    }

    public String signin(String username, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            return jwtTokenProvider.createToken(username, Arrays.asList(userRepository.findByUserName(username).getRole()));
        } catch (AuthenticationException e) {
            throw new CustomException("Invalid username/password supplied", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public String refreshToken(String username) {
        try {
            return jwtTokenProvider.createToken(username, Arrays.asList(userRepository.findByUserName(username).getRole()));
        } catch (AuthenticationException e) {
            throw new CustomException("Failed to refresh token (authentication failure)", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public void delete(String username) {
        userRepository.deleteByUserName(username);
    }

    public List<UserT> getAllUsers(){

        return userRepository.findAll();
        /*Authentication currentUser = SecurityContextHolder.getContext().getAuthentication();
        *//**return all user if admin as return only current user*//*
        if(isMeAdmin()) {
            return userRepository.findAll();
        }else{
            List<UserT> user = new ArrayList<>();
            user.add(userRepository.findByUserName(currentUser.getName()));
            return user;
        }*/
    }

    public UserT whoami(HttpServletRequest req) {
        return userRepository.findByUserName(jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(req)));
    }

    public long getUserCount() {
        return userRepository.count();
    }

}
