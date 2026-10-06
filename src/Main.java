import static java.lang.IO.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Main {
    private static final int LIMITE_CACHE = 10;
    private static int proximoId = 6;

    public static void main(String[] args) {
        // Banco de dados inicial mockado
        List<Pessoa> banco = new ArrayList<>();
        banco.add(new Pessoa(1, "Ana Silva", 28));
        banco.add(new Pessoa(2, "Bruno Costa", 34));
        banco.add(new Pessoa(3, "Carla Souza", 22));
        banco.add(new Pessoa(4, "Diego Lima", 41));
        banco.add(new Pessoa(5, "Elena Martins", 29));;

        // Cache gerenciado com LinkedList sob política LRU
        LinkedList<Pessoa> cache = new LinkedList<>();

        boolean rodando = true;

        while (rodando) {
            println("\n================ GERENCIADOR DE PESSOAS (LRU) ================");
            println("1. Consultar por ID");
            println("2. Incluir nova pessoa");
            println("3. Atualizar pessoa");
            println("4. Excluir pessoa");
            println("5. Ver estado do Cache (" + cache.size() + "/" + LIMITE_CACHE + ")");
            println("6. Listar banco completo");
            println("0. Sair");
            print("Escolha uma opção: ");

            String opcao = readln();
            if (opcao == null) break;

            switch (opcao.trim()) {
                case "1" -> consultarPessoa(banco, cache);
                case "2" -> incluirPessoa(banco);
                case "3" -> atualizarPessoa(banco, cache);
                case "4" -> excluirPessoa(banco, cache);
                case "5" -> exibirCache(cache);
                case "6" -> exibirBanco(banco);
                case "0" -> {
                    println("Encerrando o sistema...");
                    rodando = false;
                }
                default -> println("Opção inválida! Tente novamente.");
            }
        }
    }

    // 1. CONSULTAR (LRU: acerto move o item para o final da fila)
    private static void consultarPessoa(List<Pessoa> banco, LinkedList<Pessoa> cache) {
        int id = lerInteiro("Digite o ID para consulta: ");
        if (id == -1) return;

        // 1.1 Tenta localizar no cache
        Pessoa encontradaNoCache = null;
        for (int i = 0; i < cache.size(); i++) {
            if (cache.get(i).getId() == id) {
                encontradaNoCache = cache.remove(i); // Remove da posição atual
                break;
            }
        }

        if (encontradaNoCache != null) {
            // LRU Hit: recoloca no fim (agora é o mais recentemente usado)
            cache.addLast(encontradaNoCache);
            println("-> [CACHE HIT - LRU] Pessoa acessada e promovida ao topo recente: " + encontradaNoCache);
            return;
        }

        // 1.2 Cache Miss: busca no banco
        Pessoa encontradaNoBanco = buscarPorId(banco, id);
        if (encontradaNoBanco != null) {
            if (cache.size() >= LIMITE_CACHE) {
                // Remove o menos recentemente usado (Least Recently Used) da ponta inicial
                Pessoa despejada = cache.removeFirst();
                println("-> [LRU EVICTION] Cache cheio! Menos recentemente acessado removido: " + despejada.getNome());
            }
            cache.addLast(encontradaNoBanco);
            println("-> [CACHE MISS] Pessoa buscada no banco e adicionada como mais recente: " + encontradaNoBanco);
        } else {
            println("-> Registro com ID " + id + " não existe nem no cache, nem no banco.");
        }
    }

    // 2. INCLUIR (Salva no banco)
    private static void incluirPessoa(List<Pessoa> banco) {
        print("Digite o nome da pessoa: ");
        String nome = readln();
        if (nome == null || nome.isBlank()) {
            println("Nome inválido!");
            return;
        }

        int idade = lerInteiro("Digite a idade: ");
        if (idade < 0) {
            println("Idade inválida!");
            return;
        }

        Pessoa novaPessoa = new Pessoa(proximoId++, nome.trim(), idade);
        banco.add(novaPessoa);
        println("-> Pessoa cadastrada no banco: " + novaPessoa);
    }

    // 3. ATUALIZAR (Sincroniza e renova a prioridade LRU no cache)
    private static void atualizarPessoa(List<Pessoa> banco, LinkedList<Pessoa> cache) {
        int id = lerInteiro("Digite o ID da pessoa a atualizar: ");
        if (id == -1) return;

        Pessoa pessoaBanco = buscarPorId(banco, id);
        if (pessoaBanco == null) {
            println("-> Pessoa não encontrada no banco.");
            return;
        }

        print("Novo nome (Enter para manter '" + pessoaBanco.getNome() + "'): ");
        String novoNome = readln();
        if (novoNome != null && !novoNome.isBlank()) {
            pessoaBanco.setNome(novoNome.trim());
        }

        print("Nova idade (Enter para manter " + pessoaBanco.getIdade() + "): ");
        String novaIdadeStr = readln();
        if (novaIdadeStr != null && !novaIdadeStr.isBlank()) {
            try {
                int novaIdade = Integer.parseInt(novaIdadeStr.trim());
                if (novaIdade >= 0) pessoaBanco.setIdade(novaIdade);
            } catch (NumberFormatException e) {
                println("Idade inválida mantida.");
            }
        }

        // Se está no cache, atualiza dados e move para o fim da fila (acesso recente)
        Pessoa encontradaNoCache = null;
        for (int i = 0; i < cache.size(); i++) {
            if (cache.get(i).getId() == id) {
                encontradaNoCache = cache.remove(i);
                break;
            }
        }

        if (encontradaNoCache != null) {
            encontradaNoCache.setNome(pessoaBanco.getNome());
            encontradaNoCache.setIdade(pessoaBanco.getIdade());
            cache.addLast(encontradaNoCache);
            println("-> Dados atualizados no banco e promovidos ao topo recente do cache!");
        } else {
            println("-> Dados atualizados no banco.");
        }
    }

    // 4. EXCLUIR (Remove do banco e expurga do cache)
    private static void excluirPessoa(List<Pessoa> banco, LinkedList<Pessoa> cache) {
        int id = lerInteiro("Digite o ID da pessoa a remover: ");
        if (id == -1) return;

        Pessoa removida = null;
        for (int i = 0; i < banco.size(); i++) {
            if (banco.get(i).getId() == id) {
                removida = banco.remove(i);
                break;
            }
        }

        if (removida != null) {
            cache.removeIf(p -> p.getId() == id);
            println("-> Pessoa " + removida.getNome() + " excluída do banco e expurgada do cache.");
        } else {
            println("-> ID " + id + " não encontrado para exclusão.");
        }
    }

    // 5. VISUALIZAR CACHE (Início = LRU / Fim = MRU)
    private static void exibirCache(LinkedList<Pessoa> cache) {
        println("\n--- ESTADO DO CACHE LRU ---");
        if (cache.isEmpty()) {
            println("[Cache vazio]");
            return;
        }

        println("Posição 1 = Próximo a ser descartado (LRU)");
        println("Posição " + cache.size() + " = Mais recente acessado (MRU)\n");

        for (int i = 0; i < cache.size(); i++) {
            String tag = (i == 0) ? " <- [Mais antigo / Próximo a sair]" :
                    (i == cache.size() - 1) ? " <- [Mais recente]" : "";
            println((i + 1) + ". " + cache.get(i) + tag);
        }
    }

    // 6. LISTAR BANCO
    private static void exibirBanco(List<Pessoa> banco) {
        println("\n--- REGISTROS NO BANCO DE DADOS (" + banco.size() + ") ---");
        for (Pessoa p : banco) {
            println(p.toString());
        }
    }

    private static Pessoa buscarPorId(List<Pessoa> lista, int id) {
        for (Pessoa p : lista) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    private static int lerInteiro(String prompt) {
        print(prompt);
        String entrada = readln();
        if (entrada == null) return -1;
        try {
            return Integer.parseInt(entrada.trim());
        } catch (NumberFormatException e) {
            println("Erro: Entrada não é um número válido!");
            return -1;
        }
    }
}