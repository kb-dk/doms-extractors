/* $Id: FlashEstimatorProcessor.java 1762 2011-03-15 15:03:07Z csrster $
 * $Revision: 1762 $
 * $Date: 2011-03-15 16:03:07 +0100 (Tue, 15 Mar 2011) $
 * $Author: csrster $
 *
 * The Netarchive Suite - Software to harvest and preserve websites
 * Copyright 2004-2009 Det Kongelige Bibliotek and Statsbiblioteket, Denmark
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *   USA
 */
package dk.statsbiblioteket.doms.radiotv.extractor.transcoder.extractor;

import dk.statsbiblioteket.doms.radiotv.extractor.Constants;

import javax.servlet.ServletConfig;

import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.*;

import org.apache.log4j.Logger;

public class DigitvEstimatorProcessor extends ProcessorChainElement {

	private static Logger log = Logger.getLogger(DigitvEstimatorProcessor.class);

	@Override
	protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
		Long additionalStartOffset = -request.getUserAdditionalStartOffset() + Long.parseLong(Util.getInitParameter(config, Constants.START_OFFSET_DIGITV));
		Long additionalEndOffset =  request.getUserAdditionalEndOffset() + Long.parseLong(Util.getInitParameter(config, Constants.END_OFFSET_DIGITV));
		ClipTypeEnum clipType = request.getClipType();
		Long finalFileSizeBytes;
		if (clipType.equals(ClipTypeEnum.MUX)) {
			Integer audioBitrate = new Integer(256);
			Integer videoBitrate = new Integer(3000);
			Integer overhead = new Integer(100);
			//The above rates are kilobit/second
			finalFileSizeBytes = (request.getTotalLengthSeconds() + additionalStartOffset + additionalEndOffset)
					*(audioBitrate + videoBitrate + overhead)*1000L/8L;
		} else if (clipType.equals(ClipTypeEnum.MPEG1) || clipType.equals(ClipTypeEnum.MPEG2)) {
			Integer audioBitrate = new Integer(448);
			Integer videoBitrate = new Integer(5700);
			Integer overhead = new Integer(100);
			//The above rates are kilobit/second
			finalFileSizeBytes = (request.getTotalLengthSeconds() + additionalStartOffset + additionalEndOffset)*(audioBitrate + videoBitrate + overhead)*1000L/8L;
		} else {
			Integer audioBitrate = 256;
			Integer videoBitrate = 0;
			Integer overhead = 10;
			//The above rates are kilobit/second
			finalFileSizeBytes = (request.getTotalLengthSeconds() + additionalStartOffset + additionalEndOffset)*(audioBitrate + videoBitrate + overhead)*1000L/8L;
		}
		request.setFinalFileLengthBytes(finalFileSizeBytes);
		log.debug("Estimated filesize for '" + request.getPid() + "' :" + finalFileSizeBytes);
	}
}
