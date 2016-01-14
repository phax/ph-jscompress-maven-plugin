/**
 * Copyright (C) 2014-2016 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.javascript.jscomp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum EJSLanguage
{
 ECMASCRIPT6_STRICT ("ECMASCRIPT6_STRICT"),
 ECMASCRIPT6 ("ECMASCRIPT6"),
 ECMASCRIPT5_STRICT ("ECMASCRIPT5_STRICT"),
 ECMASCRIPT5 ("ECMASCRIPT5"),
 ECMASCRIPT3 ("ECMASCRIPT3"),
 ECMASCRIPT6_TYPED ("ECMASCRIPT6_TYPED");

  public static final EJSLanguage DEFAULT = ECMASCRIPT5;

  private final String m_sID;

  private EJSLanguage (@Nonnull final String sID)
  {
    m_sID = sID;
  }

  @Nonnull
  public String getID ()
  {
    return m_sID;
  }

  @Nullable
  public static EJSLanguage getFromIDOrNull (@Nullable final String sID)
  {
    if (sID != null && sID.length () > 0)
      for (final EJSLanguage e : values ())
        if (sID.equals (e.getID ()))
          return e;
    return null;
  }
}
