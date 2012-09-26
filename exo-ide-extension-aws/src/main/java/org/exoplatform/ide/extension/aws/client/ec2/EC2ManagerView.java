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
package org.exoplatform.ide.extension.aws.client.ec2;

import com.google.gwt.event.dom.client.HasClickHandlers;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import org.exoplatform.gwtframework.ui.client.component.ImageButton;
import org.exoplatform.ide.client.framework.ui.impl.ViewImpl;
import org.exoplatform.ide.client.framework.ui.impl.ViewType;
import org.exoplatform.ide.extension.aws.client.AWSExtension;
import org.exoplatform.ide.extension.aws.shared.ec2.InstanceInfo;

import java.util.List;

/**
 * View for manage Amazon EC2 virtual sever instances.
 * 
 * @author <a href="mailto:azatsarynnyy@exoplatform.com">Artem Zatsarynnyy</a>
 * @version $Id: EC2ManagerView.java Sep 21, 2012 10:12:25 AM azatsarynnyy $
 *
 */
public class EC2ManagerView extends ViewImpl implements EC2Manager.Display
{

   private static final String ID = "ideEC2ManagerView";

   private static final int WIDTH = 1000;

   private static final int HEIGHT = 500;

   @UiField
   ImageButton terminateInstanceButton;

   @UiField
   ImageButton rebootInstanceButton;

   @UiField
   ImageButton stopInstanceButton;

   @UiField
   ImageButton startInstanceButton;

   @UiField
   EC2InstancesGrid ec2ImagesGrid;

   private static EC2ManagerViewUiBinder uiBinder = GWT.create(EC2ManagerViewUiBinder.class);

   interface EC2ManagerViewUiBinder extends UiBinder<Widget, EC2ManagerView>
   {
   }

   public EC2ManagerView()
   {
      super(ID, ViewType.MODAL, AWSExtension.LOCALIZATION_CONSTANT.managementEC2ViewTitle(), null, WIDTH, HEIGHT);
      add(uiBinder.createAndBindUi(this));
   }

   /**
    * @see org.exoplatform.ide.extension.aws.client.ec2.EC2Manager.Display#getStopInstanceButton()
    */
   @Override
   public HasClickHandlers getStopInstanceButton()
   {
      return stopInstanceButton;
   }

   /**
    * @see org.exoplatform.ide.extension.aws.client.ec2.EC2Manager.Display#getTerminateInstanceButton()
    */
   @Override
   public HasClickHandlers getTerminateInstanceButton()
   {
      return terminateInstanceButton;
   }

   /**
    * @see org.exoplatform.ide.extension.aws.client.ec2.EC2Manager.Display#getRebootInstanceButton()
    */
   @Override
   public HasClickHandlers getRebootInstanceButton()
   {
      return rebootInstanceButton;
   }

   /**
    * @see org.exoplatform.ide.extension.aws.client.ec2.EC2Manager.Display#getStartInstanceButton()
    */
   @Override
   public HasClickHandlers getStartInstanceButton()
   {
      return startInstanceButton;
   }

   /**
    * @see org.exoplatform.ide.extension.aws.client.ec2.EC2Manager.Display#setImages(java.util.List)
    */
   @Override
   public void setInstances(List<InstanceInfo> instatceList)
   {
      ec2ImagesGrid.setValue(instatceList);
   }

}