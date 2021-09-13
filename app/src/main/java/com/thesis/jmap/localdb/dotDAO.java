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
    void add_dot(Dot dot);

    // Prende tutti i punti nel db
    @Query("SELECT * FROM dots")
    List<Dot> getall();

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

    // Il punto avente (model = 'LAST_DOT') è un punto particolare:
    // 1. Indica l'ultimo ID inviato al database SQL remoto
    // 2. Tutti i punti avente (ID<LAST_DOT.ID) sono eliminabili (Possibile ottibilizzazione)

    // Utilizzato per la verifica dell'esistenza del LAST_DOT
    @Query("SELECT COUNT(*) FROM dots WHERE model ='LAST_DOT'")
    List<Integer> getlastdot();

    // Aggiorna l'ultimo ID trasmesso in remoto nel LAST_DOT
    @Query("UPDATE dots SET 'id'=:id")
    void update_lastdot(int id);

}
