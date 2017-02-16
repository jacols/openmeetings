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
package org.apache.openmeetings.service.mail.template.subject;

import static org.apache.openmeetings.db.util.ApplicationHelper.ensureApplication;

import java.util.Locale;
import java.util.TimeZone;

import org.apache.openmeetings.db.entity.calendar.Appointment;
import org.apache.openmeetings.db.entity.user.User;
import org.apache.openmeetings.db.util.LocaleHelper;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.string.Strings;

public class CreatedAppointmentTemplate extends AbstractAppointmentTemplate {
	private static final long serialVersionUID = 1L;
	private final String invitorName;

	private CreatedAppointmentTemplate(Locale locale, Appointment a, TimeZone tz, String invitorName) {
		super(locale, a, tz);
		this.invitorName = invitorName;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new Label("titleLbl", getString("1151", locale)));
		add(new Label("title", a.getTitle()));
		add(new WebMarkupContainer("descContainer")
			.add(new Label("descLbl", getString("1152", locale)))
			.add(new Label("desc", a.getDescription()).setEscapeModelStrings(false))
			.setVisible(!Strings.isEmpty(a.getDescription()))
			);
		add(new Label("startLbl", getString("1153", locale)));
		add(new Label("start", format(a.getStart())));
		add(new Label("endLbl", getString("1154", locale)));
		add(new Label("end", format(a.getEnd())));
		add(new Label("invitorLbl", getString("1156", locale)));
		add(new Label("invitor", invitorName));
	}

	public static AbstractSubjectEmailTemplate get(User u, Appointment a, TimeZone tz, String invitorName) {
		ensureApplication(u.getLanguageId());
		return new CreatedAppointmentTemplate(LocaleHelper.getLocale(u), a, tz, invitorName).create();
	}

	@Override
	String getPrefix() {
		return ensureApplication().getOmString("1151", locale);
	}
}
