package com.carocean.settings.screensaver;

import android.app.Activity;

public abstract interface IScreenSaverControl
{
  public abstract void attachActivity(Activity paramActivity);

  public abstract void detachActivity();

  public abstract void resetTime();
}