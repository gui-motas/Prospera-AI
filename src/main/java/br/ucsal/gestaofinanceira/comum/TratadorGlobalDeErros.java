package br.ucsal.gestaofinanceira.comum;

import br.ucsal.gestaofinanceira.planejamento.IAIndisponivelException;
import br.ucsal.gestaofinanceira.planejamento.LimiteDeAnaliseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converte exceções em respostas JSON no formato {@code {"erro": "..."}}.
 */
@RestControllerAdvice
public class TratadorGlobalDeErros extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(TratadorGlobalDeErros.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        String primeira = campos.isEmpty() ? "Dados inválidos." : campos.values().iterator().next();
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("erro", primeira);
        corpo.put("campos", campos);
        return ResponseEntity.badRequest().body(corpo);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        return ResponseEntity.badRequest().body(Map.of("erro", "Formato de dados inválido na requisição."));
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Map<String, Object>> regraNegocio(RegraNegocioException ex) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> naoEncontrado(RecursoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** RNF-09: a falha da IA é informada sem afetar as demais funcionalidades. */
    @ExceptionHandler(IAIndisponivelException.class)
    public ResponseEntity<Map<String, Object>> iaIndisponivel(IAIndisponivelException ex) {
        LOG.warn("Recurso de IA indisponível: {}", ex.getMotivoTecnico());
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("erro", IAIndisponivelException.MENSAGEM_USUARIO);
        corpo.put("iaDisponivel", false);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(corpo);
    }

    @ExceptionHandler(LimiteDeAnaliseException.class)
    public ResponseEntity<Map<String, Object>> limiteDeAnalise(LimiteDeAnaliseException ex) {
        return resposta(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<Map<String, Object>> naoAutenticado(AuthenticationCredentialsNotFoundException ex) {
        return resposta(HttpStatus.UNAUTHORIZED, "Faça login para continuar.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> acessoNegado(AccessDeniedException ex) {
        return resposta(HttpStatus.FORBIDDEN, "Você não tem permissão para esta ação.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> parametroInvalido(MethodArgumentTypeMismatchException ex) {
        return resposta(HttpStatus.BAD_REQUEST, "Parâmetro inválido: " + ex.getName() + ".");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> integridade(DataIntegrityViolationException ex) {
        return resposta(HttpStatus.CONFLICT, "A operação conflita com dados já cadastrados.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> inesperado(Exception ex) {
        LOG.error("Erro inesperado", ex);
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente em instantes.");
    }

    private static ResponseEntity<Map<String, Object>> resposta(HttpStatus status, String mensagem) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("erro", mensagem);
        return ResponseEntity.status(status).body(corpo);
    }
}
