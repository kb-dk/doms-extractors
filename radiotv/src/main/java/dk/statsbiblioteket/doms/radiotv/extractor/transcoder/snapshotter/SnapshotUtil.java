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
package dk.statsbiblioteket.doms.radiotv.extractor.transcoder.snapshotter;

import dk.statsbiblioteket.doms.radiotv.extractor.Constants;
import dk.statsbiblioteket.doms.radiotv.extractor.ExternalJobRunner;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.ExternalProcessTimedOutException;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.OutputFileUtil;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.ProcessorException;
import dk.statsbiblioteket.doms.radiotv.extractor.transcoder.Util;

import javax.servlet.ServletConfig;
import java.io.File;

public class SnapshotUtil {

    public static void imageMagickConvert(ServletConfig config, File inputFile, File outputFile) throws ProcessorException {
        final int scaleSize = Integer.parseInt(Util.getInitParameter(config, Constants.SNAPSHOT_SCALE_SIZE));
        int height = 9*scaleSize;
        int width =  16*scaleSize;
        final int quality = Integer.parseInt(Util.getInitParameter(config, Constants.SNAPSHOT_QUALITY));
        String resolution = width + "x" +height;
        String command = "convert " + inputFile.getAbsolutePath() + " -quality " + quality + "% "
                + " -thumbnail '" + resolution + "' -bordercolor black -border 150 -gravity center -crop " + resolution + "+0+0 +repage " + outputFile.getAbsolutePath();
        try {
            ExternalJobRunner.runClipperCommand(120000L, command);
        } catch (ExternalProcessTimedOutException e) {
            throw new ProcessorException(e);
        }
    }

}
