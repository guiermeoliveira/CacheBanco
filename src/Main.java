import static java.lang.IO.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Main {
    private static final int LIMITE_CACHE = 10;
    private static int proximoId = 13;

    public static void main(String[] args) {
        // Mock inicial de registros no banco
        List<Pessoa> banco = new ArrayList<>();
        banco.add(new Pessoa(1, "Ana Silva", 28));
        banco.add(new Pessoa(2, "Bruno Costa", 34));
        banco.add(new Pessoa(3, "Carla Souza", 22));
        banco.add(new Pessoa(4, "Diego Lima", 41));
        banco.add(new Pessoa(5, "Elena Martins", 29));
        banco.add(new Pessoa(6, "Felipe Rocha", 31));
        banco.add(new Pessoa(7, "Gabriela Duarte", 26));
        banco.add(new Pessoa(8, "Hugo Ribeiro", 38));
        banco.add(new Pessoa(9, "Isabela Gomes", 24));
        banco.add(new Pessoa(10, "João Pedro", 45));
        banco.add(new Pessoa(11, "Larissa Melo", 30));
        banco.add(new Pessoa(12, "Marcos Vinicius", 27));

        // Cache LRU gerenciado por LinkedList (cabeça = mais antigo / cauda = mais recente)
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

    // 1. READ (Cache Hit move para o fim; Cache Miss busca no banco e expulsa o primeiro se lotado)
    private static void consultarPessoa(List<Pessoa> banco, LinkedList<Pessoa> cache) {
        int id = lerInteiro("Digite o ID para consulta: ");
        if (id == -1) return;

        Pessoa encontradaNoCache = null;
        for (int i = 0; i < cache.size(); i++) {
            if (cache.get(i).getId() == id) {
                encontradaNoCache = cache.remove(i);
                break;
            }
        }

        if (encontradaNoCache != null) {
            cache.addLast(encontradaNoCache);
            println("-> [CACHE HIT - LRU] Promovido ao topo recente: " + encontradaNoCache);
            return;
        }

        Pessoa encontradaNoBanco = buscarPorId(banco, id);
        if (encontradaNoBanco != null) {
            if (cache.size() >= LIMITE_CACHE) {
                Pessoa despejada = cache.removeFirst();
                println("-> [LRU EVICTION] Cache cheio! Menos recente removido: " + despejada.getNome());
            }
            cache.addLast(encontradaNoBanco);
            println("-> [CACHE MISS] Buscado no banco e adicionado ao cache: " + encontradaNoBanco);
        } else {
            println("-> Registro com ID " + id + " não encontrado.");
        }
    }

    // 2. CREATE (Insere no banco)
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
        println("-> Cadastrado com sucesso no banco: " + novaPessoa);
    }

    // 3. UPDATE (Corrige os erros das linhas 124, 132, 148 e 149)
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
            pessoaBanco.setNome(novoNome.trim()); // Linha 124 resolvida
        }

        print("Nova idade (Enter para manter " + pessoaBanco.getIdade() + "): ");
        String novaIdadeStr = readln();
        if (novaIdadeStr != null && !novaIdadeStr.isBlank()) {
            try {
                int novaIdade = Integer.parseInt(novaIdadeStr.trim());
                if (novaIdade >= 0) {
                    pessoaBanco.setIdade(novaIdade); // Linha 132 resolvida
                }
            } catch (NumberFormatException e) {
                println("Idade inválida; valor anterior mantido.");
            }
        }

        // Sincroniza no cache caso esteja carregado
        Pessoa encontradaNoCache = null;
        for (int i = 0; i < cache.size(); i++) {
            if (cache.get(i).getId() == id) {
                encontradaNoCache = cache.remove(i);
                break;
            }
        }

        if (encontradaNoCache != null) {
            encontradaNoCache.setNome(pessoaBanco.getNome());   // Linha 148 resolvida
            encontradaNoCache.setIdade(pessoaBanco.getIdade()); // Linha 149 resolvida
            cache.addLast(encontradaNoCache); // Promovido ao topo recente
            println("-> Dados sincronizados no cache e atualizados no banco!");
        } else {
            println("-> Dados atualizados no banco.");
        }
    }

    // 4. DELETE (Remove do banco e expulsa do cache)
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
            println("-> ID " + id + " não encontrado.");
        }
    }

    // 5. LISTAR CACHE
    private static void exibirCache(LinkedList<Pessoa> cache) {
        println("\n--- FILA DO CACHE LRU (" + cache.size() + "/" + LIMITE_CACHE + ") ---");
        if (cache.isEmpty()) {
            println("[Vazio]");
            return;
        }
        for (int i = 0; i < cache.size(); i++) {
            String etiqueta = (i == 0) ? " <- [Mais antigo / Primeiro a sair se lotar]" :
                              (i == cache.size() - 1) ? " <- [Mais recentemente acessado]" : "";
            println((i + 1) + ". " + cache.get(i) + etiqueta);
        }
    }

    // 6. LISTAR BANCO
    private static void exibirBanco(List<Pessoa> banco) {
        println("\n--- REGISTROS TOTAIS NO BANCO (" + banco.size() + ") ---");
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

    private static int lerInteiro(String rotulo) {
        print(rotulo);
        String entrada = readln();
        if (entrada == null) return -1;
        try {
            return Integer.parseInt(entrada.trim());
        } catch (NumberFormatException e) {
            println("Entrada inválida. Digite um número inteiro.");
            return -1;
        }
    }
}
