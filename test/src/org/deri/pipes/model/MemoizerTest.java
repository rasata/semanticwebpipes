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

package org.deri.pipes.model;

import org.deri.pipes.core.Engine;
import org.deri.pipes.core.ExecBuffer;
import org.deri.pipes.core.PipeContext;
import org.deri.pipes.rdf.TextBox;

import junit.framework.TestCase;

/**
 * @author robful
 *
 */
public class MemoizerTest extends TestCase {
	ExecBuffer expected = new SesameMemoryBuffer();
	public void test() throws Exception{
		X x = (X)Memoizer.getMemoizedInstance(X.class);
		x.expected = expected;
		PipeContext context = new PipeContext();
		x.execute(context);
		ExecBuffer result = x.execute(context);
		assertEquals("Wrong number of invocations",1,x.nInvocations);
		assertEquals("Wrong result",expected,result);
	}
	public void testSerialization()throws Exception{
		Engine engine = Engine.defaultEngine();
		TextBox t = new TextBox();
		String content = "foo bar";
		t.setContent(content);
		TextBox text = (TextBox)Memoizer.getMemoizedInstance(TextBox.class);
		text.setContent(content);
//		text.execute(context);
		String s = engine.serialize(text);
		String expected = engine.serialize(t);
		assertEquals("Wrong serialization",expected,s);
	}
	static class X implements Operator{
		volatile int nInvocations = 0;
		ExecBuffer expected;
		public ExecBuffer execute(PipeContext context) {
			nInvocations++;
			return expected;
		}
		
	}
}
