package edu.northwestern.bioinformatics.studycalendar.web.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.FilterSecurityInterceptorConfigurer;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.LegacyModeSwitch;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBasedDictionary;
import gov.nih.nci.cabig.ctms.tools.spring.BeanNameControllerUrlResolver;
import org.acegisecurity.GrantedAuthority;
import org.easymock.classextension.EasyMock;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Map;
import java.util.Vector;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static org.easymock.classextension.EasyMock.*;

@SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
public class ControllerSecureUrlCreatorTest extends StudyCalendarTestCase {
    private static final String PREFIX = "prefix";

    private ControllerSecureUrlCreator creator;
    private DefaultListableBeanFactory beanFactory;
    private BeanNameControllerUrlResolver resolver;
    private OsgiLayerTools osgiLayerTools;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        osgiLayerTools = registerMockFor(OsgiLayerTools.class);
        osgiLayerTools.updateConfiguration((Dictionary) notNull(),
            eq(FilterSecurityInterceptorConfigurer.SERVICE_PID));
        expectLastCall().asStub();

        resolver = new BeanNameControllerUrlResolver();
        resolver.setServletName(PREFIX);

        creator = new ControllerSecureUrlCreator();
        creator.setUrlResolver(resolver);
        creator.setOsgiLayerTools(osgiLayerTools);
        ControllerRequiredAuthorityExtractor extractor = new ControllerRequiredAuthorityExtractor();
        extractor.setLegacyModeSwitch(new LegacyModeSwitch(false));
        creator.setControllerRequiredAuthorityExtractor(extractor);

        beanFactory = new DefaultListableBeanFactory();
    }

    private void registerControllerBean(String name, Class clazz) {
        String beanName = name + "Controller";
        beanFactory.registerBeanDefinition(beanName, createBeanDef(clazz));
        beanFactory.registerAlias(beanName, '/' + name);
    }

    private BeanDefinition createBeanDef(Class clazz) {
        return new RootBeanDefinition(clazz);
    }

    public void testSingleGroupRegistered() throws Exception {
        registerControllerBean("single", SingleGroupController.class);
        doProcess();

        PscRole[] defs = actualRolesForPath("/prefix/single/**");
        assertEquals("Wrong number of roles", 1, defs.length);
        assertEquals("Wrong Role", DATA_READER, defs[0]);
    }

    public void testMultiGroupRegistered() throws Exception {
        registerControllerBean("multi", MultiGroupController.class);
        doProcess();

        PscRole[] defs = actualRolesForPath("/prefix/multi/**");
        assertEquals("Wrong Protection Group Size", 2, defs.length);

        assertEquals("Wrong Role", STUDY_QA_MANAGER, defs[0]);
        assertEquals("Wrong Role", STUDY_CALENDAR_TEMPLATE_BUILDER, defs[1]);
    }

    public void testNoGroupAllowsNone() throws Exception {
        registerControllerBean("zero", NoGroupController.class);
        doProcess();

        PscRole[] roles = actualRolesForPath("/prefix/zero/**");
        assertEquals("Wrong number of roles", 0, roles.length);
    }

    public void testAllAllowsAll() throws Exception {
        registerControllerBean("all", AllGroupController.class);
        doProcess();

        PscRole[] roles = actualRolesForPath("/prefix/all/**");
        assertEquals("Wrong number of roles", PscRole.values().length, roles.length);

        for (int i = 0; i < roles.length; i++) {
            PscRole actual = roles[i];
            assertEquals("Wrong role " + i, actual, PscRole.values()[i]);
        }
    }

    public void testMapResolvesPathsLongestFirst() throws Exception {
        registerControllerBean("long/plus", SingleGroupController.class);
        registerControllerBean("long", MultiGroupController.class);
        registerControllerBean("long/plus/more", NoGroupController.class);
        doProcess();

        Map<String, GrantedAuthority[]> actual = actualPathMap();
        GrantedAuthority[] noGroup = actual.get("/prefix/long/plus/more/**");
        assertEquals("Wrong number of groups: " + Arrays.asList(noGroup), 0, noGroup.length);
        GrantedAuthority[] singleGroup = actual.get("/prefix/long/plus/**");
        assertEquals("Wrong number of groups: " + Arrays.asList(singleGroup), 1, singleGroup.length);
        GrantedAuthority[] multiGroup = actual.get("/prefix/long/**");
        assertEquals("Wrong number of groups: " + Arrays.asList(multiGroup), 2, multiGroup.length);
    }

    public void testMapResolvesSeparatePathsWithSameLength() throws Exception {
        registerControllerBean("pear", SingleGroupController.class);
        registerControllerBean("pome", MultiGroupController.class);
        doProcess();

        GrantedAuthority[] singleGroup = actualRolesForPath("/prefix/pear/**");
        assertNotNull("Could not resolve pear", singleGroup);
        assertEquals("Wrong number of groups: " + Arrays.asList(singleGroup), 1, singleGroup.length);

        GrantedAuthority[] multiGroup = actualRolesForPath("/prefix/pome/**");
        assertNotNull("Could not resolve pome", multiGroup);
        assertEquals("Wrong number of groups: " + Arrays.asList(multiGroup), 2, multiGroup.length);
    }

    public void testProcessingUpdatesConfigurationAtEnd() throws Exception {
        registerControllerBean("pear", SingleGroupController.class);
        registerControllerBean("pome", MultiGroupController.class);
        EasyMock.reset(osgiLayerTools);
        osgiLayerTools.updateConfiguration(new MapBasedDictionary(Collections.singletonMap(
            FilterSecurityInterceptorConfigurer.PATH_ROLE_MAP_KEY,
            new Vector(Arrays.asList(
                "/prefix/pome/**|study_qa_manager study_calendar_template_builder",
                "/prefix/pear/**|data_reader"
            )
        ))), FilterSecurityInterceptorConfigurer.SERVICE_PID);
        doProcess();
    }

    public void testMapExposedAsFactoryBeanResult() throws Exception {
        registerControllerBean("pear", SingleGroupController.class);
        registerControllerBean("pome", MultiGroupController.class);
        doProcess();

        assertNotNull(creator.getObject());
        Map<String, PscRole[]> actual = (Map<String, PscRole[]>) creator.getObject();
        assertSame(actual, actualPathMap());
        assertEquals(2, actual.size());
    }

    public void testFactoryBeanForSingleton() throws Exception {
        doProcess();
        assertTrue(creator.isSingleton());
    }

    public void testFactoryBeanForMap() throws Exception {
        doProcess();
        assertEquals(Map.class, creator.getObjectType());
    }

    private PscRole[] actualRolesForPath(String path) {
        assertNotNull("Map is null", actualPathMap());
        PscRole[] roles = (PscRole[]) actualPathMap().get(path);
        assertNotNull("No roles for " + path, roles);
        return roles;
    }

    private Map<String, GrantedAuthority[]> actualPathMap() {
        return creator.getPathRoleMap();
    }

    private void doProcess() {
        replayMocks();
        resolver.postProcessBeanFactory(beanFactory);
        creator.postProcessBeanFactory(beanFactory);
        verifyMocks();
    }

    private static class TestingController implements Controller {
        public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
            throw new UnsupportedOperationException("handleRequest not implemented");
        }
    }

    @AuthorizedFor(DATA_READER)
    public static class SingleGroupController extends TestingController { }

    @AuthorizedFor({ STUDY_QA_MANAGER, STUDY_CALENDAR_TEMPLATE_BUILDER })
    public static class MultiGroupController extends TestingController { }

    @AuthorizedForAll
    public static class AllGroupController extends TestingController { }

    public static class NoGroupController extends TestingController { }

}
