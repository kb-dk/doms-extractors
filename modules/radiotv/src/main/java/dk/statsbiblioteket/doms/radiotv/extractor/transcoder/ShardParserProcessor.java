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

import com.sun.org.apache.xpath.internal.NodeSet;
import dk.statsbiblioteket.doms.radiotv.extractor.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.servlet.ServletConfig;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This class parses a shard element in the TranscodeRequest into a
 * set fo file clips.
 */
public class ShardParserProcessor extends ProcessorChainElement {

    private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    /**
     * Precondition: request.getShard() returns an xml fragment with the form
     * <shard_metadata><file>

     <file_url>http://bitfinder.statsbiblioteket.dk/bart/mux1.1258135200-2009-11-13-19.00.00_1258138800-2009-11-13-20.00.00_dvb1-2.ts</file_url>
     <channel_id>102</channel_id>
     <program_start_offset>1800</program_start_offset>
     <program_clip_length>1800</program_clip_length>

     <file_name>mux1.1258135200-2009-11-13-19.00.00_1258138800-2009-11-13-20.00.00_dvb1-2.ts</file_name>
     <format_uri>info:pronom/x-fmt/386</format_uri>
     </file><file>

     <file_url>http://bitfinder.statsbiblioteket.dk/bart/mux1.1258135200-2009-11-13-19.00.00_1258138800-2009-11-13-20.00.00_dvb1-2.ts</file_url>
     <channel_id>102</channel_id>
     <program_start_offset>1800</program_start_offset>
     <program_clip_length>1800</program_clip_length>

     <file_name>mux1.1258135200-2009-11-13-19.00.00_1258138800-2009-11-13-20.00.00_dvb1-2.ts</file_name>
     <format_uri>info:pronom/x-fmt/386</format_uri>
     </file></shard_metadata>
     *
     * The method parses the file elements into a list of FileClip objects
     * post-condition: request.getClips() returns a List of FileClip objects.
     * Byte offsets and lengths are calculated using bitrates based on the filenames. These values are hard-coded in
     * this class.
     *
     * @param request
     * @param config
     */
    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) {
        try {
            doParse(request, config);
        } catch (XPathExpressionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ParserConfigurationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (SAXException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
     }

    private void doParse(TranscodeRequest request, ServletConfig config) throws XPathExpressionException, ParserConfigurationException, IOException, SAXException {
       String finderUrl = config.getInitParameter(Constants.FILE_LOCATOR_URL);
        List<TranscodeRequest.FileClip> clips = new ArrayList<TranscodeRequest.FileClip>();
        DocumentBuilder builder = null;
        Document shardMetadataDocument = null;
        XPathFactory xpathFactory = XPathFactory.newInstance();
        NodeList files = null;
        builder = dbf.newDocumentBuilder();
        ByteArrayInputStream is = new ByteArrayInputStream(request.getShard().getBytes());
        shardMetadataDocument = builder.parse(is);
        files = (NodeList) xpathFactory.newXPath().evaluate("//file", shardMetadataDocument, XPathConstants.NODESET);
        for (int i=0; i<files.getLength(); i++) {
            Node fileNode = files.item(i);
            String url = (String) xpathFactory.newXPath().evaluate("file_url", fileNode, XPathConstants.STRING);
            String channelIdString = (String) xpathFactory.newXPath().evaluate("channel_id", fileNode, XPathConstants.STRING);
            String startOffset = (String) xpathFactory.newXPath().evaluate("program_start_offset", fileNode, XPathConstants.STRING);
            String length = (String) xpathFactory.newXPath().evaluate("program_clip_length", fileNode, XPathConstants.STRING);
            String fileName = (String) xpathFactory.newXPath().evaluate("file_name", fileNode, XPathConstants.STRING);
            String filePath = getFilePath(fileName, finderUrl);
            TranscodeRequest.FileClip clip = new TranscodeRequest.FileClip(filePath);
            Long bitRate = getBitrate(fileName);
            if (startOffset != null && !"".equals(startOffset)) {
                Long startSeconds = Long.parseLong(startOffset);
                clip.setStartOffsetBytes(startSeconds*bitRate);
            }
            if (length != null && !"".equals(length)) {
                Long lengthSeconds = Long.parseLong(length);
                clip.setClipLength(lengthSeconds*bitRate);
            }
            if (channelIdString != null && !"".equals(channelIdString)) {
                clip.setProgramId(Integer.parseInt(channelIdString));
            }
            clips.add(clip);
        }
        request.setClips(clips);
    }

    private String getFilePath(String fileName, String finderUrl) throws IOException {
        String url = finderUrl+fileName;
        URL url1 = new URL(url);
        InputStream is = url1.openStream();
        String result = (new BufferedReader(new InputStreamReader(is))).readLine();
        is.close();
        return result.trim();
    }

    /**
     * Get the bitrate in bytes per second from the filename.
     * @param filename
     * @return
     */
    private Long getBitrate(String filename) {
          if (filename.startsWith("mux")) {
              return 2488237L;
          } else if (filename.contains("_mpeg1_")) {
              return 169242L;
          }  else if (filename.contains("_mpeg2_")) {
              return 872254L;
          }  else if (filename.contains("_wav_")) {
              return 88200L;
          } else return null;
    }

}
