package dk.statsbiblioteket.doms.radiotv.extractor.transcoder;

import javax.servlet.ServletConfig;

import org.apache.log4j.Logger;

import dk.statsbiblioteket.doms.radiotv.extractor.Constants;

public class MediaFileFinderFactory {

	private static Logger log = Logger.getLogger(MediaFileFinderFactory.class);

	public static MediaFileFinder createMediaFileFinder(ServletConfig config) {
		String fileLocatorConfigUrl = config.getInitParameter(Constants.FILE_LOCATOR_URL);
		String serviceBaseUrl;
		if (fileLocatorConfigUrl.endsWith("?")) {
			log.warn("File locator url should not end with '?'. See property " + Constants.FILE_LOCATOR_URL);
			serviceBaseUrl = fileLocatorConfigUrl.substring(0, fileLocatorConfigUrl.length()-1);
		} else {
			serviceBaseUrl = fileLocatorConfigUrl;
		}
		return new NearLineMediaFileFinder(serviceBaseUrl);
	}

}
