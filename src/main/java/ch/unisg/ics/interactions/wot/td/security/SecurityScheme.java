package ch.unisg.ics.interactions.wot.td.security;

import java.util.Optional;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;

import ch.unisg.ics.interactions.wot.td.vocabularies.WoTSec;

public abstract class SecurityScheme {
  
  public abstract String getSchemaType();
  
  public static Optional<SecurityScheme> readScheme(String type, Model model, Resource node) {
    switch (type) {
      case WoTSec.NoSecurityScheme:
        return Optional.of(new NoSecurityScheme());
      case WoTSec.APIKeySecurityScheme:
        return Optional.of(new APIKeySecurityScheme(model,node));
    }
    
    return Optional.empty();
  }
}
