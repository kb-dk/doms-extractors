/* $Id$
 * $Revision$
 * $Date$
 * $Author$
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
package dk.statsbiblioteket.doms.radiotv.extractor.transcoder;

import javax.servlet.ServletConfig;

public class AspectRatioDetectorProcessor extends ProcessorChainElement {

    private static final long clipSize = 100000000L;

    /**
     * Detects and sets the display aspect ratio for the program. If there is a single
     * clip then it looks in the middle of the clip. If there are multiple clips then
     * it looks at the beginning of the second clip.
     * Pre-condition: non-empty clip data in the request.
     * Post-condition: request.getDisplayAspectRatio returns a non-null result.
     *
     * The command line to extract the aspect ratio is like
     * dd if=mux2.ts bs=1000 count=100000|  vlc - --intf dummy --novideo --noaudio --program 2005 --sout='#std{access=file,mux=ts,dst=-}' | ffmpeg -i - -y  -f mpeg2video /dev/null
     *
     * @param request
     * @param config
     * @throws ProcessorException
     */
    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        throw new RuntimeException("Not yet implemented");

    }
}
