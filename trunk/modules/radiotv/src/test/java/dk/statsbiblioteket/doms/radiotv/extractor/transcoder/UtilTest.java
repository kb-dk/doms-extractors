package dk.statsbiblioteket.doms.radiotv.extractor.transcoder;

import junit.framework.TestCase;

import javax.sound.midi.VoiceStatus;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public void testGetRequestFromLockFile() {
    	String lockFilename = "8a90f338-566a-0410-bc61-9ec4cb1a6b14_-400_300_Filename_prefix.DIGITV_BROADCAST_EXTRACTION.lck";
    	TranscodeRequest request = Util.getRequestFromLockFile(lockFilename);
		assertEquals(request.getKeyForLockFilename()+".lck", lockFilename);
    }
}
