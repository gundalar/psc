package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Saurabh Agrawal
 */
public class SiteResource extends AbstractRemovableStorableDomainObjectResource<Site> {

    private SiteService siteService;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
        setAuthorizedFor(Method.PUT, Role.SITE_COORDINATOR);
    }


    @Override
    protected Site loadRequestedObject(Request request) {
        String assignedIdentifier = UriTemplateParameters.SITE_IDENTIFIER.extractFrom(request);
        return siteService.getByAssignedIdentifier(assignedIdentifier);
    }


    @Override
    public void remove(final Site site) {
        try {
            siteService.removeSite(site);
        } catch (Exception e) {
            String message = "Can not delete the site" + UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest());
            log.error(message, e);
        }
    }

    @Override
    public void store(final Site site) {
        try {

            Site existingSite = getRequestedObject();
            siteService.createOrMergeSites(existingSite, site);

        } catch (Exception e) {
            String message = "Can not update the site" + UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest());
            log.error(message, e);

        }

    }

    @Override
    public void verifyRemovable(final Site site) throws ResourceException {
        super.verifyRemovable(site);
        boolean siteCanBeDeleted = siteService.checkIfSiteCanBeDeleted(site);
        if (!siteCanBeDeleted) {
            String message = "Can not delete the site" + UriTemplateParameters.SITE_IDENTIFIER.extractFrom(getRequest()) +
                    " because site has some assignments";
            log.error(message);

            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    message);

        }
    }

    @Required
    public void setSiteService(final SiteService siteService) {
        this.siteService = siteService;
    }


}