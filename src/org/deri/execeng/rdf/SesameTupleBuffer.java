package org.deri.execeng.rdf;
import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.deri.execeng.core.BoxParser;
import org.deri.execeng.core.ExecBuffer;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.repository.RepositoryException;
import org.w3c.dom.Element;
import org.deri.execeng.model.Stream;
import org.deri.execeng.model.Box;
public class SesameTupleBuffer extends org.deri.execeng.core.ExecBuffer{
	private TupleQueryResult buffer=null;
	
	public SesameTupleBuffer(){		
	}
	
	public SesameTupleBuffer(String url){
		loadFromURL(url);
	}
	
	public SesameTupleBuffer(TupleQueryResult buffer){
		this.buffer=buffer;
	}
	
	public TupleQueryResult getTupleQueryResult(){
		return buffer;
	}
	
	public void loadFromURL(String url){
		if (url==null) buffer=null;
		try{
	    	HttpURLConnection urlConn=(HttpURLConnection)((new URL(url.trim())).openConnection());
			urlConn.setRequestProperty("Accept", "application/sparql-results+xml");
			urlConn.connect();
		    buffer=QueryResultIO.parse(urlConn.getInputStream(), TupleQueryResultFormat.SPARQL);
	    }
    	catch(org.openrdf.query.resultio.QueryResultParseException e){							
		}
		catch(org.openrdf.query.TupleQueryResultHandlerException e){							
		}
		catch(org.openrdf.query.resultio.UnsupportedQueryResultFormatException e){							
		}
		catch (java.io.IOException e) {
			ExecBuffer.log.append(e.toString()+"\n");
		}
	}
	
	public void loadFromText(String text){
		if (text==null) buffer=null;
		try{
			buffer=QueryResultIO.parse(new ByteArrayInputStream(text.trim().getBytes()), TupleQueryResultFormat.SPARQL);
		}
		catch(java.io.IOException e){	
		}
		catch(org.openrdf.query.resultio.QueryResultParseException e){							
		}
		catch(org.openrdf.query.TupleQueryResultHandlerException e){							
		}
		catch(org.openrdf.query.resultio.UnsupportedQueryResultFormatException e){							
		}
	}
	
	public void loadFromSelect(Element element){	
		SesameMemoryBuffer tmpBuffer=new SesameMemoryBuffer();
		java.util.ArrayList<Element> sources=BoxParser.getSubElementByName(element, "source");
    	String query=BoxParser.getTextFromFirstSubEleByName(element, "query");
    	for(int i=0;i<sources.size();i++){
    		Element tmpEle=BoxParser.getFirstSubElement((Element)(sources.get(i)));
    		Stream tmpStream=null;
 			if(tmpEle==null)
 				tmpStream= new TextBox(BoxParser.getTextData(sources.get(i)));
 			else
 				tmpStream=BoxParserImplRDF.loadStream(tmpEle);  	
     	   if(tmpStream instanceof Box) 
 			 if(!((Box)tmpStream).isExecuted()) ((Box)tmpStream).execute();
     	   if(sources.get(i).getAttribute("uri")==null)
     		   tmpStream.streamming(tmpBuffer);
     	   else{
     		   if(sources.get(i).getAttribute("uri").trim().length()>0)
     		   ((Box)tmpStream).streamming(tmpBuffer,sources.get(i).getAttribute("uri"));
     		   else tmpStream.streamming(tmpBuffer);
     	   }
        }
    	try{
    		buffer=((tmpBuffer.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query)).evaluate());
    	}
         catch(MalformedQueryException e){ 
      	   log.append(e.toString()+"\n");
         }
         catch(QueryEvaluationException e){
      	   log.append(e.toString()+"\n");
         }
         catch(RepositoryException e){
      	   log.append(e.toString()+"\n");
         }
	}
	
	public void streamming(org.deri.execeng.core.ExecBuffer outputBuffer){
		
	}
	
	public void streamming(org.deri.execeng.core.ExecBuffer outputBuffer,String context){
		
	}

	public void toOutputStream(java.io.OutputStream output){
		
	}
}
