package edu.stanford.bmir.protege.web.server.projectimport;

import edu.stanford.bmir.protege.web.client.rpc.data.DocumentId;
import edu.stanford.bmir.protege.web.server.owlapi.OWLAPIProject;
import edu.stanford.bmir.protege.web.server.owlapi.OWLAPIProjectCache;
import edu.stanford.bmir.protege.web.client.rpc.data.NewProjectSettings;
import edu.stanford.bmir.protege.web.client.rpc.data.ProjectType;
import edu.stanford.bmir.protege.web.server.MetaProjectManager;
import edu.stanford.bmir.protege.web.shared.user.UserId;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 * Servlet implementation class TrillProjectImportServlet
 * @author RiccardoFeletto
 * Servlet per l'importazione di una ontologia da trill
 * Riceve una richiesta post da trill contenente l'ontologia la salva in un file .owl che successivamente verrà
 * caricato in webprotege sfruttando la servlet per il fileupload, successivamente viene creato un progetto temporaneo,
 * project name e project desc vengono generati usando un timestamp.
 * La servelet risponde con un redirect sulla pagina di edit del progetto appena creato.
 */
public class TrillProjectImportServlet extends HttpServlet {
       
   @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
	String contenuto = request.getParameter("ontology");	//Prelevo l'ontologia ricevuta da trill

	String url = "http://localhost:8080/webprotege-2.5.0/webprotege/submitfile"; //Indirizzo della servlet per l'upload dell'ontologia
	String charset = "UTF-8";
	String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
	String CRLF = "\r\n"; 											// Line separator required by multipart/form-data.
	
	String tDir = System.getProperty("java.io.tmpdir"); //Path della cartella tmp del server
	
	byte[] buffer = new byte[1024];
	
	File tmpZip = File.createTempFile("tmpfilezip", ".zip"); //File zip che contiene l'ontologia
	File tmpOWL = new File(tDir + "/root-ontology.owl");	//File owl che contiene l'ontologia
	
	BufferedWriter bw = new BufferedWriter(new FileWriter(tmpOWL)); //Scrivo il testo dell'ontologia all'interno del file
	bw.write(contenuto);
	bw.close();
	
	//Comprimo l'ontologia nel file zip
	FileOutputStream fos = new FileOutputStream(tmpZip.getPath());
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
	zos.close();
    
	//Creo la connesione per inviare il file tramite post
    URLConnection connection = new URL(url).openConnection();
    connection.setDoOutput(true);
    connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

    try (
    	OutputStream output = connection.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
    ){        
    	//Invio il file zip
        writer.append("--" + boundary).append(CRLF);
        writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + tmpZip.getName() + "\"").append(CRLF);
        writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
        writer.append(CRLF).flush();
        Files.copy(tmpZip.toPath(), output);
        output.flush();
        writer.append(CRLF).flush();
        
        //End of multipart/form-data.
        writer.append("--" + boundary + "--").append(CRLF).flush();
        
        //Prelevo la risposta dell'upload dell'ontologia
        InputStream inn = connection.getInputStream();
        String encoding = connection.getContentEncoding();
        encoding = encoding == null ? "UTF-8" : encoding;
        String body = IOUtils.toString(inn, encoding);
        
        //TimeStamp
        String timeStampName = new SimpleDateFormat("HH.mm.ss").format(new Date());
        String timeStampDesc = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        
        ProjectType prtype = new ProjectType("OWL Project");  			     //Project type
        String paramPrName = "project_tmp_" + timeStampName;			     //Project name
    	String paramPrDesc= "temp project " + timeStampDesc;				 //Project desc
        
        String uploadid = body.split(":")[2];
        uploadid = uploadid.substring(uploadid.indexOf('"')+1, uploadid.lastIndexOf('"')).trim();
        DocumentId docid = new DocumentId(uploadid);		  //Id file ontologia
        
        //Creo il progetto
        //Creo oggetto NewProjectSettings che contiene User proprietario del progetto projectname projectdesc projecttype e id dell'ontologia
        NewProjectSettings newProjectSettings = new NewProjectSettings(UserId.getGuest(),paramPrName,paramPrDesc,prtype,docid);
        //Oggetto usato per recuperare il projetto dal projectsettings
        OWLAPIProjectCache pc = new OWLAPIProjectCache();
        OWLAPIProject pro = pc.getProject(newProjectSettings);
        //Oggetto usato per registrare il progetto
        MetaProjectManager mpm = MetaProjectManager.getManager();
        mpm.registerProject(pro.getProjectId(), newProjectSettings);
        
        //Construisco url per aprire il progetto
        String proid = pro.getProjectId().toString();
        proid = proid.substring(proid.indexOf('{')+1, proid.lastIndexOf('}')).trim();
        String redurl = "http://localhost:8080/webprotege-2.5.0/#Edit:projectId="+proid;
        
        response.sendRedirect(redurl); //Redirect per aprire il progetto
    }
    catch (Exception e) {
		//TODO: Log!
	}
   }
}  
   
