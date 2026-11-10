package br.edu.ifma.imobiliaria.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name="alugueis")
public class Aluguel {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="id_locacao", nullable=false)
    private Locacao locacao;
    @Column(name="dt_vencimento", nullable=false)
    private LocalDate dtVencimento;
    @Column(name="valor_pago", nullable=false, precision=12, scale=2)
    private BigDecimal valorPago;
    @Column(columnDefinition="TEXT")
    private String obs;

    public Long getId() { return id; }
    public Locacao getLocacao() { return locacao; }
    public void setLocacao(Locacao locacao) { this.locacao = locacao; }
    public LocalDate getDtVencimento() { return dtVencimento; }
    public void setDtVencimento(LocalDate valor) { this.dtVencimento = valor; }
    public BigDecimal getValorPago() { return valorPago; }
    public void setValorPago(BigDecimal valor) { this.valorPago = valor; }
    public String getObs() { return obs; }
    public void setObs(String obs) { this.obs = obs; }
}
