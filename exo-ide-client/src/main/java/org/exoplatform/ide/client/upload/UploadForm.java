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
package org.exoplatform.ide.client.upload;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.StatefulCanvas;
import com.smartgwt.client.widgets.events.CloseClickHandler;
import com.smartgwt.client.widgets.events.CloseClientEvent;
import com.smartgwt.client.widgets.events.HasClickHandlers;
import com.smartgwt.client.widgets.form.fields.ToolbarItem;

import org.exoplatform.gwtframework.ui.client.GwtResources;
import org.exoplatform.gwtframework.ui.client.component.DynamicForm;
import org.exoplatform.gwtframework.ui.client.component.TextField;
import org.exoplatform.ide.client.Images;
import org.exoplatform.ide.client.framework.configuration.IDEConfiguration;
import org.exoplatform.ide.client.framework.ui.DialogWindow;
import org.exoplatform.ide.client.framework.vfs.Item;

import java.util.List;

/**
 * Class for uploading zip file.
 * 
 * @author <a href="mailto:oksana.vereshchaka@gmail.com">Oksana Vereshchaka</a>
 * @version $Id: Dec 10, 2010 $
 *
 */
public class UploadForm extends DialogWindow implements UploadPresenter.UploadDisplay
{

   public static final int WIDTH = 450;

   public static final int HEIGHT = 170;

   private static final String ID = "ideUploadForm";

   private static final String ID_UPLOAD_BUTTON = "ideUploadFormUploadButton";

   private static final String ID_CLOSE_BUTTON = "ideUploadFormCloseButton";

   private static final String ID_DYNAMIC_FORM = "ideUploadFormDynamicForm";

   private static final String FILE_NAME_FIELD = "ideUploadFormFilenameField";

   private static final String ID_BROWSE_BUTTON = "ideUploadFormBrowseButton";

   private FormPanel uploadForm;

   private TextField fileNameField;

   private IButton uploadButton;

   private IButton closeButton;

   protected UploadPresenter presenter;

   protected String title;

   protected String buttonTitle;

   protected String labelTitle;

   protected HorizontalPanel postFieldsPanel;

   protected IDEConfiguration applicationConfiguration;
   
   protected FileUploadInput fileUploadInput;

   public UploadForm(HandlerManager eventBus, List<Item> selectedItems, String path, 
      IDEConfiguration applicationConfiguration)
   {
      super(eventBus, WIDTH, HEIGHT, ID);
      initialize(eventBus, selectedItems, path, applicationConfiguration);
   }
   
   public UploadForm(HandlerManager eventBus, List<Item> selectedItems, String path, 
      IDEConfiguration applicationConfiguration, int width, int height)
   {
      super(eventBus, width, height, ID);
      initialize(eventBus, selectedItems, path, applicationConfiguration);
   }
   
   private void initialize(HandlerManager eventBus, List<Item> selectedItems, String path, 
      IDEConfiguration applicationConfiguration)
   {
      this.eventBus = eventBus;
      this.applicationConfiguration = applicationConfiguration;
      
      initTitles();

      setTitle(title);

      addCloseClickHandler(new CloseClickHandler()
      {
         public void onCloseClick(CloseClientEvent event)
         {
            destroy();
         }
      });

      createFileUploadForm();
      createButtons();

      show();
//      UIHelper.setAsReadOnly(fileNameField.getName());
      presenter = createPresenter(eventBus, selectedItems, path);
//      fileUploadInput.setFileSelectedHandler(presenter);
      presenter.bindDisplay(this);
   }
   
   protected void initTitles()
   {
      title = "Upload folder";
      buttonTitle = "Upload";
      labelTitle = "Folder to upload (zip)";
   }
   
   protected UploadPresenter createPresenter(HandlerManager eventBus, List<Item> selectedItems, String path)
   {
      return new UploadPresenter(eventBus, selectedItems, path);
   }

   private void createFileUploadForm()
   {
      addItem(createUploadFormItems());
   }
   
   protected VerticalPanel createUploadFormItems()
   {
      VerticalPanel panel = new VerticalPanel();
      panel.add(getUploadLayout());
      
      return panel;
   }

   private void createButtons()
   {
      DynamicForm uploadWindowButtonsForm = new DynamicForm();
      uploadWindowButtonsForm.setWidth(200);
      uploadWindowButtonsForm.setMargin(10);
/*TODO      uploadWindowButtonsForm.setLayoutAlign(VerticalAlignment.TOP);
      uploadWindowButtonsForm.setLayoutAlign(Alignment.CENTER);*/

      uploadButton = new IButton(buttonTitle);
      uploadButton.setID(ID_UPLOAD_BUTTON);
      uploadButton.setHeight(22);
      uploadButton.setIcon(Images.MainMenu.File.UPLOAD);

      StatefulCanvas buttonSpacer = new StatefulCanvas();
      buttonSpacer.setWidth(5);

      closeButton = new IButton("Cancel");
      closeButton.setID(ID_CLOSE_BUTTON);
      closeButton.setHeight(22);
      closeButton.setIcon(Images.Buttons.CANCEL);

      ToolbarItem buttonToolbar = new ToolbarItem();
      buttonToolbar.setButtons(uploadButton, buttonSpacer, closeButton);

   //TODO   uploadWindowButtonsForm.setFields(buttonToolbar);

      addItem(uploadWindowButtonsForm);
   }
   
   private HorizontalPanel getUploadLayout()
   {
      HorizontalPanel uploadHPanel = new HorizontalPanel();
      uploadHPanel.setWidth("330px");
      uploadHPanel.setHeight("42px");
      uploadHPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);

      fileNameField = new TextField();
      fileNameField.setName(FILE_NAME_FIELD);
      fileNameField.setShowTitle(true);
      fileNameField.setTitle("File to upload:");
      fileNameField.setWidth(245);
      fileNameField.setHeight(22);

      // create upload form

      AbsolutePanel absolutePanel = new AbsolutePanel();
      absolutePanel.setSize("80px", "22px");
      org.exoplatform.gwtframework.ui.client.component.IButton selectButton = new org.exoplatform.gwtframework.ui.client.component.IButton();
      selectButton.setTitle("Browse");
      selectButton.setWidth(80);
      selectButton.setHeight(22);

      uploadForm = new FormPanel();
      uploadForm.setMethod(FormPanel.METHOD_POST);
      uploadForm.setEncoding(FormPanel.ENCODING_MULTIPART);

      // create file upload input

      postFieldsPanel = new HorizontalPanel();

      fileUploadInput = new FileUploadInput();
      fileUploadInput.setWidth("80px");
      fileUploadInput.setHeight("22px");
      fileUploadInput.setStyleName(GwtResources.INSTANCE.css().transparent(), true);
      
      postFieldsPanel.add(fileNameField);
      postFieldsPanel.add(fileUploadInput);

      //uploadForm.setEncoding(encodingType)

      uploadForm.setAction(buildUploadPath());

      uploadForm.setWidget(postFieldsPanel);

      absolutePanel.add(selectButton, 0, 0);
      absolutePanel.add(uploadForm, 0, 0);
      uploadHPanel.add(fileNameField);
      uploadHPanel.add(absolutePanel);
      return uploadHPanel;
   }
   
   protected String buildUploadPath()
   {
      return applicationConfiguration.getUploadServiceContext() + "/folder/";
   }

   public void setHiddenFields(String location, String mimeType, String nodeType, String jcrContentNodeType)
   {
      Hidden locationField = new Hidden(FormFields.LOCATION, location);
//      Hidden mimeTypeField = new Hidden(FormFields.MIME_TYPE, mimeType);
//      Hidden nodeTypeField = new Hidden(FormFields.NODE_TYPE, nodeType);
//      Hidden jcrContentNodeTypeField = new Hidden(FormFields.JCR_CONTENT_NODE_TYPE, jcrContentNodeType);

      postFieldsPanel.add(locationField);
//      postFieldsPanel.add(mimeTypeField);
//      postFieldsPanel.add(nodeTypeField);
//      postFieldsPanel.add(jcrContentNodeTypeField);
   }

   public FormPanel getUploadForm()
   {
      return uploadForm;
   }

   public HasClickHandlers getUploadButton()
   {
      return this.uploadButton;
   }

   public HasClickHandlers getCloseButton()
   {
      return this.closeButton;
   }

   public HasValue<String> getFileNameField()
   {
      return fileNameField;
   }

   public void closeDisplay()
   {
      destroy();
   }

   @Override
   public void destroy()
   {
      presenter.destroy();
      super.destroy();
   }

   public void disableUploadButton()
   {
      uploadButton.disable();
   }

   public void enableUploadButton()
   {
      uploadButton.enable();
   }

}
