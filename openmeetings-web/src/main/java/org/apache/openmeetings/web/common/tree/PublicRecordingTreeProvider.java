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
package org.apache.openmeetings.web.common.tree;

import static org.apache.openmeetings.web.app.Application.getBean;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.openmeetings.db.dao.record.FlvRecordingDao;
import org.apache.openmeetings.db.entity.file.FileItem.Type;
import org.apache.openmeetings.db.entity.record.FlvRecording;
import org.apache.openmeetings.web.app.WebSession;

public class PublicRecordingTreeProvider extends RecordingTreeProvider {
	private static final long serialVersionUID = 1L;
	private final Long orgId;
	private final String name;

	public PublicRecordingTreeProvider(Long orgId, String name) {
		this.orgId = orgId;
		this.name = name;
	}
	
	public Iterator<? extends FlvRecording> getRoots() {
		FlvRecording r = new FlvRecording();
		r.setId(orgId == null ? -1 : -orgId);
		r.setOrganization_id(orgId);
		r.setOwnerId(null);
		r.setType(Type.Folder);
		String pub = WebSession.getString(861);
		r.setFileName(orgId == null ? pub : String.format("%s (%s)", pub, name));
		return Arrays.asList(r).iterator();
	}
	
	public Iterator<? extends FlvRecording> getChildren(FlvRecording node) {
		if (node.getId() < 0) {
			return getBean(FlvRecordingDao.class).getFlvRecordingRootByPublic(orgId).iterator();
		} else {
			return super.getChildren(node);
		}
	}
}
