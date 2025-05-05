package br.com.soejin.framework.security_guard.configs;

import br.com.soejin.framework.security_guard.controller.response.ErrorResponse;
import br.com.soejin.framework.security_guard.exception.AlreadyDataRegisterException;
import br.com.soejin.framework.security_guard.exception.BadCredentialsException;
import br.com.soejin.framework.security_guard.exception.NotFoundException;
import br.com.soejin.framework.security_guard.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manipulador global de exceções para a aplicação.
 * Esta classe centraliza o tratamento de exceções, fornecendo respostas padronizadas
 * para diferentes tipos de erros que podem ocorrer durante a execução da aplicação.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Trata exceções de credenciais inválidas.
     *
     * @param ex A exceção de credenciais inválidas
     * @return ResponseEntity com detalhes do erro
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                        HttpStatus.UNAUTHORIZED.value(),
                        ex.getMessage(),
                        LocalDateTime.now()));
    }

    /**
     * Trata exceções de credenciais inválidas do Spring Security.
     *
     * @param ex A exceção de credenciais inválidas do Spring Security
     * @return ResponseEntity com detalhes do erro
     */
    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleSpringBadCredentialsException(
            org.springframework.security.authentication.BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                        HttpStatus.UNAUTHORIZED.value(),
                        "Credenciais inválidas",
                        LocalDateTime.now()));
    }

    /**
     * Trata exceções de dados já registrados.
     *
     * @param ex A exceção de dados já registrados
     * @return ResponseEntity com detalhes do erro
     */
    @ExceptionHandler(AlreadyDataRegisterException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyDataRegisterException(AlreadyDataRegisterException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage(),
                        LocalDateTime.now()));
    }

    /**
     * Trata exceções de recurso não encontrado.
     *
     * @param ex A exceção de recurso não encontrado
     * @return ResponseEntity com detalhes do erro
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage(),
                        LocalDateTime.now()));
    }

    /**
     * Trata exceções de usuário não encontrado.
     *
     * @param ex A exceção de usuário não encontrado
     * @return ResponseEntity com detalhes do erro
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage(),
                        LocalDateTime.now()));
    }

    /**
     * Trata exceções de validação de argumentos de método.
     *
     * @param ex A exceção de validação
     * @return ResponseEntity com detalhes do erro
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Erro de validação: " + errors,
                        LocalDateTime.now()));
    }

    /**
     * Trata exceções genéricas não capturadas por outros handlers.
     *
     * @param ex A exceção genérica
     * @return ResponseEntity com detalhes do erro
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Erro interno do servidor: " + ex.getMessage(),
                        LocalDateTime.now()));
    }
}