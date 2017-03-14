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
package org.apache.openmeetings.db.entity.file;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Entity
@NamedQueries({
	@NamedQuery(name = "getAllFiles", query = "SELECT f FROM FileExplorerItem f ORDER BY f.id")
	, @NamedQuery(name = "getFileById", query = "SELECT f FROM FileExplorerItem f WHERE f.id = :id")
	, @NamedQuery(name = "getFileByHash", query = "SELECT f FROM FileExplorerItem f WHERE f.hash = :hash")
	, @NamedQuery(name = "getFilesByRoom", query = "SELECT f FROM FileExplorerItem f WHERE f.deleted = false AND f.roomId = :roomId " +
			"AND f.ownerId IS NULL AND f.parentId IS NULL ORDER BY f.type ASC, f.name ")
	, @NamedQuery(name = "getFilesByOwner", query = "SELECT f FROM FileExplorerItem f WHERE f.deleted = false AND f.ownerId = :ownerId "
			+ "AND f.parentId IS NULL ORDER BY f.type ASC, f.name ")
	, @NamedQuery(name = "getFilesByParent", query = "SELECT f FROM FileExplorerItem f WHERE f.deleted = false "
			+ "AND f.parentId = :parentId ORDER BY f.type ASC, f.name ")
	, @NamedQuery(name = "getFilesFilteredByParent", query = "SELECT f FROM FileExplorerItem f WHERE f.deleted = false "
			+ "AND f.parentId = :parentId AND f.type IN :filter ORDER BY f.type ASC, f.name ")
	, @NamedQuery(name = "getFileExternal", query = "SELECT f FROM FileExplorerItem f WHERE f.externalId = :externalId AND f.externalType LIKE :externalType")
	, @NamedQuery(name = "getFileByGroup", query = "SELECT f FROM FileExplorerItem f WHERE f.deleted = false AND f.ownerId IS NULL "
			+ "AND f.groupId = :groupId AND f.parentId IS NULL "
			+ "ORDER BY f.type ASC, f.name")
	, @NamedQuery(name = "getFileFilteredByGroup", query = "SELECT f FROM FileExplorerItem f WHERE f.deleted = false AND f.ownerId IS NULL "
			+ "AND f.groupId = :groupId AND f.parentId IS NULL AND f.type IN :filter "
			+ "ORDER BY f.type ASC, f.name")
})
@Table(name = "fileexploreritem")
@Root
public class FileExplorerItem extends FileItem {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	@Element(data = true, name = "fileExplorerItemId")
	private Long id;

	@Column(name = "filesize")
	@Element(data = true, required = false)
	private Long size;

	@Column(name = "external_id")
	private String externalId;

	@Column(name = "external_type")
	private String externalType;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long fileSize) {
		this.size = fileSize;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public String getExternalType() {
		return externalType;
	}

	public void setExternalType(String externalType) {
		this.externalType = externalType;
	}
}
