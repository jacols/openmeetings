/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License") +  you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.openmeetings.service.mail.template;

import static org.apache.openmeetings.util.OpenmeetingsVariables.CONFIG_DEFAUT_LANG_KEY;
import static org.apache.openmeetings.util.OpenmeetingsVariables.wicketApplicationName;

import org.apache.openmeetings.core.IApplication;
import org.apache.openmeetings.core.IWebSession;
import org.apache.openmeetings.db.dao.basic.ConfigurationDao;
import org.apache.openmeetings.db.dao.label.FieldLanguageDao;
import org.apache.openmeetings.db.entity.label.FieldLanguage;
import org.apache.wicket.Application;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.mock.MockWebResponse;
import org.apache.wicket.protocol.http.BufferedWebResponse;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.protocol.http.mock.MockHttpSession;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.cycle.RequestCycleContext;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;

public abstract class AbstractTemplatePanel extends Panel {
	private static final long serialVersionUID = 1L;
	protected long langId;
	
	public static IApplication getApp() {
		return (IApplication)Application.get(wicketApplicationName);
	}
	
	public static <T> T getBean(Class<T> clazz) {
		return getApp().getOmBean(clazz);
	}
	
	public static IWebSession getOmSession() {
		return (IWebSession)WebSession.get();
	}
	
	public AbstractTemplatePanel(Long langId) {
		super(TemplatePage.COMP_ID);
		this.langId = langId == null ? getBean(ConfigurationDao.class).getConfValue(CONFIG_DEFAUT_LANG_KEY, Long.class, "1") : langId;
		FieldLanguage lang = getBean(FieldLanguageDao.class).get(langId);
		add(new TransparentWebMarkupContainer("container").add(AttributeAppender.append("dir", lang.isRtl() ? "rtl" : "ltr")));
	}
	
	public static String getString(long id, long languageId) {
		return getOmSession().getOmString(id, languageId);
	}

	/**
	 * Collects the html generated by the rendering of a page.
	 * 
	 * @param panel
	 *            the panel that should be rendered.
	 * @return the html rendered by the panel
	 */
	protected static CharSequence renderPanel(final Panel panel) {
		RequestCycle requestCycle = RequestCycle.get();

		final Response oldResponse = requestCycle.getResponse();
		BufferedWebResponse tempResponse = new BufferedWebResponse(null);

		try {
			requestCycle.setResponse(tempResponse);

			TemplatePage page = new TemplatePage();
			page.add(panel);

			panel.render();
		} finally {
			requestCycle.setResponse(oldResponse);
		}

		return tempResponse.getText();
	}

	public static class TemplatePage extends WebPage implements IMarkupResourceStreamProvider {
		private static final long serialVersionUID = 1L;
		public static final String COMP_ID = "template";

		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
			return new StringResourceStream("<wicket:container wicket:id='" + COMP_ID + "'></wicket:container>");
		}
	}
	
	public static void ensureApplication(long langId) {
		if (!Application.exists()) {
			Application app = Application.get(wicketApplicationName);
			ThreadContext.setApplication(app);
			IApplication a = (IApplication)app;
			
			ServletWebRequest req = new ServletWebRequest(new MockHttpServletRequest(app, new MockHttpSession(a.getServletContext()), a.getServletContext()), "");
			RequestCycleContext rctx = new RequestCycleContext(req, new MockWebResponse(), a.getRootRequestMapper(), a.getExceptionMapperProvider().get()); 
			ThreadContext.setRequestCycle(new RequestCycle(rctx));
			
			WebSession session = WebSession.get();
			((IWebSession)session).setLanguage(langId);
			ThreadContext.setSession(session);
		}
	}
}
