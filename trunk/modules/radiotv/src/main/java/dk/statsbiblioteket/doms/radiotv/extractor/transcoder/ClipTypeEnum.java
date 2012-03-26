package dk.statsbiblioteket.doms.radiotv.extractor.transcoder;/* File:        $Id$
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

public enum ClipTypeEnum {
        MUX{
        @Override
        public long getBitrate() {
           return 2488237L;
         }}, MPEG1{
        @Override
        public long getBitrate() {
            return 169242L;
        }}, MPEG2{
        @Override
        public long getBitrate() {
            return 872254L;
        }}, WAV{
        @Override
        public long getBitrate() {
            return 88200L;
        }} ;

    public static ClipTypeEnum getType(TranscodeRequest request) throws ProcessorException {
        TranscodeRequest.FileClip clip = request.getClips().get(0);
        String filename = clip.getFilepath();
        if (filename.contains("mux")) {
            return MUX;
        } else if (filename.contains("_mpeg1_")) {
            return MPEG1;
        } else if (filename.contains("_mpeg2_")) {
            return MPEG2;
        } else if (filename.endsWith("wav")) {
            return WAV;
        } else {
            throw new ProcessorException("Cannot recognise type of file '" + filename + "'");
        }
    }

    /**
     * Get the bitrate for this type of file in bytes/second.
     * @return
     */
    public abstract long getBitrate();
}
