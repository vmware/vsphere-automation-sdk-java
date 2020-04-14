/*
 * *******************************************************
 * Copyright VMware, Inc. 2019.  All Rights Reserved.
 * SPDX-License-Identifier: MIT
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Helper class for parsing command line options or reading the options from a
 * config file
 */
public class ParametersHelper {
    private Map<Option, Boolean> optionMap;
    private  Map<String, Object> parsedOptions;
    private static final String CONFIG_FILE = "config-file";
    private static final String OAUTH_APP_JSON = "oauth_app_json";

    /**
     * Adds the common and sample-specific options to the list of options to
     * parse.
     *
     * @param optionList
     */
    public ParametersHelper(List<Option> optionList) {
        this.optionMap = new LinkedHashMap<Option, Boolean>();
        Iterator<Option> optionIter = optionList.iterator();
        while(optionIter.hasNext()) {
            Option option = optionIter.next();
            this.optionMap.put(option, option.isRequired());
        }
    }

    /**
     * Parses the command line arguments and returns a map which has the values
     * for each of the command line option required by the sample. If a
     * config-file parameter is specified, then the method reads all the
     * parameters from the configuration file first. If any sample parameters
     * are specified on the command line too, these parameters will override
     * the values specified in the configuration file.
     *
     * @param args array of String arguments passed to the sample
     * @return map of arguments and their values
     * @throws ParseException - if there are problems while parsing the
     *         arguments
     * @throws ConfigurationException - if there is an error while loading the
     *         properties file
     */
    public Map<String, Object> parse(String[] args, String sampleName)
            throws ParseException, ConfigurationException {
        parsedOptions = new HashMap<String, Object>();
        if (args.length == 0) {
            printUsage(sampleName);
        }
        List<Option> optionsToParseFromCmdLine = new ArrayList<>();

        /*
         * Mark all options as optional when reading from the config file
         * to avoid parse exception while parsing the arguments.
         */
        List<Option> optionsToParseFromConfig = new ArrayList<Option>();
        Iterator<Option> optionIter = this.optionMap.keySet().iterator();
        while(optionIter.hasNext()) {
            Option option = (Option)optionIter.next().clone();
            option.setRequired(false);
            optionsToParseFromConfig.add(option);
        }

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(
            getOptions(optionsToParseFromConfig), args);

        Iterator<Option> optionsIter = optionsToParseFromConfig.iterator();
        if (cmd.hasOption(CONFIG_FILE)) {
            // Read from configuration file first
            Configuration config = new PropertiesConfiguration(cmd.getOptionValue(CONFIG_FILE));
            while (optionsIter.hasNext()) {
                Option option = optionsIter.next();
                Object optionValue =
                        getOptionValueFromConfig(
                            option, config);
                if (optionValue == null ||
                        optionValue.toString().isEmpty()) {
                    option.setRequired(this.optionMap.get(option));
                    optionsToParseFromCmdLine.add(option);
                } else {
                    option.setRequired(false);
                    optionsToParseFromCmdLine.add(option);
                    parsedOptions.put(option.getLongOpt(), optionValue);
                }
            }
        }
        else {
            optionsToParseFromCmdLine =
                    new ArrayList<Option>(this.optionMap.keySet());
        }

        optionsIter = optionsToParseFromCmdLine.iterator();
        CommandLine cmdOverrideArgs = parser.parse(
            getOptions(optionsToParseFromCmdLine), args);
        while (optionsIter.hasNext()) {
            Option option = optionsIter.next();
            Object optionValue =
                    getOptionValueFromCmdLine(option, cmdOverrideArgs);
            if (optionValue != null && !optionValue.toString().isEmpty()) {
                parsedOptions.put(option.getLongOpt(), optionValue);
            }
        }

        return parsedOptions;
    }

    private Options getOptions(List<Option> optionList) {
        Iterator<Option> optionIter = optionList.iterator();
        Options options = new Options();
        while (optionIter.hasNext()) {
            options.addOption(optionIter.next());
        }
        return options;
    }
    private Object getOptionValueFromConfig(
        Option option, Configuration config) {
        Object optionValue = null;
        try {
            if (option.getType().equals(Boolean.class)) {
                optionValue = config.getString(option.getLongOpt());
                if (optionValue != null) {
                    optionValue = config.getBoolean(option.getLongOpt());
                }

            } else {
                optionValue = config.getString(option.getLongOpt());
            }
        } catch (ConversionException cex) {
            optionValue = null;
        }
        return optionValue;
    }

    private Object getOptionValueFromCmdLine(Option option, CommandLine cmd) {
        Object optionValue = null;
        if(!option.hasArg()) {
            if(cmd.hasOption(option.getLongOpt())) {
                optionValue = true;
            }
        }
        else {
            optionValue = cmd.getOptionValue(option.getLongOpt());
        }

        return optionValue;
    }

    /**
     * Prints the usage information for running the sample
     */
    public void printUsage(String sampleName) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(new Comparator<Option>() {
            @Override
            public int compare(Option option1, Option option2) {
                return 0;
            }

        });
        formatter.printHelp(150,
            "\njava -cp target/vsphere-samples-7.0.0.1.jar " + sampleName,
            "\nSample Options:",
            getOptions(new ArrayList<Option>(this.optionMap.keySet())),
            "",
            true);
        System.exit(0);
    }

    public Map<String, Object> parseJsonConfig(String[] args) {
        List<Option> optionsToParseFromConfig = new ArrayList<Option>();
        Iterator<Option> optionIter = this.optionMap.keySet().iterator();
        while(optionIter.hasNext()) {
            Option option = (Option)optionIter.next().clone();
            option.setRequired(false);
            optionsToParseFromConfig.add(option);
        }
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(
                getOptions(optionsToParseFromConfig), args);
           if (cmd.hasOption(OAUTH_APP_JSON) && !(parsedOptions.
                   containsKey(AuthorizationConstants.CLIENT_SECRET))
                   && !(parsedOptions.containsKey(AuthorizationConstants.CLIENT_ID))) {
                JsonNode argsJsonMapper=new ObjectMapper().readTree(new File(cmd.getOptionValue(OAUTH_APP_JSON)));
                parsedOptions.put(AuthorizationConstants.CLIENT_ID,
                        argsJsonMapper.get(AuthorizationConstants.CLIENT_ID).asText());
                parsedOptions.put(AuthorizationConstants.CLIENT_SECRET,
                        argsJsonMapper.get(AuthorizationConstants.CLIENT_SECRET).asText());
                if(argsJsonMapper.has(AuthorizationConstants.REDIRECT_URIS)) {
                    ArrayNode arrayNode=(ArrayNode)argsJsonMapper.get(AuthorizationConstants.REDIRECT_URIS);
                    parsedOptions.put(AuthorizationConstants.REDIRECT_URI, arrayNode.get(0).asText());
                }
           }
        }catch (IOException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
        catch (ParseException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
        return parsedOptions;
    }
}
