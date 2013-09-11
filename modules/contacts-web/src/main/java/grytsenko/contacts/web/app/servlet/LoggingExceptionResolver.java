package grytsenko.contacts.web.app.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

/**
 * This resolver additionally performs logging.
 */
public class LoggingExceptionResolver extends SimpleMappingExceptionResolver {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(LoggingExceptionResolver.class);

    @Override
    public ModelAndView resolveException(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception exception) {
        LOGGER.error("Internal error.", exception);

        return super.resolveException(request, response, handler, exception);
    }

}
