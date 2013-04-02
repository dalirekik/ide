// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.collide.dto;

import org.exoplatform.ide.dtogen.shared.RoutingType;

/**
 * Inheritance association between two code blocks. From the language
 * perspective, source type extends target type. From code completion
 * perspective the semantics is "source code block contains all children
 * defined in the target code block"
 */
@RoutingType(type = RoutingTypes.INHERITANCEASSOCIATION)
public interface InheritanceAssociation extends CodeBlockAssociation {
}
