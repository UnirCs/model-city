package com.modelcity.core.utils.mail;

/** Generic strategy for rendering an email body as HTML from a context object. */
public interface MailTemplate<T> {

    /** Returns the HTML body with all placeholders replaced. */
    String render(T context);
}
