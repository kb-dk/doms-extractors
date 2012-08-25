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

import org.apache.log4j.Logger;
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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class parses a shard element in the TranscodeRequest into a
 * set fo file clips.
 */
public class ShardParserProcessor extends ProcessorChainElement {

    private static Logger log = Logger.getLogger(ShardParserProcessor.class);

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
     * @throws MediaFileNotFoundException 
     */
    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        try {
            doParse(request, config);
        } catch (XPathExpressionException e) {
            throw new ProcessorException(e);
        } catch (ParserConfigurationException e) {
            throw new ProcessorException(e);
        } catch (IOException e) {
            throw new ProcessorException(e);
        } catch (SAXException e) {
            throw new ProcessorException(e);            
        } catch (ClassNotFoundException e) {
            throw new ProcessorException(e);
        } catch (NoSuchMethodException e) {
            throw new ProcessorException(e);
        } catch (InstantiationException e) {
            throw new ProcessorException(e);
        } catch (IllegalAccessException e) {
            throw new ProcessorException(e);
        } catch (InvocationTargetException e) {
            throw new ProcessorException(e);
        } catch (MediaFileNotFoundException e) {
            throw new ProcessorException(e);
		}
    }

    private void doParse(TranscodeRequest request, ServletConfig config) throws XPathExpressionException, ParserConfigurationException, IOException, SAXException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException, ProcessorException, MediaFileNotFoundException {
        Long totalLengthSeconds = 0L;
        List<TranscodeRequest.FileClip> clips = new ArrayList<TranscodeRequest.FileClip>();
        request.setClips(clips);
        DocumentBuilder builder = null;
        Document shardMetadataDocument = null;
        XPathFactory xpathFactory = XPathFactory.newInstance();
        NodeList files = null;
        builder = dbf.newDocumentBuilder();
        ByteArrayInputStream is = new ByteArrayInputStream(request.getShard().getBytes());
        shardMetadataDocument = builder.parse(is);
        files = (NodeList) xpathFactory.newXPath().evaluate("//file", shardMetadataDocument, XPathConstants.NODESET);
        Map<String, String> filenameToFilenameWithPathMap = requestFilesOnlineAndGetFileLocationMap(config, xpathFactory, files);
        for (int i=0; i<files.getLength(); i++) {
            Node fileNode = files.item(i);
            String url = (String) xpathFactory.newXPath().evaluate("file_url", fileNode, XPathConstants.STRING);
            String channelIdString = (String) xpathFactory.newXPath().evaluate("channel_id", fileNode, XPathConstants.STRING);
            String startOffset = (String) xpathFactory.newXPath().evaluate("program_start_offset", fileNode, XPathConstants.STRING);
            String length = (String) xpathFactory.newXPath().evaluate("program_clip_length", fileNode, XPathConstants.STRING);
            String fileName = (String) xpathFactory.newXPath().evaluate("file_name", fileNode, XPathConstants.STRING);
            String filePath = filenameToFilenameWithPathMap.get(fileName);
            TranscodeRequest.FileClip clip = new TranscodeRequest.FileClip(filePath);
            clips.add(clip);
            request.setClipType(ClipTypeEnum.getType(request));
            Long bitRate = request.getClipType().getBitrate();
            Long startSeconds = null;
            Long lengthSeconds = null;
            if (startOffset != null && !"".equals(startOffset)) {
                startSeconds = Long.parseLong(startOffset);
                clip.setStartOffsetBytes(startSeconds*bitRate);

            }
            if (length != null && !"".equals(length)) {
                lengthSeconds = Long.parseLong(length);
                totalLengthSeconds += lengthSeconds;
                clip.setClipLength(lengthSeconds*bitRate);
            }
            if (length == null || "".equals(length)) {
                if (startSeconds == null) {
                    totalLengthSeconds += 3600L;
                } else {
                    totalLengthSeconds += 3600L - startSeconds;
                }

            }
            if (startSeconds == 0) clip.setStartOffsetBytes(null);
            if (startSeconds != null && lengthSeconds != null && startSeconds + lengthSeconds == 3600 && fileName.startsWith("mux")) {
                clip.setClipLength(null);
            }
            if (channelIdString != null && !"".equals(channelIdString)) {
                clip.setProgramId(Integer.parseInt(channelIdString));
            }
            log.debug("Added a clip '" + clip + "'");
        }
        request.setTotalLengthSeconds(totalLengthSeconds);
        final ClipTypeEnum clipType = ClipTypeEnum.getType(request);
        log.info("Clip type for '" + request.getPid() + "' found: '" + clipType.name() + "'");
        request.setClipType(clipType);

        log.debug("Total length set to '" + request.getTotalLengthSeconds() + "'");
    }

	public Map<String, String> requestFilesOnlineAndGetFileLocationMap(ServletConfig config, XPathFactory xpathFactory, NodeList files) throws XPathExpressionException, IOException, MediaFileNotFoundException {
		List<String> filenames = new ArrayList<String>();
        for (int i=0; i<files.getLength(); i++) {
            Node fileNode = files.item(i);
            String filename = (String) xpathFactory.newXPath().evaluate("file_name", fileNode, XPathConstants.STRING);
            filenames.add(filename);
        }
        MediaFileFinder finder = MediaFileFinderFactory.createMediaFileFinder(config);
        Map<String, String> filenameToFilenameWithPathMap = finder.getFilePath(filenames);
		return filenameToFilenameWithPathMap;
	}


}
