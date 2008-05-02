package edu.northwestern.bioinformatics.studycalendar.xml.writers;

import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarXmlTestCase;
import org.dom4j.Element;
import static org.easymock.EasyMock.expect;

import static java.util.Collections.emptyList;

public class EpochXmlSerializerTest extends StudyCalendarXmlTestCase {
    private EpochXmlSerializer serializer;
    private EpochDao epochDao;
    private Element element;
    private Epoch epoch;
    private StudySegmentXmlSerializer studySegmentSerializer;

    protected void setUp() throws Exception {
        super.setUp();

        element = registerMockFor(Element.class);
        epochDao = registerDaoMockFor(EpochDao.class);
        studySegmentSerializer = registerMockFor(StudySegmentXmlSerializer.class);

        Study study = createNamedInstance("Study A", Study.class);

        serializer = new EpochXmlSerializer() {

            protected AbstractPlanTreeNodeXmlSerializer getChildSerializer() {
                return studySegmentSerializer;
            }
        };
        serializer.setEpochDao(epochDao);
        serializer.setStudy(study);

        epoch = setGridId("grid0", createNamedInstance("Epoch A", Epoch.class));
    }

    public void testCreateElementEpoch() {
        Element actual = serializer.createElement(epoch);

        assertEquals("Wrong attribute size", 2, actual.attributeCount());
        assertEquals("Wrong grid id", "grid0", actual.attribute("id").getValue());
        assertEquals("Wrong epoch name", "Epoch A", actual.attribute("name").getValue());
    }

    public void testReadElementEpoch() {
        expect(element.getName()).andReturn("epoch");
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(epochDao.getByGridId("grid0")).andReturn(null);
        expect(element.attributeValue("name")).andReturn("Epoch A").anyTimes();
        expect(element.elements()).andReturn(emptyList());
        replayMocks();

        Epoch actual = (Epoch) serializer.readElement(element);
        verifyMocks();

        assertEquals("Wrong grid id", "grid0", actual.getGridId());
        assertEquals("Wrong epoch name", "Epoch A", actual.getName());
    }

    public void testReadElementExistsEpoch() {
        expect(element.getName()).andReturn("epoch");
        expect(element.attributeValue("id")).andReturn("grid0");
        expect(epochDao.getByGridId("grid0")).andReturn(epoch);
        replayMocks();

        Epoch actual = (Epoch) serializer.readElement(element);
        verifyMocks();

        assertSame("Wrong Epoch", epoch, actual);
    }
}
