/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ide.client.workspace;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import elemental.html.Element;
import elemental.html.TableCellElement;
import elemental.html.TableElement;
import org.exoplatform.ide.Resources;
import org.exoplatform.ide.api.resources.ResourceProvider;
import org.exoplatform.ide.api.ui.part.PartAgent.PartStackType;
import org.exoplatform.ide.api.ui.wizard.NewProjectWizardAgent;
import org.exoplatform.ide.client.ImageBundle;
import org.exoplatform.ide.client.event.FileEvent;
import org.exoplatform.ide.client.event.FileEvent.FileOperation;
import org.exoplatform.ide.client.event.FileEventHandler;
import org.exoplatform.ide.client.extensionsPart.ExtensionsPage;
import org.exoplatform.ide.client.projectExplorer.ProjectExplorerPresenter;
import org.exoplatform.ide.client.welcome.WelcomePage;
import org.exoplatform.ide.command.EditorActiveExpression;
import org.exoplatform.ide.command.NoProjectOpenedExpression;
import org.exoplatform.ide.command.ProjectOpenedExpression;
import org.exoplatform.ide.command.ShowNewProjectWizardCommand;
import org.exoplatform.ide.core.editor.EditorAgent;
import org.exoplatform.ide.core.expressions.ExpressionManager;
import org.exoplatform.ide.java.client.projectmodel.JavaProject;
import org.exoplatform.ide.java.client.projectmodel.JavaProjectDesctiprion;
import org.exoplatform.ide.json.JsonArray;
import org.exoplatform.ide.json.JsonCollections;
import org.exoplatform.ide.menu.MainMenuPresenter;
import org.exoplatform.ide.outline.OutlinePartPresenter;
import org.exoplatform.ide.part.PartAgentPresenter;
import org.exoplatform.ide.presenter.Presenter;
import org.exoplatform.ide.resources.model.File;
import org.exoplatform.ide.resources.model.Folder;
import org.exoplatform.ide.resources.model.Project;
import org.exoplatform.ide.resources.model.ProjectDescription;
import org.exoplatform.ide.resources.model.Property;
import org.exoplatform.ide.rest.MimeType;
import org.exoplatform.ide.ui.list.SimpleList;
import org.exoplatform.ide.ui.list.SimpleList.View;
import org.exoplatform.ide.util.dom.Elements;
import org.exoplatform.ide.util.loging.Log;
import org.exoplatform.ide.wizard.WizardPagePresenter;
import org.exoplatform.ide.wizard.genericproject.GenericProjectPagePresenter;
import org.exoplatform.ide.wizard.newproject.NewProjectWizardAgentImpl;
import java.util.Date;

/**
 * Root Presenter that implements Workspace logic. Descendant Presenters are injected via
 * constructor and exposed to coresponding UI containers.
 *
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 24, 2012  
 */
public class WorkspacePresenter implements Presenter
{

   public interface Display extends IsWidget
   {
      HasWidgets getCenterPanel();

      void clearCenterPanel();

      HasWidgets getLeftPanel();

      HasWidgets getMenuPanel();

      HasWidgets getRightPanel();
   }

   Display display;

   EventBus eventBus;

   ProjectExplorerPresenter projectExplorerPresenter;

   private final MainMenuPresenter menuPresenter;

   private final PartAgentPresenter partAgent;

   private final EditorAgent editorAgent;

   protected final Resources resources;

   @Inject
   protected WorkspacePresenter(Display display, final ProjectExplorerPresenter projectExplorerPresenter,
                                EventBus eventBus, MainMenuPresenter menuPresenter, EditorAgent editorAgent, Resources resources,
                                final ResourceProvider resourceProvider, final ExpressionManager expressionManager, PartAgentPresenter partAgent,
                                ExtensionsPage extensionsPage, ImageBundle imageBundle,
                                OutlinePartPresenter outlinePresenter, NoProjectOpenedExpression noProjectOpenedExpression,
                                EditorActiveExpression editorActiveExpression, ProjectOpenedExpression projectOpenedExpression,
                                NewProjectWizardAgentImpl newProjectWizardAgent)
   {
      super();
      this.display = display;
      this.projectExplorerPresenter = projectExplorerPresenter;
      this.eventBus = eventBus;
      this.menuPresenter = menuPresenter;
      this.editorAgent = editorAgent;
      this.partAgent = partAgent;
      this.resources = resources;

      // FOR DEMO

      // CREATE STATIC MENU CONTENT
      menuPresenter.addMenuItem("File/New/new File", null);
      registerWizards(newProjectWizardAgent, resourceProvider);
      menuPresenter.addMenuItem("File/New/new Project", new ShowNewProjectWizardCommand(newProjectWizardAgent,
         resources));
      menuPresenter.addMenuItem("File/Open Project", null, new OpenProjectCommand(resourceProvider), null,
         noProjectOpenedExpression);

      // CREATE DYNAMIC MENU CONTENT
      menuPresenter.addMenuItem("File/Create Demo Content", null, new CreateDemoContentCommand(resourceProvider), null,
         noProjectOpenedExpression);

      menuPresenter.addMenuItem("Edit", null, null, editorActiveExpression, null);
      menuPresenter.addMenuItem("Edit/Some Editor Operation", null, null, editorActiveExpression, null);

      menuPresenter.addMenuItem("Project", null, null, projectOpenedExpression, null);
      menuPresenter.addMenuItem("Project/Some Project Operation", null, null, projectOpenedExpression,
         noProjectOpenedExpression);
      bind();

      //XXX DEMO

      partAgent.addPart(extensionsPage, PartStackType.EDITING);
      partAgent.addPart(new WelcomePage(imageBundle), PartStackType.EDITING);
      partAgent.addPart(projectExplorerPresenter, PartStackType.NAVIGATION);
      partAgent.addPart(outlinePresenter, PartStackType.TOOLING);
   }

   /**
    * Registers available wizards.
    *
    * @param newProjectWizardAgent
    * @param resourceProvider
    */
   private void registerWizards(NewProjectWizardAgent newProjectWizardAgent, final ResourceProvider resourceProvider)
   {
      Provider<WizardPagePresenter> genericProjectWizard = new Provider<WizardPagePresenter>()
      {
         public WizardPagePresenter get()
         {
            return new GenericProjectPagePresenter(resources, resourceProvider);
         }
      };

      newProjectWizardAgent.registerWizard("Generic Project", "Creates generic project", "",
         resources.genericProjectIcon(), genericProjectWizard, JsonCollections.<String>createArray());
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void go(HasWidgets container)
   {
      container.clear();
      // Expose Project Explorer into Tools Panel
      menuPresenter.go(display.getMenuPanel());

      partAgent.go(PartStackType.NAVIGATION, display.getLeftPanel());
      partAgent.go(PartStackType.EDITING, display.getCenterPanel());
      partAgent.go(PartStackType.TOOLING, display.getRightPanel());

      container.add(display.asWidget());
   }

   protected void bind()
   {
      eventBus.addHandler(FileEvent.TYPE, new FileEventHandler()
      {

         @Override
         public void onFileOperation(final FileEvent event)
         {
            if (event.getOperationType() == FileOperation.OPEN)
            {
               //               // Set up the callback object.
               //               AsyncCallback<File> callback = new AsyncCallback<File>()
               //               {
               //                  @Override
               //                  public void onFailure(Throwable caught)
               //                  {
               //                     GWT.log("error" + caught);
               //                  }
               //
               //                  @Override
               //                  public void onSuccess(File file)
               //                  {
               //                     openFile(file);
               //                  }
               //               };
               //
               //               Project project = event.getFile().getProject();
               //               project.getContent(event.getFile(), callback);
               editorAgent.openEditor(event.getFile());

               //fileSystemService.getFileContent(event.getFileName(), callback);
            }
            else if (event.getOperationType() == FileOperation.CLOSE)
            {
               // close associated editor. OR it can be closed itself TODO
            }
         }
      });
   }

   // FOR DEMO:
   private final class CreateDemoContentCommand implements Command
   {
      private final ResourceProvider resourceManager;

      private CreateDemoContentCommand(ResourceProvider resourceManager)
      {
         this.resourceManager = resourceManager;
      }

      @Override
      public void execute()
      {
         // DUMMY CREATE DEMO CONTENT
         resourceManager.createProject("Test Project " + (new Date().getTime()), JsonCollections
            .<Property>createArray(
               //
               new Property(ProjectDescription.PROPERTY_PRIMARY_NATURE, JavaProject.PRIMARY_NATURE),//
               new Property(JavaProjectDesctiprion.PROPERTY_SOURCE_FOLDERS, JsonCollections.createArray(
                  "src/main/java", "src/main/resources", "src/test/java", "src/test/resources"))//
            ), new AsyncCallback<Project>()
         {

            @Override
            public void onSuccess(final Project project)
            {
               project.createFolder(project, "src", new AsyncCallback<Folder>()
               {

                  @Override
                  public void onFailure(Throwable caught)
                  {
                     Log.error(getClass(), caught);
                  }

                  @Override
                  public void onSuccess(Folder result)
                  {
                     project.createFolder(result, "main", new AsyncCallback<Folder>()
                     {

                        @Override
                        public void onFailure(Throwable caught)
                        {
                           Log.error(getClass(), caught);
                        }

                        @Override
                        public void onSuccess(Folder result)
                        {
                           project.createFolder(result, "java/org/exoplatform/ide", new AsyncCallback<Folder>()
                           {

                              @Override
                              public void onFailure(Throwable caught)
                              {
                                 Log.error(getClass(), caught);
                              }

                              @Override
                              public void onSuccess(Folder result)
                              {
                                 project.createFile(result, "Test.java",
                                    "package org.exoplatform.ide;\n public class Test\n{\n}",
                                    MimeType.APPLICATION_JAVA, new AsyncCallback<File>()
                                 {

                                    @Override
                                    public void onFailure(Throwable caught)
                                    {
                                       Log.error(getClass(), caught);
                                    }

                                    @Override
                                    public void onSuccess(File result)
                                    {
                                    }
                                 });
                                 project.createFolder(result, "void", new AsyncCallback<Folder>()
                                 {

                                    @Override
                                    public void onFailure(Throwable caught)
                                    {
                                       Log.error(getClass(), caught);
                                    }

                                    @Override
                                    public void onSuccess(Folder result)
                                    {
                                    }
                                 });
                              }
                           });
                           project.createFolder(result, "resources/org/exoplatform/ide", new AsyncCallback<Folder>()
                           {

                              @Override
                              public void onFailure(Throwable caught)
                              {
                                 Log.error(getClass(), caught);
                              }

                              @Override
                              public void onSuccess(Folder result)
                              {
                                 project.createFile(result, "styles.css", ".test{\n\n}", "text/css",
                                    new AsyncCallback<File>()
                                    {

                                       @Override
                                       public void onSuccess(File result)
                                       {
                                          // ok
                                       }

                                       @Override
                                       public void onFailure(Throwable caught)
                                       {
                                          Log.error(getClass(), caught);
                                       }
                                    });

                              }
                           });

                           project.createFolder(result, "webapp", new AsyncCallback<Folder>()
                           {

                              @Override
                              public void onFailure(Throwable caught)
                              {
                                 Log.error(getClass(), caught);
                              }

                              @Override
                              public void onSuccess(Folder result)
                              {
                              }
                           });
                        }
                     });
                     project.createFolder(result, "test", new AsyncCallback<Folder>()
                     {

                        @Override
                        public void onFailure(Throwable caught)
                        {
                           Log.error(getClass(), caught);
                        }

                        @Override
                        public void onSuccess(Folder result)
                        {
                           project.createFolder(result, "java/org/exoplatform/ide", new AsyncCallback<Folder>()
                           {

                              @Override
                              public void onFailure(Throwable caught)
                              {
                                 Log.error(getClass(), caught);
                              }

                              @Override
                              public void onSuccess(Folder result)
                              {
                                 project.createFile(result, "TestClass.java",
                                    "package org.exoplatform.ide;\n public class TestClass\n{\n}",
                                    MimeType.APPLICATION_JAVA, new AsyncCallback<File>()
                                 {

                                    @Override
                                    public void onFailure(Throwable caught)
                                    {
                                    }

                                    @Override
                                    public void onSuccess(File result)
                                    {
                                    }
                                 });
                              }
                           });

                           project.createFolder(result, "resources/org/exoplatform/ide", new AsyncCallback<Folder>()
                           {

                              @Override
                              public void onFailure(Throwable caught)
                              {
                                 Log.error(getClass(), caught);
                              }

                              @Override
                              public void onSuccess(Folder result)
                              {
                                 project.createFile(result, "TestFileOnFs.txt",
                                    "This is file content of the file from VFS", "text/text-pain",
                                    new AsyncCallback<File>()
                                    {

                                       @Override
                                       public void onSuccess(File result)
                                       {
                                          // ok
                                       }

                                       @Override
                                       public void onFailure(Throwable caught)
                                       {
                                          GWT.log("Error creating demo folder" + caught);
                                       }
                                    });

                              }
                           });
                        }
                     });

                  }
               });

            }

            @Override
            public void onFailure(Throwable caught)
            {
               GWT.log("Error creating demo content" + caught);
            }
         });
      }
   }

   /**
    * Opens new project.
    * TODO : Extract dialog as framework UI component
    */
   private final class OpenProjectCommand implements Command
   {
      private final ResourceProvider resourceProvider;

      private SimpleList<String> list;

      private SimpleList.ListItemRenderer<String> listItemRenderer = new SimpleList.ListItemRenderer<String>()
      {
         @Override
         public void render(Element itemElement, String itemData)
         {
            TableCellElement label = Elements.createTDElement();
            label.setInnerHTML(itemData);
            itemElement.appendChild(label);
         }

         @Override
         public Element createElement()
         {
            return Elements.createTRElement();
         }
      };

      private SimpleList.ListEventDelegate<String> listDelegate = new SimpleList.ListEventDelegate<String>()
      {
         @Override
         public void onListItemClicked(Element itemElement, String itemData)
         {
            Log.info(this.getClass(), "onListItemClicked ", itemElement);
            list.getSelectionModel().setSelectedItem(itemData);
         }

         @Override
         public void onListItemDoubleClicked(Element listItemBase, String itemData)
         {
            Log.info(this.getClass(), "onListItemDoubleClicked ", itemData);
            //                     Assert.isNotNull(delegate);
            //                     delegate.onSelect(itemData);
         }
      };

      /**
       *
       */
      @Inject
      public OpenProjectCommand(ResourceProvider resourceProvider)
      {
         // TODO : create list wrapper, so it can be used as GWT Widget
         this.resourceProvider = resourceProvider;

         TableElement tableElement = Elements.createTableElement();
         tableElement.setAttribute("style", "width: 100%");
         list = SimpleList.create((View)tableElement, resources.defaultSimpleListCss(), listItemRenderer, listDelegate);
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void execute()
      {

         resourceProvider.listProjects(new AsyncCallback<JsonArray<String>>()
         {
            @Override
            public void onSuccess(JsonArray<String> result)
            {
               final PopupPanel dialogBox = createDialog(result);
               dialogBox.center();
               dialogBox.show();
            }

            @Override
            public void onFailure(Throwable caught)
            {
               Log.error(OpenProjectCommand.class, "can't list projects", caught);
            }
         });

      }

      /**
       * @return
       */
      public PopupPanel createDialog(JsonArray<String> projects)
      {

         final DialogBox dialogBox = new DialogBox();
         dialogBox.setText("Open the project");

         ScrollPanel listPanel = new ScrollPanel();
         listPanel.setSize("100%", "100%");
         listPanel.getElement().appendChild((Node)list.getView().getElement());
         dialogBox.setTitle("Select a project");
         dialogBox.setText("Select a project, please");

         DockLayoutPanel content = new DockLayoutPanel(Unit.PX);
         content.setSize("300px", "300px");
         FlowPanel bottomPanel = new FlowPanel();
         content.addSouth(bottomPanel, 24);
         content.add(listPanel);

         dialogBox.setWidget(content);

         Button closeButton = new Button("cancel", new ClickHandler()
         {
            @Override
            public void onClick(ClickEvent event)
            {
               dialogBox.hide();
            }
         });
         Button okButton = new Button("ok", new ClickHandler()
         {
            @Override
            public void onClick(ClickEvent event)
            {
               Log.info(this.getClass(), "onClick = ", list.getSelectionModel().getSelectedItem());
               if (list.getSelectionModel().getSelectedItem() != null)
               {
                  String selectedItem = list.getSelectionModel().getSelectedItem();
                  resourceProvider.getProject(selectedItem, new AsyncCallback<Project>()
                  {
                     @Override
                     public void onSuccess(Project result)
                     {
                        dialogBox.hide();
                     }

                     @Override
                     public void onFailure(Throwable caught)
                     {
                        Log.error(OpenProjectCommand.class, "can't open projects", caught);
                     }
                  });
               }
            }
         });
         bottomPanel.add(closeButton);
         bottomPanel.add(okButton);
         list.render(projects);
         return dialogBox;
      }
   }

}