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
import org.junit.Ignore;
import org.junit.Test;

import javax.xml.bind.*;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.nio.channels.OverlappingFileLockException;
import java.security.Permission;
import java.util.ArrayList;

public class ShardMetadataTest extends TestCase {

    public void testMarshallWithoutStructure() throws JAXBException {
        ShardMetadata sm = new ShardMetadata();
        ShardMetadata.ShardFile sf1 = new ShardMetadata.ShardFile();
        sf1.setChannel_id(102);
        sf1.setFile_url("foobar");
        ArrayList<ShardMetadata.ShardFile> sfs = new ArrayList<ShardMetadata.ShardFile>();
        sfs.add(sf1);
        sm.setShardFiles(sfs);
         JAXBContext context = JAXBContext.newInstance(ShardMetadata.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(sm, System.out);
    }


private static class ExitTrappedException extends SecurityException { }

  private static void forbidSystemExitCall() {
    final SecurityManager securityManager = new SecurityManager() {
      public void checkPermission( Permission permission ) {
        if( "exitVM".equals( permission.getName() ) ) {
          throw new ExitTrappedException() ;
        } else {
            System.out.println(permission.getName());
        }
      }
        /** Deny permission to exit the VM. */
          public void checkExit(int status) {
            throw( new SecurityException() );
         }

    } ;
    System.setSecurityManager( securityManager ) ;
  }


    public void testMarshallWithStructure() throws JAXBException {
        ShardMetadata sm = new ShardMetadata();
        ShardMetadata.ShardFile sf1 = new ShardMetadata.ShardFile();
        sf1.setChannel_id(102);
        sf1.setFile_url("foobar");
        ArrayList<ShardMetadata.ShardFile> sfs = new ArrayList<ShardMetadata.ShardFile>();
        sfs.add(sf1);
        sm.setShardFiles(sfs);
        ShardStructure ss = new ShardStructure();
        ShardStructure.Overlap ov = new ShardStructure.Overlap();
        ov.setFilePath1("file1");
        ov.setFilePath2("file2");
        ov.setOverlapLength(107L);
        ov.setOverlapType(0);
        ss.addOverlap(ov);
        sm.setShardStructure(ss);
        JAXBContext context = JAXBContext.newInstance(ShardMetadata.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(sm, System.out);
    }

    public void testUnmarshall() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(ShardMetadata.class);
        Unmarshaller um = context.createUnmarshaller();
        StringReader reader = new StringReader(ShardParserProcessorTest.xml1);
        Object o = um.unmarshal(reader);
        System.out.println(o.getClass());
        System.out.println(o);
        assertNotNull(o);
        assertTrue(o instanceof ShardMetadata);
        ShardMetadata sm = (ShardMetadata) o;
        assertEquals(2, sm.getShardFiles().size());
    }



    @Test
    public void testRemarshall() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(ShardMetadata.class);
        Unmarshaller um = context.createUnmarshaller();
        StringReader reader = new StringReader(ShardParserProcessorTest.xml1);
        Object o = um.unmarshal(reader);

        ShardMetadata sm = (ShardMetadata) o;
        ShardStructure ss = new ShardStructure();
        ShardStructure.MissingEnd me = new ShardStructure.MissingEnd();
        me.setMissingSeconds(3200);
        ss.setMissingEnd(me);
        sm.setShardStructure(ss);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(sm, System.out);
    }

    //@Ignore
    public void testGetSchema() throws JAXBException, IOException {
           //forbidSystemExitCall();
           JAXBContext context = JAXBContext.newInstance(ShardMetadata.class);
           context.generateSchema(new SchemaOutputResolver() {
               @Override
               public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
                   Result result = new StreamResult(System.out);
                   result.setSystemId("");
                   return result;
               }
           });
       }

}
