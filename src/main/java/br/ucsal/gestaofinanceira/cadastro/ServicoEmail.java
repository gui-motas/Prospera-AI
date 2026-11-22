package br.ucsal.gestaofinanceira.cadastro;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

/**
 * Envia o e-mail de confirmação de cadastro. Se o SMTP não estiver configurado
 * (ambiente de desenvolvimento), o link é registrado no log do backend.
 */
@Service
public class ServicoEmail {

    private static final Logger LOG = LoggerFactory.getLogger(ServicoEmail.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final String remetente;

    public ServicoEmail(ObjectProvider<JavaMailSender> mailSender,
                        @Value("${app.email.remetente}") String remetente) {
        this.mailSender = mailSender;
        this.remetente = remetente;
    }

    @Async
    public void enviarConfirmacao(String destinatario, String nome, String link) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            LOG.warn("SMTP não configurado. Link de confirmação de cadastro: {}", link);
            return;
        }
        try {
            MimeMessage mensagem = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensagem, "UTF-8");
            helper.setFrom(remetente);
            helper.setTo(destinatario);
            helper.setSubject("Confirme seu cadastro – Gestão Financeira Pessoal");
            helper.setText(montarHtml(nome, link), true);
            sender.send(mensagem);
        } catch (MessagingException | RuntimeException e) {
            LOG.error("Falha ao enviar e-mail de confirmação. Link: {}", link, e);
        }
    }

    private static String montarHtml(String nome, String link) {
        String nomeSeguro = HtmlUtils.htmlEscape(nome);
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <body style="margin:0;padding:32px;background:#f6f7f8;font-family:Arial,Helvetica,sans-serif;color:#0f172a">
                  <div style="max-width:560px;margin:0 auto;background:#ffffff;border-radius:16px;overflow:hidden">
                    <div style="background:linear-gradient(135deg,#137fec,#1A3A4D);padding:28px;color:#ffffff">
                      <h1 style="margin:0;font-size:20px">Plataforma de Gestão Financeira Pessoal</h1>
                      <p style="margin:6px 0 0;opacity:.85">com Planejamento Inteligente de Metas</p>
                    </div>
                    <div style="padding:28px">
                      <p style="font-size:18px;margin-top:0">Olá, %s!</p>
                      <p>Para ativar sua conta, confirme seu e-mail clicando no botão abaixo.</p>
                      <p style="text-align:center;margin:32px 0">
                        <a href="%s" style="background:#137fec;color:#ffffff;padding:14px 32px;border-radius:10px;text-decoration:none;font-weight:bold">Confirmar e-mail</a>
                      </p>
                      <p style="font-size:13px;color:#64748b">O link expira em 24 horas. Se você não criou esta conta, ignore este e-mail.</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(nomeSeguro, link);
    }
}
