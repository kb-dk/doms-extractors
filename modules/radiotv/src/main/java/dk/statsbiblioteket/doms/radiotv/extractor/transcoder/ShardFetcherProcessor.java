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
import sun.misc.BASE64Encoder;

import javax.servlet.ServletConfig;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class ShardFetcherProcessor extends ProcessorChainElement {

    private static Logger log = Logger.getLogger(ShardFetcherProcessor.class);

    /**
     * Fetches the shard xml.
     * The original shard-url: http://www.statsbiblioteket.dk/doms/shard/uuid:ef8ea1b2-aaa8-412a-a247-af682bb57d25
     * needs to be resolved to <DOMS_SERVER>/fedora/objects/uuid%3Aef8ea1b2-aaa8-412a-a247-af682bb57d25/datastreams/SHARD_METADATA/content
     * Pre-condition: request has a valid shard-url, config has username/password to fedora and address of doms server
     * Post-condition: request has valid xml as shard
     * @param request
     * @param config
     * @throws ProcessorException
     */
    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        URL url = Util.getDomsUrl(request.getPid(), config);
        log.info("Getting shard from '" + url + "'");
        try {
            URLConnection conn = url.openConnection();
            String userPassword = Util.getInitParameter(config, Constants.DOMS_USER)+":"+Util.getInitParameter(config, Constants.DOMS_PASSWORD);
            String encoding = (new BASE64Encoder()).encode(userPassword.getBytes());
            conn.setRequestProperty("Authorization", "Basic " + encoding);
            InputStream is = conn.getInputStream();
            try {
                Writer writer = new StringWriter();
                char[] buffer = new char[1024];
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
                request.setShard(writer.toString());
                log.debug("Downloaded shard\n" + request.getShard());
            } finally {
                is.close();
            }
        } catch (IOException e) {
            throw new ProcessorException(e);
        }
    }
}
