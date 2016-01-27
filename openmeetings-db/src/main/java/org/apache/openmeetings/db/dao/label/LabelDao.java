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
package org.apache.openmeetings.db.dao.label;

import static org.apache.openmeetings.util.OpenmeetingsVariables.webAppRootKey;
import static org.apache.openmeetings.util.OpenmeetingsVariables.wicketApplicationName;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.openmeetings.IApplication;
import org.apache.openmeetings.IWebSession;
import org.apache.openmeetings.db.dao.IDataProviderDao;
import org.apache.openmeetings.db.entity.label.StringLabel;
import org.apache.openmeetings.util.OmFileHelper;
import org.apache.openmeetings.util.XmlExport;
import org.apache.wicket.Application;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.mock.MockWebResponse;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.protocol.http.mock.MockHttpSession;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.cycle.RequestCycleContext;
import org.apache.wicket.util.string.Strings;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * 
 * CRUD operations for {@link StringLabel}
 * 
 * @author solomax, swagner
 * 
 */
public class LabelDao implements IDataProviderDao<StringLabel>{
	private static final Logger log = Red5LoggerFactory.getLogger(LabelDao.class, webAppRootKey);
	private static final String ENTRY_ELEMENT = "entry";
	private static final String KEY_ATTR = "key";
	public static final String APP_RESOURCES_EN = "Application.properties.xml";
	public static final String APP_RESOURCES = "Application_%s.properties.xml";
	public static final LinkedHashMap<Long, Locale> languages = new LinkedHashMap<Long, Locale>(); //TODO hide it and return unmodifiable map
	public static final ConcurrentHashMap<Locale, List<StringLabel>> labelCache = new ConcurrentHashMap<Locale, List<StringLabel>>();
	public static final Set<String> keys = new HashSet<String>();
	private static Class<?> APP = null;

	private static void storeLanguages() throws Exception {
		Document d = XmlExport.createDocument();
		Element r = XmlExport.createRoot(d, "language");
		for (Map.Entry<Long, Locale> e : languages.entrySet()) {
			r.addElement("lang").addAttribute("id", "" + e.getKey()).addAttribute("code", e.getValue().toLanguageTag());
		}
		XmlExport.toXml(getLangFile(), d);
	}
	
	public static void add(Locale l) throws Exception {
		Long id = 0L;
		for (Map.Entry<Long, Locale> e : languages.entrySet()) {
			id = e.getKey();
		}
		languages.put(++id,  l);
		storeLanguages();
		labelCache.put(l, new ArrayList<StringLabel>());
	}
	
	public static IApplication getApp(long langId) {
		IApplication a = null;
		if (Application.exists()) {
			a = (IApplication)Application.get();
		} else {
			Application app = Application.get(wicketApplicationName);
			ThreadContext.setApplication(app);
			a = (IApplication)Application.get(wicketApplicationName);
		}
		if (ThreadContext.getRequestCycle() == null) {
			ServletWebRequest req = new ServletWebRequest(new MockHttpServletRequest((Application)a, new MockHttpSession(a.getServletContext()), a.getServletContext()), "");
			RequestCycleContext rctx = new RequestCycleContext(req, new MockWebResponse(), a.getRootRequestMapper(), a.getExceptionMapperProvider().get()); 
			ThreadContext.setRequestCycle(new RequestCycle(rctx));
		}
		if (ThreadContext.getSession() == null) {
			WebSession s = WebSession.get();
			((IWebSession)s).setLanguage(langId);
			ThreadContext.setSession(s);
		}
		return a;
	}
	
	public String getString(long fieldValuesId, long langId) {
		return getApp(langId).getOmString(fieldValuesId, langId);
	}

	public String getString(String key, long langId) {
		return getApp(langId).getOmString(key, langId);
	}

	private static File getLangFile() {
		return new File(OmFileHelper.getLanguagesDir(), OmFileHelper.nameOfLanguageFile);
	}
	
	public static void initLanguageMap() {
		SAXReader reader = new SAXReader();
		try {
			APP = Class.forName("org.apache.openmeetings.web.app.Application"); //FIXME HACK to resolve package dependencies
			Document document = reader.read(getLangFile());
			Element root = document.getRootElement();
			languages.clear();
			for (@SuppressWarnings("unchecked")Iterator<Element> it = root.elementIterator("lang"); it.hasNext();) {
				Element item = it.next();
				Long id = Long.valueOf(item.attributeValue("id"));
				String code = item.attributeValue("code");
				if (id == 3L) {
					continue;
				}
				languages.put(id, Locale.forLanguageTag(code));
			}
		} catch (Exception e) {
			log.error("Error while building language map");
		}
	}

	public static String getLabelFileName(Locale l) {
		String name = APP_RESOURCES_EN;
		if (!Locale.ENGLISH.equals(l)) {
			name = String.format(APP_RESOURCES, l.toLanguageTag().replace('-', '_'));
		}
		return name;
	}
	
	private static void storeLabels(Locale l) throws Exception {
		Document d = XmlExport.createDocument();
		Element r = XmlExport.createRoot(d);
		List<StringLabel> labels = labelCache.get(l);
		for (StringLabel sl : labels) {
			r.addElement(ENTRY_ELEMENT).addAttribute(KEY_ATTR, sl.getKey()).addCDATA(sl.getValue());
		}
		URL u = APP.getResource(getLabelFileName(l));
		XmlExport.toXml(new File(u.toURI()), d);
	}
	
	public static void upload(Locale l, InputStream is) throws Exception {
		List<StringLabel> labels = getLabels(l, is);
		URL u = APP.getResource(getLabelFileName(Locale.ENGLISH)); //get the URL of existing resource
		File el = new File(u.toURI());
		File f = new File(el.getParentFile(), getLabelFileName(l));
		if (!f.exists()) {
			f.createNewFile();
		}
		labelCache.put(l, labels);
		storeLabels(l);
	}
	
	private static List<StringLabel> getLabels(Locale l) {
		List<StringLabel> labels = new ArrayList<StringLabel>();
		InputStream is = null;
		try {
			is = APP.getResourceAsStream(getLabelFileName(l));
			labels = getLabels(l, is);
		} catch (Exception e) {
			log.error("Error reading resources document", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
					//no-op
				}
			}
		}
		return labels;
	}
	
	private static List<StringLabel> getLabels(Locale l, InputStream is) throws Exception {
		final List<StringLabel> labels = new ArrayList<StringLabel>();
		SAXParserFactory spf = SAXParserFactory.newInstance();
	    spf.setNamespaceAware(true);
		try {
		    SAXParser parser = spf.newSAXParser();
		    XMLReader xr = parser.getXMLReader();
		    xr.setContentHandler(new ContentHandler() {
		    	StringLabel label = null;
		    	
				@Override
				public void startPrefixMapping(String prefix, String uri) throws SAXException {}
				
				@Override
				public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
					if (ENTRY_ELEMENT.equals(localName)) {
						label = new StringLabel(atts.getValue(KEY_ATTR), "");
					}
				}
				
				@Override
				public void startDocument() throws SAXException {}
				
				@Override
				public void skippedEntity(String name) throws SAXException {}
				
				@Override
				public void setDocumentLocator(Locator locator) {}
				
				@Override
				public void processingInstruction(String target, String data) throws SAXException {}
				
				@Override
				public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {}
				
				@Override
				public void endPrefixMapping(String prefix) throws SAXException {}
				
				@Override
				public void endElement(String uri, String localName, String qName) throws SAXException {
					if (ENTRY_ELEMENT.equals(localName)) {
						labels.add(label);
					}
				}
				
				@Override
				public void endDocument() throws SAXException {}
				
				@Override
				public void characters(char[] ch, int start, int length) throws SAXException {
					StringBuilder sb = new StringBuilder(label.getValue());
					sb.append(ch, start, length);
					label.setValue(sb.toString());
				}
			});
			xr.parse(new InputSource(is));
		} catch (Exception e) {
			throw e;
		}
		return labels;
	}
	
	private List<StringLabel> getLabels(Locale l, final String search) {
		if (labelCache.get(l) == null) {
			labelCache.put(l, getLabels(l));
		}
		List<StringLabel> result = new ArrayList<StringLabel>(labelCache.get(l));
		if (!Strings.isEmpty(search)) {
			CollectionUtils.filter(result, new Predicate<StringLabel>() {
				@Override
				public boolean evaluate(StringLabel o) {
					return o != null && (o.getKey().contains(search) || o.getValue().contains(search));
				}
			});
		}
		return result;
	}
	
	@Override
	public StringLabel get(Long id) {
		return null;
	}

	@Override
	public List<StringLabel> get(int start, int count) {
		return null;
	}

	@Override
	public List<StringLabel> get(String search, int start, int count, String order) {
		return null;
	}

	public List<StringLabel> get(Locale l, final String search, int start, int count, final SortParam<String> sort) {
		List<StringLabel> result = getLabels(l, search);
		if (sort != null) {
			Collections.sort(result, new Comparator<StringLabel>() {
				@Override
				public int compare(StringLabel o1, StringLabel o2) {
					int val = 0;
					if (KEY_ATTR.equals(sort.getProperty())) {
						try {
							int i1 = Integer.parseInt(o1.getKey()), i2 = Integer.parseInt(o2.getKey());
							val = i1 - i2;
						} catch (Exception e) {
							val = o1.getKey().compareTo(o2.getKey());
						}
					} else {
						val = o1.getValue().compareTo(o2.getValue());
					}
					return (sort.isAscending() ? 1 : -1) * val;
				}
			});
		}
		return result.subList(start, start + count > result.size() ? result.size() : start + count);
	}
	
	@Override
	public long count() {
		return 0;
	}

	@Override
	public long count(String search) {
		return 0;
	}

	public long count(Locale l, final String search) {
		return getLabels(l, search).size();
	}

	@Override
	public StringLabel update(StringLabel entity, Long userId) {
		return null;
	}

	public StringLabel update(Locale l, StringLabel entity) throws Exception {
		List<StringLabel> labels = labelCache.get(l);
		if (!labels.contains(entity)) {
			labels.add(entity);
			keys.add(entity.getKey());
		}
		storeLabels(l);
		return entity;
	}
	
	@Override
	public void delete(StringLabel entity, Long userId) {
	}
	
	public void delete(Locale l, StringLabel entity) throws Exception {
		List<StringLabel> labels = labelCache.get(l);
		if (labels.contains(entity)) {
			labels.remove(entity);
			keys.remove(entity.getKey());
			storeLabels(l);
		}
	}

	public static void delete(Locale l) {
		for (Map.Entry<Long, Locale> e : languages.entrySet()) {
			if (e.getValue().equals(l)) {
				languages.remove(e.getKey());
				break;
			}
		}
		labelCache.remove(l);
		try {
			URL u = APP.getResource(getLabelFileName(l));
			if (u != null) {
				File f = new File(u.toURI());
				if (f.exists()) {
					f.delete();
				}
			}
		} catch (Exception e) {
			log.error("Unexpected error while deleting language", e);
		}
	}
}
