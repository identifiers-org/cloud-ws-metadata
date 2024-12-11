package org.identifiers.cloud.ws.metadata.retrivers;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
public abstract class SparqlBasedMetadataRetriever implements MetadataRetriver {
    final SPARQLRepository repository;
    protected SparqlBasedMetadataRetriever(String sparqlEndpoint) {
        this.repository = new SPARQLRepository(sparqlEndpoint);
        this.repository.init();
    }

    TupleQuery getTupleQuery(RepositoryConnection connection,
                             Resource queryFile, String ...bindings) throws IOException {
        assert bindings.length % 2 == 0 : "Bindings must come in pairs";
        String queryStr = queryFile.getContentAsString(StandardCharsets.UTF_8);
        var preparedQuery = connection.prepareTupleQuery(queryStr);
        var iter = Arrays.stream(bindings).iterator();
        while (iter.hasNext()) {
            String varname = iter.next();
            Literal value = Values.literal(iter.next());
            preparedQuery.setBinding(varname, value);
        }
        return preparedQuery;
    }

    @Nullable
    protected List<BindingSet> runTupleQuery(Resource queryFile, String ...bindings) {
        try (var connection = this.repository.getConnection()) {
            var query = getTupleQuery(connection, queryFile, bindings);
            return QueryResults.asList(query.evaluate());
        } catch (IOException e) {
            log.error("Failed to run tuple query: {}", e.getMessage());
            return null;
        }
    }

    protected void runTupleQuery(Resource queryFile,
                                 TupleQueryResultHandler resultHandler,
                                 String ...bindings) {
        try (var connection = this.repository.getConnection()) {
            var query = getTupleQuery(connection, queryFile, bindings);
            query.evaluate(resultHandler);
        } catch (IOException e) {
            log.error("Failed to run tuple query: {}", e.getMessage());
        }
    }

    @Nullable
    protected Boolean runBoolQuery(Resource queryFile) throws IOException {
        try (var connection = this.repository.getConnection()) {
            String queryStr = queryFile.getContentAsString(StandardCharsets.UTF_8);
            var preparedQuery = connection.prepareBooleanQuery(queryStr);
            return preparedQuery.evaluate();
        } catch (IOException e) {
            log.error("Failed to run bool query: {}", e.getMessage());
            return null;
        }
    }

    @Nullable
    protected GraphQueryResult runGraphQuery(Resource queryFile) throws IOException {
        try (var connection = this.repository.getConnection()) {
            String queryStr = queryFile.getContentAsString(StandardCharsets.UTF_8);
            var preparedQuery = connection.prepareGraphQuery(queryStr);
            return preparedQuery.evaluate();
        } catch (IOException e) {
            log.error("Failed to run graph query: {}", e.getMessage());
            return null;
        }
    }
}
