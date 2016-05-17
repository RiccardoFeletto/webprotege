/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.stanford.bmir.protege.web.client.ui.projectmanager;

import edu.stanford.bmir.protege.web.shared.project.ProjectId;

/**
 *
 * @author antonio
 */
public interface ConvertAndReturnRequestHandler {
    
    void handleProjectConvertRequest(ProjectId projectId);

}
