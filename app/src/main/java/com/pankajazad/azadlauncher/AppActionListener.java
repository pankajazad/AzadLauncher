package com.pankajazad.azadlauncher;

interface AppActionListener {
    void onOpenApp(AppEntry app);

    boolean onLongPressApp(AppEntry app);
}
