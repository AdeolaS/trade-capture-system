package com.technicalchallenge.rsql;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;

import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Path;

public class GenericRsqlSpecification<T> implements Specification<T> {

    private String property;
    private ComparisonOperator operator;
    private List<String> arguments;

    public GenericRsqlSpecification(String property, ComparisonOperator operator, List<String> arguments) {
        this.property = property;
        this.operator = operator;
        this.arguments = arguments;
    }

    private Path<?> getPath(Root<T> root, String property) {
        if (property.contains(".")) {
            String[] parts = property.split("\\.");
            jakarta.persistence.criteria.From<?, ?> join = root;

            for (int i = 0; i < parts.length - 1; i++) {
                join = join.join(parts[i], jakarta.persistence.criteria.JoinType.LEFT);
            }

            return join.get(parts[parts.length - 1]);
        } else {
            return root.get(property);
        }
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        Path<?> path = getPath(root, property);
        List<Object> args = castArguments(root);
        Object argument = args.get(0);

        switch (RsqlSearchOperation.getSimpleOperator(operator)) {
            case EQUAL:
                if (argument instanceof String) {
                    return builder.like(builder.lower(path.as(String.class)), argument.toString().toLowerCase().replace('*', '%'));
                } else {
                    return builder.equal(path, argument);
                }
            case NOT_EQUAL:
                return builder.notEqual(path, argument);
            case GREATER_THAN:
                return builder.greaterThan(path.as(String.class), argument.toString());
            case GREATER_THAN_OR_EQUAL:
                return builder.greaterThanOrEqualTo(path.as(String.class), argument.toString());
            case LESS_THAN:
                return builder.lessThan(path.as(String.class), argument.toString());
            case LESS_THAN_OR_EQUAL:
                return builder.lessThanOrEqualTo(path.as(String.class), argument.toString());
            case IN:
                return path.in(args);
            case NOT_IN:
                return builder.not(path.in(args));
            default:
                return null;
        }
    }

    private List<Object> castArguments(final Root<T> root) {
        
        Class<? extends Object> type = root.get(property).getJavaType();
        
        List<Object> args = arguments.stream().map(arg -> {
            if (type.equals(Integer.class)) {
               return Integer.parseInt(arg);
            } else if (type.equals(Long.class)) {
               return Long.parseLong(arg);
            } else {
                return arg;
            }            
        }).collect(Collectors.toList());

        return args;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public ComparisonOperator getOperator() {
        return operator;
    }

    public void setOperator(ComparisonOperator operator) {
        this.operator = operator;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }
}