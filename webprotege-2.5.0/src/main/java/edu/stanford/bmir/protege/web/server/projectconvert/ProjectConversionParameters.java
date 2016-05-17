/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.bmir.protege.web.server.projectconvert;

import edu.stanford.bmir.protege.web.server.filedownload.DownloadFormat;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.project.ProjectIdFormatException;
import edu.stanford.bmir.protege.web.shared.revision.RevisionNumber;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author antonio
 */
public class ProjectConversionParameters {

    private HttpServletRequest request;
    
    public ProjectConversionParameters(HttpServletRequest request){
        this.request = request;
    }
    
    public boolean isProjectConvert(){
        String rawProjectNameParameter = getRawProjectNameParameter();
        boolean projectParamPresent = rawProjectNameParameter != null;
        if(!projectParamPresent){
            return false;
        }
         try {
            ProjectId.get(rawProjectNameParameter);
            return true;
        } catch (ProjectIdFormatException e) {
            return false;
        }
    }
    
     public ProjectId getProjectId() {
        String projectName = getRawProjectNameParameter();
        if(projectName == null) {
            throw new UnsupportedOperationException("getProjectId can only be called if the request is for a project conversion (isProjectConvert() returns true)");
        }
        return ProjectId.get(projectName);
    }
     
     public RevisionNumber getRequestedRevision() {
        String revisionString = getRawRevisionParameter();
        if(revisionString == null) {
            return RevisionNumber.getHeadRevisionNumber();
        }
        else {
            try {
                long rev = Long.parseLong(revisionString);
                return RevisionNumber.getRevisionNumber(rev);
            }
            catch (NumberFormatException e) {
                // TODO: Log!
                return RevisionNumber.getHeadRevisionNumber();
            }
        }
    }
     
      public DownloadFormat getFormat() {
        return DownloadFormat.getDefaultFormat();
    }
      
        private String getRawRevisionParameter() {
        return request.getParameter(ProjectConversionConstants.REVISION);
    }
        
         private String getRawProjectNameParameter() {
        return request.getParameter(ProjectConversionConstants.PROJECT_NAME_PARAMETER);
    }
}
