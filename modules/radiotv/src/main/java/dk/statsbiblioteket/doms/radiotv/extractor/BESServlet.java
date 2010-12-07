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

import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.Util;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import java.io.File;
import java.io.FileFilter;

public class BESServlet extends com.sun.jersey.spi.container.servlet.ServletContainer {

    private static Logger log = Logger.getLogger(BESServlet.class);

    @Override
    public void init() throws ServletException {
        super.init();
        Util.getTempDir(this.getServletConfig()).mkdirs();
        log.info("initialized BES service");
        cleanup();
    }

    @Override
    public void destroy() {
        super.destroy();
        log.info("destroyed BES service");
        cleanup();
    }

    private  void cleanup() {
        File[] lockFiles = Util.getAllLockFiles(this.getServletConfig());
        File mediaDir = Util.getFinalDir(this.getServletConfig());
        for (File lockFile: lockFiles) {
            final String mediaFileNamePrefix = lockFile.getName().replace(".lck", "");
            FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                       return pathname.getName().startsWith(mediaFileNamePrefix);
                 }
            };
            File[] mediaFiles = mediaDir.listFiles(filter);
            for (File mediaFile:mediaFiles) {
                log.info("Deleting unfinished media file '" + mediaFile.getAbsolutePath() + "'");
                if (!mediaFile.delete()) {
                    log.error("Failed to delete '" + mediaFile.getAbsolutePath() + "'");
                }
            }
            log.info("Deleting lock file '" + lockFile.getAbsolutePath() + "'");
            if (!lockFile.delete()) {
                log.error("Failed to delete '" + lockFile.getAbsolutePath() + "'");
            }
        }
    }

}
