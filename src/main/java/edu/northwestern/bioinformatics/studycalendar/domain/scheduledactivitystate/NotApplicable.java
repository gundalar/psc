package edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.apache.commons.lang.StringUtils;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Transient;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;

import java.util.List;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
@Entity
@DiscriminatorValue(value = "5")
public class NotApplicable extends ScheduledActivityState {

    public NotApplicable() { }

    public NotApplicable(String reason,Date date) {
        super(reason,date);
    }

    ////// LOGIC

    protected void appendPreposition(StringBuilder sb) {
        sb.append("on");
    }

    @Override
    @Transient
    public List<Class<? extends ScheduledActivityState>> getAvailableStates(boolean conditional) {
        List<Class<? extends ScheduledActivityState>> availableStates = getAvailableConditionalStates(conditional);
        availableStates.add(Scheduled.class);
        return availableStates;
    }

    ////// BEAN PROPERTIES

    @Override
    @Transient // use superclass annotation
    public ScheduledActivityMode getMode() { return ScheduledActivityMode.NOT_APPLICABLE; }
}