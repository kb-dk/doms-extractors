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
import sun.misc.BASE64Encoder;

import javax.servlet.ServletConfig;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class ShardFetcherProcessor extends ProcessorChainElement {

    /**
     * Fetches the shard xml.
     * Pre-condition: request has a valid shard-url
     * Post-condition: request has valid xml as shard
     * @param request
     * @param config
     * @throws ProcessorException
     */
    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        URL url = request.getShardUrl();
        try {
            URLConnection conn = url.openConnection();
            String userPassword = config.getInitParameter(Constants.DOMS_USER)+":"+config.getInitParameter(Constants.DOMS_PASSWORD);
            String encoding = (new BASE64Encoder()).encode(userPassword.getBytes());
            conn.setRequestProperty("Authorization", "Basic " + encoding);
            String contentEncoding = conn.getContentEncoding();
            InputStream is = conn.getInputStream();
            try {
                Writer writer = new StringWriter();
                char[] buffer = new char[1024];
                Reader reader = new BufferedReader(new InputStreamReader(is, contentEncoding));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
                request.setShard(writer.toString());
            } finally {
                is.close();
            }
        } catch (IOException e) {
            throw new ProcessorException(e);
        }
    }
}
