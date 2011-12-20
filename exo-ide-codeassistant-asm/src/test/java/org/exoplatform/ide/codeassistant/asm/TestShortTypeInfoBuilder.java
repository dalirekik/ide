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
package org.exoplatform.ide.codeassistant.asm;

import org.exoplatform.ide.codeassistant.jvm.ShortTypeInfo;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Modifier;

public class TestShortTypeInfoBuilder
{

   private final int access = Modifier.PUBLIC;

   private final String name = "org/exoplatform/test/TestClass";

   private final String superName = "org/exoplatform/test/TestSuper";

   private final String[] interfaces = {};

   @Test
   public void testAccess()
   {
      TypeInfoBuilder typeInfoBuilder =
         new TypeInfoBuilder(Modifier.PUBLIC | Modifier.ABSTRACT, name, superName, interfaces);
      ShortTypeInfo shortTypeInfo = typeInfoBuilder.buildShortTypeInfo();

      Assert.assertEquals(Integer.valueOf(Modifier.PUBLIC | Modifier.ABSTRACT), shortTypeInfo.getModifiers());
      Assert.assertEquals("public abstract", shortTypeInfo.modifierToString());
   }

   @Test
   public void testName()
   {
      TypeInfoBuilder typeInfoBuilder =
         new TypeInfoBuilder(access, "org/exoplatform/test/Class", superName, interfaces);
      ShortTypeInfo shortTypeInfo = typeInfoBuilder.buildShortTypeInfo();

      Assert.assertEquals("Class", shortTypeInfo.getName());
      Assert.assertEquals("org.exoplatform.test.Class", shortTypeInfo.getQualifiedName());
   }

   @Test
   public void testClassType()
   {
      TypeInfoBuilder typeInfoBuilder = new TypeInfoBuilder(Modifier.PUBLIC, name, superName, interfaces);
      ShortTypeInfo shortTypeInfo = typeInfoBuilder.buildShortTypeInfo();

      Assert.assertEquals(Integer.valueOf(Modifier.PUBLIC), shortTypeInfo.getModifiers());
      Assert.assertEquals("CLASS", shortTypeInfo.getType());
   }

   @Test
   public void testInterfaceType()
   {
      TypeInfoBuilder typeInfoBuilder =
         new TypeInfoBuilder(Modifier.PUBLIC | Modifier.INTERFACE, name, superName, interfaces);
      ShortTypeInfo shortTypeInfo = typeInfoBuilder.buildShortTypeInfo();

      Assert.assertEquals(Integer.valueOf(Modifier.PUBLIC | Modifier.INTERFACE), shortTypeInfo.getModifiers());
      Assert.assertEquals("INTERFACE", shortTypeInfo.getType());
   }

   @Test
   public void testAnnotationType()
   {
      TypeInfoBuilder typeInfoBuilder =
         new TypeInfoBuilder(Modifier.PUBLIC | TypeInfoBuilder.MODIFIER_ANNOTATION, name, superName, interfaces);
      ShortTypeInfo shortTypeInfo = typeInfoBuilder.buildShortTypeInfo();

      Assert.assertEquals(Integer.valueOf(Modifier.PUBLIC | TypeInfoBuilder.MODIFIER_ANNOTATION),
         shortTypeInfo.getModifiers());
      Assert.assertEquals("ANNOTATION", shortTypeInfo.getType());
   }

   @Test
   public void testEnumType()
   {
      TypeInfoBuilder typeInfoBuilder =
         new TypeInfoBuilder(Modifier.PUBLIC | TypeInfoBuilder.MODIFIER_ENUM, name, superName, interfaces);
      ShortTypeInfo shortTypeInfo = typeInfoBuilder.buildShortTypeInfo();

      Assert.assertEquals(Integer.valueOf(Modifier.PUBLIC | TypeInfoBuilder.MODIFIER_ENUM),
         shortTypeInfo.getModifiers());
      Assert.assertEquals("ENUM", shortTypeInfo.getType());
   }

}
