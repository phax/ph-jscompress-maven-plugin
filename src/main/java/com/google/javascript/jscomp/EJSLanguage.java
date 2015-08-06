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
