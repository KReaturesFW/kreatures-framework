package com.github.kreatures.core.reflection;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.simpleframework.xml.transform.RegistryMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kreatures.core.asml.Assign;
import com.github.kreatures.core.asml.While;
import com.github.kreatures.core.def.FrameworkPlugin;
import com.github.kreatures.core.serialize.SerializeHelper;

public class WhileTest {
	
	private static Logger LOG = LoggerFactory.getLogger(WhileTest.class);
	private While loop;
	private FrameworkPlugin fp = new FrameworkPlugin();
	@Before
	public void setUp() {
		fp.onLoading();
		
		// load file containing the following code:
		// if: left < right: result=1
		// else if: left == right: result=2
		// else: result=3

		String jarPath = "/com/github/kreatures/core/reflection/WhileTest.xml";
		InputStream stream = getClass().getResourceAsStream(jarPath);
		if(stream == null) {
			LOG.warn("Cannot find: '{}'", jarPath);
			return ;
		}
		loop = SerializeHelper.get().loadXmlTry(While.class, new InputStreamReader(stream));
	}
	@After
	public void iniDeserializer(){
		fp.unUnloaded();
		fp=null;
		loop=null;
	}
	
	@Test
	public void testManualWhile() throws ClassNotFoundException {
		Context context = new Context();
		context.set("run", true);
		
		While w = new While(new BooleanExpression(new Value("$run", Value.CONTEXT_REFERENCE_TYPE)));
		Assign assign = new Assign("run", new Value("FALSE", Boolean.class.getName()));
		w.addCommando(assign);
		assign = new Assign("sender", new Value("Boss"));
		w.addCommando(assign);
		w.execute(context);
		
		assertEquals(1, w.getIterations());
		assertEquals("Boss", context.get("sender"));
	}
	
	@Test
	public void testWhileXML() {
		Context context = new Context();
		
		loop.execute(context);
		assertEquals("Blub", context.get("counter"));
		assertEquals(1, loop.getIterations());
	}
}
