package edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * @author Rhett Sutphin
 */
@Entity
@DiscriminatorValue(value = "1")
public class Scheduled extends ScheduledActivityState {
    public Scheduled() { }

    public Scheduled(String reason, Date date) {
        super(reason, date);
    }

    protected void appendPreposition(StringBuilder sb) {
        sb.append("for");
    }

    @Transient // use superclass annotation
    public ScheduledActivityMode getMode() { return ScheduledActivityMode.SCHEDULED; }


    @Transient
    public List<Class<? extends ScheduledActivityState>> getAvailableStates(boolean conditional) {
        List<Class<? extends ScheduledActivityState>> availableStates = getAvailableConditionalStates(conditional);
        availableStates.add(Occurred.class);
        availableStates.add(Scheduled.class);
        availableStates.add(Canceled.class);
        availableStates.add(Missed.class);
        return availableStates;
    }
}
