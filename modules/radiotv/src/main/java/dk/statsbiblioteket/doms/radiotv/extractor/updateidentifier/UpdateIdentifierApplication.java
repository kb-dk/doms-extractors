package dk.statsbiblioteket.doms.radiotv.extractor.updateidentifier;

import dk.statsbiblioteket.doms.central.CentralWebservice;
import dk.statsbiblioteket.doms.central.InvalidCredentialsException;
import dk.statsbiblioteket.doms.central.MethodFailedException;
import dk.statsbiblioteket.doms.central.RecordDescription;
import dk.statsbiblioteket.doms.radiotv.extractor.DomsClient;
import org.apache.log4j.Logger;

import javax.xml.transform.sax.SAXTransformerFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 *
 */
public class UpdateIdentifierApplication {

    private static Logger logger = Logger.getLogger(UpdateIdentifierApplication.class);

    /**
     * The name of the properties bundle containing the configuration parameters.
     */
    private static final String BUNDLE = "update_identifier";

    /**
     * The environment variable pointing to the directory containing the properties file.
     */
    private static final String CONFIG_DIR =  "dk.statsbiblioteket.radiotv.extractor.updateidentifier.configdir";


    /**
     * Immediately below here is the list of parameters which must be defined in update_identfier.properties
     */
    private static final String LOCKFILE_DIR = "dk.statsbiblioteket.radiotv.extractor.updateidentifier.lockfiledir";
    private static final String TIMESTAMP_FILE_DIR = "dk.statsbiblioteket.radiotv.extractor.updateidentifier.timestampfiledir";
    private static final String DOMS_ENDPOINT = "dk.statsbiblioteket.radiotv.extractor.updateidentifier.domsendpoint";
    private static final String DOMS_USERNAME = "dk.statsbiblioteket.radiotv.extractor.updateidentifier.domsusername";
    private static final String DOMS_PASSWORD = "dk.statsbiblioteket.radiotv.extractor.updateidentifier.domspassword";





    private static final String LOCKFILE = "update_identifier_lockfile";
    private static final String TIMESTAMP_FILE = "update_identifier_timestamp";
    private static File lockFileFile;
    private static File timestampFileFile;
    private static long since;
    private static List<RecordDescription> domsRecords;


    public static class UpdateIdentifierException extends Exception {
        public UpdateIdentifierException(Throwable cause) {
            super(cause);
        }

        public UpdateIdentifierException(String message) {
            super(message);
        }
    }

    /**
     * This is a simple implementation of a processor chain in an enum.
     */
    public enum processingStep {

        /**
         * Placeholder that doesn't do anything;
         */
        START_PROCESSING {
            @Override
            public void doThisOperation() {
            }
        },

        LOAD_PROPERTIES {
            @Override
            public void doThisOperation() throws UpdateIdentifierException {
                final String configDir = System.getProperty(CONFIG_DIR);
                if (configDir == null) {
                    throw new UpdateIdentifierException("Must set System Property '" + CONFIG_DIR + "'");
                }
                File inputFile = new File(configDir, BUNDLE + ".properties");
                InputStream source;
                try {
                    source = new FileInputStream(inputFile);
                } catch (FileNotFoundException e) {
                    throw new UpdateIdentifierException(e);
                }
                props = new Properties();
                try {
                    props.load(source);
                } catch (IOException e) {
                    throw new UpdateIdentifierException(e);
                }
            }
        },

        VERIFY_PROPERTIES {
            @Override
            public void doThisOperation() {
                checkKey(LOCKFILE_DIR);
                checkKey(TIMESTAMP_FILE_DIR);
                checkKey(DOMS_ENDPOINT);
                checkKey(DOMS_USERNAME);
                checkKey(DOMS_PASSWORD);
            }
        },

        /**
         * creates a lockfile so that only one instance can run at a time.
         */
        CREATE_LOCKFILE {
            @Override
            public void doThisOperation() throws UpdateIdentifierException {
                File lockDir = new File(props.getProperty(LOCKFILE_DIR));
                if (lockDir.exists()) {
                    if (!lockDir.isDirectory()) {
                        throw new UpdateIdentifierException(lockDir.getAbsolutePath() + " exists but is not a directory");
                    }
                } else {
                    if (!lockDir.mkdirs()) {
                        throw new UpdateIdentifierException("Could not create directory '" + lockDir.getAbsolutePath() + "'");
                    }
                }
                lockFileFile = new File(lockDir, LOCKFILE);
                logger.info("Creating lockfile '" + lockFileFile.getAbsolutePath() + "'");
                try {
                    lockFileFile.createNewFile();
                } catch (IOException e) {
                    throw new UpdateIdentifierException(e);
                }

            }
        },

        READ_STARTTIME {
            @Override
            public void doThisOperation() throws UpdateIdentifierException {
                timestampFileFile = new File(props.getProperty(TIMESTAMP_FILE_DIR), TIMESTAMP_FILE);
                if (!timestampFileFile.exists()) {
                    throw new UpdateIdentifierException("'" + timestampFileFile.getAbsolutePath() + "' does not exist");
                }
                FileInputStream is;
                try {
                    is = new FileInputStream(timestampFileFile);
                } catch (FileNotFoundException e) {
                    throw new UpdateIdentifierException(e);
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                try {
                    String sinceS = reader.readLine();
                    try {
                        since = Long.parseLong(sinceS);
                    } catch (NumberFormatException e) {
                        throw new UpdateIdentifierException(e);
                    }
                } catch (IOException e) {
                    throw new UpdateIdentifierException(e);
                } finally {
                    try {
                        is.close();
                    } catch (IOException e) {
                        throw new UpdateIdentifierException(e);
                    }
                }
            }
        },

        WRITE_NEW_STARTTIME {
            @Override
            public void doThisOperation() throws UpdateIdentifierException {
                String now = "" + (new Date()).getTime();
                logger.info("Deleting '" + timestampFileFile + "'");
                timestampFileFile.delete();
                if (timestampFileFile.exists()) {
                    throw new UpdateIdentifierException("Did not successfully delete '" + timestampFileFile + "'");
                }
                FileWriter writer;
                try {
                    writer = new FileWriter(timestampFileFile);
                } catch (IOException e) {
                    throw new UpdateIdentifierException(e);
                }
                BufferedWriter bwriter = new BufferedWriter(writer);
                try {
                    bwriter.append(now);
                } catch (IOException e) {
                    throw new UpdateIdentifierException(e);
                } finally {
                    try {
                        bwriter.close();
                    } catch (IOException e) {
                        throw new UpdateIdentifierException(e);
                    }
                }
            }
        },

        CALL_DOMS {
            @Override
            public void doThisOperation() throws UpdateIdentifierException {
                String domsURL = props.getProperty(DOMS_ENDPOINT);
                String user = props.getProperty(DOMS_USERNAME);
                String password = props.getProperty(DOMS_PASSWORD);
                DomsClient.initializeSingleton(domsURL, user, password);
                CentralWebservice domsAPI = DomsClient.getDomsAPI();
                try {
                    logger.info("Querying DOMS for records modified since " + new Date(since));
                    domsRecords =
                            domsAPI.getIDsModified(since, "doms:RadioTV_Collection", "BES", "A", 0, -1);
                    logger.info(domsRecords.size() + " results returned");
                } catch (Exception e) {
                    throw new UpdateIdentifierException(e);
                }
            }
        },

        WRITE_RESULTS {
            @Override
            public void doThisOperation() throws UpdateIdentifierException {
                 for (RecordDescription record: domsRecords) {
                     String recordS = "Found updated pid = '" + record.getPid()
                             + " Entry CM = " + record.getEntryContentModelPid()
                             + " Date = " + (new Date(record.getDate()));
                     logger.info(recordS);
                 }
            }
        },

        CLEANUP {
            @Override
            public void doThisOperation() {
                lockFileFile.delete();
            }
        };

        private static void checkKey(String key) {
            if (!props.containsKey(key)) {
                throw new RuntimeException("Properties does not contain '" + key + "'");
            }
        }


        private processingStep nextStep;

        /**
         * Set the next step to be executed after this element. The default is
         * null, meaning that this is the last element.
         * @param step
         */
        public void setNextStep(processingStep step) {
            nextStep = step;
        }

        /**
         * Do the operation defined by this element and any subsequent elements.
         */
        public void doOperation() throws UpdateIdentifierException {
            logger.info("Doing operation '" + this.name() + "'");
            doThisOperation();
            if (nextStep != null) nextStep.doOperation();
        }

        /**
         * Do just the operation for this element alone.
         */
        public abstract void doThisOperation() throws UpdateIdentifierException ;

        static Long startTime;
        static Long endTime;
        static Properties props;
    }



    /**
     * Fetches a list of shards update since the last run of this application and writes them to a
     * file.
     * @param args There are no arguments
     */
    public static void main(String[] args) throws UpdateIdentifierException {
        processingStep start = processingStep.START_PROCESSING;
        processingStep load = processingStep.LOAD_PROPERTIES;
        processingStep verify = processingStep.VERIFY_PROPERTIES;
        processingStep lock = processingStep.CREATE_LOCKFILE;
        processingStep read = processingStep.READ_STARTTIME;
        processingStep write = processingStep.WRITE_NEW_STARTTIME;
        processingStep call = processingStep.CALL_DOMS;
        processingStep writeResults = processingStep.WRITE_RESULTS;
        processingStep cleanup = processingStep.CLEANUP;
        start.setNextStep(load);
        load.setNextStep(verify);
        verify.setNextStep(lock);
        lock.setNextStep(read);
        read.setNextStep(write);
        write.setNextStep(call);
        call.setNextStep(writeResults);
        writeResults.setNextStep(cleanup);
        try {
            start.doOperation();
        } finally {
            try {
                processingStep.CLEANUP.doThisOperation();
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }

}
