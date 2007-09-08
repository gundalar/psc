package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.BatchSize;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.OneToMany;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;

/**
 * @author Rhett Sutphin
 */
@Entity
@Table (name = "studies")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_studies_id")
    }
)
public class Study extends AbstractMutableDomainObject implements Named, TransientCloneable<Study>, Cloneable {
    private String name;
    private String protocolAuthorityId;
    private PlannedCalendar plannedCalendar;
    private Amendment amendment; // the current effective/approved amendment
    private List<StudySite> studySites = new ArrayList<StudySite>();

    // TODO: this needs to be handled more robustly
    private Boolean amended = false;
    private boolean memoryOnly = false;

    public Study() {
        amendment = new Amendment("[Original]");
    }

    ////// LOGIC

    public void addStudySite(StudySite studySite){
        getStudySites().add(studySite);
        studySite.setStudy(this);
    }

    public void addSite(Site site) {
        if (!getSites().contains(site)) {
            StudySite newSS = new StudySite();
            newSS.setSite(site);
            addStudySite(newSS);
        }
    }

    @Transient
    public List<Site> getSites() {
        List<Site> sites = new ArrayList<Site>(getStudySites().size());
        for (StudySite studySite : studySites) {
            sites.add(studySite.getSite());
        }
        return Collections.unmodifiableList(sites);
    }

    @Transient
    public boolean isMemoryOnly() {
        return memoryOnly;
    }

    public void setMemoryOnly(boolean memoryOnly) {
        this.memoryOnly = memoryOnly;
        getPlannedCalendar().setMemoryOnly(true);
    }

    public Study transientClone() {
        Study clone = clone();
        clone.setMemoryOnly(true);
        return clone;
    }

    ////// BEAN PROPERTIES

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProtocolAuthorityId() {
        return protocolAuthorityId;
    }

    public void setProtocolAuthorityId(String protocolAuthorityId) {
        this.protocolAuthorityId = protocolAuthorityId;
    }

    @OneToOne (mappedBy = "study")
    @Cascade (value = { CascadeType.ALL })
    public PlannedCalendar getPlannedCalendar() {
        return plannedCalendar;
    }

    public void setPlannedCalendar(PlannedCalendar plannedCalendar) {
        this.plannedCalendar = plannedCalendar;
        if (plannedCalendar != null && plannedCalendar.getStudy() != this) {
            plannedCalendar.setStudy(this);
        }
    }

    public void setStudySites(List<StudySite> studySites) {
        this.studySites = studySites;
    }

    @OneToMany (mappedBy = "study",fetch = FetchType.EAGER)
    @Cascade (value = { CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public List<StudySite> getStudySites() {
        return studySites;
    }

    public Boolean getAmended() {
        return amended;
    }

    public void setAmended(Boolean amended) {
        this.amended = amended;
    }

    @Transient // TODO: persistence
    public Amendment getAmendment() {
        return amendment;
    }

    public void setAmendment(Amendment amendment) {
        this.amendment = amendment;
    }

    ////// OBJECT METHODS

    @Override
    protected Study clone() {
        try {
            // deep-clone the template portions only, for the moment
            Study clone = (Study) super.clone();
            if (getPlannedCalendar() != null) {
                clone.setPlannedCalendar((PlannedCalendar) getPlannedCalendar().clone());
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new StudyCalendarError("Clone is supported");
        }
    }
}