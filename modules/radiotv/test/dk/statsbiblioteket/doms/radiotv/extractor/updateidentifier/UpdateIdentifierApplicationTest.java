package dk.statsbiblioteket.doms.radiotv.extractor.updateidentifier;

import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: csr
 * Date: 12/12/11
 * Time: 1:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateIdentifierApplicationTest extends TestCase {

    public void testProperties() throws UpdateIdentifierApplication.UpdateIdentifierException {
        UpdateIdentifierApplication.processingStep load = UpdateIdentifierApplication.processingStep.LOAD_PROPERTIES;
        UpdateIdentifierApplication.processingStep validate = UpdateIdentifierApplication.processingStep.VERIFY_PROPERTIES;
        load.setNextStep(validate);
        load.doOperation();
    }

}
