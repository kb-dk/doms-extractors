package dk.statsbiblioteket.doms.radiotv.extractor.updateidentifier;

import dk.statsbiblioteket.util.Files;
import junit.framework.TestCase;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

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

    public void testMain() throws IOException, UpdateIdentifierApplication.UpdateIdentifierException {
        File data = new File("test/data");
        File working = new File("test/working");
        if (working.exists()) {
            Files.delete(working);
        }
        Files.copy(data, working, true);
        UpdateIdentifierApplication.main(new String[]{});
        File outputDir = new File("test/working/update_identifier_outputdir");
        File[] files = outputDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".txt");
            }
        });
        assertEquals(1, files.length);
        assertTrue(files[0].length() > 100L);
        Files.delete(working);
    }

}
