package br.com.condo.arch.service;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class SearchSpecification<E> implements Specification<E> {

    private Collection<SearchParameter> params;

    public SearchSpecification() {
        this(null);
    }

    public SearchSpecification(Collection<SearchParameter> params) {
        super();
        this.params = params;
    }

    @Nullable
    @Override
    public Predicate toPredicate(Root<E> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if(params != null && !params.isEmpty()) {
            for (SearchParameter param : params) {
                Path path = buildPath(root, param.getAttribute());

                switch (param.getOperator()) {
                    case EQUAL:
                        predicates.add(criteriaBuilder.equal(path, param.getValue()));
                        break;
                    case NOT_EQUAL:
                        predicates.add(criteriaBuilder.notEqual(path, param.getValue()));
                        break;
                    case LIKE:
                        predicates.add(criteriaBuilder.like(path, "%" + param.getValue().toString() + "%"));
                        break;
                    case GREATER_THAN:
                        if(param.getValue() instanceof Date) {
                            predicates.add(criteriaBuilder.greaterThan(path, (Date)param.getValue()));
                        } else {
                            predicates.add(criteriaBuilder.greaterThan(path, param.getValue().toString()));
                        }
                        break;
                    case GREATER_THAN_OR_EQUAL_TO:
                        if(param.getValue() instanceof Date) {
                            predicates.add(criteriaBuilder.greaterThanOrEqualTo(path, (Date)param.getValue()));
                        } else {
                            predicates.add(criteriaBuilder.greaterThanOrEqualTo(path, param.getValue().toString()));
                        }
                        break;
                    case LESS_THAN:
                        if(param.getValue() instanceof Date) {
                            predicates.add(criteriaBuilder.lessThan(path, (Date)param.getValue()));
                        } else {
                            predicates.add(criteriaBuilder.lessThan(path, param.getValue().toString()));
                        }
                        break;
                    case LESS_THAN_OR_EQUAL_TO:
                        if(param.getValue() instanceof Date) {
                            predicates.add(criteriaBuilder.lessThanOrEqualTo(path, (Date)param.getValue()));
                        } else {
                            predicates.add(criteriaBuilder.lessThanOrEqualTo(path, param.getValue().toString()));
                        }
                        break;
                    case BETWEEN:
                        Object value1 = ((Object[]) param.getValue())[0];
                        Object value2 = ((Object[]) param.getValue())[1];
                        if(value1 instanceof Date && value2 instanceof Date) {
                            predicates.add(criteriaBuilder.between(path, (Date)value1, (Date)value2));
                        } else if(value1 instanceof Integer && value2 instanceof Integer) {
                            predicates.add(criteriaBuilder.between(path, (Integer)value1, (Integer)value2));
                        } else if(value1 instanceof Long && value2 instanceof Long) {
                            predicates.add(criteriaBuilder.between(path, (Long)value1, (Long)value2));
                        } else if(value1 instanceof Double && value2 instanceof Double) {
                            predicates.add(criteriaBuilder.between(path, (Double)value1, (Double)value2));
                        } else {
                            predicates.add(criteriaBuilder.between(path, value1.toString(), value2.toString()));
                        }
                        break;
                    case IN:
                        Collection collectionIn = getParamValueAsCollection(param.getValue());
                        predicates.add(path.in(collectionIn));
                        break;
                    case NOT_IN:
                        Collection collectionNotIn = getParamValueAsCollection(param.getValue());
                        predicates.add(path.in(collectionNotIn).not());
                        break;
                    case IS_NULL:
                        predicates.add(path.isNull());
                        break;
                    case IS_NOT_NULL:
                        predicates.add(path.isNotNull());
                        break;
                }
            }
        }

        if(!predicates.isEmpty()) {
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }
        return null;
    }

    private Path buildPath(Path startingPath, String paramName) {
        if(paramName.contains(".")) {
            String[] paramParts = paramName.split("\\.");
            Path resultingPath = startingPath;
            for (String paramPart: paramParts) {
                resultingPath = resultingPath.get(paramPart);
            }
            return resultingPath;
        } else {
            return startingPath.get(paramName);
        }
    }

    private Collection<Object> getParamValueAsCollection(Object paramValue) {
        if(paramValue instanceof  Collection) {
            return (Collection<Object>) paramValue;
        } else {
            Collection<Object> collection = new ArrayList<>();
            if(paramValue instanceof Object[]) {
                for (Object value : (Object[]) paramValue) {
                    collection.add(value);
                }
            } else {
                collection.add(paramValue);
            }
            return collection;
        }
    }

}
