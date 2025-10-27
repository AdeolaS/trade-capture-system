// package com.technicalchallenge.rsql;
// import cz.jirutka.rsql.parser.ast.ComparisonOperator;
// import cz.jirutka.rsql.parser.ast.RSQLOperators;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;

// import jakarta.persistence.criteria.CriteriaBuilder;
// import jakarta.persistence.criteria.CriteriaQuery;
// import jakarta.persistence.criteria.Path;
// import jakarta.persistence.criteria.Expression;
// import jakarta.persistence.criteria.Predicate;
// import jakarta.persistence.criteria.Root;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// import java.util.List;

// @ExtendWith(MockitoExtension.class)
// public class GenericRsqlSpecificationTest {
    
//     @Mock
//     private Root<TestTradeObject> root;

//     @Mock
//     private Path<Object> path;

//     @Mock
//     private CriteriaBuilder builder;

//     @Mock
//     private CriteriaQuery<?> query;

//     @Mock
//     private Predicate predicate;

//     private GenericRsqlSpecification<TestTradeObject> specification;

//     // Simple test entity with basic fields.
//     static class TestTradeObject {
//         private Long id;
//         private String bookName;
//         private String counterpartyName;
//     }

//     @BeforeEach
//     void setUp() {
//         when(root.get(anyString())).thenReturn(path);
//         doReturn(String.class).when(path).getJavaType();
//     }

//     @Test
//     void testToPredicate_SimpleProperty_EqualOperation() {
//         // GIVEN
//         // use known operator instance from parser so operator mapping is found
//         ComparisonOperator equalOp = RSQLOperators.EQUAL;
//         specification = new GenericRsqlSpecification<>("bookName", equalOp, List.of("RATES-BOOK-1"));
 
//         doReturn(mock(Expression.class)).when(builder).lower(any(Expression.class));
//         doReturn(predicate).when(builder).like(any(Expression.class), anyString());
 
//         // WHEN
//         Predicate result = specification.toPredicate(root, query, builder);
 
//         // THEN
//         assertNotNull(result);
//         verify(root).get("bookName");
//         // don't assert exact pattern (implementation may add wildcards / lower-case),
//         // just verify a like call was performed with some string
//         verify(builder).like(any(), anyString());
//     }
// }
// // 