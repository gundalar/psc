require "buildr"

###### buildr script for PSC
# In order to use this, you'll need buildr.  See http://buildr.apache.org/ .

VERSION_NUMBER="2.5-SNAPSHOT"
APPLICATION_SHORT_NAME = 'psc'

###### PROJECT

desc "Patient Study Calendar"
define "psc" do
  project.version = VERSION_NUMBER
  project.group = "edu.northwestern.bioinformatics.studycalendar"

  # resources.from(_("src/main/java")).exclude("**/*.java")
  compile.options.target = "1.5"
  # compile.with CTMS_COMMONS, CORE_COMMONS, SECURITY, XML, SPRING, HIBERNATE, 
  #   LOGBACK, SLF4J, JAKARTA_COMMONS, CAGRID, BERING, WEB, DB, CONTAINER_PROVIDED
  
  # test.resources.from(_("src/test/java")).exclude("**/*.java")
  # test.with(UNIT_TESTING, 'psc:test-infrastructure').include("*Test")

  # package(:war).exclude(CONTAINER_PROVIDED)
  # package(:sources)
  
  # resources task(:init)
  
  # db = ENV['DB'] || 'studycalendar'
  # dbprops = { } # Filled in by :init
  
  # test.resources task(:test_csm_config)
  
  # task :test_csm_config => :init do
  #   filter(_("conf/upt")).include('*.xml').into(_("target/test-classes")).
  #     using(:ant, {'tomcat.security.dir' => _("target/test-classes")}.merge(dbprops)).run
  # end

  task :public_demo_deploy do
    cp FileList[_("test/public/*")], "/opt/tomcat/webapps-vera/studycalendar/"
  end
  
  define "Pure utility code"
  define "utility" do
    compile.with SLF4J, SPRING, JAKARTA_COMMONS.collections, 
      JAKARTA_COMMONS.collections_generic, CTMS_COMMONS.lang
    test.with(UNIT_TESTING)
    package(:jar)
    package(:sources)
  end
  
  desc "The domain classes for PSC"
  define "domain" do
    compile.with project('utility'), SLF4J, CTMS_COMMONS, CORE_COMMONS, 
      JAKARTA_COMMONS, SPRING, HIBERNATE, SECURITY
    test.with(UNIT_TESTING)
    package(:jar)
    package(:sources)
  end
  
  desc "Core data access, serialization and non-substitutable business logic"
  define "core" do
    task :refilter do
      rm_rf Dir[_(resources.target.to_s, "applicationContext-{spring,setup}.xml")]
    end
    resources.enhance [:refilter]
    
    filter_tokens = {
      'application-short-name'  => APPLICATION_SHORT_NAME,
      'config.database'         => db_name,
      "buildInfo.versionNumber" => project.version,
      "buildInfo.username"      => ENV['USER'],
      "buildInfo.hostname"      => `hostname`.chomp,
      "buildInfo.timestamp"     => Time.now.strftime("%Y-%m-%d %H:%M:%S")
    }
    
    resources.from(_("src/main/java")).exclude("**/*.java").
      filter.using(:ant, filter_tokens)
    compile.with project('domain'), project('domain').compile.dependencies, 
      BERING, DB, XML, RESTLET.framework, FREEMARKER, CSV, CONTAINER_PROVIDED,
      QUARTZ, 
      SPRING_WEB # tmp for mail

    test.resources.from(_("src/test/java")).exclude("**/*.java")
    test.with UNIT_TESTING, project('domain').test.compile.target
    
    package(:jar)
    package(:sources)
    
    check do
      acSpring = File.read(_('target/resources/applicationContext-spring.xml'))
      acSetup = File.read(_('target/resources/applicationContext-setup.xml'))
      
      acSpring.should include(filter_tokens['config.database'])
      acSetup.should include(filter_tokens['buildInfo.hostname'])
      acSetup.should include(project.version)
    end
    
    task :migrate do
      ant('bering') do |ant|
        # Load DS properties from /etc/psc or ~/.psc
        datasource_properties(ant)

        # default values
        ant.property :name => 'migrate.version', :value => ENV['MIGRATE_VERSION'] || ""
        ant.property :name => 'bering.dialect', :value => ""
        
        ant.taskdef :resource => "edu/northwestern/bioinformatics/bering/antlib.xml",
          :classpath => ant_classpath(project('core'))
        ant.migrate :driver => '${datasource.driver}',
          :dialect => "${bering.dialect}",
          :url => "${datasource.url}",
          :userid => "${datasource.username}",
          :password => "${datasource.password}",
          :targetVersion => "${migrate.version}",
          :migrationsDir => _("src/main/db/migrate"),
          :classpath => ant_classpath(project('core'))
      end
    end

    task :create_hsqldb do |t|
      ENV['DB'] ||= 'hsqldb-psc'
      hsqldb_dir = ENV['HSQLDB_DIR'] || _('hsqldb')
      hsqldb_url = "jdbc:hsqldb:file:#{hsqldb_dir}/#{db_name}"
      rm_rf hsqldb_dir
      
      File.open("#{ENV['HOME']}/.psc/#{db_name}.properties", 'w') do |f|
        f.puts( (<<-PROPERTIES).split(/\n/).collect { |row| row.strip }.join("\n") )
          # Generated by PSC's psc:core:create_hsqldb task
          datasource.url=#{hsqldb_url}
          datasource.username=sa
          datasource.password=
          datasource.driver=org.hsqldb.jdbcDriver
        PROPERTIES
      end
      
      # Apply the bering migrations to build the HSQLDB schema
      mkdir hsqldb_dir
      task(:migrate).invoke
      
      # Explicit shutdown required to allow other processes to open the database
      ant('sql') do |ant|
        ant.sql(
          :driver => "org.hsqldb.jdbcDriver", :url => hsqldb_url,
          :userid => "sa", :password => "", 
          :classpath => ant_classpath(project('core')), 
          :autocommit => "true",
          :pcdata => "SHUTDOWN SCRIPT;")
      end
      
      # Mark read-only
      File.open("#{hsqldb_dir}/#{db_name}.properties", 'a') do |f|
        f.puts "hsqldb.files_readonly=true"
      end
      
      puts "Read-only HSQLB instance named #{db_name} generated in #{hsqldb_dir}"
    end
  end
  
  desc "Web interfaces, including the GUI and the RESTful API"
  define "web" do
    compile.with LOGBACK, project('core'), project('core').compile.dependencies, 
      SPRING_WEB, RESTLET, WEB, CAGRID
    test.with project('test-infrastructure'), 
      project('test-infrastructure').compile.dependencies,
      project('test-infrastructure').test.compile.dependencies
    package(:war).exclude(CONTAINER_PROVIDED)
    package(:sources)
  end
  
  desc "Common test code for both the module unit tests and the integrated tests"
  define "test-infrastructure", :base_dir => _('test/infrastructure') do
    compile.with UNIT_TESTING, INTEGRATED_TESTING, SPRING_WEB,
      project('core'), project('core').compile.dependencies
    test.with project('core').test.compile.target, 
      project('core').test.compile.dependencies
    package(:jar)
    package(:sources)
  end
end

###### Top-level aliases for commonly-used tasks

desc "Update the core database schema via bering"
task :migrate => 'psc:core:migrate'

desc "Recreate the HSQLDB instance for unit testing"
task :create_hsqldb => 'psc:core:create_hsqldb'

###### HELPERS

def db_name
  ENV['DB'] || 'datasource'
end

def ant_classpath(proj)
  [proj.compile.dependencies + LOGBACK].flatten.collect { |a| a.to_s }.join(':')
end

# Discovers and loads the datasource properties file into the target ant project
def datasource_properties(ant)
  ant.taskdef :name => 'datasource_properties', 
    :classname => "gov.nih.nci.cabig.ctms.tools.ant.DataSourcePropertiesTask",
    :classpath => ant_classpath(project('psc:core'))
  ant.datasource_properties :applicationDirectoryName => APPLICATION_SHORT_NAME,
    :databaseConfigurationName => db_name
  ant.echo :message => "Migrating ${datasource.url}"
end