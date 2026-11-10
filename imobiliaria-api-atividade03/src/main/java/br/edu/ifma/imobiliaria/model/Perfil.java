package br.edu.ifma.imobiliaria.model;
import jakarta.persistence.*;

@Entity
@Table(name="perfis")
public class Perfil {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, unique=true, length=40)
    private String nome;
    @Column(nullable=false, length=200)
    private String descricao;
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome=nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao=descricao; }
}
