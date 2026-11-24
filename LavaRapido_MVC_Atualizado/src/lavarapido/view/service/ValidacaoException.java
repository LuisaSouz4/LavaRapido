package lavarapido.service;

/** Erro de regra de negócio/validação; a mensagem é exibida ao usuário. */
public class ValidacaoException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public ValidacaoException(String mensagem) { super(mensagem); }
}
