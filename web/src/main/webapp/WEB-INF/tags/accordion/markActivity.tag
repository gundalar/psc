<%@tag%>
<%@taglib prefix="jsgen" uri="http://bioinformatics.northwestern.edu/taglibs/studycalendar/jsgenerator"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<h4>Study:
<c:set var="studiesCountGreaterThanOne" value="false"/>
<c:if test="${not empty schedule.studies && fn:length(schedule.studies) gt 1}">
    <c:set var="studiesCountGreaterThanOne" value="true"/>
</c:if>

<c:if test="${studiesCountGreaterThanOne}">
    <select id="studySelector" class="delayAdvanceSelector">
        <option value="all" selected="true">All Studies </option>
        <c:forEach items="${schedule.studies}" var="row" varStatus="rowStatus">
            <option value="${row.id}">${row.name}</option>
        </c:forEach>
     </select>
</c:if>
<c:if test="${! studiesCountGreaterThanOne}">
    <c:forEach items="${schedule.studies}" var="row" varStatus="rowStatus">
        <input id="studyId" type="hidden" value="${row.id}"/>${row.name}
    </c:forEach>
</c:if>
</h4>

<div class="links-row">
    <h4>
        Select Activities:
        <a id="check-all-events"     class="batch-schedule-link" href="#">All,</a>
        <a id="uncheck-all-events"   class="batch-schedule-link" href="#">None,</a>
        <a id="check-all-conditional-events"  class="batch-schedule-link" href="#">Conditional,</a>
        <a id="check-all-past-due-events"  class="batch-schedule-link" href="#">Past due</a>
    </h4>
</div>
<h4>
<label id="new-mode-selector-group">

        <select name="newMode" id="new-mode-selector">
            <option value="-1">Select an action...</option>
            <option value="">Move the date</option>
            <option value="1">Mark/Keep as scheduled</option>
            <option value="2">Mark as occurred</option>
            <option value="3">Mark as canceled or NA</option>
            <option value="6">Mark as missed</option>
        </select>

</label>
<label id="new-date-input-group">and shift date by <input type="text" name="dateOffset" value="0" size="4"/> days.</label>
<label id="move_date_by_new-date-input-group"> by <input type="text" name="moveDateOffset" value="0" size="4"/> days.</label>
<label id="new-reason-input-group">
    Why? <input type="text" name="newReason"/>
</label>
</h4>
