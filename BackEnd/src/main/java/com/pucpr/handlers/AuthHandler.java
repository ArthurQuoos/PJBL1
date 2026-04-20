package com.pucpr.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pucpr.dto.LoginRequest;
import com.pucpr.dto.RegisterRequest;
import com.pucpr.model.Usuario;
import com.pucpr.repository.UsuarioRepository;
import com.pucpr.service.JwtService;
import com.sun.net.httpserver.HttpExchange;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

/**
 * Classe responsável por gerenciar as requisições de Autenticação.
 * Aqui o aluno aprenderá a manipular o corpo de requisições HTTP e
 * aplicar conceitos de hashing e proteção de dados.
 */
public class AuthHandler {
    private final UsuarioRepository repository;
    private final JwtService jwtService;
    private final ObjectMapper mapper = new ObjectMapper();

    public AuthHandler(UsuarioRepository repository, JwtService jwtService) {
        this.repository = repository;
        this.jwtService = jwtService;
    }

    // Método auxiliar: monta e nevia a resposta Json
    private void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        //Headers necessarios para o frontend conseguir se comunicar com a API
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        //converte o objeto Java para uma String JSON e envia
        String json = mapper.writeValueAsString(body);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Gerencia o processo de Login.
     * Objetivo: Validar credenciais e emitir um passaporte (JWT).
     */
    public void handleLogin(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 204, Map.of());
            return;
        }
        
        // DICA DIDÁTICA: Em APIs REST, o Login sempre deve ser POST para
        // garantir que a senha viaje no corpo (body) e não na URL.
        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
            return;
        }

        // TODO: O ALUNO DEVE IMPLEMENTAR OS SEGUINTES PASSOS:

        // 1. EXTRAÇÃO: Use exchange.getRequestBody() para ler os bytes do JSON enviado.
        // 2. CONVERSÃO: Transforme esse JSON em um objeto (ex: LoginRequest) usando Jackson.
        LoginRequest req = mapper.readValue(exchange.getRequestBody(), LoginRequest.class);


        // 3. BUSCA E SEGURANÇA:
        //    a) Busque o usuário no 'repository' pelo e-mail fornecido.
        Optional<Usuario> usuarioOpt = repository.findByEmail(req.email);
        //    b) Se existir, use BCrypt.checkpw(senhaInformada, senhaDoArquivo) para validar.
        if(usuarioOpt.isEmpty() || !BCrypt.checkpw(req.password, usuarioOpt.get().getSenhaHash())) {
            //    c) Se o usuário não existir ou a senha for inválida, retorne 401 Unauthorized.
            sendJson(exchange, 401, Map.of("message", "E-mail ou senha inválidos"));
            return;
        }
        String token = jwtService.generateToken(usuarioOpt.get());
        sendJson(exchange, 200, Map.of("token", token));
        // 4. REGRA DE OURO DA SEGURANÇA:
        //    - NUNCA use .equals() ou == para comparar senhas. O BCrypt é a sugestão.
        //    - Em caso de falha, retorne uma mensagem GENÉRICA (ex: "E-mail ou senha inválidos").
        //      Revelar qual dos dois está errado ajuda atacantes em técnicas de enumeração.

        // 5. RESPOSTA:
        //    - Se as credenciais estiverem OK: Gere o Token via jwtService e retorne 200 OK.
        //    - Se falhar: Retorne 401 Unauthorized com o JSON de erro.
    }   

    /**
     * Gerencia o processo de Cadastro (Registro).
     * Objetivo: Criar um novo usuário de forma segura.
     */
    public void handleRegister(HttpExchange exchange) throws IOException {
        //Trata o preflight CORS
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 204, Map.of());
            return;
        }

        if (!"POST".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        // le o body e convert para um objeto Java (ex: RegisterRequest)
        RegisterRequest req = mapper.readValue(exchange.getRequestBody(), RegisterRequest.class);
        // TODO: O ALUNO DEVE IMPLEMENTAR OS SEGUINTES PASSOS:

        // 1. VALIDAÇÃO DE EXISTÊNCIA:
        //    Antes de cadastrar, verifique se o e-mail já está em uso no 'repository'.
        //    Se já existir, interrompa e retorne 400 Bad Request.
        if(repository.findByEmail(req.email).isPresent()){
            sendJson(exchange,400, Map.of("message", "E-mail já cadastrado"));
            return;
        }

        // 2. CRIPTOGRAFIA (Hashing):
        //    A senha recebida NUNCA deve chegar ao arquivo em texto claro.
        //    Gere o hash: BCrypt.hashpw(senhaPura, BCrypt.gensalt(12)).
        //    O "salt" (fator 12) protege contra ataques de Rainbow Tables.
            String hashedPassword = BCrypt.hashpw(req.password, BCrypt.gensalt(12));


        // 3. PERSISTÊNCIA:
        //    Crie uma nova instância de Usuario (model) com a senha já HASHEADA.
        //    Use o repository.save(novoUsuario) para gravar no arquivo JSON.
        Usuario novoUsuario = new Usuario(req.name, req.email, hashedPassword, "USER");
                repository.save(novoUsuario);
        // 4. RESPOSTA: Se tudo der certo, retorne 201 Created.
        sendJson(exchange, 201, Map.of("message", "Usuário registrado com sucesso"));
    }
}