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

import junit.framework.TestCase;

public class PBCoreParserProcessorTest extends TestCase {

    public void testXPaths() throws ProcessorException {
        PBCoreParserProcessor processor = new PBCoreParserProcessor();
        String pbcore = "<PBCoreDescriptionDocument xmlns=\"http://www.pbcore.org/PBCore/PBCoreNamespace.html\">" +
                "<pbcoreInstantiation>" +
                " <pbcoreDateAvailable>" +
                "<dateAvailableStart>2008-05-12T09:00:00+0200</dateAvailableStart>" +
                "<dateAvailableEnd>2008-05-12T09:07:00+0200</dateAvailableEnd>" +
                 " </pbcoreDateAvailable>" +
                "</pbcoreInstantiation>" +
                "</PBCoreDescriptionDocument>";
        TranscodeRequest request = new TranscodeRequest("foobar");
        processor.parsePBCore(request, pbcore);
    }

}
