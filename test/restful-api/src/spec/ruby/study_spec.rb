describe "/study" do
  before do
    @studies = [
      # Released, but not approved studies
      @nu481 = PscTest::Fixtures.createSingleEpochStudy("NU481", "Treatment1", ["A", "B"].to_java(:String)),
      @nu561 = PscTest::Fixtures.createSingleEpochStudy("NU561", "Treatment1", ["C", "D"].to_java(:String))
    ]
    @studies.each do|s|
      application_context['studyService'].createInDesignStudyFromExamplePlanTree(s)
    end
  end

  describe "GET" do
    it "forbids study templates access for unauthenticated users" do
      get "/studies/NU481/template", :as => nil
      response.status_code.should == 401
    end

    it "shows a specific template to a study admin given the study-identifier" do
      get "/studies/NU481/template", :as => :barbara

      response.status_code.should == 200
      response.status_message.should == "OK"
      response.content_type.should == 'text/xml'
    end

    describe "with released amendments" do
      describe "and removes" do
        before do
          application_context['amendmentService'].amend(@nu481)

          @nu481_epoch = @nu481.planned_calendar.epochs[0]
          @removing_amendment = PscTest::Fixtures.createAmendment("Remove B", PscTest::createDate(2008, 3, 7))
          @removing_amendment.addDelta(
            PscTest::createDeltaFor(
              @nu481_epoch,
              Psc::Domain::Delta::Remove.create(@nu481_epoch.study_segments[1])
            )
          )
          @nu481.setDevelopmentAmendment(@removing_amendment)
          application_context['amendmentService'].amend(@nu481)
        end

        it "it remembers indirectly added but later removed nodes" do
          get "/studies/NU481/template", :as => :barbara
          response.status_code.should == 200

          original_add = response.xml_doc.root.elements.to_a("//amendment/planned-calendar-delta/add").first
          original_add.should_not be_nil
          original_add.elements.to_a('//study-segment').collect { |e| e.attributes['name'] }.should == %w(A B)
        end

        it "can resolve removes to the originally added nodes" do
          get "/studies/NU481/template", :as => :barbara
          response.status_code.should == 200

          added_segments = response.xml_doc.root.elements.to_a("//amendment/planned-calendar-delta/add//study-segment")
          remove = response.xml_doc.root.elements.to_a("//amendment/epoch-delta/remove").first

          added_segments[1].attributes['id'].should == remove.attributes['child-id']
        end
      end
    end

    describe "and periods and planned activities" do
      before do
        period = PscTest::Fixtures.createPeriod(1, 14, 1)
        period.grid_id = "The-Period"
        period.addPlannedActivity(
          PscTest::Fixtures.createPlannedActivity( sample_activity('Eye exam'), 1 )
        )
        period.addPlannedActivity(
          PscTest::Fixtures.createPlannedActivity( sample_activity('Eye exam'), 8 )
        )
        sole_epoch = application_context['epochDao'].getById(@nu481.development_amendment.deltas[0].changes[0].child_id)
        sole_epoch.study_segments[0].addPeriod(period)

        application_context['amendmentService'].amend(@nu481) # release initial template

        removing_amendment = PscTest::Fixtures.createAmendment("Use reps", PscTest::createDate(2008, 3, 7))
        removing_amendment.addDelta(
          PscTest::createDeltaFor(
            period,
            Psc::Domain::Delta::Remove.create(period.planned_activities[1]),
            Psc::Domain::Delta::PropertyChange.create("duration.quantity", 14, 7),
            Psc::Domain::Delta::PropertyChange.create("repetitions", 1, 2)
          )
        )
        @nu481.setDevelopmentAmendment(removing_amendment)

        application_context['amendmentService'].amend(@nu481) # release amendment
      end

      it "can still export the XML" do
        get "/studies/NU481/template", :as => :barbara
        response.status_code.should == 200
      end

      it "preserves the original period attributes" do
        get "/studies/NU481/template", :as => :barbara
        response.status_code.should == 200

        original_period = response.xml_elements("//period").first
        original_period.should_not be_nil
        original_period.attributes['id'].should == 'The-Period'
        original_period.attributes['duration-quantity'].should == '14'
        original_period.attributes['repetitions'].should == '1'
      end

      it "reflects the changes in the amendment" do
        get "/studies/NU481/template", :as => :barbara
        response.status_code.should == 200

        delta = response.xml_elements("//period-delta").first
        delta.should_not be_nil
        delta.should have(3).elements
      end
    end
  end

  describe "PUT" do
    before do
      @nu482_xml = psc_xml("study", 'assigned-identifier' => "NU482") { |ss|
        ss.tag!('planned-calendar', 'id' =>'pc1')
        ss.tag!('development-amendment', 'name' => 'am1', 'date' => "2008-11-13", 'mandatory' => "true"){ |da|
          da.tag!('planned-calendar-delta', 'id' => 'd1', 'node-id' => 'pc1') { |pcd|
            pcd.tag!('add', 'id' => 'add1') { |add|
              add.tag!('epoch','id' => 'epoch1', 'name' => 'Treatment2') {|seg|
                seg.tag!('study-segment', 'id' => "segment1", 'name' => 'initial study')
              }
            }
          }
        }
      }
    end

    it "does not accept a study from a study admin" do
      put '/studies/NU482/template', @nu482_xml, :as => :barbara
      response.status_code.should == 403
    end

    it "it accepts a study from a study coordinator" do
      put '/studies/NU482/template', @nu482_xml, :as => :juno
      response.status_code.should == 201 #created
      get '/studies/', :as => :alice

      response.status_code.should == 200 #OK
      response.xml_attributes("study", "assigned-identifier").should include("NU482")
    end

    it "creates new template with all the type of duration for the period" do
      @nu483_xml = psc_xml("study", 'assigned-identifier' => "NU483") { |ss|
        ss.tag!('planned-calendar', 'id' =>'pc')
        ss.tag!('development-amendment', 'name' => 'am', 'date' => "2008-11-13", 'mandatory' => "true"){ |da|
          da.tag!('planned-calendar-delta', 'id' => 'd', 'node-id' => 'pc') { |pcd|
            pcd.tag!('add', 'id' => 'add') { |add|
              add.tag!('epoch','id' => 'epoch', 'name' => 'Treatment2') {|seg|
                seg.tag!('study-segment', 'id' => "segment", 'name' => 'initial study'){|period|
                  period.tag!('period', 'id' => "period1", 'name' => "With Day", 'repetitions' => "1",
                   'start-day' => "1", 'duration-quantity' => "28",  'duration-unit' => "day" )
                  period.tag!('period', 'id' => "period2", 'name' => "With Week", 'repetitions' => "1",
                   'start-day' => "1", 'duration-quantity' => "4",  'duration-unit' => "week" )
                  period.tag!('period', 'id' => "period3", 'name' => "With Quarter", 'repetitions' => "1",
                   'start-day' => "1", 'duration-quantity' => "3",  'duration-unit' => "quarter" )
                  period.tag!('period', 'id' => "period4", 'name' => "With Month", 'repetitions' => "1",
                   'start-day' => "1", 'duration-quantity' => "2",  'duration-unit' => "month" )
                  period.tag!('period', 'id' => "period5", 'name' => "With Fortnight", 'repetitions' => "1",
                    'start-day' => "1", 'duration-quantity' => "2",  'duration-unit' => "fortnight" )
                }
              }
            }
          }
        }
      }
      put '/studies/NU483/template', @nu483_xml, :as => :juno
      response.status_code.should == 201 #created
      get '/studies/NU483/template', :as => :juno

      response.status_code.should == 200 #OK
      response.xml_attributes("study", "assigned-identifier").should include("NU483")
      response.xml_elements('//period').size.should == 5
      response.xml_attributes("period", "name").should include("With Day")
      response.xml_attributes("period", "name").should include("With Week")
      response.xml_attributes("period", "name").should include("With Quarter")
      response.xml_attributes("period", "name").should include("With Month")
      response.xml_attributes("period", "name").should include("With Fortnight")
      response.xml_attributes("period", "duration-unit").should include("day")
      response.xml_attributes("period", "duration-unit").should include("week")
      response.xml_attributes("period", "duration-unit").should include("quarter")
      response.xml_attributes("period", "duration-unit").should include("month")
      response.xml_attributes("period", "duration-unit").should include("fortnight")
    end
  end

end
