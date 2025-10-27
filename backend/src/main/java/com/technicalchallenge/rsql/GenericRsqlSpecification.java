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

// Dynamically builds WHERE clauses (predicates) for any entity type T at runtime
public class GenericRsqlSpecification<T> implements Specification<T> {

    private String property;
    private ComparisonOperator operator;
    private List<String> arguments;

    public GenericRsqlSpecification(String property, ComparisonOperator operator, List<String> arguments) {
        this.property = property;
        this.operator = operator;
        this.arguments = arguments;
    }

    //This method figures out how to reach the path being filtered
    // Root<T> root is the root entity being queried. eg. Root<Trade>
    private Path<?> getPath(Root<T> root, String property) {

        // for nested properties like counterparty.name...
        if (property.contains(".")) {
            //splits string at the full stops
            String[] parts = property.split("\\.");

            jakarta.persistence.criteria.From<?, ?> join = root;

            for (int i = 0; i < parts.length - 1; i++) {
                // perform left joins until we get to the final column
                join = join.join(parts[i], jakarta.persistence.criteria.JoinType.LEFT);
            }

            return join.get(parts[parts.length - 1]);
        } else {
            // if the property is simple e.g. tradeID
            return root.get(property);
        }
    }


    @Override
    // Build the SQL condition
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        try {
            // Get the field path and convert string arguments into correct Java type
            Path<?> path = getPath(root, property);
            List<Object> args = castArguments(root);
            Object argument = args.get(0);

            // Look up the operation and build a predicate with the correct criteria builder method.
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
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error building predicate for property: " + property + " — " + e.getMessage(), e);
        }
    }


    private List<Object> castArguments(final Root<T> root) {
        Path<?> path = getPath(root, property);
        Class<?> type = path.getJavaType(); 

        return arguments.stream().map(arg -> {
            try {
                if (type.equals(Integer.class)) return Integer.parseInt(arg);
                if (type.equals(Long.class)) return Long.parseLong(arg);
                if (type.equals(Boolean.class)) return Boolean.parseBoolean(arg);
                if (type.equals(Double.class)) return Double.parseDouble(arg);
                return arg;
            } catch (Exception e) {
                throw new RuntimeException("Invalid value for property '" + property + "': " + arg);
            }
        }).collect(Collectors.toList());
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