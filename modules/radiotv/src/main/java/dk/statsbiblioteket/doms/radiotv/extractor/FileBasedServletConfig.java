package dk.statsbiblioteket.doms.radiotv.extractor;

import dk.statsbiblioteket.doms.radiotv.extractor.servlet.ContextType;
import dk.statsbiblioteket.doms.radiotv.extractor.servlet.ParameterType;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;

/**
 * Created by IntelliJ IDEA.
 * User: csr
 * Date: 1/11/12
 * Time: 1:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileBasedServletConfig implements ServletConfig {

    ContextType servletContext;

    public FileBasedServletConfig(InputStream is) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(ContextType.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        servletContext = (ContextType) unmarshaller.unmarshal(new InputStreamReader(is));

    }

    @Override
    public String getServletName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ServletContext getServletContext() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getInitParameter(String name) {
        for (ParameterType param : servletContext.getParameter()) {
            if (param.getName().equals(name)) {
                return param.getValue();
            }
        }
        return null;
    }

    @Override
    public Enumeration getInitParameterNames() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
