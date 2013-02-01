/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     James Blackburn (Broadcom Corp.) - ongoing development
 *******************************************************************************/
package com.codenvy.eclipse.core.internal.events;

import com.codenvy.eclipse.core.internal.utils.Messages;
import com.codenvy.eclipse.core.internal.watson.ElementTree;
import com.codenvy.eclipse.core.resources.IProject;
import com.codenvy.eclipse.core.resources.IResource;
import com.codenvy.eclipse.core.resources.IResourceChangeEvent;
import com.codenvy.eclipse.core.resources.IResourceChangeListener;
import com.codenvy.eclipse.core.resources.IResourceDelta;
import com.codenvy.eclipse.core.resources.IWorkspaceRunnable;
import com.codenvy.eclipse.core.runtime.CoreException;
import com.codenvy.eclipse.core.runtime.IProgressMonitor;
import com.codenvy.eclipse.core.runtime.ISafeRunnable;
import com.codenvy.eclipse.core.runtime.IStatus;
import com.codenvy.eclipse.core.runtime.Path;
import com.codenvy.eclipse.core.runtime.SafeRunner;
import com.codenvy.eclipse.core.runtime.Status;
import com.codenvy.eclipse.core.runtime.jobs.Job;
import com.codenvy.eclipse.resources.WorkspaceResource;

import java.util.HashSet;
import java.util.Set;

public class NotificationManager implements ILifecycleListener
{
   class NotifyJob extends Job
   {
      private final IWorkspaceRunnable noop = new IWorkspaceRunnable()
      {
         public void run(IProgressMonitor monitor)
         {
            // do nothing
         }
      };

      public NotifyJob()
      {
         super(Messages.resources_updating);
         setSystem(true);
      }

      public IStatus run(IProgressMonitor monitor)
      {
         if (monitor.isCanceled())
         {
            return Status.CANCEL_STATUS;
         }
         notificationRequested = true;
         try
         {
            workspace.run(noop, null, IResource.NONE, null);
         }
         catch (CoreException e)
         {
            return e.getStatus();
         }
         return Status.OK_STATUS;
      }
   }

   private static final long NOTIFICATION_DELAY = 1500;

   /**
    * The Threads that are currently avoiding notification.
    */
   private Set<Thread> avoidNotify = new HashSet<Thread>();

   /**
    * Indicates whether a notification is currently in progress. Used to avoid
    * causing a notification to be requested as a result of another notification.
    */
   protected boolean isNotifying;

   // if there are no changes between the current tree and the last delta state then we
   // can reuse the lastDelta (if any). If the lastMarkerChangeId is different then the current
   // one then we have to update that delta with new marker change info

   /**
    * last delta we broadcast
    */
   private ResourceDelta lastDelta;

   /**
    * the marker change Id the last time we computed a delta
    */
   private long lastDeltaId;

   /**
    * tree the last time we computed a delta
    */
   private ElementTree lastDeltaState;

   protected long lastNotifyDuration = 0L;

   /**
    * the marker change id at the end of the last POST_AUTO_BUILD
    */
   private long lastPostBuildId = 0;

   /**
    * The state of the workspace at the end of the last POST_BUILD
    * notification
    */
   private ElementTree lastPostBuildTree;

   /**
    * the marker change id at the end of the last POST_CHANGE
    */
   private long lastPostChangeId = 0;

   /**
    * The state of the workspace at the end of the last POST_CHANGE
    * notification
    */
   private ElementTree lastPostChangeTree;

   private ResourceChangeListenerList listeners;

   protected boolean notificationRequested = false;

   private Job notifyJob;

   WorkspaceResource workspace;

   public NotificationManager(WorkspaceResource workspace)
   {
      this.workspace = workspace;
      listeners = new ResourceChangeListenerList();
      notifyJob = new NotifyJob();
   }

   public void addListener(IResourceChangeListener listener, int eventMask)
   {
      listeners.add(listener, eventMask);
      //		if (ResourceStats.TRACE_LISTENERS)
      //			ResourceStats.listenerAdded(listener);
   }

   /**
    * Indicates the beginning of a block where periodic notifications should be avoided.
    * Returns true if notification avoidance really started, and false for nested
    * operations.
    */
   public boolean beginAvoidNotify()
   {
      return avoidNotify.add(Thread.currentThread());
   }

   /**
    * Signals the beginning of the notification phase at the end of a top level operation.
    */
   public void beginNotify()
   {
      notifyJob.cancel();
      notificationRequested = false;
   }

   /**
    * The main broadcast point for notification deltas
    */
   public void broadcastChanges(ElementTree lastState, ResourceChangeEvent event, boolean lockTree)
   {
      final int type = event.getType();
      try
      {
         // Do the notification if there are listeners for events of the given type.
         if (!listeners.hasListenerFor(type))
         {
            return;
         }
         isNotifying = true;
         ResourceDelta delta = getDelta(lastState, type);
         //don't broadcast POST_CHANGE or autobuild events if the delta is empty
         if (delta == null || delta.getKind() == 0)
         {
            int trigger = event.getBuildKind();
            //				if (trigger == IncrementalProjectBuilder.AUTO_BUILD || trigger == 0)
            //					return;
         }
         event.setDelta(delta);
         long start = System.currentTimeMillis();
         notify(getListeners(), event, lockTree);
         lastNotifyDuration = System.currentTimeMillis() - start;
      }
      finally
      {
         // Update the state regardless of whether people are listening.
         isNotifying = false;
         cleanUp(lastState, type);
      }
   }

   /**
    * Performs cleanup at the end of a resource change notification
    */
   private void cleanUp(ElementTree lastState, int type)
   {
      // Remember the current state as the last notified state if requested.
      // Be sure to clear out the old delta
      boolean postChange = type == IResourceChangeEvent.POST_CHANGE;
      if (postChange || type == IResourceChangeEvent.POST_BUILD)
      {
         //			long id = workspace.getMarkerManager().getChangeId();
         lastState.immutable();
         if (postChange)
         {
            lastPostChangeTree = lastState;
            //				lastPostChangeId = id;
         }
         else
         {
            lastPostBuildTree = lastState;
            //				lastPostBuildId = id;
         }
         //			workspace.getMarkerManager().resetMarkerDeltas(Math.min(lastPostBuildId, lastPostChangeId));
         lastDelta = null;
         lastDeltaState = lastState;
      }
   }

   /**
    * Helper method for the save participant lifecycle computation.
    */
   public void broadcastChanges(IResourceChangeListener listener, int type, IResourceDelta delta)
   {
      ResourceChangeListenerList.ListenerEntry[] entries;
      entries = new ResourceChangeListenerList.ListenerEntry[]{new ResourceChangeListenerList.ListenerEntry(listener,
         type)};
      notify(entries, new ResourceChangeEvent(workspace, type, 0, delta), false);
   }

   /**
    * Indicates the end of a block where periodic notifications should be avoided.
    */
   public void endAvoidNotify()
   {
      avoidNotify.remove(Thread.currentThread());
   }

   /**
    * Requests that a periodic notification be scheduled
    */
   public void requestNotify()
   {
      //don't do intermediate notifications if the current thread doesn't want them
      if (isNotifying || avoidNotify.contains(Thread.currentThread()))
      {
         return;
      }
      //notifications must never take more than one tenth of operation time
      long delay = Math.max(NOTIFICATION_DELAY, lastNotifyDuration * 10);
      if (notifyJob.getState() == Job.NONE)
      {
         notifyJob.schedule(delay);
      }
   }

   /**
    * Computes and returns the resource delta for the given event type and the
    * given current tree state.
    */
   protected ResourceDelta getDelta(ElementTree tree, int type)
   {
      //		long id = workspace.getMarkerManager().getChangeId();
      // If we have a delta from last time and no resources have changed
      // since then, we can reuse the delta structure.
      // However, be sure not to mix deltas from post_change with build events, because they use
      // a different reference point for delta computation.
      boolean postChange = type == IResourceChangeEvent.POST_CHANGE;
      if (!postChange && lastDelta != null && !ElementTree.hasChanges(tree, lastDeltaState,
         ResourceComparator.getNotificationComparator(), true))
      {
         // Markers may have changed since the delta was generated. If so, get the new
         // marker state and insert it in to the delta which is being reused.
         //			if (id != lastDeltaId) {
         //				Map<IPath, MarkerSet> markerDeltas = workspace.getMarkerManager().getMarkerDeltas(lastPostBuildId);
         //				lastDelta.updateMarkers(markerDeltas);
         //			}
      }
      else
      {
         // We don't have a delta or something changed so recompute the whole deal.
         ElementTree oldTree = postChange ? lastPostChangeTree : lastPostBuildTree;
         if (oldTree == null)
         {
            oldTree = new ElementTree();
         }
         long markerId = postChange ? lastPostChangeId : lastPostBuildId;
         lastDelta = ResourceDeltaFactory.computeDelta(workspace, oldTree, tree, Path.ROOT, markerId + 1);
      }
      // remember the state of the world when this delta was consistent
      lastDeltaState = tree;
      //		lastDeltaId = id;
      return lastDelta;
   }

   protected ResourceChangeListenerList.ListenerEntry[] getListeners()
   {
      return listeners.getListeners();
   }

   public void handleEvent(LifecycleEvent event)
   {
      switch (event.kind)
      {
         case LifecycleEvent.PRE_PROJECT_CLOSE:
            if (!listeners.hasListenerFor(IResourceChangeEvent.PRE_CLOSE))
            {
               return;
            }
            IProject project = (IProject)event.resource;
            notify(getListeners(), new ResourceChangeEvent(workspace, IResourceChangeEvent.PRE_CLOSE, project), true);
            break;
         case LifecycleEvent.PRE_PROJECT_MOVE:
            //only notify deletion on move if old project handle is going
            // away
            if (event.resource.equals(event.newResource))
            {
               return;
            }
            //fall through
         case LifecycleEvent.PRE_PROJECT_DELETE:
            if (!listeners.hasListenerFor(IResourceChangeEvent.PRE_DELETE))
            {
               return;
            }
            project = (IProject)event.resource;
            notify(getListeners(), new ResourceChangeEvent(workspace, IResourceChangeEvent.PRE_DELETE, project), true);
            break;
         case LifecycleEvent.PRE_REFRESH:
            if (!listeners.hasListenerFor(IResourceChangeEvent.PRE_REFRESH))
            {
               return;
            }
            if (event.resource.getType() == IResource.PROJECT)
            {
               notify(getListeners(),
                  new ResourceChangeEvent(event.resource, IResourceChangeEvent.PRE_REFRESH, event.resource), true);
            }
            else if (event.resource.getType() == IResource.ROOT)
            {
               notify(getListeners(), new ResourceChangeEvent(workspace, IResourceChangeEvent.PRE_REFRESH, null), true);
            }
            break;
      }
   }

   private void notify(ResourceChangeListenerList.ListenerEntry[] resourceListeners, final IResourceChangeEvent event,
      final boolean lockTree)
   {
      int type = event.getType();
      boolean oldLock = workspace.isTreeLocked();
      if (lockTree)
      {
         workspace.setTreeLocked(true);
      }
      try
      {
         for (int i = 0; i < resourceListeners.length; i++)
         {
            if ((type & resourceListeners[i].eventMask) != 0)
            {
               final IResourceChangeListener listener = resourceListeners[i].listener;
               //					if (ResourceStats.TRACE_LISTENERS)
               //						ResourceStats.startNotify(listener);
               SafeRunner.run(new ISafeRunnable()
               {
                  public void handleException(Throwable e)
                  {
                     // exception logged in SafeRunner#run
                  }

                  public void run() throws Exception
                  {
                     listener.resourceChanged(event);
                  }
               });
               //					if (ResourceStats.TRACE_LISTENERS)
               //						ResourceStats.endNotify();
            }
         }
      }
      finally
      {
         if (lockTree)
         {
            workspace.setTreeLocked(oldLock);
         }
      }
   }

   public void removeListener(IResourceChangeListener listener)
   {
      listeners.remove(listener);
      //		if (ResourceStats.TRACE_LISTENERS)
      //			ResourceStats.listenerRemoved(listener);
   }

   /**
    * Returns true if a notification is needed. This happens if
    * sufficient time has elapsed since the last notification
    *
    * @return true if a notification is needed, and false otherwise
    */
   public boolean shouldNotify()
   {
      return !isNotifying && notificationRequested;
   }

   public void shutdown(IProgressMonitor monitor)
   {
      //wipe out any existing listeners
      listeners = new ResourceChangeListenerList();
   }

   public void startup(IProgressMonitor monitor)
   {
      // get the current state of the workspace as the starting point and
      // tell the workspace to track changes from there. This gives the
      // notification manager an initial basis for comparison.
      lastPostBuildTree = lastPostChangeTree = workspace.getElementTree();
      workspace.addLifecycleListener(this);
   }
}
