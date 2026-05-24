package com.mycompany.tracklify.utils;

import com.mycompany.tracklify.dao.RegistroHabitoDAO;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Cálculo de racha de cumplimiento a partir de días devueltos por la vista SQL.
 *
 * @author Tracklify
 */
public class RachaService {

    private final RegistroHabitoDAO registroHabitoDAO;

    /** Crea el servicio con un {@link RegistroHabitoDAO} por defecto. */
    public RachaService() {
        this.registroHabitoDAO = new RegistroHabitoDAO();
    }

    /**
     * @param registroHabitoDAO DAO usado para consultar días cumplidos
     */
    public RachaService(RegistroHabitoDAO registroHabitoDAO) {
        this.registroHabitoDAO = registroHabitoDAO;
    }

    /**
     * Cuenta días consecutivos con cumplimiento desde hoy hacia el pasado.
     *
     * @param idHabito identificador del hábito
     * @return número de días de racha
     */
    public int calcularRachaActual(int idHabito) {
        List<LocalDate> dias = registroHabitoDAO.obtenerDiasCumplidos(idHabito);
        if (dias.isEmpty()) {
            return 0;
        }
        Collections.sort(dias);
        Collections.reverse(dias);

        Set<LocalDate> conjunto = new HashSet<>(dias);
        LocalDate cursor = LocalDate.now();
        int racha = 0;
        while (conjunto.contains(cursor)) {
            racha++;
            cursor = cursor.minusDays(1);
        }
        return racha;
    }
}
