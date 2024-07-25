package musico.services.databases.config;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.sparqlbuilder.constraint.Expressions;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.Variable;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatternNotTriples;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.GraphPatterns;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;
import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf;

import java.lang.reflect.Field;
import java.util.*;

import static musico.services.databases.config.OntEntityField.DataType.OBJECT;

public interface OntEntity {

    IRI getIRI();

    Variable getVar();

    default List<TriplePattern> buildInsertQueryGraphPattern(OntEntity clazz) {
        Object entity;
        if (clazz.getIRI().toString().contains("null")) {
            entity = clazz.getVar();
        } else {
            entity = clazz.getIRI();
        }
        List<TriplePattern> res = new ArrayList<>();
        for (Field field : clazz.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            OntEntityField annotation = field.getAnnotation(OntEntityField.class);
            try {
                if (annotation != null && field.get(clazz) != null) {
                    switch (annotation.type()) {
                        case OBJECT:
                            IRI[] objs = getOntEntityIRIsArray(field.get(clazz));
                            if (objs.length == 0) {
                                continue;
                            }
                            if (entity instanceof Variable) {
                                res.add(
                                        GraphPatterns.tp(
                                                (Variable) entity,
                                                Values.iri(Objects.requireNonNull(OntologyModel.getPredicate(annotation.pred()))),
                                                (objs
                                                )
                                        )
                                );
                            } else {
                                res.add(
                                        GraphPatterns.tp(
                                                (Resource) entity,
                                                Values.iri(Objects.requireNonNull(OntologyModel.getPredicate(annotation.pred()))),
                                                (objs
                                                )
                                        )
                                );
                            }
                            break;
                        case DATA:
                            Object objValue = field.get(clazz);
                            if (entity instanceof Variable) {
                                res.add(
                                        GraphPatterns.tp(
                                                (Variable) entity,
                                                Values.iri(Objects.requireNonNull(OntologyModel.getPredicate(annotation.pred()))),
                                                Values.literal(objValue))
                                );
                            } else {
                                res.add(
                                        GraphPatterns.tp(
                                                (Resource) entity,
                                                Values.iri(Objects.requireNonNull(OntologyModel.getPredicate(annotation.pred()))),
                                                Values.literal(objValue))
                                );
                            }
                            break;
                    }
                }
            } catch (IllegalAccessException e) {
                System.out.println("Error while accessing field: " + e.getMessage());
            }
        }
        return res;
    }

    default GraphPatternNotTriples buildGenericQueryGraphPattern(OntEntity clazz) {
        Object entity;
        if (clazz.getIRI().toString().contains("null")) {
            entity = clazz.getVar();
        } else {
            entity = clazz.getIRI();
        }
        GraphPatternNotTriples res = GraphPatterns.and();
        List<GraphPatternNotTriples> patterns = new ArrayList<>();
        for (Field field : clazz.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            OntEntityField annotation = field.getAnnotation(OntEntityField.class);
            try {
                // TODO: Hey Jude, Make it better
                if (annotation != null && field.get(clazz) != null) {
                    if (field.get(clazz) instanceof Set<?>) {
                        if (!((Set<?>) field.get(clazz)).isEmpty()) {
                            IRI[] objs = getOntEntityIRIsArray(field.get(clazz));
                            switch (Objects.requireNonNull(annotation).type()) {
                                case OBJECT:
                                    List<TriplePattern> optional = new ArrayList<>();
                                    if (entity instanceof Variable) {
                                        optional.add(
                                                GraphPatterns.tp(
                                                        (Variable) entity,
                                                        Values.iri(Objects.requireNonNull(OntologyModel.getPredicate(annotation.pred()))),
                                                        objs
                                                )
                                        );
                                    } else {
                                        optional.add(
                                                GraphPatterns.tp(
                                                        (Resource) entity,
                                                        Values.iri(Objects.requireNonNull(OntologyModel.getPredicate(annotation.pred()))),
                                                        objs
                                                )
                                        );
                                    }
                                    for (TriplePattern tp : optional) {
                                        patterns.add(GraphPatterns.and(tp));
                                    }
                                    optional.clear();
                                    for (IRI obj : objs) {
                                        optional.add(
                                                GraphPatterns.tp(
                                                        obj,
                                                        Values.iri(Objects.requireNonNull(OntologyModel.getPredicate("schema1:name"))),
                                                        SparqlBuilder.var(field.getName() + "_name")
                                                )
                                        );
                                    }
                                    patterns.add(GraphPatterns.optional(optional.toArray(new TriplePattern[0])));
                                    break;
                                case DATA:
                                    Object objValue = field.get(clazz);
                                    genLiteralEntityDoublePattern(entity, patterns, annotation, objValue);
                                    break;
                            }
                        } else { // FIELD IS EMPTY
                            genVariableDoubleEntityPattern(entity, patterns, field, annotation);
                        }
                    } else { // FIELD NOT A SET
                        switch (annotation.type()) {
                            case DATA: {
                                Object objValue = field.get(clazz);
                                if (!field.getName().equals("bpm")) {
                                    genLiteralEntityDoublePattern(entity, patterns, annotation, objValue);
                                } else {
                                    Integer bpm = (Integer) objValue;
                                    if (entity instanceof Variable) {
                                        patterns.add(
                                                GraphPatterns.optional(
                                                                GraphPatterns.tp(
                                                                        (Variable) entity,
                                                                        Values.iri(Objects.requireNonNull(OntologyModel.getPredicate(annotation.pred()))),
                                                                        SparqlBuilder.var(field.getName())
                                                                )
                                                        ).filter(Expressions.gte(SparqlBuilder.var(field.getName()), Rdf.literalOf(bpm - 3)))
                                                        .filter(Expressions.lte(SparqlBuilder.var(field.getName()), Rdf.literalOf(bpm + 3))
                                                        )
                                        );
                                    } else {
                                        patterns.add(
                                                GraphPatterns.optional(
                                                        GraphPatterns.tp(
                                                                        (Resource) entity,
                                                                        Values.iri(Objects.requireNonNull(OntologyModel.getPredicate(annotation.pred()))),
                                                                        SparqlBuilder.var(field.getName())
                                                                ).filter(Expressions.gte(SparqlBuilder.var(field.getName()), Rdf.literalOf(bpm - 3)))
                                                                .filter(Expressions.lte(SparqlBuilder.var(field.getName()), Rdf.literalOf(bpm + 3))
                                                                )
                                                )
                                        );
                                    }
                                }
                                break;
                            }
                            case OBJECT: {
                                OntEntity objValue = (OntEntity) field.get(clazz);
                                List<TriplePattern> optional = new ArrayList<>();
                                if (entity instanceof Variable) {
                                    optional.add(
                                            GraphPatterns.tp(
                                                    (Variable) entity,
                                                    Values.iri(Objects.requireNonNull(OntologyModel.getPredicate(annotation.pred()))),
                                                    objValue.getIRI()
                                            )
                                    );
                                } else {
                                    optional.add(
                                            GraphPatterns.tp(
                                                    (Resource) entity,
                                                    Values.iri(Objects.requireNonNull(OntologyModel.getPredicate(annotation.pred()))),
                                                    objValue.getIRI()
                                            )
                                    );
                                }

                                for (TriplePattern tp : optional) {
                                    patterns.add(GraphPatterns.and(tp));
                                }
                                optional.clear();
                                optional.add(
                                        GraphPatterns.tp(
                                                objValue.getIRI(),
                                                Values.iri(Objects.requireNonNull(OntologyModel.getPredicate("schema1:name"))),
                                                SparqlBuilder.var(field.getName() + "_name")
                                        )
                                );
                                patterns.add(GraphPatterns.optional(optional.toArray(new TriplePattern[0])));
                                break;
                            }
                        }
                    }
                } else if (annotation != null) {
                    genVariableDoubleEntityPattern(entity, patterns, field, annotation);
                }
            } catch (IllegalAccessException e) {
                System.out.println("Error while accessing field: " + e.getMessage());
            }
        }
        res.and(patterns.toArray(new GraphPatternNotTriples[0]));
        return res;
    }

    private void genVariableDoubleEntityPattern(Object entity, List<GraphPatternNotTriples> patterns, Field field, OntEntityField annotation) {
        List<TriplePattern> optional = new ArrayList<>();
        if (entity instanceof Variable) {
            optional.add(
                    GraphPatterns.tp(
                            (Variable) entity,
                            Values.iri(Objects.requireNonNull(OntologyModel.getPredicate(annotation.pred()))),
                            SparqlBuilder.var(field.getName())
                    )
            );
        } else {
            optional.add(
                    GraphPatterns.tp(
                            (Resource) entity,
                            Values.iri(Objects.requireNonNull(OntologyModel.getPredicate(annotation.pred()))),
                            SparqlBuilder.var(field.getName())
                    )
            );
        }
        if (annotation.type() == OBJECT) {
            optional.add(
                    GraphPatterns.tp(
                            SparqlBuilder.var(field.getName()),
                            Values.iri(Objects.requireNonNull(OntologyModel.getPredicate("schema1:name"))),
                            SparqlBuilder.var(field.getName() + "_name")
                    )
            );
        }
        patterns.add(GraphPatterns.optional(optional.toArray(new TriplePattern[0])));
    }

    private void genLiteralEntityDoublePattern(Object entity, List<GraphPatternNotTriples> patterns, OntEntityField annotation, Object objValue) {
        if (entity instanceof Variable) {
            patterns.add(
                    GraphPatterns.and(
                            GraphPatterns.tp(
                                    (Variable) entity,
                                    Values.iri(Objects.requireNonNull(OntologyModel.getPredicate(annotation.pred()))),
                                    Values.literal(objValue)))
            );
        } else {
            patterns.add(
                    GraphPatterns.and(
                            GraphPatterns.tp(
                                    (Resource) entity,
                                    Values.iri(Objects.requireNonNull(OntologyModel.getPredicate(annotation.pred()))),
                                    Values.literal(objValue)))
            );
        }
    }


    private IRI[] getOntEntityIRIsArray(Object entitySet) {
    if (entitySet instanceof Set) {
        Set<OntEntity> entities = (Set<OntEntity>) entitySet;
        IRI[] iriArray = new IRI[entities.size()];
        int i = 0;
        for (OntEntity entity : entities) {
            if (entity == null) {
                continue;
            }
            iriArray[i] = entity.getIRI();
            i++;
        }
        return iriArray;
    } else {
        return new IRI[]{((OntEntity) entitySet).getIRI()};
    }
}
}
