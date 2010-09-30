package org.ow2.proactive_grid_cloud_portal;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.ow2.proactive_grid_cloud_portal.exceptions.IOExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.exceptions.JobAlreadyFinishedExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.exceptions.NotConnectedExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.exceptions.PermissionExceptionExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.exceptions.SubmissionClosedExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.exceptions.UnknownJobExceptionMapper;
import org.ow2.proactive_grid_cloud_portal.exceptions.UnknownTaskExceptionMapper;

public class MyPortalApplication extends Application
{
  HashSet<Object> singletons = new HashSet<Object>();

  public MyPortalApplication()
  {
//    singletons.add(new NotConnectedExceptionMapper()); 
//    singletons.add(new PermissionExceptionExceptionMapper()); 
  }

  @Override
  public Set<Class<?>> getClasses()
  {
    HashSet<Class<?>> set = new HashSet<Class<?>>();
    set.add(NotConnectedExceptionMapper.class);
    set.add(PermissionExceptionExceptionMapper.class);
    set.add(UnknownTaskExceptionMapper.class);
    set.add(IOExceptionMapper.class);
    set.add(JobAlreadyFinishedExceptionMapper.class); 
    set.add(SubmissionClosedExceptionMapper.class);
    set.add(UnknownJobExceptionMapper.class);
    return set;
  }

  @Override
  public Set<Object> getSingletons()
  {
    return singletons;
  }
} 
