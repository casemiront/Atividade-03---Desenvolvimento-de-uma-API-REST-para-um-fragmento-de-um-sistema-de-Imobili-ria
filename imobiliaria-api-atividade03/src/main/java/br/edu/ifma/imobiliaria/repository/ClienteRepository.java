package br.edu.ifma.imobiliaria.repository;

import br.edu.ifma.imobiliaria.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> { }
