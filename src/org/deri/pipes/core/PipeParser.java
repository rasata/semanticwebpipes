/*
 * Copyright (c) 2008-2009,
 * 
 * Digital Enterprise Research Institute, National University of Ireland, 
 * Galway, Ireland
 * http://www.deri.org/
 * http://pipes.deri.org/
 *
 * Semantic Web Pipes is distributed under New BSD License.
 * 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  * Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in the 
 *    documentation and/or other materials provided with the distribution and 
 *    reference to the source code.
 *  * The name of Digital Enterprise Research Institute, 
 *    National University of Ireland, Galway, Ireland; 
 *    may not be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
/**
 * 
 */
package org.deri.pipes.core;

import java.io.InputStream;
import java.io.Writer;
import java.util.List;

import org.apache.xerces.parsers.DOMParser;
import org.deri.pipes.core.internals.BypassCGLibConverter;
import org.deri.pipes.core.internals.BypassCGLibMapper;
import org.deri.pipes.core.internals.OperatorMemoizerProvider;
import org.deri.pipes.core.internals.SourceConverter;
import org.deri.pipes.endpoints.PipeManager;
import org.deri.pipes.endpoints.Pipes;
import org.deri.pipes.rdf.SimpleMixBox;
import org.deri.pipes.rdf.TextBox;
import org.deri.pipes.utils.IDTool;
import org.deri.pipes.utils.XMLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.zkoss.util.logging.Log;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.CGLIBEnhancedConverter;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.mapper.CGLIBMapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;
/**
 * @author Danh Le Phuoc, danh.lephuoc@deri.org
 *
 */
public class PipeParser {
	final static Logger logger = LoggerFactory.getLogger(PipeParser.class);
	Context context = new Context();
	private XStream xstream;
	PipeParser(){
		
	}
	
	/**
	 * Parse the xml syntax contained in this String.
	 * @param syntax xml pipe syntax.
	 */
	public Operator parse(String syntax) {
    	return (Operator) getXStreamSerializer().fromXML(syntax);
	}

    
    
/*	public static Operator loadStoredOperator(Element element){
		String tagName = element.getTagName();
		String syntax =PipeManager.getPipeSyntax(tagName);
		if (syntax==null){
			logger.warn("no syntax found for element "+element);
			return null;
		}
		List<Element> parameters =XMLUtil.getSubElement(element);
		for (int i=0;i<parameters.size();i++) {			
			syntax = syntax.replace("${" + parameters.get(i).getTagName() + "}", XMLUtil.getTextData(parameters.get(i)));
		}
		return (new PipeParser()).parse(syntax);
	}
*/	
	
	public String addOperator(Operator operator){
		if(operator==null){
			logger.debug("addOperator invoked with null operator");
			return null;
		}
		String id=generateID();
		context.addOperator(generateID(),operator);
		return id;
	}
	
	private String generateID(){
		return IDTool.generateRandomID("ID");
	}
	

	

	public static String serialize(SimpleMixBox fixture) {
		// TODO Auto-generated method stub
		return null;
	}

	private XStream getXStreamSerializer() {
		if(xstream == null){
		  xstream =  createXStreamSerializer();
		}
		return xstream;
	}

	private XStream createXStreamSerializer() {
		XStream xstream = new XStream(new OperatorMemoizerProvider(),
			    new DomDriver() {
			        public HierarchicalStreamWriter createWriter(Writer out) {
			            return new PrettyPrintWriter(out) {
			                protected void writeText(QuickWriter writer, String text) {
			                	if(text==null || (text.indexOf('&')<0 && text.indexOf('<')<0)){
			                		writer.write(text);
			                	}else{
			                    writer.write("<![CDATA[");
			                    writer.write(text);
			                    writer.write("]]>");
			                	}
			                }
			            };
			        }
			    }
			){
			@Override
			 protected MapperWrapper wrapMapper(MapperWrapper next) {
			        return new BypassCGLibMapper(next);
			    }

		};
		xstream.registerConverter(new BypassCGLibConverter(xstream));
		xstream.alias("pipe",Pipe.class);
		xstream.registerLocalConverter(Pipe.class, "parameters", new ParameterConverter());
		xstream.registerConverter(new SourceConverter());
		//xstream normally uses 'reference' for references, we want refid
		xstream.aliasSystemAttribute("REFID", "reference");
		xstream.aliasSystemAttribute("ID", "id");
		SourceConverter.registerAliases(xstream);
		xstream.autodetectAnnotations(true);
		return xstream;
	}

	/**
	 * @return
	 */
	public Context getPipeContext() {
		return context;
	}

	/**
	 * @param o
	 * @return
	 */
	public String serializeToXML(Object o) {
		return getXStreamSerializer().toXML(o);
	}

	/**
	 * @param in
	 * @return
	 */
	public Object parse(InputStream in) {
		long start = System.currentTimeMillis();
		Object obj = getXStreamSerializer().fromXML(in);
		long elapsed = System.currentTimeMillis()- start;
		logger.info("parsed object from xml in "+elapsed+"ms");
		return obj;
	}
}