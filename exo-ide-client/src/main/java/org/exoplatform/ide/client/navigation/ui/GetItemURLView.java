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
package org.exoplatform.ide.client.navigation.ui;

import org.exoplatform.gwtframework.ui.client.component.DynamicForm;
import org.exoplatform.gwtframework.ui.client.component.IButton;
import org.exoplatform.gwtframework.ui.client.component.TextField;
import org.exoplatform.gwtframework.ui.client.component.TitleOrientation;
import org.exoplatform.ide.client.IDEImageBundle;
import org.exoplatform.ide.client.Images;
import org.exoplatform.ide.client.framework.ui.api.ViewType;
import org.exoplatform.ide.client.framework.ui.impl.ViewImpl;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version @version $Id: $
 */

public class GetItemURLView extends ViewImpl implements org.exoplatform.ide.client.navigation.GetItemURLPresenter.Display
{

   private static final int DEFAULT_WIDTH = 500;

   private static final int DEFAULT_HEIGHT = 150;
   
   private static final int BUTTON_WIDTH = 90;

   private static final int BUTTON_HEIGHT = 22;

   public static final String URL_FIELD = "ideGetItemURLFormURLField";

   public static final String ID_DYNAMIC_FORM = "ideGetItemURLFormDynamicForm";

   public static final String ID_OK_BUTTON = "ideGetItemURLFormOkButton";

   private TextField urlField;

   private IButton okButton;
   
   private VerticalPanel mainPanel;

   public GetItemURLView()
   {
      super(ID, ViewType.POPUP, "Item URL", new Image(IDEImageBundle.INSTANCE.url()), DEFAULT_WIDTH, DEFAULT_HEIGHT);

      mainPanel = new VerticalPanel();
      mainPanel.setWidth("100%");
      mainPanel.setHeight("100%");
      mainPanel.setSpacing(5);      
      add(mainPanel);
      
      createFieldForm();
      createButtons();
      
      new Timer()
      {

         @Override
         public void run()
         {
            urlField.selectValue();
            urlField.focusInItem();
         }

      }.schedule(500);
   }

   private void createFieldForm()
   {
      DynamicForm paramsForm = new DynamicForm();
      paramsForm.setID(ID_DYNAMIC_FORM);
      paramsForm.setPadding(5);
      paramsForm.setWidth(450);

      urlField = new TextField(URL_FIELD, "WebDav item's URL:");
      urlField.setWidth(450);
      urlField.setHeight(22);
      urlField.setTitleOrientation(TitleOrientation.TOP);
      paramsForm.add(urlField);
      urlField.focusInItem();
      
      mainPanel.add(paramsForm);
      mainPanel.setCellHorizontalAlignment(paramsForm, HorizontalPanel.ALIGN_CENTER);
      mainPanel.setCellVerticalAlignment(paramsForm, HorizontalPanel.ALIGN_MIDDLE);
      //urlField.setValue(url);
   }

   private void createButtons()
   {
      HorizontalPanel buttonsLayout = new HorizontalPanel();
      buttonsLayout.setHeight(BUTTON_HEIGHT + "px");

      okButton = new IButton("OK");
      okButton.setID(ID_OK_BUTTON);
      okButton.setWidth(BUTTON_WIDTH);
      okButton.setHeight(BUTTON_HEIGHT);
      okButton.setIcon(Images.Buttons.OK);

      buttonsLayout.add(okButton);
      
      mainPanel.add(buttonsLayout);
      mainPanel.setCellHorizontalAlignment(buttonsLayout, HorizontalPanel.ALIGN_CENTER);
      mainPanel.setCellVerticalAlignment(buttonsLayout, HorizontalPanel.ALIGN_MIDDLE);
   }

   @Override
   public HasClickHandlers getOkButton()
   {
      return okButton;
   }

   @Override
   public HasValue<String> getURLField()
   {
      return urlField;
   }

}
