package br.edu.ifma.imobiliaria.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name="clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="nome_cliente", nullable=false, length=255)
    private String nomeCliente;

    @Column(nullable=false, unique=true, length=11)
    private String cpf;

    @Column(nullable=false, length=12)
    private String telefone;

    @Column(nullable=false, length=100)
    private String email;

    @Column(name="dt_nascimento", nullable=false)
    private LocalDate dtNascimento;

    public Long getId() { return id; }
    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDate getDtNascimento() { return dtNascimento; }
    public void setDtNascimento(LocalDate dtNascimento) { this.dtNascimento = dtNascimento; }
}
