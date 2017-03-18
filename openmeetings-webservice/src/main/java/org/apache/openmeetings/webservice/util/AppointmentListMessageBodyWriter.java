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
package org.apache.openmeetings.webservice.util;

import static org.apache.openmeetings.webservice.util.AppointmentParamConverter.ROOT;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.apache.openmeetings.db.dto.calendar.AppointmentDTO;

import com.github.openjson.JSONArray;
import com.github.openjson.JSONObject;

@Provider
@Produces({MediaType.APPLICATION_JSON})
public class AppointmentListMessageBodyWriter implements MessageBodyWriter<List<AppointmentDTO>> {
	@Override
	public boolean isWriteable(Class<?> clazz, Type type, Annotation[] annotations, MediaType mediaType) {
		if (type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType)type;
			Type[] args = pt.getActualTypeArguments();
			if (args != null && args.length == 1) {
				return AppointmentDTO.class.equals(args[0]);
			}
		}
		return false;
	}

	@Override
	public long getSize(List<AppointmentDTO> t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return 0;
	}

	@Override
	public void writeTo(List<AppointmentDTO> t, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream out)
			throws IOException, WebApplicationException
	{
		Writer writer = new PrintWriter(out);
		JSONArray rr = new JSONArray();
		for (AppointmentDTO dto : t) {
			rr.put(AppointmentParamConverter.json(dto));
		}
		writer.write(new JSONObject().put(ROOT, rr).toString());
		writer.flush();
	}
}
