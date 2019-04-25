package br.com.condo.manager.arch.controller;

import br.com.condo.manager.arch.controller.exception.BadRequestException;
import br.com.condo.manager.arch.controller.exception.ForbiddenException;
import br.com.condo.manager.arch.controller.exception.NotFoundException;
import br.com.condo.manager.arch.security.SecurityUtils;
import br.com.condo.manager.arch.service.BaseSpringDataDAO;
import br.com.condo.manager.arch.service.util.SearchParameter;
import br.com.condo.manager.arch.service.util.SortParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.xml.bind.DatatypeConverter;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseEndpoint<E extends Serializable, P extends Serializable> {

    /**
     * Nome do parâmetro na requisição HTTP que contém o número da página, quando houver paginação
     */
    protected static final String REQUEST_PARAMETER_NAME_FOR_PAGE_NUMBER = "pageNumber";
    /**
     * Nome do parâmetro na requisição HTTP que contém o tamanho da página, em itens, quando houver paginação
     */
    protected static final String REQUEST_PARAMETER_NAME_FOR_PAGE_SIZE = "pageSize";
    /**
     * Nome do parâmetro na requisição HTTP que contém as definições de ordenação de resultados
     */
    protected static final String REQUEST_PARAMETER_NAME_FOR_ORDER_BY = "orderBy";

    @Autowired
    protected BaseSpringDataDAO<E, P> dao;

    @Autowired
    protected SecurityUtils securityUtils;

    private Class entityType;



    @GetMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Collection<E>> find(@RequestParam Map<String,String> requestParams) {
        Collection<E> result = executeFind(requestParams);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(value = {"/count", "/count/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Long> count(@RequestParam Map<String,String> requestParams) {
        Long result = dao.count(getSearchParameters(requestParams));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping(value = {"", "/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<E> create(@RequestBody E requestData) {
        E dataToCreate = validateRequestDataForCreate(requestData);
        updateId(dataToCreate, null);
        E result = dao.create(dataToCreate);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping(value = {"/{id}/exists", "/{id}/exists/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity exists(@PathVariable P id) {
        if(dao.existsById(id))
            return new ResponseEntity<>(HttpStatus.OK);
        throw new NotFoundException(getEntityType().getSimpleName() + " of ID " + id + " not found");
    }

    @GetMapping(value = {"/{id}", "/{id}/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<E> retrieve(@PathVariable P id) {
        E retrieveResult = retrieveResource(id);
        return new ResponseEntity<>(retrieveResult, HttpStatus.OK);
    }

    @PutMapping(value = {"/{id}", "/{id}/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<E> update(@PathVariable P id, @RequestBody E requestData) {
        E retrieveResult = retrieveResource(id);
        E dataToUpdate = validateRequestDataForUpdate(requestData, retrieveResult);
        updateId(dataToUpdate, id);
        E updateResult = dao.update(dataToUpdate);
        return new ResponseEntity<>(updateResult, HttpStatus.OK);
    }

    @DeleteMapping(value = {"/{id}", "/{id}/"}, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity delete(@PathVariable P id) {
        E retrieveResult = retrieveResource(id);
        dao.delete(retrieveResult);
        return new ResponseEntity(HttpStatus.OK);
    }

    protected E validateRequestDataForCreate(E requestData) {
        return requestData;
    }

    protected E validateRequestDataForUpdate(E requestData, E currentData) {
        return requestData;
    }

    /**
     * <p>Busca e retorna um recurso através de seu ID. O método faz uso da busca por ID padrão, validando a existência
     * do registro: caso nenhum resultado tenha sido encontrado, uma exceção é disparadam avisando que não há um recurso
     * com o ID informado.</p>
     *
     * @param id o valor do ID
     * @return instância da entidade encontrada
     * @throws NotFoundException em caso de não encontrar nenhum registro
     */
    protected E retrieveResource(P id) throws NotFoundException {
        Optional<E> retrieveResult = dao.retrieve(id);
        if(!retrieveResult.isPresent()) throw new NotFoundException(getEntityType().getSimpleName() + " of ID " + id + " not found");
        return retrieveResult.get();
    }

    /**
     * <p>Executa o método de busca padrão de um DAO. O método extrai as informações necessárias dos parâmetros da
     * requisição HTTP (extrai os parâmetros de busca, os dois parâmetros de paginação e os parâmetros de ordenação), e
     * envia as informações encontradas para o método <code>find</code> do {@link BaseSpringDataDAO} informado. Caso
     * nenhum DAO tenha sido informado, o método utiliza o próprio DAO do controller, que trabalha com a entidade
     * genérica <code>E</code>.</p>
     *
     * <p>Ao extrair os parâmetros de busca ({@link SearchParameter}) e os de ordenação ({@link SortParameter}), o tipo
     * da entidade que será validada é o tipo genérico do DAO que será usado na busca, respeitando assim a entidade
     * genérica <code>E</code>, caso nenhum DAO tenha sido informado (ou seja, o DAO do controller será usado), ou
     * respeitando uma outra entidade qualquer que tiver sido definida como argumento genérico no DAO informado.</p>
     *
     * <p>Para que a paginação seja aplicada, ambos os parâmetros devem estar presentes (o número da página e o tamanho
     * da página). Do contrário, uma exceção é lançada, avisando do erro.</p>
     *
     * @param requestParams mapa de parâmetros da requisição HTTP
     * @param dao instância de {@link BaseSpringDataDAO} cujo <code>find</code> será executado.
     * @return a coleção de entidades retornada pelo DAO, respeitando as informações extraídas dos parâmetros da
     * requisição HTTP
     *
     * @see #getPageNumber(Map)
     * @see #getPageSize(Map)
     * @see #getSearchParameters(Class, Map)
     * @see #getSortParameters(Class, Map)
     */
    protected Collection<? extends Serializable> executeFind(Map<String,String> requestParams, BaseSpringDataDAO<? extends Serializable, ? extends  Serializable> dao, Class entityType) {
        if(dao == null) dao = this.dao;
        if(entityType == null) entityType = getEntityType();

        Integer pageNumber = getPageNumber(requestParams);
        Integer pageSize = getPageSize(requestParams);
        if((pageNumber != null && pageSize == null) || (pageNumber == null && pageSize != null))
            throw new BadRequestException("Invalid pagination: both page number and page size must be provided for pagination to be applied or both must be empty for a result with no pagination");
        return dao.find(getSearchParameters(entityType, requestParams), pageNumber, pageSize, getSortParameters(entityType, requestParams));
    }

    /**
     * <p>Executa o método de busca padrão do DAO da entidade genérica <code>E</code> do controller. O método extrai as
     * informações necessárias dos parâmetros da requisição HTTP (extrai os parâmetros de busca, os dois parâmetros de
     * paginação e os parâmetros de ordenação), e envia as informações encontradas para o método <code>find</code> do
     * {@link BaseSpringDataDAO} do controller. Portanto, ao extrair os parâmetros de busca ({@link SearchParameter}) e
     * os de ordenação ({@link SortParameter}), o tipo da entidade que será validada é o tipo genérico <code>E</code> da
     * entidade do controller.</p>
     *
     * <p>Para que a paginação seja aplicada, ambos os parâmetros devem estar presentes (o número da página e o tamanho
     * da página). Do contrário, uma exceção é lançada, avisando do erro.</p>
     *
     * @param requestParams mapa de parâmetros da requisição HTTP
     * @return a coleção de entidades retornada pelo DAO, respeitando as informações extraídas dos parâmetros da
     * requisição HTTP
     *
     * @see #getPageNumber(Map)
     * @see #getPageSize(Map)
     * @see #getSearchParameters(Class, Map)
     * @see #getSortParameters(Class, Map)
     */
    protected Collection<E> executeFind(Map<String,String> requestParams) {
        return (Collection<E>) executeFind(requestParams, null, null);
    }

    /**
     * <p>Executa o método de busca não-padrão de um DAO. O método extrai as informações necessárias dos parâmetros da
     * requisição HTTP (extrai os parâmetros de busca, os dois parâmetros de paginação e os parâmetros de ordenação), e
     * envia as informações encontradas para o método <code>findAll</code> do {@link BaseSpringDataDAO} informado. Caso
     * nenhum DAO tenha sido informado, o método utiliza o próprio DAO do controller, que trabalha com a entidade
     * genérica <code>E</code>.</p>
     *
     * <p>Ao extrair os parâmetros de busca ({@link SearchParameter}) e os de ordenação ({@link SortParameter}), o tipo
     * da entidade que será validada é o tipo genérico do DAO que será usado na busca, respeitando assim a entidade
     * genérica <code>E</code>, caso nenhum DAO tenha sido informado (ou seja, o DAO do controller será usado), ou
     * respeitando uma outra entidade qualquer que tiver sido definida como argumento genérico no DAO informado.</p>
     *
     * <p>Para que a paginação seja aplicada, ambos os parâmetros devem estar presentes (o número da página e o tamanho
     * da página). Do contrário, uma exceção é lançada, avisando do erro.</p>
     *
     * @param requestParams mapa de parâmetros da requisição HTTP
     * @param dao instância de {@link BaseSpringDataDAO} cujo <code>findAll</code> será executado.
     * @return a coleção de entidades retornada pelo DAO, respeitando as informações extraídas dos parâmetros da
     * requisição HTTP
     *
     * @see #getPageNumber(Map)
     * @see #getPageSize(Map)
     * @see #getSearchParameters(Class, Map)
     * @see #getSortParameters(Class, Map)
     */
    protected Collection<? extends Serializable> executeFindAll(Map<String,String> requestParams, BaseSpringDataDAO<? extends Serializable, ? extends  Serializable> dao, Class entityType) {
        if(dao == null) dao = this.dao;
        if(entityType == null) entityType = getEntityType();

        Integer pageNumber = getPageNumber(requestParams);
        Integer pageSize = getPageSize(requestParams);
        if((pageNumber != null && pageSize == null) || (pageNumber == null && pageSize != null))
            throw new BadRequestException("Invalid pagination: both page number and page size must be provided for pagination to be applied or both must be empty for a result with no pagination");
        return dao.findAll(getSearchParameters(entityType, requestParams), pageNumber, pageSize, getSortParameters(entityType, requestParams));
    }

    /**
     * <p>Executa o método de busca não-padrão do DAO da entidade genérica <code>E</code> do controller. O método extrai
     * as informações necessárias dos parâmetros da requisição HTTP (extrai os parâmetros de busca, os dois parâmetros
     * de paginação e os parâmetros de ordenação), e envia as informações encontradas para o método <code>findAll</code>
     * do {@link BaseSpringDataDAO} do controller. Portanto, ao extrair os parâmetros de busca ({@link SearchParameter})
     * e os de ordenação ({@link SortParameter}), o tipo da entidade que será validada é o tipo genérico <code>E</code>
     * da entidade do controller.</p>
     *
     * <p>Para que a paginação seja aplicada, ambos os parâmetros devem estar presentes (o número da página e o tamanho
     * da página). Do contrário, uma exceção é lançada, avisando do erro.</p>
     *
     * @param requestParams mapa de parâmetros da requisição HTTP
     * @return a coleção de entidades retornada pelo DAO, respeitando as informações extraídas dos parâmetros da
     * requisição HTTP
     *
     * @see #getPageNumber(Map)
     * @see #getPageSize(Map)
     * @see #getSearchParameters(Class, Map)
     * @see #getSortParameters(Class, Map)
     */
    protected Collection<E> executeFindAll(Map<String,String> requestParams) {
        return (Collection<E>) executeFindAll(requestParams, null, null);
    }

    /**
     * <p>Extrai e entrega uma coleção de parâmetros de busca a partir dos parâmetros enviados na requisição HTTP,
     * validando-os com base na classe de entidade informada. O método verifica um por um dos parâmetros da requisição
     * HTTP, validando tanto o seu nome quanto seu conteúdo, de forma a certificar-se que trata-se de um parâmetro
     * válido em relação à classe de entidade informada, criando uma instância de {@link SearchParameter}. Se nenhuma
     * classe for informada, a classe da entidade genérica <code>E</code> do controller é utilizada como referência de
     * validação.</p>
     *
     * <p>A validação dos parâmetros consiste em verificar:</p>
     * <ul>
     *     <li>se o nome do parâmetro é um caminho que faz referência a um atributo válido da entidade</li>
     *     <li>se o conteúdo do parâmetro está no formato operador:valor</li>
     *     <li>se o valor do conteúdo do parâmetro corresponde ao tipo do atributo que o parâmetro referencia</li>
     * </ul>
     *
     * <p>Qualquer erro na verificação de algum dos parâmetros encerra o processamento, lançando uma exceção que informa
     * qual o parâmetro com problema e qual o motivo do erro.</p>
     *
     * @param entityType tipo da entidade, pra fins de validação dos parâmetros
     * @param requestParams mapa de parâmetros da requisição HTTP, de onde os parâmetros de busca serão extraídos
     * @return coleção de parâmetros de busca extraídos dos parâmetros da requisição HTTP
     *
     * @see #buildSearchParameter(String, String, Class)
     */
    protected List<SearchParameter> getSearchParameters(Class entityType, Map<String,String> requestParams) {
        if(requestParams == null) throw new IllegalArgumentException("A valid request parameters map must be provided");
        if(entityType == null) entityType = getEntityType();

        List<SearchParameter> params = new ArrayList<>();
        if(!requestParams.isEmpty()) {
            for (Map.Entry<String, String> param : requestParams.entrySet()) {
                if (!REQUEST_PARAMETER_NAME_FOR_PAGE_NUMBER.equals(param.getKey()) && !REQUEST_PARAMETER_NAME_FOR_PAGE_SIZE.equals(param.getKey()) && !REQUEST_PARAMETER_NAME_FOR_ORDER_BY.equals(param.getKey())) {
                    Class fieldType = getFieldType(entityType, param.getKey());
                    if (fieldType == null)
                        throw new BadRequestException("Invalid search parameter: the path \"" + param.getKey() + "\" does not represent a valid attribute for resources of type \"" + entityType.getSimpleName() + "\"");
                    params.add(buildSearchParameter(param.getKey(), param.getValue(), fieldType));
                }
            }
        }
        return params;
    }

    /**
     * <p>Extrai e entrega uma coleção de parâmetros de busca a partir dos parâmetros enviados na requisição HTTP,
     * validando-os com base na classe da entidade genérica <code>E</code> do controller. O método verifica um por um
     * dos parâmetros da requisição HTTP, validando tanto o seu nome quanto seu conteúdo, de forma a certificar-se que
     * trata-se de um parâmetro válido em relação à classe de entidade, criando uma instância de
     * {@link SearchParameter}.</p>
     *
     * <p>A validação dos parâmetros consiste em verificar:</p>
     * <ul>
     *     <li>se o nome do parâmetro é um caminho que faz referência a um atributo válido da entidade</li>
     *     <li>se o conteúdo do parâmetro está no formato operador:valor</li>
     *     <li>se o valor do conteúdo do parâmetro corresponde ao tipo do atributo que o parâmetro referencia</li>
     * </ul>
     *
     * <p>Qualquer erro na verificação de algum dos parâmetros encerra o processamento, lançando uma exceção que informa
     * qual o parâmetro com problema e qual o motivo do erro.</p>
     *
     * @param requestParams mapa de parâmetros da requisição HTTP, de onde os parâmetros de busca serão extraídos
     * @return coleção de parâmetros de busca extraídos dos parâmetros da requisição HTTP
     *
     * @see #buildSearchParameter(String, String, Class)
     * @see #getSearchParameters(Class, Map)
     */
    protected List<SearchParameter> getSearchParameters(Map<String,String> requestParams) {
        return getSearchParameters(null, requestParams);
    }

    /**
     * <p>Extrai e entrega uma coleção de parâmetros de ordenação a partir dos parâmetros enviados na requisição HTTP,
     * validando-os com base na classe de entidade informada. O método procura pelo parâmetro de nome "orderBy" e, caso
     * encontre, faz o tratamento do(s) valore(s) de ordenação, de forma a certificar-se que cada valor trata-se de um
     * parâmetro válido em relação à classe de entidade, criando uma instância de {@link SortParameter}. Se nenhuma
     * classe for informada, a classe da entidade genérica <code>E</code> do controller é utilizada como referência de
     * validação.</p>
     *
     * <p>O tratamento consiste em validar o conteúdo do parâmetro, de modo que:</p>
     * <ul>
     *     <li>o conteúdo deve ter um único valor ou uma sequência de valores separados por vírgula</li>
     *     <li>cada valor da sequência deve ser um caminho que faz referência a um atributo válido da entidade</li>
     *     <li>cada valor da sequência pode ou não conter o caractere "-" (sinal de menos), indicando ordenação descendente</li>
     * </ul>
     *
     * <p>Qualquer erro na verificação de algum dos valores da sequência encerra o processamento, lançando uma exceção
     * que informa qual o valor com problema e qual o motivo do erro.</p>
     *
     * @param entityType tipo da entidade, pra fins de validação dos valores do parâmetro de ordenação
     * @param requestParams mapa de parâmetros da requisição HTTP, de onde o parâmetros de ordenação serão extraídos
     * @return coleção de parâmetros de ordenação extraídos dos parâmetros da requisição HTTP, ou <code>null</code>, no
     * caso de não haver um parâmetro de ordenação na requisição
     */
    @Nullable
    protected LinkedList<SortParameter> getSortParameters(Class entityType, Map<String,String> requestParams) {
        if(requestParams == null) throw new IllegalArgumentException("A valid request parameters map must be provided");
        if(entityType == null) entityType = getEntityType();

        if(!requestParams.isEmpty() && requestParams.containsKey(REQUEST_PARAMETER_NAME_FOR_ORDER_BY)) {
            String requestParam = requestParams.get(REQUEST_PARAMETER_NAME_FOR_ORDER_BY);
            if(requestParam != null && !requestParam.trim().isEmpty()) {
                LinkedList<SortParameter> sortParameters = new LinkedList<>();
                String[] orderByParams = requestParam.split(",");
                for(int index = 0; index < orderByParams.length; index++) {
                    String param = orderByParams[index];
                    if(param != null && !param.trim().isEmpty()) {
                        SortParameter parameter = new SortParameter(param);
                        Class fieldType = getFieldType(entityType, parameter.getAttribute());
                        if (fieldType == null) throw new BadRequestException("Invalid sort parameter: the path \"" + parameter.getAttribute() + "\" does not represent a valid attribute for resources of type \"" + entityType.getSimpleName() + "\"");
                        sortParameters.add(parameter);
                    }
                }
                return sortParameters;
            }
        }
        return null;
    }

    /**
     * <p>Extrai e entrega uma coleção de parâmetros de ordenação a partir dos parâmetros enviados na requisição HTTP,
     * validando-os com base na classe da entidade genérica <code>E</code> do controller. O método procura pelo
     * parâmetro de nome "orderBy" e, caso encontre, faz o tratamento do(s) valore(s) de ordenação, de forma a
     * certificar-se que cada valor trata-se de um parâmetro válido em relação à classe de entidade, criando uma
     * instância de {@link SortParameter}.</p>
     *
     * <p>O tratamento consiste em validar o conteúdo do parâmetro, de modo que:</p>
     * <ul>
     *     <li>o conteúdo deve ter um único valor ou uma sequência de valores separados por vírgula</li>
     *     <li>cada valor da sequência deve ser um caminho que faz referência a um atributo válido da entidade</li>
     *     <li>cada valor da sequência pode ou não conter o caractere "-" (sinal de menos), indicando ordenação descendente</li>
     * </ul>
     *
     * <p>Qualquer erro na verificação de algum dos valores da sequência encerra o processamento, lançando uma exceção
     * que informa qual o valor com problema e qual o motivo do erro.</p>
     *
     * @param requestParams mapa de parâmetros da requisição HTTP, de onde o parâmetros de ordenação serão extraídos
     * @return coleção de parâmetros de ordenação extraídos dos parâmetros da requisição HTTP, ou <code>null</code>, no
     * caso de não haver um parâmetro de ordenação na requisição
     */
    @Nullable
    protected LinkedList<SortParameter> getSortParameters(Map<String,String> requestParams) {
        return getSortParameters(null, requestParams);
    }

    /**
     * <p>Informa o número da página requisitada pelos parâmetros enviados na requisição HTTP. O método procura pelo
     * parâmetro de nome "pageNumber", retornando seu valor, caso exista, desde que o valor em questão seja um número
     * inteiro maior que zero.</p>
     *
     * @param requestParams mapa de parâmetros da requisição HTTP, de onde o número da página será extraído
     * @return número inteiro maior que zero, que representa o número da página requisitada
     */
    @Nullable
    protected Integer getPageNumber(Map<String,String> requestParams) {
        if(requestParams == null) throw new IllegalArgumentException("A valid request parameters map must be provided");

        Integer pageNumber = null;
        if(!requestParams.isEmpty() && requestParams.containsKey(REQUEST_PARAMETER_NAME_FOR_PAGE_NUMBER)) {
            String requestParam = requestParams.get(REQUEST_PARAMETER_NAME_FOR_PAGE_NUMBER);
            try {
                pageNumber = Integer.parseInt(requestParam);
            } catch (Exception e) {
                // do nothing
            }
            if(pageNumber == null || pageNumber <=0) throw new BadRequestException("Invalid page number: value \"" + requestParam + "\" is not a valid number for pagination");
        }
        return pageNumber;
    }

    /**
     * <p>Informa o tamanho da página (quantidade de itens) requisitada pelos parâmetros enviados na requisição HTTP. O
     * método procura pelo parâmetro de nome "pageSize", retornando seu valor, caso exista, desde que o valor em
     * questão seja um número inteiro maior que zero.</p>
     *
     * @param requestParams mapa de parâmetros da requisição HTTP, de onde o tamanho da página será extraído
     * @return número inteiro maior que zero, que representa o tamanho da página requisitada
     */
    @Nullable
    protected Integer getPageSize(Map<String,String> requestParams) {
        if(requestParams == null) throw new IllegalArgumentException("A valid request parameters map must be provided");

        Integer pageSize = null;
        if(!requestParams.isEmpty() && requestParams.containsKey(REQUEST_PARAMETER_NAME_FOR_PAGE_SIZE)) {
            String requestParam = requestParams.get(REQUEST_PARAMETER_NAME_FOR_PAGE_SIZE);
            try {
                pageSize = Integer.parseInt(requestParam);
            } catch (Exception e) {
                // do nothing
            }
            if(pageSize == null || pageSize <= 0) throw new BadRequestException("Invalid page size: value \"" + requestParam + "\" is not a valid number for pagination");
        }
        return pageSize;
    }

    @Nullable
    private Class getFieldType(Class entityType, String fieldPath) {
        try {
            Class fieldType;
            if(fieldPath.contains(".")) {
                fieldType = entityType;
                String[] fieldPathParts = fieldPath.split("\\.");
                for (String fieldPathPart: fieldPathParts) {
                    fieldType = fieldType.getDeclaredField(fieldPathPart).getType();
                }
            } else {
                fieldType = entityType.getDeclaredField(fieldPath).getType();
            }
            return fieldType;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    private SearchParameter buildSearchParameter(String attribute, String requestValue, Class castingTo) {
        if(attribute == null || attribute.trim().isEmpty()) throw new IllegalArgumentException("The attribute must be specified");
        if(requestValue == null || requestValue.trim().isEmpty()) throw new IllegalArgumentException("The request value must be specified");

        SearchParameter.Operator operator = SearchParameter.Operator.EQUAL;
        String stringValue = requestValue;

        Pattern pattern = Pattern.compile("(" + SearchParameter.OPERATORS_REGEX + ")(.*)");
        Matcher matcher = pattern.matcher(requestValue);
        while (matcher.find()) {
            try {
                operator = SearchParameter.Operator.byValue(matcher.group(1));
            }catch (Exception e) {
                throw new BadRequestException("Invalid parameter operator: The value \"" + matcher.group(1) + "\" found at the attribute \"" + attribute + "\" does not represent a valid operator");
            }
            stringValue = matcher.group(2);
        }

        Object value = getConvertedValue(operator, stringValue, castingTo);
        if(value == null && !operator.equals(SearchParameter.Operator.IS_NULL) && !operator.equals(SearchParameter.Operator.IS_NOT_NULL)) {
            throw new BadRequestException("Invalid parameter value: The value \"" + stringValue + "\" does not represent a valid content for the attribute \"" + attribute + "\" using the \"" + operator.getValue() + "\" operator");
        }
        return new SearchParameter(attribute, operator, value);
    }

    private SearchParameter buildSearchParameter(String attribute, String requestValue) {
        return buildSearchParameter(attribute, requestValue, null);
    }

    @Nullable
    private Object getConvertedValue(SearchParameter.Operator operator, String stringValue, Class castingTo) {
        try {
            if((SearchParameter.Operator.BETWEEN.equals(operator) || SearchParameter.Operator.IN.equals(operator) || SearchParameter.Operator.NOT_IN.equals(operator)) && stringValue.contains(",")) {
                return castArrayOfValuesTo(stringValue, castingTo);
            } else {
                return castSingleValueTo(stringValue, castingTo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    private Object[] castArrayOfValuesTo(String arrayValues, Class castingTo) {
        String[] stringValues = arrayValues.split(",");
        if (castingTo == null || String.class.equals(castingTo)) {
            return stringValues;
        } else {
            Object[] result = new Object[stringValues.length];
            for (int count = 0; count < stringValues.length; count++) {
                Object objectValue = castSingleValueTo(stringValues[count], castingTo);
                if(objectValue != null) {
                    result[count] = objectValue;
                }
            }
            return result.length > 0 ? result : null;
        }
    }

    @Nullable
    private Object castSingleValueTo(String value, Class castingTo) {
        if(castingTo == null || String.class.equals(castingTo)) {
            return value;
        } else if(castingTo.isEnum() || Enum.class.isAssignableFrom(castingTo)) {
            return Enum.valueOf(castingTo, value.toUpperCase());
        } else if(Integer.class.equals(castingTo) || int.class.equals(castingTo)) {
            return new Integer(value);
        } else if(Long.class.equals(castingTo) || long.class.equals(castingTo)) {
            return new Long(value);
        } else if(Double.class.equals(castingTo) || double.class.equals(castingTo)) {
            return new Double(value);
        } else if(Boolean.class.equals(castingTo) || boolean.class.equals(castingTo)) {
            if(value.equals("1") || value.equalsIgnoreCase("true"))
                return Boolean.TRUE;
            else if(value.equals("0") || value.equalsIgnoreCase("false"))
                return Boolean.FALSE;
        } else if(Date.class.equals(castingTo)) {
            Calendar calendar = DatatypeConverter.parseDateTime(value);
            Date date = calendar.getTime();
            return date;
        }
        return null;
    }

    private Class getEntityType() {
        if(entityType == null) {
            ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
            entityType = (Class<E>) type.getActualTypeArguments()[0];
        }
        return entityType;
    }

    private boolean updateId(E data, P value) {
        Field field = dao.getIdField();
        try {
            field.setAccessible(true);
            field.set(data, value);
            return true;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
