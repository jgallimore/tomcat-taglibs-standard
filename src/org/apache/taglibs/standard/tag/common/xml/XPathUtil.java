/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.taglibs.standard.tag.common.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.jaxen.Context;
import org.jaxen.ContextSupport;
import org.jaxen.FunctionContext;
import org.jaxen.SimpleNamespaceContext;
import org.jaxen.UnresolvableException;
import org.jaxen.VariableContext;
import org.jaxen.XPath;
import org.jaxen.XPathFunctionContext;
import org.jaxen.dom.DOMXPath;
import org.jaxen.dom.DocumentNavigator;
import org.saxpath.SAXPathException;
import org.w3c.dom.Node;

/**
 * <p>Support for tag handlers that evaluate XPath expressions.</p>
 *
 * @author Shawn Bayern
 */
// would ideally be a base class, but some of our user handlers already
// have their own parents
public class XPathUtil {

    //*********************************************************************
    // Constructor

    /**
     * Constructs a new XPathUtil object associated with the given
     * PageContext.
     */
    public XPathUtil(PageContext pc) {
	pageContext = pc;
    }

    //*********************************************************************
    // Support for JSTL variable resolution

    private static final String PAGE_NS_URL
	= "http://java.sun.com/jstl/xpath/page";
    private static final String REQUEST_NS_URL
	= "http://java.sun.com/jstl/xpath/request";
    private static final String SESSION_NS_URL
	= "http://java.sun.com/jstl/xpath/session";
    private static final String APP_NS_URL
	= "http://java.sun.com/jstl/xpath/app";
    private static final String PARAM_NS_URL
	= "http://java.sun.com/jstl/xpath/param";
    private static final String INITPARAM_NS_URL
	= "http://java.sun.com/jstl/xpath/initParam";
    private static final String COOKIE_NS_URL
	= "http://java.sun.com/jstl/xpath/cookie";
    private static final String HEADER_NS_URL
	= "http://java.sun.com/jstl/xpath/header";

    private static final String PAGE_P = "pageScope";
    private static final String REQUEST_P = "requestScope";
    private static final String SESSION_P = "sessionScope";
    private static final String APP_P = "applicationScope";
    private static final String PARAM_P = "param";
    private static final String INITPARAM_P = "initParam";
    private static final String COOKIE_P = "cookie";
    private static final String HEADER_P = "header";

    protected class JstlVariableContext implements VariableContext {
	// retrieves object using JSTL's custom variable-mapping rules
        public Object getVariableValue(String namespace, String prefix,
                String localName) throws UnresolvableException {
	    // I'd prefer to match on namespace, but this doesn't appear
            // to work in Jaxen
	    if (prefix == null || prefix.equals("")) {
		return notNull(
		    pageContext.findAttribute(localName),
		    prefix,
		    localName);
	    } else if (prefix.equals(PAGE_P)) {
		return notNull(
		    pageContext.getAttribute(localName,PageContext.PAGE_SCOPE),
		    prefix,
		    localName);
	    } else if (prefix.equals(REQUEST_P)) {
		return notNull(
		    pageContext.getAttribute(localName,
			PageContext.REQUEST_SCOPE),
		    prefix,
		    localName);
	    } else if (prefix.equals(SESSION_P)) {
		return notNull(
		    pageContext.getAttribute(localName,
		        PageContext.SESSION_SCOPE),
		    prefix,
		    localName);
	    } else if (prefix.equals(APP_P)) {
		return notNull(
		    pageContext.getAttribute(localName,
		        PageContext.APPLICATION_SCOPE),
		    prefix,
		    localName);
	    } else if (prefix.equals(PARAM_P)) {
		return notNull(
		    pageContext.getRequest().getParameter(localName),
		    prefix,
		    localName);
	    } else if (prefix.equals(INITPARAM_P)) {
		return notNull(
		    pageContext.getServletContext().
		      getInitParameter(localName),
		    prefix,
		    localName);
	    } else if (prefix.equals(HEADER_P)) {
		HttpServletRequest hsr =
		    (HttpServletRequest) pageContext.getRequest();
		return notNull(
		    hsr.getHeader(localName),
		    prefix,
		    localName);
	    } else if (prefix.equals(COOKIE_P)) {
		HttpServletRequest hsr =
		    (HttpServletRequest) pageContext.getRequest();
		Cookie[] c = hsr.getCookies();
		for (int i = 0; i < c.length; i++)
		    if (c[i].getName().equals(localName))
			return c[i].getValue();
		throw new UnresolvableException("$" + prefix + ":" + localName);
	    } else
		throw new UnresolvableException("$" + prefix + ":" + localName);
        }

	private Object notNull(Object o, String prefix, String localName)
	        throws UnresolvableException {
	    if (o == null)
		throw new UnresolvableException("$" + prefix + ":" + localName);
	    return o;
	}
    }

    //*********************************************************************
    // Support for XPath evaluation

    private PageContext pageContext;
    private static SimpleNamespaceContext nc;
    private static FunctionContext fc;
    private static DocumentNavigator dn;
    private static HashMap exprCache;

    /** Initialize globally useful data. */
    private synchronized static void staticInit() {
	if (nc == null) {
	    // register supported namespaces
            nc = new SimpleNamespaceContext();
            SimpleNamespaceContext nc = new SimpleNamespaceContext();
            nc.addNamespace("page", PAGE_NS_URL);
            nc.addNamespace("request", REQUEST_NS_URL);
            nc.addNamespace("session", SESSION_NS_URL);
            nc.addNamespace("application", APP_NS_URL);
            nc.addNamespace("param", PARAM_NS_URL);
            nc.addNamespace("initParam", INITPARAM_NS_URL);
            nc.addNamespace("header", HEADER_NS_URL);
            nc.addNamespace("cookie", COOKIE_NS_URL);

	    // set up the global FunctionContext
	    fc = XPathFunctionContext.getInstance();

	    // set up the global DocumentNavigator
	    dn = DocumentNavigator.getInstance();

            // create a HashMap to cache the expressions
            exprCache = new HashMap();
	}
    }

    /**
     * Returns a String given an XPath expression and a single context
     * Node.
     */
    public String valueOf(Node n, String xpath) throws SAXPathException {
	staticInit();
        XPath xp = new DOMXPath(xpath);
        // return xp.valueOf(getLocalContext(n));
        return xp.stringValueOf(getLocalContext(n));
    }

    /** Evaluates an XPath expression to a boolean value. */
    public boolean booleanValueOf(Node n, String xpath)
	    throws SAXPathException {
	staticInit();
	XPath xp = parse(xpath);
	return xp.booleanValueOf(getLocalContext(n));
    }

    /** Evalutes an XPath expression to a List of nodes. */
    public List selectNodes(Node n, String xpath) throws SAXPathException {
	staticInit();
	XPath xp = parse(xpath);
	return xp.selectNodes(getLocalContext(n));
    }

    /** Evaluates an XPath expression to a single node. */
    public Node selectSingleNode(Node n, String xpath)
	    throws SAXPathException {
	staticInit();
	XPath xp = parse(xpath);
	return (Node) xp.selectSingleNode(getLocalContext(n));
    }

    /** Returns a locally appropriate Jaxen context given a node. */
    private Context getLocalContext(Node n) {
	// set up instance-specific contexts
        VariableContext vc = new JstlVariableContext();
        ContextSupport cs = new ContextSupport(nc, fc, vc, dn);
        Context c = new Context(cs);
        List l = new ArrayList(1);
        l.add(n);
        c.setNodeSet(l);
	return c;
    }

    /**
     * Retrieves a parsed version of the textual XPath expression.
     * The parsed version is retrieved from our static cache if we've
     * seen it previously.
     */
    private XPath parse(String xpath) throws SAXPathException {
        XPath cached = (XPath) exprCache.get(xpath);
        if (cached == null) {
          cached = new DOMXPath(xpath);
          exprCache.put(xpath, cached);
	}
        return cached;
    }

    //*********************************************************************
    // Static support for context retrieval from parent <forEach> tag

    public static Node getContext(Tag t) throws JspTagException {
	ForEachTag xt =
	    (ForEachTag) TagSupport.findAncestorWithClass(
		t, ForEachTag.class);
	if (xt == null)
	    return null;
	else
	    return (xt.getContext());
    }

}
