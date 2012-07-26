/*
 * Original author: Ed Slocombe (EJBSlocombe)
 * Created on: 24/07/12 - 15:44
 */

package ro.isdc.wro.extensions.processor.js;

import org.apache.commons.io.IOUtils;
import ro.isdc.wro.extensions.processor.support.jsdoctoolkit.JsDocToolkitConfig;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * JsDocToolkitProcessor
 *
 * A processor that will generate JS documentation.
 * <p/>
 * The default behaviour is to log the output (html), the intention is
 * that this class is extended to perform something more useful.
 *
 * @author Ed Slocombe
 */
@SupportedResourceType(ResourceType.JS)
public class JsDocToolkitProcessor implements ResourcePreProcessor, ResourcePostProcessor
{
	private JsDocToolkitConfig config;


	/**
	 * @inheritDoc
	 */
	@Override
	public void process(Reader reader, Writer writer) throws IOException
	{

	}


	/**
	 * @inheritDoc
	 */
	@Override
	public void process(Resource resource, Reader reader, Writer writer) throws IOException
	{

	}


	protected void process(Reader reader) throws IOException
	{
		String jsContent = IOUtils.toString(reader);

	}
}
