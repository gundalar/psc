<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@page contentType="text/html;charset=UTF-8" language="java"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="templ" tagdir="/WEB-INF/tags/template" %>
<%@ taglib prefix="security"
           uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/security" %>
<%@ taglib prefix="laf" uri="http://gforge.nci.nih.gov/projects/ctmscommons/taglibs/laf" %>
<html>
    <head>
        <title>Template for ${study.name}</title>
        <tags:stylesheetLink name="main"/>
        <tags:includeScriptaculous/>
        <tags:javascriptLink name="main"/>
        <style type="text/css">
            .epochs-and-arms {
                margin: 1em;
            }

            #epochs-indicator {
                margin: 0.5em 0.5em 0 0;
                float: left;
            }

            table.periods, table.periods tr, table.periods td, table.periods th {
                border-spacing: 0;
                border: 0 solid #666;
                margin: 1em;
            }
            table.periods td, table.periods th {
                width: 2em;
            }
            table.periods th {
                padding: 2px;
                border-right-width: 1px;
            }
            table.periods th.row {
                padding-right: 0.5em;
                text-align: right;
                white-space: nowrap;
            }
            table.periods th.column {
                border-top-width: 1px;
            }
            table.periods tr.resume th {
                border-right: 1px solid #ddd;
            }
            table.periods td {
                padding: 0;
                border-width: 1px 1px 0 0;
                text-align: center;
            }
            table.periods td.repetition a, table.periods td.repetition span {
                text-decoration: none;
                margin: 0;
                padding: 2px;
                display: block;
                color: #444;
                /* For IE */
                height: 1%;
            }
            table.periods a:hover {
                color: #000;
            }
            table.periods td.repetition.editable:hover {
                background-color: #ccc;
            }
            table.periods td.repetition {
                background-color: #ddd;
                border-right-width: 0;
            }
            table.periods td.empty {
                background-color: #fff;
                border-right-width: 0;
            }
            table.periods td.last {
                border-right-width: 1px;
            }
            table.periods tr.last td {
                border-bottom-width: 1px;
            }
            table.periods td.controls {
                border-width: 0 !important;
                padding: 3px;
            }
            table.periods tr.arrows td {
                border-width: 0 !important;
                border-top-width: 1px;
            }
            .days {
                margin: 0 3em 3em 5em;
            }
            li.arm, #epochs h4 {
                position: relative;

            }
            /* For IE */
            * html #epochs h4 { height: 1px; }
            .controls, table.periods a.control {
                font-family: Arial, sans-serif;
                font-size: 7.5pt;
            }
            div.arm-controls, div.epoch-controls {
                position: absolute;
                bottom: 4px;
                right: 2px;
                text-align: right;
            }
            span.study-controls {
                margin-left: 4px;
            }

            .inplaceeditor-form a {
                font-size: 11pt;
                border: 1px solid #444;
                padding: 3px;
            }

            ul#admin-options {
                padding: 0;
                margin: 0 0 1em 0;
            }
            ul#admin-options li {
                display: inline;
                padding: 2px 4px;
                margin: 0;
                list-style-type: none;
            }

            #revision-changes {
                float: right;
                width: 29%;
            }

            #with-changes #selected-arm {
                width: 70%;
                float: left;
            }
        </style>
        <c:if test="${not empty developmentRevision}">
            <script type="text/javascript" src="<c:url value="/pages/cal/template/edit.js?study=${study.id}&studyName=${study.name}"/>"></script>
        </c:if>
        <script type="text/javascript">
            var lastRequest;
            var selectedArmId = ${arm.base.id};
			var days;
			var day;
			var showButton;
			var hideButton;
			var showDay;
			var showMonth;
			var hideMonth;
			var presentMonths;
			var hiddenMonths;
			var allDaysArePresent = false;
			var allDaysAreHidden = true;
			var init;
			
            function registerSelectArmHandlers() {
                $$('#epochs a').each(registerSelectArmHandler)
            }
			function initialize_arrows()
			{
				 days = $$(".days");
				 day = $$(".day");
				 showButton = $('show_button');
				 hideButton = $('hide_button');
				 showMonth =  $$(".showMonth");
				 hideMonth =  $$(".hideMonth");
				 presentMonths = [];
				 showDay = $$(".showDay");
				 hiddenMonths = [];
				 allDaysArePresent = false;
				 allDaysAreHidden = true;
				 init = true;
			}
			function check()
			{
			
				// Will figure out when the Show Month Arrows should be visible
			    days.each(function(dc, counter) {
					
		            if (isMonthPresent(counter)){
                        hideMonth[counter].reveal();
                        showMonth[counter].conceal();
						presentMonths[counter] = true;
                    }
 					else {
                        showMonth[counter].reveal();
						presentMonths[counter] = false;
						
                    }
				})

				
				// Will figure out when the Hide Month Arrows should be visible
				
				days.each(function(dc, counter) {
                   if (isMonthHidden(counter)){
                       hideMonth[counter].conceal();
					   if(!isMonthPresent(counter)) showMonth[counter].reveal();
					   hiddenMonths[counter] = true;
					
                   }
					else {
                       hideMonth[counter].reveal();
					   hiddenMonths[counter] = false;
					
                   }
			     })
			
			
				// Will figure out when the Show All button should be visible
				allDaysArePresent = true
				for (var i=0; i < presentMonths.length; i++)
				{
					if (allDaysArePresent && presentMonths[i])
						allDaysArePresent = true
					else allDaysArePresent = false
					
				}
				
				if (allDaysArePresent)	showButton.conceal()
				else showButton.reveal()

				// Will figure out when the Hide All button should be visible
				
				allDaysAreHidden = true
				for (var i=0; i < hiddenMonths.length; i++)
				{
					if (allDaysAreHidden && hiddenMonths[i])
						allDaysAreHidden = true
					else allDaysAreHidden = false
					
				}
				
				if (allDaysAreHidden) hideButton.conceal()
				else hideButton.reveal()
				
			}

			function isDayPresent(day1){
				var present
				if ($(day1).style.display == "none")
					present = false
				else present = true
				return present
			}
			function allDaysArePresent(){
				var continue1 = true
				
				day.each(function(e){ 
				
					if (isDayPresent($(e)) && continue1){
					continue1 = true
					}
					else continue1 = false
		
				})
				return continue1
			}
			function allDaysAreHidden(){
				var continue1 = true
				
				day.each(function(e){ 
				
					if (!isDayPresent($(e)) && continue1){
					continue1 = true
					}
					else continue1 = false
		
				})
				return continue1
			}
			function isMonthPresent(num)
			{
				var continue1 = true
				
				days[num].getElementsByClassName('day').each(function(e){ 
				
					if (isDayPresent($(e)) && continue1){
					continue1 = true
					}
					else continue1 = false
		
				})
				return continue1
			}
			function isMonthHidden(num)
			{
				var continue1 = true

				days[num].getElementsByClassName('day').each(function(e){ 
				
					if (!isDayPresent($(e)) && continue1){
					continue1 = true
					}
					else continue1 = false
		
				})
				return continue1
			}
			function quickSlideAndShow(elementArray, options)
			{
				var eA = elementArray
				var effects = []
				eA.each(function(e) {
					effects = effects.concat([
						new Effect.BlindDown(e, {sync:true}),
			        	new Effect.Appear(e, {sync:true})
					])
				})

			   	 	new Effect.Parallel(
				       	effects,
						$H(options).merge({
					         duration: 1.0
					    })
					);
			}			
			function quickSlideAndHide(elementArray, options)
			{
				var eA = elementArray
				var effects = []
				eA.each(function(e) {
					effects = effects.concat([
						new Effect.BlindUp(e, {sync:true}),
			        	new Effect.Fade(e, {sync:true})
					])
				})

			   	 	new Effect.Parallel(
				       	effects,
						$H(options).merge({
					         duration: 1.0
					    })
					);
			}
	            function registerSelectArmHandler(a) {
                var aElement = $(a)
                Event.observe(aElement, "click", function(e) {
                    Event.stop(e)
                    $("epochs-indicator").reveal();
                    SC.slideAndHide('selected-arm-content', { afterFinish: function() {
                        // deselect current
                        var sel = $$("#epochs .selected")
                        if (sel && sel.length > 0) Element.removeClassName(sel[0], "selected")

                        var armId = aElement.id.substring(4)
                        selectedArmId = armId
                        aElement.href = '<c:url value="/pages/cal/template/select"/>?arm=' + armId

                        lastRequest = new Ajax.Request(aElement.href,
                            {
                                onComplete: function(req) {
                                    $("epochs-indicator").conceal()
                                },
                                onFailure: function() {
                                    Element.update('selected-arm-content', "<p class='error'>Loading failed</p>")
                                    Element.update('selected-arm-header', "Error")
                                    SC.slideAndShow('selected-arm-content')
                                }
                            }
                        );
                    } });
                })
            }
			function registerShowHandler(){
				Event.observe(showButton, "click", function(e) {
		            Event.stop(e);
				    day.each(SC.slideAndShow);
				   	showMonth.each(function(e){e.conceal();});
  					hideMonth.each(function(e){e.reveal();});
					showButton.conceal()
					hideButton.reveal()
					
					showDay.each(function (e){$(e).update('<a href="#" class="control showArrow" id="showArrow"><b>&#8212;</b></a>');});
					
				}							
				);				
			}
			
			function showSetup(){
				if (init == null) initialize_arrows();
				registerShowHandler()
			}
			
			function registerHideHandler(){
				var hide = $("hide_button")
				Event.observe(hide, "click", function(e) {
		            Event.stop(e);
				    day.each(SC.slideAndHide);
					showMonth.each(function(e){e.reveal();});
  					hideMonth.each(function(e){e.conceal();});
					showButton.conceal()
					hideButton.reveal()
					
					showDay.each(function (e){$(e).update('<a href="#" class="control showArrow" id="showArrow">&#65291;</a>');});
				}							
				);	
			
			}
			function hideSetup(){
				registerHideHandler()
			}
			
			function registerArrowHandler(a, counter){
				var aElement = $(a)
				
				Event.observe(aElement, "click", function(e) {
                    Event.stop(e)
					if ($('showArrow').innerHTML == '<b>—</b>'){
                		Element.update(this, '<a href="#" class="control showArrow" id="showArrow">&#65291;</a>')
						SC.slideAndHide(day[counter], {afterFinish: check})
				
						
					}
                	else{
						Element.update(this, '<a href="#" class="control showArrow" id="showArrow"><b>&#8212;</b></a>')
						SC.slideAndShow(day[counter], {afterFinish: check})	
					}
                })
			}
			function registerArrowHandlers(){
				var counter = 0;
                showDay.each(function(num) {registerArrowHandler(num, counter); counter++;});
            }
			
			function registerShowMonthHandler(a, counter){
				var aElement = $(a)
				Event.observe(aElement, "click", function(e) {
		            Event.stop(e)          		
						quickSlideAndShow(days[counter].getElementsByClassName('day'), {afterFinish: check})
				})
			}
			function registerHideMonthHandler(a, counter){
				var aElement = $(a)
				Event.observe(aElement, "click", function(e) {
		            Event.stop(e)          		
						quickSlideAndHide(days[counter].getElementsByClassName('day'), {afterFinish: check})
				})
			}

            function epochsAreaSetup() {
                registerSelectArmHandlers()
                <c:if test="${not empty developmentRevision}">
                    createAllArmControls()
                    createAllEpochControls()
                </c:if>
            }
			function arrowSetup(){
				registerArrowHandlers()
			}
			function showMonthSetup(){
				var counter = 0;
				showMonth.each(function(num) {registerShowMonthHandler(num, counter); counter++;});
			}
			function hideMonthSetup(){
				var counter = 0;
				hideMonth.each(function(num) {registerHideMonthHandler(num, counter); counter++;});
				
			}
			function initializeNewArm(){
			    if ($$(".day").length != 0){
			    	initialize_arrows()
					showSetup()
					hideSetup()
					arrowSetup()
					showMonthSetup()
					hideMonthSetup()
				}
					
			}


            <c:if test="${not empty developmentRevision}">
                Event.observe(window, "load", createStudyControls)
            </c:if>
            Event.observe(window, "load", epochsAreaSetup)
			<c:if test="${not empty arm.months}">
				Event.observe(window, "load", showSetup)
				Event.observe(window, "load", hideSetup)
				Event.observe(window, "load", arrowSetup)
				Event.observe(window, "load", showMonthSetup)
				Event.observe(window, "load", hideMonthSetup)
		    </c:if>
			
			
        </script>
    </head>
    <body>

        <ul id="admin-options">
            <c:if test="${not empty developmentRevision}">
                <tags:restrictedListItem url="/pages/cal/template/release" queryString="study=${study.id}" cssClass="control">
                    Release this ${study.inInitialDevelopment ? 'template' : 'amendment'} for use
                </tags:restrictedListItem>
            </c:if>
            <c:if test="${empty developmentRevision}">
                <tags:restrictedListItem cssClass="control" url="/pages/cal/assignSite" queryString="id=${study.id}">Assign sites</tags:restrictedListItem>
                <c:if test="${not empty study.studySites}">
                    <tags:restrictedListItem url="/pages/cal/assignSubject" queryString="id=${study.id}" cssClass="control"
                        >Assign Subject</tags:restrictedListItem>
                </c:if>
                <c:if test="${not empty onStudyAssignments}">
                    <security:secureOperation element="/pages/cal/schedule">
                    <li>View schedule (On Study) for
                        <select id="assigned-subject-selector">
                            <c:forEach items="${onStudyAssignments}" var="assignment">
                                <option value="${assignment.scheduledCalendar.id}">${assignment.subject.lastFirst}</option>
                            </c:forEach>
                        </select>
                        <a class="control" href="<c:url value="/pages/cal/schedule"/>" id="go-to-schedule-control">Go</a>
                    </li>
                    </security:secureOperation>
                </c:if>
                <c:if test="${not empty offStudyAssignments}">
                    <security:secureOperation element="/pages/cal/schedule">
                    <li>View schedule (Off Study) for
                        <select id="offstudy-assigned-subject-selector">
                            <c:forEach items="${offStudyAssignments}" var="assignment">
                                <option value="${assignment.scheduledCalendar.id}">${assignment.subject.lastFirst}</option>
                            </c:forEach>
                        </select>
                        <a class="control" href="<c:url value="/pages/cal/schedule"/>" id="offstudy-go-to-schedule-control">Go</a>
                    </li>
                    </security:secureOperation>
                </c:if>
            </c:if>
        </ul>
        <div id="epochs" class="section">
            <laf:box title="Epochs and arms">
                <laf:division>
                    <tags:epochsAndArms id="epochs-container" plannedCalendar="${plannedCalendar}" selectedArm="${arm.base}"/>
                </laf:division>
            </laf:box>
        </div>

        <c:set var="showChanges" value="${not empty developmentRevision and not study.inInitialDevelopment}"/>
        <c:if test="${showChanges}">
            <div id="with-changes">
                <div id="revision-changes" class="section">
                    <templ:changes revision="${developmentRevision}" changes="${revisionChanges}"/>
                </div>
            <%-- #with-changes is closed below --%>
        </c:if>

        <div id="selected-arm" class="section">
            <templ:arm arm="${arm}" developmentRevision="${developmentRevision}" visible="true"/>
        </div>

        <c:if test="${showChanges}"></div></c:if>

    </body>
</html>