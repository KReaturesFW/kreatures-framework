package com.github.kreatures.core.serialize;

import java.io.File;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Resolve;

import com.github.kreatures.core.asml.ASMLCommand;
import com.github.kreatures.core.asml.CommandSequence;

@Root(name="script-import")
public class CommandSequenceSerializeImport implements CommandSequenceSerialize{

	@Attribute(name="source")
	protected File source;

	@Resolve
	public CommandSequenceSerialize substitute() throws Exception {
		return SerializeHelper.get().loadXmlTry(CommandSequence.class, source);
	}
	
	@Override
	public List<ASMLCommand> getCommandSequence() {
		throw new IllegalStateException("Method not supported.");
	}

	@Override
	public String getName() {
		throw new IllegalStateException("Method not supported.");
	}
}
