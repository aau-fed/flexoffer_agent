package org.goflex.wp2.fmanproxy.user;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;


    @PostMapping(value = "/login", produces = "application/json")
    @ApiOperation(value = "${UserController.login}")
    @ApiResponses(value = {//
            @ApiResponse(code = 400, message = "Something went wrong"), //
            @ApiResponse(code = 422, message = "Invalid username/password supplied")})
    public Map login(@ApiParam("User login") @RequestBody UserT login) throws Exception {
        String token = userService.signin(login.getUserName(), login.getPassword());
        UserT user = userService.getUserByUserName(login.getUserName());
        if (user.getRole() != UserRole.ROLE_ADMIN && user.getRole() != UserRole.ROLE_BROKER) {
            throw new Exception("Only admin or broker users are currently allowed to signin");
        }
        return ImmutableMap.of("token", token,
                "user", user);
    }


    @PostMapping(value = "/refreshToken", produces = "application/json")
    @ApiOperation(value = "${UserController.refreshToken}")
    @ApiResponses(value = {//
            @ApiResponse(code = 400, message = "Something went wrong"), //
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public Map refreshToken(@ApiParam("User Refresh Token") @RequestBody UserT userT) throws Exception {
        String token = userService.refreshToken(userT.getUsername());
        UserT user = userService.getUserByUserName(userT.getUserName());
        if (user.getRole() != UserRole.ROLE_ADMIN && user.getRole() != UserRole.ROLE_BROKER) {
            throw new Exception("Only admin or broker users are currently allowed to refresh tokens");
        }
        return ImmutableMap.of("token", token,
                "user", user);
    }

    @PostMapping(value = "/register", produces = "application/json")
    @ApiOperation(value = "${UserController.register}")
    @ApiResponses(value = {//
            @ApiResponse(code = 400, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 422, message = "Username is already in use"), //
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public Map register(@ApiParam("Register User") @RequestBody UserT user) {
        return ImmutableMap.of("token", userService.signup(user),
                "user", userService.getUserByUserName(user.getUserName()));
    }


    @GetMapping(value = "/me")
    @ApiOperation(value = "${UserController.whoami}", response = UserT.class)
    @ApiResponses(value = {//
            @ApiResponse(code = 400, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public UserT whoami(HttpServletRequest req) {
        return userService.whoami(req);
    }


    @GetMapping(value = "/roles")
    @ApiOperation(value = "${UserController.roles}", response = UserT.class)
    @ApiResponses(value = {//
            @ApiResponse(code = 400, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public List<String> roles(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList());
    }


    @DeleteMapping(value = "/{username}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "${UserController.delete}")
    @ApiResponses(value = {//
            @ApiResponse(code = 400, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "The user doesn't exist"), //
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public String delete(@ApiParam("Username") @PathVariable String username) {
        userService.delete(username);
        return username;
    }


    @PutMapping(value = "/{username}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "${UserController.save}")
    @ApiResponses(value = {//
            @ApiResponse(code = 400, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 422, message = "Username is already in use"), //
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public UserT save(@ApiParam("User being updated") @RequestBody UserT user) {
        return userService.update(user);
    }


    @GetMapping(value = "/contract/{userName}")
    @ApiOperation(value = "${UserController.contract}")
    @ApiResponses(value = {//
            @ApiResponse(code = 400, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public ResponseEntity<String> contract(@PathVariable String userName) {
        UserT user = userService.getUserByUserName(userName);
        if (user != null) {
            //TODO
            //return userContactService.getContract(user.getUserId());}
        }
        return null;
    }


    @GetMapping(value = "/{userName}")
    @PreAuthorize("hasRole('ROLE_ADMIN') || hasRole('ROLE_BROKER')")
    @ApiOperation(value = "${UserController.getUserByUserName}")
    public UserT getUserByUserName(@PathVariable String userName) {
        UserT user = userService.getUserByUserName(userName);
        return userService.getUserByUserName(userName);
    }

    @GetMapping(value = "/")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ApiOperation(value = "${UserController.getAllUser}")
    public @ResponseBody
    List<UserT> getAllUser() {
        List<UserT> users = userService.getAllUsers();
        return users;
    }

}
