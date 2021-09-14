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

    // Prende tutti i punti nel db di uno specifico flusso (uuid)
    // I punti vengono ordinati per ordine crescente rispetto al tempo di acquisizione (time)
    @Query("SELECT * FROM dots WHERE model!='LAST_STATE' AND uuid = :uuid ORDER BY time")
    List<Dot> getQueuedots(String uuid);

    // Prende un massimo di punti nel db di uno specifico flusso (uuid)
    // I punti vengono ordinati per ordine crescente rispetto al tempo di acquisizione (time)
    @Query("SELECT * FROM dots WHERE model!='LAST_STATE' AND uuid = :uuid ORDER BY time LIMIT :limit")
    List<Dot> getQueuedots(String uuid, int limit);

    // Prende il valore di uuid del punto più anziano
    @Query("SELECT uuid FROM dots WHERE model!='LAST_STATE' ORDER BY time LIMIT 1")
    String getDot();

    // Rimuove un sub-set di punti ~ Nuke them all
    @Query("DELETE FROM dots WHERE uuid = :uuid AND time<:time")
    void removeDots(String uuid,long time);

    // Conta il numero di tutte le righe
    @Query("SELECT COUNT(*) FROM dots WHERE model!='LAST_STATE'")
    Integer num_rows();

    // Utilizzato per la verifica dell'esistenza di determinati punti (LAST_STATE)
    // LAST_STATE -> Punto che indica l'ultimo indirizzo dello Switch nella UI
    @Query("SELECT x FROM dots WHERE model ='LAST_STATE'")
    Double getSwitchState();

}
