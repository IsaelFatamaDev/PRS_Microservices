package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.exception.custom;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ErrorServidorException extends RuntimeException {

    public ErrorServidorException(String mensaje) {
        super(mensaje);
    }

    public ErrorServidorException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}