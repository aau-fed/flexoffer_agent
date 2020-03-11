package org.goflex.wp2.fogenerator.shell;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class ShellCommands {

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
}
