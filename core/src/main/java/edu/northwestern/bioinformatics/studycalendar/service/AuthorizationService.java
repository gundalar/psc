package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import static edu.northwestern.bioinformatics.studycalendar.domain.Role.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.StudyConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
public class AuthorizationService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private StudyConsumer studyConsumer;

    public List<StudySubjectAssignment> filterAssignmentsForVisibility(List<StudySubjectAssignment> source, User visibleTo) {
        log.debug("Filtering {} assignments for visibility to {}", source.size(), visibleTo);
        List<StudySubjectAssignment> visible = new LinkedList<StudySubjectAssignment>();

        UserRole subjCoord = visibleTo.getUserRole(Role.SUBJECT_COORDINATOR);
        if (subjCoord != null) {
            log.debug(" - is a subject coordinator for {}", subjCoord.getStudySites());
            for (StudySubjectAssignment assignment : source) {
                if (subjCoord.getStudySites().contains(assignment.getStudySite())) {
                    log.debug(" - {} is visible", assignment);
                    visible.add(assignment);
                } else {
                    log.debug(" - {} is not visible", assignment);
                }
            }
        }

        return visible;
    }

    public List<Study> filterStudiesForVisibility(List<Study> studies, User visibleTo) {
        studyConsumer.refresh(studies);

        Set<Study> all = new LinkedHashSet<Study>();
        for (Study study : studies) {
            for (UserRole role : visibleTo.getUserRoles()) {
                if (isTemplateVisible(role, study)) all.add(study);
            }
        }
        return new ArrayList<Study>(all);
    }

    /**
     * Returns a copy of the given list of studies containing only those which should be
     * visible to the given user role.
     *
     * @param studies
     * @return
     * @throws Exception
     */
    public List<Study> filterStudiesForVisibility(List<Study> studies, UserRole visibleTo) {
        if (visibleTo == null) {
            return Collections.emptyList();
        }
        studyConsumer.refresh(studies);

        List<Study> filtered = new ArrayList<Study>(studies);
        for (Iterator<Study> it = filtered.iterator(); it.hasNext();) {
            Study study = it.next();
            if (!isTemplateVisible(visibleTo, study)) it.remove();
        }
        return filtered;
    }

    public boolean isTemplateVisible(UserRole userRole, Study study) {
        if (userRole.getRole() == SYSTEM_ADMINISTRATOR) {
            return false;
        } else if (!userRole.getRole().isSiteSpecific()) {
            return true;
        } else if (userRole.getRole() == SITE_COORDINATOR) {
            return isTemplateVisibleToSiteCoordinator(userRole, study);
        } else if (userRole.getRole() == SUBJECT_COORDINATOR ) {
            return isTemplateVisibleToStudySpecificRole(userRole, study);
        } else {
            throw new UnsupportedOperationException("Unexpected role in userRole: " + userRole.getRole());
        }
    }

    private boolean isTemplateVisibleToStudySpecificRole(UserRole studySpecificRole, Study study) {
        for (StudySite studySite : study.getStudySites()) {
            if (studySpecificRole.getStudySites().contains(studySite)) return true;
        }
        return false;
    }

    private boolean isTemplateVisibleToSiteCoordinator(UserRole siteCoordinator, Study study) {
        for (Site siteOnStudy : study.getSites()) {
            if (siteCoordinator.getSites().contains(siteOnStudy)) return true;
        }
        return false;
    }

    ////// CONFIGURATION

    @Required
    public void setStudyConsumer(StudyConsumer studyConsumer) {
        this.studyConsumer = studyConsumer;
    }
}
