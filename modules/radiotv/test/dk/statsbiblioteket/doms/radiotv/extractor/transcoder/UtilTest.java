package dk.statsbiblioteket.doms.radiotv.extractor.transcoder;

import junit.framework.TestCase;

import javax.sound.midi.VoiceStatus;
import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: csr
 * Date: 7/19/11
 * Time: 10:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class UtilTest extends TestCase {

    public void testGetRelativePath() throws ProcessorException, IOException {
        File parent = new File("/net/vnx1/radio_top");
        File child = new File("/net/vnx1/radio_7/files/f/d/2/7fd220da-4c22-4d6c-ae7a-e76288c37f50.flv");
        String path = Util.getRelativePath(parent, child);
        assertEquals((new File(parent.getCanonicalPath()+File.separator+path)).getCanonicalPath(), child.getCanonicalPath());
    }

    public void testGetRelativePathSimple() throws ProcessorException, IOException {
        File parent = new File("/a/b/c");
        File child = new File("/a/b/c/d/e/file.txt");
        String path = Util.getRelativePath(parent, child);
        assertEquals("d/e/file.txt", path);
        assertEquals((new File(parent.getCanonicalPath()+File.separator+path)).getCanonicalPath(), child.getCanonicalPath());
    }

}
