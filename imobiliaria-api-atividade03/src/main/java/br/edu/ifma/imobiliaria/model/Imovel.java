package br.edu.ifma.imobiliaria.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name="imoveis")
public class Imovel {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="tipo_imovel", nullable=false, length=50)
    private String tipoImovel;

    @Column(nullable=false, length=255)
    private String endereco;

    @Column(nullable=false, length=10)
    private String cep;

    @Column(nullable=false)
    private Integer dormitorios;

    @Column(nullable=false)
    private Integer banheiros;

    @Column(nullable=false)
    private Integer suites;

    @Column(nullable=false)
    private Integer metragem;

    @Column(name="valor_aluguel_sug", nullable=false, precision=12, scale=2)
    private BigDecimal valorAluguelSug;

    @Column(columnDefinition="TEXT")
    private String obs;

    @Column(nullable=false, length=100)
    private String cidade;

    @Column(name="valor_diaria", nullable=false, precision=12, scale=2)
    private BigDecimal valorDiaria;

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public BigDecimal getValorDiaria() { return valorDiaria; }
    public void setValorDiaria(BigDecimal valorDiaria) { this.valorDiaria = valorDiaria; }

    public Long getId() { return id; }
    public String getTipoImovel() { return tipoImovel; }
    public void setTipoImovel(String tipoImovel) { this.tipoImovel = tipoImovel; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
    public Integer getDormitorios() { return dormitorios; }
    public void setDormitorios(Integer dormitorios) { this.dormitorios = dormitorios; }
    public Integer getBanheiros() { return banheiros; }
    public void setBanheiros(Integer banheiros) { this.banheiros = banheiros; }
    public Integer getSuites() { return suites; }
    public void setSuites(Integer suites) { this.suites = suites; }
    public Integer getMetragem() { return metragem; }
    public void setMetragem(Integer metragem) { this.metragem = metragem; }
    public BigDecimal getValorAluguelSug() { return valorAluguelSug; }
    public void setValorAluguelSug(BigDecimal valorAluguelSug) { this.valorAluguelSug = valorAluguelSug; }
    public String getObs() { return obs; }
    public void setObs(String obs) { this.obs = obs; }
}
