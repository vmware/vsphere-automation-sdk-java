/*
 * *******************************************************
 * Copyright VMware, Inc. 2016.  All Rights Reserved.
 * *******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */
package vmware.samples.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
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
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * Helper class for parsing command line options or reading the options from a
 * config file
 */
public class ParametersHelper {
    private List<Option> optionList;

    /**
     * Adds the common and sample-specific options to the list of options to
     * parse.
     *
     * @param optionList
     */
    public ParametersHelper(List<Option> optionList) {
        this.optionList = optionList;
    }

    /**
     * Parses the command line arguments and returns a map which has the values
     * for each of the command line option required by the sample. If options
     * are not specified by command line, then the config file under the is read
     * and resources folder is read and options are parsed.
     *
     * @param args array of String arguments passed to the sample
     * @return map of arguments and their values
     * @throws ParseException - if there are problems while parsing the
     *         arguments
     * @throws ConfigurationException - if there is an error while loading the
     *         properties file
     */
    public Map<String, Object> parse(String[] args)
        throws ParseException, ConfigurationException {
        Map<String, Object> parsedOptions = new HashMap<String, Object>();

        Option configFileOption = Option.builder()
                .required(true)
                .hasArg()
                .argName("CONFIGURATION FILE")
                .longOpt("config-file")
                .desc("Path to the configuration file for the sample")
                .build();
        
        if (args.length == 0) {
            printUsage();
        } else {
            Iterator<Option> optionsIter = getRequiredSampleOptions()
                    .getOptions().iterator();
            if (args.length == 2) {
                // Check if configuration file is being used
                CommandLineParser parser = new DefaultParser();
                CommandLine cmd = parser.parse(new Options().addOption(
                    configFileOption), args, true);
                
                if(!cmd.hasOption(configFileOption.getLongOpt())) {
                    printUsage();
                }
                
                // Read from configuration file first
                Configuration config = new PropertiesConfiguration(cmd
                    .getOptionValue("config-file"));
                List<String> missingOptions = new ArrayList<>();
                while (optionsIter.hasNext()) {
                    Option option = optionsIter.next();
                    String optionValue = config.getString(option.getLongOpt());
                    if (optionValue == null || optionValue.isEmpty()) {
                        missingOptions.add(option.getLongOpt());
                    } else {
                        parsedOptions.put(option.getLongOpt(), optionValue);
                    }
                }
                if (!missingOptions.isEmpty())
                    throw new ConfigurationException("Missing required options:"
                                                     + " " + missingOptions);
            } else {
                while (optionsIter.hasNext()) {
                    CommandLineParser parser = new DefaultParser();
                    CommandLine cmd = parser.parse(getRequiredSampleOptions(), args, true);
                    Option option = optionsIter.next();
                    Object optionValue = cmd.getOptionValue(option
                        .getLongOpt());
                    parsedOptions.put(option.getLongOpt(), optionValue);
                }
            }
        }
        return parsedOptions;
    }

    private Options getRequiredSampleOptions() {
        Iterator<Option> optionIter = optionList.iterator();
        Options options = new Options();
        while (optionIter.hasNext()) {
            options.addOption(optionIter.next());
        }
        return options;
    }
    
    /**
     * Prints the usage information for running the sample
     */
    public void printUsage() {
        Options options = getRequiredSampleOptions();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(new Comparator<Option>() {
            @Override
            public int compare(Option option1, Option option2) {
                return 0;
            }

        });
        System.out.println("\nusage: java -cp target/vsphere-automation-java-samples-6.5.0-jar-with-dependencies.jar <fully_qualified_sample_name> --config-file <ABSOLUTE PATH TO THE CONFIGURATION FILE>\nOR");
        formatter.printHelp(300,
            "java -cp vsphere-automation-java-samples-6.5.0-jar-with-dependencies.jar <fully_qualified_sample_name>",
            "\nOptions to be specified on command line or configuration file: ",
            options,
            "",
            true);
        System.exit(0);
    }
}
