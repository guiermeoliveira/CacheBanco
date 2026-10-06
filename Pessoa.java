import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Pessoa {

    private int id;
    private String nome;
    public int idade;

    public Pessoa(int id, String nome, int idade){
        this.id = id;
        this.idade = idade;
        this.nome = nome;
    }

    public int getId(){
        return id;
    }

    public String getNome(){
        return nome;
    }

    public int getIdade() {
        return idade;
    }

    @Override
    public

    @Override
    public String toString(){
        return "ID: " + id + "Nome: " + nome + "Idade: " + idade;
    }
}
