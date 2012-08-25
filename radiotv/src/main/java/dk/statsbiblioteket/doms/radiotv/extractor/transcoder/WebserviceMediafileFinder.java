/* File:        $Id$
 * Revision:    $Revision$
 * Author:      $Author$
 * Date:        $Date$
 *
 * Copyright 2004-2009 Det Kongelige Bibliotek and Statsbiblioteket, Denmark
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package dk.statsbiblioteket.doms.radiotv.extractor.transcoder;

import dk.statsbiblioteket.doms.radiotv.extractor.Constants;
import org.apache.log4j.Logger;

import javax.mail.MethodNotSupportedException;
import javax.servlet.ServletConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class WebserviceMediafileFinder implements MediaFileFinder {

    private static Logger log = Logger.getLogger(WebserviceMediafileFinder.class);

    @Override
    @Deprecated
    public String getFilePath(String filename, ServletConfig config) throws IOException {
        String finderUrl = config.getInitParameter(Constants.FILE_LOCATOR_URL);
        String url = finderUrl+filename;
        URL url1 = new URL(url);
        InputStream is = url1.openStream();
        String result = (new BufferedReader(new InputStreamReader(is))).readLine();
        is.close();
        String trimmedResult = result.trim();
        log.debug("Found file '" + filename + "' at '" + trimmedResult + "'");
        return trimmedResult;
    }

	@Override
	public Map<String, String> getFilePath(List<String> filenames) {
		throw new UnsupportedOperationException("Old implementation does not support new functionality");
	}
}
