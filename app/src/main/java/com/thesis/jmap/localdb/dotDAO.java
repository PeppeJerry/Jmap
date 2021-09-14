package com.thesis.jmap.localdb;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface dotDAO {

    // Inserisci un punto
    // Se il nuovo punto ha un id (Primary Key) ripetuta -> Non lo aggiunge
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addDot(Dot dot);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void forceAddDot(Dot dot);

    // Prende tutti i punti nel db ;
    @Query("SELECT * FROM dots WHERE model!='LAST_STATE'")
    List<Dot> getQueuedots();

    // Prende un massimo di punti nel db
    @Query("SELECT * FROM dots WHERE model!='LAST_STATE' LIMIT :limit")
    List<Dot> getQueuedotsLimited(int limit);

    // Nuke them all
    @Query("DELETE FROM dots")
    void nuke();

    // Prende l'id maggiore, MAX(id) dava dei problemi
    // Alternativamente:
    //     @Query("SELECT id FROM dots ORDER BY id DESC LIMIT 1")
    @Query("SELECT MAX(id) FROM dots")
    List<Integer> max_id();

    // Conta il numero di tutte le righe
    @Query("SELECT COUNT(*) FROM dots")
    List<Integer> num_rows();

    // Utilizzato per la verifica dell'esistenza di determinati punti (LAST_STATE)
    // LAST_STATE -> Punto che indica l'ultimo indirizzo dello Switch nella UI
    @Query("SELECT x FROM dots WHERE model ='LAST_STATE'")
    List<Double> getSwitchState();

}
