/*
 * Original author: Ed Slocombe (EJBSlocombe)
 * Created on: 24/07/12 - 15:44
 */

package ro.isdc.wro.extensions.processor.js;

import org.apache.commons.io.IOUtils;
import ro.isdc.wro.extensions.processor.support.ObjectPoolHelper;
import ro.isdc.wro.extensions.processor.support.jsdoctoolkit.JsDocGenerateInvoker;
import ro.isdc.wro.extensions.processor.support.jsdoctoolkit.JsDocToolkitConfig;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.ObjectFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * JsDocToolkitProcessor
 * <p/>
 * A processor that will generate JS documentation.
 * <p/>
 * The default behaviour is to log the output (html), this is expected
 * to be changed via the configuration object.
 *
 * @author Ed Slocombe
 */
@SupportedResourceType(ResourceType.JS)
public class JsDocToolkitProcessor implements ResourcePreProcessor, ResourcePostProcessor
{
	private final ObjectPoolHelper<JsDocGenerateInvoker> enginePool;


	/**
	 *
	 */
	public JsDocToolkitProcessor(final JsDocToolkitConfig config)
	{
		enginePool = new ObjectPoolHelper<JsDocGenerateInvoker>(new ObjectFactory<JsDocGenerateInvoker>()
		{
			@Override
			public JsDocGenerateInvoker create()
			{
				return new JsDocGenerateInvoker(config);
			}
		});
	}


	/**
	 * @inheritDoc
	 */
	@Override
	public void process(Reader reader, Writer writer) throws IOException
	{
		process(null, reader, writer);
	}


	/**
	 * @inheritDoc
	 */
	@Override
	public void process(Resource resource, Reader reader, Writer writer) throws IOException
	{
		String content = IOUtils.toString(reader);
		JsDocGenerateInvoker invoker = enginePool.getObject();

		try
		{
			invoker.generateJsDoc(content);
		}
		finally
		{
			writer.write(content);
			reader.close();
			writer.close();

			enginePool.returnObject(invoker);
		}
	}
}
