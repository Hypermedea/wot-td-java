package ch.unisg.ics.interactions.wot.td.bindings.coap;

import ch.unisg.ics.interactions.wot.td.affordances.Form;
import ch.unisg.ics.interactions.wot.td.bindings.NoResponseException;
import ch.unisg.ics.interactions.wot.td.bindings.Operation;
import ch.unisg.ics.interactions.wot.td.bindings.Response;
import ch.unisg.ics.interactions.wot.td.bindings.ResponseCallback;
import ch.unisg.ics.interactions.wot.td.bindings.http.TDHttpRequest;
import ch.unisg.ics.interactions.wot.td.clients.UriTemplate;
import ch.unisg.ics.interactions.wot.td.schemas.ArraySchema;
import ch.unisg.ics.interactions.wot.td.schemas.DataSchema;
import ch.unisg.ics.interactions.wot.td.schemas.ObjectSchema;
import ch.unisg.ics.interactions.wot.td.vocabularies.COV;
import ch.unisg.ics.interactions.wot.td.vocabularies.TD;
import com.google.gson.Gson;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper for constructing and executing a CoAP request based on a given <code>ThingDescription</code>.
 * When constructing the request, clients can set payloads that conform to a <code>DataSchema</code>.
 */
public class TDCoapRequest implements Operation {
  private final static Logger LOGGER = Logger.getLogger(TDHttpRequest.class.getCanonicalName());

  private final Form form;
  private final Request request;

  private final TDCoapHandler handler;

  private final List<CoapClient> executors = new ArrayList<>();
  private final ReentrantLock executorsLock = new ReentrantLock();

  private final String target;

  public TDCoapRequest(Form form, String operationType) {
    this.form = form;
    this.target = form.getTarget();

    Optional<String> methodName = form.getMethodName(operationType);
    Optional<String> subProtocol = form.getSubProtocol(operationType);

    if (methodName.isPresent()) {
      this.request = new Request(CoAP.Code.valueOf(methodName.get()));
      this.request.setURI(this.target);
    } else {
      throw new IllegalArgumentException("No default binding for the given operation type: "
        + operationType);
    }

    this.handler = new TDCoapHandler();

    if (subProtocol.isPresent() && subProtocol.get().equals(COV.observe)) {
      if (operationType.equals(TD.observeProperty)) {
        this.request.setObserve();
      }
      if (operationType.equals(TD.unobserveProperty)) {
        this.request.setObserveCancel();
      }
    }
    this.request.getOptions().setContentFormat(MediaTypeRegistry.parse(form.getContentType()));
  }

  public TDCoapRequest(Form form, String operationType, Map<String, DataSchema> uriVariables, Map<String, Object> values) {
    this.form = form;
    this.target = new UriTemplate(form.getTarget()).createUri(uriVariables, values);

    Optional<String> methodName = form.getMethodName(operationType);
    Optional<String> subProtocol = form.getSubProtocol(operationType);

    if (methodName.isPresent()) {
      this.request = new Request(CoAP.Code.valueOf(methodName.get()));
      this.request.setURI(this.target);
    } else {
      throw new IllegalArgumentException("No default binding for the given operation type: "
        + operationType);
    }

    this.handler = new TDCoapHandler();

    if (subProtocol.isPresent() && subProtocol.get().equals(COV.observe)) {
      if (operationType.equals(TD.observeProperty)) {
        this.request.setObserve();
      }
      if (operationType.equals(TD.unobserveProperty)) {
        this.request.setObserveCancel();
      }
    }
    this.request.getOptions().setContentFormat(MediaTypeRegistry.parse(form.getContentType()));
  }

  public String getTarget() {
    return target;
  }

  @Override
  public void sendRequest() {
    CoapClient client = new CoapClient();
    client.advanced(handler, request);
    addExecutor(client);
  }

  @Override
  public void sendRequest(DataSchema schema, Object payload) {
    setPayload(schema, payload);
    sendRequest();
  }

  @Override
  public Response getResponse() throws NoResponseException {
    Optional<TDCoapResponse> r = handler.getLastResponse();

    if (r.isPresent()) return r.get();
    else throw new NoResponseException();
  }

  @Override
  public void registerResponseCallback(ResponseCallback callback) {
    handler.registerResponseCallback(callback);
  }

  @Override
  public void unregisterResponseCallback(ResponseCallback callback) {
    handler.unregisterResponseCallback(callback);
  }

  public void shutdownExecutors() {
    try {
      executorsLock.lock();
      if (!executors.isEmpty()) {
        for (CoapClient client : executors) {
          client.shutdown();
        }
        executors.clear();
      }
    } finally {
      executorsLock.unlock();
    }
  }

  public TDCoapRequest addOption(String key, String value) {
    // TODO Support CoAP options e.g. for observation flag
    return null;
  }

  public void setPayload(DataSchema schema, Object payload) {
    if (payload instanceof Map) setObjectPayload((ObjectSchema) schema, (Map<String, Object>) payload);
    else if (payload instanceof List) setArrayPayload((ArraySchema) schema, (List<Object>) payload);
    else if (payload instanceof String) setPrimitivePayload(schema, (String) payload);
    else if (payload instanceof Boolean) setPrimitivePayload(schema, (Boolean) payload);
    else if (payload instanceof Long) setPrimitivePayload(schema, (Long) payload);
    else if (payload instanceof Double) setPrimitivePayload(schema, (Double) payload);
    // TODO else, throw payload type error
    // TODO add common interface to HTTP, CoAP(, and any other binding)?
  }

  public TDCoapRequest setPrimitivePayload(DataSchema dataSchema, boolean value)
    throws IllegalArgumentException {
    if (dataSchema.getDatatype().equals(DataSchema.BOOLEAN)) {
      request.setPayload(String.valueOf(value));
    } else {
      throw new IllegalArgumentException("The payload's datatype does not match BooleanSchema "
        + "(payload datatype: " + dataSchema.getDatatype() + ")");
    }

    return this;
  }

  public TDCoapRequest setPrimitivePayload(DataSchema dataSchema, String value)
    throws IllegalArgumentException {
    if (dataSchema.getDatatype().equals(DataSchema.STRING)) {
      request.setPayload(String.valueOf(value));
    } else {
      throw new IllegalArgumentException("The payload's datatype does not match StringSchema "
        + "(payload datatype: " + dataSchema.getDatatype() + ")");
    }

    return this;
  }

  public TDCoapRequest setPrimitivePayload(DataSchema dataSchema, long value)
    throws IllegalArgumentException {
    if (dataSchema.getDatatype().equals(DataSchema.INTEGER)
      || dataSchema.getDatatype().equals(DataSchema.NUMBER)) {
      request.setPayload(String.valueOf(value));
    } else {
      throw new IllegalArgumentException("The payload's datatype does not match IntegerSchema or "
        + "NumberSchema (payload datatype: " + dataSchema.getDatatype() + ")");
    }

    return this;
  }

  public TDCoapRequest setPrimitivePayload(DataSchema dataSchema, double value)
    throws IllegalArgumentException {
    if (dataSchema.getDatatype().equals(DataSchema.NUMBER)) {
      request.setPayload(String.valueOf(value));
    } else {
      throw new IllegalArgumentException("The payload's datatype does not match NumberSchema "
        + "(payload datatype: " + dataSchema.getDatatype() + ")");
    }

    return this;
  }

  /**
   * Sets a payload of type <code>ObjectSchema</code>. The object payload is given as a map where:
   * <ul>
   * <li>a key is a string that represents either a semantic type or an object property name</li>
   * <li>a value can be a primitive, an object represented as a <code>Map&lt;String,Object&gt;</code>
   * (that is, a nested object), or an ordered list of values of type <code>List&lt;Object&gt;</code></li>
   * </ul>
   *
   * @param objectSchema schema to be used for validating the payload and constructing the body of
   *                     the request
   * @param payload      the actual payload
   * @return this <code>TDCoapRequest</code>
   */
  public TDCoapRequest setObjectPayload(ObjectSchema objectSchema, Map<String, Object> payload) {
    if (objectSchema.validate(payload)) {
      Map<String, Object> instance = objectSchema.instantiate(payload);
      String body = new Gson().toJson(instance);
      request.setPayload(body);
    }

    return this;
  }

  /**
   * Sets a payload of type <code>ArraySchema</code>. The payload is given as an ordered list of
   * values of type <code>List&lt;Object&gt;</code>. Values can be primitives, objects represented
   * as <code>Map&lt;String,Object&gt;</code>, or lists of values (that is, nested lists).
   *
   * @param arraySchema schema used for validating the payload and constructing the body of
   *                    the request
   * @param payload     the actual payload
   * @return this <code>TDCoapRequest</code>
   */
  public TDCoapRequest setArrayPayload(ArraySchema arraySchema, List<Object> payload) {
    if (arraySchema.validate(payload)) {
      String body = new Gson().toJson(payload);
      request.setPayload(body);
    }

    return this;
  }

  public String getPayloadAsString() {
    return request.getPayloadString();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("[TDCoapRequest] Method: " + request.getCode().name());

    try {
      builder.append(", Target: " + request.getURI());
      builder.append(", " + request.getOptions().toString());

      if (request.getPayload() != null) {
        builder.append(", Payload: " + request.getPayloadString());
      }
    } catch (UnsupportedOperationException e) {
      LOGGER.log(Level.WARNING, e.getMessage());
    }

    return builder.toString();
  }

  Request getRequest() {
    return this.request;
  }

  // TODO expose CoapObserveRelation if cov:observe declared in form

  private void addExecutor(CoapClient client) {
    try {
      executorsLock.lock();
      if (client != null && !executors.contains(client)) {
        executors.add(client);
      }
    } finally {
      executorsLock.unlock();
    }
  }

  private void removeExecutor(CoapClient client) {
    try {
      executorsLock.lock();
      if (client != null) {
        executors.remove(client);
      }
    } finally {
      executorsLock.unlock();
    }
  }

}
