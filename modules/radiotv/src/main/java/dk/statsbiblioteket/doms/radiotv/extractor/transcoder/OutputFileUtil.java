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

import javax.servlet.ServletConfig;
import java.io.File;
import java.io.FileFilter;

/**
 * Manages final output files where files like 8hjiee-.... are placed
 * in underdirectories like 8/h/j/i/8jiee-....
 */
public class OutputFileUtil {

    private static Logger log = Logger.getLogger(Util.class);
    private final static int depth = 4;

    /**
     * Get the specific directory for the putput of this request. This lies 4
     * directories down under the base output directory.
     * @param request
     * @param config
     * @return
     */
    public static File getOutputDir(TranscodeRequest request, ServletConfig config) {
        File rootDir = getBaseOutputDir(request, config);
        String relativePath = "";
        String pid = request.getPid();
        for (int pos = 0; pos < depth; pos++) {
            relativePath += pid.charAt(pos) + "/";
        }
        return new File(rootDir, relativePath);
    }

    /**
     * Get the root output directory for this type of request.
     * @param request
     * @param config
     * @return
     */
    public static File getBaseOutputDir(TranscodeRequest request, ServletConfig config) {
        String rootDir = "";
        switch (request.getServiceType()) {
             case BROADCAST_EXTRACTION:
                 rootDir = Util.getInitParameter(config, Constants.FINAL_DIR_INIT_PARAM);
                 break;
            case PREVIEW_GENERATION:
                 rootDir = Util.getInitParameter(config, Constants.PREVIEW_DIRECTORY);
                 break;
            case THUMBNAIL_GENERATION:
                rootDir = Util.getInitParameter(config, Constants.SNAPSHOT_DIRECTORY);
                break;
            case PREVIEW_THUMBNAIL_GENERATION:
                rootDir = Util.getInitParameter(config, Constants.SNAPSHOT_DIRECTORY);
                break;
        }
        return new File(rootDir);
    }

    /**
     * Determine whether any output files from this request have been created.
     * @param request
     * @param config
     * @return
     */
    public static boolean hasOutputFile(TranscodeRequest request, ServletConfig config) {
         switch(request.getServiceType()) {
            case BROADCAST_EXTRACTION:
                return hasExtractedBroadcast(request, config);
            case PREVIEW_GENERATION:
                return hasPreview(request, config);
            case THUMBNAIL_GENERATION:
                return hasSnapshot(request, config);
            case PREVIEW_THUMBNAIL_GENERATION:
                return hasPreview(request, config);
        }
        log.error("Unexpected state for " + request);
        return false;
    }

    /**
     * Return the final output directory for this request. Create it if necessary.
     * @param request
     * @param config
     * @return
     */
    public static File getAndCreateOutputDir(TranscodeRequest request, ServletConfig config) {
        File result = getOutputDir(request, config);
        result.mkdirs();
        return result;
    }

    public static File getFlashVideoOutputFile(TranscodeRequest request, ServletConfig config) {
        return new File(getOutputDir(request, config), request.getPid() + ".flv");
    }

    public static File getFlashVideoPreviewOutputFile(TranscodeRequest request, ServletConfig config) {
        return new File(getOutputDir(request, config), request.getPid() + ".preview.flv");
    }

    public static File getMP3AudioOutputFile(TranscodeRequest request, ServletConfig config) {
        return new File(getOutputDir(request, config), request.getPid() + ".mp3");
    }

    public static File getExistingMediaOutputFile(TranscodeRequest request, ServletConfig config) {
        switch(request.getServiceType()) {
            case BROADCAST_EXTRACTION:
                return getBroadcastFile(request, config);
            case PREVIEW_GENERATION:
                return getPreviewFile(request, config);
        }
        throw new RuntimeException("Didn't return a media output file for request '" + request.getPid() + "'");
    }

    private static File getBroadcastFile(TranscodeRequest request, ServletConfig config) {
        final String uuid = request.getPid();
        final FileFilter filter = new FileFilter(){
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith(uuid+".") && !pathname.getName().contains("preview");
            }
        };
        File outputDir = getOutputDir(request, config);
        return outputDir.listFiles(filter)[0];
    }

     private static File getPreviewFile(TranscodeRequest request, ServletConfig config) {
        final String uuid = request.getPid();
        final FileFilter filter = new FileFilter(){
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith(uuid+".") && pathname.getName().contains("preview");
            }
        };
        File outputDir = getOutputDir(request, config);
        return outputDir.listFiles(filter)[0];
    }

    private static boolean hasExtractedBroadcast(TranscodeRequest request, ServletConfig config) {
        final String uuid = request.getPid();
        final FileFilter filter = new FileFilter(){
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith(uuid+".") && !pathname.getName().contains("preview");
            }
        };
        File outputDir = getOutputDir(request, config);
        return outputDir.listFiles(filter).length > 0;
    }

   private static boolean hasPreview(TranscodeRequest request, ServletConfig config) {
        final String uuid = request.getPid();
        final FileFilter filter = new FileFilter(){
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith(uuid+".") && pathname.getName().contains("preview");
            }
        };
        File outputDir = getOutputDir(request, config);
        return outputDir.listFiles(filter).length > 0;
    }

    private static boolean hasSnapshot(TranscodeRequest request, ServletConfig config) {
        final String uuid = request.getPid();
        final FileFilter filter = new FileFilter(){
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().startsWith(uuid+".") && !pathname.getName().contains("preview");
            }
        };
        File outputDir = getOutputDir(request, config);
        return outputDir.listFiles(filter).length > 0;
    }

    public static String[] getAllSnapshotFilenames(final ServletConfig config, final TranscodeRequest request) throws ProcessorException {
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                boolean matchesPid = pathname.getName().contains(request.getPid());
                boolean matchesFormat = pathname.getName().endsWith(Util.getInitParameter(config, Constants.SNAPSHOT_FINAL_FORMAT));
                return matchesFormat && matchesPid;
            }
        };
        File[] allFiles = getOutputDir(request, config).listFiles(fileFilter);
        String[] filenames = new String[allFiles.length];
        for (int i=0; i<allFiles.length; i++) {
            filenames[i] = Util.getRelativePath(OutputFileUtil.getBaseOutputDir(request, config), allFiles[i]);
        }
        return filenames;
    }


    public static String getSnapshotBasename(ServletConfig config, TranscodeRequest request, String label, String count) {
        return request.getPid() + "." + label + "." + count;
    }


    public static File getFullPrimarySnapshotFile(ServletConfig config, TranscodeRequest request, String label, String count) {
         return new File(getAndCreateOutputDir(request, config), getSnapshotBasename(config, request, label, count) + "." + Util.getPrimarySnapshotSuffix(config));
    }

    public static File getFullFinalSnapshotFile(ServletConfig config, TranscodeRequest request, String label, String count) {
         return new File(getAndCreateOutputDir(request, config), getSnapshotBasename(config, request, label, count) + "." + Util.getInitParameter(config, Constants.SNAPSHOT_FINAL_FORMAT));
    }

}
