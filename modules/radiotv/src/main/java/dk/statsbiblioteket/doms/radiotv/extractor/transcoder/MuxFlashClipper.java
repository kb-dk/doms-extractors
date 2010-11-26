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

import javax.servlet.ServletConfig;
import java.io.File;

public class MuxFlashClipper extends ProcessorChainElement {

    private static final String algorithm = "vlc";

    @Override
    protected void processThis(TranscodeRequest request, ServletConfig config) throws ProcessorException {
             this.seamlessClip(request, config);
     }

    private void seamlessClip(TranscodeRequest request, ServletConfig config) throws ProcessorException {
        Long offsetBytes = 0L;
        Long totalLengthBytes = 0L;
        String fileList = "";
        final int clipSize = request.getClips().size();
        int programNumber = 0;
        for (int iclip = 0; iclip < clipSize; iclip++ ) {
            TranscodeRequest.FileClip clip = request.getClips().get(iclip);
            final long fileLength = new File(clip.getFilepath()).length();  //This line has the side-effect of automounting the media file
            Long clipLength = clip.getClipLength();
            fileList += " " + clip.getFilepath() + " ";
            if (iclip == 0) {
                programNumber = clip.getProgramId();
                offsetBytes = clip.getStartOffsetBytes();
                if (offsetBytes == null) offsetBytes = 0L;
                if (clipLength != null && clipSize == 1) {
                    totalLengthBytes = clipLength;   //Program contained within file
                } else {         //Otherwise always go to end of file
                    totalLengthBytes = fileLength - offsetBytes;
                }
            } else if (iclip == clipSize - 1 && clipSize != 1) {   //last clip in multiclip program
                if (clipLength != null) {
                    totalLengthBytes += clip.getClipLength();
                } else {
                    totalLengthBytes += fileLength;
                }
            } else {   //A file in the middle of a program so take the whole file
                totalLengthBytes += fileLength;
            }
        }
        Long blocksize = 1880L;
        String clipperCommand;
        if (algorithm.equals("full_vlc")) {
            clipperCommand = "cat " + fileList + " | dd bs=" + blocksize
                    + " skip=" + offsetBytes/blocksize + " count=" + totalLengthBytes/blocksize
                    + " | vlc - --program=" + programNumber + " --demux=ts --intf dummy --play-and-exit --noaudio --novideo "
                    + "--sout-all --sout '#duplicate{dst=\"transcode{senc=dvbsub}"
                    + ":transcode{acodec=mp3,vcodec=h264,vb=" + config.getInitParameter(Constants.VIDEO_BITRATE) + ",venc=x264{profile=baseline,preset=superfast},soverlay,deinterlace,audio-sync,"
                    + "ab=" + config.getInitParameter(Constants.AUDIO_BITRATE)
                    + ",width=" + FlashTranscoderProcessor.getWidth(request, config)
                    + ",height=" + FlashTranscoderProcessor.getHeight(request, config) +",samplerate=44100,threads=0}"
                    + ":std{access=file,mux=ffmpeg{mux=flv}"
                    + ",dst='" + Util.getFlashFile(request, config).getAbsolutePath() +"'}\""
                    + ",select=\"program=" + programNumber + "\"' ";
        } else {
           /* clipperCommand = "cat " + fileList + " | dd bs=" + blocksize
                    + " skip=" + offsetBytes/blocksize + " count=" + totalLengthBytes/blocksize
                    + " | vlc - --program=" + programNumber + " --demux=ts --intf dummy --play-and-exit --noaudio --novideo "
                    + "--sout '#std{access=file, mux=ts, dst=-}' | "
                    + FlashTranscoderProcessor.getFfmpegCommandLine(request, config);*/
           clipperCommand = "cat " + fileList + " | dd bs=" + blocksize
                    + " skip=" + offsetBytes/blocksize + " count=" + totalLengthBytes/blocksize
                    + " | vlc - --program=" + programNumber + " --demux=ts --intf dummy --play-and-exit --noaudio --novideo "
                    + " --sout '#transcode{acodec=mp3,vcodec=h264,vb=" + config.getInitParameter(Constants.VIDEO_BITRATE)
                    + ",venc=x264{profile=baseline,preset=superfast},deinterlace,audio-sync,"
                    + "ab=" + config.getInitParameter(Constants.AUDIO_BITRATE)
                    + ",width=" + FlashTranscoderProcessor.getWidth(request, config)
                    + ",height=" + FlashTranscoderProcessor.getHeight(request, config) +",samplerate=44100,threads=0}"
                    + ":std{access=file,mux=ts,dst=-}'|ffmpeg -i - -async 2 -acodec copy -vcodec copy -f flv "
                   + Util.getFlashFile(request, config).getAbsolutePath();
        }
        FlashTranscoderProcessor.runClipperCommand(clipperCommand);
    }
}
