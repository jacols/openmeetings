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
package org.apache.openmeetings.core.remote;

import static org.apache.openmeetings.util.OpenmeetingsVariables.webAppRootKey;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.openmeetings.core.data.conference.RoomManager;
import org.apache.openmeetings.db.dao.calendar.AppointmentDao;
import org.apache.openmeetings.db.dao.room.RoomDao;
import org.apache.openmeetings.db.dao.room.RoomModeratorDao;
import org.apache.openmeetings.db.dao.server.ISessionManager;
import org.apache.openmeetings.db.dao.server.SessiondataDao;
import org.apache.openmeetings.db.dao.user.UserDao;
import org.apache.openmeetings.db.dto.basic.SearchResult;
import org.apache.openmeetings.db.entity.calendar.Appointment;
import org.apache.openmeetings.db.entity.room.Client;
import org.apache.openmeetings.db.entity.room.Room;
import org.apache.openmeetings.db.entity.room.Room.Type;
import org.apache.openmeetings.db.entity.room.RoomModerator;
import org.apache.openmeetings.db.entity.room.RoomGroup;
import org.apache.openmeetings.db.util.AuthLevelUtil;
import org.red5.logging.Red5LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author sebawagner
 * 
 */
public class ConferenceService {
	private static final Logger log = Red5LoggerFactory.getLogger(ConferenceService.class, webAppRootKey);

	@Autowired
	private AppointmentDao appointmentDao;
	@Autowired
	private SessiondataDao sessiondataDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private RoomManager roomManager;
	@Autowired
	private RoomDao roomDao;
	@Autowired
	private RoomModeratorDao roomModeratorsDao;
	@Autowired
	private ISessionManager sessionManager = null;

	public List<RoomGroup> getRoomsByOrganisationWithoutType(String SID, long organisation_id) {
		try {
			Long users_id = sessiondataDao.checkSession(SID);
			if (AuthLevelUtil.hasUserLevel(userDao.getRights(users_id))) {
				log.debug("getRoomsByOrganisationAndType");
				List<RoomGroup> roomOrgsList = roomManager.getRoomsOrganisationByOrganisationId(organisation_id);
				
				for (RoomGroup roomOrg : roomOrgsList) {
					roomOrg.getRoom().setCurrentusers(sessionManager.getClientListByRoom(roomOrg.getRoom().getId()));
				}
	
				return roomOrgsList;
			}
		} catch (Exception err) {
			log.error("[getRoomsByOrganisationAndType]", err);
		}
		return null;
	}

	/**
	 * gets all rooms of an organization TODO:check if the requesting user is
	 * also member of that organization
	 * 
	 * @param SID
	 * @param organisation_id
	 * @return - all rooms of an organization
	 */
	public SearchResult<RoomGroup> getRoomsByOrganisation(String SID,
			long organisation_id, int start, int max, String orderby,
			boolean asc) {

		log.debug("getRoomsByOrganisation");

		Long user_id = sessiondataDao.checkSession(SID);
		if (AuthLevelUtil.hasModLevel(userDao.get(user_id), organisation_id)) {
			return roomManager.getRoomsOrganisationsByOrganisationId(organisation_id, start, max, orderby, asc);
		}
		return null;
	}

	/**
	 * get a List of all public availible rooms (non-appointments)
	 * 
	 * @param SID
	 * @param roomtypes_id
	 * @return - public rooms with given type, null in case of the error
	 */
	public List<Room> getRoomsPublic(String SID, String roomtype) {
		try {
			log.debug("getRoomsPublic");

			Long users_id = sessiondataDao.checkSession(SID);
			if (AuthLevelUtil.hasUserLevel(userDao.getRights(users_id))) {
	
				List<Room> roomList = roomDao.getPublicRooms(Type.valueOf(roomtype));
	
				// Filter : no appointed meetings
				List<Room> filtered = new ArrayList<Room>();
	
				for (Iterator<Room> iter = roomList.iterator(); iter.hasNext();) {
					Room rooms = iter.next();
	
					if (!rooms.isAppointment()) {
						rooms.setCurrentusers(this.getRoomClientsListByRoomId(rooms
								.getId()));
						filtered.add(rooms);
					}
				}
	
				return filtered;
			}
		} catch (Exception err) {
			log.error("[getRoomsByOrganisationAndType]", err);
		}
		return null;
	}

	public List<Room> getRoomsPublicWithoutType(String SID) {
		try {

			Long users_id = sessiondataDao.checkSession(SID);
			if (AuthLevelUtil.hasUserLevel(userDao.getRights(users_id))) {
	
				List<Room> roomList = roomDao.getPublicRooms();
				
				for (Room room : roomList) {
					room.setCurrentusers(sessionManager.getClientListByRoom(room.getId()));
				}
	
				return roomList;
			}
		} catch (Exception err) {
			log.error("[getRoomsPublicWithoutType]", err);
		}
		return null;
	}

	/**
	 * retrieving ServerTime
	 * 
	 * @return - server time
	 */
	// --------------------------------------------------------------------------------------------
	public Date getServerTime() {
		log.debug("getServerTime");

		return new Date(System.currentTimeMillis());

	}

	// --------------------------------------------------------------------------------------------

	/**
	 * 
	 * retrieving Appointment for Room
	 * 
	 * @param roomId
	 * @return - Appointment in case the room is appointment, null otherwise
	 */
	public Appointment getAppointMentDataForRoom(Long roomId) {
		log.debug("getAppointMentDataForRoom");

		Room room = roomDao.get(roomId);

		if (room.isAppointment() == false)
			return null;

		try {
			Appointment ment = appointmentDao.getByRoom(roomId);

			return ment;
		} catch (Exception e) {
			log.error("getAppointMentDataForRoom " + e.getMessage());
			return null;
		}

	}

	// --------------------------------------------------------------------------------------------

	public List<Room> getAppointedMeetingRoomsWithoutType(String SID) {
		log.debug("ConferenceService.getAppointedMeetings");
		try {
			Long users_id = sessiondataDao.checkSession(SID);

			if (AuthLevelUtil.hasUserLevel(userDao.getRights(users_id))) {
				List<Appointment> appointments = appointmentDao.getForToday(users_id);
				List<Room> result = new ArrayList<Room>();

				if (appointments != null) {
					for (int i = 0; i < appointments.size(); i++) {
						Appointment ment = appointments.get(i);

						Long rooms_id = ment.getRoom().getId();
						Room rooom = roomDao.get(rooms_id);

						rooom.setCurrentusers(this
								.getRoomClientsListByRoomId(rooom.getId()));
						result.add(rooom);
					}
				}

				log.debug("Found " + result.size() + " rooms");
				return result;
			}
		} catch (Exception err) {
			log.error("[getAppointedMeetingRoomsWithoutType]", err);
		}
		return null;
	}

	/**
	 * 
	 * @param SID
	 * @param rooms_id
	 * @return - room with the id given
	 */
	public Room getRoomById(String SID, long rooms_id) {
		Long users_id = sessiondataDao.checkSession(SID);
		if (AuthLevelUtil.hasUserLevel(userDao.getRights(users_id))) {
			return roomDao.get(rooms_id);
		}
		return null;
	}

	public Room getRoomWithCurrentUsersById(String SID, long rooms_id) {
		Room room = null;
		Long users_id = sessiondataDao.checkSession(SID);
		if (AuthLevelUtil.hasUserLevel(userDao.getRights(users_id))) {
			room = roomDao.get(rooms_id);
			room.setCurrentusers(sessionManager.getClientListByRoom(room.getId()));
		}
		return room;
	}

	/**
	 * 
	 * @param SID
	 * @param externalUserId
	 * @param externalUserType
	 * @param roomtypes_id
	 * @return - room with the given external id
	 */
	public Room getRoomByExternalId(String SID, Long externalUserId, String externalUserType, long roomtypes_id) {
		Long users_id = sessiondataDao.checkSession(SID);
		if (AuthLevelUtil.hasUserLevel(userDao.getRights(users_id))) {
			//TODO FIXME roomtype
			return roomDao.getExternal(Room.Type.get(roomtypes_id), externalUserType, externalUserId);
		}
		return null;
	}

	/**
	 * gets a list of all available rooms
	 * 
	 * @param SID
	 * @param start
	 * @param max
	 * @param orderby
	 * @param asc
	 * @return - list of rooms being searched
	 */
	public SearchResult<Room> getRooms(String SID, int start, int max,
			String orderby, boolean asc, String search) {
		log.debug("getRooms");

		Long users_id = sessiondataDao.checkSession(SID);
		if (AuthLevelUtil.hasAdminLevel(userDao.getRights(users_id))) {
			return roomManager.getRooms(start, max, orderby, asc, search);
		}
		return null;
	}

	public SearchResult<Room> getRoomsWithCurrentUsers(String SID, int start,
			int max, String orderby, boolean asc) {
		log.debug("getRooms");

		Long users_id = sessiondataDao.checkSession(SID);
		if (AuthLevelUtil.hasAdminLevel(userDao.getRights(users_id))) {
			return roomManager.getRoomsWithCurrentUsers(start, max, orderby, asc);
		}
		return null;
	}

	public List<RoomModerator> getRoomModeratorsByRoomId(String SID,
			Long roomId) {
		try {

			Long users_id = sessiondataDao.checkSession(SID);

			if (AuthLevelUtil.hasUserLevel(userDao.getRights(users_id))) {

				return roomModeratorsDao.getByRoomId(roomId);

			}

		} catch (Exception err) {
			log.error("[getRoomModeratorsByRoomId]", err);
			err.printStackTrace();
		}

		return null;
	}

	/**
	 * return all participants of a room
	 * 
	 * @param room_id
	 * @return - true if room is full, false otherwise
	 */
	public boolean isRoomFull(Long room_id) {
		try {
			Room room = roomDao.get(room_id);
			
			if (room.getNumberOfPartizipants() <= this.sessionManager
					.getClientListByRoom(room_id).size()) {
				return true;
			}
			
			return false;
		} catch (Exception err) {
			log.error("[isRoomFull]", err);
		}
		return true;
	}

	/**
	 * return all participants of a room
	 * 
	 * @param room_id
	 * @return - all participants of a room
	 */
	public List<Client> getRoomClientsListByRoomId(Long room_id) {
		return sessionManager.getClientListByRoom(room_id);
	}

	public List<Room> getRoomsWithCurrentUsersByList(String SID, int start, int max, String orderby, boolean asc) {
		log.debug("getRoomsWithCurrentUsersByList");

		Long users_id = sessiondataDao.checkSession(SID);
		if (AuthLevelUtil.hasAdminLevel(userDao.getRights(users_id))) {
			return roomManager.getRoomsWithCurrentUsersByList(start, max, orderby, asc);
		}
		return null;
	}

	public List<Room> getRoomsWithCurrentUsersByListAndType(String SID, int start, int max, String orderby, boolean asc, String externalRoomType) {
		log.debug("getRoomsWithCurrentUsersByListAndType");

		Long users_id = sessiondataDao.checkSession(SID);
		if (AuthLevelUtil.hasAdminLevel(userDao.getRights(users_id))) {
			return roomManager.getRoomsWithCurrentUsersByListAndType(start, max, orderby, asc, externalRoomType);
		}
		return null;
	}

	public Room getRoomByOwnerAndType(String SID, Long roomTypeId, String roomName) {
		Long userId = sessiondataDao.checkSession(SID);
		if (AuthLevelUtil.hasUserLevel(userDao.getRights(userId))) {
			//TODO FIXME roomtype
			return roomDao.getUserRoom(userId, Room.Type.get(roomTypeId), roomName);
		}
		return null;
	}
}
