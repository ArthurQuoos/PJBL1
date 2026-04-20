package com.pucpr.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pucpr.model.Usuario;

//===============================
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioRepository {
    private final String FILE_PATH = "usuarios.json";
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Busca um usuário pelo e-mail dentro do arquivo JSON.
     * * TODO: O ALUNO DEVE IMPLEMENTAR:
     * 1. Carregar a lista completa de usuários usando o método findAll().
     * 2. Utilizar Java Streams para encontrar o primeiro usuário que possua o e-mail informado.
     * 3. Importante: A comparação de e-mail deve ser 'case-insensitive' (ignorar maiúsculas/minúsculas).
     * 4. Retornar um Optional.of(usuario) se encontrar, ou Optional.empty() se não existir.
     */
    
    public Optional<Usuario> findByEmail(String email) {
        //findAll() retorna a lista Usuario e o for procura na lista se há algum usuário com o mesmo email do que estou procurando.
        for (Usuario u : findAll()){
            if(u.getEmail().equalsIgnoreCase(email)){
                return Optional.of(u);
            }
        }
        return Optional.empty();
    }

    /**
     * Retorna todos os usuários cadastrados no arquivo JSON.
     * * TODO: O ALUNO DEVE IMPLEMENTAR:
     * 1. Verificar se o arquivo definido em 'FILE_PATH' existe no sistema.
     * 2. Se o arquivo NÃO existir, deve retornar uma lista vazia (new ArrayList<>()) para evitar erros.
     * 3. Se existir, usar o 'mapper.readValue' do Jackson para converter o conteúdo do arquivo
     * em uma List<Usuario>. Dica: Use 'new TypeReference<List<Usuario>>(){}'.
     */
    public List<Usuario> findAll() {
        //Aponta para o Json com os usuarios
        File file = new File(FILE_PATH);
        //Verifica se existe o arquivo json usuarios, se não existir retorna uma lista vazia, se existir pula esse if e segue rodando o código.
        if (!file.exists()){
            return new ArrayList<>();
        }
        //Jackson lê o Json e converte o Json na Lista Usuario.  //TypeReference serve para o Jackson saber das informações que se tratam do Json.
        try{
            return mapper.readValue(file, new TypeReference<List<Usuario>>(){});
        }catch(IOException e){
            return new ArrayList<>();
        }
    }

    /**
     * Salva um novo usuário no arquivo JSON.
     * * TODO: O ALUNO DEVE IMPLEMENTAR:
     * 1. Obter a lista atual de usuários através do findAll().
     * 2. Verificar se o e-mail do novo usuário já está cadastrado (Regra de Negócio).
     * 3. Adicionar o novo objeto à lista.
     * 4. Utilizar 'mapper.writerWithDefaultPrettyPrinter().writeValue' para gravar a lista
     * atualizada no arquivo, garantindo que o JSON fique legível (formatado).
     */
    public void save(Usuario usuario) throws IOException {
        // Implementar lógica de persistência
        
        //Carrega o arquivo da lista de Usuario.
        List<Usuario> lista = findAll();

        //Variavel para verificar se o email já existe, usuario.getEmail() pega o email que está sendo usado para cadastrar,
        //findByEmail procura este email na lista e retorna um Optional, isPresent() verifica se tem algo dentro do Optional, se sim o email já existe.
        boolean emailExiste = findByEmail(usuario.getEmail()).isPresent();
        if (emailExiste){
            throw new IllegalArgumentException("Email já cadastrado.");
        }

        lista.add(usuario);
        //Formata o Json 
        mapper.writerWithDefaultPrettyPrinter()
              .writeValue(new File(FILE_PATH), lista); //Converte a linha em Json e salva no arquivo.
    }
}