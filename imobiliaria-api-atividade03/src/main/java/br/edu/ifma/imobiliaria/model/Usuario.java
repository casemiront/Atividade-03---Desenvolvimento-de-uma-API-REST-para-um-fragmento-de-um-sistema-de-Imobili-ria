package br.edu.ifma.imobiliaria.model;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="usuarios")
public class Usuario {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, unique=true, length=60)
    private String login;
    @Column(nullable=false, length=100)
    private String nome;
    @JsonIgnore
    @Column(name="senha_hash", nullable=false, length=100)
    private String senhaHash;
    @Column(nullable=false)
    private boolean ativo;
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="id_cliente")
    private Cliente cliente;
    @ManyToMany
    @JoinTable(name="usuario_perfis", joinColumns=@JoinColumn(name="id_usuario"),
        inverseJoinColumns=@JoinColumn(name="id_perfil"))
    private Set<Perfil> perfis=new HashSet<>();
    public Long getId() { return id; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login=login; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome=nome; }
    @JsonIgnore
    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash=senhaHash; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo=ativo; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente=cliente; }
    public Set<Perfil> getPerfis() { return perfis; }
    public void setPerfis(Set<Perfil> perfis) { this.perfis=perfis; }
}
