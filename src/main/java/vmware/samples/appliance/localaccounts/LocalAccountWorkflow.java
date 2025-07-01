/*
 * *******************************************************
 * Copyright VMware, Inc. 2017.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.appliance.localaccounts;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.cli.Option;

import com.vmware.appliance.LocalAccounts;
import com.vmware.appliance.LocalAccountsTypes.Config;
import com.vmware.appliance.LocalAccountsTypes.UpdateConfig;

import vmware.samples.common.SamplesAbstractBase;

/**
 * Description: Demonstrates local accounts workflow as below
 * 1) Create a user
 * 2) List all users to check if the user is added successfully
 * 3) Get the user details to check parameters passed in create call,here not
 *    passing anything other than mandatory attributes password and role
 *    list.
 *    Setting all attributes in set operation ,the workflow is identical and
 *    same can be used in create api as well.
 * 4) Set certain user details
 * 5) Get the user details to check for values set
 * 6) Update certain user details
 * 7) Get the user details to check for updated values
 * 8) Delete the user
 * 9) List all users to check if the user is deleted successfully
 * Author: VMware, Inc. Sample Prerequisites: vCenter 6.7+
 */
public class LocalAccountWorkflow extends SamplesAbstractBase {
    private LocalAccounts laServiceApiStub;
    private String user;
    private Long days_after_passwd_exp;
    private Long max_days;
    private Long min_days;
    private Long warn_days;
    private Boolean enabled;
    private Boolean password_Expires;
    private Boolean inactiveAftrPasswdExpired;
    private String fullName;
    private String email;
    private String old_Password;
    private String userPassword;
    private String[] userRoles;
    private String password_Expires_At;
    private Config laConfig;
    private UpdateConfig laUpdateConfig;
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_SUPERADMIN = "superAdmin";
    public static final String UPDATE_EMAIL = "update@test.com";
    public static final String UPDATE_FULL_NAME = "Updated_full_name";
    public static final String USER_NAME = "user_name";
    public static final String USER_PASSWD = "user_pwd";
    public static final String USER_ROLES = "user_roles";
    public static final String DAYS_AFTR_PASSWD_EXPIRES =
            "days_after_passwd_exp";
    public static final String USER_EMAIL = "usr_email";
    public static final String USER_ACCNT_ENABLED = "usraccnt_enabled";
    public static final String USER_FULL_NAME = "usr_fullname";
    public static final String USER_INACTIVE = "usr_inactive_afterPwd_expired";
    public static final String MAX_DAYS = "max_days";
    public static final String MIN_DAYS = "min_days";
    public static final String WARN_DAYS = "warn_days";
    public static final String OLD_PASSWD = "old_password";
    public static final String PASSWD_EXPIRES = "password_expires";
    public static final String PASSWD_EXPIRES_AT = "password_expires_at";
    public static final String DEFAULT_USER_ROLES = "operator";
    public static final String DEFAULT_DAYS_AFTR_PASSWD_EXPIRES = "10";
    public static final String DEFAULT_USER_EMAIL = "usr_email@test.com";
    public static final String DEFAULT_USER_ACCNT_ENABLED = "false";
    public static final String DEFAULT_USER_FULL_NAME = "test_user_def";
    public static final String DEFAULT_USER_INACTIVE = "false";
    public static final String DEFAULT_MAX_DAYS = "70";
    public static final String DEFAULT_MIN_DAYS = "10";
    public static final String DEFAULT_WARN_DAYS = "30";
    public static final String DEFAULT_OLD_PASSWD = "Pass123";
    public static final String DEFAULT_PASSWD_EXPIRES = "false";
    public static final String DEFAULT_PASSWD_EXPIRES_AT = "1/1/28";

    protected void setup() throws Exception {
        laServiceApiStub = vapiAuthHelper.getStubFactory().createStub(
            LocalAccounts.class, sessionStubConfig);
        // initializing configuration object to be passed for create and set
        // operations
        laConfig = new Config();
        // initializing configuration object to be passed for update operations
        laUpdateConfig = new UpdateConfig();
    }

    protected void run() throws Exception {
        System.out.println("#### Example: Step1-Creating users");
        // setting password and role passed in config
        laConfig.setPassword(old_Password.toCharArray());
        laConfig.setRoles(Arrays.asList(userRoles));
        // create user with basic configuration
        laServiceApiStub.create(user, laConfig);
        // list all users to check if user added in above step is listed
        System.out.println("\n#### Example: List Users \n" + laServiceApiStub
            .list());
        // get the user details for the created user
        System.out.println("\n#### Example:Get user details \n"
                           + laServiceApiStub.get(user));
        DateFormat formatter = new SimpleDateFormat("MM/dd/yy");
        Calendar cal = Calendar.getInstance();
        cal.setTime(formatter.parse(password_Expires_At));
        System.out.println("\n#### Example: Step2-Setting user attributes");
        /*
         * Populating configuration details for set operation.
         * Note:The same steps can be used to invoke create localaccounts
         * api as well
         */
        // old password is required for set api operation for a non-super admin
        laConfig.setOldPassword(old_Password.toCharArray());
        laConfig.setDaysAfterPasswordExpiration(days_after_passwd_exp);
        laConfig.setEmail(email);
        laConfig.setEnabled(enabled);
        laConfig.setFullName(fullName);
        laConfig.setInactiveAfterPasswordExpiration(inactiveAftrPasswdExpired);
        laConfig.setMaxDaysBetweenPasswordChange(max_days);
        laConfig.setMinDaysBetweenPasswordChange(min_days);
        laConfig.setPassword(userPassword.toCharArray());
        laConfig.setPasswordExpires(password_Expires);
        laConfig.setRoles(Arrays.asList(ROLE_ADMIN));
        laConfig.setWarnDaysBeforePasswordExpiration(warn_days);
        // invoking set local accounts api passing above configuration
        laServiceApiStub.set(user, laConfig);
        System.out.println("\nUser details \n" + laServiceApiStub.get(user));
        // Atleast one attribute needs to be set for update api
        System.out.println("\n#### Example: Step3-Updating user attributes");
        /*
         * Populating configuration details for update operation.
         * 1.Updating role from operator to admin
         * 2.Setting user to enabled, the user was disabled in step2 enabled
         * flag changes from false to true
         * 3.Setting password expires to true and setting a date,max days
         * between password change changes from -1 (password expires false)
         * 4.Setting inactive after password expiration to true:check for
         * inactive at date changes
         * 5.Updating user full name and email id
         * Note:Similarly all other attributes can be updated
         */
        laUpdateConfig.setRoles(Arrays.asList(ROLE_SUPERADMIN));
        laUpdateConfig.setEnabled(true);
        laUpdateConfig.setPasswordExpires(true);
        laUpdateConfig.setPasswordExpiresAt(cal);
        laUpdateConfig.setInactiveAfterPasswordExpiration(true);
        laUpdateConfig.setEmail(UPDATE_EMAIL);
        laUpdateConfig.setFullName(UPDATE_FULL_NAME);
        // invoking update local accounts api passing above configuration
        laServiceApiStub.update(user, laUpdateConfig);
        System.out.println("\nUser details \n" + laServiceApiStub.get(user));
    }

    public void cleanup() throws Exception {
        System.out.println("\n#### Example: Step4-Delete User");
        // Deleting the user created
        laServiceApiStub.delete(user);
        // list all users to check if user removed in above step is not listed
        System.out.println("#### Example: List Users \n" + laServiceApiStub
            .list());
    }

    public static void main(String[] args) throws Exception {
        /*
         * Execute the sample using the command line arguments or parameters
         * from the configuration file. This executes the following steps:
         * 1. Parse the arguments required by the sample
         * 2. Login to the server
         * 3. Setup any resources required by the sample run
         * 4. Run the sample
         * 5. Cleanup any data created by the sample run, if cleanup=true
         * 6. Logout of the server
         */
        new LocalAccountWorkflow().execute(args);
    }

    protected void parseArgs(String[] args) {
        Option usrNameOption = Option.builder()
            .longOpt(USER_NAME)
            .desc("REQUIRED: Specify username")
            .argName("USER_NAME")
            .required(true)
            .hasArg()
            .build();
        Option usrPasswordOption = Option.builder()
            .longOpt(USER_PASSWD)
            .desc("REQUIRED: Specify user password")
            .argName("USER_PWD")
            .required(true)
            .hasArg()
            .build();
        Option usrRoles = Option.builder()
            .longOpt(USER_ROLES)
            .desc(
                "OPTIONAL: Comma seperated list of roles ,valid roles are operator, admin, superAdmin")
            .argName("USER_ROLES")
            .required(false)
            .hasArg()
            .valueSeparator(',')
            .build();
        Option daysAfterPasswordExpiration = Option.builder()
            .longOpt(DAYS_AFTR_PASSWD_EXPIRES)
            .desc(
                "OPTIONAL: Number of days after password expiration before the account will be locked")
            .argName("DAYS_AFTER_PASSWD_EXPIRATION")
            .required(false)
            .hasArg()
            .build();
        Option usrEmail = Option.builder()
            .longOpt(USER_EMAIL)
            .desc("OPTIONAL: Email address of the local account")
            .argName("USR_EMAIL")
            .required(false)
            .hasArg()
            .build();
        Option usrEnabled = Option.builder()
            .longOpt(USER_ACCNT_ENABLED)
            .desc("OPTIONAL: Indicates if the sepcific user account is enabled."
                  + "By default it is enabled ,set false to disable the account")
            .argName("USR_ACCNT_ENABLED")
            .required(false)
            .hasArg()
            .build();
        Option usrFullName = Option.builder()
            .longOpt(USER_FULL_NAME)
            .desc("OPTIONAL: Full name of the user")
            .argName("USR_FULL_NAME")
            .required(false)
            .hasArg()
            .build();
        Option usrInactive = Option.builder()
            .longOpt(USER_INACTIVE)
            .desc(
                "OPTIONAL: if the account will be locked after password expiration")
            .argName("USR_INACTIVE_AFTR_EXPIRY")
            .required(false)
            .hasArg()
            .build();
        Option maxDaysBwPasswdChange = Option.builder()
            .longOpt(MAX_DAYS)
            .desc("OPTIONAL: Maximum number of days between password change,"
                  + "if not specifed will pick up from /etc/login.defs")
            .argName("MAX_DAYS_PASSWD_CHANGE")
            .required(false)
            .hasArg()
            .build();
        Option minDaysBwPasswdChange = Option.builder()
            .longOpt(MIN_DAYS)
            .desc("OPTIONAL: Minimum number of days between password change,"
                  + "if not specifed will pick up from /etc/login.defs ")
            .argName("MIN_DAYS_PASSWD_CHANGE")
            .required(false)
            .hasArg()
            .build();
        Option warnDaysBwPasswdChange = Option.builder()
            .longOpt(WARN_DAYS)
            .desc("OPTIONAL: Number of days of warning before password expires,"
                  + "if not specifed will pick up from /etc/login.defs ")
            .argName("WARN_DAYS_PASSWD_CHANGE")
            .required(false)
            .hasArg()
            .build();
        Option oldPassword = Option.builder()
            .longOpt(OLD_PASSWD)
            .desc(
                "OPTIONAL:  Old password of the user (required in case of the password change, "
                  + "not required if superAdmin user changes the password of the other.")
            .argName("OLD_PASSWD")
            .required(false)
            .hasArg()
            .build();
        Option passwordExpires = Option.builder()
            .longOpt(PASSWD_EXPIRES)
            .desc(
                "OPTIONAL: Indicates if the account passsword expires and needs to be reset."
                  + "By default it expires after 60days,this can be disabled by settting "
                  + "to false and then password never expires for the account")
            .argName("PASS_EXPIRES")
            .required(false)
            .hasArg()
            .build();
        Option passwordExpiresAt = Option.builder()
            .longOpt(PASSWD_EXPIRES_AT)
            .desc("OPTIONAL: Date when the account's password will expire")
            .argName("PASSWD_EXPIRES_AT")
            .required(false)
            .hasArg()
            .build();
        List<Option> optionList = Arrays.asList(usrNameOption,
            usrPasswordOption,
            usrRoles,
            daysAfterPasswordExpiration,
            usrEmail,
            usrEnabled,
            usrFullName,
            usrInactive,
            maxDaysBwPasswdChange,
            minDaysBwPasswdChange,
            warnDaysBwPasswdChange,
            oldPassword,
            passwordExpiresAt,
            passwordExpires);
        super.parseArgs(optionList, args);
        user = (String) parsedOptions.get(USER_NAME);
        userPassword = (String) parsedOptions.get(USER_PASSWD);
        if (isOptionPassed(USER_ROLES)) {
            userRoles = (String[]) parsedOptions.get(USER_ROLES)
                .toString()
                .split(",");
        } else {
            userRoles = DEFAULT_USER_ROLES.split(",");
        }
        if (isOptionPassed(DAYS_AFTR_PASSWD_EXPIRES)) {
            days_after_passwd_exp = Long.valueOf(parsedOptions.get(
                DAYS_AFTR_PASSWD_EXPIRES).toString());
        } else {
            days_after_passwd_exp = Long.valueOf(
                DEFAULT_DAYS_AFTR_PASSWD_EXPIRES.toString());
        }
        if (isOptionPassed(USER_EMAIL)) {
            email = (String) parsedOptions.get(USER_EMAIL);
        } else {
            email = DEFAULT_USER_EMAIL;
        }
        if (isOptionPassed(USER_ACCNT_ENABLED)) {
            enabled = Boolean.valueOf(parsedOptions.get(USER_ACCNT_ENABLED)
                .toString());
        } else {
            enabled = Boolean.valueOf(DEFAULT_USER_ACCNT_ENABLED);
        }
        if (isOptionPassed(USER_FULL_NAME)) {
            fullName = (String) parsedOptions.get(USER_FULL_NAME);
        } else {
            fullName = DEFAULT_USER_FULL_NAME;
        }
        if (isOptionPassed(USER_INACTIVE)) {
            inactiveAftrPasswdExpired = Boolean.valueOf(parsedOptions.get(
                USER_INACTIVE).toString());
        } else {
            inactiveAftrPasswdExpired = Boolean.valueOf(DEFAULT_USER_INACTIVE);
        }
        if (isOptionPassed(MAX_DAYS)) {
            max_days = Long.valueOf(parsedOptions.get(MAX_DAYS).toString());
        } else {
            max_days = Long.valueOf(DEFAULT_MAX_DAYS);
        }
        if (isOptionPassed(MIN_DAYS)) {
            min_days = Long.valueOf(parsedOptions.get(MIN_DAYS).toString());
        } else {
            min_days = Long.valueOf(DEFAULT_MIN_DAYS);
        }
        if (isOptionPassed(WARN_DAYS)) {
            warn_days = Long.valueOf(parsedOptions.get(WARN_DAYS).toString());
        } else {
            warn_days = Long.valueOf(DEFAULT_WARN_DAYS);
        }
        if (isOptionPassed(OLD_PASSWD)) {
            old_Password = (String) parsedOptions.get(OLD_PASSWD);
        } else {
            old_Password = userPassword;
        }
        if (isOptionPassed(PASSWD_EXPIRES)) {
            password_Expires = Boolean.valueOf(parsedOptions.get(PASSWD_EXPIRES)
                .toString());
        } else {
            password_Expires = Boolean.valueOf(DEFAULT_PASSWD_EXPIRES);
        }
        if (isOptionPassed(PASSWD_EXPIRES_AT)) {
            password_Expires_At = (String) parsedOptions.get(PASSWD_EXPIRES_AT);
        } else {
            password_Expires_At = DEFAULT_PASSWD_EXPIRES_AT;
        }
    }

    private boolean isOptionPassed(String option) {
        return (parsedOptions.containsKey(option) && parsedOptions.get(
            option) != null);
    }

}
