package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.domain.DayOfTheWeek;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Canceled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Conditional;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.NotApplicable;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Occurred;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class ScheduleServiceTest extends StudyCalendarTestCase {
    private static final String REVISION_DISPLAY_NAME = "10/01/1926 (Leopard)";

    private ScheduleService service;
    private ParticipantService participantService;

    private ScheduledArm scheduledArm;
    private Site site;
    private Amendment amendment;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Just so there's a non-null chain from event to Site
        site = createNamedInstance("The Sun", Site.class);
        Study study = createBasicTemplate();
        StudyParticipantAssignment assignment
            = createAssignment(study, site, createParticipant("Alice", "Wonder"));
        scheduledArm = new ScheduledArm();
        assignment.getScheduledCalendar().addArm(scheduledArm);

        amendment = createAmendments("Leopard");
        amendment.setDate(DateTools.createDate(1926, Calendar.OCTOBER, 1));

        service = new ScheduleService();
        // this is a real instance instead of mock because eventually
        // some or all of the methods invoked by SS on PS are going to
        // be moved into SS.
        participantService = new ParticipantService();
        service.setParticipantService(participantService);
    }

    public void testReviseDateForScheduledScheduledEvent() throws Exception {
        ScheduledActivity event = createScheduledEvent("DC", 2004, Calendar.APRIL, 1);
        scheduledArm.addEvent(event);

        service.reviseDate(event, 7, amendment);
        assertEquals(2, event.getAllStates().size());
        assertEquals("Shifted forward 7 days in revision " + REVISION_DISPLAY_NAME, event.getCurrentState().getReason());
        assertEquals(ScheduledActivityMode.SCHEDULED, event.getCurrentState().getMode());
        assertDayOfDate(2004, Calendar.APRIL, 8, event.getActualDate());
    }

    public void testReviseDateForConditionalScheduledEvent() throws Exception {
        ScheduledActivity event = createScheduledEvent("DC", 2004, Calendar.APRIL, 24,
            new Conditional("DC", DateTools.createDate(2004, Calendar.APRIL, 30)));
        scheduledArm.addEvent(event);

        service.reviseDate(event, -7, amendment);
        assertEquals(3, event.getAllStates().size());
        assertEquals("Shifted back 7 days in revision " + REVISION_DISPLAY_NAME, event.getCurrentState().getReason());
        assertEquals(ScheduledActivityMode.CONDITIONAL, event.getCurrentState().getMode());
        assertDayOfDate(2004, Calendar.APRIL, 23, event.getActualDate());
    }

    public void testReviseDateForOccurredScheduledEvent() throws Exception {
        ScheduledActivity event = createScheduledEvent("DC", 2004, Calendar.APRIL, 24,
            new Occurred("DC", DateTools.createDate(2004, Calendar.APRIL, 30)));
        scheduledArm.addEvent(event);

        service.reviseDate(event, -7, amendment);
        assertEquals(2, event.getAllStates().size());
        assertEquals(ScheduledActivityMode.OCCURRED, event.getCurrentState().getMode());
        assertDayOfDate(2004, Calendar.APRIL, 30, event.getActualDate());
    }

    public void testReviseDateForCanceledScheduledEvent() throws Exception {
        ScheduledActivity event = createScheduledEvent("DC", 2004, Calendar.APRIL, 24,
            new Canceled("DC"));
        scheduledArm.addEvent(event);

        service.reviseDate(event, -7, amendment);
        assertEquals(2, event.getAllStates().size());
        assertEquals(ScheduledActivityMode.CANCELED, event.getCurrentState().getMode());
        assertDayOfDate(2004, Calendar.APRIL, 24, event.getActualDate());
    }

    public void testReviseDateForNotApplicableScheduledEvent() throws Exception {
        ScheduledActivity event = createScheduledEvent("DC", 2004, Calendar.APRIL, 24,
            new NotApplicable("DC"));
        scheduledArm.addEvent(event);

        service.reviseDate(event, -7, amendment);
        assertEquals(2, event.getAllStates().size());
        assertEquals(ScheduledActivityMode.NOT_APPLICABLE, event.getCurrentState().getMode());
        assertDayOfDate(2004, Calendar.APRIL, 24, event.getActualDate());
    }

    public void testReviseDateForScheduledScheduledEventAvoidsBlackouts() throws Exception {
        DayOfTheWeek noThursdays = new DayOfTheWeek();
        noThursdays.setDayOfTheWeek("Thursday");
        site.getHolidaysAndWeekends().add(noThursdays);

        ScheduledActivity event = createScheduledEvent("DC", 2007, Calendar.OCTOBER, 2);
        scheduledArm.addEvent(event);

        service.reviseDate(event, 2, amendment);

        assertEquals(ScheduledActivityMode.SCHEDULED, event.getCurrentState().getMode());
        assertDayOfDate(2007, Calendar.OCTOBER, 5, event.getActualDate());
        assertEquals(3, event.getAllStates().size());
    }
}
