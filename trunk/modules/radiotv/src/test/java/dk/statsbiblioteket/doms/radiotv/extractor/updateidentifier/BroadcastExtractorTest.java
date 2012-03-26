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
package dk.statsbiblioteket.doms.radiotv.extractor.updateidentifier;

import dk.statsbiblioteket.util.Files;
import junit.framework.TestCase;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class BroadcastExtractorTest extends TestCase {

    public void testMain() throws IOException, UpdateIdentifierApplication.UpdateIdentifierException, BroadcastExtractor.BroadcastExtractorException {
        File data = new File("test/data");
        File working = new File("test/working");
        if (working.exists()) {
            Files.delete(working);
        }
        Files.copy(data, working, true);
        BroadcastExtractor.main(new String[]{});
        Files.delete(working);
    }

}
