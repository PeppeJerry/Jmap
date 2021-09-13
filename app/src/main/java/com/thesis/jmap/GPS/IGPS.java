package com.thesis.jmap.GPS;

import android.location.Location;

// Grazie all'interfaccia sono in grado di eseguire una serie di operazioni (Tra cui anche funzioni)
// Come se fossi nella MainActivity ma nella classe GPS
// In questo caso permetto l'aggiornamento della UI dalla classe nella MainActivity
public interface IGPS {
    public void updateUI(Location location);
}
