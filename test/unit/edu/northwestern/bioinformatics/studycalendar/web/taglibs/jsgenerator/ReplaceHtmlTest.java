package edu.northwestern.bioinformatics.studycalendar.web.taglibs.jsgenerator;

import edu.northwestern.bioinformatics.studycalendar.testing.MockPageContext;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.classextension.EasyMock.expect;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.Tag;


/**
 * @author Rhett Sutphin
 */
public class ReplaceHtmlTest extends StudyCalendarTestCase {
    private static final String TARGET_ELEMENT = "target";

    private ReplaceHtml tag;
    private JspWriter writer;
    private MockPageContext pageContext;
    private BodyContent bodyContent;

    protected void setUp() throws Exception {
        super.setUp();
        tag = new ReplaceHtml();
        writer = registerMockFor(JspWriter.class);
        pageContext = new MockPageContext(writer);
        bodyContent = registerMockFor(BodyContent.class);

        tag.setPageContext(pageContext);
        tag.setTargetElement(TARGET_ELEMENT);
        tag.setBodyContent(bodyContent);
    }

    public void testStart() throws Exception {
        replayMocks();
        assertEquals(BodyTag.EVAL_BODY_BUFFERED, tag.doStartTag());
        verifyMocks();
    }

    public void testEnd() throws Exception {
        expect(bodyContent.getString()).andReturn("content");
        writer.write("Element.update(\"" + TARGET_ELEMENT + "\", \"content\")");

        replayMocks();
        assertEquals(Tag.EVAL_PAGE, tag.doEndTag());
        verifyMocks();
    }

    public void testEndWithComplexContent() throws Exception {
        expect(bodyContent.getString()).andReturn("<div id=\"foo\">\n  <strong>Bad</strong>\n</div>");
        writer.write("Element.update(\"" + TARGET_ELEMENT + "\", \"<div id=\\\"foo\\\">\\n  <strong>Bad</strong>\\n</div>\")");

        replayMocks();
        assertEquals(Tag.EVAL_PAGE, tag.doEndTag());
        verifyMocks();
    }
}
