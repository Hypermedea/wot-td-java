package ch.unisg.ics.interactions.wot.td.bindings.coap;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.bindings.Operation;
import ch.unisg.ics.interactions.wot.td.bindings.ProtocolBinding;
import ch.unisg.ics.interactions.wot.td.vocabularies.COV;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TDCoapBinding implements ProtocolBinding {

  private final static String COAP_PROTOCOL = "CoAP";

  private final static Map<String, String> DEFAULT_METHODS = new HashMap<>();

  private final static Map<String, String> DEFAULT_SUBPROTOCOLS = new HashMap<>();

  static {
    DEFAULT_METHODS.put(TD.readProperty, "GET");
    DEFAULT_METHODS.put(TD.observeProperty, "GET");
    DEFAULT_METHODS.put(TD.unobserveProperty, "GET");
    DEFAULT_METHODS.put(TD.writeProperty, "PUT");
    DEFAULT_METHODS.put(TD.invokeAction, "POST");

    DEFAULT_SUBPROTOCOLS.put(TD.observeProperty, COV.observe);
    DEFAULT_SUBPROTOCOLS.put(TD.unobserveProperty, COV.observe);
  }

  @Override
  public String getProtocol() {
    return COAP_PROTOCOL;
  }

  @Override
  public Optional<String> getDefaultMethod(String operationType) {
    if (DEFAULT_METHODS.containsKey(operationType)) return Optional.of(DEFAULT_METHODS.get(operationType));
    else return Optional.empty();
  }

  @Override
  public Optional<String> getDefaultSubProtocol(String operationType) {
    if (DEFAULT_SUBPROTOCOLS.containsKey(operationType)) return Optional.of(DEFAULT_SUBPROTOCOLS.get(operationType));
    else return Optional.empty();
  }

  @Override
  public Operation bind(Form form, String operationType) {
    return new TDCoapRequest(form, operationType);
  }

}
