package edu.northwestern.bioinformatics.studycalendar.xml;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.BeansException;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 * @author John Dzak
 */
public abstract class AbstractStudyCalendarXmlSerializer<R> implements StudyCalendarXmlSerializer<R>, BeanFactoryAware {
    public static final String XML_NS = "http://www.w3.org/2000/xmlns/";
    public static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String PSC_NS = "http://bioinformatics.northwestern.edu/ns/psc";
    public static final String SCHEMA_LOCATION  = "http://bioinformatics.northwestern.edu/ns/psc/psc.xsd";

    public static final Namespace DEFAULT_NAMESPACE = DocumentHelper.createNamespace("", PSC_NS);
    protected static final OutputFormat OUTPUT_FORMAT = new OutputFormat("  ", true);

    public static final String SCHEMA_NAMESPACE_ATTRIBUTE = "xmlns";
    public static final String SCHEMA_LOCATION_ATTRIBUTE  = "schemaLocation";
    public static final String XML_SCHEMA_ATTRIBUTE       = "xsi";

    // Attributes
    public static final String ID = "id";
    public static final String NAME = "name";
    private BeanFactory beanFactory;

    public Document createDocument(R root) {
        Document document = DocumentHelper.createDocument();
        Element element = createElement(root);

        configureRootElement(element);

        document.add(element);
        
        return document;
    }

    protected void configureRootElement(Element element) {
        element.add(DEFAULT_NAMESPACE);
        element.addNamespace(XML_SCHEMA_ATTRIBUTE, XSI_NS)
                .addAttribute("xsi:"+ SCHEMA_LOCATION_ATTRIBUTE, PSC_NS + ' ' + SCHEMA_LOCATION);
    }

    public String createDocumentString(R root) {
        return createDocumentString(createDocument(root));
    }

    protected String createDocumentString(Document doc) {
        StringWriter capture = new StringWriter();
        try {
            new XMLWriter(capture, OUTPUT_FORMAT).write(doc);
        } catch (IOException e) {
            throw new StudyCalendarSystemException("Unexpected error when serializing XML", e);
        }
        return capture.toString();
    }

    public R readDocument(Document document) {
        return readElement(document.getRootElement());
    }

    public R readDocument(InputStream in) {
        Document document = deserializeDocument(in);
        return readElement(document.getRootElement());
    }

    protected Document deserializeDocument(InputStream in) {
        Document document;
        try {
            SAXReader saxReader = new SAXReader();
            document = saxReader.read(in);
        } catch(DocumentException de) {
            throw new StudyCalendarSystemException("Could not read the XML for deserialization", de);
        }
        return document;
    }

    public abstract Element createElement(R object);

    public abstract R readElement(Element element);

    //// Helper Methods
    protected Element element(String elementName) {
        // Using QName is the only way to attach the namespace to the element
        QName qNode = DocumentHelper.createQName(elementName, DEFAULT_NAMESPACE);
        return DocumentHelper.createElement(qNode);
    }

    // Bean Getter and Setter methods
    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
