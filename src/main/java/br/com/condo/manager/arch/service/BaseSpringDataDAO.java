package br.com.condo.manager.arch.service;

import br.com.condo.manager.arch.service.repository.SpringDataRepository;
import br.com.condo.manager.arch.service.util.SearchParameter;
import br.com.condo.manager.arch.service.util.SearchSpecification;
import br.com.condo.manager.arch.service.util.SortParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;

import javax.persistence.EmbeddedId;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.PersistenceContext;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public abstract class BaseSpringDataDAO<E extends Serializable, P extends Serializable> {

    protected Logger LOGGER = LoggerFactory.getLogger(BaseSpringDataDAO.class);

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected SpringDataRepository<E, P> repository;

    /**
     * Guarda o campo da entidade genérica {@link E} que possui a anotação {@link javax.persistence.Id}, indicando que
     * este é o campo que contém a chave primária da entidade.
     */
    private Field idField;

    /**
     * <p>Verifica se já existe uma entidade válida com o mesmo ID da entidade informada. O método descobre o valor do
     * atributo que representa o ID da entidade (através do método {@link #getIdField()}), e faz a verificação de
     * existência utilizando esse valor, conforme o método {@link #exists(Serializable)}.</p>
     *
     * @param entity instância da entidade cuja existência será verificada
     * @return <code>true</code> caso exista, <code>false</code> do contrário
     *
     * @see #getIdField()
     * @see #exists(Serializable)
     */
    public boolean exists(E entity) {
        P id = null;
        try {
            getIdField().setAccessible(true);
            id = (P) getIdField().get(entity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return existsById(id);
    }

    /**
     * <p>Verifica se um determinado ID já existe para uma entidade válida no sistema. O método monta um parâmetro de
     * busca ({@link SearchParameter}) onde o atributo de ID da entidade seja o valor informado, e executa uma contagem
     * de quantos registros são encontrados nessas condições. Caso o ID não seja nulo e a contagem retorne algum valor
     * diferente de zero, considera-se que o ID já existe.</p>
     *
     * @param id o valor de ID a ser verificado
     * @return <code>true</code> caso já exista uma entidade válida com esse ID, <code>false</code> do contrário
     *
     * @see #count()
     */
    public boolean existsById(P id) {
        List<SearchParameter> params = new ArrayList<>();
        params.add(new SearchParameter(getIdField().getName(), SearchParameter.Operator.EQUAL, id));
        Long count = count(params);
        return (id != null && count != 0);
    }

    /**
     * <p>Executa a persistência da entidade informada, retornando o resultado. O método serve tanto para criação quanto
     * para a atualização de registros, e não executa nenhum tipo de validação em cima da entidade que estará sendo
     * persistida.</p>
     *
     * @param entity a entidade a ser persistida
     * @return a entidade pós persistência
     */
    protected E persist(E entity) {
        E persistedEntity = repository.save(entity);
        return persistedEntity;
    }

    /**
     * <p>Responsável pela criação de uma entidade nova no sistema. O método verifica se a entidade informada já existe,
     * e, neste caso, lança uma exceção. Do contrário, a entidade é criada, retornando o resultado da persistência.</p>
     *
     * @param entity a instância da entidade e ser criada
     * @return instância da entidade pós criação
     *
     * @see #exists(Serializable)
     */
    public E create(E entity) {
        if(exists(entity)) throw new IllegalArgumentException("A valid entity with this ID already exists");
        return persist(entity);
    }

    /**
     * <p>Responsável pela atualização dos dados de uma entidade. O método verifica se a entidade informada existe, e,
     * em caso negativo, lança uma exceção. Do contrário, a entidade é atualizada, retornando o resultado da
     * persistência.</p>
     *
     * @param entity a instância da entidade a ser atualizada
     * @return instância da entidade pós atualização
     *
     * @see #exists(Serializable)
     */
    public E update(E entity) {
        if(!exists(entity)) throw new IllegalArgumentException("A valid entity with this ID does not exists");
        return persist(entity);
    }

    /**
     * <p>Busca uma entidade válida em específico, através de seu ID. O método monta um parâmetro de busca onde o valor
     * do atributo de ID da entidade seja igual ao valor informado, executando a query em busca de um resultado único.</p>
     *
     * <p><strong>OBS.: Este método é limitado pelos os parâmetros de busca padrão, definidos pelo método
     * {@link #defaultSearchParameters()}, caso existam.</strong></p>
     *
     * @param id o ID que será buscado
     * @return instância de {@link java.util.Optional}, que pode conter a entidade encontrada, ou pode estar vazia
     *
     * @see #getDefaultSearchSpecification(Collection)
     */
    public Optional<E> retrieve(P id) {
        List<SearchParameter> params = new ArrayList<>();
        params.add(new SearchParameter(getIdField().getName(), SearchParameter.Operator.EQUAL, id));
        return repository.findOne(getDefaultSearchSpecification(params));
    }

    /**
     * <p>Encontra um grupo de entidades válida em específico, através do ID de cada uma. O método monta um parâmetro de
     * busca onde o valor do atributo de ID da entidade seja algum dos ao valores informados, executando a query em
     * busca de uma lista.</p>
     *
     * <p><strong>OBS.: Este método é limitado pelos os parâmetros de busca padrão, definidos pelo método
     * {@link #defaultSearchParameters()}, caso existam.</strong></p>
     *
     * @param ids coleção de IDs das entidades que serão buscadas
     * @return coleção contendo as instâncias das entidades encontradas, podendo estar vazia
     *
     * @see #find(Collection)
     * @see #getDefaultSearchSpecification(Collection)
     */
    public Collection<E> retrieve(Collection<P> ids) {
        List<SearchParameter> params = new ArrayList<>();
        params.add(new SearchParameter(getIdField().getName(), SearchParameter.Operator.IN, ids));
        return find(params);
    }

    /**
     * <p>Encontra todas as entidades válidas que se encaixam nos parâmetros de busca informados, ordenando-as conforme
     * os parâmetros de ordenação, e entregando somente a porção destas entidades que está na página solicitada.</p>
     *
     * <p><strong>OBS.: Este método é limitado pelos os parâmetros de busca padrão, definidos pelo método
     * {@link #defaultSearchParameters()}, caso existam. Se algum dos parâmetros informados já estiver definido dentre
     * os parâmetros padrão, o parâmetro padrão em questão será sobrescrito pelo parâmetro que foi informado.</strong></p>
     *
     * @param searchParameters coleção de parâmetros de busca que serão utilizados
     * @param pageNumber o número da página cujo conteúdo será entregue
     * @param pageSize o total de itens por página
     * @param sortParameters coleção de parâmetros de ordenação que serão aplicados aos resultados
     * @return coleção contendo as instâncias das entidades encontradas, já paginadas e ordenadas, podendo estar vazia;
     * As entidades presentes se encaixam tanto nos parâmetros informados quanto nos parâmetros padrão definidos no DAO
     *
     * @see #getDefaultSearchSpecification(Collection)
     */
    public Collection<E> find(Collection<SearchParameter> searchParameters, Integer pageNumber, Integer pageSize, LinkedList<SortParameter> sortParameters) {
        Sort sort = buildSort(sortParameters);
        if(pageNumber != null && pageNumber > 0 && pageSize != null) {
            Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);
            Page<E> page = repository.findAll(getDefaultSearchSpecification(searchParameters), pageable);
            return page.getContent();
        }
        return repository.findAll(getDefaultSearchSpecification(searchParameters), sort);
    }

    /**
     * <p>Encontra todas as entidades válidas que se encaixam nos parâmetros de busca informados, entregando somente a
     * porção destas entidades que está na página solicitada.</p>
     *
     * <p><strong>OBS.: Este método é limitado pelos os parâmetros de busca padrão, definidos pelo método
     * {@link #defaultSearchParameters()}, caso existam. Se algum dos parâmetros informados já estiver definido dentre
     * os parâmetros padrão, o parâmetro padrão em questão será sobrescrito pelo parâmetro que foi informado.</strong></p>
     *
     * @param searchParameters coleção de parâmetros de busca que serão utilizados
     * @param pageNumber o número da página cujo conteúdo será entregue
     * @param pageSize o total de itens por página
     * @return coleção contendo as instâncias das entidades encontradas, já paginadas, podendo estar vazia; As entidades
     * presentes se encaixam tanto nos parâmetros informados quanto nos parâmetros padrão definidos no DAO
     *
     * @see #getDefaultSearchSpecification(Collection)
     */
    public Collection<E> find(Collection<SearchParameter> searchParameters, Integer pageNumber, Integer pageSize) {
        return find(searchParameters, pageNumber, pageSize, null);
    }

    /**
     * <p>Encontra todas as entidades válidas que se encaiam nos parâmetros de busca informados, ordenando-as conforme
     * os parâmetros de ordenação.</p>
     *
     * <p><strong>OBS.: Este método é limitado pelos os parâmetros de busca padrão, definidos pelo método
     * {@link #defaultSearchParameters()}, caso existam. Se algum dos parâmetros informados já estiver definido dentre
     * os parâmetros padrão, o parâmetro padrão em questão será sobrescrito pelo parâmetro que foi informado.</strong></p>
     *
     * @param searchParameters coleção de parâmetros de busca que serão utilizados
     * @param sortParameters coleção de parâmetros de ordenação que serão aplicados aos resultados
     * @return coleção contendo as instâncias das entidades encontradas, já ordenadas, podendo estar vazia; As entidades
     * presentes se encaixam tanto nos parâmetros informados quanto nos parâmetros padrão definidos no DAO
     *
     * @see #getDefaultSearchSpecification(Collection)
     */
    public Collection<E> find(Collection<SearchParameter> searchParameters, LinkedList<SortParameter> sortParameters) {
        return find(searchParameters, null, null, sortParameters);
    }

    /**
     * <p>Encontra todas as entidades válidas que se encaixam nos parâmetros de busca informados.</p>
     *
     * <p><strong>OBS.: Este método é limitado pelos os parâmetros de busca padrão, definidos pelo método
     * {@link #defaultSearchParameters()}, caso existam. Se algum dos parâmetros informados já estiver definido dentre
     * os parâmetros padrão, o parâmetro padrão em questão será sobrescrito pelo parâmetro que foi informado.</strong></p>
     *
     * @param searchParameters coleção de parâmetros de busca que serão utilizados
     * @return coleção contendo as instâncias das entidades encontradas, podendo estar vazia; As entidades presentes se
     * encaixam tanto nos parâmetros informados quanto nos parâmetros padrão definidos no DAO
     *
     * @see #getDefaultSearchSpecification(Collection)
     */
    public Collection<E> find(Collection<SearchParameter> searchParameters) {
        return find(searchParameters, null, null, null);
    }

    /**
     * <p>Encontra todas as entidades válidas que se encaixam no parâmetro de busca informado.</p>
     *
     * <p><strong>OBS.: Este método é limitado pelos os parâmetros de busca padrão, definidos pelo método
     * {@link #defaultSearchParameters()}, caso existam. Se o parâmetro informado já estiver definido dentre os
     * parâmetros padrão, o parâmetro padrão em questão será sobrescrito pelo parâmetro que foi informado.</strong></p>
     *
     * @param searchParameter o parâmetro de busca que será utilizado
     * @return coleção contendo as instâncias das entidades encontradas, podendo estar vazia; As entidades presentes se
     * encaixam tanto no parâmetro informado quanto nos parâmetros padrão definidos no DAO
     *
     * @see #getDefaultSearchSpecification(Collection)
     */
    public Collection<E> find(SearchParameter searchParameter) {
        if(searchParameter == null) return find();
        List<SearchParameter> searchParameters = new ArrayList<>();
        searchParameters.add(searchParameter);
        return find(searchParameters, null, null, null);
    }

    /**
     * <p>Encontra todas as entidades válidas, entregando somente a porção destas entidades que está na página
     * solicitada.</p>
     *
     * <p><strong>OBS.: Este método é limitado pelos os parâmetros de busca padrão, definidos pelo método
     * {@link #defaultSearchParameters()}, caso existam.</strong></p>
     *
     * @param pageNumber o número da página cujo conteúdo será entregue
     * @param pageSize o total de itens por página
     * @return coleção contendo as instâncias das entidades encontradas, já paginadas, podendo estar vazia; As entidades
     * presentes se encaixam nos parâmetros padrão definidos no DAO
     *
     * @see #getDefaultSearchSpecification(Collection)
     */
    public Collection<E> find(Integer pageNumber, Integer pageSize) {
        return find(null, pageNumber, pageSize, null);
    }

    /**
     * <p>Encontra todas as entidades válidas, ordenando-as conforme os parâmetros de ordenação.</p>
     *
     * <p><strong>OBS.: Este método é limitado pelos os parâmetros de busca padrão, definidos pelo método
     * {@link #defaultSearchParameters()}, caso existam.</strong></p>
     *
     * @param sortParameters coleção de parâmetros de ordenação que serão aplicados aos resultados
     * @return coleção contendo as instâncias das entidades encontradas, já ordenadas, podendo estar vazia; As entidades
     * presentes se encaixam nos parâmetros padrão definidos no DAO
     *
     * @see #getDefaultSearchSpecification(Collection)
     */
    public Collection<E> find(LinkedList<SortParameter> sortParameters) {
        return find(null, null, null, sortParameters);
    }

    /**
     * <p>Encontra todas as entidades válidas.</p>
     *
     * <p><strong>OBS.: Este método é limitado pelos os parâmetros de busca padrão, definidos pelo método
     * {@link #defaultSearchParameters()}, caso existam.</strong></p>
     *
     * @return coleção contendo as instâncias das entidades encontradas, podendo estar vazia; As entidades presentes se
     * encaixam nos parâmetros padrão definidos no DAO
     *
     * @see #getDefaultSearchSpecification(Collection)
     */
    public Collection<E> find() {
        return find(null, null, null, null);
    }

    /**
     * <p>Encontra todas as entidades que se encaixam nos parâmetros de busca informados, ordenando-as conforme os
     * parâmetros de ordenação, e entregando somente a porção destas entidades que está na página solicitada.</p>
     *
     * <p><strong>OBS.: Este método ignora completamente quaisquer parâmetros de busca padrão.</strong></p>
     *
     * @param searchParameters coleção de parâmetros de busca que serão utilizados
     * @param pageNumber o número da página cujo conteúdo será entregue
     * @param pageSize o total de itens por página
     * @param sortParameters coleção de parâmetros de ordenação que serão aplicados aos resultados
     * @return coleção contendo as instâncias das entidades encontradas, já paginadas e ordenadas, podendo estar vazia;
     * As entidades presentes se encaixam somente nos parâmetros informados
     */
    public Collection<E> findAll(Collection<SearchParameter> searchParameters, Integer pageNumber, Integer pageSize, LinkedList<SortParameter> sortParameters) {
        Sort sort = buildSort(sortParameters);
        if(pageNumber != null && pageNumber > 0 && pageSize != null) {
            Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, sort);
            Page<E> page = repository.findAll(new SearchSpecification<>(searchParameters), pageable);
            return page.getContent();
        }
        return repository.findAll(new SearchSpecification<>(searchParameters), sort);
    }

    /**
     * <p>Encontra todas as entidades que se encaixam nos parâmetros de busca informados, entregando somente a porção
     * destas entidades que está na página solicitada.</p>
     *
     * <p><strong>OBS.: Este método ignora completamente quaisquer parâmetros de busca padrão.</strong></p>
     *
     * @param searchParameters coleção de parâmetros de busca que serão utilizados
     * @param pageNumber o número da página cujo conteúdo será entregue
     * @param pageSize o total de itens por página
     * @return coleção contendo as instâncias das entidades encontradas, já paginadas e ordenadas, podendo estar vazia;
     * As entidades presentes se encaixam somente nos parâmetros informados
     */
    public Collection<E> findAll(Collection<SearchParameter> searchParameters, Integer pageNumber, Integer pageSize) {
        return findAll(searchParameters, pageNumber, pageSize, null);
    }

    /**
     * <p>Encontra todas as entidades que se encaixam nos parâmetros de busca informados, ordenando-as conforme os
     * parâmetros de ordenação.</p>
     *
     * <p><strong>OBS.: Este método ignora completamente quaisquer parâmetros de busca padrão.</strong></p>
     *
     * @param searchParameters coleção de parâmetros de busca que serão utilizados
     * @param sortParameters coleção de parâmetros de ordenação que serão aplicados aos resultados
     * @return coleção contendo as instâncias das entidades encontradas, já ordenadas, podendo estar vazia; As entidades
     * presentes se encaixam somente nos parâmetros informados
     */
    public Collection<E> findAll(Collection<SearchParameter> searchParameters, LinkedList<SortParameter> sortParameters) {
        return findAll(searchParameters, null, null, sortParameters);
    }

    /**
     * <p>Encontra todas as entidades que se encaixam nos parâmetros de busca informados.</p>
     *
     * <p><strong>OBS.: Este método ignora completamente quaisquer parâmetros de busca padrão.</strong></p>
     *
     * @param searchParameters coleção de parâmetros de busca que serão utilizados
     * @return coleção contendo as instâncias das entidades encontradas, podendo estar vazia; As entidades presentes se
     * encaixam apenas nos parâmetros informados
     */
    public Collection<E> findAll(Collection<SearchParameter> searchParameters) {
        return findAll(searchParameters, null, null, null);
    }

    /**
     * <p>Encontra todas as entidades que se encaixam no parâmetro de busca informado.</p>
     *
     * <p><strong>OBS.: Este método ignora completamente quaisquer parâmetros de busca padrão.</strong></p>
     *
     * @param searchParameter o parâmetro de busca que será utilizado
     * @return coleção contendo as instâncias das entidades encontradas, podendo estar vazia; As entidades presentes se
     * encaixam apenas nos parâmetro informado
     */
    public Collection<E> findAll(SearchParameter searchParameter) {
        if(searchParameter == null) return findAll();
        List<SearchParameter> searchParameters = new ArrayList<>();
        searchParameters.add(searchParameter);
        return findAll(searchParameters, null, null, null);
    }

    /**
     * <p>Encontra todas as entidades, entregando somente a porção destas entidades que está na página solicitada.</p>
     *
     * <p><strong>OBS.: Este método ignora completamente quaisquer parâmetros de busca padrão.</strong></p>
     *
     * @param pageNumber o número da página cujo conteúdo será entregue
     * @param pageSize o total de itens por página
     * @return coleção contendo as instâncias das entidades encontradas, já paginadas, podendo estar vazia; As entidades
     * presentes não são limitadas de forma alguma
     */
    public Collection<E> findAll(Integer pageNumber, Integer pageSize) {
        return findAll(null, pageNumber, pageSize, null);
    }

    /**
     * <p>Encontra todas as entidades, ordenando-as conforme os parâmetros de ordenação.</p>
     *
     * <p><strong>OBS.: Este método ignora completamente quaisquer parâmetros de busca padrão.</strong></p>
     *
     * @param sortParameters coleção de parâmetros de ordenação que serão aplicados aos resultados
     * @return coleção contendo as instâncias das entidades encontradas, já ordenadas, podendo estar vazia; As entidades
     * presentes não são limitadas de forma alguma
     */
    public Collection<E> findAll(LinkedList<SortParameter> sortParameters) {
        return findAll(null, null, null, sortParameters);
    }

    /**
     * <p>Encontra todas as entidades, sem distinções.</p>
     *
     * <p><strong>OBS.: Este método ignora completamente quaisquer parâmetros de busca padrão.</strong></p>
     *
     * @return coleção contendo as instâncias das entidades encontradas, podendo estar vazia; As entidades presentes não
     * são limitadas de forma alguma
     */
    public Collection<E> findAll() {
        return findAll(null, null, null, null);
    }

    /**
     * <p>Dá a contagem de todas as entidades válidas que se encaixam nos parâmetros de busca informados.</p>
     *
     * <p><strong>OBS.: Este método é limitado pelos os parâmetros de busca padrão, definidos pelo método
     * {@link #defaultSearchParameters()}, caso existam. Se algum dos parâmetros informados já estiver definido dentre
     * os parâmetros padrão, o parâmetro padrão em questão será sobrescrito pelo parâmetro que foi informado.</strong></p>
     *
     * @param searchParameters coleção de parâmetros de busca que serão utilizados
     * @return o total de entidades contadas; As entidades contadas se encaixam tanto nos parâmetros informados quanto
     * nos parâmetros padrão definidos no DAO
     *
     * @see #getDefaultSearchSpecification(Collection)
     */
    public Long count(Collection<SearchParameter> searchParameters) {
        Long count =  repository.count(getDefaultSearchSpecification(searchParameters));
        return count;
    }

    /**
     * <p>Dá a contagem de todas as entidades válidas que se encaixam no parâmetro de busca informado.</p>
     *
     * <p><strong>OBS.: Este método é limitado pelos os parâmetros de busca padrão, definidos pelo método
     * {@link #defaultSearchParameters()}, caso existam. Se o parâmetro informado já estiver definido dentre os
     * parâmetros padrão, o parâmetro padrão em questão será sobrescrito pelo parâmetro que foi informado.</strong></p>
     *
     * @param searchParameter o parâmetro de busca que será utilizado
     * @return o total de entidades contadas; As entidades contadas se encaixam tanto no parâmetro informado quanto nos
     * parâmetros padrão definidos no DAO
     *
     * @see #getDefaultSearchSpecification(Collection)
     */
    public Long count(SearchParameter searchParameter) {
        if(searchParameter == null) return repository.count(getDefaultSearchSpecification());
        List<SearchParameter> searchParameters = new ArrayList<>();
        searchParameters.add(searchParameter);
        return repository.count(getDefaultSearchSpecification(searchParameters));
    }

    /**
     * <p>Dá a contagem de todas as entidades válidas.</p>
     *
     * <p><strong>OBS.: Este método é limitado pelos os parâmetros de busca padrão, definidos pelo método
     * {@link #defaultSearchParameters()}, caso existam.</strong></p>
     *
     * @return o total de entidades contadas; As entidades contadas se encaixam apenas nos parâmetros padrão definidos
     * no DAO
     *
     * @see #getDefaultSearchSpecification(Collection)
     */
    public Long count() {
        return repository.count(getDefaultSearchSpecification());
    }

    /**
     * <p>Dá a contagem de todas as entidades que se encaixam nos parâmetros de busca informados.</p>
     *
     * <p><strong>OBS.: Este método ignora quaisquer parâmetros de busca padrão</strong></p>
     *
     * @param searchParameters coleção de parâmetros de busca que serão utilizados
     * @return o total de entidades contadas; As entidades contadas se encaixam apenas nos parâmetros informados
     */
    public Long countAll(Collection<SearchParameter> searchParameters) {
        return repository.count(new SearchSpecification<>(searchParameters));
    }

    /**
     * <p>Dá a contagem de todas as entidades que se encaixam nos parâmetro de busca informado.</p>
     *
     * <p><strong>OBS.: Este método ignora quaisquer parâmetros de busca padrão</strong></p>
     *
     * @param searchParameter o parâmetro de busca que será utilizado
     * @return o total de entidades contadas; As entidades contadas se encaixam apenas no parâmetro informado
     */
    public Long countAll(SearchParameter searchParameter) {
        if(searchParameter == null) return repository.count(new SearchSpecification<>());
        List<SearchParameter> searchParameters = new ArrayList<>();
        searchParameters.add(searchParameter);
        return repository.count(new SearchSpecification<>(searchParameters));
    }

    /**
     * <p>Dá a contagem de todas as entidades, sem distinções.</p>
     *
     * <p><strong>OBS.: Este método ignora quaisquer parâmetros de busca padrão</strong></p>
     *
     * @return o total de entidades contadas; As entidades contadas não são limitadas de forma alguma
     */
    public Long countAll() {
        return repository.count(new SearchSpecification<>());
    }

    public void deleteById(P id) {
        repository.deleteById(id);
    }

    public void deleteById(Collection<P> ids) {
        Collection<E> entities = retrieve(ids);
        delete(entities);
    }

    public void delete(E entity) {
        repository.delete(entity);
    }

    public void delete(Collection<E> entities) {
        repository.deleteAll(entities);
    }

    /**
     * <p>Determina quais são os parâmetros de busca padrão para todas as pesquisasa executadas pelo DAO. Inicialmente,
     * nenhum parâmetro padrão é definido - esta definição deve ser feita pela implementação concreta. Ao definir um
     * conjunto de parâmetros de busca padrão, estes parâmetros passam a ser utilizados em todas as buscas e contagens
     * que fazem uso da estrutura de parâmetros, no sentido de que são eles que derfinem as características de uma
     * entidade considerada válida.</p>
     *
     * @return a coleção de atributos de busca que serão utilizados por padrão em todas as buscas e contagens do DAO
     */
    @Nullable
    protected Collection<SearchParameter> defaultSearchParameters() {
        return null;
    }

    /**
     * <p>Retorna uma instância de {@link SearchSpecification<E>}, que é uma implementação de
     * {@link org.springframework.data.jpa.domain.Specification<E>} com a capacidade de interpretar os parâmetros de
     * busca customizados da API ({@link SearchParameter}). O método leva em conta apenas os parâmetros de busca padrão,
     * definidos em {@link #defaultSearchParameters()}, caso existam.</p>
     *
     * @return instância de {@link SearchSpecification<E>}, contendo as especificações dos parâmetros de busca padrão,
     * caso existam
     */
    protected SearchSpecification<E> getDefaultSearchSpecification() {
        return getDefaultSearchSpecification(null);
    }

    /**
     * <p>Retorna uma instância de {@link SearchSpecification<E>}, que é uma implementação de
     * {@link org.springframework.data.jpa.domain.Specification<E>} com a capacidade de interpretar os parâmetros de
     * busca customizados da API ({@link SearchParameter}). O método leva em conta tanto os parâmetros de busca
     * informados, como também os parâmetros de busca padrão, definidos em {@link #defaultSearchParameters()}, caso
     * existam.</p>
     *
     * <p>No caso de algum parâmetro informado já estar definido no conjunto de parâmetros padrão, <strong>o valor
     * informado como argumento tem precedência sobre o valor padrão</strong>, sobrescrevendo-o na busca.</p>
     *
     * @param params coleção de parâmetros de busca que serão aplicados ao <code>SearchSpecification</code>, em conjunto
     *               com os parâmetros padrão
     * @return instância de {@link SearchSpecification<E>}, contendo as especificações dos parâmetros de busca padrão,
     * caso existam
     */
    protected SearchSpecification<E> getDefaultSearchSpecification(Collection<SearchParameter> params) {
        List<SearchParameter> parameters = params != null ? new ArrayList<>(params) : new ArrayList<>();

        Collection<SearchParameter> defaultParams = defaultSearchParameters();
        if(defaultParams != null && !defaultParams.isEmpty()) {
            for(SearchParameter defaultParam : defaultParams) {
                if(!parameters.contains(defaultParam)) parameters.add(defaultParam);
            }
        }

        return new SearchSpecification<>(parameters);
    }

    /**
     * <p>Constróio e retorna a ordenação a ser usada em uma consulta, conforme os parâmetros de ordenação informados. O
     * método transforma cada instância de {@link SortParameter} em um novo {@link org.springframework.data.domain.Sort},
     * encadeando-os conforme a ordem em que os parâmetros se apresentam. Caso nenhum parâmetro de ordenação tenha sido
     * informado, um parâmetro padrão, de ordenação por ID da entidade é entregue como resultado.</p>
     *
     * @param sortParameters coleção ordenada dos parâmetros de ordenação
     * @return a ordenação, levando em conta os parâmetros informados, ou a ordenação padrão por ID da entidade
     */
    protected Sort buildSort(LinkedList<SortParameter> sortParameters) {
        if(sortParameters == null || sortParameters.isEmpty()) return Sort.by(getIdField().getName());

        Sort sort = null;
        for (SortParameter sortParameter : sortParameters) {
            Sort sorting = Sort.by(SortParameter.Order.DESC.equals(sortParameter.getOrder()) ? Sort.Order.desc(sortParameter.getAttribute()) : Sort.Order.asc(sortParameter.getAttribute()));
            sort = (sort == null) ? sorting : sort.and(sorting);
        }
        return sort;
    }

    /**
     * <p>Encontra e entrega o atributo da entidade que possui a anotação {@link javax.persistence.Id}, indicando que é
     * a sua chave primária. O método se utiliza de <code>Reflection</code> para iterar sobre os campos declarados da
     * entidade, retornando aquele que possuir a anotação mencionada.</p>
     *
     * @return instância de {@link java.lang.reflect.Field} do atributo marcado como {@link javax.persistence.Id} ou
     * <code>null</code>, caso não encontre
     */
    @Nullable
    public Field getIdField() {
        if (idField == null) {
            final ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
            Class<E> entityType = (Class<E>) (type).getActualTypeArguments()[0];
            for (Field entityField : entityType.getDeclaredFields()) {
                Id idAnnotation = entityField.getAnnotation(Id.class);
                EmbeddedId embeddedIdAnnotation = entityField.getAnnotation(EmbeddedId.class);

                if (idAnnotation != null || embeddedIdAnnotation != null) {
                    idField = entityField;
                    break;
                }
            }
        }
        return idField;
    }

}

