package org.ow2.proactive_grid_cloud_portal;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.ow2.proactive.scheduler.common.exception.NotConnectedException;

@Provider
public class NotConnectedExceptionWriter implements MessageBodyWriter<NotConnectedException> {

  public boolean isWriteable(Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    return NotConnectedException.class.isAssignableFrom(type);
    }

  public void writeTo(NotConnectedException formRestEasyException, Class<?> type, Type genericType,
          Annotation[] annotations, MediaType mediaType,
          MultivaluedMap<String, Object> headers,
          OutputStream out) throws IOException {
    out.write("<ns1:exception xmlns:ns1='http://scrs.forms.resteasy.service/'>".getBytes());
    out.write("</ns1:exception>".getBytes());
  }

  public long getSize(NotConnectedException formRestEasyException,
      java.lang.Class<?> type,
      java.lang.reflect.Type genericType,
      java.lang.annotation.Annotation[] annotations,
          MediaType mediaType) {
    return -1;
  }
} 