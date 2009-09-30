<%@taglib prefix="tags" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="laf" tagdir="/WEB-INF/tags/laf"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@taglib prefix="commons" uri="http://bioinformatics.northwestern.edu/taglibs/commons" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<tags:javascriptLink name="psc-tools/misc"/>

<tags:stylesheetLink name="yui-sam/2.7.0/datatable"/>
<%-- TODO: move common YUI parts to a tag if they are re-used --%>
<c:forEach items="${fn:split('yahoo-dom-event element-min datasource-debug logger-min json-min connection-min get-min datatable-min', ' ')}" var="script">
   <tags:javascriptLink name="yui/2.7.0/${script}"/>
</c:forEach>

<html>
  <head>
      <title>Study Manipulations</title>
      <tags:includeScriptaculous/>

    <style type="text/css">
      div.submit {
          text-align: center;
      }
      form {
          width: 90%;
      }
      ul#population-checkboxes {
          margin: 0; padding: 0;
      }
      ul#population-checkboxes li {
          list-style-type: none
      }

      td.existingSubjectContent  {
          vertical-align:top;
      }

      .bundle-list {
          padding-top:5em;
          padding-left:1em;
          width:100%
      }

      .selectedInfo {
          padding: 1em;
      }

      .myLi {
          list-style-type: none;
          padding: 0 0 5px 0;
      }

      .myUl {
          margin: 0 0 0 9em;
          padding: 0;
      }


      /*.mainTable{*/
          /*width:100%;*/
          /*border:1em;*/
      /*}*/
      /*.mainForm {*/
          /*width:100%;*/
      /*}*/


        /*.selectingIdDiv {*/
            /*padding-left:33%;*/
            /*padding-right:33%;*/
            /*float:left;*/
        /*}*/
      </style>
      <script type="text/javascript">
       var studyAutocompleter;


       function createStudyAutocompleter() {
            studyAutocompleter = new SC.FunctionalAutocompleter(
                'study-autocompleter-input', 'study-autocompleter-div', studyAutocompleterChoices, {
                    select: "subjects-name",
                    afterUpdateElement: function(input, selected) {
                        input.value = ""
                        input.focus()
                    }
                }
            );
        }

        var bundleList;

       function studyAutocompleterChoices(str, callback) {
           var searchString = $F("study-autocompleter-input")
           if (searchString == "Search for study") {
               searchString = ""
           }

           var uri = SC.relativeUri("/api/v1/provided-studies")
           if (searchString.blank()) {
               return;
           }

           var params = {};
           if (!searchString.blank()) params.q = searchString;


            SC.asyncRequest(uri+".json", {
                method: "GET", parameters: params,
                onSuccess: function(response) {
                     var bundleListColumns = [
                       { key: "long_title", label: "Long Title", sortable: true , formatter:YAHOO.widget.DataTable.formatLink},
                       { key: "assigned_identifier", label: "Assigned Identifier", sortable: true },
                       { key: "provider", label:"Provider", sortable:true},
                       { key: "secondary_identifiers", label: "Secondary Identifier", sortable: true, formatter: secondaryIdentifierFormatter},
                       {key: "button", label:"Show record data", formatter:myButtonFormatter}
                    ];


//                    var myLogReader = new YAHOO.widget.LogReader();


                    var myDataSource = new YAHOO.util.DataSource(response.responseJSON);
                    myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSON;

                    myDataSource.responseSchema = {
                        resultsList : "studies",
                        fields : [
                            { key: "long_title", formatter:YAHOO.widget.DataTable.formatLink},
                            { key: "assigned_identifier"},
                            { key: "provider"},
                            { key: "secondary_identifiers", formatter: secondaryIdentifierFormatter},
                            { key: "button", label:"Show record data", formatter:myButtonFormatter}
                        ]
                    };


                    bundleList = new YAHOO.widget.DataTable("bundle-list", bundleListColumns, myDataSource, {scrollable:true});
                }
            })

           var myButtonFormatter = function (elCell, oRecord, oCollumn, oData) {
               var container = jQuery('<div class="row" />')
               var divSubmit = jQuery('<div class="submit" />')
               var submitButton = jQuery('<input type="submit" value="Assign" />')

               divSubmit.append(submitButton);
               container.append(divSubmit);
                    

               jQuery(elCell).append(container);
               YAHOO.util.Event.addListener( submitButton, "click", myClickHandler, oRecord);

           }

            var myClickHandler = function(event, oRecord){
                event.stop();
                var elt = $(event.target);
                var parent = jQuery(elt.up());//gives us div element
                var trParent = parent.closest('tr')
                var tdRowChildren = trParent.children('td')

                var assignedId = tdRowChildren[1].getElementsByTagName('div')[0].innerHTML
                var provider = tdRowChildren[2].getElementsByTagName('div')[0].innerHTML
                var secondaryIds = tdRowChildren[3].getElementsByTagName('li')
                var studyIdentifier = "${study.assignedIdentifier}"

                var text1 = "<p class='selectedInfo'> You picked '" + assignedId + "' from '" + provider + "'. <br> Pick the identifier to use with this study in PSC: <br>";
                var container = jQuery('#edit-notes-lightbox');

                container.empty();

                container.append(jQuery('<h1> Assigning new study identifier </h1> '))

                var input1 = jQuery('<input type="radio" name="group" checked value="' + studyIdentifier +' >' )
                var input2 = jQuery('<input type="radio" name="group" value="' + assignedId + '>')

                var keepStudyId = "Keep <b>" + studyIdentifier + "</b>"
                var keepAssignedId = "Use <b>" + assignedId + "</b> (suggested by provider)"

                var divRow1 = jQuery('<div class="row"/>')
                var divLabel1 = jQuery('<div class="label"/>')
                var divValue1 = jQuery('<div class="value"/>')

                divLabel1.append(input1);
                divValue1.append(keepStudyId);
                divRow1.append(divLabel1);
                divRow1.append(divValue1);


                var divRow2 = jQuery('<div class="row"/>')
                var divLabel2 = jQuery('<div class="label"/>')
                var divValue2 = jQuery('<div class="value"/>')

                divLabel2.append(input2);
                divValue2.append(keepAssignedId);
                divRow2.append(divLabel2);
                divRow2.append(divValue2);

                container.append(divRow1)
                container.append(divRow2)
                for (var i = 0; i < secondaryIds.length; i++) {
                    var input3 = jQuery('<input type="radio" name="group" value="' + studyIdentifier + '>');
                    var keepSecondaryId = "Use secondary identifier <b>" + secondaryIds[i].innerHTML + "</b>"
                    var divRow3 = jQuery('<div class="row"/>')
                    var divLabel3 = jQuery('<div class="label"/>')
                    var divValue3 = jQuery('<div class="value"/>')

                    divLabel3.append(input3);
                    divValue3.append(keepSecondaryId);
                    divRow3.append(divLabel3);
                    divRow3.append(divValue3);

                    container.append(divRow3)
                }
                var divRowForButton = jQuery('<div class="row/>')
                var divClassSubmit = jQuery('<div class="submit"/>')

                var submitButton = jQuery('<input id="submitBtn" type="submit" value="Assign">')
                var cancelButton = jQuery('<input id="cancelBtn" type="submit" value="Cancel">')


                divClassSubmit.append(submitButton)
                divClassSubmit.append(cancelButton)

                divRowForButton.append(divClassSubmit)

                container.append(divRowForButton)
                LB.Lightbox.activate();


                YAHOO.util.Event.addListener( cancelButton, "click", function(e){
                    e.stop()
                    LB.Lightbox.deactivate()
                });
            };

         var secondaryIdentifierFormatter = function (elCell, oRecord, oColumn, oData) {
            var container = jQuery('<div class="secondaryIdDiv"/>');
            var list = container.append('<ul class="myUl"/>')
            var text = "";
            var identifiers = oRecord.getData('secondary_identifiers'), identifier = undefined, i = 0;
            if (identifiers.length>3) {
                for (i =0; i <3; i++) {
                    var listItem1 = jQuery('<li class="myLi show" />');
                    var type1 = identifiers[i].type;
                    var value1 = identifiers[i].value;
                    text = type1 + " : " + value1;
                    listItem1.text(text)
                    list.append(listItem1);
                }
                for (i = 3; i <identifiers.length; i++) {
                    var listItem2 = jQuery('<li class="myLi hide" />');
                    var type2 = identifiers[i].type;
                    var value2 = identifiers[i].value;
                    text = type2 + " : " + value2 ;
                    listItem2.text(text)
                    list.append(listItem2);
                }
            } else {
                for (i = 0; i <identifiers.length; i++) {
                    var listItem3 = jQuery('<li class="myLi show" />');
                    var type3 = identifiers[i].type;
                    var value3 = identifiers[i].value;
                    text = type3 + " : " + value3 ;
                    listItem3.text(text)
                    list.append(listItem3);
                }
            }

            jQuery('.hide').hide() 

            if (identifiers.length >3) {
                var showMoreLink = jQuery('<a class="more"/>').attr('href', '#').text('More').click(function (evt) {
                   var elt = $(evt.target);
                   var parent = jQuery(elt.up());  //gives us div element
                    var arrayOfHiddenSecondaryIds = parent.children('.hide');
                   if (parent.children('.hide')[0].style.display == 'none'){
                       for (var i = 0; i<arrayOfHiddenSecondaryIds.length; i++){
                           arrayOfHiddenSecondaryIds[i].show();
                       }
                       parent.children('.more')[0].innerHTML = 'Less';
                   } else {
                       for (var i = 0; i<arrayOfHiddenSecondaryIds.length; i++){
                           arrayOfHiddenSecondaryIds[i].hide();
                       }
                       parent.children('.more')[0].innerHTML = 'More';
                   }
               });

               container.append(showMoreLink)
            }
            jQuery(elCell).append(container);
        };


           // Override the built-in formatter
	        YAHOO.widget.DataTable.formatLink = function(elCell, oRecord, oColumn, oData) {
                var title = oRecord.getData('long_title');
                var partOfTitle;
                if (title.length > 100) {
                    partOfTitle = title.substr(0, 100)+ "... ";
                } else {
                    partOfTitle = title
                }
               title = title + " ";
               var spanShortTitle = jQuery('<span class="short"/>').text(partOfTitle);
               var spanLongTitle = jQuery('<span class="long"/>').text(title);


               jQuery(elCell).append(spanShortTitle);
               jQuery(elCell).append(spanLongTitle);
               jQuery('.long').hide();

               var showMoreLink = jQuery('<a class="more"/>').attr('href', '#').text('More').click(function (evt) {
                   var elt = $(evt.target);
                   var parent = jQuery(elt.up());//gives us div element
                   if (parent.children('.short')[0].style.display == 'none'){
                     parent.children('.short')[0].show();
                     parent.children('.long')[0].hide();
                     parent.children('.more')[0].innerHTML = 'More';
                   } else {
                       parent.children('.short')[0].hide();
                       parent.children('.long')[0].show();
                       parent.children('.more')[0].innerHTML = 'Less';
                   }

               });

               jQuery(elCell).append(showMoreLink);
               jQuery('.long').hide();
	        };

       }

      

        $(document).observe('dom:loaded', function() {
            createStudyAutocompleter();
        })

      </script>
      <style type="text/css">
          ul {
              padding:0;
          }

          ul.sites {
              width:80%
          }

          ul.subjects {
              padding-bottom:1em
          }

          li {
              margin: 0;
              padding-left:1em;
              list-style:none;
          }

          label.site {
              padding:.2em;
              font-weight:bold;
          }

          div.row div.study {
              float:left;
          }

          .site-coord-dash-link {
              color:#0000cc;
              cursor:pointer;
              white-space:nowrap;
          }
      </style>
  </head>
  <body>
  <laf:box title="Study Manipulations" cssClass="yui-skin-sam" autopad="true">
      <laf:division>
          <form:form id="PCSelectionForm" action="${action}">
            <form:errors path="*"/>
                <div class="row">
                     <div class="label">
                        Find external study:
                     </div>
                     <div class="value">
                            <input id="study-autocompleter-input" class="autocomplete input-hint" type="text" autocomplete="off" hint="Search for study" value=""/>
                            <div id="study-autocompleter-div" class="autocomplete" style="display: none;"/>
                     </div>
                </div>
                <div id="bundle-list" class="bundle-list">
                    </div>
            </form:form>

            <div id="lightbox">
                <div id="edit-notes-lightbox">
                </div>
            </div>

        </laf:division>
  </laf:box>
  </body>
</html>