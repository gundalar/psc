package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;

/**
 * @author Rhett Sutphin
 */
public class StudyDaoTest extends DaoTestCase {
    private StudyDao dao = (StudyDao) getApplicationContext().getBean("studyDao");

    public void testGetById() throws Exception {
        Study study = dao.getById(1);
        assertNotNull("Study 1 not found", study);
        assertEquals("Wrong name", "First Study", study.getName());
    }

    public void testLoadingArms() throws Exception {
        Study study = dao.getById(1);
        assertNotNull("Study 1 not found", study);

        assertEquals("Wrong number of arms", 2, study.getArms().size());
        assertArm("Wrong arm 0", 3, 1, "Sinister", study.getArms().get(0));
        assertArm("Wrong arm 0", 2, 2, "Dexter", study.getArms().get(1));

        assertSame("Arm <=> Study relationship not bidirectional on load", study, study.getArms().get(0).getStudy());
    }

    private static void assertArm(
        String message, Integer expectedId, Integer expectedNumber, String expectedName, Arm actualArm
    ) {
        assertEquals(message + ": wrong id", expectedId, actualArm.getId());
        assertEquals(message + ": wrong number", expectedNumber, actualArm.getNumber());
        assertEquals(message + ": wrong name", expectedName, actualArm.getName());
    }
}
