package org.jasig.cas.web.flow;

import org.jasig.cas.authentication.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.execution.RequestContext;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 自定义异常处理Handler 统计错误次数
 * Created by Administrator on 2016/4/27 0027.
 */
public class MyAuthenticationExceptionHandler {

    /** State name when no matching exception is found. */
    private static final String UNKNOWN = "UNKNOWN";

    /** Default message bundle prefix. */
    private static final String DEFAULT_MESSAGE_BUNDLE_PREFIX = "authenticationFailure.";

    /** Default list of errors this class knows how to handle. */
    private static final List<Class<? extends Exception>> DEFAULT_ERROR_LIST =
            new ArrayList<Class<? extends Exception>>();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    static {
        DEFAULT_ERROR_LIST.add(javax.security.auth.login.AccountLockedException.class);
        DEFAULT_ERROR_LIST.add(javax.security.auth.login.FailedLoginException.class);
        DEFAULT_ERROR_LIST.add(javax.security.auth.login.CredentialExpiredException.class);
        DEFAULT_ERROR_LIST.add(javax.security.auth.login.AccountNotFoundException.class);
        DEFAULT_ERROR_LIST.add(org.jasig.cas.authentication.AccountDisabledException.class);
        DEFAULT_ERROR_LIST.add(org.jasig.cas.authentication.InvalidLoginLocationException.class);
        DEFAULT_ERROR_LIST.add(org.jasig.cas.authentication.InvalidLoginTimeException.class);
    }

    /** Ordered list of error classes that this class knows how to handle. */
    @NotNull
    private List<Class<? extends Exception>> errors = DEFAULT_ERROR_LIST;

    /** String appended to exception class name to create a message bundle key for that particular error. */
    private String messageBundlePrefix = DEFAULT_MESSAGE_BUNDLE_PREFIX;

    /**
     * Sets the list of errors that this class knows how to handle.
     *
     * @param errors List of errors in order of descending precedence.
     */
    public void setErrors(final List<Class<? extends Exception>> errors) {
        this.errors = errors;
    }

    public final List<Class<? extends Exception>> getErrors() {
        return Collections.unmodifiableList(this.errors);
    }

    /**
     * Sets the message bundle prefix appended to exception class names to create a message bundle key for that
     * particular error.
     *
     * @param prefix Prefix appended to exception names.
     */
    public void setMessageBundlePrefix(final String prefix) {
        this.messageBundlePrefix = prefix;
    }

    /**
     * Maps an authentication exception onto a state name equal to the simple class name of the
     * {@link org.jasig.cas.authentication.AuthenticationException#getHandlerErrors()} with highest precedence.
     * Also sets an ERROR severity message in the message context of the form
     * <code>[messageBundlePrefix][exceptionClassSimpleName]</code> for each handler error that is
     * configured. If not match is found, {@value #UNKNOWN} is returned.
     *
     * @param e Authentication error to handle.
     *
     * @return Name of next flow state to transition to or {@value #UNKNOWN}
     */
    public String handle(final RequestContext context, final AuthenticationException e, final MessageContext messageContext) {
        int count;
        try {
            count = (Integer) context.getFlowScope().get("count");
        } catch (Exception ex) {
            count = 0;
        }
        if (e != null) {
            for (final Class<? extends Exception> kind : this.errors) {
                for (final Class<? extends Exception> handlerError : e.getHandlerErrors().values()) {
                    if (handlerError != null && handlerError.equals(kind)) {
                        final String messageCode = this.messageBundlePrefix + handlerError.getSimpleName();
                        messageContext.addMessage(new MessageBuilder().error().code(messageCode).build());
                        count++;
                        context.getFlowScope().put("count", count);
                        return handlerError.getSimpleName();
                    }
                }
            }
        }
        final String messageCode = this.messageBundlePrefix + UNKNOWN;
        logger.trace("Unable to translate handler errors of the authentication exception {}. Returning {} by default...", e, messageCode);
        messageContext.addMessage(new MessageBuilder().error().code(messageCode).build());
        count++;
        context.getFlowScope().put("count", count);
        return UNKNOWN;
    }

}
