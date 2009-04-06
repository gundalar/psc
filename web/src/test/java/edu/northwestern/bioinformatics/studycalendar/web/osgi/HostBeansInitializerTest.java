package edu.northwestern.bioinformatics.studycalendar.web.osgi;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.HostBeans;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.Membrane;
import org.acegisecurity.userdetails.UserDetailsService;
import static org.easymock.EasyMock.expect;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.mock.MockServiceReference;

import javax.sql.DataSource;

/**
 * @author Rhett Sutphin
 */
public class HostBeansInitializerTest extends StudyCalendarTestCase {
    private HostBeansInitializer initializer;
    private HostBeans hostBeans;

    private BundleContext bundleContext;
    private Membrane membrane;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        initializer = new HostBeansInitializer();
        hostBeans = registerNiceMockFor(HostBeans.class);
        bundleContext = registerMockFor(BundleContext.class);
        membrane = registerMockFor(Membrane.class);

        initializer.setBundleContext(bundleContext);
        initializer.setMembrane(membrane);
    }

    public void testDataSourcePassedOnOnInit() throws Exception {
        DataSource dataSource = registerMockFor(DataSource.class);
        initializer.setDataSource(dataSource);

        expectGetHostBeansFromBundleContext();
        hostBeans.setDataSource(dataSource); // expect

        replayMocks();
        initializer.afterPropertiesSet();
        verifyMocks();
    }

    public void testUserDetailsServicePassedOnOnInit() throws Exception {
        UserDetailsService uds = registerMockFor(UserDetailsService.class);
        initializer.setUserDetailsService(uds);

        expectGetHostBeansFromBundleContext();
        hostBeans.setUserDetailsService(uds); // expect

        replayMocks();
        initializer.afterPropertiesSet();
        verifyMocks();
    }

    private void expectGetHostBeansFromBundleContext() {
        MockServiceReference sr = new MockServiceReference();
        expect(bundleContext.getServiceReference(HostBeans.class.getName())).andReturn(sr);
        expect(bundleContext.getService(sr)).andReturn(hostBeans);
        expect(membrane.farToNear(hostBeans)).andReturn(hostBeans);
    }
}