package ro.isdc.wro.model.group.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.locator.factory.ResourceLocatorFactory;
import ro.isdc.wro.model.resource.processor.ProcessorsUtils;
import ro.isdc.wro.model.resource.processor.ResourceProcessor;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.util.StopWatch;
import ro.isdc.wro.util.WroUtil;


/**
 * TODO: refactor this class. Apply all preProcessor on provided {@link Resource} and returns the result of execution as
 * String.
 * <p>
 * This is useful when you want to preProcess a resource which is not a part of the model (css import use-case).
 * 
 * @author Alex Objelean
 */
public class PreProcessorExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(PreProcessorExecutor.class);
  @Inject
  private LifecycleCallbackRegistry callbackRegistry;
  @Inject
  private ResourceLocatorFactory resourceLocatorFactory;
  @Inject
  private ProcessorsFactory processorsFactory;
  private ExecutorService executor;
  
  /**
   * Apply preProcessors on resources and merge them.
   *
   * @param resources
   *          what are the resources to merge.
   * @param minimize
   *          whether minimize aware processors must be applied or not.
   * @return preProcessed merged content.
   * @throws IOException
   *           if IO error occurs while merging.
   */
  public String processAndMerge(final Group group, final boolean minimize)
      throws IOException {
    Validate.notNull(group);
    final List<Resource> resources = group.getResources();
    
    final StringBuffer result = new StringBuffer();
    if (shouldRunInParallel(resources)) {
      result.append(runInParallel(resources, minimize));
    } else {
      for (final Resource resource : resources) {
        LOG.debug("\tmerging resource: {}", resource);
        result.append(applyPreProcessors(resource, minimize));
      }
    }
    return result.toString();
  }
  
  private boolean shouldRunInParallel(final List<Resource> resources) {
    final boolean isParallel = Context.get().getConfig().isParallelPreprocessing();
    final int availableProcessors = Runtime.getRuntime().availableProcessors();
    return isParallel && resources.size() > 1 && availableProcessors > 1;
  }
  
  /**
   * runs the pre processors in parallel.
   *
   * @return merged and pre processed content.
   */
  private String runInParallel(final List<Resource> resources, final boolean minimize)
      throws IOException {
    final StringBuffer result = new StringBuffer();
    final List<Callable<String>> callables = new ArrayList<Callable<String>>();
    for (final Resource resource : resources) {
      callables.add(new Callable<String>() {
        public String call()
            throws Exception {
          return applyPreProcessors(resource, minimize);
        }
      });
    }
    final ExecutorService exec = getExecutorService();
    final List<Future<String>> futures = new ArrayList<Future<String>>();
    for (final Callable<String> callable : callables) {
      futures.add(exec.submit(callable));
    }

    for (final Future<String> future : futures) {
      try {
        result.append(future.get());
      } catch (final Exception e) {
        // propagate original cause
        final Throwable cause = e.getCause();
        if (cause instanceof WroRuntimeException) {
          throw (WroRuntimeException) cause;
        } else if (cause instanceof IOException) {
          throw (IOException) cause;
        } else {
          throw new WroRuntimeException("Problem during parallel pre processing", e.getCause());
        }
      }
    }
    return result.toString();
  }
  
  private ExecutorService getExecutorService() {
    if (executor == null) {
      // use at most the number of available processors (true parallelism)
      final int threadPoolSize = Runtime.getRuntime().availableProcessors();
      executor = Executors.newFixedThreadPool(threadPoolSize,
          WroUtil.createDaemonThreadFactory("parallelPreprocessing"));
    }
    return executor;
  }
  
  /**
   * Apply a list of preprocessors on a resource.
   *
   * @param resource the {@link Resource} on which processors will be applied
   * @param processors the list of processor to apply on the resource.
   */
  private String applyPreProcessors(final Resource resource, final boolean minimize)
    throws IOException {
    // merge preProcessorsBy type and anyPreProcessors
    final Collection<ResourceProcessor> processors = ProcessorsUtils.filterProcessorsToApply(minimize,
      resource.getType(), processorsFactory.getPreProcessors());
    LOG.debug("applying preProcessors: {}", processors);
    String resourceContent = getResourceContent(resource);
    if (processors.isEmpty()) {
      return resourceContent;
    }
    Writer writer = null;
    final StopWatch stopWatch = new StopWatch();
    for (final ResourceProcessor processor : processors) {
      stopWatch.start("Processor: " + processor.getClass().getSimpleName());
      writer = new StringWriter();
      final Reader reader = new StringReader(resourceContent);
      decorateWithPreProcessCallback(decorateWithMinimizeAware(processor)).process(resource, reader, writer);
      resourceContent = writer.toString();
      stopWatch.stop();
    }
    LOG.debug(stopWatch.prettyPrint());
    return writer.toString();
  }
  
  /**
   * @return a Reader for the provided resource.
   * @param resource
   *          {@link Resource} which content to return.
   * @param resources
   *          the list of all resources processed in this context, used for duplicate resource detection.
   */
  private String getResourceContent(final Resource resource)
      throws IOException {
    final WroConfiguration config = Context.get().getConfig();
    try {
      final InputStream is = new BOMInputStream(resourceLocatorFactory.locate(resource.getUri()).getInputStream());
      final String result = IOUtils.toString(is, config.getEncoding());
      is.close();
      return result;
    } catch (final IOException e) {
      LOG.warn("Invalid resource found: " + resource);
      if (config.isIgnoreMissingResources()) {
        return StringUtils.EMPTY;
      } else {
        LOG.error("Cannot ignore the missing resource:  " + resource);
        throw e;
      }
    }
  }
  
  /**
   * @return a decorated preProcessor which invokes callback methods.
   */
  private ResourceProcessor decorateWithPreProcessCallback(final ResourceProcessor processor) {
    return new ResourceProcessor() {
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        callbackRegistry.onBeforePreProcess();
        try {
          processor.process(resource, reader, writer);
        } finally {
          callbackRegistry.onAfterPreProcess();
        }
      }
    };
  }
  
  /**
   * The decorated processor will skip processing if the processor has @Minimize annotation and resource being processed
   * doesn't require the minimization.
   */
  private ResourceProcessor decorateWithMinimizeAware(final ResourceProcessor processor) {
    return new ResourceProcessor() {
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        final boolean applyProcessor = resource.isMinimize()
            || !processor.getClass().isAnnotationPresent(Minimize.class);
        if (applyProcessor) {
          LOG.debug("\tUsing Processor: {}", processor.getClass().getSimpleName());
          try {
            processor.process(resource, reader, writer);
          } catch (final IOException e) {
            if (!Context.get().getConfig().isIgnoreMissingResources()) {
              throw e;
            }
          }
        } else {
          IOUtils.copy(reader, writer);
          LOG.debug("skipped processing on resource: {}", resource);
        }
      }
    };
  }
}