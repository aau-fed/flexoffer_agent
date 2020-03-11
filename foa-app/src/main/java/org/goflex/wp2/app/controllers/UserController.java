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
 *  Last Modified 2/21/18 11:21 PM
 */

package org.goflex.wp2.app.controllers;


import org.goflex.wp2.app.common.CustomException;
import org.goflex.wp2.app.fmanintegration.listener.event.UserCreatedEvent;
import org.goflex.wp2.app.fmanintegration.user.FmanUserService;
import org.goflex.wp2.app.security.TokenGenerator;
import org.goflex.wp2.core.entities.DeviceParameters;
import org.goflex.wp2.core.entities.OrganizationLoadControlState;
import org.goflex.wp2.core.entities.ResponseMessage;
import org.goflex.wp2.core.entities.UserRole;
import org.goflex.wp2.core.models.Organization;
import org.goflex.wp2.core.models.UserAddress;
import org.goflex.wp2.core.models.UserMessage;
import org.goflex.wp2.core.models.UserT;
import org.goflex.wp2.core.repository.OrganizationRepository;
import org.goflex.wp2.foa.implementation.TpLinkDeviceService;
import org.goflex.wp2.foa.interfaces.UserMessageService;
import org.goflex.wp2.foa.interfaces.UserService;
import org.goflex.wp2.foa.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by bijay on 12/6/17.
 */

@RestController
@RequestMapping("/api/v1.0/prosumer")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    @Value("${cloud.api.url}")
    private String cloudApiUrl;

    private UserService userService;
    private TpLinkDeviceService tpLinkDeviceService;
    private PasswordEncoder passwordEncoder;
    private UserMessageService userMessageService;
    private OrganizationRepository organizationRepository;
    private EmailService emailService;
    private ApplicationEventPublisher applicationEventPublisher;
    private FmanUserService fmanUserService;


    public UserController() {
    }

    @Autowired
    public UserController(
            UserService userService,
            TpLinkDeviceService tpLinkDeviceService,
            PasswordEncoder passwordEncoder,
            UserMessageService userMessageService,
            OrganizationRepository organizationRepository,
            EmailService emailService,
            ApplicationEventPublisher applicationEventPublisher,
            FmanUserService fmanUserService
    ) {
        this.userService = userService;
        this.tpLinkDeviceService = tpLinkDeviceService;
        this.passwordEncoder = passwordEncoder;
        this.userMessageService = userMessageService;
        this.organizationRepository = organizationRepository;
        this.emailService = emailService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.fmanUserService = fmanUserService;
    }


    private String getSessionUser(Authentication authentication) {
        return authentication.getName();
    }

    private ResponseEntity<ResponseMessage> errorResponse(String msg, HttpStatus status) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus(status);
        responseMessage.setMessage(msg);
        return new ResponseEntity<>(responseMessage, status);
    }

    private ResponseEntity<ResponseMessage> successResponse(String msg) {
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus(HttpStatus.OK);
        responseMessage.setMessage(msg);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    /* returns URI to register FOA user*/
    @RequestMapping(value = "/userNameExists", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> doesUserNameExist(@RequestBody UserT user) {
        try {

            if (StringUtils.isEmpty(user.getUserName())) {
                return errorResponse("username can not be empty", HttpStatus.BAD_REQUEST);
            }

            if (userService.getUser(user.getUserName()) != null) {
                return errorResponse("Username already exists", HttpStatus.CONFLICT);
            }


            ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setStatus(HttpStatus.OK);
            responseMessage.setMessage("Success: username available!");

            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return errorResponse(ex.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/tpLinkAccountExists")
    public ResponseEntity<ResponseMessage> doesTpLinkAccountExist(@RequestBody UserT user) {
        try {
            if (StringUtils.isEmpty(user.getTpLinkUserName())) {
                return errorResponse("Must provide TpLink account email", HttpStatus.CONFLICT);
            }

            if (userService.getUserByTpLinkUserName(user.getTpLinkUserName()) != null) {
                return errorResponse("TpLink account already exists!", HttpStatus.CONFLICT);
            }

            ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setStatus(HttpStatus.OK);
            responseMessage.setMessage("Success: TpLink account available!");
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } catch (Exception ex) {
            return errorResponse(ex.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*URI to register FOA user*/
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> prosumerRegistration(@RequestBody UserT user) {
        try {

            if (StringUtils.isEmpty(user.getUserName()) || StringUtils.isEmpty(user.getPassword()) || StringUtils.isEmpty(user.getOrganizationId())) {
                //|| StringUtils.isEmpty(user.getEmail()) || StringUtils.isEmpty(user.getOrganizationId())) {
                return errorResponse("username, password, email, and organizationId fields can not be empty", HttpStatus.BAD_REQUEST);
            }

            if (userService.getUser(user.getUserName()) != null) {
                return errorResponse("Username already exists", HttpStatus.CONFLICT);
            }

            if (!StringUtils.isEmpty(user.getEmail())) {
                if (userService.getUserByEmail(user.getEmail()) != null) {
                    return errorResponse("Another user is already registered with the provided email", HttpStatus.CONFLICT);
                }
            }

            Organization org = organizationRepository.findByOrganizationId(user.getOrganizationId());

            if (org == null) {
                return errorResponse(String.format("Organization: %s does not exists", user.getOrganizationId()), HttpStatus.UNPROCESSABLE_ENTITY);
            }

            String tpLinkUserName = null;
            String tpLinkPassword = null;
            DeviceParameters deviceParameters = new DeviceParameters("", "", "", "");

            if (!StringUtils.isEmpty(user.getTpLinkUserName()) && !StringUtils.isEmpty(user.getTpLinkPassword())) {
                tpLinkUserName = user.getTpLinkUserName();
                tpLinkPassword = user.getTpLinkPassword();
                if (userService.tpLinkUserExist(user.getTpLinkUserName())) {
                    return errorResponse(String.format("TpLinkUserName: %s already exists", user.getTpLinkUserName()), HttpStatus.CONFLICT);
                }
                deviceParameters.setCloudUserName(user.getTpLinkUserName());
                deviceParameters.setCloudPassword(user.getTpLinkPassword());
                deviceParameters.setCloudAPIUrl(cloudApiUrl);
            }

            String password = user.getPassword();
            user.setPassword(passwordEncoder.encode(password));

            if (user.getUserAddress() == null) {
                user.setUserAddress(new UserAddress());
            }

            if (user.getRole() == null) {
                user.setRole(UserRole.ROLE_PROSUMER);
            }

            user.setRegistrationDate(new Date());
            user.setTpLinkUserName(tpLinkUserName);
            user.setTpLinkPassword(tpLinkPassword);
            UserT registeredUser = userService.save(user);

            tpLinkDeviceService.setTpLinkDevices(registeredUser, deviceParameters);

            ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setStatus(HttpStatus.OK);
            responseMessage.setData(registeredUser);
            responseMessage.setMessage("Success");

            UserCreatedEvent userCreatedEvent = new UserCreatedEvent(registeredUser,
                    org.getOrganizationName(), password);
            applicationEventPublisher.publishEvent(userCreatedEvent);

            return new ResponseEntity<>(responseMessage, HttpStatus.CREATED);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            return errorResponse(ex.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * send a randomly generated new password to the supplied email
     *
     * @return
     */
    @RequestMapping(value = "/forgotPassword", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> emailNewPassword(@RequestParam(value = "email") String email) {

        if (userService.getUserByEmail(email) == null) {
            return errorResponse("No user registered with the provided email", HttpStatus.CONFLICT);
        }

        // generate random password
        int len = 7;
        String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        String randomPwd = sb.toString();

        // set user's password to the newly generated random password
        UserT user = userService.getUserByEmail(email);
        user.setPassword(passwordEncoder.encode(randomPwd));
        userService.save(user);

        // also email it to the user
        String msgBody = "Your new password is '" + randomPwd + "'. You can login using this password, then change it to your preferred password in profile settings.";
        emailService.sendSimpleMessage(email, "New Password", msgBody);

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setStatus(HttpStatus.OK);
        responseMessage.setMessage("Successfully sent new password. Please check your email");
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }


    /**
     * URI to login FOA user
     */
    @CrossOrigin
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> prosumerLogin(@RequestBody UserT user) {
        /** get user with given username */
        UserT isValidUser = userService.getUser(user.getUserName(), user.getPassword());
        /**Check if provided password is valid, compare with encryption */
        if (isValidUser != null) {
            if (!passwordEncoder.matches(user.getPassword(), isValidUser.getPassword())) {
                isValidUser = null;
            }
        }

        ResponseMessage responseMessage = new ResponseMessage();
        if (!StringUtils.isEmpty(user.getUserName()) || !StringUtils.isEmpty(user.getPassword())) {
            if (isValidUser != null) {
                userService.registerLoginTime(isValidUser);
                Map<String, Object> loggedInUser = new HashMap<>();
                loggedInUser.put("user", isValidUser.getUserName());
                loggedInUser.put("role", isValidUser.getRole());
                loggedInUser.put("organizationId", isValidUser.getOrganizationId());
                loggedInUser.put("pic", isValidUser.getPic());
                responseMessage.setData(loggedInUser);
                responseMessage.setMessage("success");
                responseMessage.setStatus(HttpStatus.OK);
                return new ResponseEntity<>(responseMessage, HttpStatus.OK);
            } else {
                return errorResponse("Invalid User, either username or password is wrong", HttpStatus.UNAUTHORIZED);
            }
        } else {
            return errorResponse("UserName or Password field cannot be empty", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * URI to login FOA user
     */
    @CrossOrigin
    @RequestMapping(value = "/refreshToken", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> refreshToken() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        ResponseMessage responseMessage = new ResponseMessage();

        TokenGenerator tokenGenerator = new TokenGenerator();
        String JWT = tokenGenerator.getToken(authentication.getName(), authentication.getAuthorities());
        Map<String, String> res = new HashMap<>();
        res.put("token", JWT);
        responseMessage.setData(res);
        responseMessage.setStatus(HttpStatus.OK);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);

    }


    /**
     * URI to update FOA user
     */
    @RequestMapping(value = "/updateUser", method = RequestMethod.POST)
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> updateUser(@RequestBody UserT receivedUsr) {
        String message;
        String sessionUser;
        ResponseMessage statusMsg = new ResponseMessage();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Invalid User", HttpStatus.UNAUTHORIZED);
        }

        sessionUser = authentication.getName();

        UserT exisitingUsr = userService.getUser(sessionUser);

        if (exisitingUsr == null) {
            return errorResponse(String.format("User: %s not found", sessionUser), HttpStatus.UNAUTHORIZED);
        }

        if (!exisitingUsr.isEnabled()) {
            return errorResponse(String.format("User: %s is disabled", sessionUser), HttpStatus.UNAUTHORIZED);
        }


        Map<String, Object> payload = new HashMap<>();
        //check tplink username is null or not
        if (!StringUtils.isEmpty(receivedUsr.getTpLinkUserName())) {

            //check if existing username is empty
            if (StringUtils.isEmpty(exisitingUsr.getTpLinkUserName())) {
                //check if username already exist
                if (userService.tpLinkUserExist(receivedUsr.getTpLinkUserName())) {
                    return errorResponse(String.format("TpLinkUserName: %s already exists", receivedUsr.getTpLinkUserName()), HttpStatus.UNPROCESSABLE_ENTITY);
                } else {
                    payload.put("tpLinkUserName", receivedUsr.getTpLinkUserName());
                }
            } else {
                //if user name is changed
                if (!receivedUsr.getTpLinkUserName().equals(exisitingUsr.getTpLinkUserName())) {
                    if (userService.tpLinkUserExist(receivedUsr.getTpLinkUserName())) {
                        return errorResponse(String.format("TpLinkUserName: %s already exists", receivedUsr.getTpLinkUserName()), HttpStatus.UNPROCESSABLE_ENTITY);
                    } else {
                        payload.put("tpLinkUserName", receivedUsr.getTpLinkUserName());
                    }
                }

            }
        } else {
            //if tplinkusername exist and new name null
            if (!StringUtils.isEmpty(exisitingUsr.getTpLinkUserName())) {
                payload.put("tpLinkUserName", null);
                payload.put("tpLinkPassword", null);
            }
        }



        if (!StringUtils.isEmpty(receivedUsr.getTpLinkPassword())) {
            //if username is empty then set password to null
            if (receivedUsr.getTpLinkUserName() != null) {
                if (receivedUsr.getTpLinkUserName().equals("")) {
                    payload.put("tpLinkPassword", null);
                } else {
                    payload.put("tpLinkPassword", receivedUsr.getTpLinkPassword());
                }
            } else {
                payload.put("tpLinkPassword", null);
            }

        }


        if (!StringUtils.isEmpty(receivedUsr.getPassword())) {
            payload.put("password", passwordEncoder.encode(receivedUsr.getPassword()));
        }

        if (!StringUtils.isEmpty(receivedUsr.getEmail())) {
            if (exisitingUsr != null && !exisitingUsr.getUserName().equals(receivedUsr.getUserName())) {
                return errorResponse("Another user is already registered with the provided email", HttpStatus.CONFLICT);
            }
            payload.put("email", receivedUsr.getEmail());
        }

        Organization org = organizationRepository.findByOrganizationId(receivedUsr.getOrganizationId());
        if (org != null) {
            payload.put("organizationId", receivedUsr.getOrganizationId());
        }

        if (receivedUsr.getUserAddress() != null) {
            payload.put("address", receivedUsr.getUserAddress());
        }


        if (Boolean.valueOf(receivedUsr.isEnabled()) != null) {
            payload.put("enabled", receivedUsr.isEnabled());
        }

        if (receivedUsr.getRole() != null) {
            payload.put("role", receivedUsr.getRole());
        }

        message = userService.updateUserCredential(sessionUser, payload);

        statusMsg.setStatus(HttpStatus.OK);
        statusMsg.setMessage(message);
        return new ResponseEntity<>(statusMsg, HttpStatus.OK);
    }

    /*URI to the details of current session user*/
    @RequestMapping(value = "/getUser", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getProsumer(Authentication authentication) {

        String userName = this.getSessionUser(authentication);//authentication.getName();
        if (userName == null) {
            return errorResponse("Not Authorized to access resource", HttpStatus.UNAUTHORIZED);
        }

        UserT user = userService.getUser(userName);
        if (user == null) {
            return errorResponse("Not Found", HttpStatus.UNAUTHORIZED);
        }

        ResponseMessage statusMsg = new ResponseMessage();
        statusMsg.setStatus(HttpStatus.OK);
        statusMsg.setMessage("Success");
        statusMsg.setData(user);
        return new ResponseEntity<>(statusMsg, HttpStatus.OK);
    }

    /*URI to get list of registered user*/
    @RequestMapping(value = "/getUsers", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> getProsumers() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (getSessionUser(authentication) == null) {
            return errorResponse("Invalid User", HttpStatus.UNAUTHORIZED);
        }

        List<UserT> users = userService.getUsers()
                .stream()
                //.filter(u -> !u.getRole().equals(UserRole.ROLE_SYSADMIN))
                .filter(u -> !u.getUserName().equals(
                        organizationRepository.findByOrganizationId(u.getOrganizationId()).getOrganizationName().toLowerCase()+"SysAdmin"
                        )
                )
                .collect(Collectors.toList());
        if (users.size() > 0) {
            ResponseMessage statusMsg = new ResponseMessage();
            statusMsg.setStatus(HttpStatus.OK);
            statusMsg.setMessage("Success");
            statusMsg.setData(users);
            return new ResponseEntity<>(statusMsg, HttpStatus.OK);
        } else {
            return errorResponse("No users found", HttpStatus.NOT_FOUND);
        }
    }


    /**
     * URI to remove FOA user
     */
    @RequestMapping(value = "/removeUser/{username}", method = RequestMethod.DELETE)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> removeUser(@PathVariable(value = "username") String userName) {
        Boolean status = userService.removeUser(userName);
        ResponseMessage responseMessage = new ResponseMessage();

        if (!status) {
            return errorResponse(String.format("User: %s not found", userName), HttpStatus.NOT_FOUND);
        }

        responseMessage.setMessage(String.format("User: %s successfully removed", userName));
        responseMessage.setStatus(HttpStatus.OK);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }


    @RequestMapping(value = "/markAllMessagesAsRead", method = RequestMethod.POST)
    public ResponseEntity<ResponseMessage> markAllMessagesAsRead() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Unauthorized to access the resource", HttpStatus.UNAUTHORIZED);
        }
        String sessionUser = authentication.getName();
        UserT user = userService.getUser(sessionUser);
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessage("Success");
        responseMessage.setStatus(HttpStatus.OK);
        try {
            //if (authentication.getAuthorities().contains(UserRole.ROLE_ADMIN)) {
            if (authentication.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
                List<UserT> users = userService.getActiveUsersForOrganization(user.getOrganizationId());
                List<UserMessage> messages = users.parallelStream().flatMap(usr -> userMessageService
                        .getLatestMessages(usr.getUserName()).stream()
                        .map(userMessage -> {
                            userMessage.setNotifiedToAdmin(1);
                            return userMessageService.save(userMessage);
                        })).collect(Collectors.toList());
                responseMessage.setData(messages);
            } else {
                responseMessage.setData(userMessageService.getLatestMessages(user.getOrganizationId(), sessionUser).stream()
                        .map(userMessage -> {
                            userMessage.setMessageStatus(1);
                            return userMessageService.save(userMessage);
                        }));
            }

            responseMessage.setMessage("Marked all messages as read");
            return new ResponseEntity<>(responseMessage, HttpStatus.OK);

        } catch (RuntimeException ex) {
            throw new CustomException("No message found", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(value = "/getUnreadMessages", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getUnreadMassages() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Unauthorized to access the resource", HttpStatus.UNAUTHORIZED);
        }
        String sessionUser = authentication.getName();
        UserT user = userService.getUser(sessionUser);
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessage("Success");
        responseMessage.setStatus(HttpStatus.OK);
        try {
            //if (authentication.getAuthorities().contains(UserRole.ROLE_ADMIN)) {
            if (authentication.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
                List<UserT> users = userService.getActiveUsersForOrganization(user.getOrganizationId());
                List<UserMessage> messages = users.parallelStream().flatMap(usr -> userMessageService
                        .getLatestMessages(usr.getUserName()).stream()
                        .map(userMessage -> {
                            //userMessage.setNotifiedToAdmin(1);
                            return userMessageService.save(userMessage);
                        })).collect(Collectors.toList());
                responseMessage.setData(messages);
            } else {
                responseMessage.setData(userMessageService.getLatestMessages(user.getOrganizationId(), sessionUser).stream()
                        .map(userMessage -> {
                            //userMessage.setMessageStatus(1);
                            return userMessageService.save(userMessage);
                        }));
            }

            return new ResponseEntity<>(responseMessage, HttpStatus.OK);

        } catch (RuntimeException ex) {
            throw new CustomException("No message found", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @RequestMapping(value = "/getAllMessages", method = RequestMethod.GET)
    public ResponseEntity<ResponseMessage> getAllMassages() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Unauthorized to access the resource", HttpStatus.UNAUTHORIZED);
        }
        String sessionUser = authentication.getName();
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessage("Success");
        responseMessage.setStatus(HttpStatus.OK);
        UserT user = userService.getUser(sessionUser);
        try {
            //if (authentication.getAuthorities().contains(UserRole.ROLE_ADMIN)) {
            if (authentication.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
                List<String> userNames = userService.getActiveUsersForOrganization(user.getOrganizationId()).stream()
                        .map(usr -> usr.getUserName()).collect(Collectors.toList());
                responseMessage.setData(userMessageService.getAllMessagesAdmin(userNames));
            } else {
                responseMessage.setData(userMessageService.getAllMessages(user.getOrganizationId(), sessionUser));
            }
        } catch (RuntimeException ex) {
            throw new CustomException("No message found", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }


    @RequestMapping(value = "/clearAllMessages", method = RequestMethod.DELETE)
    public ResponseEntity<ResponseMessage> clearAllMassages() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Unauthorized to access the resource", HttpStatus.UNAUTHORIZED);
        }
        String sessionUser = authentication.getName();
        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessage("Success");
        responseMessage.setStatus(HttpStatus.OK);
        UserT user = userService.getUser(sessionUser);
        try {
            //if (authentication.getAuthorities().contains(UserRole.ROLE_ADMIN)) {
            if (authentication.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals(UserRole.ROLE_ADMIN.name()))) {
                for (UserT usr : userService.getActiveUsersForOrganization(user.getOrganizationId())) {
                    userMessageService.deleteAllMessages(user.getOrganizationId(), usr.getUserName());
                }
            } else {
                userMessageService.deleteAllMessages(user.getOrganizationId(), sessionUser);
            }
        } catch (RuntimeException ex) {
            throw new CustomException("No message found", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }


    @GetMapping(value = "/contract")
    public ResponseEntity getUserContract() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserT user = userService.getUser(authentication.getName());
        if (user == null) {
            return errorResponse(String.format("User '%s' does not exist", authentication.getName()),
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return this.fmanUserService.getUserContract(user);
    }


    @GetMapping(value = "/bill/{year}/{month}")
    public ResponseEntity getUserBill(@PathVariable(value = "year") String year,
                                      @PathVariable(value = "month") String month) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserT user = userService.getUser(authentication.getName());
        if (user == null) {
            return errorResponse(String.format("User '%s' does not exist", authentication.getName()),
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
        try {
            return this.fmanUserService.getUserBill(user, year, month);
        } catch (Exception ex) {
            return null;
        }
    }


    @PostMapping(value = "/pic")
    public ResponseEntity handlePhotoUpload(@RequestParam("uploadFile") MultipartFile pic) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        try {

            if (this.getSessionUser(authentication) == null) {
                return errorResponse("Invalid User", HttpStatus.UNAUTHORIZED);
            }

            String sessionUser = authentication.getName();

            UserT exisitingUsr = userService.getUser(sessionUser);

            if (exisitingUsr == null) {
                return errorResponse(String.format("User: %s not found", sessionUser), HttpStatus.UNAUTHORIZED);
            }

            if (pic == null) {
                return errorResponse("No profile pic supplied!", HttpStatus.EXPECTATION_FAILED);
            }

            this.userService.updateProfilePhoto(exisitingUsr, pic);
            return successResponse("successfully updated profile pic for "
                    + authentication.getName() + "!");

        } catch (Exception e) {
            return errorResponse("failed to upload profile pic for user: " + authentication.getName() + "!",
                    HttpStatus.EXPECTATION_FAILED);
        }
    }


    /**
     * URI for admin to create a new user belonging to their organization
     *
     * @param newUser
     * @return
     */
    @RequestMapping(value = "/registerUserByOrganization", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> registerUserByOrganization(@RequestBody UserT newUser) {
        try {

            String sessionUser;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (this.getSessionUser(authentication) == null) {
                return errorResponse("Invalid admin User", HttpStatus.UNAUTHORIZED);
            }
            sessionUser = authentication.getName();

            UserT adminUsr = userService.getUser(sessionUser);
            if (adminUsr == null) {
                return errorResponse(String.format("Admin user: %s not found", sessionUser), HttpStatus.UNAUTHORIZED);
            }

            if (adminUsr.getOrganizationId() == 0) {
                String msg = String.format("Admin user: %s does not belong to an organization. Please set its organization first.", adminUsr.getUserName());
                return errorResponse(msg, HttpStatus.NOT_FOUND);
            }

            if (StringUtils.isEmpty(newUser.getUserName()) || StringUtils.isEmpty(newUser.getPassword())) {
                return errorResponse("username and password fields can not be empty", HttpStatus.BAD_REQUEST);
            }

            if (StringUtils.isEmpty(newUser.getRole())) {
                return errorResponse("Must specify a role (ROLE_PROSUMER or ROLE_ADMIN)", HttpStatus.BAD_REQUEST);
            }

            if (newUser.getRole() == UserRole.ROLE_ADMIN) {
                if (StringUtils.isEmpty(newUser.getEmail())) {
                    return errorResponse("Email field can not be empty for admins", HttpStatus.BAD_REQUEST);
                }
            }

            if (userService.getUser(newUser.getUserName()) != null) {
                return errorResponse("Username already exists", HttpStatus.CONFLICT);
            }

            if (userService.getUserByEmail(newUser.getEmail()) != null) {
                return errorResponse("Another user is already registered with the provided email", HttpStatus.CONFLICT);
            }

            String tpLinkUserName = null;
            String tpLinkPassword = null;
            DeviceParameters deviceParameters = new DeviceParameters("", "", "", "");
            if (!StringUtils.isEmpty(newUser.getTpLinkUserName()) && !StringUtils.isEmpty(newUser.getTpLinkPassword())) {
                tpLinkUserName = newUser.getTpLinkUserName();
                tpLinkPassword = newUser.getTpLinkPassword();
                if (userService.tpLinkUserExist(newUser.getTpLinkUserName())) {
                    return errorResponse(String.format("TpLinkUserName: %s already exists", newUser.getTpLinkUserName()), HttpStatus.CONFLICT);
                }
                deviceParameters.setCloudUserName(newUser.getTpLinkUserName());
                deviceParameters.setCloudPassword(newUser.getTpLinkPassword());
                deviceParameters.setCloudAPIUrl(cloudApiUrl);
            }

            if (newUser.getUserAddress() == null) {
                newUser.setUserAddress(new UserAddress());
            }

            newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

            newUser.setRegistrationDate(new Date());
            newUser.setTpLinkUserName(tpLinkUserName);
            newUser.setTpLinkPassword(tpLinkPassword);
            newUser.setOrganization(adminUsr.getOrganizationId()); // new user automatically belongs to admin user's org
            UserT registeredUser = userService.save(newUser);

            tpLinkDeviceService.setTpLinkDevices(registeredUser, deviceParameters);

            UserCreatedEvent userCreatedEvent = new UserCreatedEvent(registeredUser,
                    organizationRepository.findByOrganizationId(registeredUser.getOrganizationId()).getOrganizationName(),
                    newUser.getPassword()
            );
            applicationEventPublisher.publishEvent(userCreatedEvent);

            ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setStatus(HttpStatus.OK);
            responseMessage.setData(newUser);
            responseMessage.setMessage("Success");
            return new ResponseEntity<>(responseMessage, HttpStatus.CREATED);
        } catch (Exception ex) {
            return errorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * URI for admins to get a list of registered users belonging to a their organization
     */
    @RequestMapping(value = "/getUsersByOrganization", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> getUsersByOrganization() {
        String sessionUser;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (getSessionUser(authentication) == null) {
            return errorResponse("Invalid admin User", HttpStatus.UNAUTHORIZED);
        }
        sessionUser = getSessionUser(authentication);

        UserT adminUsr = userService.getUser(getSessionUser(authentication));
        if (adminUsr == null) {
            return errorResponse(String.format("Admin user: %s not found", sessionUser), HttpStatus.UNAUTHORIZED);
        }

        if (adminUsr.getOrganizationId() == 0) {
            String msg = String.format("Admin user: %s does not belong to an organization. Please set its organization first.", adminUsr.getUserName());
            return errorResponse(msg, HttpStatus.NOT_FOUND);
        }

        List<UserT> users = userService.getAllUsersForOrganization(adminUsr.getOrganizationId())
                .stream()
                //.filter(u -> !u.getRole().equals(UserRole.ROLE_SYSADMIN))
                .filter(u -> !u.getUserName().equals(
                        organizationRepository.findByOrganizationId(u.getOrganizationId()).getOrganizationName().toLowerCase()+"SysAdmin"
                        )
                )
                .collect(Collectors.toList());

        if (users.size() > 0) {
            ResponseMessage statusMsg = new ResponseMessage();
            statusMsg.setStatus(HttpStatus.OK);
            statusMsg.setMessage("Success");
            statusMsg.setData(users);
            return new ResponseEntity<>(statusMsg, HttpStatus.OK);
        } else {
            return errorResponse("No users found for the given organization", HttpStatus.NOT_FOUND);
        }
    }


    @RequestMapping(value = "/getUserByOrganization/{username}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> getUserByOrganization(@PathVariable(value = "username") String userName) {


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserT adminUsr = userService.getUser(authentication.getName());
        UserT user = userService.getUser(userName);
        if (user != null) {
            if (adminUsr.getOrganizationId() != user.getOrganizationId()) {
                String msg = String.format("Changing organization for user: %s is not allowed", user.getUserName());
                return errorResponse(msg, HttpStatus.BAD_REQUEST);
            }
        }
        ResponseMessage statusMsg = new ResponseMessage();
        statusMsg.setStatus(HttpStatus.OK);
        statusMsg.setMessage("Success");
        statusMsg.setData(user);
        return new ResponseEntity<>(statusMsg, HttpStatus.OK);
    }


    /**
     * URI for admins to update a user belonging to their organization
     */
    @RequestMapping(value = "/updateUserByOrganization", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> updateUserByOrganization(@RequestBody UserT receivedUser) {

        String sessionUser;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Invalid admin User", HttpStatus.UNAUTHORIZED);
        }
        sessionUser = authentication.getName();

        UserT adminUsr = userService.getUser(sessionUser);
        if (adminUsr == null) {
            return errorResponse(String.format("Admin user: %s not found", sessionUser), HttpStatus.UNAUTHORIZED);
        }

        if (adminUsr.getOrganizationId() == 0) {
            String msg = String.format("Admin user: %s does not belong to an organization. Please set its organization first.", adminUsr.getUserName());
            return errorResponse(msg, HttpStatus.NOT_FOUND);
        }


        if (receivedUser.getOrganizationId() == 0) {
            String msg = String.format("Must provide organization id for user: %s", receivedUser.getUserName());
            return errorResponse(msg, HttpStatus.BAD_REQUEST);
        }

        if (adminUsr.getOrganizationId() != receivedUser.getOrganizationId()) {
            String msg = String.format("Changing organization for user: %s is not allowed", receivedUser.getUserName());
            return errorResponse(msg, HttpStatus.BAD_REQUEST);
        }

        UserT userToUpdate = userService.getUser(receivedUser.getUserName());
        if (userToUpdate == null) {
            String msg = String.format("Failure to update user because no user exists with username: '%s'", receivedUser.getUserName());
            return errorResponse(msg, HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> payload = new HashMap<>();

        // if received tplink username is not null
        if (!StringUtils.isEmpty(receivedUser.getTpLinkUserName())) {

            // check if another user is already using it
            UserT tmpUsr = userService.getUserByTpLinkUserName(receivedUser.getTpLinkUserName());

            // if tplinkusername doesn't exist and new name not null
            if (tmpUsr == null) {
                payload.put("tpLinkUserName", receivedUser.getTpLinkUserName());
            } else {
                if (!tmpUsr.getUserName().equals(receivedUser.getUserName())) {
                    return errorResponse(String.format("TpLinkUserName: %s already exists", receivedUser.getTpLinkUserName()), HttpStatus.CONFLICT);
                }

                // only add to map if new tpLinkUserName received
                if (!tmpUsr.getTpLinkUserName().equals(userToUpdate.getTpLinkUserName())) {
                    payload.put("tpLinkUserName", receivedUser.getTpLinkUserName());
                }
            }

        } else {
            //if tplinkusername exist and new name null
            if (!StringUtils.isEmpty(userToUpdate.getTpLinkUserName())) {
                payload.put("tpLinkUserName", null);
                payload.put("tpLinkPassword", null);
            }
        }

        if (!StringUtils.isEmpty(receivedUser.getTpLinkPassword())) {
            //if username is empty then set password to null
            if (receivedUser.getTpLinkUserName() == null || receivedUser.getTpLinkUserName().equals("")) {
                payload.put("tpLinkPassword", null);
            } else {
                payload.put("tpLinkPassword", receivedUser.getTpLinkPassword());
            }
        }

        if (!StringUtils.isEmpty(receivedUser.getPassword())) {
            payload.put("password", passwordEncoder.encode(receivedUser.getPassword()));
        }

        if (!StringUtils.isEmpty(receivedUser.getEmail())) {
            UserT tmpUsr = userService.getUserByEmail(receivedUser.getEmail());
            if (tmpUsr != null && !tmpUsr.getUserName().equals(receivedUser.getUserName())) {
                return errorResponse("Another user is already registered with the provided email", HttpStatus.CONFLICT);
            }
            payload.put("email", receivedUser.getEmail());
        }

        if (receivedUser.getOrganizationId() != 0) {
            payload.put("organizationId", receivedUser.getOrganizationId());
        }

        if (receivedUser.getUserAddress() != null) {
            payload.put("address", receivedUser.getUserAddress());
        }

        if (Boolean.valueOf(receivedUser.isEnabled()) != null) {
            payload.put("enabled", receivedUser.isEnabled());
        }

        if (receivedUser.getRole() != null) {
            payload.put("role", receivedUser.getRole());
        }

        String message = userService.updateUserCredential(receivedUser.getUserName(), payload);
        ResponseMessage statusMsg = new ResponseMessage();
        statusMsg.setStatus(HttpStatus.OK);
        statusMsg.setMessage(message);
        return new ResponseEntity<>(statusMsg, HttpStatus.OK);
    }


    @PostMapping(value = "/updateUserPicByOrganization/{userName}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity updateUserPicByOrganization(@PathVariable(value = "userName") String userName,
                                                      @RequestParam("uploadFile") MultipartFile pic) {
        try {
            String sessionUser;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (this.getSessionUser(authentication) == null) {
                return errorResponse("Invalid admin User", HttpStatus.UNAUTHORIZED);
            }
            sessionUser = authentication.getName();

            UserT adminUsr = userService.getUser(sessionUser);
            if (adminUsr == null) {
                return errorResponse(String.format("Admin User: %s not found", sessionUser), HttpStatus.UNAUTHORIZED);
            }

            if (adminUsr.getOrganizationId() == 0) {
                String msg = String.format("Admin user: %s does not belong to an organization. Please set its organization first.", adminUsr.getUserName());
                return errorResponse(msg, HttpStatus.NOT_FOUND);
            }


            UserT receivedUser = userService.getUser(userName);
            if (receivedUser == null) {
                String msg = String.format("Failure to delete user because no user exists with username: '%s'", userName);
                return errorResponse(msg, HttpStatus.BAD_REQUEST);
            }

            if (adminUsr.getOrganizationId() != receivedUser.getOrganizationId()) {
                String msg = String.format("User: %s doesn't belong to the admin user: %s organization", receivedUser.getUserName(), adminUsr.getUserName());
                return errorResponse(msg, HttpStatus.UNAUTHORIZED);
            }

            if (pic == null) {
                return errorResponse("No profile pic supplied!", HttpStatus.EXPECTATION_FAILED);
            }

            this.userService.updateProfilePhoto(receivedUser, pic);
            return successResponse("successfully updated profile pic for " + receivedUser.getUserName() + "!");

        } catch (Exception e) {
            return errorResponse("failed to upload profile pic for user: " + userName + "!",
                    HttpStatus.EXPECTATION_FAILED);
        }
    }


    /**
     * URI for admins to remove a user belonging to their organization
     */
    @RequestMapping(value = "/removeUserByOrganization/{username}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ResponseMessage> removeUserByOrganization(@PathVariable(value = "username") String userName) {

        String sessionUser;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (this.getSessionUser(authentication) == null) {
            return errorResponse("Invalid admin User", HttpStatus.UNAUTHORIZED);
        }
        sessionUser = authentication.getName();

        UserT adminUsr = userService.getUser(sessionUser);
        if (adminUsr == null) {
            return errorResponse(String.format("Admin User: %s not found", sessionUser), HttpStatus.UNAUTHORIZED);
        }

        if (adminUsr.getOrganizationId() == 0) {
            String msg = String.format("Admin user: %s does not belong to an organization. Please set its organization first.", adminUsr.getUserName());
            return errorResponse(msg, HttpStatus.NOT_FOUND);
        }

        UserT userToDelete = userService.getUser(userName);
        if (userToDelete == null) {
            String msg = String.format("Failure to delete user because no user exists with username: '%s'", userName);
            return errorResponse(msg, HttpStatus.BAD_REQUEST);
        }

        if (adminUsr.getOrganizationId() != userToDelete.getOrganizationId()) {
            String msg = String.format("User: %s doesn't belong to the admin user: %s organization", userToDelete.getUserName(), adminUsr.getUserName());
            return errorResponse(msg, HttpStatus.UNAUTHORIZED);
        }

        //Boolean status = userService.disableUser(userToDelete);
        Boolean status = userService.removeUser(userToDelete.getUserName());
        ResponseMessage responseMessage = new ResponseMessage();

        if (!status) {
            return errorResponse(String.format("Error deleting User: '%s'", userName), HttpStatus.NOT_FOUND);
        }

        responseMessage.setMessage(String.format("User: '%s' successfully removed", userName));
        responseMessage.setStatus(HttpStatus.OK);
        return new ResponseEntity<>(responseMessage, HttpStatus.OK);
    }

    @RequestMapping(value = "/getUserContractByOrganization/{username}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity getUserContractByOrganization(@PathVariable(value = "username") String userName) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserT adminUsr = userService.getUser(authentication.getName());
        if (adminUsr == null) {
            return errorResponse(String.format("Admin user: %s not found", authentication.getName()), HttpStatus.UNAUTHORIZED);
        }

        UserT user = userService.getUser(userName);
        if (user == null) {
            return errorResponse(String.format("User '%s' does not exist", userName), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if (user.getOrganizationId() != adminUsr.getOrganizationId()) {
            String msg = String.format("User '%s' does not belong to %s's organization", userName, adminUsr.getUserName());
            return errorResponse(msg, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return this.fmanUserService.getUserContract(user);
    }


    @RequestMapping(value = "/getUserBillByOrganization/{username}/{year}/{month}", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity getUserBillByOrganization(@PathVariable(value = "username") String userName,
                                                    @PathVariable(value = "year") String year,
                                                    @PathVariable(value = "month") String month) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserT adminUsr = userService.getUser(authentication.getName());
        if (adminUsr == null) {
            return errorResponse(String.format("Admin user: %s not found", authentication.getName()), HttpStatus.UNAUTHORIZED);
        }

        UserT user = userService.getUser(userName);
        if (user == null) {
            return errorResponse(String.format("User '%s' does not exist", userName), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        if (user.getOrganizationId() != adminUsr.getOrganizationId()) {
            String msg = String.format("User '%s' does not belong to %s's organization", userName, adminUsr.getUserName());
            return errorResponse(msg, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return this.fmanUserService.getUserBill(user, year, month);
    }

    @RequestMapping(value = "/getOrganization", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity getOrganization() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserT adminUsr = userService.getUser(authentication.getName());
        if (adminUsr == null) {
            return errorResponse(String.format("Admin user: %s not found", authentication.getName()), HttpStatus.UNAUTHORIZED);
        }

        Organization organization = organizationRepository.findByOrganizationId(adminUsr.getOrganizationId());
        if (organization == null) {
            String msg = String.format("Organization with id: %d does not exist", adminUsr.getOrganizationId());
            return errorResponse(msg, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        ResponseMessage responseMessage = new ResponseMessage();
        responseMessage.setMessage("Success");
        responseMessage.setData(organization);
        responseMessage.setStatus(HttpStatus.OK);
        return new ResponseEntity(responseMessage, HttpStatus.OK);
    }

    @RequestMapping(value = "/setOrgControl/{status}", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity getOrganization(@PathVariable(value = "status") int status) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserT adminUsr = userService.getUser(authentication.getName());
            if (adminUsr == null) {
                return errorResponse(String.format("Admin user: %s not found", authentication.getName()), HttpStatus.UNAUTHORIZED);
            }

            Organization organization = organizationRepository.findByOrganizationId(adminUsr.getOrganizationId());
            if (organization == null) {
                String msg = String.format("Organization with id: %d does not exist", adminUsr.getOrganizationId());
                return errorResponse(msg, HttpStatus.UNPROCESSABLE_ENTITY);
            }

            organization.setDirectControlMode(status == 0 ? OrganizationLoadControlState.Paused : OrganizationLoadControlState.Active);
            organizationRepository.save(organization);

            ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setMessage("Success");
            responseMessage.setData(organization);
            responseMessage.setStatus(HttpStatus.OK);
            return new ResponseEntity(responseMessage, HttpStatus.OK);
        } catch (Exception ex) {
            return errorResponse(ex.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
