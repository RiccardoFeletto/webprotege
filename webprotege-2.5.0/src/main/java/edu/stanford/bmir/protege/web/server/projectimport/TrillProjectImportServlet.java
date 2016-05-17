package edu.stanford.bmir.protege.web.server.projectimport;

import edu.stanford.bmir.protege.web.client.rpc.data.DocumentId;
import edu.stanford.bmir.protege.web.client.rpc.data.NewProjectSettings;
import edu.stanford.bmir.protege.web.client.rpc.data.NotSignedInException;
import edu.stanford.bmir.protege.web.client.rpc.data.ProjectType;
import edu.stanford.bmir.protege.web.server.MetaProjectManager;
import edu.stanford.bmir.protege.web.server.ProjectManagerServiceImpl;
import edu.stanford.bmir.protege.web.server.owlapi.OWLAPIProject;
import edu.stanford.bmir.protege.web.server.owlapi.OWLAPIProjectManager;
import edu.stanford.bmir.protege.web.shared.crud.oboid.UserIdRange;
import edu.stanford.bmir.protege.web.shared.project.ProjectAlreadyRegisteredException;
import edu.stanford.bmir.protege.web.shared.project.ProjectDetails;
import edu.stanford.bmir.protege.web.shared.project.ProjectDocumentExistsException;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.user.UserId;
import edu.stanford.smi.protege.server.metaproject.ProjectInstance;
import edu.stanford.bmir.protege.web.client.rpc.ProjectManagerService;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gwt.core.client.GWT;

/**
 * Servlet implementation class TrillProjectImportServlet
 */
public class TrillProjectImportServlet extends HttpServlet {
       
   @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	String contenuto = request.getParameter("ontology");

	String url = "http://localhost:8080/webprotege-2.5.0/webprotege/submitfile";
	String charset = "UTF-8";
	String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
	String CRLF = "\r\n"; // Line separator required by multipart/form-data.
	
	String paramPrName = "progettoprova";
	String paramPrDesc= "progetto prova descrizione";
	String paramPrType = "OWL Project";
	
	byte[] buffer = new byte[1024];
	
	String tDir = System.getProperty("java.io.tmpdir");
	
	File tmpZip = File.createTempFile("tmpfilezip", ".zip");
	File tmpOWL = new File(tDir + "/root-ontology.owl");
	
	BufferedWriter bw = new BufferedWriter(new FileWriter(tmpOWL));
	bw.write(contenuto);
	bw.close();
	
	FileOutputStream fos = new FileOutputStream(tmpZip.getPath());
    //ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zos = new ZipOutputStream(fos);
    
    ZipEntry entry = new ZipEntry(tmpOWL.getName());
    zos.putNextEntry(entry);
    FileInputStream in = new FileInputStream(tmpOWL.getPath());
   
    int len;
	while ((len = in.read(buffer)) > 0) {
		zos.write(buffer, 0, len);
	}

	in.close();
	zos.closeEntry();
   
	//remember close it
	zos.close();
    
    URLConnection connection = new URL(url).openConnection();
    connection.setDoOutput(true);
    connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

    try (
    	OutputStream output = connection.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
    ){
    	// Send normal param.
        writer.append("--" + boundary).append(CRLF);
        writer.append("Content-Disposition: form-data; name=\"projectname\"").append(CRLF);
        writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
        writer.append(CRLF).append(paramPrName).append(CRLF).flush();
        
        writer.append("--" + boundary).append(CRLF);
        writer.append("Content-Disposition: form-data; name=\"projectdescription\"").append(CRLF);
        writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(tmpZip.getName())).append(CRLF);
        writer.append("Content-Transfer-Encoding: binary").append(CRLF);
        
        writer.append("--" + boundary).append(CRLF);
        writer.append("Content-Disposition: form-data; name=\"projecttype\"").append(CRLF);
        writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
        writer.append(CRLF).append(paramPrType).append(CRLF).flush();
        
    	//Send zip file.
        writer.append("--" + boundary).append(CRLF);
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + tmpZip.getName() + "\"").append(CRLF);
        writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF); // Text file itself must be saved in this charset!
        writer.append(CRLF).flush();
        Files.copy(tmpZip.toPath(), output);
        output.flush(); // Important before continuing with writer!
        writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
        
        //End of multipart/form-data.
        writer.append("--" + boundary + "--").append(CRLF).flush();
        
        /*response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><head><title>prova</title></head><body>");
        out.println(tmpZip.toPath()+"<br>");
        out.println(((HttpURLConnection) connection).getResponseMessage());
        out.println("<br></body></html>");*/
        
        ProjectType prtype = new ProjectType(paramPrType);
        
        NewProjectSettings newProjectSettings = new NewProjectSettings(UserId.getGuest(),paramPrName,paramPrDesc,prtype);
        
        OWLAPIProjectManager pm = null;
        
        pm.createNewProject(newProjectSettings);
    }
   }
}  
   
