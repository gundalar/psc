package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.PscSimpleFormController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 *
 * @author Nataliya Shurupova
 */
@AccessControl(roles = Role.STUDY_COORDINATOR)
public class ExternalStudyController extends PscSimpleFormController {
    private StudyDao studyDao;

    public ExternalStudyController() {
        setFormView("template/externalStudy");
        setSuccessView("redirectToStudyList");
        setBindOnNewForm(true);
        setCrumb(new DefaultCrumb("External Study"));
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        int id = ServletRequestUtils.getRequiredIntParameter(request, "study");
        Study study = studyDao.getById(id);
        return new ModelAndView("template/externalStudy", model);
    }

    @Override
    protected ModelAndView onSubmit(Object oCommand, BindException errors) throws Exception {
        return new ModelAndView(getSuccessView());
    }
     @Override
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception {
        getControllerTools().registerDomainObjectEditor(binder, "study", studyDao);
    }

    ////// CONFIGURATION
    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

//    private static class Crumb extends DefaultCrumb {
//        public Crumb() {
//            super("External Study");
//        }
//
//        @Override
//        public Map<String, String> getParameters(BreadcrumbContext context) {
//            return Collections.singletonMap("study", context.getStudy().getId().toString());
//        }
//    }
}
