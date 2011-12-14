package dk.statsbiblioteket.doms.radiotv.extractor.updateidentifier;

import org.apache.log4j.Logger;

import java.io.*;
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
    private static final String CONFIG_DIR =  "dk.statsbiblioteket.radiotv.extractor.updateidentifier.configdir";

     /**
     * Immediately below here is the list of parameters which must be defined in update_identfier.properties
     */
    private static final String LOCKFILE_DIR = "dk.statsbiblioteket.radiotv.extractor.updateidentifier.lockfiledir";

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
            public void doThisOperation() {
                checkKey(LOCKFILE_DIR);

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

         FIND_PIDFILES{
             @Override
             public void doThisOperation() throws BroadcastExtractorException {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
         },

         CALL_BES{
             @Override
             public void doThisOperation() throws BroadcastExtractorException {
                 //To change body of implemented methods use File | Settings | File Templates.
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









    public static void main(String[] args) {

    }
}
