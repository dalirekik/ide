/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.ide.core;

import static org.junit.Assert.assertEquals;

import org.exoplatform.ide.TestConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Created by The eXo Platform SAS .
 * 
 * @author <a href="mailto:gavrikvetal@gmail.com">Vitaliy Gulyy</a>
 * @version $
 */

public class Preview extends AbstractTestModule
{
   public interface Locators
   {
      public static final String GROOVY_TEMPLATE_PREVIEW = "//div[@view-id='Preview']";

      public static final String GADGET_PREVIEW = "//div[@view-id='gadgetpreview']";

      public static final String HTML_PREVIEW = "//div[@view-id='idePreviewHTMLView']";
   }
   
   
   @FindBy(xpath = Locators.HTML_PREVIEW)
   private WebElement htmlPreview;

   
   /**
    * Wait for HTML preview view opened.
    * 
    * @throws Exception
    */
   public void waitHtmlPreviewOpened() throws Exception
   {
      new WebDriverWait(driver(), 3).until(new ExpectedCondition<Boolean>()
      {

         @Override
         public Boolean apply(WebDriver input)
         {
            return (htmlPreview != null && htmlPreview.isDisplayed());
         }
      });
   }

   /**
    * Wait for HTML preview view closed.
    * 
    * @throws Exception
    */
   public void waitHtmlPreviewClosed() throws Exception
   {
      new WebDriverWait(driver(), 3).until(new ExpectedCondition<Boolean>()
      {

         @Override
         public Boolean apply(WebDriver input)
         {
            try
            {
               input.findElement(By.xpath(Locators.HTML_PREVIEW));
               return false;
            }
            catch (NoSuchElementException e)
            {
               return true;
            }
         }
      });
   }

   public void checkPreviewHTMLIsOpened(boolean isOpened)
   {
      String locator = "//div[@view-id='idePreviewHTMLView']";
      assertEquals(isOpened, selenium().isElementPresent(locator));
   }

   public void checkPreviewGadgetIsOpened(boolean isOpened)
   {
      assertEquals(isOpened, selenium().isElementPresent(Locators.GADGET_PREVIEW));
   }

   public void selectIFrame(String iFrameURL)
   {
      selenium().selectFrame("//iframe[@src='" + iFrameURL + "']");
   }

   public boolean isGroovyTemplateVisible()
   {
      return selenium().isElementPresent(Locators.GROOVY_TEMPLATE_PREVIEW);
   }

   /**
    * Selenium selects preview iframe.
    */
   public void selectPreviewIFrame()
   {
      selenium().selectFrame("eXo-IDE-preview-frame");
   }

   public void close() throws Exception
   {
      String locator =
         "//div[@panel-id='operation']//table[@class='gwt-DecoratedTabBar']//div[@role='tab']//div[@button-name='close-tab' and @tab-title='Preview']";
      selenium().mouseOver(locator);
      Thread.sleep(TestConstants.ANIMATION_PERIOD);

      selenium().click(locator);
      Thread.sleep(TestConstants.REDRAW_PERIOD);
   }

}
