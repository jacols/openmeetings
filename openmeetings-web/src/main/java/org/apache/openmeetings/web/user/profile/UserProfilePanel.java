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
package org.apache.openmeetings.web.user.profile;

import static org.apache.openmeetings.web.app.Application.getBean;
import static org.apache.openmeetings.web.app.WebSession.getUserId;

import org.apache.openmeetings.db.dao.user.UserContactDao;
import org.apache.openmeetings.db.dao.user.UserDao;
import org.apache.openmeetings.db.entity.user.User;
import org.apache.openmeetings.web.app.Application;
import org.apache.openmeetings.web.common.ProfileImagePanel;
import org.apache.openmeetings.web.common.UserPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.model.CompoundPropertyModel;

public class UserProfilePanel extends UserPanel {
	private static final long serialVersionUID = 1L;
	private final WebMarkupContainer address = new WebMarkupContainer("address");
	private final Label addressDenied = new Label("addressDenied", "");

	public UserProfilePanel(String id, long userId) {
		this(id, new CompoundPropertyModel<User>(getBean(UserDao.class).get(userId)));
	}
	
	public UserProfilePanel(String id, CompoundPropertyModel<User> model) {
		super(id, model);

		add(new ProfileImagePanel("img", model.getObject().getId()));
		add(new Label("firstname"));
		add(new Label("lastname"));
		add(new Label("timeZoneId"));
		add(new Label("regdate"));
		add(new TextArea<String>("userOffers").setEnabled(false));
		add(new TextArea<String>("userSearchs").setEnabled(false));
		if (getUserId().equals(model.getObject().getId()) || model.getObject().isShowContactData()
				|| (model.getObject().isShowContactDataToContacts() && getBean(UserContactDao.class).isContact(model.getObject().getId(), getUserId())))
		{
			addressDenied.setVisible(false);
			address.add(new Label("address.phone"));
			address.add(new Label("address.street"));
			address.add(new Label("address.additionalname"));
			address.add(new Label("address.zip"));
			address.add(new Label("address.town"));
			address.add(new Label("address.state.name"));
			address.add(new Label("address.comment"));
		} else {
			address.setVisible(false);
			addressDenied.setDefaultModelObject(Application.getString(model.getObject().isShowContactDataToContacts() ? 1269 : 1268));
		}
		add(address.setDefaultModel(model));
		add(addressDenied);
	}
}
