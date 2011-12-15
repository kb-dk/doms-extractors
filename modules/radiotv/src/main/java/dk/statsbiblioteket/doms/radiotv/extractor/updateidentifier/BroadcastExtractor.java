package dk.statsbiblioteket.doms.radiotv.extractor.updateidentifier;

import com.sun.deploy.util.UpdateCheck;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.util.Properties;

/**
 * A utility for calling BroadcastExtractionService with pid's of new or
 * altered program-shards.
 */
public class BroadcastExtractor {

    private static Logger logger = Logger.getLogger(BroadcastExtractor.class);

    /**
     * The name of the properties bundle containing the configuration parameters.
     */
    private static final String BUNDLE = "update_identifier";

    /**
     * The environment variable pointing to the directory containing the properties file.
     */
    private static final String CONFIG_DIR = UpdateIdentifierApplication.CONFIG_DIR;

     /**
     * Immediately below here is the list of parameters which must be defined in update_identfier.properties
     */
    private static final String LOCKFILE_DIR = UpdateIdentifierApplication.LOCKFILE_DIR;
    private static final String PIDFILE_DIR = UpdateIdentifierApplication.OUTPUT_DIR;
    private static final String BES_ENDPOINTS = "dk.statsbiblioteket.radiotv.extractor.updateidentifier.besendpoints";
    private static final String BES_PAUSE_MILLISECONDS = "dk.statsbiblioteket.radiotv.extractor.updateidentifier.bespause_milliseconds";



    /**
     * Use the same lockfile as UpdateIdentifier so that only one of them can run at a time
     */
    private static final String LOCKFILE = "lockfile";

    public static class BroadcastExtractorException extends Exception {
        public BroadcastExtractorException(Throwable cause) {
            super(cause);
        }

        public BroadcastExtractorException(String message) {
            super(message);
        }
    }

     private static File lockFileFile;
    private static File[] pidFiles;


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
            public void doThisOperation() throws BroadcastExtractorException {
                final String configDir = System.getProperty(CONFIG_DIR);
                if (configDir == null) {
                    throw new BroadcastExtractorException("Must set System Property '" + CONFIG_DIR + "'");
                }
                File inputFile = new File(configDir, BUNDLE + ".properties");
                InputStream source;
                try {
                    source = new FileInputStream(inputFile);
                } catch (FileNotFoundException e) {
                    throw new BroadcastExtractorException(e);
                }
                props = new Properties();
                try {
                    props.load(source);
                } catch (IOException e) {
                    throw new BroadcastExtractorException(e);
                }
            }
        },

        VERIFY_PROPERTIES {
            @Override
            public void doThisOperation() throws BroadcastExtractorException {
                checkKey(LOCKFILE_DIR);
                checkKey(PIDFILE_DIR);
                checkKey(BES_ENDPOINTS);
                checkKey(BES_PAUSE_MILLISECONDS);
            }
        },

        /**
         * creates a lockfile so that only one instance can run at a time.
         */
        CREATE_LOCKFILE {
            @Override
            public void doThisOperation() throws BroadcastExtractorException {
                File lockDir = new File(props.getProperty(LOCKFILE_DIR));
                if (lockDir.exists()) {
                    if (!lockDir.isDirectory()) {
                        throw new BroadcastExtractorException(lockDir.getAbsolutePath() + " exists but is not a directory");
                    }
                } else {
                    if (!lockDir.mkdirs()) {
                        throw new BroadcastExtractorException("Could not create directory '" + lockDir.getAbsolutePath() + "'");
                    }
                }
                lockFileFile = new File(lockDir, LOCKFILE);
                if (lockFileFile.exists()) {
                    logger.warn("Found lockfile '" + lockFileFile.getAbsolutePath() + "'. Previous run is not yet finished. Exiting.");
                    System.exit(-1);
                }
                logger.info("Creating lockfile '" + lockFileFile.getAbsolutePath() + "'");
                try {
                    lockFileFile.createNewFile();
                } catch (IOException e) {
                    throw new BroadcastExtractorException(e);
                }

            }
        },

         FIND_PIDFILES {
             @Override
             public void doThisOperation() throws BroadcastExtractorException {
                 File pidDir = new File(props.getProperty(PIDFILE_DIR));
                 if (!pidDir.exists() && !pidDir.mkdirs()) {
                      throw new BroadcastExtractorException("Could not create directory '" + pidDir.getAbsolutePath() + "'");
                 }
                 if (!pidDir.isDirectory()) {
                     throw new BroadcastExtractorException("Not a directory '" + pidDir.getAbsolutePath() + "'");
                 }
                 pidFiles = pidDir.listFiles(new FileFilter() {
                     @Override
                     public boolean accept(File pathname) {
                          return pathname.isFile();
                     }
                 });
             }
         },

         CALL_BES {
             @Override
             public void doThisOperation() throws BroadcastExtractorException {
                 String[] besEndpoints = props.getProperty(BES_ENDPOINTS).split(",");
                 int nEndpoints = besEndpoints.length;
                 int currentEndpoint = 0;
                 long sleepTime = Long.parseLong(props.getProperty(BES_PAUSE_MILLISECONDS));
                 String additionalUrlComponent = "/forcetranscode?propgrampid=";
                 for (File pidFile: pidFiles) {
                     BufferedReader reader;
                     try {
                         reader = new BufferedReader(new FileReader(pidFile));
                     } catch (FileNotFoundException e) {
                         throw new BroadcastExtractorException(e);
                     }
                     String pid;
                     try {
                         while ((pid = reader.readLine()) != null) {
                             logger.debug("Found pid '" + pid + "'");
                             if (pid.startsWith("uuid:")) {
                                 String endPoint = besEndpoints[currentEndpoint];
                                 String besUrl = endPoint + additionalUrlComponent + pid;
                                 logger.debug("Opening '" + besUrl + "'");
                                 URL url = new URL(besUrl);
                                 Object content = url.getContent();
                                 logger.debug(content);
                                 try {
                                     Thread.sleep(sleepTime);
                                 } catch (InterruptedException e) {
                                     throw new BroadcastExtractorException(e);
                                 }
                                 currentEndpoint = (currentEndpoint + 1) % nEndpoints;
                             }
                         }
                     } catch (IOException e) {
                         throw new BroadcastExtractorException(e);
                     }
                 }
             }
         },


         CLEANUP {
            @Override
            public void doThisOperation() {
                lockFileFile.delete();
            }
        };


         private static void checkKey(String key) throws BroadcastExtractorException {
            if (!props.containsKey(key)) {
                throw new BroadcastExtractorException("Properties does not contain '" + key + "'");
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
        public void doOperation() throws BroadcastExtractorException {
            logger.info("Doing operation '" + this.name() + "'");
            doThisOperation();
            if (nextStep != null) nextStep.doOperation();
        }

        /**
         * Do just the operation for this element alone.
         */
        public abstract void doThisOperation() throws BroadcastExtractorException ;

        static Properties props;
    }

    public static void main(String[] args) throws BroadcastExtractorException {
        processingStep start = processingStep.START_PROCESSING;
        processingStep load = processingStep.LOAD_PROPERTIES;
        processingStep verify = processingStep.VERIFY_PROPERTIES;
        processingStep lock = processingStep.CREATE_LOCKFILE;
        processingStep find = processingStep.FIND_PIDFILES;
        processingStep call = processingStep.CALL_BES;
        start.setNextStep(load);
        load.setNextStep(verify);
        verify.setNextStep(lock);
        lock.setNextStep(find);
        find.setNextStep(call);
        try {
            start.doOperation();
        } finally {
            try {
                processingStep.CLEANUP.doOperation();
            } catch (Exception e) {
                logger.error(e);
            }
        }
    }
}
