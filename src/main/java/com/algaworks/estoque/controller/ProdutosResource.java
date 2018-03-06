package com.algaworks.estoque.controller;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

import com.algaworks.estoque.model.Produto;
import com.algaworks.estoque.repository.Produtos;

@RestController
@RequestMapping("/produtos")
public class ProdutosResource {
	
	@Autowired
	private Produtos produtos;
	
	@GetMapping("/pesquisarProdutos")
	public List<Produto> pesquisarProdutos(@RequestParam String nome) {
		return produtos.pesquisarProdutos(nome);
	}
	
	@GetMapping("/por-nome")
	public Produto porNome(@RequestParam String nome) {
		return produtos.findByNome(nome);
	}
	
	@GetMapping("/por-nome-comecando-com")
	public List<Produto> porNomeComecandoCom(@RequestParam String nome) {
		return produtos.findByNomeStartingWithIgnoreCase(nome);
	}
	
	@GetMapping("/sem-descricao")
	public List<Produto> semDescricao() {
		return produtos.findByDescricaoIsNull();
	}
	
	@GetMapping("/{id}")
	public Produto buscar(@PathVariable Long id) {
		return produtos.findOne(id);
	}
	
	@GetMapping
	public Page<Produto> pesquisar(
			@RequestParam(defaultValue = "0") int pagina, 
			@RequestParam(defaultValue = "10") int porPagina,
			@RequestParam(defaultValue = "nome") String ordenacao,
			@RequestParam(defaultValue = "ASC") Sort.Direction direcao) {
		return produtos.findAll(new PageRequest(
				pagina, porPagina, new Sort(direcao, ordenacao)));
	}
	
	// Spring will inject here the entity manager object
	  @PersistenceContext
	  private EntityManager entityManager;
	    
	  /**
	   * A basic search for the entity User. The search is done by exact match per
	   * keywords on fields name, city and email.
	   * 
	   * @param text The query text.
	   */
	  @GetMapping("/lucene")
	  public List search(@RequestParam String text) {
	    
		System.out.println(text);  
		  
	    // get the full text entity manager
	    FullTextEntityManager fullTextEntityManager =
	        org.hibernate.search.jpa.Search.
	        getFullTextEntityManager(entityManager);
	    
	    // create the query using Hibernate Search query DSL
	    QueryBuilder queryBuilder = 
	        fullTextEntityManager.getSearchFactory()
	        .buildQueryBuilder().forEntity(Produto.class).get();
	    
	    // a very basic query by keywords
	    org.apache.lucene.search.Query query =
	        queryBuilder
	          .keyword()
	          .onFields("nome", "descricao")
	          .matching(text)
	          .createQuery();

	    // wrap Lucene query in an Hibernate Query object
	    org.hibernate.search.jpa.FullTextQuery jpaQuery =
	        fullTextEntityManager.createFullTextQuery(query, Produto.class);
	  
	    // execute search and return results (sorted by relevance as default)
	    @SuppressWarnings("unchecked")
	    List results = jpaQuery.getResultList();
	    
	    return results;
	  } // method search
	
	@PostMapping
	public Produto salvar(@RequestBody Produto produto) {
		return produtos.save(produto);
	}
	
	@DeleteMapping("/{id}")
	public void deletar(@PathVariable Long id) {
		produtos.delete(id);
	}
}