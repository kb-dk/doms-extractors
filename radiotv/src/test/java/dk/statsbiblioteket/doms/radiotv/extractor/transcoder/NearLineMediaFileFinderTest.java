package dk.statsbiblioteket.doms.radiotv.extractor.transcoder;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NearLineMediaFileFinderTest {

	private static final String SERVICE_BASE_URL_STRING = "http://plufire/~bart/stage_files.cgi";
	private ArrayList<String> archiveFilenames;
	
	public NearLineMediaFileFinderTest() {
		super();
		archiveFilenames = new ArrayList<String>();
		archiveFilenames.add("mux1.1277953200-2010-07-01-05.00.00_1277956800-2010-07-01-06.00.00_dvb1-1.ts");
		archiveFilenames.add("mux1.1291302000-2010-12-02-16.00.00_1291305600-2010-12-02-17.00.00_dvb1-1.ts");
		archiveFilenames.add("mux2.1279738800-2010-07-21-21.00.00_1279742400-2010-07-21-22.00.00_dvb1-1.ts");
		archiveFilenames.add("mux2.1279735200-2010-07-21-20.00.00_1279738800-2010-07-21-21.00.00_dvb1-1.ts");
		archiveFilenames.add("dr1_196.250_K11-DR1_mpeg2_20080623055601_20080624012201_encoder2-2.mpeg");
		archiveFilenames.add("dr2_203.250_K9-DR2_mpeg2_20071110121001_20071111030001_encoder3-2.mpeg");
		archiveFilenames.add("dk4_807.250_K63-DK4_mpeg1_20080728060001_20080729000002_encoder2-2.mpeg");
		archiveFilenames.add("dk4_391.250_K63-DK4_mpeg1_20101012055602_20101013001601_encoder2-2.mpeg");
		archiveFilenames.add("tv2d_511.250_K10-TV2-Danmark_mpeg2_20090217045601_20090218045502_encoder1-2.mpeg");
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetFilePathListOfStringEmpty() throws Exception {
		NearLineMediaFileFinder mediaFileFinder = new NearLineMediaFileFinder(SERVICE_BASE_URL_STRING);
		List<String> filenames = new ArrayList<String>();
		Map<String, String> filenamesWithPathMap = mediaFileFinder.getFilePath(filenames);
		assertEquals(0, filenamesWithPathMap.size());
	}

	@Test
	public void testGetFilePathListOfStringNonExistingFile() throws IOException {
		NearLineMediaFileFinder mediaFileFinder = new NearLineMediaFileFinder(SERVICE_BASE_URL_STRING);
		List<String> filenames = new ArrayList<String>();
		filenames.add("TEST-filename_of_file_that_does_not_exist.mpeg");
		try {
			mediaFileFinder.getFilePath(filenames);
			fail("Must not reach this statement!");
		} catch (MediaFileNotFoundException e) {
			// Expecting to reach this catch clause
		}
	}

	@Test
	public void testGetFilePathListOfStringOneElement() throws Exception {
		NearLineMediaFileFinder mediaFileFinder = new NearLineMediaFileFinder(SERVICE_BASE_URL_STRING);
		List<String> filenames = new ArrayList<String>();
		filenames.add(archiveFilenames.get(0));
		Map<String, String> filenamesWithPathMap = mediaFileFinder.getFilePath(filenames);
		assertEquals(1, filenamesWithPathMap.size());
		String filename = archiveFilenames.get(0);
		assertTrue(filenamesWithPathMap.get(filename).endsWith(filename));
		assertTrue(filenamesWithPathMap.get(filename).length() > archiveFilenames.get(0).length());
	}

	@Test
	public void testGetFilePathListOfStringManyElements() throws IOException, MediaFileNotFoundException {
		NearLineMediaFileFinder mediaFileFinder = new NearLineMediaFileFinder(SERVICE_BASE_URL_STRING);
		List<String> filenames = new ArrayList<String>();
		for (int i=0;i<archiveFilenames.size();i++) {
			filenames.add(archiveFilenames.get(i));
		}
		Map<String, String> filenamesWithPathMap = mediaFileFinder.getFilePath(filenames);
		assertEquals(archiveFilenames.size(), filenamesWithPathMap.size());
		for (int i=0; i<archiveFilenames.size(); i++) {
			String filename = archiveFilenames.get(i);
			assertTrue(filenamesWithPathMap.get(filename).endsWith(filename));
			assertTrue(filenamesWithPathMap.get(filename).length() > archiveFilenames.get(0).length());
		}
	}

}
