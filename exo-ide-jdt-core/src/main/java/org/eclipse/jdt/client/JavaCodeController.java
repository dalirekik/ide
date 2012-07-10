/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.eclipse.jdt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;

import org.eclipse.jdt.client.core.compiler.IProblem;
import org.eclipse.jdt.client.core.dom.AST;
import org.eclipse.jdt.client.core.dom.ASTNode;
import org.eclipse.jdt.client.core.dom.ASTParser;
import org.eclipse.jdt.client.core.dom.CompilationUnit;
import org.eclipse.jdt.client.event.CancelParseEvent;
import org.eclipse.jdt.client.event.CancelParseHandler;
import org.eclipse.jdt.client.event.ParseActiveFileEvent;
import org.eclipse.jdt.client.event.ParseActiveFileHandler;
import org.eclipse.jdt.client.internal.compiler.env.INameEnvironment;
import org.exoplatform.gwtframework.commons.exception.ExceptionThrownEvent;
import org.exoplatform.gwtframework.commons.rest.AsyncRequest;
import org.exoplatform.gwtframework.commons.rest.AsyncRequestCallback;
import org.exoplatform.gwtframework.commons.rest.HTTPHeader;
import org.exoplatform.gwtframework.commons.rest.MimeType;
import org.exoplatform.gwtframework.commons.rest.Unmarshallable;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedEvent;
import org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedHandler;
import org.exoplatform.ide.client.framework.editor.event.EditorFileContentChangedEvent;
import org.exoplatform.ide.client.framework.editor.event.EditorFileContentChangedHandler;
import org.exoplatform.ide.client.framework.editor.event.EditorFileOpenedEvent;
import org.exoplatform.ide.client.framework.editor.event.EditorFileOpenedHandler;
import org.exoplatform.ide.client.framework.job.Job;
import org.exoplatform.ide.client.framework.job.Job.JobStatus;
import org.exoplatform.ide.client.framework.job.JobChangeEvent;
import org.exoplatform.ide.client.framework.module.IDE;
import org.exoplatform.ide.client.framework.output.event.OutputEvent;
import org.exoplatform.ide.client.framework.output.event.OutputMessage.Type;
import org.exoplatform.ide.editor.codemirror.CodeMirror;
import org.exoplatform.ide.vfs.client.VirtualFileSystem;
import org.exoplatform.ide.vfs.client.marshal.ItemUnmarshaller;
import org.exoplatform.ide.vfs.client.model.FileModel;
import org.exoplatform.ide.vfs.client.model.ItemWrapper;
import org.exoplatform.ide.vfs.client.model.ProjectModel;
import org.exoplatform.ide.vfs.shared.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * Java code controller is used for getting AST and updating all modules, that depend on the received AST.
 * 
 * @author <a href="mailto:azhuleva@exoplatform.com">Ann Shumilova</a>
 * @version $Id: Feb 6, 2012 10:26:58 AM anya $
 * 
 */
public class JavaCodeController implements EditorFileContentChangedHandler, EditorActiveFileChangedHandler,
   CancelParseHandler, EditorFileOpenedHandler, ParseActiveFileHandler
{

   /**
    * Get build log method's path.
    */
   private static final String LOG = "/ide/maven/log";

   /**
    * Active file in editor.
    */
   private FileModel activeFile;

   private boolean needReparse = false;

   private CodeMirror editor;

   private Map<Integer, IProblem> problems = new HashMap<Integer, IProblem>();

   public static INameEnvironment NAME_ENVIRONMENT;

   private static JavaCodeController instance;

   public JavaCodeController()
   {
      instance = this;
      IDE.addHandler(EditorFileContentChangedEvent.TYPE, this);
      IDE.addHandler(EditorActiveFileChangedEvent.TYPE, this);

      IDE.addHandler(CancelParseEvent.TYPE, this);
      IDE.addHandler(EditorFileOpenedEvent.TYPE, this);
      IDE.addHandler(ParseActiveFileEvent.TYPE, this);

   }

   public static JavaCodeController get()
   {
      return instance;
   }

   /** @return */
   private CompilationUnit parseFile()
   {
      if (editor == null)
         return null;
      ASTParser parser = ASTParser.newParser(AST.JLS3);
      parser.setSource(editor.getText());
      parser.setKind(ASTParser.K_COMPILATION_UNIT);
      parser.setUnitName(activeFile.getName().substring(0, activeFile.getName().lastIndexOf('.')));
      parser.setResolveBindings(true);
      parser.setNameEnvironment(NAME_ENVIRONMENT);
      ASTNode ast = parser.createAST(null);
      CompilationUnit unit = (CompilationUnit)ast;
      return unit;
   }

   /** @see org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedHandler#onEditorActiveFileChanged(org.exoplatform.ide.client.framework.editor.event.EditorActiveFileChangedEvent) */
   @Override
   public void onEditorActiveFileChanged(EditorActiveFileChangedEvent event)
   {
      if (event.getFile() == null)
         return;
      if (event.getFile().getMimeType().equals(MimeType.APPLICATION_JAVA))
      {
         activeFile = event.getFile();
         NAME_ENVIRONMENT = new NameEnvironment(activeFile.getProject().getId());
         if (event.getEditor() instanceof CodeMirror)
         {
            editor = (CodeMirror)event.getEditor();
            if (needReparse)
            {
               timer.cancel();
               timer.schedule(2000);
            }
         }
         else
            editor = null;
      }
      else
      {
         activeFile = null;
         editor = null;
      }
   }

   private Timer timer = new Timer()
   {

      @Override
      public void run()
      {
         Scheduler.get().scheduleIncremental(com);
      }
   };

   private void asyncParse()
   {
      GWT.runAsync(new RunAsyncCallback()
      {

         @Override
         public void onSuccess()
         {
            CompilationUnit unit = parseFile();
            if (unit == null)
            {
               return;
            }
            if (needReparse)
            {
               IProblem[] problems = unit.getProblems();
               if (problems.length == JavaCodeController.this.problems.size())
               {
                  for (IProblem problem : problems)
                  {
                     if (!JavaCodeController.this.problems.containsKey(problem.hashCode()))
                     {
                        reparse(problems);
                        return;
                     }
                  }
                  needReparse = false;
                  JavaCodeController.this.problems.clear();
                  finishJob();
               }
               else
               {
                  reparse(problems);
                  return;
               }

            }
            editor.unmarkAllProblems();
            IDE.fireEvent(new UpdateOutlineEvent(unit, activeFile));
            if (unit.getProblems().length == 0 || editor == null)
               return;

            boolean hasError = false;
            for (IProblem p : unit.getProblems())
            {
               editor.markProblem(new ProblemImpl(p));
               if (p.isError())
                  hasError = true;
            }
            if (hasError)
               checkBuildStatus();
         }

         /**
          * @param problems
          */
         private void reparse(IProblem[] problems)
         {
            JavaCodeController.this.problems.clear();
            for (IProblem problem : problems)
            {
               JavaCodeController.this.problems.put(problem.hashCode(), problem);
            }
            timer.schedule(2000);
         }

         @Override
         public void onFailure(Throwable reason)
         {
            reason.printStackTrace();
         }
      });
   }

   /**
    * 
    */
   private void checkBuildStatus()
   {
      try
      {
         VirtualFileSystem.getInstance().getItemById(activeFile.getProject().getId(),
            new AsyncRequestCallback<ItemWrapper>(new ItemUnmarshaller(new ItemWrapper(activeFile.getProject())))
            {

               @Override
               protected void onSuccess(ItemWrapper result)
               {
                  Item item = result.getItem();
                  if (item instanceof ProjectModel)
                  {
                     ProjectModel project = (ProjectModel)item;
                     if (project.hasProperty("exoide:build_error"))
                     {
                        getBuildLog((String)project.getPropertyValue("exoide:build_error"));
                     }
                  }
               }

               @Override
               protected void onFailure(Throwable exception)
               {
                  IDE.eventBus().fireEvent(new ExceptionThrownEvent(exception));
               }
            });
      }
      catch (RequestException e)
      {
         IDE.eventBus().fireEvent(new ExceptionThrownEvent(e));
      }
   }

   /**
    * @param buildid
    */
   private void getBuildLog(String buildid)
   {
      final String requestUrl = JdtExtension.REST_CONTEXT + LOG + "/" + buildid;

      try
      {
         AsyncRequest.build(RequestBuilder.GET, requestUrl).header(HTTPHeader.CONTENT_TYPE, MimeType.APPLICATION_JSON)
            .send(new AsyncRequestCallback<StringBuilder>(new StringUnmarshaller(new StringBuilder()))
            {

               @Override
               protected void onSuccess(StringBuilder result)
               {
                  IDE.eventBus()
                     .fireEvent(
                        new OutputEvent("Can't build classpath:<br>" + "<pre>" + result.toString() + "</pre>",
                           Type.ERROR));
                  IDE.eventBus().fireEvent(new OutputEvent("After you fix error, do clean project", Type.INFO));
               }

               @Override
               protected void onFailure(Throwable exception)
               {
                  IDE.eventBus().fireEvent(new ExceptionThrownEvent(exception));
               }
            });
      }
      catch (RequestException e)
      {
         IDE.eventBus().fireEvent(new ExceptionThrownEvent(e));
      }
   }

   /**
    * @see org.exoplatform.ide.client.framework.editor.event.EditorFileOpenedHandler#onEditorFileOpened(org.exoplatform.ide.client.framework.editor.event.EditorFileOpenedEvent)
    */
   @Override
   public void onEditorFileOpened(EditorFileOpenedEvent event)
   {
      needReparse = true;
      if (event.getFile().getMimeType().equals(MimeType.APPLICATION_JAVA))
      {
         startJob(event.getFile());
      }
   }

   /**
    * @param event
    */
   private void startJob(FileModel file)
   {
      Job job = new Job(file.getId(), JobStatus.STARTED);
      job.setStartMessage("Initialize Java tooling for " + file.getName());
      IDE.fireEvent(new JobChangeEvent(job));
   }

   /**
    * @see org.eclipse.jdt.client.event.CancelParseHandler#onCancelParse(org.eclipse.jdt.client.event.CancelParseEvent)
    */
   @Override
   public void onCancelParse(CancelParseEvent event)
   {
      timer.cancel();
   }

   RepeatingCommand com = new RepeatingCommand()
   {

      @Override
      public boolean execute()
      {
         asyncParse();
         return false;
      }
   };

   /**
    * @see org.exoplatform.ide.client.framework.editor.event.EditorFileContentChangedHandler#onEditorFileContentChanged(org.exoplatform.ide.client.framework.editor.event.EditorFileContentChangedEvent)
    */
   @Override
   public void onEditorFileContentChanged(EditorFileContentChangedEvent event)
   {
      if (activeFile == null)
         return;
      timer.cancel();
      needReparse = false;
      finishJob();
      if (editor != null)
      {
         timer.schedule(2000);
      }
   }

   /**
    * 
    */
   private void finishJob()
   {
      Job job = new Job(activeFile.getId(), JobStatus.FINISHED);
      job.setFinishMessage("Java Tooling initialized  for " + activeFile.getName());
      IDE.fireEvent(new JobChangeEvent(job));
   }

   /**
    * @see org.eclipse.jdt.client.event.ParseActiveFileHandler#onPaerseActiveFile(org.eclipse.jdt.client.event.ParseActiveFileEvent)
    */
   @Override
   public void onParseActiveFile(ParseActiveFileEvent event)
   {
      needReparse = true;
      startJob(activeFile);
      timer.cancel();
      timer.schedule(2000);
   }

   public FileModel getActiveFile()
   {
      return activeFile;
   }

   public INameEnvironment getNameEnvironment()
   {
      return NAME_ENVIRONMENT;
   }

   private class StringUnmarshaller implements Unmarshallable<StringBuilder>
   {

      protected StringBuilder builder;

      /**
       * @param callback
       */
      public StringUnmarshaller(StringBuilder builder)
      {
         this.builder = builder;
      }

      /**
       * @see org.exoplatform.gwtframework.commons.rest.Unmarshallable#unmarshal(com.google.gwt.http.client.Response)
       */
      @Override
      public void unmarshal(Response response)
      {
         builder.append(response.getText());
      }

      @Override
      public StringBuilder getPayload()
      {
         return builder;
      }
   }
}
