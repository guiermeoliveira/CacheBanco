public class Pessoa {
    private final int id;
    private String nome;
    private int idade;

    public Pessoa(int id, String nome, int idade) {
        validarNome(nome);
        validarIdade(idade);
        this.id = id;
        this.nome = nome.trim();
        this.idade = idade;
    }

    // Método encapsulado para atualizar tudo com validação interna
    public void atualizarDados(String novoNome, Integer novaIdade) {
        if (novoNome != null && !novoNome.isBlank()) {
            validarNome(novoNome);
            this.nome = novoNome.trim();
        }
        if (novaIdade != null) {
            validarIdade(novaIdade);
            this.idade = novaIdade;
        }
    }

    // Setters diretos adicionados para resolver o erro "cannot find symbol"
    public void setNome(String nome) {
        validarNome(nome);
        this.nome = nome.trim();
    }

    public void setIdade(int idade) {
        validarIdade(idade);
        this.idade = idade;
    }

    private void validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser vazio.");
        }
    }

    private void validarIdade(int idade) {
        if (idade < 0 || idade > 130) {
            throw new IllegalArgumentException("Idade deve estar entre 0 e 130.");
        }
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getIdade() {
        return idade;
    }

    @Override
    public String toString() {
        return "[ID: " + id + " | Nome: " + nome + " | Idade: " + idade + "]";
    }
}
