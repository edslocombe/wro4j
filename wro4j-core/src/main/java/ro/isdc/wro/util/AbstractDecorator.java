package ro.isdc.wro.util;

import org.apache.commons.lang3.Validate;


/**
 * Templated decorator.
 * 
 * @author Alex Objelean
 * @created 25 Apr 2012
 * @since 1.4.6
 */
public abstract class AbstractDecorator<T> implements ObjectDecorator<T> {
  private T decorated;
  
  public AbstractDecorator(final T decorated) {
    Validate.notNull(decorated);
    this.decorated = decorated;
  }

  /**
   * @return the decorated object.
   */
  public final T getDecoratedObject() {
    return decorated;
  }
  
  /**
   * @return the object which is was originally decorated and is not a decorator itself.
   */
  public final T getOriginalDecoratedObject() {
    return getOriginalDecoratedObject(decorated);
  }

  /**
   * @return the object which is was originally decorated and is not a decorator itself.
   */
  @SuppressWarnings("unchecked")
  public static <T> T getOriginalDecoratedObject(final T object) {
    return (object instanceof ObjectDecorator) ? ((ObjectDecorator<T>) object).getOriginalDecoratedObject() : object;
  }
}
