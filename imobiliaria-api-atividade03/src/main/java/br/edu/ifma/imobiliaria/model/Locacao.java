package br.edu.ifma.imobiliaria.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name="locacao")
public class Locacao {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="id_imovel", nullable=false)
    private Imovel imovel;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="id_inquilino", nullable=false)
    private Cliente inquilino;

    @Column(nullable=false)
    private Boolean ativo;

    @Column(name="data_fim", nullable=false)
    private LocalDate dataFim;

    @Column(name="data_inicio", nullable=false)
    private LocalDate dataInicio;

    @Column(name="dia_vencimento", nullable=false)
    private Integer diaVencimento;

    @Column(name="perc_multa", nullable=false, precision=5, scale=2)
    private BigDecimal percMulta;

    @Column(name="valor_aluguel", nullable=false, precision=12, scale=2)
    private BigDecimal valorAluguel;

    @Column(columnDefinition="TEXT")
    private String obs;

    @Column(name="valor_diaria_aplicada", nullable=false, precision=12, scale=2)
    private BigDecimal valorDiariaAplicada;

    public BigDecimal getValorDiariaAplicada() { return valorDiariaAplicada; }
    public void setValorDiariaAplicada(BigDecimal valor) { this.valorDiariaAplicada = valor; }

    public Long getId() { return id; }
    public Imovel getImovel() { return imovel; }
    public void setImovel(Imovel imovel) { this.imovel = imovel; }
    public Cliente getInquilino() { return inquilino; }
    public void setInquilino(Cliente inquilino) { this.inquilino = inquilino; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
    public LocalDate getDataFim() { return dataFim; }
    public void setDataFim(LocalDate dataFim) { this.dataFim = dataFim; }
    public LocalDate getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDate dataInicio) { this.dataInicio = dataInicio; }
    public Integer getDiaVencimento() { return diaVencimento; }
    public void setDiaVencimento(Integer diaVencimento) { this.diaVencimento = diaVencimento; }
    public BigDecimal getPercMulta() { return percMulta; }
    public void setPercMulta(BigDecimal percMulta) { this.percMulta = percMulta; }
    public BigDecimal getValorAluguel() { return valorAluguel; }
    public void setValorAluguel(BigDecimal valorAluguel) { this.valorAluguel = valorAluguel; }
    public String getObs() { return obs; }
    public void setObs(String obs) { this.obs = obs; }
}
