package dk.statsbiblioteket.doms.radiotv.extractor.updateidentifier;

import dk.statsbiblioteket.doms.central.CentralWebservice;
import dk.statsbiblioteket.doms.central.InvalidCredentialsException;
import dk.statsbiblioteket.doms.central.InvalidResourceException;
import dk.statsbiblioteket.doms.central.MethodFailedException;
import dk.statsbiblioteket.doms.central.ObjectProfile;
import dk.statsbiblioteket.doms.central.Relation;
import dk.statsbiblioteket.doms.radiotv.extractor.DomsClient;
import dk.statsbiblioteket.util.Files;
import dk.statsbiblioteket.util.xml.DOM;
import junit.framework.TestCase;

import javax.xml.crypto.dsig.dom.DOMSignContext;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

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

    public void testClient() throws InvalidCredentialsException, MethodFailedException, InvalidResourceException {
        DomsClient.initializeSingleton( "http://alhena:7880/centralWebservice-service/central/",
                "fedoraReadOnlyAdmin",
                "fedoraReadOnlyPass");
        CentralWebservice service = DomsClient.getDomsAPI();
        List<Relation> relations = service.getInverseRelations("uuid:8f16e6a0-9967-4e3d-9a51-d2dc76a45bb2");
        for (Relation relation: relations) {
            String program = relation.getSubject();
            ObjectProfile profile = service.getObjectProfile(program);
            String stream = service.getDatastreamContents(program, "PBCORE");
            System.out.println(stream);
        }
    }


}
