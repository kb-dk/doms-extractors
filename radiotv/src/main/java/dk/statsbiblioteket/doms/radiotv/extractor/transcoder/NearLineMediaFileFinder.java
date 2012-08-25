package dk.statsbiblioteket.doms.radiotv.extractor.transcoder;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.rmi.UnexpectedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;

import org.apache.log4j.Logger;

public class NearLineMediaFileFinder implements MediaFileFinder {

	private static Logger log = Logger.getLogger(NearLineMediaFileFinder.class);
	
	// Service base URL is of the format: "http://plufire/~bart/stage_files.cgi" 
	private String serviceBaseUrl;

	@SuppressWarnings("unused")
	private NearLineMediaFileFinder() {}
	
	public NearLineMediaFileFinder(String serviceBaseUrl) {
		this.serviceBaseUrl = serviceBaseUrl;
	}

	@Override
	@Deprecated
	public String getFilePath(String filename, ServletConfig config) throws IOException {
		log.error("Using old interface methods in new implementation.");
		throw new UnsupportedOperationException("Old interface handle not implemented. Use others");
	}

	@Override
	public Map<String, String> getFilePath(List<String> filenames) throws IOException, MediaFileNotFoundException {
		List<String> filenamesWithPath = new ArrayList<String>();
		String query = "";
        for (int i=0; i<filenames.size(); i++) {
        	String filename = filenames.get(i);
        	String urlParameterSeparator = "";
        	if (i>0) {
        		urlParameterSeparator = "&";
        	}
        	query += urlParameterSeparator + filename;
		}
        String url = serviceBaseUrl + "?" + query;
        log.debug("Requesting URL: " + url);
        InputStream is = new URL(url).openStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line=bufferedReader.readLine())!=null) {
        	log.debug("Reading line: '" + line + "'");
        	filenamesWithPath.add(line.trim());
        }
        is.close();
        if (filenames.size() != filenamesWithPath.size()) {
        	throw new MediaFileNotFoundException("Number of returned files does not match number of requested files.");
        }
        Map<String, String> fileTofilepathMap = new HashMap<String, String>();
        for (String filename : filenames) {
        	boolean filenameFound = false;
			for (String filenameWithPath : filenamesWithPath) {
				if (filenameWithPath.endsWith(filename)) {
					filenameFound = true;
					fileTofilepathMap.put(filename, filenameWithPath);
				}
			}
			if (!filenameFound) {
				String msg = "File '" + filename + "' was not found in returned list of files with path.";
				log.error(msg + " Return value: " + filenamesWithPath);
				throw new MediaFileNotFoundException(msg);
			}
		}
        return fileTofilepathMap;
	}
}
