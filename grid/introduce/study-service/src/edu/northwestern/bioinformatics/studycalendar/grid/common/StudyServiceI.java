/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.grid.common;

import java.rmi.RemoteException;

/** 
 * This class is autogenerated, DO NOT EDIT.
 * 
 * This interface represents the API which is accessable on the grid service from the client. 
 * 
 * @created by Introduce Toolkit version 1.0
 * 
 */
public interface StudyServiceI {

    public gov.nih.nci.cagrid.metadata.security.ServiceSecurityMetadata getServiceSecurityMetadata() throws RemoteException ;

    public edu.northwestern.bioinformatics.studycalendar.grid.Study retrieveStudyByAssignedIdentifier(java.lang.String assignedIdentifier) throws RemoteException, edu.northwestern.bioinformatics.studycalendar.grid.stubs.types.StudyDoesNotExistsException ;

    public edu.northwestern.bioinformatics.studycalendar.grid.Study createStudy(edu.northwestern.bioinformatics.studycalendar.grid.Study study) throws RemoteException, edu.northwestern.bioinformatics.studycalendar.grid.stubs.types.StudyCreationException ;

}

