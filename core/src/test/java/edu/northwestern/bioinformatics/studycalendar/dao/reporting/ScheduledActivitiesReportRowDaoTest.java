package edu.northwestern.bioinformatics.studycalendar.dao.reporting;

import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;
import edu.northwestern.bioinformatics.studycalendar.tools.MutableRange;
import edu.nwu.bioinformatics.commons.DateUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance;

/**
 * @author John Dzak
 */
public class ScheduledActivitiesReportRowDaoTest extends
    ReportDaoTestCase<ScheduledActivitiesReportFilters, ScheduledActivitiesReportRow, ScheduledActivitiesReportRowDao>
{
    private static final long NEG_16 = -16;
    private static final long NEG_17 = -17;
    private static final long NEG_18 = -18;
    private static final long NEG_19 = -19;

    @Override
    protected ScheduledActivitiesReportFilters createFilters() {
        return new ScheduledActivitiesReportFilters();
    }

    public void testSearchWithStudyFilter_Pos() {
        filters.setStudyAssignedIdentifier("Foo");
        ScheduledActivitiesReportRow row = assertSearchWithResults(NEG_19, NEG_18, NEG_17, NEG_16).get(2);

        assertNotNull("ID should not be null", row.getId());

        ScheduledActivity schd = row.getScheduledActivity();
        assertNotNull(schd.getDetails());
        assertNotNull("Scheduled Activity id should not be null", schd.getId());
        assertNotNull("Scheduled Activity actual date should not be null", schd.getCurrentState());
        assertNotNull("Scheduled Activity ideal date should not be null", schd.getIdealDate());

        Activity acivity = schd.getActivity();
        assertNotNull("Activity id should not be null", acivity.getId());
        assertNotNull("Activity name should not be null", acivity.getName());
        assertNotNull("Actiivty type should not be null", acivity.getType());

        assertNotNull("Subject should not be null", row.getSubject());

        assertNotNull("Study should not be null", row.getStudy());

        assertNotNull("Site should not be null", row.getSite());
        // TODO: #1111
//        assertNotNull("Site coordinator name should not be null", row.getSubjectCoordinatorName());
    }

    public void testSearchWithStudyFilter_Neg() {
        filters.setStudyAssignedIdentifier("Fla");
        assertSearchWithResults();
    }

    public void testSearchWithScheduledActivityMode_Scheduled() {
        filters.setCurrentStateMode(ScheduledActivityMode.SCHEDULED);
        assertSearchWithResults(NEG_16);
    }

    public void testSearchWithScheduledActivityMode_Canceled() {
        filters.setCurrentStateMode(ScheduledActivityMode.CANCELED);
        assertSearchWithResults(NEG_17);
    }

    public void testSearchWithScheduledActivityMode_Occurred() {
        filters.setCurrentStateMode(ScheduledActivityMode.OCCURRED);
        assertSearchWithResults();
    }

    public void testSearchWithSiteFilter_Pos() {
        filters.setSiteName("DC");
        assertSearchWithResults(NEG_19, NEG_18, NEG_17, NEG_16);
    }

    public void testSearchWithLabelFilter() {
        filters.setLabel("LABELB");
        assertSearchWithResults(NEG_17, NEG_16);
    }

    public void testSearchWithSiteFilter_Neg() {
        filters.setSiteName("Bedrock");
        assertSearchWithResults();
    }

    public void testSearchWithStartDateFilter_Pos() {
        MutableRange<Date> range = new MutableRange<Date>();
        range.setStart(DateUtils.createDate(2006, Calendar.NOVEMBER, 5));
        filters.setActualActivityDate(range);
        assertSearchWithResults(NEG_19, NEG_18, NEG_17);
    }

    public void testSearchWithStartDateFilter_Neg() {
        MutableRange<Date> range = new MutableRange<Date>();
        range.setStart(DateUtils.createDate(2006, Calendar.NOVEMBER, 15));
        filters.setActualActivityDate(range);
        assertSearchWithResults(NEG_19, NEG_18);
    }

    public void testSearchWithStopDateFilter_Pos() {
        MutableRange<Date> range = new MutableRange<Date>();
        range.setStop(DateUtils.createDate(2006, Calendar.NOVEMBER, 5));
        filters.setActualActivityDate(range);
        assertSearchWithResults(NEG_16);
    }

    public void testSearchWithStopDateFilter_Neg() {
        MutableRange<Date> range = new MutableRange<Date>();
        range.setStop(DateUtils.createDate(2006, Calendar.OCTOBER, 25));
        filters.setActualActivityDate(range);
        assertSearchWithResults();
    }

    public void testSearchWithActivityTypeFilter_Pos() {
        ActivityType activityType = createNamedInstance("INTERVENTION", ActivityType.class);
        activityType.setId(2);
        filters.setActivityType(activityType);
        assertSearchWithResults(NEG_19, NEG_18, NEG_17, NEG_16);
    }

    public void testSearchWithActivityTypeFilter_Neg() {
        ActivityType activityType = createNamedInstance("DISEASE_MEASURE", ActivityType.class);
        activityType.setId(3);
        filters.setActivityType(activityType);
        assertSearchWithResults();
    }

    // TODO: issue #1111
    // Also: actually test filtering -- some positive, some negative
    /*
    public void testSearchWithSubjectCoordinatorFilter_Pos() {
        filters.setSubjectCoordinator(edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setId(-200, new User()));
        assertSearchWithResults(NEG_19, NEG_18, NEG_17, NEG_16);
    }

    public void testSearchWithSubjectCoordinatorFilter_Neg() {
        filters.setSubjectCoordinator(setId(-100, new User()));
        assertSearchWithResults();
    }
    */

    public void testSearchWithNoFiltersIsEmpty() {
        assertSearchWithResults();
    }

    public void testRowWithPersonIdFilter() {
        filters.setPersonId("UNIVERSAL");
        assertSearchWithResults(NEG_18, NEG_17, NEG_16);
    }

    public void testRowWithPersonIdAndStartAndEndDate() {
        filters.setPersonId("UNIVERSAL");
        MutableRange<Date> range = new MutableRange<Date>();
        Date startDate = null;
        Date endDate = null;
        try {
            startDate = API_DATE_FORMAT.get().parse("2007-09-29");
            endDate = API_DATE_FORMAT.get().parse("2007-10-29");
        } catch (ParseException pe) {
            pe.getMessage(); 
        }
        range.setStart(startDate);
        range.setStop(endDate);
        filters.setActualActivityDate(range);
        assertSearchWithResults(NEG_18);
    }

    public void testRowWithLabelFilter() {
        filters.setLabel("LABELB");
        List<ScheduledActivitiesReportRow> rows = doSearch();
        for (ScheduledActivitiesReportRow row : rows ) {
            assertTrue("labelB is not in the row", row.getScheduledActivity().getLabels().contains("labelB"));
        }
    }

    public void testAuthorizedStudySiteIdsFilter() throws Exception {
        filters.setAuthorizedStudySiteIds(Arrays.asList(-2));
        assertSearchWithResults(NEG_18);
    }

    private static final ThreadLocal<DateFormat> API_DATE_FORMAT = new ThreadLocal<DateFormat>() {
        @Override protected DateFormat initialValue() { return new SimpleDateFormat("yyyy-MM-dd"); }
    };
}
