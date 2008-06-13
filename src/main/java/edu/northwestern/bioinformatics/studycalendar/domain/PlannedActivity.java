package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.*;
import org.apache.commons.lang.StringUtils;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Rhett Sutphin
 * @author Yufang Wang
 */
@Entity
@Table (name = "planned_activities")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_planned_activities_id")
    }
)
public class PlannedActivity extends PlanTreeNode<Period> implements Comparable<PlannedActivity> {
    private Activity activity;
    private Population population;
    private Integer day;
    private String details;
    private String condition;
    private List<PlannedActivityLabel> plannedActivityLabels;

    ////// LOGIC

    @Override public Class<Period> parentClass() { return Period.class; }

    public int compareTo(PlannedActivity other) {
        // by day
        int dayDiff = getDay() - other.getDay();
        if (dayDiff != 0) return dayDiff;
        // then by activity
        return getActivity().compareTo(other.getActivity());
    }

    @Transient
    public List<Integer> getDaysInStudySegment() {
        int day = getDay();
        int actualDay = calculateDay(day);
        List<Integer> days = new ArrayList<Integer>();
        while (days.size() < getPeriod().getRepetitions()) {
            days.add(actualDay);
            actualDay += getPeriod().getDuration().getDays();
        }
        return days;
    }

    public int calculateDay(int day) {
        int daysInDuration = getPeriod().getDuration().getUnit().inDays();
        int dayToReturn = 0;
        if(daysInDuration != 1 && day > 1) {
            int multiplication = day-1;
            dayToReturn = daysInDuration * multiplication + getPeriod().getStartDay();
        } else {
            //else case is for days, where everything should stay as before. no shifting is needed
            dayToReturn =  getPeriod().getStartDay() + getDay() - 1;
        }
        return dayToReturn;
    }

    @Transient
    public ScheduledActivityMode getInitialScheduledMode() {
        if (StringUtils.isBlank(getCondition())) {
            return ScheduledActivityMode.SCHEDULED;
        } else {
            return ScheduledActivityMode.CONDITIONAL;
        }
    }

    ////// BEAN PROPERTIES

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "activity_id")
    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }                                                                                            

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_id")
    public Period getPeriod() {
        return getParent();
    }

    public void setPeriod(Period period) {
        setParent(period);
    }

    @ManyToOne
    public Population getPopulation() {
        return population;
    }

    @OneToMany(mappedBy = "plannedActivity")
    @OrderBy // order by ID for testing consistency
    // TODO: why isn't this just "ALL"?
    @Cascade(value = { org.hibernate.annotations.CascadeType.DELETE, org.hibernate.annotations.CascadeType.LOCK, org.hibernate.annotations.CascadeType.MERGE,
            org.hibernate.annotations.CascadeType.PERSIST, org.hibernate.annotations.CascadeType.REFRESH, org.hibernate.annotations.CascadeType.REMOVE, org.hibernate.annotations.CascadeType.REPLICATE,
            org.hibernate.annotations.CascadeType.SAVE_UPDATE })
    public List<PlannedActivityLabel> getPlannedActivityLabels() {
        return plannedActivityLabels;
    }


    public void setPlannedActivityLabels(List<PlannedActivityLabel> plannedActivityLabels){
        this.plannedActivityLabels = plannedActivityLabels;
    }

    public void setPopulation(Population population) {
        this.population = population;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }



    @Override
    protected PlannedActivity clone() {
        PlannedActivity clone = (PlannedActivity) super.clone();
//        clone.setActivity(getActivity());
//        clone.setPopulation(getPopulation());
        return clone;
    }
}
