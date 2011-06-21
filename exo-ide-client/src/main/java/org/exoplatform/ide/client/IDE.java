/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.exoplatform.ide.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;

import org.exoplatform.gwtframework.ui.client.command.Control;
import org.exoplatform.gwtframework.ui.client.component.GWTDialogs;
import org.exoplatform.ide.client.application.ApplicationStateSnapshotListener;
import org.exoplatform.ide.client.application.ControlsRegistration;
import org.exoplatform.ide.client.application.IDEForm;
import org.exoplatform.ide.client.application.IDEPresenter;
import org.exoplatform.ide.client.application.MainMenuControlsFormatter;
import org.exoplatform.ide.client.application.NewItemControlsFormatter;
import org.exoplatform.ide.client.debug.SeleniumTestsHelper;
import org.exoplatform.ide.client.download.DownloadForm;
import org.exoplatform.ide.client.edit.TextEditModule;
import org.exoplatform.ide.client.editor.EditorFactory;
import org.exoplatform.ide.client.framework.control.event.AddControlsFormatterEvent;
import org.exoplatform.ide.client.framework.control.event.RegisterControlEvent.DockTarget;
import org.exoplatform.ide.client.framework.editor.EditorNotFoundException;
import org.exoplatform.ide.client.framework.module.Extension;
import org.exoplatform.ide.client.framework.ui.api.View;
import org.exoplatform.ide.client.framework.ui.impl.ViewHighlightManager;
import org.exoplatform.ide.client.hotkeys.HotKeyManagementModule;
import org.exoplatform.ide.client.messages.IdeEditorLocalizationConstant;
import org.exoplatform.ide.client.messages.IdeErrorsLocalizationConstant;
import org.exoplatform.ide.client.messages.IdeLocalizationMessages;
import org.exoplatform.ide.client.messages.IdeNavigationLocalizationConstant;
import org.exoplatform.ide.client.messages.IdeOperationLocalizationConstant;
import org.exoplatform.ide.client.messages.IdePermissionsLocalizationConstant;
import org.exoplatform.ide.client.messages.IdePreferencesLocalizationConstant;
import org.exoplatform.ide.client.messages.IdeTemplateLocalizationConstant;
import org.exoplatform.ide.client.messages.IdeUploadLocalizationConstant;
import org.exoplatform.ide.client.messages.IdeVersionsLocalizationConstant;
import org.exoplatform.ide.client.model.ApplicationContext;
import org.exoplatform.ide.client.navigation.NavigationModule;
import org.exoplatform.ide.client.operation.OperationModule;
import org.exoplatform.ide.client.preferences.PreferencesModule;
import org.exoplatform.ide.client.project.ProjectSupportingModule;
import org.exoplatform.ide.editor.api.EditorProducer;

/**
 * Created by The eXo Platform SAS .
 * @author <a href="mailto:dmitry.ndp@exoplatform.com.ua">Dmytro Nochevnov</a>
 * @version $Id: $
*/
public class IDE extends org.exoplatform.ide.client.framework.module.IDE
{

   private ControlsRegistration controlsRegistration;

   private ApplicationContext context;

   private IDEPresenter presenter;
   
   /**
    * Initialize constants  for UI 
    */
   public static final IdeLocalizationConstant IDE_LOCALIZATION_CONSTANT = GWT.create(IdeLocalizationConstant.class);
   
   public static final IdeTemplateLocalizationConstant TEMPLATE_CONSTANT = GWT.create(IdeTemplateLocalizationConstant.class);
   
   public static final IdePreferencesLocalizationConstant PREFERENCES_CONSTANT = GWT.create(IdePreferencesLocalizationConstant.class);
   
   public static final IdeVersionsLocalizationConstant VERSIONS_CONSTANT = GWT.create(IdeVersionsLocalizationConstant.class);
   
   public static final IdeUploadLocalizationConstant UPLOAD_CONSTANT = GWT.create(IdeUploadLocalizationConstant.class);
   
   public static final IdePermissionsLocalizationConstant PERMISSIONS_CONSTANT = GWT.create(IdePermissionsLocalizationConstant.class);
   
   public static final IdeNavigationLocalizationConstant NAVIGATION_CONSTANT = GWT.create(IdeNavigationLocalizationConstant.class);
   
   public static final IdeEditorLocalizationConstant EDITOR_CONSTANT = GWT.create(IdeEditorLocalizationConstant.class);
   
   public static final IdeErrorsLocalizationConstant ERRORS_CONSTANT = GWT.create(IdeErrorsLocalizationConstant.class);
   
   public static final IdeOperationLocalizationConstant OPERATION_CONSTANT = GWT.create(IdeOperationLocalizationConstant.class);
   
   public static final IdeLocalizationMessages IDE_LOCALIZATION_MESSAGES = GWT.create(IdeLocalizationMessages.class);

   public IDE()
   {
      new GWTDialogs();
      
      /*
       * Registering Icons
       */
      IDEIconSet.init();
      
      /*
       * Initialise SeleniumTestsHelper
       */
      new SeleniumTestsHelper(EVENT_BUS);

      context = new ApplicationContext();

      new ExceptionThrownEventHandler(EVENT_BUS);

      //new CookieManager(eventBus);
      // new HistoryManager(eventBus, context); // commented to fix the bug with javascript error in IE8 (WBT-321)

      controlsRegistration = new ControlsRegistration(EVENT_BUS);

      EVENT_BUS.fireEvent(new AddControlsFormatterEvent(new MainMenuControlsFormatter()));
      EVENT_BUS.fireEvent(new AddControlsFormatterEvent(new NewItemControlsFormatter()));

      IDEForm ideForm = new IDEForm();
      presenter = new IDEPresenter(EVENT_BUS, ideForm, controlsRegistration);
      new DownloadForm(EVENT_BUS);
      new ViewHighlightManager(EVENT_BUS);

      new ApplicationStateSnapshotListener(EVENT_BUS);

      // MODULES INITIALIZATION
      new NavigationModule(EVENT_BUS, context);
      new ProjectSupportingModule(EVENT_BUS);
      new TextEditModule(EVENT_BUS);
      new OperationModule(EVENT_BUS);

      new PreferencesModule(EVENT_BUS);

      new HotKeyManagementModule(EVENT_BUS);
      
      //initialize extensions
      for (Extension ext : extensions)
      {
         ext.initialize();
      }

      controlsRegistration.formatControls();
      /*
       * Find a method to disable selection of text and elements on the page ( exclude text fields ).
       */
      //disableTextSelectInternal(RootPanel.get().getElement(), true);
   }
   
   /**
    * Disables selection of HTML on element. 
    * 
    * @param e element
    * @param disable <b>true</b> disables all selection on element, <b>false</b> enables selection
    */
   private native static void disableTextSelectInternal(Element e, boolean disable)/*-{
      if (disable) {
          e.ondrag = function () { return false; };
          e.onselectstart = function () { return false; };
          e.style.MozUserSelect="none"
      } else {
          e.ondrag = null;
          e.onselectstart = null;
          e.style.MozUserSelect="text"
      }
   }-*/;

   /**
    * @see org.exoplatform.ide.client.framework.module.IDE#addControl(org.exoplatform.gwtframework.ui.client.command.Control, org.exoplatform.ide.client.framework.control.event.RegisterControlEvent.DockTarget, boolean)
    */
   @Override
   public void addControl(Control<?> control, DockTarget dockTarget, boolean rightDocking)
   {
      controlsRegistration.addControl(control, dockTarget, rightDocking);
   }

   /**
    * @see org.exoplatform.ide.client.framework.module.IDE#closeView(java.lang.String)
    */
   @Override
   public void closeView(String viewId)
   {
      presenter.closeView(viewId);
   }

   /**
    * @see org.exoplatform.ide.client.framework.module.IDE#addEditor(org.exoplatform.ide.editor.api.EditorProducer)
    */
   @Override
   public void addEditor(EditorProducer editorProducer)
   {
      EditorFactory.addEditor(editorProducer);
   }

   @Override
   public void openView(View view)
   {
      presenter.openView(view);
   }

   /**
    * @see org.exoplatform.ide.client.framework.module.IDE#getEditor(java.lang.String)
    */
   @Override
   public EditorProducer getEditor(String mimeType) throws EditorNotFoundException
   {
      return EditorFactory.getDefaultEditor(mimeType);
   }

}
