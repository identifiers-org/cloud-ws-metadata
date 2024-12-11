package org.identifiers.cloud.ws.metadata.retrivers;

import org.identifiers.cloud.libapi.models.resolver.ParsedCompactIdentifier;

import java.util.Map;

public interface MetadataRetriver {
   public boolean isEnabled(ParsedCompactIdentifier compactIdentifier);

   public Object getRawMetaData(ParsedCompactIdentifier compactIdentifier);

   public Map<String,String> getParsedMetaData(ParsedCompactIdentifier compactIdentifier);
}
