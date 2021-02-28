package org.goflex.wp2.fmanproxy.shell;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.goflex.wp2.fmanproxy.user.UserRole;
import org.goflex.wp2.fmanproxy.user.UserService;
import org.goflex.wp2.fmanproxy.user.UserT;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class ShellCommands {

    @Autowired
    private UserService userSvc;


    @ShellMethod("Set the logging level")
    public String loglevel(@ShellOption() String loglevel) {

        String packageName = "org.goflex.wp2";
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        if (loglevel.equalsIgnoreCase("ALL")) {
            loggerContext.getLogger(packageName).setLevel(Level.ALL);
            return "Logging level successfully set to " + loglevel;
        } else if (loglevel.equalsIgnoreCase("TRACE")) {
            loggerContext.getLogger(packageName).setLevel(Level.TRACE);
            return "Logging level successfully set to " + loglevel;
        } else if (loglevel.equalsIgnoreCase("DEBUG")) {
            loggerContext.getLogger(packageName).setLevel(Level.DEBUG);
            return "Logging level successfully set to " + loglevel;
        } else if (loglevel.equalsIgnoreCase("INFO")) {
            loggerContext.getLogger(packageName).setLevel(Level.INFO);
            return "Logging level successfully set to " + loglevel;
        } else if (loglevel.equalsIgnoreCase("WARN")) {
            loggerContext.getLogger(packageName).setLevel(Level.WARN);
            return "Logging level successfully set to " + loglevel;
        } else if (loglevel.equalsIgnoreCase("ERROR")) {
            loggerContext.getLogger(packageName).setLevel(Level.ERROR);
            return "Logging level successfully set to " + loglevel;
        } else if (loglevel.equalsIgnoreCase("OFF")) {
            loggerContext.getLogger(packageName).setLevel(Level.OFF);
            return "Logging level successfully set to " + loglevel;
        } else {
            return "Error, not a known loglevel: " + loglevel;
        }
    }

    // Authorization methods
    @ShellMethod("Generate a JWT for an arbitrary user")
    public String signin(@ShellOption() String username, @ShellOption String password) {
        return this.userSvc.signin(username, password);
    }

    @ShellMethod("Creates a user account without the GUI")
    public String signup(@ShellOption() String userName, @ShellOption String password,
                         @ShellOption(defaultValue="") String firstName, @ShellOption(defaultValue="") String lastName,
                         @ShellOption(defaultValue="") String email,
                         @ShellOption(defaultValue="ROLE_PROSUMER") String role) {
        UserT user = new UserT();
        user.setUserName(userName);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setFirstName(lastName);
        user.setEmail(email);
        user.setRole(UserRole.valueOf(role));

        return this.userSvc.signup(user);
    }

}
