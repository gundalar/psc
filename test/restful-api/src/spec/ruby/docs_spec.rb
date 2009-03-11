describe "/docs" do
  it "returns HTML by default" do
    get "/docs"
    response.status_code.should == 200
    response.content_type.should == 'text/html'
  end
  
  describe "/psc.wadl" do
    before do
      pending '#628'
      get "/docs/psc.wadl"
      response.status_code.should == 200
    end
    
    it "returns WADL" do
      response.content_type.should == 'application/vnd.sun.wadl+xml'
    end
    
    it "contains the actual URI for the application in the resources" do
      response.entity.should =~ 'http://localhost:7200/psc/api/v1/studies'
    end
  end
  
  describe "/psc.xsd" do
    before do
      pending '#628'
      get "/docs/psc.xsd"
      response.status_code.should == 200
    end
    
    it "returns XML Schema" do
      response.content_type.should == 'application/x-xsd+xml'
    end
  end
end