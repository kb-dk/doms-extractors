package dk.statsbiblioteket.doms.radiotv.extractor.updateidentifier;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: csr
 * Date: 12/12/11
 * Time: 10:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateIdentifierApplication {

    private static Logger logger = Logger.getLogger(UpdateIdentifierApplication.class);


    /**
     * This is a simple implementation of a processor chain in an enum.
     */
    public enum processingStep {

        START_PROCESSING {
            @Override
            public void doThisOperation() {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        },

        CREATE_LOCKFILE {
            @Override
            public void doThisOperation() {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        },

        READ_STARTTIME {
            @Override
            public void doThisOperation() {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        },

        WRITE_NEW_STARTTIME {
            @Override
            public void doThisOperation() {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        },

        CALL_DOMS {
            @Override
            public void doThisOperation() {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        },

        CLEANUP {
            @Override
            public void doThisOperation() {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        };



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
        public void doOperation() {
            logger.info("Doing operation '" + this.name() + "'");
            doThisOperation();
            if (nextStep != null) nextStep.doOperation();
        }

        /**
         * Do just the operation for this element alone.
         */
        public abstract void doThisOperation();


    }



    /**
     * Fetches a list of shards update since the last run of this application and writes them to a
     * file.
     * @param args There are no arguments
     */
    public static void main(String[] args) {
        processingStep step1 = processingStep.START_PROCESSING;
        processingStep step2 = processingStep.CREATE_LOCKFILE;
        processingStep step3 = processingStep.READ_STARTTIME;
        processingStep step4 = processingStep.WRITE_NEW_STARTTIME;
        processingStep step5 = processingStep.CALL_DOMS;
        processingStep step6 = processingStep.CLEANUP;
        step1.setNextStep(step2);
        step2.setNextStep(step3);
        step3.setNextStep(step4);
        step4.setNextStep(step5);
        step5.setNextStep(step6);
        step1.doOperation();
    }

}
