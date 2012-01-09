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
package dk.statsbiblioteket.doms.radiotv.extractor;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

public class GrizzlyTestCase extends TestCase {


    final String baseUri = "http://localhost:9998/";
    SelectorThread threadSelector;


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("com.sun.jersey.config.property.packages", "dk.statsbiblioteket.doms.radiotv.extractor");
        initParams.put(Constants.TEMP_DIR_INIT_PARAM, "tempdir");
        initParams.put(Constants.FINAL_DIR_INIT_PARAM, "clips");
        System.out.println("Starting grizzly...");
        threadSelector = GrizzlyWebContainerFactory.create(baseUri, initParams);
        System.out.println("Grizzly started.");
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.out.println("Stopping grizzly...");
        threadSelector.stopEndpoint();
        System.out.println("Grizzly stopped.");
    }

}
