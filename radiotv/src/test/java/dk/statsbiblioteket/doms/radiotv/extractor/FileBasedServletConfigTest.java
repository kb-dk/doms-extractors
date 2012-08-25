package dk.statsbiblioteket.doms.radiotv.extractor;

import junit.framework.TestCase;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.StringReader;

/**
 * Created by IntelliJ IDEA.
 * User: csr
 * Date: 1/11/12
 * Time: 2:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileBasedServletConfigTest extends TestCase {

    String servletContext2 ="<Context></Context>";
    String servletContext = "<Context docBase=\"/home/larm/war/bes_CSR.war\">\n" +
            "\n" +
            "    <Parameter name=\"log4jConfigLocation\" value=\"../../../etc/log4j.CSR.xml\"\n" +
            "             override=\"false\"/>\n" +
            "    \n" +
            "<Parameter name=\"log4jExposeWebAppRoot\" value=\"false\" override=\"false\" />\n" +
            "\n" +
            "    \n" +
            "\n" +
            "         <Parameter name=\"log4jConfigLocation\" value=\"../../../etc/log4j.CSR.xml\"\n" +
            "             override=\"false\"/>\n" +
            "    <Parameter name=\"log4jExposeWebAppRoot\" value=\"false\" override=\"false\" />\n" +
            "\n" +
            "    <!--\n" +
            "    Directory where the full streamable files are to be deployed\n" +
            "    -->\n" +
            "    <Parameter name=\"dk.statsbiblioteket.doms.radiotv.extractor.finaldir\" value=\"/home/larm/csr/streamingContent\"/> </Context>";

    public void testParseContext() throws JAXBException {
        ByteArrayInputStream is = new ByteArrayInputStream(servletContext.getBytes());
        FileBasedServletConfig config = new FileBasedServletConfig(is);
        assertEquals("false", config.getInitParameter("log4jExposeWebAppRoot"));
    }

}
