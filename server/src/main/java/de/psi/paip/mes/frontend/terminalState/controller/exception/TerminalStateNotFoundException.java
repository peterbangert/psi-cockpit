package de.psi.paip.mes.frontend.terminalState.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TerminalStateNotFoundException extends RuntimeException {
}
