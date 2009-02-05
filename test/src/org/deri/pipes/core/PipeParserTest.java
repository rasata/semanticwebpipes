package org.deri.pipes.core;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HttpClient;
import org.deri.pipes.core.internals.SourceConverter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamer;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;

public class PipeParserTest extends TestCase {
	public void testXStreamParser() throws Exception{
		Engine env = Engine.defaultEngine();
		System.out.println(env.getPipeParser().serializeToXML(SourceConverter.ALIAS_MAPPINGS));
		if(false){
		testSource(env, "pipe5.xml");
//		if(true){
		testSource(env, "pipe2.xml");
		testSource(env, "pipe3.xml");
		testSource(env, "pipe4.xml");
//		}
		testSource(env, "pipe5.xml");
		}
	}



	private void testSource(Engine engine, String controlXml) throws Exception{
		InputStream in = getClass().getResourceAsStream(controlXml);
		ProcessingPipe pipe = (ProcessingPipe) engine.parse(in);
		PipeContext context = engine.newContext();
		long time = timedExecute(pipe, context);
		long repeat = timedExecute(pipe, context);
		System.out.println("timing was original:"+time+", repeat:"+repeat);
		
	}



	private long timedExecute(ProcessingPipe pipe, PipeContext context)
			throws Exception {
		long start = System.currentTimeMillis();
		ExecBuffer result = pipe.execute(context);
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		result.stream(bout);
		System.out.println("output: "+bout.toString("UTF-8"));
		return System.currentTimeMillis() -start;
	}
	
}
